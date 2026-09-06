package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SurviveTextProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).punishment
               > 0.0
            ? "§4Survive §7"
               + Math.round(
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).punishment
               )
               + " §fSeconds"
            : " ";
      }
   }
}
