package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class BuyPotionFatigue3Procedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
            >= 800.0) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 800.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Fatigue
               - 1000.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Fatigue = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player _player) {
               _player.closeContainer();
            }
         }
      }
   }
}
