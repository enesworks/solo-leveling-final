package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class DoesHaveMurderiousIntentProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
            .abilities
            .contains("murderiousintent");
   }
}
