package dev.eness.sololevelingfinal.core.dungeon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataManager;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataSnapshot;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataTypes;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDefinition;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonEncounterRuntime;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonInstanceSavedData;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonRuntimeGenerator;
import dev.eness.sololevelingfinal.core.entity.DatapackGateEntity;
import dev.eness.sololevelingfinal.core.entity.Portal1Entity;
import dev.eness.sololevelingfinal.core.guild.GuildGateHelper;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.MagicReadingHelper;
import dev.eness.sololevelingfinal.core.util.PlayerEntryGenerationGuard;
import dev.eness.sololevelingfinal.core.util.UrgentQuestManager;

public final class DatapackDungeonGateHandler {
   private static final String GATE_MARKER = "slr_datapack_gate";
   private static final String MANUAL = "slr_datapack_gate_manual";
   private static final String AWAITING_SELECTION = "slr_datapack_gate_awaiting_selection";
   private static final String DUNGEON = "slr_datapack_gate_dungeon";
   private static final String RANK = "slr_datapack_gate_rank";
   private static final String BOUND_REVISION = "slr_datapack_gate_bound_revision";
   private static final String GENERATED = "slr_datapack_gate_generated";
   private static final String GENERATING = "slr_datapack_gate_generating";
   private static final String INSTANCE = "slr_datapack_gate_instance";
   private static final String START_X = "slr_datapack_gate_start_x";
   private static final String START_Y = "slr_datapack_gate_start_y";
   private static final String START_Z = "slr_datapack_gate_start_z";
   private static final String SAVE_STATE = "SLRDatapackGate";
   private static final String PLAYER_PROCEDURAL = "slr_procedural_dungeon";
   private static final String PLAYER_PROCEDURAL_RED = "slr_procedural_red_gate";
   private static final String LEGACY_RANK = "slr_procedural_rank";
   private static final double MAX_SELECTION_DISTANCE_SQR = 64.0;
   private static final double PARTY_DISTANCE = 10.0;

   private DatapackDungeonGateHandler() {
   }

   public static DatapackDungeonGateHandler.OptionSnapshot options() {
      DungeonDataSnapshot snapshot = DungeonDataManager.snapshot();
      List<DatapackDungeonGateHandler.DungeonOption> values = snapshot.dungeons()
         .values()
         .stream()
         .map(definition -> new DatapackDungeonGateHandler.DungeonOption(definition.id(), definition.kind(), definition.allowedRanks()))
         .toList();
      return new DatapackDungeonGateHandler.OptionSnapshot(snapshot.revision(), values);
   }

   public static List<DatapackDungeonGateHandler.DungeonOption> optionsForRank(ProceduralDungeonRank rank) {
      return rank == null ? List.of() : options().options().stream().filter(option -> option.allowedRanks().contains(rank)).toList();
   }

   public static boolean hasAvailableDungeons() {
      return !DungeonDataManager.snapshot().dungeons().isEmpty();
   }

   public static boolean isDatapackGate(Entity entity) {
      return entity instanceof DatapackGateEntity && entity.getPersistentData().getBoolean("slr_datapack_gate");
   }

   public static boolean isManual(DatapackGateEntity gate) {
      return gate != null && gate.getPersistentData().getBoolean("slr_datapack_gate_manual");
   }

   public static boolean isAwaitingSelection(DatapackGateEntity gate) {
      return gate != null && gate.getPersistentData().getBoolean("slr_datapack_gate_awaiting_selection") && binding(gate).isEmpty() && !isGenerated(gate);
   }

   public static boolean isGenerated(DatapackGateEntity gate) {
      return gate != null && gate.getPersistentData().getBoolean("slr_datapack_gate_generated");
   }

   public static Optional<ResourceLocation> binding(DatapackGateEntity gate) {
      if (gate == null) {
         return Optional.empty();
      }

      String value = gate.getPersistentData().getString("slr_datapack_gate_dungeon");
      return value.isBlank() ? Optional.empty() : Optional.ofNullable(ResourceLocation.tryParse(value));
   }

   public static Optional<ProceduralDungeonRank> rank(DatapackGateEntity gate) {
      return gate == null ? Optional.empty() : ProceduralDungeonRank.tryParse(gate.getPersistentData().getString("slr_datapack_gate_rank"));
   }

