package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class DkcFloorTextReturnProcedure {
   public static String execute(Entity entity, double floor) {
      if (entity == null) {
         return "";
      } else {
         SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (floor == 1.0 && !vars.dkc_started && vars.dkc_cleared <= 0.0) {
            return "Sealed";
         } else if (vars.dkc_cleared >= floor) {
            return "Cleared";
         } else if (vars.dkc_cleared == floor - 1.0) {
            return "Current";
         } else {
            return vars.dkc_cleared < floor - 1.0 ? "Not Yet Unlocked!" : "";
         }
      }
   }
}
