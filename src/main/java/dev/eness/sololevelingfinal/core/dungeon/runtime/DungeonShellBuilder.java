package dev.eness.sololevelingfinal.core.dungeon.runtime;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class DungeonShellBuilder {
   public static final int DEFAULT_MAX_CHANGED_BLOCKS = 250000;
   public static final int ABSOLUTE_MAX_CHANGED_BLOCKS = 2000000;
   public static final int DEFAULT_MAX_CARVED_BLOCKS = 4096;
   public static final int ABSOLUTE_MAX_CARVED_BLOCKS = 32768;
   private static final int MAX_THICKNESS = 4;
   private static final int MAX_CARVE_AXIS = 64;
   private static final long MAX_RAW_SHELL_CANDIDATES = 8000000L;
   private static final int QUIET_UPDATE_FLAGS = 50;

   private DungeonShellBuilder() {
   }

   public static DungeonShellBuilder.ShellResult buildShell(
      ServerLevel level,
      Collection<DungeonTemplatePlacer.WorldBounds> rooms,
      Collection<DungeonTemplatePlacer.WorldBounds> carvedConnections,
      ResourceLocation shellBlockId,
      int thickness
   ) {
      return buildShell(level, rooms, carvedConnections, shellBlockId, thickness, 250000);
   }

   public static DungeonShellBuilder.ShellResult buildShell(
      ServerLevel level,
      Collection<DungeonTemplatePlacer.WorldBounds> rooms,
      Collection<DungeonTemplatePlacer.WorldBounds> carvedConnections,
      ResourceLocation shellBlockId,
      int thickness,
      int maxChangedBlocks
   ) {
      if (shellBlockId == null) {
         return DungeonShellBuilder.ShellResult.failure(DungeonShellBuilder.ErrorCode.INVALID_ARGUMENT, "A shell block ID is required.");
      }

      Block block = ForgeRegistries.BLOCKS.getValue(shellBlockId);
      return block == null
         ? DungeonShellBuilder.ShellResult.failure(DungeonShellBuilder.ErrorCode.UNKNOWN_BLOCK, "Unknown shell block " + shellBlockId + ".")
         : buildShell(level, rooms, carvedConnections, block.defaultBlockState(), thickness, maxChangedBlocks);
   }

   public static DungeonShellBuilder.ShellResult buildShell(
      ServerLevel level,
      Collection<DungeonTemplatePlacer.WorldBounds> rooms,
      Collection<DungeonTemplatePlacer.WorldBounds> carvedConnections,
      BlockState shellState,
      int thickness
   ) {
      return buildShell(level, rooms, carvedConnections, shellState, thickness, 250000);
   }

   public static DungeonShellBuilder.ShellResult buildShell(
      ServerLevel level,
      Collection<DungeonTemplatePlacer.WorldBounds> rooms,
      Collection<DungeonTemplatePlacer.WorldBounds> carvedConnections,
      BlockState shellState,
      int thickness,
      int maxChangedBlocks
   ) {
      return processShell(level, rooms, carvedConnections, shellState, thickness, maxChangedBlocks, true);
   }

   public static DungeonShellBuilder.ShellResult preflightShell(
      ServerLevel level,
      Collection<DungeonTemplatePlacer.WorldBounds> rooms,
      Collection<DungeonTemplatePlacer.WorldBounds> carvedConnections,
      BlockState shellState,
      int thickness
   ) {
      return processShell(level, rooms, carvedConnections, shellState, thickness, 250000, false);
   }

   private static DungeonShellBuilder.ShellResult processShell(
      ServerLevel level,
      Collection<DungeonTemplatePlacer.WorldBounds> rooms,
      Collection<DungeonTemplatePlacer.WorldBounds> carvedConnections,
      BlockState shellState,
      int thickness,
      int maxChangedBlocks,
      boolean applyChanges
   ) {
      if (level == null || rooms == null || carvedConnections == null || shellState == null) {
         return DungeonShellBuilder.ShellResult.failure(
            DungeonShellBuilder.ErrorCode.INVALID_ARGUMENT, "Level, room bounds, connection bounds, and shell state are required."
         );
      }

      if (thickness >= 0 && thickness <= 4) {
         if (maxChangedBlocks >= 0 && maxChangedBlocks <= 2000000) {
            if (shellState.isAir()) {
               return DungeonShellBuilder.ShellResult.failure(
                  DungeonShellBuilder.ErrorCode.INVALID_SHELL_STATE, "Air cannot be used as a protective shell block."
               );
            }

            DungeonShellBuilder.BoundsResult boundsResult = collectBounds(rooms, carvedConnections);
            if (!boundsResult.success) {
               return DungeonShellBuilder.ShellResult.failure(boundsResult.error, boundsResult.message);
            }

            List<DungeonTemplatePlacer.WorldBounds> allVolumes = boundsResult.bounds;
            long rawCandidateEstimate = 0L;
            List<DungeonTemplatePlacer.WorldBounds> expandedVolumes = new ArrayList<>(allVolumes.size());

            for (DungeonTemplatePlacer.WorldBounds volume : allVolumes) {
               DungeonShellBuilder.ValidationResult validation = validateBounds(level, volume, thickness);
               if (!validation.success) {
                  return DungeonShellBuilder.ShellResult.failure(validation.error, validation.message);
               }

               DungeonTemplatePlacer.WorldBounds expanded = expand(volume, thickness);
               expandedVolumes.add(expanded);
               rawCandidateEstimate = saturatedAdd(rawCandidateEstimate, shellCandidateVolume(expanded, volume));
               if (rawCandidateEstimate > 8000000L) {
                  return DungeonShellBuilder.ShellResult.failure(
                     DungeonShellBuilder.ErrorCode.TOO_MANY_CANDIDATES, "Shell preflight exceeds the hard candidate limit of 8000000 blocks."
                  );
               }
            }

            if (thickness == 0) {
               return DungeonShellBuilder.ShellResult.success("Shell thickness is zero; no blocks changed.", 0L, 0L, 0L, 0L, 0L);
            }

            LongOpenHashSet candidates = new LongOpenHashSet((int)Math.min(rawCandidateEstimate, 2000000L));
            long skippedInside = 0L;

            for (int i = 0; i < allVolumes.size(); i++) {
               DungeonTemplatePlacer.WorldBounds interior = allVolumes.get(i);
               DungeonTemplatePlacer.WorldBounds expanded = expandedVolumes.get(i);

               for (int x = expanded.min().getX(); x <= expanded.max().getX(); x++) {
                  for (int y = expanded.min().getY(); y <= expanded.max().getY(); y++) {
                     for (int z = expanded.min().getZ(); z <= expanded.max().getZ(); z++) {
                        if (!contains(interior, x, y, z)) {
                           if (containsAny(allVolumes, x, y, z)) {
                              skippedInside++;
                           } else {
                              candidates.add(BlockPos.asLong(x, y, z));
                           }
                        }
                     }
                  }
               }
            }

            LongArrayList changes = new LongArrayList(Math.min(candidates.size(), maxChangedBlocks));
            long alreadyMatching = 0L;
            long protectedBlockEntities = 0L;
            LongIterator candidateIterator = candidates.iterator();

            while (candidateIterator.hasNext()) {
               long packed = candidateIterator.nextLong();
               BlockPos position = BlockPos.of(packed);
               BlockState existing = level.getBlockState(position);
               if (existing.equals(shellState)) {
                  alreadyMatching++;
               } else if (level.getBlockEntity(position) != null) {
                  protectedBlockEntities++;
               } else {
                  if (changes.size() >= maxChangedBlocks) {
                     return DungeonShellBuilder.ShellResult.failure(
                        DungeonShellBuilder.ErrorCode.CHANGE_LIMIT_EXCEEDED,
                        "Shell requires more than the allowed " + maxChangedBlocks + " block changes.",
                        candidates.size(),
                        0L,
                        alreadyMatching,
                        skippedInside,
                        0L
                     );
                  }

                  changes.add(packed);
               }
            }

            if (protectedBlockEntities > 0L) {
               return DungeonShellBuilder.ShellResult.failure(
                  DungeonShellBuilder.ErrorCode.PROTECTED_BLOCK_ENTITY,
                  "Protective shell would overwrite " + protectedBlockEntities + " block entities; no shell blocks were changed.",
                  candidates.size(),
                  0L,
                  alreadyMatching,
                  skippedInside,
                  0L
               );
            }

            if (!applyChanges) {
               return DungeonShellBuilder.ShellResult.success("Protective shell preflight passed.", candidates.size(), 0L, alreadyMatching, skippedInside, 0L);
            }

            long changed = 0L;
            long failedWrites = 0L;

            for (int i = 0; i < changes.size(); i++) {
               BlockPos position = BlockPos.of(changes.getLong(i));
               if (level.setBlock(position, shellState, 50)) {
                  changed++;
               } else {
                  failedWrites++;
               }
            }

            return failedWrites > 0L
               ? DungeonShellBuilder.ShellResult.failure(
                  DungeonShellBuilder.ErrorCode.WRITE_FAILED,
                  "Some protective shell blocks could not be written.",
                  candidates.size(),
                  changed,
                  alreadyMatching,
                  skippedInside,
                  failedWrites
               )
               : DungeonShellBuilder.ShellResult.success("Protective dungeon shell built.", candidates.size(), changed, alreadyMatching, skippedInside, 0L);
         } else {
            return DungeonShellBuilder.ShellResult.failure(DungeonShellBuilder.ErrorCode.INVALID_CHANGE_CAP, "Shell change cap must be between 0 and 2000000.");
         }
      } else {
         return DungeonShellBuilder.ShellResult.failure(DungeonShellBuilder.ErrorCode.INVALID_THICKNESS, "Shell thickness must be between 0 and 4.");
      }
   }

   public static DungeonShellBuilder.CarveResult carveConnection(ServerLevel level, DungeonTemplatePlacer.WorldBounds connection) {
      return carveConnection(level, connection, 4096);
   }

   public static DungeonShellBuilder.CarveResult preflightConnection(ServerLevel level, DungeonTemplatePlacer.WorldBounds connection) {
      return preflightConnection(level, connection, 4096);
   }

   public static DungeonShellBuilder.CarveResult preflightConnection(ServerLevel level, DungeonTemplatePlacer.WorldBounds connection, int maxChangedBlocks) {
      return processConnection(level, connection, maxChangedBlocks, false, true);
   }

   public static DungeonShellBuilder.CarveResult carveConnection(ServerLevel level, DungeonTemplatePlacer.WorldBounds connection, int maxChangedBlocks) {
      return processConnection(level, connection, maxChangedBlocks, true, false);
   }

   private static DungeonShellBuilder.CarveResult processConnection(
      ServerLevel level, DungeonTemplatePlacer.WorldBounds connection, int maxChangedBlocks, boolean applyChanges, boolean conservativeChangeBudget
   ) {
      if (level == null || connection == null) {
         return DungeonShellBuilder.CarveResult.failure(DungeonShellBuilder.ErrorCode.INVALID_ARGUMENT, "A level and explicit connection volume are required.");
      }

      if (maxChangedBlocks >= 0 && maxChangedBlocks <= 32768) {
         DungeonShellBuilder.ValidationResult validation = validateBounds(level, connection, 0);
         if (!validation.success) {
            return DungeonShellBuilder.CarveResult.failure(validation.error, validation.message);
         }

         long volume = volumeOf(connection);
         if (connection.sizeX() <= 64 && connection.sizeY() <= 64 && connection.sizeZ() <= 64 && volume <= 32768L) {
            if (conservativeChangeBudget && volume > maxChangedBlocks) {
               return DungeonShellBuilder.CarveResult.failure(
                  DungeonShellBuilder.ErrorCode.CHANGE_LIMIT_EXCEEDED,
                  "Connection could require " + volume + " block changes after room placement, above the allowed " + maxChangedBlocks + ".",
                  volume,
                  0L,
                  0L,
                  0L,
                  0L
               );
            }

            LongArrayList changes = new LongArrayList((int)Math.min(volume, maxChangedBlocks));
            long alreadyAir = 0L;
            long protectedBlockEntities = 0L;

            for (int x = connection.min().getX(); x <= connection.max().getX(); x++) {
               for (int y = connection.min().getY(); y <= connection.max().getY(); y++) {
                  for (int z = connection.min().getZ(); z <= connection.max().getZ(); z++) {
                     BlockPos position = new BlockPos(x, y, z);
                     if (level.getBlockState(position).isAir()) {
                        alreadyAir++;
                     } else if (level.getBlockEntity(position) != null) {
                        protectedBlockEntities++;
                     } else {
                        if (changes.size() >= maxChangedBlocks) {
                           return DungeonShellBuilder.CarveResult.failure(
                              DungeonShellBuilder.ErrorCode.CHANGE_LIMIT_EXCEEDED,
                              "Connection requires more than the allowed " + maxChangedBlocks + " block changes.",
                              volume,
                              0L,
                              alreadyAir,
                              protectedBlockEntities,
                              0L
                           );
                        }

                        changes.add(position.asLong());
                     }
                  }
               }
            }

            if (protectedBlockEntities > 0L) {
               return DungeonShellBuilder.CarveResult.failure(
                  DungeonShellBuilder.ErrorCode.PROTECTED_BLOCK_ENTITY,
                  "Connection contains " + protectedBlockEntities + " block entities; nothing was carved to protect their data.",
                  volume,
                  0L,
                  alreadyAir,
                  protectedBlockEntities,
                  0L
               );
            }

            if (!applyChanges) {
               return DungeonShellBuilder.CarveResult.success("Connection preflight passed.", volume, 0L, alreadyAir, protectedBlockEntities, 0L);
            }

            long changed = 0L;
            long failedWrites = 0L;
            BlockState air = Blocks.AIR.defaultBlockState();

            for (int i = 0; i < changes.size(); i++) {
               if (level.setBlock(BlockPos.of(changes.getLong(i)), air, 50)) {
                  changed++;
               } else {
                  failedWrites++;
               }
            }

            return failedWrites > 0L
               ? DungeonShellBuilder.CarveResult.failure(
                  DungeonShellBuilder.ErrorCode.WRITE_FAILED,
                  "Some connection blocks could not be cleared.",
                  volume,
                  changed,
                  alreadyAir,
                  protectedBlockEntities,
                  failedWrites
               )
               : DungeonShellBuilder.CarveResult.success("Connection carved.", volume, changed, alreadyAir, protectedBlockEntities, 0L);
         } else {
            return DungeonShellBuilder.CarveResult.failure(
               DungeonShellBuilder.ErrorCode.CARVE_VOLUME_TOO_LARGE,
               "Connection carving is limited to 64 blocks per axis and 32768 blocks total.",
               volume,
               0L,
               0L,
               0L,
               0L
            );
         }
      } else {
         return DungeonShellBuilder.CarveResult.failure(DungeonShellBuilder.ErrorCode.INVALID_CHANGE_CAP, "Connection carve cap must be between 0 and 32768.");
      }
   }

   private static DungeonShellBuilder.BoundsResult collectBounds(
      Collection<DungeonTemplatePlacer.WorldBounds> rooms, Collection<DungeonTemplatePlacer.WorldBounds> connections
   ) {
      List<DungeonTemplatePlacer.WorldBounds> all = new ArrayList<>(rooms.size() + connections.size());

      for (DungeonTemplatePlacer.WorldBounds room : rooms) {
         if (room == null) {
            return DungeonShellBuilder.BoundsResult.failure(DungeonShellBuilder.ErrorCode.INVALID_BOUNDS, "Room bounds cannot contain null entries.");
         }

         all.add(room);
      }

      for (DungeonTemplatePlacer.WorldBounds connection : connections) {
         if (connection == null) {
            return DungeonShellBuilder.BoundsResult.failure(DungeonShellBuilder.ErrorCode.INVALID_BOUNDS, "Connection bounds cannot contain null entries.");
         }

         all.add(connection);
      }

      return all.isEmpty()
         ? DungeonShellBuilder.BoundsResult.failure(DungeonShellBuilder.ErrorCode.EMPTY_VOLUMES, "At least one room or connection volume is required.")
         : DungeonShellBuilder.BoundsResult.success(all);
   }

   private static DungeonShellBuilder.ValidationResult validateBounds(ServerLevel level, DungeonTemplatePlacer.WorldBounds bounds, int expansion) {
      long minX = (long)bounds.min().getX() - expansion;
      long minY = (long)bounds.min().getY() - expansion;
      long minZ = (long)bounds.min().getZ() - expansion;
      long maxX = (long)bounds.max().getX() + expansion;
      long maxY = (long)bounds.max().getY() + expansion;
      long maxZ = (long)bounds.max().getZ() + expansion;
      if (minX < -2147483648L || minZ < -2147483648L || maxX > 2147483647L || maxZ > 2147483647L) {
         return DungeonShellBuilder.ValidationResult.failure(
            DungeonShellBuilder.ErrorCode.INVALID_BOUNDS, "Expanded bounds exceed supported block coordinates."
         );
      } else if (minY >= level.getMinBuildHeight() && maxY < level.getMaxBuildHeight()) {
         BlockPos expandedMin = new BlockPos((int)minX, (int)minY, (int)minZ);
         BlockPos expandedMax = new BlockPos((int)maxX, (int)maxY, (int)maxZ);
         return level.getWorldBorder().isWithinBounds(expandedMin) && level.getWorldBorder().isWithinBounds(expandedMax)
            ? DungeonShellBuilder.ValidationResult.valid()
            : DungeonShellBuilder.ValidationResult.failure(
               DungeonShellBuilder.ErrorCode.OUTSIDE_WORLD_BORDER, "Dungeon shell bounds must remain inside the world border."
            );
      } else {
         return DungeonShellBuilder.ValidationResult.failure(
            DungeonShellBuilder.ErrorCode.OUT_OF_BUILD_HEIGHT,
            "Bounds and shell must stay between Y=" + level.getMinBuildHeight() + " and Y=" + (level.getMaxBuildHeight() - 1) + "."
         );
      }
   }

   private static DungeonTemplatePlacer.WorldBounds expand(DungeonTemplatePlacer.WorldBounds bounds, int amount) {
      return new DungeonTemplatePlacer.WorldBounds(bounds.min().offset(-amount, -amount, -amount), bounds.max().offset(amount, amount, amount));
   }

   private static boolean containsAny(List<DungeonTemplatePlacer.WorldBounds> bounds, int x, int y, int z) {
      for (DungeonTemplatePlacer.WorldBounds volume : bounds) {
         if (contains(volume, x, y, z)) {
            return true;
         }
      }

      return false;
   }

   private static boolean contains(DungeonTemplatePlacer.WorldBounds bounds, int x, int y, int z) {
      return x >= bounds.min().getX()
         && x <= bounds.max().getX()
         && y >= bounds.min().getY()
         && y <= bounds.max().getY()
         && z >= bounds.min().getZ()
         && z <= bounds.max().getZ();
   }

   private static long volumeOf(DungeonTemplatePlacer.WorldBounds bounds) {
      long x = (long)bounds.max().getX() - bounds.min().getX() + 1L;
      long y = (long)bounds.max().getY() - bounds.min().getY() + 1L;
      long z = (long)bounds.max().getZ() - bounds.min().getZ() + 1L;
      return saturatedMultiply(saturatedMultiply(x, y), z);
   }

   private static long shellCandidateVolume(DungeonTemplatePlacer.WorldBounds expanded, DungeonTemplatePlacer.WorldBounds interior) {
      long expandedVolume = volumeOf(expanded);
      if (expandedVolume == Long.MAX_VALUE) {
         return Long.MAX_VALUE;
      }

      long interiorVolume = volumeOf(interior);
      return Math.max(0L, expandedVolume - interiorVolume);
   }

   private static long saturatedMultiply(long first, long second) {
      if (first <= 0L || second <= 0L) {
         return 0L;
      } else {
         return first > Long.MAX_VALUE / second ? Long.MAX_VALUE : first * second;
      }
   }

   private static long saturatedAdd(long first, long second) {
      return first > Long.MAX_VALUE - second ? Long.MAX_VALUE : first + second;
   }

   private record BoundsResult(boolean success, DungeonShellBuilder.ErrorCode error, String message, List<DungeonTemplatePlacer.WorldBounds> bounds) {
      private static DungeonShellBuilder.BoundsResult success(List<DungeonTemplatePlacer.WorldBounds> bounds) {
         return new DungeonShellBuilder.BoundsResult(true, DungeonShellBuilder.ErrorCode.NONE, "", bounds);
      }

      private static DungeonShellBuilder.BoundsResult failure(DungeonShellBuilder.ErrorCode error, String message) {
         return new DungeonShellBuilder.BoundsResult(false, error, message, List.of());
      }
   }

   public record CarveResult(
      boolean success,
      DungeonShellBuilder.ErrorCode error,
      String message,
      long volume,
      long changedBlocks,
      long alreadyAir,
      long protectedBlockEntities,
      long failedWrites
   ) {
      private static DungeonShellBuilder.CarveResult success(String message, long volume, long changed, long air, long protectedEntities, long failed) {
         return new DungeonShellBuilder.CarveResult(true, DungeonShellBuilder.ErrorCode.NONE, message, volume, changed, air, protectedEntities, failed);
      }

      private static DungeonShellBuilder.CarveResult failure(DungeonShellBuilder.ErrorCode error, String message) {
         return failure(error, message, 0L, 0L, 0L, 0L, 0L);
      }

      private static DungeonShellBuilder.CarveResult failure(
         DungeonShellBuilder.ErrorCode error, String message, long volume, long changed, long air, long protectedEntities, long failed
      ) {
         return new DungeonShellBuilder.CarveResult(false, error, message, volume, changed, air, protectedEntities, failed);
      }
   }

   public enum ErrorCode {
      NONE,
      INVALID_ARGUMENT,
      INVALID_THICKNESS,
      INVALID_CHANGE_CAP,
      INVALID_SHELL_STATE,
      UNKNOWN_BLOCK,
      INVALID_BOUNDS,
      EMPTY_VOLUMES,
      OUT_OF_BUILD_HEIGHT,
      OUTSIDE_WORLD_BORDER,
      TOO_MANY_CANDIDATES,
      CHANGE_LIMIT_EXCEEDED,
      CARVE_VOLUME_TOO_LARGE,
      PROTECTED_BLOCK_ENTITY,
      WRITE_FAILED;
   }

   public record ShellResult(
      boolean success,
      DungeonShellBuilder.ErrorCode error,
      String message,
      long candidatePositions,
      long changedBlocks,
      long alreadyMatching,
      long skippedInsideVolumes,
      long failedWrites
   ) {
      private static DungeonShellBuilder.ShellResult success(String message, long candidates, long changed, long matching, long skipped, long failed) {
         return new DungeonShellBuilder.ShellResult(true, DungeonShellBuilder.ErrorCode.NONE, message, candidates, changed, matching, skipped, failed);
      }

      private static DungeonShellBuilder.ShellResult failure(DungeonShellBuilder.ErrorCode error, String message) {
         return failure(error, message, 0L, 0L, 0L, 0L, 0L);
      }

      private static DungeonShellBuilder.ShellResult failure(
         DungeonShellBuilder.ErrorCode error, String message, long candidates, long changed, long matching, long skipped, long failed
      ) {
         return new DungeonShellBuilder.ShellResult(false, error, message, candidates, changed, matching, skipped, failed);
      }
   }

   private record ValidationResult(boolean success, DungeonShellBuilder.ErrorCode error, String message) {
      private static DungeonShellBuilder.ValidationResult valid() {
         return new DungeonShellBuilder.ValidationResult(true, DungeonShellBuilder.ErrorCode.NONE, "");
      }

      private static DungeonShellBuilder.ValidationResult failure(DungeonShellBuilder.ErrorCode error, String message) {
         return new DungeonShellBuilder.ValidationResult(false, error, message);
      }
   }
}
