package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

public class HammerLivingEntityIsHitWithToolProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         world.levelEvent(
            2001,
            BlockPos.containing(entity.getX(), entity.getY() - 1.0, entity.getZ()),
            Block.getId(world.getBlockState(BlockPos.containing(entity.getX(), entity.getY() - 1.0, entity.getZ())))
         );
         world.levelEvent(
            2001,
            BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 1.0),
            Block.getId(world.getBlockState(BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 1.0)))
         );
         world.levelEvent(
            2001,
            BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 0.0),
            Block.getId(world.getBlockState(BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 1.0)))
         );
         world.levelEvent(
            2001,
            BlockPos.containing(entity.getX() + 0.0, entity.getY() - 1.0, entity.getZ() + 1.0),
            Block.getId(world.getBlockState(BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 1.0)))
         );
         world.levelEvent(
            2001,
            BlockPos.containing(entity.getX() + -1.0, entity.getY() - 1.0, entity.getZ() + -1.0),
            Block.getId(world.getBlockState(BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 1.0)))
         );
         world.levelEvent(
            2001,
            BlockPos.containing(entity.getX() + 0.0, entity.getY() - 1.0, entity.getZ() + -1.0),
            Block.getId(world.getBlockState(BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 1.0)))
         );
         world.levelEvent(
            2001,
            BlockPos.containing(entity.getX() + -1.0, entity.getY() - 1.0, entity.getZ() + 0.0),
            Block.getId(world.getBlockState(BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 1.0)))
         );
         world.levelEvent(
            2001,
            BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + -1.0),
            Block.getId(world.getBlockState(BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 1.0)))
         );
         world.levelEvent(
            2001,
            BlockPos.containing(entity.getX() + -1.0, entity.getY() - 1.0, entity.getZ() + 1.0),
            Block.getId(world.getBlockState(BlockPos.containing(entity.getX() + 1.0, entity.getY() - 1.0, entity.getZ() + 1.0)))
         );
      }
   }
}
