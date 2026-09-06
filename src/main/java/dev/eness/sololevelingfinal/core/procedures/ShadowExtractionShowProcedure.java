package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

public final class ShadowExtractionShowProcedure {
   private ShadowExtractionShowProcedure() {
   }

   public static void execute(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         VesselProgressionManager.sync(player);
      }
   }
}
