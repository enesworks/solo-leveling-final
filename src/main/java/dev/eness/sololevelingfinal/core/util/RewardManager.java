package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.RewardCollectProcedure;

public final class RewardManager {
   private static final String DELIMITER = "\\|";
   private static final String JOIN_DELIMITER = "|";
   private static final String FULL_RECOVERY_REWARD = "FR";

   private RewardManager() {
   }

   public static boolean hasRewards(Entity entity) {
      return !allRewards(entity).isEmpty();
   }

   public static List<String> allRewards(Entity entity) {
      List<String> rewards = new ArrayList<>();
      if (entity == null) {
         return rewards;
      }

      SololevelingModVariables.PlayerVariables vars = vars(entity);
      addIfPresent(rewards, vars.reward_1);
      addIfPresent(rewards, vars.reward_2);
      addIfPresent(rewards, vars.reward_3);

      for (String reward : parseExtra(vars.reward_extra)) {
         addIfPresent(rewards, reward);
      }

      return rewards;
   }

   public static String rewardAt(Entity entity, int slot) {
      if (entity != null && slot >= 1) {
         SololevelingModVariables.PlayerVariables vars = vars(entity);
         if (slot == 1) {
            return clean(vars.reward_1);
         }

         if (slot == 2) {
            return clean(vars.reward_2);
         }

         if (slot == 3) {
            return clean(vars.reward_3);
         }

         List<String> extra = parseExtra(vars.reward_extra);
         int index = slot - 4;
         return index >= 0 && index < extra.size() ? extra.get(index) : "";
      } else {
         return "";
      }
   }

