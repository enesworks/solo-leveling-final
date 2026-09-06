package dev.eness.sololevelingfinal.core.dungeon.runtime;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import dev.eness.sololevelingfinal.core.world.dimension.rift.RiftTerritory;

public final class RedGateRealmLayout {
   public static final int REGION_ORIGIN = 1048576;
   public static final int CELL_SIZE = 1024;
   public static final int MAX_SLOTS_PER_TERRITORY = 256;
   private static final int LANE_RECOVERY_RADIUS = 341;

   private RedGateRealmLayout() {
   }

   public static BlockPos center(RiftTerritory territory, int slot, int y) {
      if (territory == null) {
         throw new IllegalArgumentException("A Red Gate territory is required.");
      } else if (slot >= 0 && slot < 256) {
         return new BlockPos(1048576 + territory.ordinal() * 1024, y, 1048576 + slot * 1024);
      } else {
         throw new IllegalArgumentException("Red Gate cell slot is outside the supported range.");
      }
   }

   public static Optional<RedGateRealmLayout.Cell> cellAt(BlockPos center) {
      if (center == null) {
         return Optional.empty();
      } else {
         long deltaX = center.getX() - 1048576L;
         long deltaZ = center.getZ() - 1048576L;
         long lane = Math.floorDiv(deltaX + 512L, 1024);
         long slot = Math.floorDiv(deltaZ + 512L, 1024);
         if (lane >= 0L && lane < RiftTerritory.values().length && slot >= 0L && slot < 256L) {
            long expectedX = 1048576L + lane * 1024L;
            long expectedZ = 1048576L + slot * 1024L;
            return Math.abs(center.getX() - expectedX) <= 341L && Math.abs(center.getZ() - expectedZ) <= 341L
               ? Optional.of(new RedGateRealmLayout.Cell(RiftTerritory.byIndex((int)lane), (int)slot))
               : Optional.empty();
         } else {
            return Optional.empty();
         }
      }
   }

   public static Optional<RiftTerritory> territoryAtX(double x) {
      long blockX = (long)Math.floor(x);
      long deltaX = blockX - 1048576L;
      long lane = Math.floorDiv(deltaX + 512L, 1024);
      if (lane >= 0L && lane < RiftTerritory.values().length) {
         long expectedX = 1048576L + lane * 1024L;
         return Math.abs(blockX - expectedX) > 341L ? Optional.empty() : Optional.of(RiftTerritory.byIndex((int)lane));
      } else {
         return Optional.empty();
      }
   }

   public record Cell(RiftTerritory territory, int slot) {
   }
}
