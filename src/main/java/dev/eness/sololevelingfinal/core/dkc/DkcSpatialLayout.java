package dev.eness.sololevelingfinal.core.dkc;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class DkcSpatialLayout {
   public static final String ACTIVE_RUN_TAG = "dkc_inside_castle";
   public static final int CELL_SIZE = 2048;
   public static final int FLOOR_COLUMNS = 5;
   public static final int FLOOR_ROWS = 4;
   public static final int SECTOR_COLUMNS = 64;
   public static final int SECTOR_STRIDE = 16384;
   public static final int BASE_X = 4000000;
   public static final int BASE_Z = 4000000;
   public static final int MAX_SECTOR_ROWS = 64;
   public static final int MAX_SLOTS = 4096;
   private static final int HALF_CELL = 1024;
   private static final DkcSpatialLayout.Location OUTSIDE = new DkcSpatialLayout.Location(-1, 0);

   private DkcSpatialLayout() {
   }

   public static BlockPos floorOrigin(int slot, int floor) {
      validateSlot(slot);
      validateFloor(floor);
      int sectorColumn = slot % 64;
      int sectorRow = slot / 64;
      int floorIndex = floor - 1;
      int floorColumn = floorIndex % 5;
      int floorRow = floorIndex / 5;
      return new BlockPos(4000000 + sectorColumn * 16384 + floorColumn * 2048, 29, 4000000 + sectorRow * 16384 + floorRow * 2048);
   }

   public static int floorAt(BlockPos position) {
      if (position == null) {
         return 0;
      }

      long packed = packedLocation(position.getX(), position.getZ());
      return packed == 0L ? 0 : (int)(packed & 255L);
   }

   public static int slotAt(BlockPos position) {
      if (position == null) {
         return -1;
      }

      long packed = packedLocation(position.getX(), position.getZ());
      return packed == 0L ? -1 : (int)(packed >>> 8) - 1;
   }

   public static int floor(ServerPlayer player) {
      if (player != null && player.server != null && DkcFloorRegistry.isSharedDkc(player.level())) {
         int slot = DkcRunSavedData.get(player.server).slot(player);
         long packed = packedLocation(player.getX(), player.getZ());
         return packed != 0L && (int)(packed >>> 8) - 1 == slot ? (int)(packed & 255L) : 0;
      } else {
         return 0;
      }
   }

   public static boolean isPlayerInFloor(ServerPlayer player, int floor) {
      return validFloor(floor) && floor(player) == floor;
   }

   public static boolean isEntityInOwnedFloor(Entity entity, UUID owner, int floor) {
      if (entity != null && owner != null && validFloor(floor) && DkcFloorRegistry.isSharedDkc(entity.level()) && entity.getServer() != null) {
         int slot = DkcRunSavedData.get(entity.getServer()).slot(owner);
         if (slot < 0) {
            return false;
         }

         long packed = packedLocation(entity.getX(), entity.getZ());
         return packed != 0L && (int)(packed >>> 8) - 1 == slot && (int)(packed & 255L) == floor;
      } else {
         return false;
      }
   }

   public static boolean isInsideSlotFloor(int slot, int floor, BlockPos position) {
      if (validSlot(slot) && validFloor(floor) && position != null) {
         long packed = packedLocation(position.getX(), position.getZ());
         return packed != 0L && (int)(packed >>> 8) - 1 == slot && (int)(packed & 255L) == floor;
      } else {
         return false;
      }
   }

   public static DkcSpatialLayout.Location locate(double x, double z) {
      long packed = packedLocation(x, z);
      return packed == 0L ? OUTSIDE : new DkcSpatialLayout.Location((int)(packed >>> 8) - 1, (int)(packed & 255L));
   }

   private static long packedLocation(double x, double z) {
      long blockX = (long)Math.floor(x);
      long blockZ = (long)Math.floor(z);
      long firstSectorMinX = 3998976L;
      long firstSectorMinZ = 3998976L;
      int sectorColumn = (int)Math.floorDiv(blockX - firstSectorMinX, 16384L);
      int sectorRow = (int)Math.floorDiv(blockZ - firstSectorMinZ, 16384L);
      if (sectorColumn >= 0 && sectorColumn < 64 && sectorRow >= 0 && sectorRow < 64) {
         long sectorBaseX = 4000000L + sectorColumn * 16384L;
         long sectorBaseZ = 4000000L + sectorRow * 16384L;
         int floorColumn = (int)Math.floorDiv(blockX - sectorBaseX + 1024L, 2048L);
         int floorRow = (int)Math.floorDiv(blockZ - sectorBaseZ + 1024L, 2048L);
         if (floorColumn >= 0 && floorColumn < 5 && floorRow >= 0 && floorRow < 4) {
            int floor = floorRow * 5 + floorColumn + 1;
            int slot = sectorRow * 64 + sectorColumn;
            return (long)(slot + 1) << 8 | floor;
         } else {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   public static boolean validSlot(int slot) {
      return slot >= 0 && slot < 4096;
   }

   private static boolean validFloor(int floor) {
      return floor >= 1 && floor <= 20;
   }

   private static void validateSlot(int slot) {
      if (!validSlot(slot)) {
         throw new IllegalArgumentException("DKC slot must be between 0 and 4095: " + slot);
      }
   }

   private static void validateFloor(int floor) {
      if (!validFloor(floor)) {
         throw new IllegalArgumentException("DKC floor must be between 1 and 20: " + floor);
      }
   }

   public record Location(int slot, int floor) {
      public boolean isInside() {
         return this.slot >= 0 && this.floor > 0;
      }
   }
}
