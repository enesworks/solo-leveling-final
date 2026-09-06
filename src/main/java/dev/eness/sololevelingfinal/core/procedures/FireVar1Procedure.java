package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class FireVar1Procedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.FireVar = _setval;
            capability.syncPlayerVariables(entity);
         });
         if (entity instanceof Player _player) {
            _player.closeContainer();
         }
      }
   }
}
