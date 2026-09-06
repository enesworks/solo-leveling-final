package dev.eness.sololevelingfinal.core.util;

public final class ShadowExperienceRules {
   private static final int MAX_TARGET_XP_POOL = 100000;

   private ShadowExperienceRules() {
   }

   public static int targetXpPool(
      double maxHealth,
      double attackDamage,
      double armor,
      double armorToughness,
      double targetLevel,
      int configuredBaseXp,
      boolean elite,
      boolean boss,
      boolean passiveAnimal
   ) {
      double health = finiteNonNegative(maxHealth);
      double attack = finiteNonNegative(attackDamage);
      double protection = finiteNonNegative(armor);
      double toughness = finiteNonNegative(armorToughness);
      double level = finiteNonNegative(targetLevel);
      double naturalStrength = 2.0 + health * 0.35 + attack * 2.0 + protection * 0.75 + toughness * 1.5 + level * 0.5;
      if (passiveAnimal) {
         naturalStrength *= 0.25;
      }

      if (boss) {
         naturalStrength = naturalStrength * 2.5 + 40.0;
      } else if (elite) {
         naturalStrength *= 1.5;
      }

      double configuredFloor = configuredBaseXp > 0 ? configuredBaseXp * 0.1 : 0.0;
      double minimum = passiveAnimal ? 1.0 : 5.0;
      double result = Math.max(minimum, Math.max(naturalStrength, configuredFloor));
      return !Double.isFinite(result) ? 100000 : Math.max(1, (int)Math.min(100000.0, Math.ceil(result)));
   }

   public static int contributionXp(int targetXpPool, double shadowDamage, double countedTargetDamage, int shadowLevel) {
      if (targetXpPool > 0 && Double.isFinite(shadowDamage) && Double.isFinite(countedTargetDamage) && !(shadowDamage <= 0.0) && !(countedTargetDamage <= 0.0)) {
         double share = Math.min(1.0, Math.max(0.0, shadowDamage / countedTargetDamage));
         double earned = targetXpPool * share * catchUpMultiplier(shadowLevel);
         return !Double.isFinite(earned) ? Integer.MAX_VALUE : Math.max(1, (int)Math.min(2147483647L, Math.round(earned)));
      } else {
         return 0;
      }
   }

   public static double catchUpMultiplier(int shadowLevel) {
      int level = Math.max(1, shadowLevel);
      if (level <= 10) {
         return 2.5;
      } else if (level <= 20) {
         return 1.75;
      } else {
         return level <= 30 ? 1.25 : 1.0;
      }
   }

   private static double finiteNonNegative(double value) {
      return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
   }
}
