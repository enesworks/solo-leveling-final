package dev.eness.sololevelingfinal.core.party;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class PartySavedData extends SavedData {
   private static final String DATA_NAME = "solocraft_parties";
   private final Map<UUID, Party> parties = new LinkedHashMap<>();
   private final Map<UUID, UUID> playerParties = new LinkedHashMap<>();
   private final Map<String, UUID> partyNames = new LinkedHashMap<>();
   private final Map<String, UUID> legacyParties = new LinkedHashMap<>();
   private final Map<UUID, Set<UUID>> outgoingRequests = new LinkedHashMap<>();
   private final Map<UUID, PartySavedData.GlowPreference> glowPreferences = new LinkedHashMap<>();
   private final Set<UUID> knownPlayers = new LinkedHashSet<>();
   private final Set<String> retiredLegacyKeys = new LinkedHashSet<>();

   public static PartySavedData get(ServerLevel level) {
      return level.getServer().overworld().getDataStorage().computeIfAbsent(PartySavedData::load, PartySavedData::new, "solocraft_parties");
   }

   Party party(UUID id) {
      return id == null ? null : this.parties.get(id);
   }

   Party partyForPlayer(UUID playerId) {
      if (playerId == null) {
         return null;
      }

      UUID partyId = this.playerParties.get(playerId);
      Party party = partyId == null ? null : this.parties.get(partyId);
      return party != null && party.contains(playerId) ? party : null;
   }

   Party partyByName(String name) {
      if (name == null) {
         return null;
      }

      String key = name.trim().toLowerCase(Locale.ROOT);
      return this.parties.get(this.partyNames.get(key));
   }

   Party partyByLegacyKey(String legacyKey) {
      return legacyKey != null && !legacyKey.isEmpty() ? this.parties.get(this.legacyParties.get(legacyKey)) : null;
   }

   Party create(String name, PartyMember leader) {
      if (name != null && leader != null && this.partyByName(name) == null && this.partyForPlayer(leader.id()) == null) {
         Party party = new Party(UUID.randomUUID(), name, leader);
         this.parties.put(party.id(), party);
         this.indexParty(party);
         this.knownPlayers.add(leader.id());
         this.setDirty();
         return party;
      } else {
         return null;
      }
   }

   Party createMigrated(String name, String legacyKey, PartyMember leader) {
      if (name != null
         && legacyKey != null
         && !legacyKey.isEmpty()
         && leader != null
         && this.partyByName(name) == null
         && this.partyByLegacyKey(legacyKey) == null
         && !this.retiredLegacyKeys.contains(legacyKey)
         && this.partyForPlayer(leader.id()) == null) {
         Party party = new Party(UUID.randomUUID(), name, leader, legacyKey);
         this.parties.put(party.id(), party);
         this.indexParty(party);
         this.knownPlayers.add(leader.id());
         this.setDirty();
         return party;
      } else {
         return null;
      }
   }

   boolean isKnownPlayer(UUID playerId) {
      return playerId != null && this.knownPlayers.contains(playerId);
   }

   void markKnownPlayer(UUID playerId) {
      if (playerId != null && this.knownPlayers.add(playerId)) {
         this.setDirty();
      }
   }

   boolean remove(UUID partyId) {
      if (partyId == null) {
         return false;
      }

      Party removed = this.parties.remove(partyId);
      if (removed == null) {
         return false;
      }

      this.unindexParty(removed);
      if (!removed.legacyKey().isEmpty()) {
         this.retiredLegacyKeys.add(removed.legacyKey());
      }

      this.setDirty();
      return true;
   }

   boolean isRetiredLegacyKey(String legacyKey) {
      return legacyKey != null && this.retiredLegacyKeys.contains(legacyKey);
   }

   boolean addMember(Party party, PartyMember member) {
      if (party != null && member != null && this.parties.get(party.id()) == party && !this.playerParties.containsKey(member.id()) && party.addMember(member)) {
         this.unindexRequest(member.id(), party.id());
         this.playerParties.put(member.id(), party.id());
         this.knownPlayers.add(member.id());
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   boolean addRequest(Party party, PartyJoinRequest request) {
      if (party != null && request != null && this.parties.get(party.id()) == party && party.addRequest(request)) {
         this.outgoingRequests.computeIfAbsent(request.playerId(), ignored -> new LinkedHashSet<>()).add(party.id());
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   boolean removeRequest(Party party, UUID playerId) {
      if (party != null && playerId != null && this.parties.get(party.id()) == party && party.removeRequest(playerId)) {
         this.unindexRequest(playerId, party.id());
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   List<Party> requestedParties(UUID playerId) {
      Set<UUID> ids = playerId == null ? null : this.outgoingRequests.get(playerId);
      return ids != null && !ids.isEmpty() ? ids.stream().map(this.parties::get).filter(Objects::nonNull).toList() : List.of();
   }

   boolean removeMember(Party party, UUID playerId) {
      if (party != null && playerId != null && this.parties.get(party.id()) == party && party.removeMember(playerId)) {
         this.playerParties.remove(playerId, party.id());
         this.knownPlayers.add(playerId);
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   boolean removeRequestsForPlayer(UUID playerId) {
      Set<UUID> requestedIds = playerId == null ? null : this.outgoingRequests.get(playerId);
      if (requestedIds != null && !requestedIds.isEmpty()) {
         boolean changed = false;

         for (UUID partyId : List.copyOf(requestedIds)) {
            Party party = this.parties.get(partyId);
            if (party != null) {
               changed |= party.removeRequest(playerId);
            }

            this.unindexRequest(playerId, partyId);
         }

         if (changed) {
            this.setDirty();
         }

         return changed;
      } else {
         return false;
      }
   }

   boolean pruneExpiredRequests(long now) {
      boolean changed = false;

      for (Party party : this.parties.values()) {
         changed |= this.pruneExpiredRequests(party, now);
      }

      if (changed) {
         this.setDirty();
      }

      return changed;
   }

   boolean pruneExpiredRequests(Party party, long now) {
      if (party != null && this.parties.get(party.id()) == party) {
         boolean changed = false;

         for (PartyJoinRequest request : List.copyOf(party.requests())) {
            if (request.expired(now) && party.removeRequest(request.playerId())) {
               this.unindexRequest(request.playerId(), party.id());
               changed = true;
            }
         }

         if (changed) {
            this.setDirty();
         }

         return changed;
      } else {
         return false;
      }
   }

   boolean glowEnabled(UUID playerId) {
      PartySavedData.GlowPreference preference = this.glowPreferences.get(playerId);
      return preference == null || preference.enabled();
   }

   int glowColor(UUID playerId) {
      PartySavedData.GlowPreference preference = this.glowPreferences.get(playerId);
      return preference == null ? 5626111 : preference.color();
   }

   void setGlow(UUID playerId, boolean enabled, int color) {
      if (playerId != null) {
         PartySavedData.GlowPreference next = new PartySavedData.GlowPreference(enabled, color & 16777215);
         if (!next.equals(this.glowPreferences.get(playerId))) {
            this.glowPreferences.put(playerId, next);
            this.setDirty();
         }
      }
   }

   void markChanged() {
      this.setDirty();
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag root) {
      ListTag partyTags = new ListTag();

      for (Party party : this.parties.values()) {
         partyTags.add(party.save());
      }

      root.put("Parties", partyTags);
      ListTag preferenceTags = new ListTag();

      for (Entry<UUID, PartySavedData.GlowPreference> entry : this.glowPreferences.entrySet()) {
         CompoundTag tag = new CompoundTag();
         tag.putUUID("PlayerId", entry.getKey());
         tag.putBoolean("Enabled", entry.getValue().enabled());
         tag.putInt("Color", entry.getValue().color());
         preferenceTags.add(tag);
      }

      root.put("GlowPreferences", preferenceTags);
      ListTag knownPlayerTags = new ListTag();

      for (UUID playerId : this.knownPlayers) {
         CompoundTag tag = new CompoundTag();
         tag.putUUID("Id", playerId);
         knownPlayerTags.add(tag);
      }

      root.put("KnownPlayers", knownPlayerTags);
      ListTag retiredKeyTags = new ListTag();

      for (String legacyKey : this.retiredLegacyKeys) {
         CompoundTag tag = new CompoundTag();
         tag.putString("Key", legacyKey);
         retiredKeyTags.add(tag);
      }

      root.put("RetiredLegacyKeys", retiredKeyTags);
      return root;
   }

   private static PartySavedData load(CompoundTag root) {
      PartySavedData data = new PartySavedData();
      ListTag partyTags = root.getList("Parties", 10);

      for (int index = 0; index < partyTags.size(); index++) {
         Party party = Party.load(partyTags.getCompound(index));
         if (party != null
            && !data.parties.containsKey(party.id())
            && data.partyByName(party.name()) == null
            && (party.legacyKey().isEmpty() || data.partyByLegacyKey(party.legacyKey()) == null)
            && party.members().stream().noneMatch(member -> data.partyForPlayer(member.id()) != null)) {
            data.parties.put(party.id(), party);
            data.indexParty(party);
            party.members().forEach(member -> data.knownPlayers.add(member.id()));
         }
      }

      ListTag preferenceTags = root.getList("GlowPreferences", 10);

      for (int index = 0; index < preferenceTags.size(); index++) {
         CompoundTag tag = preferenceTags.getCompound(index);
         if (tag.hasUUID("PlayerId")) {
            data.glowPreferences
               .put(
                  tag.getUUID("PlayerId"),
                  new PartySavedData.GlowPreference(!tag.contains("Enabled") || tag.getBoolean("Enabled"), tag.getInt("Color") & 16777215)
               );
         }
      }

      ListTag knownPlayerTags = root.getList("KnownPlayers", 10);

      for (int index = 0; index < knownPlayerTags.size(); index++) {
         CompoundTag tag = knownPlayerTags.getCompound(index);
         if (tag.hasUUID("Id")) {
            data.knownPlayers.add(tag.getUUID("Id"));
         }
      }

      ListTag retiredKeyTags = root.getList("RetiredLegacyKeys", 10);

      for (int index = 0; index < retiredKeyTags.size(); index++) {
         String legacyKey = retiredKeyTags.getCompound(index).getString("Key");
         if (!legacyKey.isEmpty()) {
            data.retiredLegacyKeys.add(legacyKey);
         }
      }

      return data;
   }

   private void indexParty(Party party) {
      this.partyNames.put(party.name().trim().toLowerCase(Locale.ROOT), party.id());
      if (!party.legacyKey().isEmpty()) {
         this.legacyParties.put(party.legacyKey(), party.id());
      }

      for (PartyMember member : party.members()) {
         this.playerParties.put(member.id(), party.id());
      }

      for (PartyJoinRequest request : party.requests()) {
         this.outgoingRequests.computeIfAbsent(request.playerId(), ignored -> new LinkedHashSet<>()).add(party.id());
      }
   }

   private void unindexParty(Party party) {
      this.partyNames.remove(party.name().trim().toLowerCase(Locale.ROOT), party.id());
      if (!party.legacyKey().isEmpty()) {
         this.legacyParties.remove(party.legacyKey(), party.id());
      }

      for (PartyMember member : party.members()) {
         this.playerParties.remove(member.id(), party.id());
      }

      for (PartyJoinRequest request : party.requests()) {
         this.unindexRequest(request.playerId(), party.id());
      }
   }

   private void unindexRequest(UUID playerId, UUID partyId) {
      Set<UUID> ids = this.outgoingRequests.get(playerId);
      if (ids != null) {
         ids.remove(partyId);
         if (ids.isEmpty()) {
            this.outgoingRequests.remove(playerId);
         }
      }
   }

   private record GlowPreference(boolean enabled, int color) {
   }
}
