package dev.eness.sololevelingfinal.core.util;

public final class ShadowHealingRules {
   public static final double HEALTH_PER_MANA = 4.0;

   private ShadowHealingRules() {
   }

   public static int manaCost(double missingHealth) {
      if (Double.isFinite(missingHealth) && !(missingHealth <= 0.0)) {
         double cost = Math.ceil(missingHealth / 4.0);
         return (int)Math.min(2.147483647E9, Math.max(1.0, cost));
      } else {
         return 0;
      }
   }
}