   public static void initializeSpawn(DatapackGateEntity gate, MobSpawnType reason) {
      if (gate != null) {
         CompoundTag data = gate.getPersistentData();
         data.putBoolean("slr_datapack_gate", true);
         boolean manual = reason == MobSpawnType.SPAWN_EGG;
         data.putBoolean("slr_datapack_gate_manual", manual);
         data.putBoolean("slr_datapack_gate_awaiting_selection", manual);
         data.putBoolean("slr_datapack_gate_generated", false);
         data.putBoolean("slr_datapack_gate_generating", false);
         data.remove("slr_datapack_gate_dungeon");
         data.remove("slr_datapack_gate_rank");
         data.remove("slr_datapack_gate_bound_revision");
         data.remove("slr_datapack_gate_instance");
         data.remove("slr_procedural_rank");
         data.remove("slr_procedural_gate");
         data.remove("slr_procedural_red_gate");
         data.remove("slr_is_red_gate");
         gate.setTexture("gate_zero_purple");
      }
   }

   public static void discardInvalidUnboundSpawn(DatapackGateEntity gate) {
      if (gate != null && !gate.isRemoved() && !binding(gate).isPresent()) {
         if (!isManual(gate) || !hasAvailableDungeons()) {
            gate.discard();
         }
      }
   }

