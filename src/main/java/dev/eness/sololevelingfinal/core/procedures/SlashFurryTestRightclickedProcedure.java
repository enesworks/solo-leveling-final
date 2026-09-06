package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SlashFurryTestRightclickedProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).slashfurrybroad
            && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).slashfur
            )
          {
            if (entity.isShiftKeyDown()) {
               boolean _setval = true;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.slashfur = _setval;
                  capability.syncPlayerVariables(entity);
               });
            } else {
               boolean _setval = true;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.slashfur = _setval;
                  capability.syncPlayerVariables(entity);
               });
            }
         }
      }
   }
}
