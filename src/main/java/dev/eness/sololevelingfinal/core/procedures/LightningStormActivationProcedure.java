package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class LightningStormActivationProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         CooldownManager.set(entity, "job_2", 400);
         double _setval = 10.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.baranlightningstrike = _setval;
            capability.syncPlayerVariables(entity);
         });
      }
   }
}
