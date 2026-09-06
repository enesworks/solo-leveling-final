package dev.eness.sololevelingfinal.core.guild;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public class GuildTeam {
   public UUID id;
   public String name;
   public List<UUID> memberIds = new ArrayList<>();
   public boolean autoRaidEnabled = false;
   public int autoRaidMaxRank = 1;
   public static final int MAX_SIZE = 5;

   public GuildTeam(UUID id, String name) {
      this.id = id;
      this.name = name;
   }

   public boolean hasHunter(UUID hunterId) {
      return this.memberIds.contains(hunterId);
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("id", this.id);
      tag.putString("name", this.name);
      tag.putBoolean("autoRaidEnabled", this.autoRaidEnabled);
      tag.putInt("autoRaidMaxRank", this.autoRaidMaxRank);
      ListTag list = new ListTag();

      for (UUID uid : this.memberIds) {
         list.add(StringTag.valueOf(uid.toString()));
      }

      tag.put("members", list);
      return tag;
   }

   public static GuildTeam load(CompoundTag tag) {
      GuildTeam t = new GuildTeam(tag.getUUID("id"), tag.getString("name"));
      t.autoRaidEnabled = tag.getBoolean("autoRaidEnabled");
      t.autoRaidMaxRank = tag.contains("autoRaidMaxRank") ? Math.max(1, Math.min(6, tag.getInt("autoRaidMaxRank"))) : 1;
      ListTag list = tag.getList("members", 8);

      for (int i = 0; i < list.size(); i++) {
         t.memberIds.add(UUID.fromString(list.getString(i)));
      }

      return t;
   }
}
