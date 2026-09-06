package dev.eness.sololevelingfinal.core.dungeon.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.saveddata.SavedData;

public final class DungeonInstanceSavedData extends SavedData {
   private static final String DATA_NAME = "sololeveling_dungeon_instances";
   private static final int SCHEMA_VERSION = 5;
   public static final int MAX_INSTANCES = 256;
   public static final int MAX_PARTICIPANTS = 64;
   public static final int MAX_ROOMS = 512;
   public static final int MAX_CONNECTIONS = 1024;
   public static final int MAX_OCCUPIED_VOLUMES = 1536;
   public static final int MAX_ENCOUNTERS = 256;
   public static final int MAX_MARKERS_PER_ENCOUNTER = 256;
   public static final int MAX_TRACKED_MOBS_PER_ENCOUNTER = 2048;
   public static final int MAX_EFFECTIVE_LEVEL = 1000;
   public static final int MAX_WAVE_DELAY_TICKS = 1000000;
   private static final int MAX_TOTAL_PARTICIPANTS_ON_LOAD = 8192;
   private static final int MAX_TOTAL_ROOMS_ON_LOAD = 16384;
   private static final int MAX_TOTAL_CONNECTIONS_ON_LOAD = 32768;
   private static final int MAX_TOTAL_OCCUPIED_VOLUMES_ON_LOAD = 65536;
   private static final int MAX_TOTAL_ENCOUNTERS_ON_LOAD = 8192;
   private static final int MAX_TOTAL_MARKERS_ON_LOAD = 65536;
   private static final int MAX_TOTAL_TRACKED_MOBS_ON_LOAD = 131072;
   private static final int MAX_RESOURCE_ID_LENGTH = 256;
   private static final int MAX_KEY_LENGTH = 128;
   private static final int MAX_MARKER_TEXT_LENGTH = 128;
   private static final int MAX_HORIZONTAL_COORDINATE = 30000000;
   private static final int MAX_ABSOLUTE_VERTICAL_COORDINATE = 4096;
   private static final int MAX_BOUNDS_AXIS = 1024;
   private static final long MAX_BOUNDS_VOLUME = 16777216L;
   private final Map<UUID, DungeonInstanceSavedData.Instance> instances = new LinkedHashMap<>();

   public static DungeonInstanceSavedData get(ServerLevel level) {
      if (level == null) {
         throw new IllegalArgumentException("A server level is required.");
      } else {
         return get(level.getServer());
      }
   }

   public static DungeonInstanceSavedData get(MinecraftServer server) {
      if (server == null) {
         throw new IllegalArgumentException("A Minecraft server is required.");
      }

      ServerLevel overworld = server.overworld();
      return overworld.getDataStorage().computeIfAbsent(DungeonInstanceSavedData::load, DungeonInstanceSavedData::new, "sololeveling_dungeon_instances");
   }

   public DungeonInstanceSavedData.MutationResult<DungeonInstanceSavedData.Instance> create(
      ResourceLocation dungeonId, ResourceKey<Level> dimension, long seed, int effectiveLevel, long createdGameTime
   ) {
      UUID id;
      do {
         id = UUID.randomUUID();
      } while (this.instances.containsKey(id));

      return this.create(id, dungeonId, dimension, seed, effectiveLevel, createdGameTime);
   }

   public DungeonInstanceSavedData.MutationResult<DungeonInstanceSavedData.Instance> create(
      UUID instanceId, ResourceLocation dungeonId, ResourceKey<Level> dimension, long seed, int effectiveLevel, long createdGameTime
   ) {
      String problem = validateInstanceIdentity(instanceId, dungeonId, dimension);
      if (problem != null) {
         return DungeonInstanceSavedData.MutationResult.failure(problem);
      }

      this.pruneCompletedEmptyInstances();
      if (this.instances.size() >= 256) {
         return DungeonInstanceSavedData.MutationResult.failure("The server already has the maximum number of dungeon instances.");
      }

      if (this.instances.containsKey(instanceId)) {
         return DungeonInstanceSavedData.MutationResult.failure("Dungeon instance " + instanceId + " already exists.");
      }

      DungeonInstanceSavedData.Instance instance = new DungeonInstanceSavedData.Instance(
         instanceId, dungeonId, dimension, seed, clampLevel(effectiveLevel), Math.max(0L, createdGameTime), this::setDirty
      );
      this.instances.put(instanceId, instance);
      this.setDirty();
      return DungeonInstanceSavedData.MutationResult.success(instance, "Created dungeon instance " + instanceId + ".");
   }

   public Optional<DungeonInstanceSavedData.Instance> getInstance(UUID instanceId) {
      return Optional.ofNullable(instanceId == null ? null : this.instances.get(instanceId));
   }

   public List<DungeonInstanceSavedData.Instance> listInstances() {
      return List.copyOf(this.instances.values());
   }

   public List<DungeonInstanceSavedData.InstanceView> views() {
      return this.instances.values().stream().map(DungeonInstanceSavedData.Instance::view).toList();
   }

   public Optional<DungeonInstanceSavedData.InstanceView> remove(UUID instanceId) {
      if (instanceId == null) {
         return Optional.empty();
      }

      DungeonInstanceSavedData.Instance removed = this.instances.remove(instanceId);
      if (removed == null) {
         return Optional.empty();
      }

      this.setDirty();
      return Optional.of(removed.view());
   }

   public int size() {
      return this.instances.size();
   }

