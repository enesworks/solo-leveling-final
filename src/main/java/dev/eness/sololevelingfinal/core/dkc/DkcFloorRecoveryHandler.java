package dev.eness.sololevelingfinal.core.dkc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.PlayerEntryGenerationGuard;

@EventBusSubscriber
public final class DkcFloorRecoveryHandler {
   private DkcFloorRecoveryHandler() {
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         SololevelingModVariables.PlayerVariables var8 = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         int storedFloor = clamp((int)player.getPersistentData().getDouble("dkc_current_floor"));
         boolean nearLegacyDistrict = storedFloor > 0
            && var8.dkc_started
            && (var8.dkc_x != 0.0 || var8.dkc_z != 0.0)
            && Math.abs(player.getX() - var8.dkc_x) <= 512.0
            && Math.abs(player.getZ() - var8.dkc_z) <= 4096.0;
         boolean legacyStranded = player.getPersistentData().getBoolean("dkc_inside_castle") || var8.dungeoning && storedFloor > 0 || nearLegacyDistrict;
         if (DkcFloorRegistry.isDkc(player.level()) || legacyStranded) {
            long entryGeneration = PlayerEntryGenerationGuard.capture(player);
            SololevelingMod.queueServerWork(player.server, 20, () -> {
               if (PlayerEntryGenerationGuard.isCurrent(player, entryGeneration)) {
                  recover(player);
               }
            });
         }
      }
   }

   private static void recover(ServerPlayer player) {
      if (player.isAlive() && player.server != null) {
         SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         double legacyOriginZ = vars.dkc_z;
         int storedFloor = clamp((int)player.getPersistentData().getDouble("dkc_current_floor"));
         int legacyDimensionFloor = DkcFloorRegistry.legacyFloor(player.level().dimension());
         DkcSpatialLayout.Location location = DkcSpatialLayout.locate(player.getX(), player.getZ());
         DkcRunSavedData runs = DkcRunSavedData.get(player.server);
         DkcRunSavedData.RunState state = runs.getOrCreate(player);
         int targetFloor = location.slot() == state.slot() ? location.floor() : legacyDimensionFloor;
         if (legacyDimensionFloor == 1 && targetFloor <= 1 && legacyOriginZ != 0.0) {
            int coordinateFloor = (int)Math.floor((player.getZ() - legacyOriginZ) / 200.0) + 1;
            if (coordinateFloor >= 1 && coordinateFloor <= 20) {
               targetFloor = coordinateFloor;
            }
         }

         targetFloor = Math.max(targetFloor, storedFloor);
         if (targetFloor <= 0) {
            targetFloor = 1;
         }

         while (targetFloor > 1 && !runs.isUnlocked(player, targetFloor)) {
            targetFloor--;
         }

         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.dkc_started = true;
            capability.dungeoning = false;
            capability.syncPlayerVariables(player);
         });
         DkcFloorBuilder.teleportToFloor(player, targetFloor);
      }
   }

   private static int clamp(int floor) {
      return Math.max(0, Math.min(20, floor));
   }
}
