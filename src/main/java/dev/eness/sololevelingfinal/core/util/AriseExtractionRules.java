package dev.eness.sololevelingfinal.core.util;

import java.util.Locale;

public final class AriseExtractionRules {
   public static final String FAILURE_COUNT_TAG = "slr_arise_failures";
   public static final String TARGET_LEVEL_TAG = "slr_arise_target_level";
   public static final int MAX_BOSS_EXTRACTION_FAILURES = 3;
   public static final double MAX_EXTRACTION_LEVEL_GAP = 20.0;

   private AriseExtractionRules() {
   }

   public static boolean isBossSoul(String soulType) {
      return switch (normalize(soulType)) {
         case "igris", "beru", "tusk", "kaisel" -> true;
         default -> false;
      };
   }

   public static double defaultTargetLevel(String soulType) {
      return switch (normalize(soulType)) {
         case "orc", "bear" -> 40.0;
         case "highorc" -> 50.0;
         case "igris" -> 60.0;
         case "tusk", "kaisel" -> 70.0;
         case "beru" -> 100.0;
         default -> 0.0;
      };
   }

   public static double effectiveTargetLevel(String soulType, double storedTargetLevel) {
      return finiteNonNegative(storedTargetLevel) > 0.0 ? finiteNonNegative(storedTargetLevel) : defaultTargetLevel(soulType);
   }

   public static boolean isOverwhelming(double playerLevel, double targetLevel, boolean creativeMode) {
      return creativeMode ? false : finiteNonNegative(targetLevel) > finiteNonNegative(playerLevel) + 20.0;
   }

   public static double successChance(double playerLevel, double targetLevel, boolean creativeMode) {
      if (creativeMode) {
         return 1.0;
      } else {
         double target = finiteNonNegative(targetLevel);
         if (target <= 0.0) {
            return 1.0;
         } else {
            return isOverwhelming(playerLevel, target, false) ? 0.0 : Math.min(1.0, finiteNonNegative(playerLevel) / target);
         }
      }
   }

   public static int nextFailureCount(int currentFailures) {
      return Math.min(3, Math.max(0, currentFailures) + 1);
   }

   public static boolean failuresExhausted(int failures) {
      return failures >= 3;
   }

   private static double finiteNonNegative(double value) {
      return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
   }

   private static String normalize(String soulType) {
      return soulType == null ? "" : soulType.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
   }
}
