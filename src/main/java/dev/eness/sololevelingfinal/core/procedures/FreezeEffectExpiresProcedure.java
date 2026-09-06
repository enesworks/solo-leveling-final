package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class FreezeEffectExpiresProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      world.levelEvent(2001, BlockPos.containing(x, y + 1.0, z), Block.getId(Blocks.GLASS.defaultBlockState()));
      world.levelEvent(2001, BlockPos.containing(x, y + 1.0, z), Block.getId(Blocks.ICE.defaultBlockState()));
   }
}
