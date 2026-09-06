package dev.eness.sololevelingfinal.core.dungeon.builder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.builder.model.RoomSnapshot;

public final class DungeonBuilderRoomStore {
   private static final String STORE_DIRECTORY = "slr_builder/room_snapshots";

   private DungeonBuilderRoomStore() {
   }

   public static DungeonBuilderRoomStore.CaptureResult capture(ServerPlayer player, DungeonBuilderProjectData.Project project, long metadataRevision) {
      DungeonBuilderProjectData.Bounds bounds = project.structureBounds();
      if (bounds == null) {
         return DungeonBuilderRoomStore.CaptureResult.failure("Select Structure Bounds before capturing this room.");
      }

      BlockPos captureSize = bounds.size();
      if (captureSize.getX() <= 48 && captureSize.getY() <= 48 && captureSize.getZ() <= 48 && bounds.volume() <= 110592L) {
         ServerLevel level = player.serverLevel();
         String readiness = captureReadinessProblem(level, project, bounds);
         if (readiness != null) {
            return DungeonBuilderRoomStore.CaptureResult.failure(readiness);
         }

         ResourceLocation structureKey = new ResourceLocation(project.namespace(), "slr_dungeons/" + project.name());

         Path destination;
         try {
            destination = snapshotPath(player, structureKey);
         } catch (IOException exception) {
            return DungeonBuilderRoomStore.CaptureResult.failure(exception.getMessage());
         }

         Path staging = destination.resolveSibling("." + destination.getFileName() + "." + UUID.randomUUID() + ".tmp");

         try {
            Files.createDirectories(destination.getParent());
            StructureTemplate template = new StructureTemplate();
            BlockPos size = bounds.size();
            template.fillFromWorld(level, bounds.min(), new Vec3i(size.getX(), size.getY(), size.getZ()), false, Blocks.STRUCTURE_VOID);
            template.setAuthor(player.getGameProfile().getName());
            CompoundTag structureTag = NbtUtils.addCurrentDataVersion(template.save(new CompoundTag()));
            NbtIo.writeCompressed(structureTag, staging.toFile());
            String checksum = checksum(staging);
            moveAtomically(staging, destination);
            RoomSnapshot snapshot = new RoomSnapshot(
               structureKey, size.immutable(), bounds.min().immutable(), checksum, System.currentTimeMillis(), Math.max(0L, metadataRevision)
            );
            project.setRoomSnapshot(snapshot);
            return DungeonBuilderRoomStore.CaptureResult.success(snapshot, destination, "Captured " + project.id() + " as a versioned room snapshot.");
         } catch (Exception exception) {
            try {
               Files.deleteIfExists(staging);
            } catch (IOException cleanupException) {
               SololevelingMod.LOGGER.warn("Could not remove failed room snapshot staging file {}", staging, cleanupException);
            }

            SololevelingMod.LOGGER.error("Could not capture Dungeon Builder room {}", project.id(), exception);
            return DungeonBuilderRoomStore.CaptureResult.failure("Room capture failed. Check latest.log for the exact file error.");
         }
      } else {
         return DungeonBuilderRoomStore.CaptureResult.failure("Room snapshots are limited to 48 blocks per axis (48 x 48 x 48 maximum).");
      }
   }

   public static DungeonBuilderRoomStore.CopyResult copyVerified(ServerPlayer player, DungeonBuilderProjectData.Project project, Path destination) {
      Optional<RoomSnapshot> optionalSnapshot = project.roomSnapshot();
      if (optionalSnapshot.isEmpty()) {
         return DungeonBuilderRoomStore.CopyResult.failure("Room " + project.id() + " has no captured snapshot. Open the Studio and choose Capture Room.");
      }

      RoomSnapshot snapshot = optionalSnapshot.get();

      try {
         Path source = snapshotPath(player, snapshot.structureKey());
         if (!Files.isRegularFile(source)) {
            return DungeonBuilderRoomStore.CopyResult.failure("The captured snapshot file for " + project.id() + " is missing. Capture the room again.");
         }

         String actualChecksum = checksum(source);
         if (!MessageDigest.isEqual(actualChecksum.getBytes(StandardCharsets.US_ASCII), snapshot.checksum().getBytes(StandardCharsets.US_ASCII))) {
            return DungeonBuilderRoomStore.CopyResult.failure("The captured snapshot for " + project.id() + " failed its checksum. Capture the room again.");
         }

         Path normalizedDestination = destination.toAbsolutePath().normalize();
         Files.createDirectories(normalizedDestination.getParent());
         Files.copy(source, normalizedDestination, StandardCopyOption.REPLACE_EXISTING);
         return DungeonBuilderRoomStore.CopyResult.success(normalizedDestination);
      } catch (Exception exception) {
         SololevelingMod.LOGGER.error("Could not copy Dungeon Builder snapshot for {}", project.id(), exception);
         return DungeonBuilderRoomStore.CopyResult.failure("Could not read the captured snapshot for " + project.id() + ". Check latest.log.");
      }
   }

