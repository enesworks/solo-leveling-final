package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ReturnLycanProcedure {
   public static String execute(Entity entity) {
      return entity == null
         ? ""
         : Math.round(
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).WolfShadow
            )
            + "/"
            + Math.round(
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).WolfShadowMax
            );
   }
}
