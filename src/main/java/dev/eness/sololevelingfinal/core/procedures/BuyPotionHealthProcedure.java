package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class BuyPotionHealthProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
            >= 100.0) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 100.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) + 5.0F);
            }

            if (entity instanceof Player _player) {
               _player.closeContainer();
            }
         }
      }
   }
}