   public static boolean bind(DatapackGateEntity gate, ResourceLocation dungeonId, ProceduralDungeonRank gateRank) {
      if (gate != null && !gate.isRemoved() && dungeonId != null && gateRank != null && !isGenerated(gate) && !binding(gate).isPresent()) {
         DungeonDataSnapshot snapshot = DungeonDataManager.snapshot();
         Optional<DungeonDefinition> definition = snapshot.dungeon(dungeonId);
         if (!definition.isEmpty() && definition.get().supportsRank(gateRank)) {
            CompoundTag data = gate.getPersistentData();
            data.putBoolean("slr_datapack_gate", true);
            data.putString("slr_datapack_gate_dungeon", dungeonId.toString());
            data.putString("slr_datapack_gate_rank", gateRank.name());
            data.putString("slr_procedural_rank", gateRank.name());
            data.putLong("slr_datapack_gate_bound_revision", snapshot.revision());
            data.putBoolean("slr_datapack_gate_awaiting_selection", false);
            data.putBoolean("slr_datapack_gate_generating", false);
            gate.setTexture("gate_zero_purple");
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static DatapackDungeonGateHandler.BindingResult bindSelection(
      ServerPlayer player, DatapackGateEntity gate, ResourceLocation dungeonId, ProceduralDungeonRank gateRank
   ) {
      return bindSelection(player, gate, dungeonId, gateRank, DungeonDataManager.snapshot().revision());
   }

   public static DatapackDungeonGateHandler.BindingResult bindSelection(
      ServerPlayer player, DatapackGateEntity gate, ResourceLocation dungeonId, ProceduralDungeonRank gateRank, long expectedRevision
   ) {
      if (player != null && gate != null && dungeonId != null && gateRank != null) {
         if (gate.isRemoved() || player.level() != gate.level() || player.distanceToSqr(gate) > 64.0) {
            return DatapackDungeonGateHandler.BindingResult.failure("This gate is no longer close enough to configure.");
         }

         if (isManual(gate) && isAwaitingSelection(gate)) {
            DungeonDataSnapshot snapshot = DungeonDataManager.snapshot();
            if (snapshot.revision() != expectedRevision) {
               return DatapackDungeonGateHandler.BindingResult.failure("Datapacks changed while this screen was open. Choose again.");
            } else {
               Optional<DungeonDefinition> definition = snapshot.dungeon(dungeonId);
               if (definition.isEmpty()) {
                  return DatapackDungeonGateHandler.BindingResult.failure("That datapack dungeon is no longer loaded or valid.");
               } else if (!definition.get().supportsRank(gateRank)) {
                  return DatapackDungeonGateHandler.BindingResult.failure("That dungeon does not support " + gateRank.name() + "-rank gates.");
               } else {
                  return !bind(gate, dungeonId, gateRank)
                     ? DatapackDungeonGateHandler.BindingResult.failure("The datapack gate could not be bound.")
                     : DatapackDungeonGateHandler.BindingResult.ok("Configured " + gateRank.name() + "-rank gate for " + dungeonId + ".");
               }
            }
         } else {
            return DatapackDungeonGateHandler.BindingResult.failure("This gate is no longer awaiting a selection.");
         }
      } else {
         return DatapackDungeonGateHandler.BindingResult.failure("Invalid datapack gate selection.");
      }
   }

   public static void interact(ServerPlayer player, DatapackGateEntity gate) {
      if (player != null && gate != null && !gate.isRemoved()) {
         if (isAwaitingSelection(gate)) {
            DatapackGateSelectionService.requestOpen(player, gate);
         } else {
            Optional<ProceduralDungeonRank> gateRank = rank(gate);
            if (MagicReadingHelper.isHoldingMagicReader(player)) {
               if (gateRank.isPresent()) {
                  MagicReadingHelper.showRankReading(player, gateRank.get());
               } else {
                  player.displayClientMessage(Component.literal("This datapack gate is not configured."), true);
               }
            } else {
               enter(player, gate);
            }
         }
      }
   }

   public static void enter(ServerPlayer player, DatapackGateEntity gate) {
      if (player != null && gate != null && !gate.isRemoved()) {
         Optional<ResourceLocation> dungeonId = binding(gate);
         Optional<ProceduralDungeonRank> gateRank = rank(gate);
         if (!dungeonId.isEmpty() && !gateRank.isEmpty()) {
            Optional<DungeonDefinition> definition = DungeonDataManager.dungeon(dungeonId.get());
            if (definition.isEmpty()) {
               player.displayClientMessage(Component.literal("This gate's datapack dungeon is no longer loaded or valid."), true);
            } else if (!definition.get().supportsRank(gateRank.get())) {
               player.displayClientMessage(
                  Component.literal("This " + gateRank.get().name() + "-rank gate is no longer supported by its datapack dungeon."), true
               );
            } else if (isCompleted(gate, player.server)) {
               player.displayClientMessage(Component.literal("This gate has already been cleared."), true);
            } else if (isDungeonBound(player)) {
               player.displayClientMessage(Component.literal("You are already bound to a dungeon."), true);
            } else if (!GuildGateHelper.prepareGateEntry(gate.level(), gate, player)) {
               List<ServerPlayer> entrants = nearbyPartyMembers(gate, player);
               Map<UUID, Long> entryGenerations = new LinkedHashMap<>();

               for (ServerPlayer entrant : entrants) {
                  prepareEntrant(gate, entrant);
                  entryGenerations.put(entrant.getUUID(), PlayerEntryGenerationGuard.begin(entrant));
               }

               teleportEntrants(gate, dungeonId.get(), gateRank.get(), entrants, entryGenerations);
            }
         } else {
            player.displayClientMessage(Component.literal("This datapack gate is not configured."), true);
         }
      }
   }

   private static void teleportEntrants(
      DatapackGateEntity gate, ResourceLocation dungeonId, ProceduralDungeonRank gateRank, List<ServerPlayer> entrants, Map<UUID, Long> entryGenerations
   ) {
      ServerPlayer firstPlayer = entrants.stream().findFirst().orElse(null);
      if (firstPlayer != null) {
         ServerLevel destination = firstPlayer.server.getLevel(destinationFor(gateRank));
         if (destination == null) {
            failEntrants(entrants, "The dungeon destination dimension is unavailable.");
         } else {
            BlockPos target = storedTarget(gate);
            boolean firstGeneration = !isGenerated(gate);
            if (firstGeneration) {
               if (gate.getPersistentData().getBoolean("slr_datapack_gate_generating")) {
                  failEntrants(entrants, "This datapack gate is already preparing its dungeon.");
                  return;
               }

               gate.getPersistentData().putBoolean("slr_datapack_gate_generating", true);
            }

            SololevelingMod.queueServerWork(
               firstGeneration ? 5 : 0,
               () -> {
                  try {
                     List<ServerPlayer> currentEntrants = currentPreparedEntrants(gate.getStringUUID(), entrants, entryGenerations);
                     if (currentEntrants.isEmpty()) {
                        return;
                     }

                     ServerPlayer currentFirst = currentEntrants.get(0);
                     if (gate.isRemoved()) {
                        failEntrants(currentEntrants, "The datapack gate disappeared before entry finished.");
                        return;
                     }

                     Optional<DungeonDefinition> liveDefinition = DungeonDataManager.dungeon(dungeonId);
                     if (liveDefinition.isEmpty() || !liveDefinition.get().supportsRank(gateRank)) {
                        failEntrants(currentEntrants, "The gate's datapack dungeon changed or became unavailable.");
                        return;
                     }

                     if (isCompleted(gate, currentFirst.server)) {
                        failEntrants(currentEntrants, "This gate was cleared before entry finished.");
                        return;
                     }

                     DungeonInstanceSavedData.Instance instance;
                     if (!isGenerated(gate)) {
                        long seed = gate.getUUID().getMostSignificantBits() ^ gate.getUUID().getLeastSignificantBits() ^ destination.getSeed();
                        DungeonRuntimeGenerator.GenerationResult generated = DungeonRuntimeGenerator.generate(
                           destination, dungeonId, target, seed, currentEntrants, null
                        );
                        if (!generated.success() || generated.instanceId() == null || generated.playerStart() == null) {
                           SololevelingMod.LOGGER.error("Datapack gate {} failed to generate {}: {}", gate.getUUID(), dungeonId, generated.message());
                           failEntrants(
                              currentEntrants, "Datapack dungeon generation failed. The gate was not consumed; check the Builder validation or server log."
                           );
                           return;
                        }

                        gate.getPersistentData().putString("slr_datapack_gate_instance", generated.instanceId().toString());
                        gate.getPersistentData().putBoolean("slr_datapack_gate_generated", true);
                        storeStart(gate, generated.playerStart());
                        gate.getEntityData().set(Portal1Entity.DATA_usedbefore, true);
                        instance = DungeonInstanceSavedData.get(destination).getInstance(generated.instanceId()).orElse(null);
                     } else {
                        instance = boundInstance(gate, destination).orElse(null);
                     }

                     if (instance == null) {
                        failEntrants(currentEntrants, "This gate no longer has valid dungeon instance state.");
                        return;
                     }

                     instance.setReturnPortalDeferred(true);
                     instance.setReturnPortalSuppressed(false);
                     ProceduralDungeonCompletionHandler.discardMatchingReturnPortals(destination, instance.id(), gate.getStringUUID());
                     BlockPos playerStart = instance.playerStart().orElseGet(() -> storedStart(gate));

                     for (ServerPlayer entrant : currentEntrants) {
                        enterInstance(destination, gate, instance, playerStart, entrant);
                     }
                  } finally {
                     gate.getPersistentData().putBoolean("slr_datapack_gate_generating", false);
                  }
               }
            );
         }
      }
   }

   private static List<ServerPlayer> currentPreparedEntrants(String gateId, List<ServerPlayer> entrants, Map<UUID, Long> entryGenerations) {
      return entrants.stream()
         .filter(
            entrant -> {
               Long generation = entryGenerations.get(entrant.getUUID());
               return generation != null
                  && PlayerEntryGenerationGuard.isCurrent(entrant, generation)
                  && gateId.equals(entrant.getPersistentData().getString("dungeon_tag"))
                  && entrant.getPersistentData().getBoolean("slr_procedural_dungeon")
                  && entrant.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.dungeoning).orElse(false);
            }
         )
         .toList();
   }

   private static void enterInstance(
      ServerLevel destination, DatapackGateEntity gate, DungeonInstanceSavedData.Instance instance, BlockPos playerStart, ServerPlayer entrant
   ) {
      if (!instance.participants().contains(entrant.getUUID()) && !instance.addParticipant(entrant.getUUID())) {
         resetFailedEntrant(entrant);
         entrant.sendSystemMessage(Component.literal("This dungeon already has the maximum number of participants."));
      } else {
         entrant.getPersistentData().putString("slr_dungeon_instance", instance.id().toString());
         UrgentQuestManager.markDungeonId(entrant, instance.dungeonId().toString());
         DungeonEncounterRuntime.restoreCompletionFor(entrant, instance);
         entrant.teleportTo(destination, playerStart.getX() + 0.5, playerStart.getY(), playerStart.getZ() + 0.5, entrant.getYRot(), entrant.getXRot());
         entrant.setNoGravity(false);
         entrant.fallDistance = 0.0F;
      }
   }

   private static Optional<DungeonInstanceSavedData.Instance> boundInstance(DatapackGateEntity gate, ServerLevel level) {
      String value = gate.getPersistentData().getString("slr_datapack_gate_instance");
      if (value.isBlank()) {
         return Optional.empty();
      }

      try {
         return DungeonInstanceSavedData.get(level).getInstance(UUID.fromString(value));
      } catch (IllegalArgumentException ignored) {
         return Optional.empty();
      }
   }

   private static boolean isCompleted(DatapackGateEntity gate, MinecraftServer server) {
      String gateToken = gate.getStringUUID() + ",";
      return SololevelingModVariables.MapVariables.get(gate.level()).GatesCleared.contains(gateToken)
         ? true
         : boundInstance(gate, server.overworld()).map(DungeonInstanceSavedData.Instance::completed).orElse(false);
   }

   private static List<ServerPlayer> nearbyPartyMembers(DatapackGateEntity gate, ServerPlayer leader) {
      String party = partyOf(leader);
      if (party.isBlank()) {
         return List.of(leader);
      }

      List<ServerPlayer> entrants = new ArrayList<>();
      entrants.add(leader);

      for (ServerPlayer candidate : leader.server.getPlayerList().getPlayers()) {
         if (entrants.size() >= 64) {
            break;
         }

         if (candidate != leader
            && candidate.level() == gate.level()
            && !(candidate.distanceTo(gate) > 10.0)
            && party.equals(partyOf(candidate))
            && !isDungeonBound(candidate)) {
            entrants.add(candidate);
         }
      }

      return List.copyOf(entrants);
   }

   private static String partyOf(ServerPlayer player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.party).orElse("");
   }

   private static boolean isDungeonBound(ServerPlayer player) {
      return !player.getPersistentData().getString("slr_dungeon_instance").isBlank()
         ? true
         : player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.dungeoning).orElse(false);
   }

