package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class GoldTextProcedure {
   public static String execute(Entity entity) {
      return entity == null
         ? ""
         : "§6Total Gold: "
            + Math.round(
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
            );
   }
}
