package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class Ab1OnKeyPressedProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).combatmode
            )
          {
            String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .Pslot1;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.PselectedPower = _setval;
               capability.syncPlayerVariables(entity);
            });
            double _setvalx = 1.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Skillcycle = _setval;
               capability.syncPlayerVariables(entity);
            });
         }
      }
   }
}
