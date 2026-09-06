package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class PanelEarlyThisGUIIsOpenedProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double _setval = 1.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.investvalue = _setval;
            capability.syncPlayerVariables(entity);
         });
      }
   }
}
