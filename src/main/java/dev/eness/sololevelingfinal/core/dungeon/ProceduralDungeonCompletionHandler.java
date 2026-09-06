package dev.eness.sololevelingfinal.core.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonInstanceSavedData;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonReturnPortalSpawner;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public final class ProceduralDungeonCompletionHandler {
   public static final String PROCEDURAL_DUNGEON_TAG = "slr_procedural_dungeon";
   public static final String EXIT_HANDLED_TAG = "slr_procedural_exit_handled";

   private ProceduralDungeonCompletionHandler() {
   }

   public static boolean isProceduralCompletion(Entity boss, @Nullable Entity creditedSource) {
      return hasProceduralTag(boss) || hasProceduralTag(creditedSource);
   }

   public static void markProceduralMob(Entity entity) {
      if (entity != null) {
         entity.getPersistentData().putBoolean("slr_procedural_dungeon", true);
      }
   }

   public static void markExitHandled(Entity boss) {
      if (boss != null) {
         boss.getPersistentData().putBoolean("slr_procedural_exit_handled", true);
      }
   }

   public static boolean isExitHandled(Entity boss) {
      return boss != null && boss.getPersistentData().getBoolean("slr_procedural_exit_handled");
   }

   public static void recordUnscopedEntrant(ServerPlayer player, String dungeonTag, ResourceKey<Level> dungeonDimension) {
      if (player != null && dungeonDimension != null) {
         ProceduralGateRunSavedData.get(player.server).recordEntrant(dungeonTag, player.getUUID(), dungeonDimension, player.serverLevel().getGameTime());
      }
   }

   public static void removeUnscopedEntrant(ServerPlayer player, String dungeonTag) {
      if (player != null) {
         ProceduralGateRunSavedData.get(player.server).removeEntrant(dungeonTag, player.getUUID());
      }
   }

   public static void preserveLegacyUnscopedRoster(ServerPlayer player, String dungeonTag, ResourceKey<Level> dungeonDimension) {
      if (player != null && dungeonDimension != null) {
         ProceduralGateRunSavedData.get(player.server).recordLegacyRun(dungeonTag, dungeonDimension, player.serverLevel().getGameTime());
      }
   }

   public static void recordUnscopedReturnAnchor(ServerLevel level, String dungeonTag, BlockPos returnAnchor) {
      if (level != null && returnAnchor != null) {
         ProceduralGateRunSavedData.get(level.getServer()).recordReturnAnchor(dungeonTag, level.dimension(), returnAnchor, level.getGameTime());
      }
   }

   @Nullable
   public static BlockPos unscopedReturnAnchor(ServerLevel level, String dungeonTag) {
      return level == null
         ? null
         : ProceduralGateRunSavedData.get(level.getServer())
            .run(dungeonTag)
            .filter(run -> run.dimension().equals(level.dimension()))
            .map(ProceduralGateRunSavedData.RunView::returnAnchor)
            .orElse(null);
   }

   public static BlockPos resolveUnscopedReturnPosition(ServerLevel level, String dungeonTag) {
      if (level == null) {
         return null;
      }

      BlockPos desired = unscopedReturnAnchor(level, dungeonTag);
      if (desired == null) {
         desired = level.players()
            .stream()
            .filter(player -> dungeonTag != null && dungeonTag.equals(player.getPersistentData().getString("dungeon_tag")))
            .map(Entity::blockPosition)
            .findFirst()
            .orElseGet(() -> level.getSharedSpawnPos().above());
      }

      return safeReturnPortalPosition(level, desired);
   }

   public static BlockPos safeReturnPortalPosition(ServerLevel level, BlockPos desired) {
      if (level != null && desired != null) {
         if (!loadReturnPortalChunk(level, desired)) {
            return desired.immutable();
         }

         int[] verticalOffsets = new int[]{0, 1, -1, 2, -2};

         for (int radius = 0; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                     for (int dy : verticalOffsets) {
                        BlockPos candidate = desired.offset(dx, dy, dz);
                        if (isSafeReturnPortalPosition(level, candidate)) {
                           return candidate.immutable();
                        }
                     }
                  }
               }
            }
         }

         return desired.immutable();
      } else {
         return null;
      }
   }

   public static boolean loadReturnPortalChunk(ServerLevel level, BlockPos position) {
      if (level != null && position != null) {
         level.getChunkAt(position);
         return level.hasChunkAt(position);
      } else {
         return false;
      }
   }

   private static boolean isSafeReturnPortalPosition(ServerLevel level, BlockPos position) {
      if (!level.hasChunkAt(position)) {
         return false;
      } else {
         BlockPos floorPos = position.below();
         BlockState floor = level.getBlockState(floorPos);
         if (floor.isFaceSturdy(level, floorPos, Direction.UP) && floor.getFluidState().isEmpty()) {
            AABB footprint = new AABB(
               position.getX() - 0.1, position.getY(), position.getZ() - 0.1, position.getX() + 1.1, position.getY() + 3.6, position.getZ() + 1.1
            );
            return !level.noCollision(null, footprint)
               ? false
               : level.getEntitiesOfClass(
                     LivingEntity.class, footprint.inflate(0.35), entity -> entity.isAlive() && entity.getType() != SololevelingModEntities.PORTAL_12.get()
                  )
                  .isEmpty();
         } else {
            return false;
         }
      }
   }

   public static Optional<List<ServerPlayer>> activeUnscopedParticipants(ServerLevel level, String dungeonTag) {
      if (level != null && dungeonTag != null && !dungeonTag.isBlank()) {
         ProceduralGateRunSavedData.RunView run = ProceduralGateRunSavedData.get(level.getServer()).run(dungeonTag).orElse(null);
         if (run != null && run.authoritativeRoster() && !run.participants().isEmpty() && run.dimension().equals(level.dimension())) {
            List<ServerPlayer> participants = new ArrayList<>();

            for (UUID participantId : run.participants()) {
               ServerPlayer participant = level.getServer().getPlayerList().getPlayer(participantId);
               if (participant == null
                  || participant.serverLevel() != level
                  || !dungeonTag.equals(participant.getPersistentData().getString("dungeon_tag"))
                  || !participant.getPersistentData().getBoolean("slr_procedural_dungeon")) {
                  return Optional.empty();
               }

               participants.add(participant);
            }

            return Optional.of(List.copyOf(participants));
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   public static void migrateLegacyUnscopedRuns(MinecraftServer server) {
      if (server != null) {
         ProceduralGateRunSavedData runs = ProceduralGateRunSavedData.get(server);

         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String dungeonTag = player.getPersistentData().getString("dungeon_tag");
            if (!player.serverLevel().dimension().equals(Level.OVERWORLD)
               && player.getPersistentData().getBoolean("slr_procedural_dungeon")
               && player.getPersistentData().getString("slr_dungeon_instance").isBlank()
               && !dungeonTag.isBlank()
               && !runs.run(dungeonTag).isPresent()) {
               runs.recordLegacyRun(dungeonTag, player.serverLevel().dimension(), player.serverLevel().getGameTime());
            }
         }
      }
   }

   public static boolean isUnscopedRunDecided(MinecraftServer server, String dungeonTag) {
      return server == null
         ? false
         : ProceduralGateRunSavedData.get(server).run(dungeonTag).map(run -> run.decision() != ProceduralGateRunSavedData.ExitDecision.UNDECIDED).orElse(false);
   }

   public static void chooseUnscopedReturnPortal(ServerLevel level, String dungeonTag, BlockPos exit) {
      if (level != null && exit != null) {
         ProceduralGateRunSavedData.get(level.getServer()).chooseReturnPortal(dungeonTag, level.dimension(), exit, level.getGameTime());
      }
   }

   public static void chooseCartenonExit(ServerLevel level, String dungeonTag) {
      if (level != null) {
         ProceduralGateRunSavedData.get(level.getServer()).chooseCartenon(dungeonTag, level.dimension(), level.getGameTime());
      }
   }

   public static void ensureUnscopedReturnPortals(MinecraftServer server) {
      if (server != null) {
         for (ProceduralGateRunSavedData.ReturnRequest request : ProceduralGateRunSavedData.get(server).returnRequests()) {
            ServerLevel level = server.getLevel(request.dimension());
            if (level != null) {
               boolean entrantPresent = level.players()
                  .stream()
                  .anyMatch(
                     player -> request.dungeonTag().equals(player.getPersistentData().getString("dungeon_tag"))
                        && player.getPersistentData().getBoolean("slr_procedural_dungeon")
                  );
               if (entrantPresent && loadReturnPortalChunk(level, request.exit()) && !reconcileReturnPortal(level, null, request.dungeonTag(), request.exit())) {
                  spawnUnscopedReturnPortal(level, request.exit(), request.dungeonTag());
               }
            }
         }
      }
   }

   public static boolean spawnUnscopedReturnPortal(ServerLevel level, BlockPos position, String dungeonTag) {
      if (level != null && position != null && loadReturnPortalChunk(level, position)) {
         discardMatchingReturnPortals(level, null, dungeonTag);
         discardReturnPortalsAt(level, position);
         return DungeonReturnPortalSpawner.spawnUnscoped(level, position, dungeonTag) != null;
      } else {
         return false;
      }
   }

   public static boolean spawnScopedReturnPortal(
      ServerLevel level, BlockPos position, @Nullable Direction facing, DungeonInstanceSavedData.Instance instance, String dungeonTag
   ) {
      if (level != null && position != null && instance != null && loadReturnPortalChunk(level, position)) {
         discardMatchingReturnPortals(level, instance.id(), dungeonTag);
         discardReturnPortalsAt(level, position);
         return DungeonReturnPortalSpawner.spawn(level, position, facing, instance.id(), dungeonTag) != null;
      } else {
         return false;
      }
   }

   public static boolean reconcileReturnPortal(ServerLevel level, @Nullable UUID instanceId, String dungeonTag, BlockPos expectedPosition) {
      if (level != null && expectedPosition != null) {
         String instanceText = instanceId == null ? "" : instanceId.toString();
         String cleanDungeonTag = dungeonTag == null ? "" : dungeonTag.trim();
         Entity kept = null;
         List<Entity> duplicates = new ArrayList<>();

         for (Entity entity : level.getAllEntities()) {
            if (entity.getType() == SololevelingModEntities.PORTAL_12.get()) {
               boolean atExpectedPosition = expectedPosition.equals(entity.blockPosition());
               boolean matchingInstance = !instanceText.isEmpty() && instanceText.equals(entity.getPersistentData().getString("slr_dungeon_instance"));
               boolean matchingDungeon = !cleanDungeonTag.isEmpty() && cleanDungeonTag.equals(entity.getPersistentData().getString("dungeon_tag"));
               if (atExpectedPosition || matchingInstance || matchingDungeon) {
                  if (atExpectedPosition && kept == null) {
                     kept = entity;
                  } else {
                     duplicates.add(entity);
                  }
               }
            }
         }

         duplicates.forEach(Entity::discard);
         if (kept == null) {
            return false;
         }

         if (!instanceText.isEmpty()) {
            kept.getPersistentData().putString("slr_dungeon_instance", instanceText);
         }

         if (!cleanDungeonTag.isEmpty()) {
            kept.getPersistentData().putString("dungeon_tag", cleanDungeonTag);
         }

         return true;
      } else {
         return false;
      }
   }

   public static void discardMatchingReturnPortals(ServerLevel level, @Nullable UUID instanceId, String dungeonTag) {
      if (level != null) {
         String instanceText = instanceId == null ? "" : instanceId.toString();
         String cleanDungeonTag = dungeonTag == null ? "" : dungeonTag.trim();
         List<Entity> matches = new ArrayList<>();

         for (Entity entity : level.getAllEntities()) {
            if (entity.getType() == SololevelingModEntities.PORTAL_12.get()) {
               boolean matchingInstance = !instanceText.isEmpty() && instanceText.equals(entity.getPersistentData().getString("slr_dungeon_instance"));
               boolean matchingDungeon = !cleanDungeonTag.isEmpty() && cleanDungeonTag.equals(entity.getPersistentData().getString("dungeon_tag"));
               if (matchingInstance || matchingDungeon) {
                  matches.add(entity);
               }
            }
         }

         matches.forEach(Entity::discard);
      }
   }

   private static void discardReturnPortalsAt(ServerLevel level, BlockPos position) {
      if (level != null && position != null) {
         List<Entity> matches = new ArrayList<>();

         for (Entity entity : level.getAllEntities()) {
            if (entity.getType() == SololevelingModEntities.PORTAL_12.get() && position.equals(entity.blockPosition())) {
               matches.add(entity);
            }
         }

         matches.forEach(Entity::discard);
      }
   }

   public static boolean hasMatchingReturnPortal(ServerLevel level, @Nullable UUID instanceId, String dungeonTag) {
      if (level == null) {
         return false;
      }

      String instanceText = instanceId == null ? "" : instanceId.toString();
      String cleanDungeonTag = dungeonTag == null ? "" : dungeonTag.trim();

      for (Entity entity : level.getAllEntities()) {
         if (entity.getType() == SololevelingModEntities.PORTAL_12.get()) {
            if (!instanceText.isEmpty() && instanceText.equals(entity.getPersistentData().getString("slr_dungeon_instance"))) {
               return true;
            }

            if (!cleanDungeonTag.isEmpty() && cleanDungeonTag.equals(entity.getPersistentData().getString("dungeon_tag"))) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isDuplicateReturnPortal(ServerLevel level, Entity candidate) {
      if (level != null && candidate != null && candidate.getType() == SololevelingModEntities.PORTAL_12.get()) {
         BlockPos position = candidate.blockPosition();

         for (Entity entity : level.getAllEntities()) {
            if (entity != candidate && entity.getType() == SololevelingModEntities.PORTAL_12.get() && position.equals(entity.blockPosition())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isObsoleteScopedReturnPortal(ServerLevel level, Entity entity) {
      if (level != null && entity != null && entity.getType() == SololevelingModEntities.PORTAL_12.get()) {
         String dungeonTag = entity.getPersistentData().getString("dungeon_tag");
         ProceduralGateRunSavedData.RunView unscopedRun = ProceduralGateRunSavedData.get(level.getServer()).run(dungeonTag).orElse(null);
         if (unscopedRun != null) {
            if (unscopedRun.decision() == ProceduralGateRunSavedData.ExitDecision.UNDECIDED) {
               return true;
            }

            if (unscopedRun.decision() == ProceduralGateRunSavedData.ExitDecision.CARTENON) {
               return true;
            }

            if (unscopedRun.decision() == ProceduralGateRunSavedData.ExitDecision.RETURN_PORTAL
               && (unscopedRun.exit() == null || !unscopedRun.exit().equals(entity.blockPosition()))) {
               return true;
            }
         }

         String instanceText = entity.getPersistentData().getString("slr_dungeon_instance");
         if (instanceText.isBlank()) {
            return false;
         }

         try {
            DungeonInstanceSavedData.Instance instance = DungeonInstanceSavedData.get(level).getInstance(UUID.fromString(instanceText)).orElse(null);
            if (instance == null) {
               return true;
            } else if (instance.returnPortalSuppressed()) {
               return true;
            } else if (!instance.returnPortalDeferred()) {
               return false;
            } else {
               return !instance.completed() ? true : instance.exit().map(exit -> !exit.equals(entity.blockPosition())).orElse(true);
            }
         } catch (IllegalArgumentException ignored) {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean hasProceduralTag(@Nullable Entity entity) {
      return entity != null && entity.getPersistentData().getBoolean("slr_procedural_dungeon");
   }
}
