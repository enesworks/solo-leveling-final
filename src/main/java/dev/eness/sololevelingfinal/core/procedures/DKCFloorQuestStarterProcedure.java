package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

public class DKCFloorQuestStarterProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (world instanceof ServerLevel level && entity instanceof ServerPlayer player && level.getGameTime() % 20L == 0L) {
         int floor = DkcSpatialLayout.floor(player);
         if (floor != 0) {
            GameType mode = player.gameMode.getGameModeForPlayer();
            if (mode != GameType.SPECTATOR && mode != GameType.CREATIVE) {
               DKCFloorDetectorProcedure.execute(player);
               CompoundTag data = player.getPersistentData();
               if (data.getBoolean("dkc_floor_" + floor + "_spawned")
                  && (DkcFloorRegistry.isBossFloor(floor) || floor == 10 && data.getBoolean("dkc_floor_10_complete"))
                  && level.getGameTime() % 40L == 0L) {
                  DkcFloorBuilder.ensureBosses(player, floor);
               }

               if (data.getBoolean("dkc_floor_just_changed") || !data.getBoolean("dkc_floor_" + floor + "_spawned")) {
                  startFloorQuest(world, player, floor);
                  data.putBoolean("dkc_floor_just_changed", false);
               }
            }
         }
      }
   }

   private static void startFloorQuest(LevelAccessor world, ServerPlayer player, int floor) {
      DkcRunSavedData runs = DkcRunSavedData.get(player.server);
      if (!runs.isUnlocked(player, floor)) {
         notifyNegative(player, "FLOOR LOCKED", "Present the previous floor's Entry Permit.");
      } else {
         CompoundTag data = player.getPersistentData();
         String spawnedKey = "dkc_floor_" + floor + "_spawned";
         double cleared = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables())
            .dkc_cleared;
         if (cleared >= floor) {
            data.putBoolean(spawnedKey, true);
         } else if (!data.getBoolean(spawnedKey)) {
            if (floor == 1) {
               data.putBoolean(spawnedKey, true);
               DkcFloorBuilder.ensureBosses(player, floor);
            } else if (floor == 20) {
               data.putBoolean(spawnedKey, true);
               DkcFloorBuilder.ensureBosses(player, floor);
            } else {
               DKCDemonSpawnerProcedure.execute(world, player);
            }
         }
      }
   }

   private static void notifyNegative(ServerPlayer player, String title, String under) {
      SystemNotifications.showNegativeTitleUnder(
         player,
         -49859,
         80,
         Component.literal(title).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
         Component.literal(under).withStyle(ChatFormatting.RED)
      );
   }
}
