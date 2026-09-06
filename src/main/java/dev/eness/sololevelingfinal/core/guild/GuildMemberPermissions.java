package dev.eness.sololevelingfinal.core.guild;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public class GuildMemberPermissions {
   public UUID playerUUID;
   public String playerName;
   public boolean canOpen = false;
   public boolean tabOverview = false;
   public boolean tabRoster = false;
   public boolean tabTeams = false;
   public boolean tabDungeons = false;
   public boolean tabStorage = false;
   public boolean tabBuffs = false;
   public boolean tabLeaderboard = false;

   public GuildMemberPermissions(UUID playerUUID, String playerName) {
      this.playerUUID = playerUUID;
      this.playerName = playerName;
   }

   public void setAll(boolean value) {
      this.canOpen = value;
      this.tabOverview = value;
      this.tabRoster = value;
      this.tabTeams = value;
      this.tabDungeons = value;
      this.tabStorage = value;
      this.tabBuffs = value;
      this.tabLeaderboard = value;
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("uuid", this.playerUUID);
      tag.putString("name", this.playerName);
      tag.putBoolean("canOpen", this.canOpen);
      tag.putBoolean("tabOverview", this.tabOverview);
      tag.putBoolean("tabRoster", this.tabRoster);
      tag.putBoolean("tabTeams", this.tabTeams);
      tag.putBoolean("tabDungeons", this.tabDungeons);
      tag.putBoolean("tabStorage", this.tabStorage);
      tag.putBoolean("tabBuffs", this.tabBuffs);
      tag.putBoolean("tabLeaderboard", this.tabLeaderboard);
      return tag;
   }

   public static GuildMemberPermissions load(CompoundTag tag) {
      GuildMemberPermissions p = new GuildMemberPermissions(tag.getUUID("uuid"), tag.getString("name"));
      p.canOpen = tag.getBoolean("canOpen");
      p.tabOverview = tag.getBoolean("tabOverview");
      p.tabRoster = tag.getBoolean("tabRoster");
      p.tabTeams = tag.getBoolean("tabTeams");
      p.tabDungeons = tag.getBoolean("tabDungeons");
      p.tabStorage = tag.getBoolean("tabStorage");
      p.tabBuffs = tag.getBoolean("tabBuffs");
      p.tabLeaderboard = tag.getBoolean("tabLeaderboard");
      return p;
   }

   public void writeToTag(CompoundTag out, int index) {
      out.put("perm_" + index, this.save());
   }

   public static GuildMemberPermissions readFromTag(CompoundTag in, int index) {
      return load(in.getCompound("perm_" + index));
   }
}
