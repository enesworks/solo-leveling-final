package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.RangerCombatManager;

public final class PowerAppendRangerProcedure {
   private PowerAppendRangerProcedure() {
   }

   public static void execute(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         RangerCombatManager.reconcileRanger(player);
      }
   }
}
