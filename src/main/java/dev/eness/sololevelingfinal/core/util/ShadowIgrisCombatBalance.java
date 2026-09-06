package dev.eness.sololevelingfinal.core.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ShadowIgrisCombatBalance {
   public static final String APPLIED_RANK_TAG = "sl_shadow_applied_rank";
   public static final double ATTACK_ATTRIBUTE_WEIGHT = 0.4;
   private static final double MAX_ABILITY_DAMAGE = 1000000.0;

   private ShadowIgrisCombatBalance() {
   }

   public static float abilityDamage(Entity source, double baseDamage) {
      double attackDamage = source instanceof LivingEntity living ? living.getAttributeValue(Attributes.ATTACK_DAMAGE) : 0.0;
      int rank = source == null ? 0 : source.getPersistentData().getInt("sl_shadow_applied_rank");
      return (float)abilityDamage(baseDamage, attackDamage, rank);
   }

   public static double abilityDamage(double baseDamage, double attackDamage, int rank) {
      double authoredDamage = finiteNonNegative(baseDamage);
      double progressedAttack = finiteNonNegative(attackDamage);
      double result = (authoredDamage + progressedAttack * 0.4) * rankMultiplier(rank);
      return !Double.isFinite(result) ? 1000000.0 : Math.min(1000000.0, Math.max(0.0, result));
   }

   public static double rankMultiplier(int rank) {
      return switch (Math.max(0, Math.min(6, rank))) {
         case 1 -> 1.08;
         case 2 -> 1.18;
         case 3 -> 1.32;
         case 4 -> 1.5;
         case 5 -> 1.75;
         case 6 -> 2.15;
         default -> 1.0;
      };
   }

   private static double finiteNonNegative(double value) {
      return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
   }
}
