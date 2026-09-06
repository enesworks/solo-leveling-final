package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public final class PowerAppendTankerProcedure {
   private PowerAppendTankerProcedure() {
   }

   public static void execute(Entity entity) {
      TankerProgressionHelper.reconcileRankEntitlements(entity);
   }
}
