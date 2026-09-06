package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;

public class DKCFloorDetectorProcedure {
   public static void execute(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         int currentFloor = DkcSpatialLayout.floor(player);
         if (currentFloor != 0) {
            CompoundTag data = entity.getPersistentData();
            int oldFloor = (int)data.getDouble("dkc_current_floor");
            if (oldFloor != currentFloor) {
               data.putDouble("dkc_previous_floor", oldFloor);
               data.putDouble("dkc_current_floor", currentFloor);
               data.putBoolean("dkc_floor_just_changed", true);
            }

            BlockPos origin = DkcFloorBuilder.origin(player, currentFloor);
            double localZ = entity.getZ() - origin.getZ();
            String section;
            if (currentFloor == 1) {
               section = localZ < 72.0 ? "arrival" : (localZ < 152.0 ? "courtyard" : "tower");
            } else {
               section = localZ < 24.0 ? "arrival" : "district";
            }

            data.putString("dkc_current_section", section);
         }
      }
   }

   public static int getCurrentFloor(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         return DkcSpatialLayout.floor(player);
      } else {
         if (entity == null) {
            return 0;
         }

         int stored = (int)entity.getPersistentData().getDouble("dkc_current_floor");
         return stored <= 0 ? 0 : Math.min(20, stored);
      }
   }

   public static String getCurrentSection(Entity entity) {
      if (entity == null) {
         return "arrival";
      }

      String section = entity.getPersistentData().getString("dkc_current_section");
      return section.isEmpty() ? "arrival" : section;
   }
}
