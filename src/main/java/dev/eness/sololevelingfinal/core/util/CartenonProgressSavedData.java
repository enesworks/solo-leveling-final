package dev.eness.sololevelingfinal.core.util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class CartenonProgressSavedData extends SavedData {
   private static final String DATA_NAME = "sololeveling_cartenon_progress";
   private final Map<UUID, CartenonProgressSavedData.PlayerProgress> players = new LinkedHashMap<>();
   private final Set<Integer> builtInstances = new LinkedHashSet<>();
   private int nextInstanceId = 1;

   public static CartenonProgressSavedData get(ServerLevel level) {
      return level.getServer()
         .overworld()
         .getDataStorage()
         .computeIfAbsent(CartenonProgressSavedData::load, CartenonProgressSavedData::new, "sololeveling_cartenon_progress");
   }

   public boolean recordDungeonClear(UUID playerId, String dungeonTag) {
      if (playerId != null && dungeonTag != null && !dungeonTag.isBlank()) {
         CartenonProgressSavedData.PlayerProgress progress = this.progress(playerId);
         if (!progress.resolved && !progress.gateOffered && progress.dungeonTags.add(dungeonTag)) {
            this.setDirty();
            return progress.dungeonTags.size() >= progress.targetClear;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public int allocateInstance() {
      int instanceId = Math.max(1, this.nextInstanceId++);
      this.setDirty();
      return instanceId;
   }

   public void markGateOffered(UUID playerId, int instanceId) {
      CartenonProgressSavedData.PlayerProgress progress = this.progress(playerId);
      progress.gateOffered = true;
      progress.instanceId = instanceId;
      this.setDirty();
   }

   public void cancelGateOffer(UUID playerId) {
      CartenonProgressSavedData.PlayerProgress progress = this.progress(playerId);
      progress.gateOffered = false;
      progress.instanceId = 0;
      this.setDirty();
   }

   public void associateInstance(UUID playerId, int instanceId) {
      CartenonProgressSavedData.PlayerProgress progress = this.progress(playerId);
      if (progress.instanceId != instanceId) {
         progress.instanceId = instanceId;
         this.setDirty();
      }
   }

   public int instanceFor(UUID playerId) {
      CartenonProgressSavedData.PlayerProgress progress = this.players.get(playerId);
      return progress == null ? 0 : progress.instanceId;
   }

   public boolean isResolved(UUID playerId) {
      CartenonProgressSavedData.PlayerProgress progress = this.players.get(playerId);
      return progress != null && progress.resolved;
   }

   public boolean accepted(UUID playerId) {
      CartenonProgressSavedData.PlayerProgress progress = this.players.get(playerId);
      return progress != null && progress.resolved && progress.accepted;
   }

   public void resolve(UUID playerId, boolean accepted) {
      CartenonProgressSavedData.PlayerProgress progress = this.progress(playerId);
      progress.resolved = true;
      progress.accepted = accepted;
      this.setDirty();
   }

   public boolean isInstanceBuilt(int instanceId) {
      return this.builtInstances.contains(instanceId);
   }

   public void markInstanceBuilt(int instanceId) {
      if (instanceId > 0 && this.builtInstances.add(instanceId)) {
         this.setDirty();
      }
   }

   private CartenonProgressSavedData.PlayerProgress progress(UUID playerId) {
      return this.players.computeIfAbsent(playerId, id -> new CartenonProgressSavedData.PlayerProgress(1 + Math.floorMod(id.hashCode(), 3)));
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag tag) {
      tag.putInt("NextInstance", this.nextInstanceId);
      tag.put("BuiltInstances", new IntArrayTag(this.builtInstances.stream().mapToInt(Integer::intValue).toArray()));
      ListTag playerList = new ListTag();

      for (Entry<UUID, CartenonProgressSavedData.PlayerProgress> entry : this.players.entrySet()) {
         CompoundTag playerTag = new CompoundTag();
         playerTag.putUUID("Player", entry.getKey());
         CartenonProgressSavedData.PlayerProgress progress = entry.getValue();
         playerTag.putInt("TargetClear", progress.targetClear);
         playerTag.putBoolean("GateOffered", progress.gateOffered);
         playerTag.putBoolean("Resolved", progress.resolved);
         playerTag.putBoolean("Accepted", progress.accepted);
         playerTag.putInt("Instance", progress.instanceId);
         ListTag clears = new ListTag();

         for (String dungeonTag : progress.dungeonTags) {
            clears.add(StringTag.valueOf(dungeonTag));
         }

         playerTag.put("DungeonTags", clears);
         playerList.add(playerTag);
      }

      tag.put("Players", playerList);
      return tag;
   }

   private static CartenonProgressSavedData load(CompoundTag tag) {
      CartenonProgressSavedData data = new CartenonProgressSavedData();
      data.nextInstanceId = Math.max(1, tag.getInt("NextInstance"));

      for (int instanceId : tag.getIntArray("BuiltInstances")) {
         if (instanceId > 0) {
            data.builtInstances.add(instanceId);
         }
      }

      ListTag playerList = tag.getList("Players", 10);

      for (int i = 0; i < playerList.size(); i++) {
         CompoundTag playerTag = playerList.getCompound(i);
         if (playerTag.hasUUID("Player")) {
            UUID playerId = playerTag.getUUID("Player");
            int targetClear = playerTag.contains("TargetClear", 3)
               ? Math.max(1, Math.min(3, playerTag.getInt("TargetClear")))
               : 1 + Math.floorMod(playerId.hashCode(), 3);
            CartenonProgressSavedData.PlayerProgress progress = new CartenonProgressSavedData.PlayerProgress(targetClear);
            progress.gateOffered = playerTag.getBoolean("GateOffered");
            progress.resolved = playerTag.getBoolean("Resolved");
            progress.accepted = playerTag.getBoolean("Accepted");
            progress.instanceId = Math.max(0, playerTag.getInt("Instance"));
            ListTag clears = playerTag.getList("DungeonTags", 8);

            for (int clearIndex = 0; clearIndex < clears.size(); clearIndex++) {
               progress.dungeonTags.add(clears.getString(clearIndex));
            }

            data.players.put(playerId, progress);
         }
      }

      return data;
   }

   private static final class PlayerProgress {
      private final int targetClear;
      private final LinkedHashSet<String> dungeonTags = new LinkedHashSet<>();
      private boolean gateOffered;
      private boolean resolved;
      private boolean accepted;
      private int instanceId;

      private PlayerProgress(int targetClear) {
         this.targetClear = targetClear;
      }
   }
}
