package dev.eness.sololevelingfinal.core.util;

public final class LevelRewardRules {
   public static final int SKILL_POINTS_PER_LEVEL = 3;

   private LevelRewardRules() {
   }

   public static int skillPointsForLevels(int levelsGained) {
      return Math.max(0, levelsGained) * 3;
   }
}