   public static DungeonBuilderRoomStore.VerificationResult verify(ServerPlayer player, DungeonBuilderProjectData.Project project) {
      Optional<RoomSnapshot> optionalSnapshot = project.roomSnapshot();
      if (optionalSnapshot.isEmpty()) {
         return new DungeonBuilderRoomStore.VerificationResult(false, "NOT CAPTURED", null);
      }

      RoomSnapshot snapshot = optionalSnapshot.get();

      try {
         Path source = snapshotPath(player, snapshot.structureKey());
         if (!Files.isRegularFile(source)) {
            return new DungeonBuilderRoomStore.VerificationResult(false, "SNAPSHOT FILE MISSING", source);
         } else {
            return !checksum(source).equalsIgnoreCase(snapshot.checksum())
               ? new DungeonBuilderRoomStore.VerificationResult(false, "CHECKSUM FAILED", source)
               : new DungeonBuilderRoomStore.VerificationResult(true, "CAPTURED", source);
         }
      } catch (Exception exception) {
         return new DungeonBuilderRoomStore.VerificationResult(false, "SNAPSHOT UNREADABLE", null);
      }
   }

   public static DungeonBuilderRoomStore.Footprint footprint(ServerPlayer player, DungeonBuilderProjectData.Project project) {
      Optional<RoomSnapshot> optionalSnapshot = project.roomSnapshot();
      if (optionalSnapshot.isEmpty()) {
         return DungeonBuilderRoomStore.Footprint.empty();
      }

      try {
         Path source = snapshotPath(player, optionalSnapshot.get().structureKey());
         if (!Files.isRegularFile(source)) {
            return DungeonBuilderRoomStore.Footprint.empty();
         }

         CompoundTag structure = NbtIo.readCompressed(source.toFile());
         ListTag size = structure.getList("size", 3);
         if (size.size() < 3) {
            return DungeonBuilderRoomStore.Footprint.empty();
         }

         int width = Math.max(0, Math.min(48, size.getInt(0)));
         int depth = Math.max(0, Math.min(48, size.getInt(2)));
         if (width != 0 && depth != 0) {
            ListTag palette = structure.getList("palette", 10);
            int[] paletteColors = new int[palette.size()];

            for (int index = 0; index < palette.size(); index++) {
               String name = palette.getCompound(index).getString("Name");
               paletteColors[index] = footprintColor(name);
            }

            int[] colors = new int[width * depth];
            int[] heights = new int[colors.length];
            Arrays.fill(heights, Integer.MIN_VALUE);
            ListTag blocks = structure.getList("blocks", 10);

            for (int index = 0; index < blocks.size(); index++) {
               CompoundTag block = blocks.getCompound(index);
               ListTag position = block.getList("pos", 3);
               if (position.size() >= 3) {
                  int x = position.getInt(0);
                  int y = position.getInt(1);
                  int z = position.getInt(2);
                  int paletteIndex = block.getInt("state");
                  if (x >= 0 && z >= 0 && x < width && z < depth && paletteIndex >= 0 && paletteIndex < paletteColors.length) {
                     int color = paletteColors[paletteIndex];
                     if (color != 0) {
                        int cell = z * width + x;
                        if (y >= heights[cell]) {
                           heights[cell] = y;
                           colors[cell] = color;
                        }
                     }
                  }
               }
            }

            return new DungeonBuilderRoomStore.Footprint(width, depth, colors);
         } else {
            return DungeonBuilderRoomStore.Footprint.empty();
         }
      } catch (Exception exception) {
         SololevelingMod.LOGGER.warn("Could not build room-snapshot footprint for {}", project.id(), exception);
         return DungeonBuilderRoomStore.Footprint.empty();
      }
   }

