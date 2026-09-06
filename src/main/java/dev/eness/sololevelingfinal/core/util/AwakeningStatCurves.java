package dev.eness.sololevelingfinal.core.util;

public final class AwakeningStatCurves {
   private static final double[] MAGE_INTELLIGENCE = new double[]{0.0, 3.0, 13.0, 30.0, 53.0, 83.0, 120.0};
   private static final double[] HEALER_INTELLIGENCE = new double[]{0.0, 3.0, 11.0, 25.0, 44.0, 69.0, 100.0};
   private static final double[] RANGER_INTELLIGENCE = new double[]{0.0, 2.0, 9.0, 20.0, 36.0, 56.0, 80.0};

   private AwakeningStatCurves() {
   }

   public static double intelligenceBonus(int classId, double hunterRank) {
      int rank = Math.max(1, Math.min(6, (int)Math.round(hunterRank)));

      return switch (classId) {
         case 2 -> MAGE_INTELLIGENCE[rank];
         default -> 0.0;
         case 5 -> HEALER_INTELLIGENCE[rank];
         case 6 -> RANGER_INTELLIGENCE[rank];
      };
   }
}
