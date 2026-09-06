package dev.eness.sololevelingfinal.core.guild;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public class GuildHunter {
   public UUID id;
   public String name;
   public String rank;
   public String hunterClass;
   public String status;

   public GuildHunter(UUID id, String name, String rank, String hunterClass) {
      this.id = id;
      this.name = name;
      this.rank = rank;
      this.hunterClass = hunterClass;
      this.status = "idle";
   }

   public int rankScore() {
      return switch (this.rank) {
         case "E" -> 1;
         case "D" -> 2;
         case "C" -> 3;
         case "B" -> 4;
         case "A" -> 5;
         case "S" -> 6;
         default -> 0;
      };
   }

   public static String rankColor(String rank) {
      return switch (rank) {
         case "E" -> "§7";
         case "D" -> "§f";
         case "C" -> "§a";
         case "B" -> "§b";
         case "A" -> "§e";
         case "S" -> "§6";
         default -> "§7";
      };
   }

   public static String classColor(String cls) {
      return switch (cls) {
         case "Assassin" -> "§5";
         case "Fighter" -> "§c";
         case "Tanker" -> "§6";
         case "Mage" -> "§9";
         case "Ranger" -> "§a";
         case "Healer" -> "§d";
         default -> "§7";
      };
   }

   public static String hireMaterialId(String rank) {
      return switch (rank) {
         case "E" -> "minecraft:iron_ingot";
         case "D" -> "minecraft:gold_ingot";
         default -> "minecraft:diamond";
      };
   }

   public static String hireMaterialName(String rank) {
      return switch (rank) {
         case "E" -> "Iron";
         case "D" -> "Gold";
         default -> "Diamond";
      };
   }

   public static int hireCost(String rank) {
      return switch (rank) {
         case "E" -> 8;
         case "D" -> 8;
         case "C" -> 4;
         case "B" -> 8;
         case "A" -> 16;
         case "S" -> 32;
         default -> 0;
      };
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("id", this.id);
      tag.putString("name", this.name);
      tag.putString("rank", this.rank);
      tag.putString("class", this.hunterClass);
      tag.putString("status", this.status);
      return tag;
   }

   public static GuildHunter load(CompoundTag tag) {
      GuildHunter h = new GuildHunter(tag.getUUID("id"), tag.getString("name"), tag.getString("rank"), tag.getString("class"));
      h.status = tag.contains("status") ? tag.getString("status") : "idle";
      return h;
   }
}
