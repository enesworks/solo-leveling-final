package dev.eness.sololevelingfinal.core.dungeon.builder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import org.joml.Vector3f;

public final class DungeonBuilderPreview {
   private static final DustParticleOptions BOUNDS = dust(3532799, 1.05F);
   private static final DustParticleOptions REGION = dust(11558143, 0.85F);
   private static final DustParticleOptions SOCKET = dust(16752429, 1.15F);
   private static final DustParticleOptions FEATURE = dust(16767037, 1.05F);
   private static final DustParticleOptions ENCOUNTER = dust(16727384, 1.05F);

   private DungeonBuilderPreview() {
   }

   public static void show(ServerPlayer player, DungeonBuilderProjectData.Project project) {
      for (int delay : new int[]{1, 11, 21, 31}) {
         SololevelingMod.queueServerWork(delay, () -> {
            if (player.isAlive()) {
               showOnce(player, project);
            }
         });
      }
   }

   private static void showOnce(ServerPlayer player, DungeonBuilderProjectData.Project project) {
      ServerLevel level = player.serverLevel();
      if (project.structureBounds() != null) {
         box(level, player, project.structureBounds(), BOUNDS);
      }

      for (DungeonBuilderProjectData.Region region : project.regions()) {
         box(level, player, region.bounds(), REGION);
      }

      for (DungeonBuilderProjectData.Socket socket : project.sockets()) {
         box(level, player, socket.opening(), SOCKET);
         socketFacing(level, player, socket);
      }

      for (DungeonBuilderProjectData.Marker marker : project.markers()) {
         point(level, player, marker.position(), isEncounter(marker.type()) ? ENCOUNTER : FEATURE);
      }

      if (project.origin() != null) {
         point(level, player, project.origin(), BOUNDS);
      }

      if (project.pendingPosition() != null) {
         point(level, player, project.pendingPosition(), REGION);
      }
   }

   private static void box(ServerLevel level, ServerPlayer player, DungeonBuilderProjectData.Bounds bounds, DustParticleOptions particle) {
      BlockPos min = bounds.min();
      BlockPos max = bounds.max();
      int longest = Math.max(max.getX() - min.getX(), Math.max(max.getY() - min.getY(), max.getZ() - min.getZ()));
      int step = Math.max(1, longest / 20);

      for (int x = min.getX(); x <= max.getX(); x += step) {
         point(level, player, new BlockPos(x, min.getY(), min.getZ()), particle);
         point(level, player, new BlockPos(x, min.getY(), max.getZ()), particle);
         point(level, player, new BlockPos(x, max.getY(), min.getZ()), particle);
         point(level, player, new BlockPos(x, max.getY(), max.getZ()), particle);
      }

      for (int y = min.getY(); y <= max.getY(); y += step) {
         point(level, player, new BlockPos(min.getX(), y, min.getZ()), particle);
         point(level, player, new BlockPos(min.getX(), y, max.getZ()), particle);
         point(level, player, new BlockPos(max.getX(), y, min.getZ()), particle);
         point(level, player, new BlockPos(max.getX(), y, max.getZ()), particle);
      }

      for (int z = min.getZ(); z <= max.getZ(); z += step) {
         point(level, player, new BlockPos(min.getX(), min.getY(), z), particle);
         point(level, player, new BlockPos(min.getX(), max.getY(), z), particle);
         point(level, player, new BlockPos(max.getX(), min.getY(), z), particle);
         point(level, player, new BlockPos(max.getX(), max.getY(), z), particle);
      }
   }

   private static void point(ServerLevel level, ServerPlayer player, BlockPos pos, DustParticleOptions particle) {
      level.sendParticles(player, particle, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.03, 0.03, 0.03, 0.0);
   }

   private static void socketFacing(ServerLevel level, ServerPlayer player, DungeonBuilderProjectData.Socket socket) {
      DungeonBuilderProjectData.Bounds opening = socket.opening();
      double centerX = (opening.min().getX() + opening.max().getX() + 1.0) / 2.0;
      double centerY = (opening.min().getY() + opening.max().getY() + 1.0) / 2.0;
      double centerZ = (opening.min().getZ() + opening.max().getZ() + 1.0) / 2.0;

      for (int step = 0; step <= 4; step++) {
         double distance = step * 0.45;
         level.sendParticles(
            player,
            SOCKET,
            true,
            centerX + socket.facing().getStepX() * distance,
            centerY + socket.facing().getStepY() * distance,
            centerZ + socket.facing().getStepZ() * distance,
            1,
            0.015,
            0.015,
            0.015,
            0.0
         );
      }
   }

   private static boolean isEncounter(String type) {
      return type.contains("spawn") || type.equals("trigger_region");
   }

   private static DustParticleOptions dust(int rgb, float scale) {
      return new DustParticleOptions(new Vector3f((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F), scale);
   }
}
