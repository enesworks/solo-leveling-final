package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

public class DoesHaveExchangeProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : VesselProgressionManager.isShadowMonarch(entity)
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).ShadowExchange;
   }
}
