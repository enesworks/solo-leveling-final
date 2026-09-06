package dev.eness.sololevelingfinal.core.guild;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

@EventBusSubscriber
public class GuildTickHandler {
   private static int counter = 0;

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         if (++counter >= 100) {
            counter = 0;
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
               ServerLevel overworld = server.overworld();
               GuildSavedData data = GuildSavedData.get(overworld);
               resolveDueDeployments(server, overworld, data);
               assignAutoRaids(server, overworld, data);
            }
         }
      }
   }

   public static void resolveDueDeployments(MinecraftServer server, ServerLevel overworld, GuildSavedData data) {
      if (server != null && overworld != null && data != null) {
         long gameTime = overworld.getGameTime();
         boolean changed = false;

         for (GuildData guild : data.allGuilds()) {
            guild.pruneTeamMembers();
            guild.reconcileHunterDeploymentStatus();
            Iterator<GuildDeployment> iterator = guild.deployments.iterator();

            while (iterator.hasNext()) {
               GuildDeployment dep = iterator.next();
               if (gameTime >= dep.completesAt) {
                  guild.awardXp(dep.xpReward);
                  guild.totalClears++;
                  List<ItemStack> crystalRewards = createManaCrystalRewards(guild, dep);
                  int storedCrystalCount = insertRewards(guild, crystalRewards);
                  GuildGateHelper.markGateCleared(overworld, dep.gateEntityUUID);
                  GuildTeam team = guild.getTeam(dep.teamId);
                  if (team != null) {
                     for (UUID hid : team.memberIds) {
                        GuildHunter h = guild.getHunter(hid);
                        if (h != null) {
                           h.status = "idle";
                        }
                     }
                  }

                  ServerPlayer owner = server.getPlayerList().getPlayer(guild.ownerUUID);
                  if (owner != null) {
                     owner.sendSystemMessage(
                        Component.literal(
                           "§a[Guild] Mission complete! Team §e"
                              + dep.teamName
                              + " §areturned from §f"
                              + dep.gateLabel
                              + "§a. Guild gained §e"
                              + dep.xpReward
                              + " XP§a"
                              + (storedCrystalCount > 0 ? " §7and stored §b" + storedCrystalCount + " mana crystals§a!" : "!")
                        )
                     );
                  }

                  iterator.remove();
                  changed = true;
               }
            }

            guild.reconcileHunterDeploymentStatus();
         }

         if (changed) {
            data.markDirty();
         }
      }
   }

   private static void assignAutoRaids(MinecraftServer server, ServerLevel overworld, GuildSavedData data) {
      if (server != null && overworld != null && data != null) {
         List<Entity> availableGates = new ArrayList<>();

         for (Entity gate : overworld.getAllEntities()) {
            if (GuildGateHelper.isDeployableGate(gate)) {
               String gateId = gate.getUUID().toString();
               if (!GuildGateHelper.isGateInteracted(gate)
                  && !GuildGateHelper.isGateReserved(gate)
                  && GuildGateHelper.findGuildRaidingGate(data, gateId) == null) {
                  availableGates.add(gate);
               }
            }
         }

         if (!availableGates.isEmpty()) {
            boolean changed = false;
            long now = overworld.getGameTime();

            label96:
            for (GuildData guild : data.allGuilds()) {
               guild.pruneTeamMembers();
               guild.reconcileHunterDeploymentStatus();
               Iterator var9 = guild.teams.iterator();

               while (true) {
                  if (var9.hasNext()) {
                     GuildTeam team = (GuildTeam)var9.next();
                     if (!team.autoRaidEnabled || team.memberIds.isEmpty() || guild.getDeploymentForTeam(team.id) != null || !teamMembersIdle(guild, team)) {
                        continue;
                     }

                     Entity chosenGate = null;
                     int chosenRank = 0;

                     for (Entity gate : availableGates) {
                        int rank = GuildGateHelper.gateRank(gate);
                        if (rank <= team.autoRaidMaxRank) {
                           chosenGate = gate;
                           chosenRank = rank;
                           break;
                        }
                     }

                     if (chosenGate == null) {
                        continue;
                     }

                     String gateUuid = chosenGate.getUUID().toString();
                     GuildDeployment dep = new GuildDeployment(
                        UUID.randomUUID(),
                        team.id,
                        team.name,
                        GuildGateHelper.gateLabel(chosenGate),
                        chosenRank,
                        gateUuid,
                        now + adjustedDurationTicks(guild, team, chosenRank),
                        adjustedXpReward(guild, team, chosenRank)
                     );
                     guild.deployments.add(dep);

                     for (UUID hid : team.memberIds) {
                        GuildHunter h = guild.getHunter(hid);
                        if (h != null) {
                           h.status = "deployed";
                        }
                     }

                     GuildGateHelper.reserveGate(chosenGate, guild);
                     availableGates.remove(chosenGate);
                     changed = true;
                     ServerPlayer owner = server.getPlayerList().getPlayer(guild.ownerUUID);
                     if (owner != null) {
                        owner.sendSystemMessage(Component.literal("§b[Guild] Auto raid: Team §e" + team.name + " §bdeparted for §f" + dep.gateLabel + "§b."));
                     }

                     if (!availableGates.isEmpty()) {
                        continue;
                     }
                  }

                  guild.reconcileHunterDeploymentStatus();
                  if (availableGates.isEmpty()) {
                     break label96;
                  }
                  break;
               }
            }

            if (changed) {
               data.markDirty();
            }
         }
      }
   }

   private static boolean teamMembersIdle(GuildData guild, GuildTeam team) {
      for (UUID hid : team.memberIds) {
         GuildHunter h = guild.getHunter(hid);
         if (h == null || "deployed".equals(h.status)) {
            return false;
         }
      }

      return true;
   }

   private static long adjustedDurationTicks(GuildData guild, GuildTeam team, int gateRank) {
      double powerRatio = Math.max(0.65, Math.min(1.35, teamPower(guild, team) / Math.max(1.0, gateRank * 5.0)));
      return Math.max(1200L, Math.round(GuildDeployment.durationTicks(gateRank) / powerRatio));
   }

   private static long adjustedXpReward(GuildData guild, GuildTeam team, int gateRank) {
      double powerRatio = Math.max(0.75, Math.min(1.25, teamPower(guild, team) / Math.max(1.0, gateRank * 5.0)));
      return Math.round(GuildDeployment.xpForRank(gateRank) * powerRatio);
   }

   private static double teamPower(GuildData guild, GuildTeam team) {
      if (guild != null && team != null) {
         double power = 0.0;

         for (UUID hid : team.memberIds) {
            GuildHunter hunter = guild.getHunter(hid);
            if (hunter != null) {
               power += hunter.rankScore() + classBonus(hunter.hunterClass);
            }
         }

         return Math.max(1.0, power);
      } else {
         return 1.0;
      }
   }

   private static double classBonus(String hunterClass) {
      return switch (hunterClass) {
         case "Tanker", "Healer" -> 0.35;
         case "Mage", "Ranger" -> 0.25;
         case "Assassin", "Fighter" -> 0.2;
         default -> 0.0;
      };
   }

   private static List<ItemStack> createManaCrystalRewards(GuildData guild, GuildDeployment dep) {
      double powerRatio = Math.max(0.75, Math.min(1.35, teamPower(guild, guild.getTeam(dep.teamId)) / Math.max(1.0, dep.gateRank * 5.0)));
      int main = Math.max(1, (int)Math.round((2 + dep.gateRank) * powerRatio));
      int lower = dep.gateRank > 1 ? Math.max(1, (int)Math.round((1.0 + dep.gateRank / 2.0) * powerRatio)) : 0;
      List<ItemStack> rewards = new ArrayList<>();
      rewards.add(new ItemStack(crystalForRank(dep.gateRank), main));
      if (lower > 0) {
         rewards.add(new ItemStack(crystalForRank(dep.gateRank - 1), lower));
      }

      if (dep.gateRank >= 5) {
         rewards.add(new ItemStack(crystalForRank(dep.gateRank), 1));
      }

      return rewards;
   }

   private static int insertRewards(GuildData guild, List<ItemStack> rewards) {
      int inserted = 0;

      for (ItemStack reward : rewards) {
         int before = reward.getCount();
         mergeIntoGuildStorage(guild, reward);
         inserted += before - reward.getCount();
      }

      return inserted;
   }

   private static void mergeIntoGuildStorage(GuildData guild, ItemStack stack) {
      if (guild != null && !stack.isEmpty()) {
         for (int i = 0; i < guild.storageItems.size() && !stack.isEmpty(); i++) {
            ItemStack slot = guild.storageItems.get(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
               int move = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
               slot.grow(move);
               stack.shrink(move);
            }
         }

         for (int i = 0; i < guild.storageItems.size() && !stack.isEmpty(); i++) {
            if (guild.storageItems.get(i).isEmpty()) {
               int move = Math.min(stack.getCount(), stack.getMaxStackSize());
               ItemStack copy = stack.copy();
               copy.setCount(move);
               guild.storageItems.set(i, copy);
               stack.shrink(move);
            }
         }
      }
   }

   private static Item crystalForRank(int rank) {
      return switch (rank) {
         case 2 -> (Item)SololevelingModItems.MANA_CRYSTAL_D.get();
         case 3 -> (Item)SololevelingModItems.MANA_CRYSTAL_C.get();
         case 4 -> (Item)SololevelingModItems.MANA_CRYSTAL_B.get();
         case 5 -> (Item)SololevelingModItems.MANA_CRYSTAL_A.get();
         case 6 -> (Item)SololevelingModItems.MANA_CRYSTAL_S.get();
         default -> (Item)SololevelingModItems.MANA_CRYSTAL_E.get();
      };
   }
}
