package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;

public class FloorCreateNewProcedure {
   public static void execute(LevelAccessor world, Entity entity, String bossType) {
      if (entity instanceof ServerPlayer player && player.server != null) {
         int floor = DkcSpatialLayout.floor(player);
         if (floor == 0) {
            floor = Math.max(1, Math.min(20, (int)player.getPersistentData().getDouble("dkc_current_floor")));
         }

         DkcRunSavedData.get(player.server).unlockFloor(player, floor);
         DkcFloorBuilder.prepareFloor(player, floor);
      }
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(world, entity, "normal");
   }
}
