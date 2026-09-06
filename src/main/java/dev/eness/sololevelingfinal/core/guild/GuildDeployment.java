package dev.eness.sololevelingfinal.core.guild;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public class GuildDeployment {
   public UUID id;
   public UUID teamId;
   public String teamName;
   public String gateLabel;
   public int gateRank;
   public String gateEntityUUID = "";
   public long completesAt;
   public long xpReward;

   public GuildDeployment(UUID id, UUID teamId, String teamName, String gateLabel, int gateRank, String gateEntityUUID, long completesAt, long xpReward) {
      this.id = id;
      this.teamId = teamId;
      this.teamName = teamName;
      this.gateLabel = gateLabel;
      this.gateRank = gateRank;
      this.gateEntityUUID = gateEntityUUID;
      this.completesAt = completesAt;
      this.xpReward = xpReward;
   }

   public long ticksRemaining(long currentGameTime) {
      return this.completesAt - currentGameTime;
   }

   public static String rankLabel(int rank) {
      return switch (rank) {
         case 1 -> "E";
         case 2 -> "D";
         case 3 -> "C";
         case 4 -> "B";
         case 5 -> "A";
         case 6 -> "S";
         default -> "E";
      };
   }

   public static String rankColor(int rank) {
      return switch (rank) {
         case 1 -> "§7";
         case 2 -> "§f";
         case 3 -> "§a";
         case 4 -> "§b";
         case 5 -> "§e";
         case 6 -> "§6";
         default -> "§7";
      };
   }

   public static long durationTicks(int rank) {
      return switch (rank) {
         case 1 -> 3600L;
         case 2 -> 6000L;
         case 3 -> 9600L;
         case 4 -> 14400L;
         case 5 -> 24000L;
         case 6 -> 36000L;
         default -> 3600L;
      };
   }

   public static long xpForRank(int rank) {
      return switch (rank) {
         case 1 -> 200L;
         case 2 -> 500L;
         case 3 -> 1200L;
         case 4 -> 3000L;
         case 5 -> 8000L;
         case 6 -> 20000L;
         default -> 200L;
      };
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("id", this.id);
      tag.putUUID("teamId", this.teamId);
      tag.putString("teamName", this.teamName);
      tag.putString("gateLabel", this.gateLabel);
      tag.putInt("gateRank", this.gateRank);
      tag.putString("gateEntityUUID", this.gateEntityUUID);
      tag.putLong("completesAt", this.completesAt);
      tag.putLong("xpReward", this.xpReward);
      return tag;
   }

   public static GuildDeployment load(CompoundTag tag) {
      return new GuildDeployment(
         tag.getUUID("id"),
         tag.getUUID("teamId"),
         tag.contains("teamName") ? tag.getString("teamName") : "?",
         tag.getString("gateLabel"),
         tag.getInt("gateRank"),
         tag.contains("gateEntityUUID") ? tag.getString("gateEntityUUID") : "",
         tag.getLong("completesAt"),
         tag.getLong("xpReward")
      );
   }
}
