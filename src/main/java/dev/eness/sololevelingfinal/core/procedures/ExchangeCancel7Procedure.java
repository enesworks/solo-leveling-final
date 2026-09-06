package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ExchangeCancel7Procedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double EX = 0.0;
         double EY = 0.0;
         double EZ = 0.0;
         String ESY = "";
         String ESX = "";
         String ESZ = "";
         if (ExchangeCon7Procedure.execute(entity)) {
            ESX = ExchangeCordReturn7Procedure.execute(entity);
            ESY = ExchangeDim7Procedure.execute(entity);
            String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .ExchangeDimensions
               .replace(ESY + ",", "");
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.ExchangeDimensions = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
               .ExchangeCords
               .replace(ESX + ",", "");
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.ExchangeCords = _setval;
               capability.syncPlayerVariables(entity);
            });
         }
      }
   }
}
