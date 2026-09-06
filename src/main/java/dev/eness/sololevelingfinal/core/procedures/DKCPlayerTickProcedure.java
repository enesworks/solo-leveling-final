package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcQuestProgressTracker;
import dev.eness.sololevelingfinal.core.dkc.DkcRadiruManager;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;
import dev.eness.sololevelingfinal.core.util.DkcTargetHighlightManager;

public class DKCPlayerTickProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity instanceof ServerPlayer player && world != null) {
         DkcFloorBuilder.tickPlayer(player);
         int floor = DkcSpatialLayout.floor(player);
         if (floor > 0 && player.server != null && DkcRunSavedData.get(player.server).isGenerated(player, floor)) {
            DkcQuestProgressTracker.tick(player);
            DKCFloorQuestStarterProcedure.execute(world, player);
            DKCDemonSpawnerProcedure.checkDelayedSpawn(world, player);
            DkcRadiruManager.tick(player);
            DkcTargetHighlightManager.sync(player, floor);
         }
      }
   }
}
