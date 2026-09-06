package dev.eness.sololevelingfinal.core.party;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

final class PartyMember {
   private final UUID id;
   private String name;
   private int level;
   private String rank;

   PartyMember(UUID id, String name, int level, String rank) {
      this.id = id;
      this.name = cleanText(name, 32, "Unknown");
      this.level = Math.max(0, level);
      this.rank = cleanText(rank, 24, "Unranked");
   }

   UUID id() {
      return this.id;
   }

   String name() {
      return this.name;
   }

   int level() {
      return this.level;
   }

   String rank() {
      return this.rank;
   }

   boolean update(String nextName, int nextLevel, String nextRank) {
      String cleanName = cleanText(nextName, 32, "Unknown");
      String cleanRank = cleanText(nextRank, 24, "Unranked");
      int cleanLevel = Math.max(0, nextLevel);
      if (this.name.equals(cleanName) && this.level == cleanLevel && this.rank.equals(cleanRank)) {
         return false;
      }

      this.name = cleanName;
      this.level = cleanLevel;
      this.rank = cleanRank;
      return true;
   }

   CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("Id", this.id);
      tag.putString("Name", this.name);
      tag.putInt("Level", this.level);
      tag.putString("Rank", this.rank);
      return tag;
   }

   static PartyMember load(CompoundTag tag) {
      return tag != null && tag.hasUUID("Id") ? new PartyMember(tag.getUUID("Id"), tag.getString("Name"), tag.getInt("Level"), tag.getString("Rank")) : null;
   }

   private static String cleanText(String value, int maxLength, String fallback) {
      if (value == null) {
         return fallback;
      }

      String clean = value.trim();
      return clean.isEmpty() ? fallback : clean.substring(0, Math.min(maxLength, clean.length()));
   }
}
