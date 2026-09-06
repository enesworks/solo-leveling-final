package dev.eness.sololevelingfinal.core.entity.ai;

public final class ShadowIronCombatPolicy {
   public static final double MELEE_REACH = 3.4;
   public static final double OWNER_LEASH_RANGE = 28.0;
   public static final double GUARDIAN_SCAN_RANGE = 16.0;
   public static final double TAUNT_RANGE = 12.0;
   public static final int TAUNT_TARGET_CAP = 8;
   public static final int ATTACK_IMPACT_TICK = 10;
   public static final int ATTACK_END_TICK = 20;
   public static final int BLOCK_ACTIVE_START_TICK = 4;
   public static final int BLOCK_ACTIVE_END_TICK = 12;
   public static final int BLOCK_END_TICK = 25;
   public static final int ROAR_PULSE_TICK = 12;
   public static final int ROAR_END_TICK = 35;
   public static final int ATTACK_COOLDOWN_TICKS = 26;
   public static final int BLOCK_COOLDOWN_TICKS = 80;
   public static final int ROAR_COOLDOWN_TICKS = 360;
   public static final int DEFAULT_INTERCEPT_COOLDOWN_TICKS = 200;
   public static final int PROTECT_INTERCEPT_COOLDOWN_TICKS = 160;
   public static final int NORMAL_TAUNT_TICKS = 100;
   public static final int ELITE_TAUNT_TICKS = 60;
   public static final int BOSS_TAUNT_TICKS = 30;
   public static final int PASSIVE_CHALLENGE_TICKS = 60;
   public static final int PASSIVE_BOSS_CHALLENGE_TICKS = 20;
   public static final int REPATH_STALLED_TICKS = 20;
   public static final int ESCAPE_STALLED_TICKS = 40;
   public static final int RECALL_STALLED_TICKS = 70;

   private ShadowIronCombatPolicy() {
   }

   public static float primaryDamage(double attackDamage, boolean counter) {
      double safeAttack = Double.isFinite(attackDamage) ? Math.max(0.0, attackDamage) : 0.0;
      return (float)Math.max(4.0, safeAttack * (counter ? 1.15 : 1.0));
   }

   public static float secondaryDamage(double attackDamage) {
      double safeAttack = Double.isFinite(attackDamage) ? Math.max(0.0, attackDamage) : 0.0;
      return (float)Math.max(1.5, safeAttack * 0.35);
   }

   public static float blockReduction(boolean projectile, boolean boss) {
      if (boss) {
         return projectile ? 0.45F : 0.35F;
      } else {
         return projectile ? 0.7F : 0.6F;
      }
   }

   public static float fortificationReduction(int tauntedTargets) {
      return tauntedTargets <= 0 ? 0.0F : Math.min(0.24F, 0.12F + Math.min(6, tauntedTargets) * 0.02F);
   }

   public static int tauntDuration(boolean boss, boolean elite) {
      return boss ? 30 : (elite ? 60 : 100);
   }

   public static int passiveChallengeDuration(boolean boss) {
      return boss ? 20 : 60;
   }

   public static boolean shouldEmergencyIntercept(boolean protectCommand, double ownerHealthRatio, double threatDistance, boolean projectile) {
      return projectile ? true : protectCommand || ownerHealthRatio <= 0.6 || threatDistance <= 3.5;
   }

   public static float ownerDamageFraction(boolean boss) {
      return boss ? 0.35F : 0.15F;
   }

   public static float redirectedDamageFraction(boolean boss) {
      return boss ? 0.75F : 0.65F;
   }

   public static ShadowIronCombatPolicy.RecoveryStage recoveryStage(int stalledTicks) {
      if (stalledTicks >= 70) {
         return ShadowIronCombatPolicy.RecoveryStage.RECALL;
      } else if (stalledTicks >= 40) {
         return ShadowIronCombatPolicy.RecoveryStage.ESCAPE;
      } else {
         return stalledTicks >= 20 ? ShadowIronCombatPolicy.RecoveryStage.REPATH : ShadowIronCombatPolicy.RecoveryStage.NONE;
      }
   }

   public enum RecoveryStage {
      NONE,
      REPATH,
      ESCAPE,
      RECALL;
   }
}
