package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;

public class HunterNaturalEntitySpawningConditionProcedure {
   public static boolean execute(LevelAccessor world, double x, double y, double z) {
      return world.getBlockState(BlockPos.containing(x, y, z)).getBlock() != Blocks.BEDROCK
         && world.getBlockState(BlockPos.containing(x, y, z)).getBlock() != SololevelingModBlocks.UNBREAKABLE_DEEPSLATE.get();
   }
}
