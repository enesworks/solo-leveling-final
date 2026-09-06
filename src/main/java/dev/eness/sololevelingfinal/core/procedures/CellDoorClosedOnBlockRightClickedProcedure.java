package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;

public class CellDoorClosedOnBlockRightClickedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      world.setBlock(BlockPos.containing(x, y, z), SololevelingModBlocks.CELL_DOOR_OPEN.get().defaultBlockState(), 3);
   }
}
