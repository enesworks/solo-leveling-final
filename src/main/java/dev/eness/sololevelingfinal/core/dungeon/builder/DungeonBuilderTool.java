package dev.eness.sololevelingfinal.core.dungeon.builder;

import java.util.List;
import net.minecraft.ChatFormatting;

public enum DungeonBuilderTool {
   SURVEYOR(ChatFormatting.AQUA, List.of("structure_bounds", "room_bounds")),
   SOCKET(ChatFormatting.GOLD, List.of("corridor", "required_corridor", "stair_up", "required_stair_up", "stair_down", "required_stair_down")),
   ENCOUNTER(ChatFormatting.RED, List.of("spawn_point", "trigger_region")),
   FEATURE(ChatFormatting.YELLOW, List.of("player_start", "exit", "return_portal")),
   BUILDER(ChatFormatting.GREEN, List.of("studio", "capture", "status", "preview", "validate", "undo", "erase", "export"));

   private final ChatFormatting color;
   private final List<String> modes;

   DungeonBuilderTool(ChatFormatting color, List<String> modes) {
      this.color = color;
      this.modes = modes;
   }

   public ChatFormatting color() {
      return this.color;
   }

   public List<String> modes() {
      return this.modes;
   }

   public String mode(int index) {
      return this.modes.get(Math.floorMod(index, this.modes.size()));
   }

   public int nextMode(int index) {
      return Math.floorMod(index + 1, this.modes.size());
   }
}
