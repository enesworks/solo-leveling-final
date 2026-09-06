package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;

public class MiniGemGolemEntityDiesProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      if (world instanceof ServerLevel _level) {
         FallingBlockEntity.fall(_level, BlockPos.containing(x, y, z), SololevelingModBlocks.GOLEM_DROP_BLOCK_GEM.get().defaultBlockState());
      }
   }
}
