package dev.eness.sololevelingfinal.core.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.block.entity.GuildComputerBlockEntity;
import dev.eness.sololevelingfinal.core.guild.GuildBuffRegistry;
import dev.eness.sololevelingfinal.core.guild.GuildData;
import dev.eness.sololevelingfinal.core.guild.GuildDeployment;
import dev.eness.sololevelingfinal.core.guild.GuildGateHelper;
import dev.eness.sololevelingfinal.core.guild.GuildHunter;
import dev.eness.sololevelingfinal.core.guild.GuildInvitationManager;
import dev.eness.sololevelingfinal.core.guild.GuildMemberPermissions;
import dev.eness.sololevelingfinal.core.guild.GuildSavedData;
import dev.eness.sololevelingfinal.core.guild.GuildTeam;
import dev.eness.sololevelingfinal.core.guild.GuildTickHandler;
import dev.eness.sololevelingfinal.core.guild.HunterRecruitManager;

@EventBusSubscriber(bus = Bus.MOD)
public class GuildActionMessage {
   public final String action;
   public final BlockPos computerPos;
   public final String param1;
   public final String param2;

   public GuildActionMessage(String action, BlockPos computerPos, String param1, String param2) {
      this.action = action;
      this.computerPos = computerPos;
      this.param1 = param1;
      this.param2 = param2;
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(GuildActionMessage.class, GuildActionMessage::encode, GuildActionMessage::decode, GuildActionMessage::handle);
   }

   public static void encode(GuildActionMessage msg, FriendlyByteBuf buf) {
      buf.writeUtf(msg.action);
      buf.writeBlockPos(msg.computerPos);
      buf.writeUtf(msg.param1);
      buf.writeUtf(msg.param2);
   }

   public static GuildActionMessage decode(FriendlyByteBuf buf) {
      return new GuildActionMessage(buf.readUtf(), buf.readBlockPos(), buf.readUtf(), buf.readUtf());
   }