   public int pruneCompletedEmptyInstances() {
      int before = this.instances.size();
      this.instances.entrySet().removeIf(entry -> entry.getValue().completed() && entry.getValue().participants().isEmpty());
      int removed = before - this.instances.size();
      if (removed > 0) {
         this.setDirty();
      }

      return removed;
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag tag) {
      tag.putInt("SchemaVersion", 5);
      ListTag list = new ListTag();

      for (DungeonInstanceSavedData.Instance instance : this.instances.values()) {
         list.add(instance.save());
      }

      tag.put("Instances", list);
      return tag;
   }

   private static DungeonInstanceSavedData load(CompoundTag root) {
      DungeonInstanceSavedData data = new DungeonInstanceSavedData();
      DungeonInstanceSavedData.LoadBudget budget = new DungeonInstanceSavedData.LoadBudget();
      if (!root.contains("Instances", 9)) {
         return data;
      }

      ListTag list = root.getList("Instances", 10);
      int limit = Math.min(list.size(), 256);
      if (list.size() > limit) {
         budget.sanitized = true;
      }

      for (int index = 0; index < limit; index++) {
         DungeonInstanceSavedData.Instance instance = DungeonInstanceSavedData.Instance.load(list.getCompound(index), data::setDirty, budget);
         if (instance == null || data.instances.putIfAbsent(instance.id, instance) != null) {
            budget.sanitized = true;
         }
      }

      if (budget.sanitized) {
         data.setDirty();
      }

      return data;
   }

   private static String validateInstanceIdentity(@Nullable UUID id, @Nullable ResourceLocation dungeonId, @Nullable ResourceKey<Level> dimension) {
      if (id == null) {
         return "Instance UUID is required.";
      } else if (!safeResourceId(dungeonId)) {
         return "Dungeon id is missing or too long.";
      } else {
         return dimension != null && safeResourceId(dimension.location()) ? null : "Dungeon dimension is missing or invalid.";
      }
   }

   private static int clampLevel(int level) {
      return Math.max(0, Math.min(1000, level));
   }

   private static boolean safeResourceId(@Nullable ResourceLocation id) {
      return id != null && id.toString().length() <= 256;
   }

   @Nullable
   private static ResourceLocation parseResourceId(String value) {
      return value != null && !value.isBlank() && value.length() <= 256 ? ResourceLocation.tryParse(value) : null;
   }