   private static void prepareEntrant(DatapackGateEntity gate, ServerPlayer entrant) {
      dismissOwnedShadows(gate, entrant);
      entrant.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.DunX = entrant.getX();
         capability.DunY = entrant.getY();
         capability.DunZ = entrant.getZ();
         capability.dungeoning = true;
         capability.BossKilled = false;
         capability.syncPlayerVariables(entrant);
      });
      entrant.getPersistentData().putString("dungeon_tag", gate.getStringUUID());
      entrant.getPersistentData().putBoolean("slr_procedural_dungeon", true);
      entrant.getPersistentData().putBoolean("slr_procedural_red_gate", false);
      entrant.getPersistentData().remove("slr_dungeon_instance");
   }

   private static void dismissOwnedShadows(DatapackGateEntity gate, ServerPlayer owner) {
      Vec3 center = gate.position();
      AABB bounds = new AABB(center, center).inflate(250.0);
      List<Entity> nearby = gate.level()
         .getEntitiesOfClass(Entity.class, bounds, entityx -> true)
         .stream()
         .sorted(Comparator.comparingDouble(entityx -> entityx.distanceToSqr(center)))
         .toList();
      TagKey<EntityType<?>> shadows = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows"));

      for (Entity entity : nearby) {
         if (entity.getType().is(shadows) && entity instanceof TamableAnimal tame && owner instanceof LivingEntity && tame.isOwnedBy(owner)) {
            entity.discard();
         }
      }
   }

   private static void failEntrants(List<ServerPlayer> entrants, String message) {
      for (ServerPlayer entrant : entrants) {
         resetFailedEntrant(entrant);
         entrant.sendSystemMessage(Component.literal(message));
      }
   }

   private static void resetFailedEntrant(ServerPlayer entrant) {
      entrant.setNoGravity(false);
      entrant.fallDistance = 0.0F;
      entrant.getPersistentData().putBoolean("slr_procedural_dungeon", false);
      entrant.getPersistentData().putBoolean("slr_procedural_red_gate", false);
      entrant.getPersistentData().remove("dungeon_tag");
      entrant.getPersistentData().remove("slr_dungeon_instance");
      entrant.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.dungeoning = false;
         capability.BossKilled = false;
         capability.syncPlayerVariables(entrant);
      });
   }

   private static BlockPos storedTarget(DatapackGateEntity gate) {
      return BlockPos.containing(
         gate.getPersistentData().getDouble("tpx"), gate.getPersistentData().getDouble("tpy"), gate.getPersistentData().getDouble("tpz")
      );
   }

   private static void storeStart(DatapackGateEntity gate, BlockPos start) {
      gate.getPersistentData().putDouble("slr_datapack_gate_start_x", start.getX());
      gate.getPersistentData().putDouble("slr_datapack_gate_start_y", start.getY());
      gate.getPersistentData().putDouble("slr_datapack_gate_start_z", start.getZ());
   }

   private static BlockPos storedStart(DatapackGateEntity gate) {
      return BlockPos.containing(
         gate.getPersistentData().getDouble("slr_datapack_gate_start_x"),
         gate.getPersistentData().getDouble("slr_datapack_gate_start_y"),
         gate.getPersistentData().getDouble("slr_datapack_gate_start_z")
      );
   }

   private static ResourceKey<Level> destinationFor(ProceduralDungeonRank rank) {
      String path = switch (rank) {
         case E, D -> "dungeon_dimension_d";
         case C -> "dungeon_dimension_c";
         case B -> "dungeon_dimension_b";
         case A -> "dungeon_dimension_a";
         case S -> "dungeon_dimension_s";
      };
      return ResourceKey.create(Registries.DIMENSION, new ResourceLocation("sololeveling", path));
   }

   public static void writeAdditionalSaveData(DatapackGateEntity gate, CompoundTag root) {
      if (gate != null && root != null) {
         CompoundTag source = gate.getPersistentData();
         CompoundTag state = new CompoundTag();
         copyBoolean(source, state, "slr_datapack_gate");
         copyBoolean(source, state, "slr_datapack_gate_manual");
         copyBoolean(source, state, "slr_datapack_gate_awaiting_selection");
         copyBoolean(source, state, "slr_datapack_gate_generated");
         copyBoolean(source, state, "slr_datapack_gate_generating");
         copyString(source, state, "slr_datapack_gate_dungeon");
         copyString(source, state, "slr_datapack_gate_rank");
         copyLong(source, state, "slr_datapack_gate_bound_revision");
         copyString(source, state, "slr_datapack_gate_instance");
         copyDouble(source, state, "slr_datapack_gate_start_x");
         copyDouble(source, state, "slr_datapack_gate_start_y");
         copyDouble(source, state, "slr_datapack_gate_start_z");
         root.put("SLRDatapackGate", state);
      }
   }

   public static void readAdditionalSaveData(DatapackGateEntity gate, CompoundTag root) {
      if (gate != null && root != null && root.contains("SLRDatapackGate", 10)) {
         CompoundTag state = root.getCompound("SLRDatapackGate");
         CompoundTag target = gate.getPersistentData();
         copyBoolean(state, target, "slr_datapack_gate");
         copyBoolean(state, target, "slr_datapack_gate_manual");
         copyBoolean(state, target, "slr_datapack_gate_awaiting_selection");
         copyBoolean(state, target, "slr_datapack_gate_generated");
         copyBoolean(state, target, "slr_datapack_gate_generating");
         copyString(state, target, "slr_datapack_gate_dungeon");
         copyString(state, target, "slr_datapack_gate_rank");
         copyLong(state, target, "slr_datapack_gate_bound_revision");
         copyString(state, target, "slr_datapack_gate_instance");
         copyDouble(state, target, "slr_datapack_gate_start_x");
         copyDouble(state, target, "slr_datapack_gate_start_y");
         copyDouble(state, target, "slr_datapack_gate_start_z");
         rank(gate).ifPresent(value -> target.putString("slr_procedural_rank", value.name()));
         target.remove("slr_procedural_gate");
         target.remove("slr_procedural_red_gate");
         target.remove("slr_is_red_gate");
         target.putBoolean("slr_datapack_gate_generating", false);
         gate.setTexture("gate_zero_purple");
      }
   }

   private static void copyBoolean(CompoundTag from, CompoundTag to, String key) {
      if (from.contains(key, 1)) {
         to.putBoolean(key, from.getBoolean(key));
      }
   }

   private static void copyString(CompoundTag from, CompoundTag to, String key) {
      if (from.contains(key, 8)) {
         to.putString(key, from.getString(key));
      }
   }

   private static void copyLong(CompoundTag from, CompoundTag to, String key) {
      if (from.contains(key, 4)) {
         to.putLong(key, from.getLong(key));
      }
   }

   private static void copyDouble(CompoundTag from, CompoundTag to, String key) {
      if (from.contains(key, 6)) {
         to.putDouble(key, from.getDouble(key));
      }
   }

   public record BindingResult(boolean success, String message) {
      private static DatapackDungeonGateHandler.BindingResult ok(String message) {
         return new DatapackDungeonGateHandler.BindingResult(true, message);
      }

      private static DatapackDungeonGateHandler.BindingResult failure(String message) {
         return new DatapackDungeonGateHandler.BindingResult(false, message);
      }
   }

   public record DungeonOption(ResourceLocation id, DungeonDataTypes.DungeonKind kind, Set<ProceduralDungeonRank> allowedRanks) {
      public DungeonOption {
         allowedRanks = Set.copyOf(allowedRanks);
      }
   }

   public record OptionSnapshot(long revision, List<DatapackDungeonGateHandler.DungeonOption> options) {
      public OptionSnapshot {
         options = List.copyOf(options);
      }
   }
}
