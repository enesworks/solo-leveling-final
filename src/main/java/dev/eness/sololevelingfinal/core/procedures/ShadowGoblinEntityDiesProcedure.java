package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ShadowGoblinEntityDiesProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double _setval = (entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .GobShadow
            - 1.0;
         (entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)
            .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .ifPresent(capability -> {
               capability.GobShadow = _setval;
               capability.syncPlayerVariables(entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null);
            });
      }
   }
}
