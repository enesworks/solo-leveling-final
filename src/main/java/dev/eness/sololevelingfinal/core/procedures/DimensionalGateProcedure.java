package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class DimensionalGateProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("ability wip"), false);
         }
      }
   }
}
