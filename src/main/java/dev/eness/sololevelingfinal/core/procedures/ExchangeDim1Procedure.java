package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ExchangeDim1Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
               .ExchangeDimensions
               .contains(",")
            ? entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
               .ExchangeDimensions
               .substring(
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                        .ExchangeDimensions
                        .indexOf(".")
                     + ".".length(),
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .ExchangeDimensions
                     .indexOf(",")
               )
            : "";
      }
   }
}
