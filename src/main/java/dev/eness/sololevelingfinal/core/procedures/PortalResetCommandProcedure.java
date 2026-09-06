package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class PortalResetCommandProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Portals has been reset!"), true);
         }

         SololevelingModVariables.MapVariables.get(world).gatetimer = 0.0;
         SololevelingModVariables.MapVariables.get(world).syncData(world);
         SololevelingModVariables.MapVariables.get(world).portalreset = true;
         SololevelingModVariables.MapVariables.get(world).syncData(world);
         SololevelingMod.queueServerWork(2, () -> {
            SololevelingModVariables.MapVariables.get(world).portalreset = false;
            SololevelingModVariables.MapVariables.get(world).syncData(world);
         });
      }
   }
}
