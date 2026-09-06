package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;

public final class SoloDungeonStuckProcedureProcedure {
   private SoloDungeonStuckProcedureProcedure() {
   }

   public static boolean execute(ServerPlayer player) {
      return DungeonDimensionPlayerLeavesDimensionProcedure.emergencyExit(player);
   }
}
