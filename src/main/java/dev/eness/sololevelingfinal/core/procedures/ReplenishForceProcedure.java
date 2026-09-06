package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ReplenishForceProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof LivingEntity _entity) {
            _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
         }

         double _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Fatigue = _setval;
            capability.syncPlayerVariables(entity);
         });
         boolean _setvalx = false;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.giftstatus = _setval;
            capability.syncPlayerVariables(entity);
         });
         if (entity instanceof Player _player) {
            _player.closeContainer();
         }
      }
   }
}