   private static boolean safeKey(String value, int maxLength) {
      if (value != null && !value.isBlank() && value.length() <= maxLength) {
         for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if ((character < 'a' || character > 'z')
               && (character < '0' || character > '9')
               && character != '_'
               && character != '-'
               && character != '.'
               && character != '/'
               && character != ':') {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean safePosition(@Nullable BlockPos position) {
      return position != null
         && Math.abs((long)position.getX()) <= 30000000L
         && Math.abs((long)position.getZ()) <= 30000000L
         && Math.abs((long)position.getY()) <= 4096L;
   }

   @Nullable
   private static BlockPos readPosition(CompoundTag owner, String key, DungeonInstanceSavedData.LoadBudget budget) {
      if (!owner.contains(key, 10)) {
         return null;
      } else {
         BlockPos position = NbtUtils.readBlockPos(owner.getCompound(key));
         if (!safePosition(position)) {
            budget.sanitized = true;
            return null;
         } else {
            return position;
         }
      }
   }

   private static Rotation parseRotation(String value, DungeonInstanceSavedData.LoadBudget budget) {
      try {
         return Rotation.valueOf(value);
      } catch (IllegalArgumentException | NullPointerException ignored) {
         budget.sanitized = true;
         return Rotation.NONE;
      }
   }

   public record Bounds(BlockPos min, BlockPos max) {
      public Bounds(BlockPos min, BlockPos max) {
         if (min != null && max != null) {
            BlockPos first = min;
            BlockPos second = max;
            min = new BlockPos(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()));
            max = new BlockPos(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
            this.min = min;
            this.max = max;
         } else {
            throw new IllegalArgumentException("Bounds require two positions.");
         }
      }

      public long sizeX() {
         return (long)this.max.getX() - this.min.getX() + 1L;
      }

      public long sizeY() {
         return (long)this.max.getY() - this.min.getY() + 1L;
      }

      public long sizeZ() {
         return (long)this.max.getZ() - this.min.getZ() + 1L;
      }

      public boolean contains(BlockPos position) {
         return position != null
            && position.getX() >= this.min.getX()
            && position.getX() <= this.max.getX()
            && position.getY() >= this.min.getY()
            && position.getY() <= this.max.getY()
            && position.getZ() >= this.min.getZ()
            && position.getZ() <= this.max.getZ();
      }

      public boolean intersects(DungeonInstanceSavedData.Bounds other) {
         return other != null
            && this.max.getX() >= other.min.getX()
            && this.min.getX() <= other.max.getX()
            && this.max.getY() >= other.min.getY()
            && this.min.getY() <= other.max.getY()
            && this.max.getZ() >= other.min.getZ()
            && this.min.getZ() <= other.max.getZ();
      }

      private boolean isSafe() {
         long x = this.sizeX();
         long y = this.sizeY();
         long z = this.sizeZ();
         return DungeonInstanceSavedData.safePosition(this.min)
            && DungeonInstanceSavedData.safePosition(this.max)
            && x <= 1024L
            && y <= 1024L
            && z <= 1024L
            && x * y * z <= 16777216L;
      }

      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.put("Min", NbtUtils.writeBlockPos(this.min));
         tag.put("Max", NbtUtils.writeBlockPos(this.max));
         return tag;
      }

      @Nullable
      private static DungeonInstanceSavedData.Bounds load(CompoundTag tag, DungeonInstanceSavedData.LoadBudget budget) {
         BlockPos min = DungeonInstanceSavedData.readPosition(tag, "Min", budget);
         BlockPos max = DungeonInstanceSavedData.readPosition(tag, "Max", budget);
         if (min != null && max != null) {
            DungeonInstanceSavedData.Bounds bounds = new DungeonInstanceSavedData.Bounds(min, max);
            if (!bounds.isSafe()) {
               budget.sanitized = true;
               return null;
            } else {
               return bounds;
            }
         } else {
            budget.sanitized = true;
            return null;
         }
      }
   }

   public record EncounterMarker(String id, String role, BlockPos position) {
      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putString("Id", this.id);
         tag.putString("Role", this.role);
         tag.put("Position", NbtUtils.writeBlockPos(this.position));
         return tag;
      }

      @Nullable
      private static DungeonInstanceSavedData.EncounterMarker load(CompoundTag tag, DungeonInstanceSavedData.LoadBudget budget) {
         String id = tag.getString("Id");
         String role = tag.getString("Role");
         BlockPos position = DungeonInstanceSavedData.readPosition(tag, "Position", budget);
         if (DungeonInstanceSavedData.safeKey(id, 128) && DungeonInstanceSavedData.safeKey(role, 128) && position != null) {
            return new DungeonInstanceSavedData.EncounterMarker(id, role, position);
         }

         budget.sanitized = true;
         return null;
      }
   }

   public static final class EncounterState {
      private final String key;
      private final ResourceLocation poolId;
      private final boolean boss;
      private final boolean levelOverride;
      private final int minSpawnLevel;
      private final int maxSpawnLevel;
      private final String sequenceKey;
      private final int sequenceOrder;
      private final int delayTicks;
      private final Runnable dirty;
      private final List<DungeonInstanceSavedData.EncounterMarker> markers = new ArrayList<>();
      @Nullable
      private DungeonInstanceSavedData.Bounds triggerBounds;
      private boolean activated;
      private boolean completed;
      private long scheduledGameTime = -1L;
      private final Set<UUID> trackedMobs = new LinkedHashSet<>();

      private EncounterState(
         String key,
         ResourceLocation poolId,
         boolean boss,
         boolean levelOverride,
         int minSpawnLevel,
         int maxSpawnLevel,
         String sequenceKey,
         int sequenceOrder,
         int delayTicks,
         Runnable dirty
      ) {
         this.key = key;
         this.poolId = poolId;
         this.boss = boss;
         this.levelOverride = levelOverride;
         this.minSpawnLevel = minSpawnLevel;
         this.maxSpawnLevel = maxSpawnLevel;
         this.sequenceKey = sequenceKey;
         this.sequenceOrder = sequenceOrder;
         this.delayTicks = delayTicks;
         this.dirty = dirty;
      }

      public String key() {
         return this.key;
      }

      public ResourceLocation poolId() {
         return this.poolId;
      }

      public boolean boss() {
         return this.boss;
      }

      public boolean levelOverride() {
         return this.levelOverride;
      }

      public int minSpawnLevel() {
         return this.minSpawnLevel;
      }

      public int maxSpawnLevel() {
         return this.maxSpawnLevel;
      }

      public boolean sequenced() {
         return !this.sequenceKey.isBlank();
      }

      public String sequenceKey() {
         return this.sequenceKey;
      }

      public int sequenceOrder() {
         return this.sequenceOrder;
      }

      public int delayTicks() {
         return this.delayTicks;
      }

      public long scheduledGameTime() {
         return this.scheduledGameTime;
      }

      public boolean scheduleActivation(long gameTime) {
         if (this.sequenced() && !this.activated && !this.completed && this.scheduledGameTime < 0L) {
            this.scheduledGameTime = Math.max(0L, gameTime);
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public List<DungeonInstanceSavedData.EncounterMarker> markers() {
         return List.copyOf(this.markers);
      }

      public boolean addMarker(DungeonInstanceSavedData.EncounterMarker marker) {
         if (validMarker(marker) && this.markers.size() < 256 && !this.markers.stream().anyMatch(existing -> existing.id.equals(marker.id))) {
            this.markers.add(marker);
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public boolean addMarker(String id, String role, BlockPos position) {
         return id != null && role != null && position != null ? this.addMarker(new DungeonInstanceSavedData.EncounterMarker(id, role, position)) : false;
      }

      public boolean removeMarker(String id) {
         boolean removed = this.markers.removeIf(marker -> marker.id.equals(id));
         if (removed) {
            this.dirty.run();
         }

         return removed;
      }

      public Optional<DungeonInstanceSavedData.Bounds> triggerBounds() {
         return Optional.ofNullable(this.triggerBounds);
      }

      public boolean setTriggerBounds(@Nullable DungeonInstanceSavedData.Bounds bounds) {
         if (bounds != null && !bounds.isSafe()) {
            return false;
         }

         if (Objects.equals(this.triggerBounds, bounds)) {
            return false;
         }

         this.triggerBounds = bounds;
         this.dirty.run();
         return true;
      }

      public boolean activated() {
         return this.activated;
      }

      public boolean activate() {
         if (this.activated) {
            return false;
         }

         this.activated = true;
         this.dirty.run();
         return true;
      }

      public boolean completed() {
         return this.completed;
      }

      public boolean markCompleted() {
         if (this.completed) {
            return false;
         }

         this.activated = true;
         this.completed = true;
         this.dirty.run();
         return true;
      }

      public boolean resetProgress() {
         if (!this.activated && !this.completed && this.trackedMobs.isEmpty()) {
            return false;
         }

         this.activated = false;
         this.completed = false;
         this.trackedMobs.clear();
         this.dirty.run();
         return true;
      }

      public Set<UUID> trackedMobs() {
         return Set.copyOf(this.trackedMobs);
      }

      public boolean trackMob(UUID mobId) {
         if (mobId != null && this.trackedMobs.size() < 2048 && this.trackedMobs.add(mobId)) {
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public boolean untrackMob(UUID mobId) {
         if (mobId != null && this.trackedMobs.remove(mobId)) {
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public boolean clearTrackedMobs() {
         if (this.trackedMobs.isEmpty()) {
            return false;
         }

         this.trackedMobs.clear();
         this.dirty.run();
         return true;
      }

      public DungeonInstanceSavedData.EncounterView view() {
         return new DungeonInstanceSavedData.EncounterView(
            this.key,
            this.poolId,
            this.boss,
            this.levelOverride,
            this.minSpawnLevel,
            this.maxSpawnLevel,
            this.sequenceKey,
            this.sequenceOrder,
            this.delayTicks,
            this.scheduledGameTime,
            List.copyOf(this.markers),
            this.triggerBounds,
            this.activated,
            this.completed,
            Set.copyOf(this.trackedMobs)
         );
      }

      private static boolean validMarker(@Nullable DungeonInstanceSavedData.EncounterMarker marker) {
         return marker != null
            && DungeonInstanceSavedData.safeKey(marker.id, 128)
            && DungeonInstanceSavedData.safeKey(marker.role, 128)
            && DungeonInstanceSavedData.safePosition(marker.position);
      }

      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putString("Key", this.key);
         tag.putString("Pool", this.poolId.toString());
         tag.putBoolean("Boss", this.boss);
         tag.putBoolean("LevelOverride", this.levelOverride);
         tag.putInt("MinSpawnLevel", this.minSpawnLevel);
         tag.putInt("MaxSpawnLevel", this.maxSpawnLevel);
         if (this.sequenced()) {
            tag.putString("SequenceKey", this.sequenceKey);
            tag.putInt("SequenceOrder", this.sequenceOrder);
            tag.putInt("DelayTicks", this.delayTicks);
            if (this.scheduledGameTime >= 0L) {
               tag.putLong("ScheduledGameTime", this.scheduledGameTime);
            }
         }

         tag.putBoolean("Activated", this.activated);
         tag.putBoolean("Completed", this.completed);
         if (this.triggerBounds != null) {
            tag.put("TriggerBounds", this.triggerBounds.save());
         }

         ListTag markerList = new ListTag();
         this.markers.forEach(marker -> markerList.add(marker.save()));
         tag.put("Markers", markerList);
         ListTag mobList = new ListTag();

         for (UUID mobId : this.trackedMobs) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", mobId);
            mobList.add(entry);
         }

         tag.put("TrackedMobs", mobList);
         return tag;
      }

      @Nullable
      private static DungeonInstanceSavedData.EncounterState load(CompoundTag tag, Runnable dirty, DungeonInstanceSavedData.LoadBudget budget) {
         String key = tag.getString("Key");
         ResourceLocation poolId = DungeonInstanceSavedData.parseResourceId(tag.getString("Pool"));
         if (DungeonInstanceSavedData.safeKey(key, 128) && poolId != null) {
            int firstLevel = DungeonInstanceSavedData.clampLevel(tag.getInt("MinSpawnLevel"));
            int secondLevel = DungeonInstanceSavedData.clampLevel(tag.getInt("MaxSpawnLevel"));
            boolean levelOverride = !tag.contains("LevelOverride", 1) || tag.getBoolean("LevelOverride");
            String sequenceKey = tag.getString("SequenceKey");
            int sequenceOrder = -1;
            int delayTicks = 0;
            if (!sequenceKey.isBlank()) {
               if (!DungeonInstanceSavedData.safeKey(sequenceKey, 128)) {
                  budget.sanitized = true;
                  return null;
               }

               sequenceOrder = tag.getInt("SequenceOrder");
               delayTicks = tag.getInt("DelayTicks");
               if (sequenceOrder < 0 || sequenceOrder >= 256 || delayTicks < 0 || delayTicks > 1000000) {
                  budget.sanitized = true;
                  return null;
               }
            }

            DungeonInstanceSavedData.EncounterState encounter = new DungeonInstanceSavedData.EncounterState(
               key,
               poolId,
               tag.getBoolean("Boss"),
               levelOverride,
               Math.min(firstLevel, secondLevel),
               Math.max(firstLevel, secondLevel),
               sequenceKey,
               sequenceOrder,
               delayTicks,
               dirty
            );
            encounter.activated = tag.getBoolean("Activated");
            encounter.completed = tag.getBoolean("Completed");
            if (!sequenceKey.isBlank() && tag.contains("ScheduledGameTime", 4)) {
               encounter.scheduledGameTime = Math.max(0L, tag.getLong("ScheduledGameTime"));
            }

            if (encounter.completed) {
               encounter.activated = true;
            }

            if (tag.contains("TriggerBounds", 10)) {
               encounter.triggerBounds = DungeonInstanceSavedData.Bounds.load(tag.getCompound("TriggerBounds"), budget);
            }

            ListTag markerList = tag.getList("Markers", 10);
            int markerLimit = Math.min(markerList.size(), 256);
            if (markerList.size() > markerLimit) {
               budget.sanitized = true;
            }

            for (int index = 0; index < markerLimit && budget.takeMarker(); index++) {
               DungeonInstanceSavedData.EncounterMarker marker = DungeonInstanceSavedData.EncounterMarker.load(markerList.getCompound(index), budget);
               if (marker != null && encounter.markers.stream().noneMatch(existing -> existing.id.equals(marker.id))) {
                  encounter.markers.add(marker);
               } else {
                  budget.sanitized = true;
               }
            }

            ListTag mobList = tag.getList("TrackedMobs", 10);
            int mobLimit = Math.min(mobList.size(), 2048);
            if (mobList.size() > mobLimit) {
               budget.sanitized = true;
            }

            for (int index = 0; index < mobLimit && budget.takeTrackedMob(); index++) {
               CompoundTag entry = mobList.getCompound(index);
               if (entry.hasUUID("Id")) {
                  encounter.trackedMobs.add(entry.getUUID("Id"));
               } else {
                  budget.sanitized = true;
               }
            }

            return encounter;
         } else {
            budget.sanitized = true;
            return null;
         }
      }
   }

   public record EncounterView(
      String key,
      ResourceLocation poolId,
      boolean boss,
      boolean levelOverride,
      int minSpawnLevel,
      int maxSpawnLevel,
      String sequenceKey,
      int sequenceOrder,
      int delayTicks,
      long scheduledGameTime,
      List<DungeonInstanceSavedData.EncounterMarker> markers,
      @Nullable DungeonInstanceSavedData.Bounds triggerBounds,
      boolean activated,
      boolean completed,
      Set<UUID> trackedMobs
   ) {
   }

   public static final class Instance {
      private final UUID id;
      private final ResourceLocation dungeonId;
      private final ResourceKey<Level> dimension;
      private final long seed;
      private final int effectiveLevel;
      private final long createdGameTime;
      private final Runnable dirty;
      private boolean completed;
      private boolean returnPortalDeferred;
      private boolean returnPortalSuppressed;
      private final Set<UUID> participants = new LinkedHashSet<>();
      @Nullable
      private BlockPos playerStart;
      @Nullable
      private BlockPos exit;
      @Nullable
      private Direction exitFacing;
      private final List<DungeonInstanceSavedData.PlacedRoom> rooms = new ArrayList<>();
      private final List<DungeonInstanceSavedData.Bounds> carvedConnections = new ArrayList<>();
      private final List<DungeonInstanceSavedData.Bounds> occupiedVolumes = new ArrayList<>();
      private final Map<String, DungeonInstanceSavedData.EncounterState> encounters = new LinkedHashMap<>();

      private Instance(UUID id, ResourceLocation dungeonId, ResourceKey<Level> dimension, long seed, int effectiveLevel, long createdGameTime, Runnable dirty) {
         this.id = id;
         this.dungeonId = dungeonId;
         this.dimension = dimension;
         this.seed = seed;
         this.effectiveLevel = effectiveLevel;
         this.createdGameTime = createdGameTime;
         this.dirty = dirty;
      }

      public UUID id() {
         return this.id;
      }

      public ResourceLocation dungeonId() {
         return this.dungeonId;
      }

      public ResourceKey<Level> dimension() {
         return this.dimension;
      }

      public long seed() {
         return this.seed;
      }

      public int effectiveLevel() {
         return this.effectiveLevel;
      }

      public long createdGameTime() {
         return this.createdGameTime;
      }

      public boolean completed() {
         return this.completed;
      }

      public boolean setCompleted(boolean completed) {
         if (this.completed == completed) {
            return false;
         }

         this.completed = completed;
         this.dirty.run();
         return true;
      }

      public boolean returnPortalDeferred() {
         return this.returnPortalDeferred;
      }

      public boolean setReturnPortalDeferred(boolean deferred) {
         if (this.returnPortalDeferred == deferred) {
            return false;
         }

         this.returnPortalDeferred = deferred;
         this.dirty.run();
         return true;
      }

      public boolean returnPortalSuppressed() {
         return this.returnPortalSuppressed;
      }

      public boolean setReturnPortalSuppressed(boolean suppressed) {
         if (this.returnPortalSuppressed == suppressed) {
            return false;
         }

         this.returnPortalSuppressed = suppressed;
         this.dirty.run();
         return true;
      }

      public Set<UUID> participants() {
         return Set.copyOf(this.participants);
      }

      public boolean addParticipant(UUID playerId) {
         if (playerId != null && this.participants.size() < 64 && this.participants.add(playerId)) {
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public boolean removeParticipant(UUID playerId) {
         if (playerId != null && this.participants.remove(playerId)) {
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public Optional<BlockPos> playerStart() {
         return Optional.ofNullable(this.playerStart);
      }

      public boolean setPlayerStart(@Nullable BlockPos position) {
         if (position != null && !DungeonInstanceSavedData.safePosition(position)) {
            return false;
         }

         if (Objects.equals(this.playerStart, position)) {
            return false;
         }

         this.playerStart = position;
         this.dirty.run();
         return true;
      }

      public Optional<BlockPos> exit() {
         return Optional.ofNullable(this.exit);
      }

      public boolean setExit(@Nullable BlockPos position) {
         if (position != null && !DungeonInstanceSavedData.safePosition(position)) {
            return false;
         }

         if (Objects.equals(this.exit, position)) {
            return false;
         }

         this.exit = position;
         this.dirty.run();
         return true;
      }

      public Optional<Direction> exitFacing() {
         return Optional.ofNullable(this.exitFacing);
      }

      public boolean setExitFacing(@Nullable Direction facing) {
         if (facing != null && !facing.getAxis().isHorizontal()) {
            return false;
         }

         if (this.exitFacing == facing) {
            return false;
         }

         this.exitFacing = facing;
         this.dirty.run();
         return true;
      }

      public List<DungeonInstanceSavedData.PlacedRoom> rooms() {
         return List.copyOf(this.rooms);
      }

      public boolean addRoom(DungeonInstanceSavedData.PlacedRoom room) {
         if (validRoom(room) && this.rooms.size() < 512) {
            this.rooms.add(room);
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public boolean addRoom(String roomId, ResourceLocation structureId, Rotation rotation, DungeonInstanceSavedData.Bounds bounds) {
         return roomId != null && structureId != null && rotation != null && bounds != null
            ? this.addRoom(new DungeonInstanceSavedData.PlacedRoom(roomId, structureId, rotation, bounds))
            : false;
      }

      public boolean removeRoom(String roomId) {
         boolean removed = this.rooms.removeIf(room -> room.roomId.equals(roomId));
         if (removed) {
            this.dirty.run();
         }

         return removed;
      }

      public List<DungeonInstanceSavedData.Bounds> carvedConnections() {
         return List.copyOf(this.carvedConnections);
      }

      public boolean addCarvedConnection(DungeonInstanceSavedData.Bounds bounds) {
         if (bounds != null && bounds.isSafe() && this.carvedConnections.size() < 1024 && !this.carvedConnections.contains(bounds)) {
            this.carvedConnections.add(bounds);
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public List<DungeonInstanceSavedData.Bounds> occupiedVolumes() {
         return List.copyOf(this.occupiedVolumes);
      }

      public boolean addOccupiedVolume(DungeonInstanceSavedData.Bounds bounds) {
         if (bounds != null && bounds.isSafe() && this.occupiedVolumes.size() < 1536 && !this.occupiedVolumes.contains(bounds)) {
            this.occupiedVolumes.add(bounds);
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public List<DungeonInstanceSavedData.EncounterState> encounters() {
         return List.copyOf(this.encounters.values());
      }

      public Optional<DungeonInstanceSavedData.EncounterState> encounter(String key) {
         return Optional.ofNullable(key == null ? null : this.encounters.get(key));
      }

      public DungeonInstanceSavedData.MutationResult<DungeonInstanceSavedData.EncounterState> createEncounter(
         String key, ResourceLocation poolId, boolean boss, int minSpawnLevel, int maxSpawnLevel
      ) {
         return this.createEncounter(key, poolId, boss, true, minSpawnLevel, maxSpawnLevel);
      }

      public DungeonInstanceSavedData.MutationResult<DungeonInstanceSavedData.EncounterState> createEncounter(
         String key, ResourceLocation poolId, boolean boss, boolean levelOverride, int minSpawnLevel, int maxSpawnLevel
      ) {
         return this.createEncounter(key, poolId, boss, levelOverride, minSpawnLevel, maxSpawnLevel, "", -1, 0);
      }

      public DungeonInstanceSavedData.MutationResult<DungeonInstanceSavedData.EncounterState> createEncounter(
         String key,
         ResourceLocation poolId,
         boolean boss,
         boolean levelOverride,
         int minSpawnLevel,
         int maxSpawnLevel,
         String sequenceKey,
         int sequenceOrder,
         int delayTicks
      ) {
         if (!DungeonInstanceSavedData.safeKey(key, 128)) {
            return DungeonInstanceSavedData.MutationResult.failure("Encounter key is missing or invalid.");
         }

         if (!DungeonInstanceSavedData.safeResourceId(poolId)) {
            return DungeonInstanceSavedData.MutationResult.failure("Encounter pool id is missing or invalid.");
         }

         boolean sequenced = sequenceKey != null && !sequenceKey.isBlank();
         if (sequenced && !DungeonInstanceSavedData.safeKey(sequenceKey, 128)) {
            return DungeonInstanceSavedData.MutationResult.failure("Encounter sequence key is invalid.");
         }

         if (!sequenced || sequenceOrder >= 0 && sequenceOrder < 256) {
            if (!sequenced || delayTicks >= 0 && delayTicks <= 1000000) {
               if (this.encounters.size() >= 256) {
                  return DungeonInstanceSavedData.MutationResult.failure("This instance has the maximum number of encounters.");
               }

               if (this.encounters.containsKey(key)) {
                  return DungeonInstanceSavedData.MutationResult.failure("Encounter " + key + " already exists.");
               }

               if (sequenced
                  && this.encounters
                     .values()
                     .stream()
                     .anyMatch(existing -> existing.sequenced() && existing.sequenceKey().equals(sequenceKey) && existing.sequenceOrder() == sequenceOrder)) {
                  return DungeonInstanceSavedData.MutationResult.failure(
                     "Encounter sequence " + sequenceKey + " already contains wave order " + sequenceOrder + "."
                  );
               }

               int min = DungeonInstanceSavedData.clampLevel(Math.min(minSpawnLevel, maxSpawnLevel));
               int max = DungeonInstanceSavedData.clampLevel(Math.max(minSpawnLevel, maxSpawnLevel));
               DungeonInstanceSavedData.EncounterState encounter = new DungeonInstanceSavedData.EncounterState(
                  key,
                  poolId,
                  boss,
                  levelOverride,
                  min,
                  max,
                  sequenced ? sequenceKey : "",
                  sequenced ? sequenceOrder : -1,
                  sequenced ? delayTicks : 0,
                  this.dirty
               );
               this.encounters.put(key, encounter);
               this.dirty.run();
               return DungeonInstanceSavedData.MutationResult.success(encounter, "Created encounter " + key + ".");
            } else {
               return DungeonInstanceSavedData.MutationResult.failure("Encounter wave delay is outside the runtime safety range.");
            }
         } else {
            return DungeonInstanceSavedData.MutationResult.failure("Encounter sequence order is outside the runtime safety range.");
         }
      }

      public boolean removeEncounter(String key) {
         if (key != null && this.encounters.remove(key) != null) {
            this.dirty.run();
            return true;
         } else {
            return false;
         }
      }

      public DungeonInstanceSavedData.InstanceView view() {
         return new DungeonInstanceSavedData.InstanceView(
            this.id,
            this.dungeonId,
            this.dimension,
            this.seed,
            this.effectiveLevel,
            this.createdGameTime,
            this.completed,
            Set.copyOf(this.participants),
            this.playerStart,
            this.exit,
            List.copyOf(this.rooms),
            List.copyOf(this.carvedConnections),
            List.copyOf(this.occupiedVolumes),
            this.encounters.values().stream().map(DungeonInstanceSavedData.EncounterState::view).toList()
         );
      }

      private static boolean validRoom(@Nullable DungeonInstanceSavedData.PlacedRoom room) {
         return room != null
            && DungeonInstanceSavedData.safeKey(room.roomId, 128)
            && DungeonInstanceSavedData.safeResourceId(room.structureId)
            && room.rotation != null
            && room.bounds != null
            && room.bounds.isSafe();
      }

      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putUUID("Id", this.id);
         tag.putString("DungeonId", this.dungeonId.toString());
         tag.putString("Dimension", this.dimension.location().toString());
         tag.putLong("Seed", this.seed);
         tag.putInt("EffectiveLevel", this.effectiveLevel);
         tag.putLong("CreatedGameTime", this.createdGameTime);
         tag.putBoolean("Completed", this.completed);
         tag.putBoolean("ReturnPortalDeferred", this.returnPortalDeferred);
         tag.putBoolean("ReturnPortalSuppressed", this.returnPortalSuppressed);
         ListTag participantList = new ListTag();

         for (UUID participant : this.participants) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", participant);
            participantList.add(entry);
         }

         tag.put("Participants", participantList);
         if (this.playerStart != null) {
            tag.put("PlayerStart", NbtUtils.writeBlockPos(this.playerStart));
         }

         if (this.exit != null) {
            tag.put("Exit", NbtUtils.writeBlockPos(this.exit));
         }

         if (this.exitFacing != null) {
            tag.putString("ExitFacing", this.exitFacing.getName());
         }

         ListTag roomList = new ListTag();
         this.rooms.forEach(room -> roomList.add(room.save()));
         tag.put("Rooms", roomList);
         ListTag connectionList = new ListTag();
         this.carvedConnections.forEach(bounds -> connectionList.add(bounds.save()));
         tag.put("Connections", connectionList);
         ListTag occupiedList = new ListTag();
         this.occupiedVolumes.forEach(bounds -> occupiedList.add(bounds.save()));
         tag.put("OccupiedVolumes", occupiedList);
         ListTag encounterList = new ListTag();
         this.encounters.values().forEach(encounter -> encounterList.add(encounter.save()));
         tag.put("Encounters", encounterList);
         return tag;
      }

      @Nullable
      private static DungeonInstanceSavedData.Instance load(CompoundTag tag, Runnable dirty, DungeonInstanceSavedData.LoadBudget budget) {
         if (!tag.hasUUID("Id")) {
            budget.sanitized = true;
            return null;
         }

         UUID id = tag.getUUID("Id");
         ResourceLocation dungeonId = DungeonInstanceSavedData.parseResourceId(tag.getString("DungeonId"));
         ResourceLocation dimensionId = DungeonInstanceSavedData.parseResourceId(tag.getString("Dimension"));
         if (dungeonId != null && dimensionId != null) {
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
            DungeonInstanceSavedData.Instance instance = new DungeonInstanceSavedData.Instance(
               id,
               dungeonId,
               dimension,
               tag.getLong("Seed"),
               DungeonInstanceSavedData.clampLevel(tag.getInt("EffectiveLevel")),
               Math.max(0L, tag.getLong("CreatedGameTime")),
               dirty
            );
            instance.completed = tag.getBoolean("Completed");
            instance.returnPortalDeferred = tag.getBoolean("ReturnPortalDeferred");
            instance.returnPortalSuppressed = tag.getBoolean("ReturnPortalSuppressed");
            ListTag participantList = tag.getList("Participants", 10);
            int participantLimit = Math.min(participantList.size(), 64);
            if (participantList.size() > participantLimit) {
               budget.sanitized = true;
            }

            for (int index = 0; index < participantLimit && budget.takeParticipant(); index++) {
               CompoundTag entry = participantList.getCompound(index);
               if (entry.hasUUID("Id")) {
                  instance.participants.add(entry.getUUID("Id"));
               } else {
                  budget.sanitized = true;
               }
            }

            instance.playerStart = DungeonInstanceSavedData.readPosition(tag, "PlayerStart", budget);
            instance.exit = DungeonInstanceSavedData.readPosition(tag, "Exit", budget);
            Direction savedExitFacing = Direction.byName(tag.getString("ExitFacing"));
            if (savedExitFacing != null && savedExitFacing.getAxis().isHorizontal()) {
               instance.exitFacing = savedExitFacing;
            }

            ListTag roomList = tag.getList("Rooms", 10);
            int roomLimit = Math.min(roomList.size(), 512);
            if (roomList.size() > roomLimit) {
               budget.sanitized = true;
            }

            for (int index = 0; index < roomLimit && budget.takeRoom(); index++) {
               DungeonInstanceSavedData.PlacedRoom room = DungeonInstanceSavedData.PlacedRoom.load(roomList.getCompound(index), budget);
               if (room != null) {
                  instance.rooms.add(room);
               } else {
                  budget.sanitized = true;
               }
            }

            ListTag connectionList = tag.getList("Connections", 10);
            int connectionLimit = Math.min(connectionList.size(), 1024);
            if (connectionList.size() > connectionLimit) {
               budget.sanitized = true;
            }

            for (int index = 0; index < connectionLimit && budget.takeConnection(); index++) {
               DungeonInstanceSavedData.Bounds bounds = DungeonInstanceSavedData.Bounds.load(connectionList.getCompound(index), budget);
               if (bounds != null && !instance.carvedConnections.contains(bounds)) {
                  instance.carvedConnections.add(bounds);
               } else {
                  budget.sanitized = true;
               }
            }

            ListTag occupiedList = tag.getList("OccupiedVolumes", 10);
            int occupiedLimit = Math.min(occupiedList.size(), 1536);
            if (occupiedList.size() > occupiedLimit) {
               budget.sanitized = true;
            }

            for (int index = 0; index < occupiedLimit && budget.takeOccupiedVolume(); index++) {
               DungeonInstanceSavedData.Bounds bounds = DungeonInstanceSavedData.Bounds.load(occupiedList.getCompound(index), budget);
               if (bounds != null && !instance.occupiedVolumes.contains(bounds)) {
                  instance.occupiedVolumes.add(bounds);
               } else {
                  budget.sanitized = true;
               }
            }

            ListTag encounterList = tag.getList("Encounters", 10);
            int encounterLimit = Math.min(encounterList.size(), 256);
            if (encounterList.size() > encounterLimit) {
               budget.sanitized = true;
            }

            for (int index = 0; index < encounterLimit && budget.takeEncounter(); index++) {
               DungeonInstanceSavedData.EncounterState encounter = DungeonInstanceSavedData.EncounterState.load(encounterList.getCompound(index), dirty, budget);
               boolean duplicateSequenceOrder = encounter != null
                  && encounter.sequenced()
                  && instance.encounters
                     .values()
                     .stream()
                     .anyMatch(
                        existing -> existing.sequenced()
                           && existing.sequenceKey().equals(encounter.sequenceKey())
                           && existing.sequenceOrder() == encounter.sequenceOrder()
                     );
               if (encounter == null || duplicateSequenceOrder || instance.encounters.putIfAbsent(encounter.key, encounter) != null) {
                  budget.sanitized = true;
               }
            }

            return instance;
         } else {
            budget.sanitized = true;
            return null;
         }
      }
   }

   public record InstanceView(
      UUID id,
      ResourceLocation dungeonId,
      ResourceKey<Level> dimension,
      long seed,
      int effectiveLevel,
      long createdGameTime,
      boolean completed,
      Set<UUID> participants,
      @Nullable BlockPos playerStart,
      @Nullable BlockPos exit,
      List<DungeonInstanceSavedData.PlacedRoom> rooms,
      List<DungeonInstanceSavedData.Bounds> carvedConnections,
      List<DungeonInstanceSavedData.Bounds> occupiedVolumes,
      List<DungeonInstanceSavedData.EncounterView> encounters
   ) {
   }

   private static final class LoadBudget {
      private int participants;
      private int rooms;
      private int connections;
      private int occupiedVolumes;
      private int encounters;
      private int markers;
      private int trackedMobs;
      private boolean sanitized;

      private boolean takeParticipant() {
         return this.take(++this.participants, 8192);
      }

      private boolean takeRoom() {
         return this.take(++this.rooms, 16384);
      }

      private boolean takeConnection() {
         return this.take(++this.connections, 32768);
      }

      private boolean takeOccupiedVolume() {
         return this.take(++this.occupiedVolumes, 65536);
      }

      private boolean takeEncounter() {
         return this.take(++this.encounters, 8192);
      }

      private boolean takeMarker() {
         return this.take(++this.markers, 65536);
      }

      private boolean takeTrackedMob() {
         return this.take(++this.trackedMobs, 131072);
      }

      private boolean take(int used, int maximum) {
         if (used <= maximum) {
            return true;
         }

         this.sanitized = true;
         return false;
      }
   }

   public record MutationResult<T>(boolean success, String message, @Nullable T value) {
      private static <T> DungeonInstanceSavedData.MutationResult<T> success(T value, String message) {
         return new DungeonInstanceSavedData.MutationResult<>(true, message, value);
      }

      private static <T> DungeonInstanceSavedData.MutationResult<T> failure(String message) {
         return new DungeonInstanceSavedData.MutationResult<>(false, message, null);
      }
   }

   public record PlacedRoom(String roomId, ResourceLocation structureId, Rotation rotation, DungeonInstanceSavedData.Bounds bounds) {
      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putString("RoomId", this.roomId);
         tag.putString("Structure", this.structureId.toString());
         tag.putString("Rotation", this.rotation.name());
         tag.put("Bounds", this.bounds.save());
         return tag;
      }

      @Nullable
      private static DungeonInstanceSavedData.PlacedRoom load(CompoundTag tag, DungeonInstanceSavedData.LoadBudget budget) {
         String roomId = tag.getString("RoomId");
         ResourceLocation structureId = DungeonInstanceSavedData.parseResourceId(tag.getString("Structure"));
         DungeonInstanceSavedData.Bounds bounds = tag.contains("Bounds", 10) ? DungeonInstanceSavedData.Bounds.load(tag.getCompound("Bounds"), budget) : null;
         if (DungeonInstanceSavedData.safeKey(roomId, 128) && structureId != null && bounds != null) {
            return new DungeonInstanceSavedData.PlacedRoom(
               roomId, structureId, DungeonInstanceSavedData.parseRotation(tag.getString("Rotation"), budget), bounds
            );
         }

         budget.sanitized = true;
         return null;
      }
   }
}
