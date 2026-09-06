package dev.eness.sololevelingfinal.core.dungeon.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonCompletionHandler;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataTypes;
import dev.eness.sololevelingfinal.core.dungeon.data.MobPoolResolver;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.ShadowKillCreditHelper;
import dev.eness.sololevelingfinal.core.util.CartenonTempleManager;
import dev.eness.sololevelingfinal.core.util.EntityHighlightSystem;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

@EventBusSubscriber
public final class DungeonEncounterRuntime {
   private static final int ACTIVATION_INTERVAL = 10;
   private static final int FAILED_ACTIVATION_RETRY_TICKS = 200;
   private static final int MISSING_MOB_RECOVERY_TICKS = 600;
   private static final Map<String, Long> RETRY_AFTER = new HashMap<>();
   private static final Map<String, Long> MISSING_SINCE = new HashMap<>();
   private static int tickCounter;

   private DungeonEncounterRuntime() {
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END && ++tickCounter % 10 == 0) {
         MinecraftServer server = event.getServer();
         ProceduralDungeonCompletionHandler.migrateLegacyUnscopedRuns(server);
         DungeonInstanceSavedData registry = DungeonInstanceSavedData.get(server);

         for (DungeonInstanceSavedData.Instance instance : registry.listInstances()) {
            if (instance.completed()) {
               ensureDeferredReturnPortal(server, instance);
            } else {
               ServerLevel level = server.getLevel(instance.dimension());
               if (level != null) {
                  for (DungeonInstanceSavedData.EncounterState encounter : instance.encounters()) {
                     if (!encounter.completed()) {
                        if (encounter.activated()) {
                           reconcileActivatedEncounter(level, instance, encounter);
                        } else if (encounter.sequenced()
                           ? sequencedWaveReady(level, instance, encounter)
                           : (encounter.triggerBounds().isPresent() ? playerEntered(level, instance, encounter) : markerChunksLoaded(level, encounter))) {
                           String retryKey = retryKey(instance, encounter);
                           if (RETRY_AFTER.getOrDefault(retryKey, 0L) <= level.getGameTime()) {
                              if (activate(level, instance, encounter)) {
                                 RETRY_AFTER.remove(retryKey);
                              } else {
                                 RETRY_AFTER.put(retryKey, level.getGameTime() + 200L);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         ProceduralDungeonCompletionHandler.ensureUnscopedReturnPortals(server);
      }
   }

   private static boolean sequencedWaveReady(ServerLevel level, DungeonInstanceSavedData.Instance instance, DungeonInstanceSavedData.EncounterState encounter) {
      if (!sequencePrerequisitesCompleted(instance, encounter)) {
         return false;
      }

      if (encounter.scheduledGameTime() < 0L) {
         if (encounter.sequenceOrder() == 0) {
            boolean triggerReady = encounter.triggerBounds().isPresent() ? playerEntered(level, instance, encounter) : markerChunksLoaded(level, encounter);
            if (!triggerReady) {
               return false;
            }
         }

         encounter.scheduleActivation(activationTime(level.getGameTime(), encounter.delayTicks()));
      }

      return encounter.scheduledGameTime() >= 0L && level.getGameTime() >= encounter.scheduledGameTime() && markerChunksLoaded(level, encounter);
   }

   private static boolean sequencePrerequisitesCompleted(DungeonInstanceSavedData.Instance instance, DungeonInstanceSavedData.EncounterState encounter) {
      if (encounter.sequenced() && encounter.sequenceOrder() != 0) {
         boolean foundImmediatePredecessor = false;

         for (DungeonInstanceSavedData.EncounterState candidate : instance.encounters()) {
            if (candidate.sequenced() && candidate.sequenceKey().equals(encounter.sequenceKey())) {
               if (candidate.sequenceOrder() == encounter.sequenceOrder() - 1) {
                  foundImmediatePredecessor = true;
               }

               if (candidate.sequenceOrder() < encounter.sequenceOrder() && !candidate.completed()) {
                  return false;
               }
            }
         }

         return foundImmediatePredecessor;
      } else {
         return true;
      }
   }

   private static long activationTime(long gameTime, int delayTicks) {
      long safeTime = Math.max(0L, gameTime);
      return safeTime > Long.MAX_VALUE - delayTicks ? Long.MAX_VALUE : safeTime + delayTicks;
   }

   private static boolean playerEntered(ServerLevel level, DungeonInstanceSavedData.Instance instance, DungeonInstanceSavedData.EncounterState encounter) {
      Optional<DungeonInstanceSavedData.Bounds> trigger = encounter.triggerBounds();
      if (trigger.isEmpty()) {
         return false;
      }

      for (ServerPlayer player : level.players()) {
         if ((instance.participants().isEmpty() || instance.participants().contains(player.getUUID()))
            && trigger.get().contains(BlockPos.containing(player.position()))) {
            return true;
         }
      }

      return false;
   }

   private static boolean markerChunksLoaded(ServerLevel level, DungeonInstanceSavedData.EncounterState encounter) {
      return !encounter.markers().isEmpty() && encounter.markers().stream().allMatch(marker -> level.hasChunkAt(marker.position()));
   }

   private static boolean activate(ServerLevel level, DungeonInstanceSavedData.Instance instance, DungeonInstanceSavedData.EncounterState encounter) {
      if (!encounter.activate()) {
         return false;
      }

      List<Mob> spawnedMobs = new ArrayList<>();
      String compatibilityTag = level.players()
         .stream()
         .filter(player -> instance.participants().isEmpty() || instance.participants().contains(player.getUUID()))
         .map(player -> player.getPersistentData().getString("dungeon_tag"))
         .filter(value -> !value.isBlank())
         .findFirst()
         .orElse(instance.id().toString());

      for (DungeonInstanceSavedData.EncounterMarker marker : encounter.markers()) {
         Optional<MobPoolResolver.Selection> selection = MobPoolResolver.select(
            level,
            encounter.poolId(),
            instance.effectiveLevel(),
            encounter.levelOverride() ? Optional.of(new DungeonDataTypes.IntRange(encounter.minSpawnLevel(), encounter.maxSpawnLevel())) : Optional.empty(),
            level.random
         );
         if (selection.isEmpty()) {
            SololevelingMod.LOGGER
               .warn("Dungeon encounter {} in instance {} could not resolve mob pool {}", encounter.key(), instance.id(), encounter.poolId());
         } else {
            DungeonMobLevelAdapter.MobRole role = DungeonMobLevelAdapter.MobRole.fromString(marker.role());
            DungeonMobLevelAdapter.SpawnSpec spec = new DungeonMobLevelAdapter.SpawnSpec(
               instance.id().toString(), encounter.key(), marker.id(), role, selection.get().level(), compatibilityTag, selection.get().baseXp().orElse(-1)
            );
            DungeonMobLevelAdapter.SpawnResult result = DungeonMobLevelAdapter.spawnExact(
               level, selection.get().entityType(), marker.position(), level.random.nextFloat() * 360.0F, spec
            );
            if (result.succeeded() && result.mob() != null) {
               spawnedMobs.add(result.mob());
            } else {
               SololevelingMod.LOGGER.warn("Dungeon encounter {} could not spawn marker {}: {}", encounter.key(), marker.id(), result.message());
            }
         }
      }

      if (spawnedMobs.size() != encounter.markers().size()) {
         spawnedMobs.forEach(Entity::discard);
         encounter.resetProgress();
         return false;
      }

      for (Mob mob : spawnedMobs) {
         if (!encounter.trackMob(mob.getUUID())) {
            spawnedMobs.forEach(Entity::discard);
            encounter.resetProgress();
            return false;
         }

         mob.getPersistentData().remove("slr_dungeon_pending_track");
      }

      syncEncounterHighlights(level, instance, encounter);
      return true;
   }

   private static void reconcileActivatedEncounter(
      ServerLevel level, DungeonInstanceSavedData.Instance instance, DungeonInstanceSavedData.EncounterState encounter
   ) {
      String key = retryKey(instance, encounter);
      if (encounter.trackedMobs().isEmpty()) {
         encounter.resetProgress();
         RETRY_AFTER.put(key, level.getGameTime() + 200L);
         MISSING_SINCE.remove(key);
      } else {
         boolean activationAreaReady = !encounter.sequenced() && !encounter.triggerBounds().isEmpty()
            ? playerEntered(level, instance, encounter)
            : markerChunksLoaded(level, encounter);
         boolean markerChunksLoaded = markerChunksLoaded(level, encounter);
         boolean anyLoadedMob = encounter.trackedMobs().stream().anyMatch(id -> level.getEntity(id) != null);
         if (activationAreaReady && markerChunksLoaded && !anyLoadedMob) {
            long missingSince = MISSING_SINCE.computeIfAbsent(key, ignored -> level.getGameTime());
            if (level.getGameTime() - missingSince >= 600L) {
               SololevelingMod.LOGGER
                  .warn("Resetting dungeon encounter {} in instance {} after tracked mobs were missing for {} ticks", encounter.key(), instance.id(), 600);
               clearEncounterHighlights(level.getServer(), instance, encounter);
               encounter.resetProgress();
               MISSING_SINCE.remove(key);
               RETRY_AFTER.put(key, level.getGameTime() + 200L);
            }
         } else {
            MISSING_SINCE.remove(key);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLivingDeath(LivingDeathEvent event) {
      LivingEntity entity = event.getEntity();
      if (!entity.level().isClientSide()) {
         CompoundTag data = entity.getPersistentData();
         String instanceText = data.getString("slr_dungeon_instance");
         String encounterKey = data.getString("slr_dungeon_encounter");
         if (!instanceText.isBlank() && !encounterKey.isBlank()) {
            UUID instanceId;
            try {
               instanceId = UUID.fromString(instanceText);
            } catch (IllegalArgumentException ignored) {
               return;
            }

            ServerLevel level = (ServerLevel)entity.level();
            Optional<DungeonInstanceSavedData.Instance> found = DungeonInstanceSavedData.get(level).getInstance(instanceId);
            if (!found.isEmpty()) {
               DungeonInstanceSavedData.Instance instance = found.get();
               Optional<DungeonInstanceSavedData.EncounterState> state = instance.encounter(encounterKey);
               if (!state.isEmpty() && state.get().untrackMob(entity.getUUID())) {
                  DungeonInstanceSavedData.EncounterState encounter = state.get();
                  clearEncounterHighlight(level.getServer(), instance, entity.getUUID());
                  RETRY_AFTER.remove(retryKey(instance, encounter));
                  MISSING_SINCE.remove(retryKey(instance, encounter));
                  if (encounter.trackedMobs().isEmpty()) {
                     encounter.markCompleted();
                     if (encounter.sequenced()) {
                        scheduleNextWave(level, instance, encounter);
                     }

                     if (encounter.boss() && completionRequirementsMet(instance)) {
                        completeInstance(level.getServer(), instance, entity, event.getSource().getEntity());
                     }
                  }
               }
            }
         }
      }
   }

   private static void scheduleNextWave(ServerLevel level, DungeonInstanceSavedData.Instance instance, DungeonInstanceSavedData.EncounterState completed) {
      instance.encounters()
         .stream()
         .filter(
            candidate -> candidate.sequenced()
               && candidate.sequenceKey().equals(completed.sequenceKey())
               && candidate.sequenceOrder() == completed.sequenceOrder() + 1
         )
         .findFirst()
         .ifPresent(next -> next.scheduleActivation(activationTime(level.getGameTime(), next.delayTicks())));
   }

   private static boolean completionRequirementsMet(DungeonInstanceSavedData.Instance instance) {
      List<DungeonInstanceSavedData.EncounterState> encounters = instance.encounters();
      if (encounters.stream().filter(DungeonInstanceSavedData.EncounterState::boss).anyMatch(encounter -> !encounter.completed())) {
         return false;
      }

      for (DungeonInstanceSavedData.EncounterState boss : encounters) {
         if (boss.boss() && boss.sequenced()) {
            boolean terminal = encounters.stream()
               .noneMatch(
                  candidate -> candidate.sequenced() && candidate.sequenceKey().equals(boss.sequenceKey()) && candidate.sequenceOrder() > boss.sequenceOrder()
               );
            boolean predecessorsComplete = encounters.stream()
               .filter(
                  candidate -> candidate.sequenced() && candidate.sequenceKey().equals(boss.sequenceKey()) && candidate.sequenceOrder() < boss.sequenceOrder()
               )
               .allMatch(DungeonInstanceSavedData.EncounterState::completed);
            if (!terminal || !predecessorsComplete) {
               return false;
            }
         }
      }

      return true;
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel level) {
         if (ProceduralDungeonCompletionHandler.isDuplicateReturnPortal(level, event.getEntity())
            || ProceduralDungeonCompletionHandler.isObsoleteScopedReturnPortal(level, event.getEntity())) {
            event.setCanceled(true);
            event.getEntity().discard();
         } else if (event.getEntity() instanceof LivingEntity entity && entity.getPersistentData().getBoolean("slr_dungeon_spawned")) {
            String instanceText = entity.getPersistentData().getString("slr_dungeon_instance");
            String encounterKey = entity.getPersistentData().getString("slr_dungeon_encounter");

            try {
               Optional<DungeonInstanceSavedData.Instance> found = DungeonInstanceSavedData.get(level).getInstance(UUID.fromString(instanceText));
               boolean pendingTrack = entity.getPersistentData().getBoolean("slr_dungeon_pending_track");
               Optional<DungeonInstanceSavedData.EncounterState> state = found.flatMap(instance -> instance.encounter(encounterKey));
               boolean tracked = state.<Boolean>map(value -> value.trackedMobs().contains(entity.getUUID())).orElse(false);
               boolean valid = found.isPresent()
                  && !found.get().completed()
                  && state.<Boolean>map(value -> !value.completed() && (pendingTrack || tracked)).orElse(false);
               if (!valid) {
                  event.setCanceled(true);
                  entity.discard();
               } else if (tracked) {
                  DungeonMobLevelAdapter.MobRole role = DungeonMobLevelAdapter.MobRole.fromString(entity.getPersistentData().getString("slr_dungeon_role"));
                  syncTargetHighlights(level.getServer(), found.get(), entity.getUUID(), role);
               }
            } catch (IllegalArgumentException ignored) {
               event.setCanceled(true);
               entity.discard();
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         syncActiveHighlights(player);
         String currentInstance = player.getPersistentData().getString("slr_dungeon_instance");
         boolean completedParticipant = !currentInstance.isBlank()
            && DungeonInstanceSavedData.get(player.serverLevel())
               .listInstances()
               .stream()
               .anyMatch(
                  instance -> instance.completed() && instance.id().toString().equals(currentInstance) && instance.participants().contains(player.getUUID())
               );
         if (completedParticipant) {
            setBossKilledCompatibility(player);
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         syncActiveHighlights(player);
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      RETRY_AFTER.clear();
      MISSING_SINCE.clear();
      tickCounter = 0;
   }

   public static boolean restoreCompletionFor(ServerPlayer player, DungeonInstanceSavedData.Instance instance) {
      if (player != null && instance != null && instance.completed() && instance.participants().contains(player.getUUID())) {
         player.getPersistentData().putString("slr_dungeon_instance", instance.id().toString());
         setBossKilledCompatibility(player);
         return true;
      } else {
         return false;
      }
   }

   private static void completeInstance(
      MinecraftServer server, DungeonInstanceSavedData.Instance instance, @Nullable LivingEntity defeatedBoss, @Nullable Entity damageSource
   ) {
      if (instance.setCompleted(true)) {
         clearInstanceHighlights(server, instance);
         ServerLevel level = server.getLevel(instance.dimension());
         boolean managedExit = instance.returnPortalDeferred() || SnowRedGateArenaManager.isArenaInstance(instance);
         if (level != null && defeatedBoss != null && managedExit) {
            BlockPos exitPosition = instance.exit().or(() -> instance.playerStart()).orElse(null);
            if (instance.returnPortalDeferred() && exitPosition != null) {
               exitPosition = ProceduralDungeonCompletionHandler.safeReturnPortalPosition(level, exitPosition);
               instance.setExit(exitPosition);
            }

            String dungeonTag = completionDungeonTag(server, instance, defeatedBoss);
            Entity creditedSource = ShadowKillCreditHelper.creditedSourceForDeath(level, defeatedBoss, damageSource, null);
            Optional<List<ServerPlayer>> exactParticipants = activeInstanceParticipants(server, level, instance);
            boolean cartenonSpawned = exactParticipants.isPresent()
               && CartenonTempleManager.onDungeonBossDefeated(level, defeatedBoss, creditedSource, dungeonTag, exactParticipants.get());
            instance.setReturnPortalSuppressed(cartenonSpawned);
            if (cartenonSpawned) {
               ProceduralDungeonCompletionHandler.chooseCartenonExit(level, dungeonTag);
            }

            if (cartenonSpawned) {
               ProceduralDungeonCompletionHandler.discardMatchingReturnPortals(level, instance.id(), dungeonTag);
               ProceduralDungeonCompletionHandler.markExitHandled(defeatedBoss);
            } else if (exitPosition != null) {
               boolean portalReady = ProceduralDungeonCompletionHandler.reconcileReturnPortal(level, instance.id(), dungeonTag, exitPosition)
                  || ProceduralDungeonCompletionHandler.spawnScopedReturnPortal(level, exitPosition, instance.exitFacing().orElse(null), instance, dungeonTag);
               if (portalReady) {
                  ProceduralDungeonCompletionHandler.markExitHandled(defeatedBoss);
               }
            }
         }

         for (DungeonInstanceSavedData.EncounterState encounter : instance.encounters()) {
            RETRY_AFTER.remove(retryKey(instance, encounter));
            MISSING_SINCE.remove(retryKey(instance, encounter));
            if (level != null) {
               for (UUID mobId : encounter.trackedMobs()) {
                  if (level.getEntity(mobId) != null) {
                     level.getEntity(mobId).discard();
                  }
               }
            }

            encounter.clearTrackedMobs();
            encounter.markCompleted();
         }

         for (UUID participant : instance.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(participant);
            if (player != null && instance.id().toString().equals(player.getPersistentData().getString("slr_dungeon_instance"))) {
               setBossKilledCompatibility(player);
               SystemNotifications.showTitleUnder(player, -11665528, 90, Component.literal("DUNGEON CLEARED"), Component.literal("Boss defeated."));
            }
         }

         SololevelingMod.LOGGER.info("Completed dungeon instance {} ({})", instance.id(), instance.dungeonId());
      }
   }

   private static Optional<List<ServerPlayer>> activeInstanceParticipants(MinecraftServer server, ServerLevel level, DungeonInstanceSavedData.Instance instance) {
      if (instance.participants().isEmpty()) {
         return Optional.empty();
      }

      String instanceText = instance.id().toString();
      List<ServerPlayer> participants = new ArrayList<>();

      for (UUID participantId : instance.participants()) {
         ServerPlayer participant = server.getPlayerList().getPlayer(participantId);
         if (participant == null
            || participant.serverLevel() != level
            || !instanceText.equals(participant.getPersistentData().getString("slr_dungeon_instance"))) {
            return Optional.empty();
         }

         participants.add(participant);
      }

      return Optional.of(List.copyOf(participants));
   }

   private static void ensureDeferredReturnPortal(MinecraftServer server, DungeonInstanceSavedData.Instance instance) {
      if (instance.returnPortalDeferred() && !instance.returnPortalSuppressed() && !SnowRedGateArenaManager.isArenaInstance(instance)) {
         ServerLevel level = server.getLevel(instance.dimension());
         BlockPos exit = instance.exit().orElse(null);
         if (level != null && exit != null) {
            boolean participantPresent = level.players()
               .stream()
               .anyMatch(
                  player -> instance.participants().contains(player.getUUID())
                     && instance.id().toString().equals(player.getPersistentData().getString("slr_dungeon_instance"))
               );
            if (participantPresent
               && ProceduralDungeonCompletionHandler.loadReturnPortalChunk(level, exit)
               && !ProceduralDungeonCompletionHandler.reconcileReturnPortal(level, instance.id(), "", exit)) {
               String dungeonTag = completionDungeonTag(server, instance, null);
               ProceduralDungeonCompletionHandler.spawnScopedReturnPortal(level, exit, instance.exitFacing().orElse(null), instance, dungeonTag);
            }
         }
      }
   }

   private static String completionDungeonTag(MinecraftServer server, DungeonInstanceSavedData.Instance instance, @Nullable Entity boss) {
      if (boss != null) {
         String bossTag = boss.getPersistentData().getString("dungeon_tag");
         if (!bossTag.isBlank()) {
            return bossTag;
         }
      }

      for (UUID participantId : instance.participants()) {
         ServerPlayer participant = server.getPlayerList().getPlayer(participantId);
         if (participant != null) {
            String participantTag = participant.getPersistentData().getString("dungeon_tag");
            if (!participantTag.isBlank()) {
               return participantTag;
            }
         }
      }

      return instance.id().toString();
   }

   private static void setBossKilledCompatibility(ServerPlayer player) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
         variables.BossKilled = true;
         variables.syncPlayerVariables(player);
      });
   }

   private static void syncEncounterHighlights(ServerLevel level, DungeonInstanceSavedData.Instance instance, DungeonInstanceSavedData.EncounterState encounter) {
      for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
         if (canReceiveInstanceHighlights(player, instance)) {
            for (UUID targetId : encounter.trackedMobs()) {
               Entity target = level.getEntity(targetId);
               DungeonMobLevelAdapter.MobRole role = roleFor(target, encounter);
               EntityHighlightSystem.show(
                  player,
                  targetId,
                  instance.dimension(),
                  EntityHighlightSystem.dungeonSource(instance.id()),
                  EntityHighlightSystem.dungeonColor(role),
                  0,
                  EntityHighlightSystem.dungeonPriority(role)
               );
            }
         }
      }
   }

   private static void syncActiveHighlights(ServerPlayer player) {
      DungeonInstanceSavedData registry = DungeonInstanceSavedData.get(player.server);

      for (DungeonInstanceSavedData.Instance instance : registry.listInstances()) {
         if (!instance.completed() && canReceiveInstanceHighlights(player, instance)) {
            ServerLevel level = player.server.getLevel(instance.dimension());
            if (level != null) {
               for (DungeonInstanceSavedData.EncounterState encounter : instance.encounters()) {
                  if (encounter.activated() && !encounter.completed()) {
                     for (UUID targetId : encounter.trackedMobs()) {
                        Entity target = level.getEntity(targetId);
                        DungeonMobLevelAdapter.MobRole role = roleFor(target, encounter);
                        EntityHighlightSystem.show(
                           player,
                           targetId,
                           instance.dimension(),
                           EntityHighlightSystem.dungeonSource(instance.id()),
                           EntityHighlightSystem.dungeonColor(role),
                           0,
                           EntityHighlightSystem.dungeonPriority(role)
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private static void clearEncounterHighlight(MinecraftServer server, DungeonInstanceSavedData.Instance instance, UUID targetId) {
      String source = EntityHighlightSystem.dungeonSource(instance.id());

      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
         if (canReceiveInstanceHighlights(player, instance)) {
            EntityHighlightSystem.hide(player, targetId, instance.dimension(), source);
         }
      }
   }

   public static void clearEncounterHighlights(
      MinecraftServer server, DungeonInstanceSavedData.Instance instance, DungeonInstanceSavedData.EncounterState encounter
   ) {
      if (server != null && instance != null && encounter != null) {
         String source = EntityHighlightSystem.dungeonSource(instance.id());
         List<UUID> targetIds = List.copyOf(encounter.trackedMobs());

         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (UUID targetId : targetIds) {
               EntityHighlightSystem.hide(player, targetId, instance.dimension(), source);
            }
         }
      }
   }

   public static void clearHighlightsFor(ServerPlayer player, DungeonInstanceSavedData.Instance instance) {
      if (player != null && instance != null) {
         EntityHighlightSystem.clearSource(player, EntityHighlightSystem.dungeonSource(instance.id()));
      }
   }

   private static void syncTargetHighlights(
      MinecraftServer server, DungeonInstanceSavedData.Instance instance, UUID targetId, DungeonMobLevelAdapter.MobRole role
   ) {
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
         if (canReceiveInstanceHighlights(player, instance)) {
            EntityHighlightSystem.show(
               player,
               targetId,
               instance.dimension(),
               EntityHighlightSystem.dungeonSource(instance.id()),
               EntityHighlightSystem.dungeonColor(role),
               0,
               EntityHighlightSystem.dungeonPriority(role)
            );
         }
      }
   }

   public static void clearInstanceHighlights(MinecraftServer server, DungeonInstanceSavedData.Instance instance) {
      if (server != null && instance != null) {
         String source = EntityHighlightSystem.dungeonSource(instance.id());

         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            EntityHighlightSystem.clearSource(player, source);
         }
      }
   }

   private static boolean canReceiveInstanceHighlights(ServerPlayer player, DungeonInstanceSavedData.Instance instance) {
      return instance.participants().contains(player.getUUID())
         ? true
         : instance.participants().isEmpty() && instance.id().toString().equals(player.getPersistentData().getString("slr_dungeon_instance"));
   }

   private static DungeonMobLevelAdapter.MobRole roleFor(Entity target, DungeonInstanceSavedData.EncounterState encounter) {
      if (target != null) {
         return DungeonMobLevelAdapter.MobRole.fromString(target.getPersistentData().getString("slr_dungeon_role"));
      } else {
         return encounter.boss() ? DungeonMobLevelAdapter.MobRole.BOSS : DungeonMobLevelAdapter.MobRole.NORMAL;
      }
   }

   private static String retryKey(DungeonInstanceSavedData.Instance instance, DungeonInstanceSavedData.EncounterState encounter) {
      return instance.id() + ":" + encounter.key();
   }
}
