package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcRadiruManager;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

public class DKCLevelItemRightclickedProcedure {
   public static void execute(Entity entity) {
      if (entity instanceof ServerPlayer player && player.server != null) {
         SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         int delta = player.isShiftKeyDown() ? -1 : 1;
         int cleared = Math.max(0, Math.min(20, (int)vars.dkc_cleared + delta));
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.dkc_cleared = cleared;
            capability.dkc_started = cleared > 0 || capability.dkc_started;
            capability.syncPlayerVariables(player);
         });
         DkcRadiruManager.normalizeDebugProgress(player, cleared);
         DkcRunSavedData.get(player.server).setDebugProgress(player, cleared);
         DkcRadiruManager.reconcileDebugProgress(player, cleared);
         VesselProgressionManager.reconcileEntitlements(player);
      }
   }

   public static boolean setCurrentFloor(ServerPlayer player, int floor) {
      if (player != null && player.server != null && floor >= 1 && floor <= 20) {
         int cleared = floor - 1;
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.dkc_started = true;
            capability.dkc_cleared = cleared;
            capability.syncPlayerVariables(player);
         });
         resetFloorAttempt(player, floor);
         DkcRadiruManager.normalizeDebugProgress(player, cleared);
         DkcRunSavedData.get(player.server).setDebugProgress(player, cleared);
         VesselProgressionManager.reconcileEntitlements(player);
         DkcFloorBuilder.debugTeleportToFloor(
            player,
            floor,
            () -> {
               DkcFloorBuilder.resetEncounterForDebug(player, floor);
               DkcRadiruManager.reconcileDebugProgress(player, cleared);
               player.sendSystemMessage(
                  Component.literal("DKC test state set to Floor " + floor + " - " + DkcFloorRegistry.name(floor)).withStyle(ChatFormatting.LIGHT_PURPLE)
               );
            }
         );
         return true;
      } else {
         return false;
      }
   }

   private static void resetFloorAttempt(ServerPlayer player, int floor) {
      CompoundTag data = player.getPersistentData();
      String prefix = "dkc_floor_" + floor;

      for (String suffix : new String[]{
         "_spawned",
         "_initial_spawned",
         "_spawning",
         "_complete",
         "_killed",
         "_required",
         "_demon_count",
         "_knight_count",
         "_miniboss_spawned",
         "_spawn_retry_after",
         "_enter_time"
      }) {
         data.remove(prefix + suffix);
      }

      data.remove(prefix + "_boss_defeated");
      if (floor == 20) {
         data.remove("dkc_floor_20_baran_defeated");
         data.remove("dkc_floor_20_kaiselin_defeated");
         data.remove("dkc_floor_20_kaisel_soul_spawned");
      }

      DKCDemonSpawnerProcedure.invalidateAttempt(player, floor);
      ServerLevel dkc = player.server.getLevel(DkcFloorRegistry.SHARED_DIMENSION);
      if (dkc != null) {
         DKCDemonSpawnerProcedure.discardOwnedWave(dkc, player, floor);
      }
   }
}
