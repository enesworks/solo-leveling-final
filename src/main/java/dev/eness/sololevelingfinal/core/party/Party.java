package dev.eness.sololevelingfinal.core.party;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

final class Party {
   static final int MAX_MEMBERS = 8;
   static final int MAX_REQUESTS = 16;
   private final UUID id;
   private final String name;
   private final String legacyKey;
   private boolean legacyElectionOpen;
   private UUID leaderId;
   private boolean discoverable;
   private final LinkedHashMap<UUID, PartyMember> members = new LinkedHashMap<>();
   private final LinkedHashMap<UUID, PartyJoinRequest> requests = new LinkedHashMap<>();

   Party(UUID id, String name, PartyMember leader) {
      this(id, name, leader, "");
   }

   Party(UUID id, String name, PartyMember leader, String legacyKey) {
      this.id = id;
      this.name = name;
      this.legacyKey = legacyKey == null ? "" : legacyKey;
      this.legacyElectionOpen = !this.legacyKey.isEmpty();
      this.leaderId = leader.id();
      this.discoverable = true;
      this.members.put(leader.id(), leader);
   }

   private Party(UUID id, String name, String legacyKey, boolean legacyElectionOpen, UUID leaderId, boolean discoverable) {
      this.id = id;
      this.name = name;
      this.legacyKey = legacyKey == null ? "" : legacyKey;
      this.legacyElectionOpen = !this.legacyKey.isEmpty() && legacyElectionOpen;
      this.leaderId = leaderId;
      this.discoverable = discoverable;
   }

   UUID id() {
      return this.id;
   }

   String name() {
      return this.name;
   }

   String legacyKey() {
      return this.legacyKey;
   }

   UUID leaderId() {
      return this.leaderId;
   }

   boolean isLeader(UUID playerId) {
      return playerId != null && playerId.equals(this.leaderId);
   }

   boolean discoverable() {
      return this.discoverable;
   }

   void toggleDiscoverable() {
      this.discoverable = !this.discoverable;
   }

   Collection<PartyMember> members() {
      return List.copyOf(this.members.values());
   }

   Collection<PartyJoinRequest> requests() {
      return List.copyOf(this.requests.values());
   }

   PartyMember member(UUID playerId) {
      return this.members.get(playerId);
   }

   boolean contains(UUID playerId) {
      return this.members.containsKey(playerId);
   }

   boolean full() {
      return this.members.size() >= 8;
   }

   int size() {
      return this.members.size();
   }

   boolean addMember(PartyMember member) {
      if (member != null && !this.full() && !this.members.containsKey(member.id())) {
         this.members.put(member.id(), member);
         this.requests.remove(member.id());
         return true;
      } else {
         return false;
      }
   }

   boolean removeMember(UUID playerId) {
      if (playerId != null && this.members.remove(playerId) != null) {
         if (playerId.equals(this.leaderId)) {
            this.leaderId = this.members.isEmpty() ? null : this.members.keySet().iterator().next();
         }

         return true;
      } else {
         return false;
      }
   }

   boolean transferLeader(UUID playerId) {
      if (playerId != null && !playerId.equals(this.leaderId) && this.members.containsKey(playerId)) {
         this.leaderId = playerId;
         return true;
      } else {
         return false;
      }
   }

   void preferLegacyLeader(UUID playerId) {
      if (this.legacyElectionOpen && playerId != null && this.members.containsKey(playerId) && (this.leaderId == null || playerId.compareTo(this.leaderId) < 0)
         )
       {
         this.leaderId = playerId;
      }
   }

   void settleLegacyLeadership() {
      this.legacyElectionOpen = false;
   }

   boolean updateMember(PartyMember update) {
      PartyMember current = update == null ? null : this.members.get(update.id());
      return current != null && current.update(update.name(), update.level(), update.rank());
   }

   PartyJoinRequest request(UUID playerId) {
      return this.requests.get(playerId);
   }

   boolean addRequest(PartyJoinRequest request) {
      if (request != null && this.requests.size() < 16 && !this.requests.containsKey(request.playerId())) {
         this.requests.put(request.playerId(), request);
         return true;
      } else {
         return false;
      }
   }

   boolean removeRequest(UUID playerId) {
      return playerId != null && this.requests.remove(playerId) != null;
   }

   CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("Id", this.id);
      tag.putString("Name", this.name);
      if (!this.legacyKey.isEmpty()) {
         tag.putString("LegacyKey", this.legacyKey);
      }

      if (!this.legacyKey.isEmpty()) {
         tag.putBoolean("LegacyElectionOpen", this.legacyElectionOpen);
      }

      if (this.leaderId != null) {
         tag.putUUID("LeaderId", this.leaderId);
      }

      tag.putBoolean("Discoverable", this.discoverable);
      ListTag memberTags = new ListTag();

      for (PartyMember member : this.members.values()) {
         memberTags.add(member.save());
      }

      tag.put("Members", memberTags);
      ListTag requestTags = new ListTag();

      for (PartyJoinRequest request : this.requests.values()) {
         requestTags.add(request.save());
      }

      tag.put("Requests", requestTags);
      return tag;
   }

   static Party load(CompoundTag tag) {
      if (tag != null && tag.hasUUID("Id")) {
         UUID leaderId = tag.hasUUID("LeaderId") ? tag.getUUID("LeaderId") : null;
         String legacyKey = tag.getString("LegacyKey");
         Party party = new Party(
            tag.getUUID("Id"),
            tag.getString("Name"),
            legacyKey,
            !tag.contains("LegacyElectionOpen") || tag.getBoolean("LegacyElectionOpen"),
            leaderId,
            !tag.contains("Discoverable") || tag.getBoolean("Discoverable")
         );
         ListTag memberTags = tag.getList("Members", 10);

         for (int index = 0; index < memberTags.size() && party.members.size() < 8; index++) {
            PartyMember member = PartyMember.load(memberTags.getCompound(index));
            if (member != null) {
               party.members.putIfAbsent(member.id(), member);
            }
         }

         if (party.members.isEmpty()) {
            return null;
         }

         if (party.leaderId == null || !party.members.containsKey(party.leaderId)) {
            party.leaderId = party.members.keySet().iterator().next();
         }

         ListTag requestTags = tag.getList("Requests", 10);
         long now = System.currentTimeMillis();

         for (int index = 0; index < requestTags.size() && party.requests.size() < 16; index++) {
            PartyJoinRequest request = PartyJoinRequest.load(requestTags.getCompound(index));
            if (request != null && !request.expired(now) && !party.members.containsKey(request.playerId())) {
               party.requests.putIfAbsent(request.playerId(), request);
            }
         }

         return party;
      } else {
         return null;
      }
   }
}
