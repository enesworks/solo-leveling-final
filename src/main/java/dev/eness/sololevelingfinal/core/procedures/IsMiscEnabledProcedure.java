package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;

public class IsMiscEnabledProcedure {
   public static boolean execute(LevelAccessor world) {
      return world.getLevelData().getGameRules().getBoolean(SololevelingModGameRules.SOLO_MISC_ITEMS);
   }
}
