package dev.eness.sololevelingfinal.core.guild;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class GuildSavedData extends SavedData {
   private static final String DATA_NAME = "solocraft_guilds";
   private final Map<UUID, GuildData> guilds = new LinkedHashMap<>();

   public static GuildSavedData get(ServerLevel level) {
      return level.getServer().overworld().getDataStorage().computeIfAbsent(GuildSavedData::load, GuildSavedData::new, "solocraft_guilds");
   }

   public GuildData createGuild(String name, UUID ownerUUID, String ownerName) {
      for (GuildData g : this.guilds.values()) {
         if (g.name.equalsIgnoreCase(name)) {
            return null;
         }
      }

      UUID id = UUID.randomUUID();
      GuildData g = new GuildData(id, name, ownerUUID, ownerName);
      HunterRecruitManager.fillPool(g);
      this.guilds.put(id, g);
      this.setDirty();
      return g;
   }

   public GuildData getGuild(UUID id) {
      return this.guilds.get(id);
   }

   public GuildData getGuildByOwner(UUID playerUUID) {
      for (GuildData g : this.guilds.values()) {
         if (g.ownerUUID.equals(playerUUID)) {
            return g;
         }
      }

      return null;
   }

   public GuildData getGuildForPlayer(UUID playerUUID) {
      for (GuildData g : this.guilds.values()) {
         if (g.ownerUUID.equals(playerUUID)) {
            return g;
         }

         for (GuildMemberPermissions p : g.memberPermissions) {
            if (p.playerUUID.equals(playerUUID)) {
               return g;
            }
         }
      }

      return null;
   }

   public List<GuildData> getLeaderboard() {
      List<GuildData> list = new ArrayList<>(this.guilds.values());
      list.sort(Comparator.<GuildData>comparingInt(g -> -g.totalClears).thenComparingInt(g -> -g.level).thenComparingLong(g -> -g.xp));
      return list;
   }

   public Collection<GuildData> allGuilds() {
      return this.guilds.values();
   }

   public boolean deleteGuild(UUID guildId) {
      boolean removed = this.guilds.remove(guildId) != null;
      if (removed) {
         this.setDirty();
      }

      return removed;
   }

   public void markDirty() {
      this.setDirty();
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag tag) {
      ListTag list = new ListTag();

      for (GuildData g : this.guilds.values()) {
         list.add(g.save());
      }

      tag.put("guilds", list);
      return tag;
   }

   private static GuildSavedData load(CompoundTag tag) {
      GuildSavedData data = new GuildSavedData();
      ListTag list = tag.getList("guilds", 10);

      for (int i = 0; i < list.size(); i++) {
         GuildData g = GuildData.load(list.getCompound(i));
         data.guilds.put(g.id, g);
      }

      return data;
   }
}
