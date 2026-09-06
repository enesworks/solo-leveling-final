package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ExchangeDim6Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      }

      String stringprev = "";
      stringprev = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).ExchangeDimensions;
      if (stringprev.contains(",")) {
         stringprev = stringprev.replace(stringprev.substring(stringprev.indexOf("."), stringprev.indexOf(",") + ",".length()), ".");
         if (stringprev.contains(",")) {
            stringprev = stringprev.replace(stringprev.substring(stringprev.indexOf("."), stringprev.indexOf(",") + ",".length()), ".");
            if (stringprev.contains(",")) {
               stringprev = stringprev.replace(stringprev.substring(stringprev.indexOf("."), stringprev.indexOf(",") + ",".length()), ".");
               if (stringprev.contains(",")) {
                  stringprev = stringprev.replace(stringprev.substring(stringprev.indexOf("."), stringprev.indexOf(",") + ",".length()), ".");
                  if (stringprev.contains(",")) {
                     stringprev = stringprev.replace(stringprev.substring(stringprev.indexOf("."), stringprev.indexOf(",") + ",".length()), ".");
                     if (stringprev.contains(",")) {
                        return stringprev.substring(stringprev.indexOf(".") + ".".length(), stringprev.indexOf(","));
                     }
                  }
               }
            }
         }
      }

      return "";
   }
}
