package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class DkcButtonVisibilityConditionProcedure {
   public static boolean execute(Entity entity, double floor) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      return floor != 1.0 ? vars.dkc_cleared >= floor - 1.0 : vars.dkc_started || vars.dkc_cleared > 0.0;
   }
}
