package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SLRcompletedDungeonPlayerProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Dungeon in"), false);
         }

         if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal(entity.getPersistentData().getString("dungeon_tag")), false);
         }

         if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Cleared Dungeons:"), false);
         }

         if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal(SololevelingModVariables.MapVariables.get(world).GatesCleared), false);
         }
      }
   }
}
