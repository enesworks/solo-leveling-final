package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelAccessor;

public class DifficultyXPMultiplierProcedure {
   public static double execute(LevelAccessor world) {
      if (world.getDifficulty() == Difficulty.PEACEFUL || world.getDifficulty() == Difficulty.EASY) {
         return 1.0;
      } else if (world.getDifficulty() == Difficulty.NORMAL) {
         return 0.75;
      } else {
         return world.getDifficulty() == Difficulty.HARD ? 0.5 : 1.0;
      }
   }
}
