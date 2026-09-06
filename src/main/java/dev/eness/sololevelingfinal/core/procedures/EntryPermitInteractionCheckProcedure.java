package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;

public class EntryPermitInteractionCheckProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity instanceof ServerPlayer player && player.server != null) {
         int floor = DkcSpatialLayout.floor(player);
         if (floor > 0 && floor < 20 && DkcRunSavedData.get(player.server).isTransitionArmed(player, floor)) {
            DkcFloorBuilder.prepareFloor(player, floor + 1);
         }
      }
   }
}
