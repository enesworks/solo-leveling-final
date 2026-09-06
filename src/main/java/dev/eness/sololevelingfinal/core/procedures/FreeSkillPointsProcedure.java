package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class FreeSkillPointsProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .SkillPoints
            + 10.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.SkillPoints = _setval;
            capability.syncPlayerVariables(entity);
         });
         boolean _setvalx = false;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.giftstatus = _setval;
            capability.syncPlayerVariables(entity);
         });
         if (entity instanceof Player _player) {
            _player.closeContainer();
         }
      }
   }
}