   private static int footprintColor(String blockId) {
      if (blockId != null
         && !blockId.isBlank()
         && !blockId.equals("minecraft:air")
         && !blockId.equals("minecraft:cave_air")
         && !blockId.equals("minecraft:void_air")
         && !blockId.equals("minecraft:structure_void")) {
         int hash = blockId.hashCode();
         int red = 72 + Math.floorMod(hash, 112);
         int green = 72 + Math.floorMod(hash >>> 8, 112);
         int blue = 72 + Math.floorMod(hash >>> 16, 112);
         return 0xFF000000 | red << 16 | green << 8 | blue;
      } else {
         return 0;
      }
   }

   @Nullable
   private static String captureReadinessProblem(ServerLevel level, DungeonBuilderProjectData.Project project, DungeonBuilderProjectData.Bounds bounds) {
      if (bounds.min().getY() >= level.getMinBuildHeight() && bounds.max().getY() < level.getMaxBuildHeight()) {
         for (int chunkX = bounds.min().getX() >> 4; chunkX <= bounds.max().getX() >> 4; chunkX++) {
            for (int chunkZ = bounds.min().getZ() >> 4; chunkZ <= bounds.max().getZ() >> 4; chunkZ++) {
               if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) {
                  return "All selected chunks must be loaded. Move near the room and try again.";
               }
            }
         }

         for (DungeonBuilderProjectData.Socket socket : project.sockets()) {
            DungeonBuilderProjectData.Bounds opening = socket.opening();

            for (int x = opening.min().getX(); x <= opening.max().getX(); x++) {
               for (int y = opening.min().getY(); y <= opening.max().getY(); y++) {
                  for (int z = opening.min().getZ(); z <= opening.max().getZ(); z++) {
                     if (level.getBlockEntity(new BlockPos(x, y, z)) != null) {
                        return "Socket " + socket.id() + " contains a block entity. Move it out of the opening before capture.";
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return "Selected bounds extend outside the world's build height.";
      }
   }

   private static Path snapshotPath(ServerPlayer player, ResourceLocation structureKey) throws IOException {
      Path root = player.serverLevel()
         .getServer()
         .getWorldPath(LevelResource.ROOT)
         .toAbsolutePath()
         .normalize()
         .resolve("slr_builder/room_snapshots")
         .normalize();
      Path ownerRoot = root.resolve(player.getUUID().toString()).normalize();
      Path namespaceRoot = ownerRoot.resolve(structureKey.getNamespace()).normalize();
      Path path = namespaceRoot.resolve(structureKey.getPath() + ".nbt").normalize();
      if (ownerRoot.startsWith(root) && namespaceRoot.startsWith(ownerRoot) && path.startsWith(namespaceRoot)) {
         return path;
      } else {
         throw new IOException("Unsafe room snapshot path was rejected.");
      }
   }

   private static void moveAtomically(Path source, Path destination) throws IOException {
      try {
         Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException ignored) {
         Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
      }
   }

   private static String checksum(Path path) throws IOException, NoSuchAlgorithmException {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      try (InputStream input = Files.newInputStream(path)) {
         byte[] buffer = new byte[8192];

         int read;
         while ((read = input.read(buffer)) >= 0) {
            if (read > 0) {
               digest.update(buffer, 0, read);
            }
         }
      }

      return HexFormat.of().formatHex(digest.digest());
   }

   public record CaptureResult(boolean success, String message, @Nullable RoomSnapshot snapshot, @Nullable Path path) {
      private static DungeonBuilderRoomStore.CaptureResult success(RoomSnapshot snapshot, Path path, String message) {
         return new DungeonBuilderRoomStore.CaptureResult(true, message, snapshot, path);
      }

      private static DungeonBuilderRoomStore.CaptureResult failure(String message) {
         return new DungeonBuilderRoomStore.CaptureResult(false, message, null, null);
      }
   }

   public record CopyResult(boolean success, String message, @Nullable Path path) {
      private static DungeonBuilderRoomStore.CopyResult success(Path path) {
         return new DungeonBuilderRoomStore.CopyResult(true, "Ready.", path);
      }

      private static DungeonBuilderRoomStore.CopyResult failure(String message) {
         return new DungeonBuilderRoomStore.CopyResult(false, message, null);
      }
   }

   public record Footprint(int width, int depth, int[] colors) {
      public Footprint {
         width = Math.max(0, Math.min(48, width));
         depth = Math.max(0, Math.min(48, depth));
         int maximum = width * depth;
         colors = colors == null ? new int[maximum] : Arrays.copyOf(colors, maximum);
      }

      public static DungeonBuilderRoomStore.Footprint empty() {
         return new DungeonBuilderRoomStore.Footprint(0, 0, new int[0]);
      }
   }

   public record VerificationResult(boolean valid, String status, @Nullable Path path) {
   }
}
