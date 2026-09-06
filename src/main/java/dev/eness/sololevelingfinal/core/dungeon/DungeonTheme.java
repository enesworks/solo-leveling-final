package dev.eness.sololevelingfinal.core.dungeon;

import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum DungeonTheme {
   STONE(
      "§8Stone Vault",
      Blocks.STONE_BRICKS,
      Blocks.CRACKED_STONE_BRICKS,
      Blocks.MOSSY_STONE_BRICKS,
      Blocks.SMOOTH_STONE,
      Blocks.CHISELED_STONE_BRICKS,
      Blocks.SEA_LANTERN
   ),
   ICE("§bFrozen Crypt", Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.PACKED_ICE, Blocks.SMOOTH_STONE, Blocks.ICE, Blocks.SOUL_LANTERN),
   NETHER(
      "§cNether Forge", Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS, Blocks.BLACKSTONE, Blocks.BASALT, Blocks.SHROOMLIGHT
   ),
   DESERT(
      "§eDesert Tomb", Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SEA_LANTERN
   ),
   MOSSY("§aOvergrown Ruins", Blocks.MOSSY_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.STONE_BRICKS, Blocks.DIRT, Blocks.OAK_LOG, Blocks.SHROOMLIGHT),
   VOID("§5Void Sanctum", Blocks.BLACKSTONE, Blocks.GILDED_BLACKSTONE, Blocks.OBSIDIAN, Blocks.BLACKSTONE, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN),
   DEEPSLATE(
      "§8Deepslate Citadel",
      Blocks.DEEPSLATE_BRICKS,
      Blocks.DEEPSLATE_TILES,
      Blocks.CRACKED_DEEPSLATE_BRICKS,
      Blocks.POLISHED_DEEPSLATE,
      Blocks.CHISELED_DEEPSLATE,
      Blocks.SOUL_LANTERN
   ),
   PRISMARINE(
      "§3Sunken Ruins",
      Blocks.PRISMARINE_BRICKS,
      Blocks.DARK_PRISMARINE,
      Blocks.PRISMARINE,
      Blocks.DARK_PRISMARINE,
      Blocks.PRISMARINE_BRICKS,
      Blocks.SEA_LANTERN
   );

   public final String displayName;
   public final Block wall;
   public final Block accent;
   public final Block rare;
   public final Block floor;
   public final Block pillar;
   public final Block light;

   DungeonTheme(String displayName, Block wall, Block accent, Block rare, Block floor, Block pillar, Block light) {
      this.displayName = displayName;
      this.wall = wall;
      this.accent = accent;
      this.rare = rare;
      this.floor = floor;
      this.pillar = pillar;
      this.light = light;
   }

   public static DungeonTheme fromString(String value) {
      for (DungeonTheme theme : values()) {
         if (theme.name().equalsIgnoreCase(value)) {
            return theme;
         }
      }

      return random();
   }

   public static DungeonTheme random() {
      DungeonTheme[] values = values();
      return values[new Random().nextInt(values.length)];
   }
}
