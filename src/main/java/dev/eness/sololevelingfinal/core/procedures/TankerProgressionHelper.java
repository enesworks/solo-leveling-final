package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public final class TankerProgressionHelper {
   public static final String TAUNT = "Taunt";
   public static final String REINFORCEMENT = "Reinforcement";
   public static final String TANK_LEAP = "Tank Leap";
   public static final String SHIELD_BASH = "Shield Bash";
   public static final String WILLPOWER = "Willpower";
   public static final String PROTECTION_MARK = "Protection Mark";
   private static final String RECONCILED_TAG = "slr_tanker_reconciled_v1";
   private static final String RECONCILED_RANK_TAG = "slr_tanker_reconciled_rank_v1";
   private static final String COOLDOWN_PREFIX = "cd_";
   private static final String FULL_COOLDOWN_PREFIX = "slr_cd_full_";
   private static final Map<String, Integer> MAX_COOLDOWN_TICKS = Map.of(
      "Taunt", 240, "Shield Bash", 160, "Tank Leap", 280, "Reinforcement", 440, "Willpower", 900, "Protection Mark", 1200
   );

   private TankerProgressionHelper() {
   }

   public static void reconcileRankEntitlements(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            if ((int)Math.round(vars.Classes) == 4) {
               boolean changed = migrateVariables(vars);
               int rank = Math.max(1, Math.min(6, (int)Math.round(vars.HunterRank)));

               for (String skill : TankerProgressionRules.entitlementsForRank(rank)) {
                  changed |= ensureSkill(vars, skill);
               }

               if (changed) {
                  vars.syncPlayerVariables(player);
               }

               CompoundTag persisted = persistedData(player);
               persisted.putBoolean("slr_tanker_reconciled_v1", true);
               persisted.putInt("slr_tanker_reconciled_rank_v1", rank);
            }
         });
         migrateCooldownAliases(player);
      }
   }

   public static void reconcileAliases(ServerPlayer player) {
      if (player != null) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            if (migrateVariables(vars)) {
               vars.syncPlayerVariables(player);
            }
         });
         migrateCooldownAliases(player);
      }
   }

   public static String grantNextMasterySkill(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         String[] granted = new String[]{""};
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            boolean changed = migrateVariables(vars);
            String missing = TankerProgressionRules.firstMissingSkill(vars.Plist);
            if (!missing.isEmpty()) {
               changed |= ensureSkill(vars, missing);
               granted[0] = missing;
            }

            if (changed) {
               vars.syncPlayerVariables(player);
            }
         });
         migrateCooldownAliases(player);
         return granted[0];
      } else {
         return "";
      }
   }

   public static void learnFromRunestone(Entity entity, ItemStack stack, String requestedSkill) {
      if (entity instanceof ServerPlayer player && stack != null && !stack.isEmpty()) {
         String skill = TankerProgressionRules.canonicalName(requestedSkill);
         if (TankerProgressionRules.isTankerSkill(skill)) {
            boolean[] learned = new boolean[]{false};
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
               boolean changed = migrateVariables(vars);
               if (!TankerProgressionRules.hasSkill(vars.Plist, skill)) {
                  changed |= ensureSkill(vars, skill);
                  learned[0] = true;
               }

               if (changed) {
                  vars.syncPlayerVariables(player);
               }
            });
            migrateCooldownAliases(player);
            if (!learned[0]) {
               player.displayClientMessage(Component.translatable("message.sololeveling.tanker.skill_known", skill), false);
            } else {
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               player.displayClientMessage(Component.translatable("message.sololeveling.tanker.skill_gained", skill), false);
            }
         }
      }
   }

   public static boolean isTanker(ServerPlayer player) {
      return player == null
         ? false
         : player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(vars -> (int)Math.round(vars.Classes) == 4).orElse(false);
   }

   private static boolean migrateVariables(SololevelingModVariables.PlayerVariables vars) {
      boolean changed = false;
      String migratedList = TankerProgressionRules.canonicalizeSkillList(vars.Plist);
      if (!migratedList.equals(vars.Plist)) {
         vars.Plist = migratedList;
         changed = true;
      }

      String selected = TankerProgressionRules.canonicalizeReference(vars.PselectedPower);
      if (!selected.equals(vars.PselectedPower)) {
         vars.PselectedPower = selected;
         changed = true;
      }

      for (int slot = 1; slot <= 16; slot++) {
         String current = SkillSlotHelper.getSlot(vars, slot);
         String canonical = TankerProgressionRules.canonicalizeReference(current);
         if (!canonical.equals(current)) {
            SkillSlotHelper.setSlot(vars, slot, canonical);
            changed = true;
         }
      }

      return changed;
   }

   private static boolean ensureSkill(SololevelingModVariables.PlayerVariables vars, String skill) {
      String updated = TankerProgressionRules.ensureSkill(vars.Plist, skill);
      if (updated.equals(vars.Plist)) {
         return false;
      }

      vars.Plist = updated;
      return true;
   }

   private static void migrateCooldownAliases(ServerPlayer player) {
      Map<String, List<String>> storedKeys = new LinkedHashMap<>();

      for (String nbtKey : new ArrayList<>(player.getPersistentData().getAllKeys())) {
         String rawSkill = null;
         if (nbtKey.startsWith("cd_")) {
            rawSkill = nbtKey.substring("cd_".length());
         } else if (nbtKey.startsWith("slr_cd_full_")) {
            rawSkill = nbtKey.substring("slr_cd_full_".length());
         }

         if (rawSkill != null) {
            String canonical = TankerProgressionRules.canonicalName(rawSkill);
            if (TankerProgressionRules.isTankerSkill(canonical)) {
               storedKeys.computeIfAbsent(canonical, ignored -> new ArrayList<>());
               if (!storedKeys.get(canonical).contains(rawSkill)) {
                  storedKeys.get(canonical).add(rawSkill);
               }
            }
         }
      }

      for (Entry<String, List<String>> entry : storedKeys.entrySet()) {
         String canonical = entry.getKey();
         int maximum = MAX_COOLDOWN_TICKS.getOrDefault(canonical, 0);
         int greatestRemaining = Math.min(maximum, CooldownManager.getRemainingTicks(player, canonical));
         boolean hasAlias = false;

         for (String storedKey : entry.getValue()) {
            greatestRemaining = Math.min(maximum, Math.max(greatestRemaining, CooldownManager.getRemainingTicks(player, storedKey)));
            if (!canonical.equals(storedKey)) {
               hasAlias = true;
               CooldownManager.clear(player, storedKey);
            }
         }

         int canonicalRemaining = CooldownManager.getRemainingTicks(player, canonical);
         if (hasAlias || canonicalRemaining > maximum) {
            if (greatestRemaining > 0) {
               CooldownManager.setFullDuration(player, canonical, greatestRemaining);
            } else {
               CooldownManager.clear(player, canonical);
            }
         }
      }
   }

   private static CompoundTag persistedData(Player player) {
      CompoundTag root = player.getPersistentData();
      if (!root.contains("PlayerPersisted", 10)) {
         root.put("PlayerPersisted", new CompoundTag());
      }

      return root.getCompound("PlayerPersisted");
   }
}
