package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class StoryModeIntroSavedData extends SavedData {
   private static final String DATA_NAME = "sololeveling_story_mode_intro";
   private static final int SCHEMA_VERSION = 3;
   public static final int DUNGEON_BREAK_GRACE_CLEARS = 10;
   private StoryModeIntroSavedData.Stage stage = StoryModeIntroSavedData.Stage.NOT_STARTED;
   @Nullable
   private UUID ownerId;
   private int playerClassId;
   private boolean dungeonPlaced;
   @Nullable
   private UUID bossId;
   private final ArrayList<UUID> hunterIds = new ArrayList<>();
   @Nullable
   private UUID gateId;
   private int cartenonInstanceId;
   @Nullable
   private UUID godStatueId;
   @Nullable
   private BlockPos godStatuePosition;
   private long stageStartedTick;
   private final ArrayList<UUID> laserTargetIds = new ArrayList<>();
   private int laserFiredCount;
   private int laserKilledCount;
   private long laserLastFiredTick;
   private int postIntroDungeonClears;
   private final LinkedHashSet<String> dungeonClearReceipts = new LinkedHashSet<>();
   private long bossMissingSinceTick = -1L;

   public static StoryModeIntroSavedData get(ServerLevel level) {
      if (level == null) {
         throw new IllegalArgumentException("A server level is required.");
      } else {
         return get(level.getServer());
      }
   }

   public static StoryModeIntroSavedData get(MinecraftServer server) {
      if (server == null) {
         throw new IllegalArgumentException("A Minecraft server is required.");
      } else {
         return server.overworld()
            .getDataStorage()
            .computeIfAbsent(StoryModeIntroSavedData::load, StoryModeIntroSavedData::new, "sololeveling_story_mode_intro");
      }
   }

   public StoryModeIntroSavedData.Stage stage() {
      return this.stage;
   }

   public boolean isActive() {
      return this.ownerId != null && this.stage != StoryModeIntroSavedData.Stage.NOT_STARTED && this.stage != StoryModeIntroSavedData.Stage.COMPLETE;
   }

   @Nullable
   public UUID ownerId() {
      return this.ownerId;
   }

   public boolean isOwner(UUID playerId) {
      return this.ownerId != null && this.ownerId.equals(playerId);
   }

   public int playerClassId() {
      return this.playerClassId;
   }

   public boolean dungeonPlaced() {
      return this.dungeonPlaced;
   }

   @Nullable
   public UUID bossId() {
      return this.bossId;
   }

   public List<UUID> hunterIds() {
      return List.copyOf(this.hunterIds);
   }

   @Nullable
   public UUID gateId() {
      return this.gateId;
   }

   public int cartenonInstanceId() {
      return this.cartenonInstanceId;
   }

   @Nullable
   public UUID godStatueId() {
      return this.godStatueId;
   }

   @Nullable
   public BlockPos godStatuePosition() {
      return this.godStatuePosition;
   }

   public long stageStartedTick() {
      return this.stageStartedTick;
   }

   public List<UUID> laserTargetIds() {
      return List.copyOf(this.laserTargetIds);
   }

   public int laserFiredCount() {
      return this.laserFiredCount;
   }

   public int laserKilledCount() {
      return this.laserKilledCount;
   }

   public long laserLastFiredTick() {
      return this.laserLastFiredTick;
   }

   public int postIntroDungeonClears() {
      return this.postIntroDungeonClears;
   }

   public int dungeonBreakGraceRemaining() {
      return Math.max(0, 10 - this.postIntroDungeonClears);
   }

   public boolean hasDungeonBreakGrace() {
      return this.ownerId != null && this.stage != StoryModeIntroSavedData.Stage.NOT_STARTED && this.postIntroDungeonClears < 10;
   }

   public boolean recordPostIntroDungeonClear(UUID playerId, String receipt) {
      String normalizedReceipt = normalizeDungeonClearReceipt(receipt);
      if (this.isOwner(playerId)
         && this.stage == StoryModeIntroSavedData.Stage.COMPLETE
         && this.postIntroDungeonClears < 10
         && !normalizedReceipt.isEmpty()
         && this.dungeonClearReceipts.add(normalizedReceipt)) {
         this.postIntroDungeonClears = Math.min(10, this.postIntroDungeonClears + 1);
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean claimOwner(UUID playerId, int classId, long gameTick) {
      if (playerId != null && this.stage == StoryModeIntroSavedData.Stage.NOT_STARTED && this.ownerId == null) {
         this.ownerId = playerId;
         this.playerClassId = classId;
         this.stage = StoryModeIntroSavedData.Stage.PREPARING;
         this.stageStartedTick = Math.max(0L, gameTick);
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public void setStage(StoryModeIntroSavedData.Stage next, long gameTick) {
      if (next != null && this.stage != next) {
         this.stage = next;
         this.stageStartedTick = Math.max(0L, gameTick);
         this.setDirty();
      }
   }

   public void markDungeonPlaced() {
      if (!this.dungeonPlaced) {
         this.dungeonPlaced = true;
         this.setDirty();
      }
   }

   public void setBossId(@Nullable UUID id) {
      if (id != null) {
         this.noteBossObserved();
      }

      if (!equalsUuid(this.bossId, id)) {
         this.bossId = id;
         this.setDirty();
      }
   }

   public void noteBossObserved() {
      this.bossMissingSinceTick = -1L;
   }

   public boolean bossRecoveryGraceElapsed(long gameTick, long graceTicks) {
      long now = Math.max(0L, gameTick);
      if (this.bossMissingSinceTick >= 0L && now >= this.bossMissingSinceTick) {
         return now - this.bossMissingSinceTick >= Math.max(0L, graceTicks);
      }

      this.bossMissingSinceTick = now;
      return false;
   }

   public void replaceHunterIds(List<UUID> ids) {
      LinkedHashSet<UUID> sanitized = new LinkedHashSet<>();
      if (ids != null) {
         for (UUID id : ids) {
            if (id != null && sanitized.size() < 6) {
               sanitized.add(id);
            }
         }
      }

      if (!this.hunterIds.equals(new ArrayList<>(sanitized))) {
         this.hunterIds.clear();
         this.hunterIds.addAll(sanitized);
         this.setDirty();
      }
   }

   public void setGate(@Nullable UUID id, int instanceId, long gameTick) {
      if (this.stage == StoryModeIntroSavedData.Stage.ANCIENT_GOLEM || this.stage == StoryModeIntroSavedData.Stage.GATE_WAIT) {
         boolean changed = !equalsUuid(this.gateId, id)
            || this.cartenonInstanceId != Math.max(1, instanceId)
            || this.stage != StoryModeIntroSavedData.Stage.GATE_WAIT;
         this.gateId = id;
         this.cartenonInstanceId = Math.max(1, instanceId);
         this.stage = StoryModeIntroSavedData.Stage.GATE_WAIT;
         this.stageStartedTick = Math.max(0L, gameTick);
         if (changed) {
            this.setDirty();
         }
      }
   }

   public void setTemple(int instanceId, @Nullable UUID statueId, long gameTick) {
      if (this.stage == StoryModeIntroSavedData.Stage.GATE_WAIT) {
         boolean changed = this.cartenonInstanceId != Math.max(1, instanceId)
            || !equalsUuid(this.godStatueId, statueId)
            || this.stage != StoryModeIntroSavedData.Stage.TEMPLE;
         this.cartenonInstanceId = Math.max(1, instanceId);
         if (!equalsUuid(this.godStatueId, statueId)) {
            this.godStatuePosition = null;
         }

         this.godStatueId = statueId;
         this.stage = StoryModeIntroSavedData.Stage.TEMPLE;
         this.stageStartedTick = Math.max(0L, gameTick);
         if (changed) {
            this.setDirty();
         }
      }
   }

   public void setGodStatueId(@Nullable UUID id) {
      if (!equalsUuid(this.godStatueId, id)) {
         this.godStatueId = id;
         this.godStatuePosition = null;
         this.setDirty();
      }
   }

   public void trackGodStatue(UUID id, BlockPos position) {
      if (id != null && position != null) {
         boolean changed = !equalsUuid(this.godStatueId, id) || !position.equals(this.godStatuePosition);
         this.godStatueId = id;
         this.godStatuePosition = position.immutable();
         if (changed) {
            this.setDirty();
         }
      }
   }

   public void beginLasers(List<UUID> targets, long gameTick) {
      LinkedHashSet<UUID> sanitized = new LinkedHashSet<>();
      if (targets != null) {
         for (UUID id : targets) {
            if (id != null && sanitized.size() < 2) {
               sanitized.add(id);
            }
         }
      }

      this.laserTargetIds.clear();
      this.laserTargetIds.addAll(sanitized);
      this.laserFiredCount = 0;
      this.laserKilledCount = 0;
      this.laserLastFiredTick = 0L;
      this.stage = StoryModeIntroSavedData.Stage.LASER_EXECUTION;
      this.stageStartedTick = Math.max(0L, gameTick);
      this.setDirty();
   }

   public void markLaserFired(int count, long gameTick) {
      int next = Math.max(0, Math.min(this.laserTargetIds.size(), count));
      long nextTick = Math.max(0L, gameTick);
      if (this.laserFiredCount != next || this.laserLastFiredTick != nextTick) {
         this.laserFiredCount = next;
         this.laserLastFiredTick = nextTick;
         this.setDirty();
      }
   }

   public boolean replacePendingLaserTarget(int index, UUID replacement) {
      if (this.stage == StoryModeIntroSavedData.Stage.LASER_EXECUTION
         && replacement != null
         && index == this.laserKilledCount
         && index >= 0
         && index < this.laserTargetIds.size()) {
         for (int targetIndex = 0; targetIndex < this.laserTargetIds.size(); targetIndex++) {
            if (targetIndex != index && replacement.equals(this.laserTargetIds.get(targetIndex))) {
               return false;
            }
         }

         if (replacement.equals(this.laserTargetIds.get(index))) {
            return true;
         }

         this.laserTargetIds.set(index, replacement);
         this.laserFiredCount = Math.min(this.laserFiredCount, index);
         this.laserKilledCount = Math.min(this.laserKilledCount, index);
         this.laserLastFiredTick = 0L;
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public void setLaserKilledCount(int count) {
      int next = Math.max(0, Math.min(this.laserFiredCount, count));
      if (this.laserKilledCount != next) {
         this.laserKilledCount = next;
         this.setDirty();
      }
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag root) {
      root.putInt("SchemaVersion", 3);
      root.putString("Stage", this.stage.name());
      if (this.ownerId != null) {
         root.putUUID("Owner", this.ownerId);
      }

      root.putInt("PlayerClass", this.playerClassId);
      root.putBoolean("DungeonPlaced", this.dungeonPlaced);
      if (this.bossId != null) {
         root.putUUID("Boss", this.bossId);
      }

      root.put("Hunters", saveUuidList(this.hunterIds));
      if (this.gateId != null) {
         root.putUUID("Gate", this.gateId);
      }

      root.putInt("CartenonInstance", this.cartenonInstanceId);
      if (this.godStatueId != null) {
         root.putUUID("GodStatue", this.godStatueId);
      }

      if (this.godStatuePosition != null) {
         root.putLong("GodStatuePos", this.godStatuePosition.asLong());
      }

      root.putLong("StageStartedTick", this.stageStartedTick);
      root.put("LaserTargets", saveUuidList(this.laserTargetIds));
      root.putInt("LaserFired", this.laserFiredCount);
      root.putInt("LaserKilled", this.laserKilledCount);
      root.putLong("LaserLastFiredTick", this.laserLastFiredTick);
      root.putInt("PostIntroDungeonClears", this.postIntroDungeonClears);
      ListTag clearReceipts = new ListTag();

      for (String receipt : this.dungeonClearReceipts) {
         clearReceipts.add(StringTag.valueOf(receipt));
      }

      root.put("DungeonClearReceipts", clearReceipts);
      return root;
   }

   private static StoryModeIntroSavedData load(CompoundTag root) {
      StoryModeIntroSavedData data = new StoryModeIntroSavedData();
      String storedStage = root.getString("Stage");
      boolean unknownStage = !StoryModeIntroSavedData.Stage.recognizes(storedStage);
      int storedSchema = root.contains("SchemaVersion", 3) ? root.getInt("SchemaVersion") : 0;
      boolean unsupportedSchema = storedSchema < 0 || storedSchema > 3;
      boolean olderSchema = storedSchema >= 0 && storedSchema < 3;
      data.stage = StoryModeIntroSavedData.Stage.load(storedStage);
      data.ownerId = root.hasUUID("Owner") ? root.getUUID("Owner") : null;
      data.playerClassId = sanitizeClass(root.getInt("PlayerClass"));
      data.dungeonPlaced = root.getBoolean("DungeonPlaced");
      data.bossId = root.hasUUID("Boss") ? root.getUUID("Boss") : null;
      data.hunterIds.addAll(loadUuidList(root, "Hunters", 6));
      data.gateId = root.hasUUID("Gate") ? root.getUUID("Gate") : null;
      data.cartenonInstanceId = Math.max(0, root.getInt("CartenonInstance"));
      data.godStatueId = root.hasUUID("GodStatue") ? root.getUUID("GodStatue") : null;
      data.godStatuePosition = root.contains("GodStatuePos", 4) ? BlockPos.of(root.getLong("GodStatuePos")) : null;
      data.stageStartedTick = Math.max(0L, root.getLong("StageStartedTick"));
      data.laserTargetIds.addAll(loadUuidList(root, "LaserTargets", 2));
      data.laserFiredCount = Math.max(0, Math.min(data.laserTargetIds.size(), root.getInt("LaserFired")));
      data.laserKilledCount = Math.max(0, Math.min(data.laserFiredCount, root.getInt("LaserKilled")));
      data.laserLastFiredTick = Math.max(0L, root.getLong("LaserLastFiredTick"));
      data.postIntroDungeonClears = Math.max(0, Math.min(10, root.getInt("PostIntroDungeonClears")));
      ListTag clearReceipts = root.getList("DungeonClearReceipts", 8);

      for (int index = 0; index < clearReceipts.size() && data.dungeonClearReceipts.size() < 10; index++) {
         String receipt = normalizeDungeonClearReceipt(clearReceipts.getString(index));
         if (!receipt.isEmpty()) {
            data.dungeonClearReceipts.add(receipt);
         }
      }

      data.postIntroDungeonClears = Math.max(data.postIntroDungeonClears, data.dungeonClearReceipts.size());
      if (olderSchema && data.laserFiredCount > data.laserKilledCount && data.laserLastFiredTick == 0L) {
         data.laserLastFiredTick = data.stageStartedTick;
      }

      boolean recoverOwnedState = data.ownerId != null
         && (
            unknownStage
               || unsupportedSchema && data.stage != StoryModeIntroSavedData.Stage.COMPLETE
               || data.stage == StoryModeIntroSavedData.Stage.NOT_STARTED
         );
      if (recoverOwnedState) {
         data.stage = StoryModeIntroSavedData.Stage.PREPARING;
         data.bossId = null;
         data.hunterIds.clear();
         data.gateId = null;
         data.cartenonInstanceId = 0;
         data.godStatueId = null;
         data.godStatuePosition = null;
         data.laserTargetIds.clear();
         data.laserFiredCount = 0;
         data.laserKilledCount = 0;
         data.laserLastFiredTick = 0L;
         data.setDirty();
      }

      boolean missingRequiredInstance = data.ownerId != null
         && data.stage.ordinal() >= StoryModeIntroSavedData.Stage.GATE_WAIT.ordinal()
         && data.stage != StoryModeIntroSavedData.Stage.COMPLETE
         && data.cartenonInstanceId <= 0;
      if (missingRequiredInstance) {
         data.stage = StoryModeIntroSavedData.Stage.ANCIENT_GOLEM;
         data.gateId = null;
         data.cartenonInstanceId = 0;
         data.godStatueId = null;
         data.godStatuePosition = null;
         data.laserTargetIds.clear();
         data.laserFiredCount = 0;
         data.laserKilledCount = 0;
         data.laserLastFiredTick = 0L;
         data.setDirty();
      }

      boolean missingLaserPlan = data.ownerId != null
         && data.stage.ordinal() >= StoryModeIntroSavedData.Stage.LASER_EXECUTION.ordinal()
         && data.stage.ordinal() <= StoryModeIntroSavedData.Stage.PLAYER_HUNT.ordinal()
         && data.laserTargetIds.isEmpty();
      if (missingLaserPlan) {
         data.stage = StoryModeIntroSavedData.Stage.TEMPLE;
         data.laserFiredCount = 0;
         data.laserKilledCount = 0;
         data.laserLastFiredTick = 0L;
         data.setDirty();
      }

      boolean invalidOwnerState = data.ownerId == null && data.stage != StoryModeIntroSavedData.Stage.NOT_STARTED;
      if (invalidOwnerState) {
         data.stage = StoryModeIntroSavedData.Stage.NOT_STARTED;
         data.playerClassId = 0;
         data.dungeonPlaced = false;
         data.bossId = null;
         data.hunterIds.clear();
         data.gateId = null;
         data.cartenonInstanceId = 0;
         data.godStatueId = null;
         data.godStatuePosition = null;
         data.laserTargetIds.clear();
         data.laserFiredCount = 0;
         data.laserKilledCount = 0;
         data.laserLastFiredTick = 0L;
         data.postIntroDungeonClears = 0;
         data.dungeonClearReceipts.clear();
         data.setDirty();
      } else if (data.ownerId != null && data.playerClassId == 0) {
         data.playerClassId = 1;
         data.setDirty();
      }

      if (olderSchema) {
         data.setDirty();
      }

      return data;
   }

   private static ListTag saveUuidList(List<UUID> values) {
      ListTag list = new ListTag();

      for (UUID value : values) {
         if (value != null) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", value);
            list.add(entry);
         }
      }

      return list;
   }

   private static List<UUID> loadUuidList(CompoundTag root, String key, int maximum) {
      Set<UUID> result = new LinkedHashSet<>();
      ListTag list = root.getList(key, 10);

      for (int index = 0; index < list.size() && result.size() < maximum; index++) {
         CompoundTag entry = list.getCompound(index);
         if (entry.hasUUID("Id")) {
            result.add(entry.getUUID("Id"));
         }
      }

      return List.copyOf(result);
   }

   private static int sanitizeClass(int classId) {
      return classId != 1 && classId != 3 ? 0 : classId;
   }

   private static String normalizeDungeonClearReceipt(String receipt) {
      if (receipt == null) {
         return "";
      }

      String normalized = receipt.trim();
      return normalized.length() <= 256 ? normalized : normalized.substring(0, 256);
   }

   private static boolean equalsUuid(@Nullable UUID first, @Nullable UUID second) {
      return first == null ? second == null : first.equals(second);
   }

   public enum Stage {
      NOT_STARTED,
      PREPARING,
      ANCIENT_GOLEM,
      GATE_WAIT,
      TEMPLE,
      LASER_EXECUTION,
      WAITING_FOR_SNEAK,
      STATUE_WAKING,
      STATUE_HUNT,
      PLAYER_HUNT,
      COMPLETE;

      private static StoryModeIntroSavedData.Stage load(String name) {
         if (name != null && !name.isBlank()) {
            try {
               return valueOf(name);
            } catch (IllegalArgumentException ignored) {
               return NOT_STARTED;
            }
         } else {
            return NOT_STARTED;
         }
      }

      private static boolean recognizes(String name) {
         if (name != null && !name.isBlank()) {
            try {
               valueOf(name);
               return true;
            } catch (IllegalArgumentException ignored) {
               return false;
            }
         } else {
            return false;
         }
      }
   }
}