   public static void appendReward(Entity entity, String reward) {
      if (entity != null && !isEmptyReward(reward)) {
         String cleanReward = clean(reward);
         reconcileFullRecoveryRewards(entity);
         if (isFullRecovery(cleanReward) && hasPendingFullRecovery(entity)) {
            applyFullRecovery(entity);
         } else {
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
               if (isEmptyReward(vars.reward_1)) {
                  vars.reward_1 = cleanReward;
               } else if (isEmptyReward(vars.reward_2)) {
                  vars.reward_2 = cleanReward;
               } else if (isEmptyReward(vars.reward_3)) {
                  vars.reward_3 = cleanReward;
               } else {
                  List<String> extra = parseExtra(vars.reward_extra);
                  extra.add(cleanReward);
                  vars.reward_extra = String.join("|", extra);
               }

               vars.syncPlayerVariables(entity);
            });
         }
      }
   }

   public static boolean hasPendingFullRecovery(Entity entity) {
      return entity == null ? false : allRewards(entity).stream().anyMatch(RewardManager::isFullRecovery);
   }

   public static void setFullRecoveryReward(Entity entity, int slot, boolean preservePreviousReward) {
      if (entity != null && slot >= 1 && slot <= 3) {
         reconcileFullRecoveryRewards(entity);
         if (hasPendingFullRecovery(entity)) {
            applyFullRecovery(entity);
         } else {
            String previous = rewardAt(entity, slot);
            if (preservePreviousReward && !isEmptyReward(previous)) {
               appendReward(entity, previous);
            }

            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
               if (slot == 1) {
                  vars.reward_1 = "FR";
               } else if (slot == 2) {
                  vars.reward_2 = "FR";
               } else {
                  vars.reward_3 = "FR";
               }

               vars.syncPlayerVariables(entity);
            });
         }
      }
   }

   public static boolean reconcileFullRecoveryRewards(Entity entity) {
      if (entity != null && !entity.level().isClientSide()) {
         List<String> rewards = allRewards(entity);
         boolean found = false;
         boolean duplicate = false;
         List<String> normalized = new ArrayList<>(rewards.size());

         for (String reward : rewards) {
            if (isFullRecovery(reward)) {
               if (found) {
                  duplicate = true;
                  continue;
               }

               found = true;
            }

            normalized.add(reward);
         }

         if (!duplicate) {
            return false;
         }

         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            writeRewards(vars, normalized);
            vars.syncPlayerVariables(entity);
         });
         applyFullRecovery(entity);
         return true;
      } else {
         return false;
      }
   }

   public static boolean claimReward(Entity entity, int slot) {
      String reward = rewardAt(entity, slot);
      if (isEmptyReward(reward)) {
         return false;
      }

      if (reward.startsWith("DAGGER_RECOVERY:")) {
         if (!(entity instanceof ServerPlayer player) || !DaggerThrowManager.claimRecovery(player, reward)) {
            return false;
         }
      } else {
         RewardCollectProcedure.execute(entity, reward);
      }

      entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
         if (slot == 1) {
            vars.reward_1 = "";
         } else if (slot == 2) {
            vars.reward_2 = "";
         } else if (slot == 3) {
            vars.reward_3 = "";
         } else {
            List<String> extra = parseExtra(vars.reward_extra);
            int index = slot - 4;
            if (index >= 0 && index < extra.size()) {
               extra.remove(index);
            }

            vars.reward_extra = String.join("|", extra);
         }

         compactSlots(vars);
         vars.syncPlayerVariables(entity);
      });
      return true;
   }

   public static String displayName(Entity entity, int slot) {
      return displayName(rewardAt(entity, slot));
   }

   public static String displayName(String reward) {
      String cleanReward = clean(reward);
      if (cleanReward.startsWith("DAGGER_RECOVERY:")) {
         return "§l" + DaggerThrowManager.displayRecoveryName(cleanReward);
      }

      if (cleanReward.startsWith("SP")) {
         try {
            int amount = Integer.parseInt(cleanReward.substring(2));
            return "§l" + amount + " Skill Points";
         } catch (NumberFormatException e) {
            return "§lInvalid Reward";
         }
      } else if (cleanReward.startsWith("GOLD")) {
         try {
            int amount = Integer.parseInt(cleanReward.substring(4));
            return "§l" + amount + " System Golds";
         } catch (NumberFormatException e) {
            return "§lInvalid Reward";
         }
      } else if (cleanReward.startsWith("XP")) {
         try {
            int amount = Integer.parseInt(cleanReward.substring(2));
            return "§l" + amount + " XP";
         } catch (NumberFormatException e) {
            return "§lInvalid Reward";
         }
      } else if (cleanReward.startsWith("ITEM:")) {
         String itemResourceLocation = cleanReward.substring(5);

         try {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemResourceLocation));
            return item != null && item != Items.AIR ? "§lItem: " + new ItemStack(item).getDisplayName().getString() : "§lUnknown Item";
         } catch (Exception e) {
            return "§lInvalid Item";
         }
      } else if (isFullRecovery(cleanReward)) {
         return "§lFull Recovery";
      } else {
         return "ITEMBOX".equals(cleanReward) ? "§lRandom Item" : "§lCollected!";
      }
   }

   public static boolean isEmptyReward(String reward) {
      return clean(reward).isEmpty();
   }

   private static SololevelingModVariables.PlayerVariables vars(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static void addIfPresent(List<String> rewards, String reward) {
      String cleanReward = clean(reward);
      if (!cleanReward.isEmpty()) {
         rewards.add(cleanReward);
      }
   }

   private static List<String> parseExtra(String encoded) {
      List<String> rewards = new ArrayList<>();
      if (encoded != null && !encoded.isBlank() && !"\"\"".equals(encoded.trim())) {
         for (String reward : encoded.split("\\|")) {
            addIfPresent(rewards, reward);
         }

         return rewards;
      } else {
         return rewards;
      }
   }

   private static void compactSlots(SololevelingModVariables.PlayerVariables vars) {
      List<String> rewards = new ArrayList<>();
      addIfPresent(rewards, vars.reward_1);
      addIfPresent(rewards, vars.reward_2);
      addIfPresent(rewards, vars.reward_3);
      rewards.addAll(parseExtra(vars.reward_extra));
      writeRewards(vars, rewards);
   }

   private static void writeRewards(SololevelingModVariables.PlayerVariables vars, List<String> rewards) {
      vars.reward_1 = rewards.size() > 0 ? rewards.get(0) : "";
      vars.reward_2 = rewards.size() > 1 ? rewards.get(1) : "";
      vars.reward_3 = rewards.size() > 2 ? rewards.get(2) : "";
      vars.reward_extra = rewards.size() > 3 ? String.join("|", rewards.subList(3, rewards.size())) : "";
   }

   private static boolean isFullRecovery(String reward) {
      return "FR".equals(clean(reward));
   }

   private static void applyFullRecovery(Entity entity) {
      if (entity != null && !entity.level().isClientSide()) {
         RewardCollectProcedure.execute(entity, "FR");
      }
   }

   public static void removeReward(Entity entity, String reward) {
      String target = clean(reward);
      if (entity != null && !target.isEmpty()) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            List<String> rewards = new ArrayList<>();
            addIfPresent(rewards, vars.reward_1);
            addIfPresent(rewards, vars.reward_2);
            addIfPresent(rewards, vars.reward_3);
            rewards.addAll(parseExtra(vars.reward_extra));
            if (rewards.remove(target)) {
               writeRewards(vars, rewards);
               vars.syncPlayerVariables(entity);
            }
         });
      }
   }

   private static String clean(String reward) {
      if (reward == null) {
         return "";
      } else {
         String trimmed = reward.trim();
         if ("\"\"".equals(trimmed)) {
            return "";
         } else {
            return trimmed.startsWith("ITEM:") && MageSpellProgression.isRetiredRunestoneId(trimmed.substring(5)) ? "SP5" : trimmed;
         }
      }
   }
}
