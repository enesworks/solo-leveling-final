package dev.eness.sololevelingfinal.core.entity.ai;

public final class TuskShadowCombatPolicy {
   public static final double RETREAT_RANGE = 7.0;
   public static final double PREFERRED_MIN_RANGE = 10.0;
   public static final double PREFERRED_MAX_RANGE = 20.0;
   public static final double MAX_CAST_RANGE = 24.0;
   public static final double OWNER_LEASH_RANGE = 32.0;
   public static final int REPATH_STALLED_TICKS = 20;
   public static final int ESCAPE_STALLED_TICKS = 40;
   public static final int RECALL_STALLED_TICKS = 70;

   private TuskShadowCombatPolicy() {
   }

   public static boolean shouldRetreat(double surfaceDistance) {
      return surfaceDistance < 7.0;
   }

   public static boolean shouldApproach(double surfaceDistance, boolean hasLineOfSight) {
      return !hasLineOfSight || surfaceDistance > 20.0;
   }

   public static boolean isUsefulCastingPosition(double surfaceDistance, boolean hasLineOfSight) {
      return hasLineOfSight && surfaceDistance >= 7.0 && surfaceDistance <= 24.0;
   }

   public static float soulFlameDamage(double attackDamage) {
      return scaledDamage(attackDamage, 0.7, 7.0);
   }

   public static float curseFieldDamage(double attackDamage) {
      return scaledDamage(attackDamage, 0.4, 5.0);
   }

   public static float groundSmashDamage(double attackDamage) {
      return scaledDamage(attackDamage, 1.0, 12.0);
   }

   public static TuskShadowCombatPolicy.RecoveryStage recoveryStage(int stalledTicks) {
      if (stalledTicks >= 70) {
         return TuskShadowCombatPolicy.RecoveryStage.RECALL;
      } else if (stalledTicks >= 40) {
         return TuskShadowCombatPolicy.RecoveryStage.ESCAPE;
      } else {
         return stalledTicks >= 20 ? TuskShadowCombatPolicy.RecoveryStage.REPATH : TuskShadowCombatPolicy.RecoveryStage.NONE;
      }
   }

   private static float scaledDamage(double attackDamage, double multiplier, double minimum) {
      double safeAttack = Double.isFinite(attackDamage) ? Math.max(0.0, attackDamage) : 0.0;
      return (float)Math.max(minimum, safeAttack * multiplier);
   }

   public enum RecoveryStage {
      NONE,
      REPATH,
      ESCAPE,
      RECALL;
   }
}
