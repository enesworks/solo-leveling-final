package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SLRcompletedDungeonsProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         double commaCount = 0.0;
         String gates_cleared = "";
         if (SololevelingModVariables.MapVariables.get(world).GatesCleared.isEmpty()
            && !SololevelingModVariables.MapVariables.get(world).GatesCleared.contains(",")) {
            if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("§l§9Amount of gates Cleared:"), false);
            }

            if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("§5 0"), false);
            }
         } else {
            gates_cleared = SololevelingModVariables.MapVariables.get(world).GatesCleared;

            for (int i = 0; i < gates_cleared.length(); i++) {
               if (gates_cleared.charAt(i) == ',') {
                  commaCount++;
               }
            }

            if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("§l§9Amount of gates Cleared: "), false);
            }

            if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("§6" + commaCount), false);
            }
         }
      }
   }
}
