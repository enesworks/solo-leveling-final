package dev.eness.sololevelingfinal.core.party;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

final class PartyJoinRequest {
   private final UUID playerId;
   private final String playerName;
   private final long expiresAtMillis;

   PartyJoinRequest(UUID playerId, String playerName, long expiresAtMillis) {
      this.playerId = playerId;
      String cleanName = playerName == null ? "" : playerName.trim();
      this.playerName = cleanName.isEmpty() ? "Unknown" : cleanName.substring(0, Math.min(32, cleanName.length()));
      this.expiresAtMillis = expiresAtMillis;
   }

   UUID playerId() {
      return this.playerId;
   }

   String playerName() {
      return this.playerName;
   }

   long expiresAtMillis() {
      return this.expiresAtMillis;
   }

   boolean expired(long now) {
      return this.expiresAtMillis <= now;
   }

   CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("Id", this.playerId);
      tag.putString("Name", this.playerName);
      tag.putLong("ExpiresAt", this.expiresAtMillis);
      return tag;
   }

   static PartyJoinRequest load(CompoundTag tag) {
      return tag != null && tag.hasUUID("Id") ? new PartyJoinRequest(tag.getUUID("Id"), tag.getString("Name"), tag.getLong("ExpiresAt")) : null;
   }
}
