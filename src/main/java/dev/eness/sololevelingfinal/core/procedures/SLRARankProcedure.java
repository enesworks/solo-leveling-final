package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SLRARankProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         String _setval = "A";
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.ranking = _setval;
            capability.syncPlayerVariables(entity);
         });
      }
   }
}
