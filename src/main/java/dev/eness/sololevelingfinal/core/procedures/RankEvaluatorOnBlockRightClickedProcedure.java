package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.util.HunterEvaluationManager;

public final class RankEvaluatorOnBlockRightClickedProcedure {
   private RankEvaluatorOnBlockRightClickedProcedure() {
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity instanceof ServerPlayer player) {
         HunterEvaluationManager.openEvaluator(player, BlockPos.containing(x, y, z));
      }
   }
}