   public static void handle(GuildActionMessage msg, Supplier<Context> ctx) {
      ctx.get()
         .enqueueWork(
            () -> {
               ServerPlayer player = ctx.get().getSender();
               if (player != null) {
                  ServerLevel level = player.serverLevel();
                  GuildSavedData data = GuildSavedData.get(level);
                  GuildTickHandler.resolveDueDeployments(level.getServer(), level.getServer().overworld(), data);
                  UUID playerUUID = player.getUUID();
                  BlockEntity computerBlock = level.getBlockEntity(msg.computerPos);
                  if (computerBlock instanceof GuildComputerBlockEntity) {
                     switch (msg.action) {
                        case "create":
                           String guildName = msg.param1.trim();
                           if (guildName.isEmpty() || guildName.length() > 24) {
                              return;
                           }

                           if (data.getGuildForPlayer(playerUUID) != null) {
                              return;
                           }

                           GuildData guild = data.createGuild(guildName, playerUUID, player.getName().getString());
                           if (guild == null) {
                              player.sendSystemMessage(Component.literal("§cGuild name \"" + guildName + "\" is already taken."));
                              return;
                           }

                           if (level.getBlockEntity(msg.computerPos) instanceof GuildComputerBlockEntity computer) {
                              computer.setBoundGuildId(guild.id);
                           }

                           data.markDirty();
                           player.sendSystemMessage(Component.literal("§aGuild §e\"" + guildName + "\" §acreated successfully!"));
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "invite_member":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           String targetName = msg.param1.trim();
                           ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
                           if (target == null) {
                              player.sendSystemMessage(Component.literal("§cPlayer \"" + targetName + "\" is not online."));
                              return;
                           }

                           GuildInvitationManager.sendInvitation(player, target);
                           break;
                        case "delete_guild":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           String guildName = guild.name;
                           if (data.deleteGuild(guild.id)) {
                              player.sendSystemMessage(Component.literal("Â§cGuild Â§e\"" + guildName + "\" Â§chas been disbanded."));
                              reopenScreen(player, level, msg.computerPos);
                           }
                           break;
                        case "remove_member":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           UUID targetUUID = UUID.fromString(msg.param1);
                           guild.memberPermissions.removeIf(p -> p.playerUUID.equals(targetUUID));
                           data.markDirty();
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "toggle_perm":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           UUID targetUUID = UUID.fromString(msg.param1);
                           GuildMemberPermissions perms = guild.getPermissions(targetUUID);
                           if (perms == null) {
                              return;
                           }

                           switch (msg.param2) {
                              case "canOpen":
                                 perms.setAll(!perms.canOpen);
                                 break;
                              case "tabOverview":
                                 perms.tabOverview = !perms.tabOverview;
                                 break;
                              case "tabRoster":
                                 perms.tabRoster = !perms.tabRoster;
                                 break;
                              case "tabTeams":
                                 perms.tabTeams = !perms.tabTeams;
                                 break;
                              case "tabDungeons":
                                 perms.tabDungeons = !perms.tabDungeons;
                                 break;
                              case "tabStorage":
                                 perms.tabStorage = !perms.tabStorage;
                                 break;
                              case "tabBuffs":
                                 perms.tabBuffs = !perms.tabBuffs;
                                 break;
                              case "tabLeaderboard":
                                 perms.tabLeaderboard = !perms.tabLeaderboard;
                           }

                           data.markDirty();
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "set_buff":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           int slot;
                           int buffId;
                           try {
                              slot = Integer.parseInt(msg.param1);
                              buffId = Integer.parseInt(msg.param2);
                           } catch (NumberFormatException e) {
                              return;
                           }

                           if (slot < 1 || slot > 2) {
                              return;
                           }

                           if (!GuildBuffRegistry.isSlotUnlocked(guild, slot)) {
                              player.sendSystemMessage(Component.literal("§cThat guild buff slot is still locked."));
                              return;
                           }

                           if (!GuildBuffRegistry.isUnlocked(guild, buffId)) {
                              player.sendSystemMessage(Component.literal("§cYour guild has not unlocked that buff yet."));
                              return;
                           }

                           if (slot == 1) {
                              guild.activeBuffSlot1 = buffId;
                           } else {
                              guild.activeBuffSlot2 = buffId;
                           }

                           data.markDirty();
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "hire":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null) {
                              return;
                           }

                           if (!guild.canOperate(playerUUID)) {
                              return;
                           }

                           guild.pruneTeamMembers();
                           if (guild.hunters.size() >= 25) {
                              player.sendSystemMessage(Component.literal("§cGuild is full! (max 25 hunters)"));
                              return;
                           }

                           UUID recruitId = UUID.fromString(msg.param1);
                           GuildHunter recruit = guild.getRecruit(recruitId);
                           if (recruit == null) {
                              return;
                           }

                           int cost = GuildHunter.hireCost(recruit.rank);
                           ItemStack costItem = getCostItem(recruit.rank);
                           if (!player.isCreative() && !hasEnoughItems(player, costItem, cost)) {
                              player.sendSystemMessage(
                                 Component.literal("§cNot enough " + GuildHunter.hireMaterialName(recruit.rank) + "! Need §e" + cost + "§c.")
                              );
                              return;
                           }

                           if (!player.isCreative()) {
                              removeItems(player, costItem, cost);
                           }

                           guild.recruitPool.remove(recruit);
                           guild.removeHunterFromAllTeams(recruit.id);
                           recruit.status = "idle";
                           guild.hunters.add(recruit);
                           data.markDirty();
                           player.sendSystemMessage(Component.literal("§aHired §e" + recruit.name + " §7[" + recruit.rank + " " + recruit.hunterClass + "]§a!"));
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "dismiss":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           guild.pruneTeamMembers();
                           UUID hunterId = UUID.fromString(msg.param1);
                           GuildHunter hunter = guild.getHunter(hunterId);
                           if (hunter == null) {
                              return;
                           }

                           if ("deployed".equals(hunter.status)) {
                              player.sendSystemMessage(Component.literal("§cCannot dismiss a deployed hunter!"));
                              return;
                           }

                           guild.removeHunterFromAllTeams(hunterId);
                           guild.hunters.remove(hunter);
                           data.markDirty();
                           player.sendSystemMessage(Component.literal("§7Dismissed §f" + hunter.name + "§7."));
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "refresh_pool":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           int cost = 8;
                           ItemStack gold = new ItemStack(Items.GOLD_INGOT);
                           if (!hasEnoughItems(player, gold, cost)) {
                              player.sendSystemMessage(Component.literal("§cNeed §e8 Gold Ingots §cto refresh the recruit pool."));
                              return;
                           }

                           removeItems(player, gold, cost);
                           HunterRecruitManager.fillPool(guild);
                           data.markDirty();
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "assign_hunter":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           guild.pruneTeamMembers();
                           UUID teamId = UUID.fromString(msg.param1);
                           UUID hunterId = UUID.fromString(msg.param2);
                           GuildTeam team = guild.getTeam(teamId);
                           if (team == null) {
                              return;
                           }

                           if (guild.getDeploymentForTeam(teamId) != null) {
                              player.sendSystemMessage(Component.literal("Â§cCannot modify a deployed team!"));
                              return;
                           }

                           if (team.memberIds.size() >= 5) {
                              player.sendSystemMessage(Component.literal("§cTeam is full! (max 5 hunters)"));
                              return;
                           }

                           if (guild.getHunter(hunterId) == null) {
                              return;
                           }

                           guild.removeHunterFromAllTeams(hunterId);
                           team.memberIds.add(hunterId);
                           data.markDirty();
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "remove_from_team":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           guild.pruneTeamMembers();
                           UUID teamId = UUID.fromString(msg.param1);
                           UUID hunterId = UUID.fromString(msg.param2);
                           GuildTeam team = guild.getTeam(teamId);
                           if (team == null) {
                              return;
                           }

                           if (guild.getDeploymentForTeam(teamId) != null) {
                              player.sendSystemMessage(Component.literal("§cCannot modify a deployed team!"));
                              return;
                           }

                           guild.removeHunterFromAllTeams(hunterId);
                           data.markDirty();
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "set_team_auto_raid":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           guild.pruneTeamMembers();
                           UUID teamId = UUID.fromString(msg.param1);
                           GuildTeam team = guild.getTeam(teamId);
                           if (team == null) {
                              return;
                           }

                           String[] parts = msg.param2.split(":");
                           if (parts.length != 2) {
                              return;
                           }

                           int maxRank;
                           try {
                              maxRank = Integer.parseInt(parts[1]);
                           } catch (NumberFormatException e) {
                              return;
                           }

                           team.autoRaidEnabled = Boolean.parseBoolean(parts[0]);
                           team.autoRaidMaxRank = Math.max(1, Math.min(6, maxRank));
                           data.markDirty();
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "deploy_team":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           guild.pruneTeamMembers();
                           UUID teamId = UUID.fromString(msg.param1);
                           GuildTeam team = guild.getTeam(teamId);
                           if (team == null || team.memberIds.isEmpty()) {
                              player.sendSystemMessage(Component.literal("§cTeam has no members!"));
                              return;
                           }

                           if (guild.getDeploymentForTeam(teamId) != null) {
                              player.sendSystemMessage(Component.literal("§cTeam is already on a mission!"));
                              return;
                           }

                           for (UUID hid : team.memberIds) {
                              GuildHunter h = guild.getHunter(hid);
                              if (h == null || "deployed".equals(h.status)) {
                                 player.sendSystemMessage(Component.literal("§cAll hunters must be idle before deploying!"));
                                 return;
                              }
                           }

                           String gateEntityUUID = "";
                           String gateLabel;
                           int gateRank;
                           if (msg.param2.startsWith("sim:")) {
                              try {
                                 gateRank = Integer.parseInt(msg.param2.substring(4));
                              } catch (NumberFormatException e) {
                                 return;
                              }

                              gateRank = Math.max(1, Math.min(6, gateRank));
                              gateLabel = GuildDeployment.rankLabel(gateRank) + "-Rank Mission";
                           } else {
                              Entity gate;
                              try {
                                 gateEntityUUID = msg.param2;
                                 gate = GuildGateHelper.findGate(level, UUID.fromString(gateEntityUUID));
                              } catch (IllegalArgumentException e) {
                                 return;
                              }

                              if (gate != null) {
                                 GuildData raidingGuild = GuildGateHelper.findGuildRaidingGate(data, gateEntityUUID);
                                 if (raidingGuild != null) {
                                    player.sendSystemMessage(
                                       Component.literal(
                                          "§cThis gate has already been bought by §e" + raidingGuild.name + " §cguild and is currently being raided."
                                       )
                                    );
                                    return;
                                 }

                                 if (GuildGateHelper.isGateReserved(gate)) {
                                    player.sendSystemMessage(
                                       Component.literal(
                                          "§cThis gate has already been bought by §e"
                                             + GuildGateHelper.reservedGuildName(gate)
                                             + " §cguild and is currently being raided."
                                       )
                                    );
                                    return;
                                 }

                                 if (GuildGateHelper.isGateInteracted(gate)) {
                                    player.sendSystemMessage(Component.literal("§cThat gate has already been interacted with."));
                                    return;
                                 }
                              }

                              if (gate == null) {
                                 player.sendSystemMessage(Component.literal("§cThat gate is no longer available."));
                                 return;
                              }

                              for (GuildDeployment d : guild.deployments) {
                                 if (gateEntityUUID.equals(d.gateEntityUUID)) {
                                    player.sendSystemMessage(Component.literal("§cAnother team is already assigned to that gate."));
                                    return;
                                 }
                              }

                              gateRank = GuildGateHelper.gateRank(gate);
                              gateLabel = GuildGateHelper.gateLabel(gate);
                           }

                           long now = level.getGameTime();
                           long duration = GuildDeployment.durationTicks(gateRank);
                           long xp = GuildDeployment.xpForRank(gateRank);
                           GuildDeployment dep = new GuildDeployment(
                              UUID.randomUUID(), teamId, team.name, gateLabel, gateRank, gateEntityUUID, now + duration, xp
                           );
                           guild.deployments.add(dep);

                           for (UUID hid : team.memberIds) {
                              GuildHunter h = guild.getHunter(hid);
                              if (h != null) {
                                 h.status = "deployed";
                              }
                           }

                           if (!gateEntityUUID.isEmpty()) {
                              try {
                                 Entity gate = GuildGateHelper.findGate(level, UUID.fromString(gateEntityUUID));
                                 GuildGateHelper.reserveGate(gate, guild);
                              } catch (IllegalArgumentException var26) {
                              }
                           }

                           data.markDirty();
                           player.sendSystemMessage(Component.literal("§aTeam §e" + team.name + " §adeployed to §f" + gateLabel + "§a!"));
                           reopenScreen(player, level, msg.computerPos);
                           break;
                        case "recall_team":
                           GuildData guild = data.getGuildForPlayer(playerUUID);
                           if (guild == null || !guild.canOperate(playerUUID)) {
                              return;
                           }

                           UUID depId = UUID.fromString(msg.param1);
                           GuildDeployment dep = null;

                           for (GuildDeployment d : guild.deployments) {
                              if (d.id.equals(depId)) {
                                 dep = d;
                                 break;
                              }
                           }

                           if (dep == null) {
                              return;
                           }

                           GuildTeam team = guild.getTeam(dep.teamId);
                           if (team != null) {
                              for (UUID hid : team.memberIds) {
                                 GuildHunter h = guild.getHunter(hid);
                                 if (h != null) {
                                    h.status = "idle";
                                 }
                              }
                           }

                           if (!dep.gateEntityUUID.isEmpty()) {
                              try {
                                 Entity gate = GuildGateHelper.findGate(level, UUID.fromString(dep.gateEntityUUID));
                                 GuildGateHelper.clearReservation(gate);
                              } catch (IllegalArgumentException var25) {
                              }
                           }

                           guild.deployments.remove(dep);
                           data.markDirty();
                           player.sendSystemMessage(Component.literal("§7Team recalled (no reward)."));
                           reopenScreen(player, level, msg.computerPos);
                     }
                  }
               }
            }
         );
      ctx.get().setPacketHandled(true);
   }

   private static void reopenScreen(ServerPlayer player, ServerLevel level, BlockPos pos) {
      if (level.getBlockEntity(pos) instanceof GuildComputerBlockEntity computer) {
         NetworkHooks.openScreen(player, computer, buf -> computer.writeScreenOpeningData(player, buf));
      }
   }

   private static ItemStack getCostItem(String rank) {
      return switch (rank) {
         case "E" -> new ItemStack(Items.IRON_INGOT);
         case "D" -> new ItemStack(Items.GOLD_INGOT);
         default -> new ItemStack(Items.DIAMOND);
      };
   }

   private static boolean hasEnoughItems(ServerPlayer player, ItemStack template, int count) {
      return player.getInventory().countItem(template.getItem()) >= count;
   }

   private static void removeItems(ServerPlayer player, ItemStack template, int count) {
      int remaining = count;

      for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
         ItemStack slot = player.getInventory().getItem(i);
         if (slot.getItem() == template.getItem()) {
            int take = Math.min(slot.getCount(), remaining);
            slot.shrink(take);
            remaining -= take;
         }
      }
   }
}
