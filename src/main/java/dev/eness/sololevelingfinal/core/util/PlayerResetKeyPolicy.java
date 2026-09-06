package dev.eness.sololevelingfinal.core.util;

import java.util.List;
import java.util.Set;

public final class PlayerResetKeyPolicy {
   private static final String SHADOW_RESET_GENERATION = "sl_shadow_reset_generation";
   private static final String FORGIVING_DEATH_SNAPSHOT = "slr_forgiving_death_snapshot";
   private static final Set<String> CONSUMED_ENTITLEMENTS = Set.of(
      "slr_tanker_starter_redeemed_v1", "slr_instance_dungeon_key_claimed", "sl_urgent_pvp_first_reward_claimed", "SLRKangTaeshikAmbushCompleted"
   );
   private static final List<String> PRESERVED_PREFIXES = List.of(
      "slr_story_intro_", "slr_cartenon_", "slr_party_", "slr_guild_gate_reserved", "slr_runestone_skill_"
   );
   private static final List<String> RESET_PREFIXES = List.of("slr_", "sl_", "sololeveling", "solocraft_", "cd_", "dkc_", "radiru_", "mowf_");
   private static final Set<String> RESET_EXACT_KEYS = Set.of(
      "dungeon_tag", "mage_casting", "mage_qte_zone_start", "Critical_Attack_Targetting", "CriticalAttackTarget", "Mutilation_Targetting", "MutilationTarget"
   );

   private PlayerResetKeyPolicy() {
   }

   public static boolean shouldClear(String key) {
      if (key != null
         && !key.isBlank()
         && !"sl_shadow_reset_generation".equals(key)
         && !"slr_player_entry_generation".equals(key)
         && !"slr_temporary_stat_bonus_model_v1".equals(key)
         && !"slr_temporary_armor_generation".equals(key)
         && !"slr_temporary_armor_escrow_active".equals(key)
         && !"slr_temporary_armor_escrow_equipped".equals(key)
         && !"slr_forgiving_death_snapshot".equals(key)
         && !CONSUMED_ENTITLEMENTS.contains(key)) {
         for (String prefix : PRESERVED_PREFIXES) {
            if (key.startsWith(prefix)) {
               return false;
            }
         }

         if (RESET_EXACT_KEYS.contains(key)) {
            return true;
         }

         for (String prefix : RESET_PREFIXES) {
            if (key.startsWith(prefix)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
