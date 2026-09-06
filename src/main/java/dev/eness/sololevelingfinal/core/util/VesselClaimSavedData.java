package dev.eness.sololevelingfinal.core.util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class VesselClaimSavedData extends SavedData {
   private static final String DATA_NAME = "sololeveling_vessel_claims";
   private final Map<String, LinkedHashSet<UUID>> claims = new LinkedHashMap<>();

   public static VesselClaimSavedData get(ServerLevel level) {
      return level.getServer()
         .overworld()
         .getDataStorage()
         .computeIfAbsent(VesselClaimSavedData::load, VesselClaimSavedData::new, "sololeveling_vessel_claims");
   }

   public boolean tryClaim(String key, UUID playerId, int limit) {
      LinkedHashSet<UUID> target = this.claims.computeIfAbsent(key, ignored -> new LinkedHashSet<>());
      if (target.contains(playerId)) {
         return true;
      }

      if (limit > 0 && target.size() >= limit) {
         return false;
      }

      this.releaseInternal(playerId);
      target = this.claims.computeIfAbsent(key, ignored -> new LinkedHashSet<>());
      target.add(playerId);
      this.setDirty();
      return true;
   }

   public void claimExisting(String key, UUID playerId) {
      Set<UUID> target = this.claims.computeIfAbsent(key, ignored -> new LinkedHashSet<>());
      if (!target.contains(playerId)) {
         this.releaseInternal(playerId);
         this.claims.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(playerId);
         this.setDirty();
      }
   }

   public void release(UUID playerId) {
      if (this.releaseInternal(playerId)) {
         this.setDirty();
      }
   }

   public int count(String key) {
      return this.claims.getOrDefault(key, new LinkedHashSet<>()).size();
   }

   private boolean releaseInternal(UUID playerId) {
      boolean changed = false;

      for (Set<UUID> owners : this.claims.values()) {
         changed |= owners.remove(playerId);
      }

      this.claims.entrySet().removeIf(entry -> entry.getValue().isEmpty());
      return changed;
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag tag) {
      ListTag entries = new ListTag();

      for (Entry<String, LinkedHashSet<UUID>> claim : this.claims.entrySet()) {
         for (UUID owner : claim.getValue()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Key", claim.getKey());
            entry.putUUID("Owner", owner);
            entries.add(entry);
         }
      }

      tag.put("Claims", entries);
      return tag;
   }

   private static VesselClaimSavedData load(CompoundTag tag) {
      VesselClaimSavedData data = new VesselClaimSavedData();
      ListTag entries = tag.getList("Claims", 10);

      for (int i = 0; i < entries.size(); i++) {
         CompoundTag entry = entries.getCompound(i);
         if (entry.contains("Key") && entry.hasUUID("Owner")) {
            data.claims.computeIfAbsent(entry.getString("Key"), ignored -> new LinkedHashSet<>()).add(entry.getUUID("Owner"));
         }
      }

      return data;
   }
}
