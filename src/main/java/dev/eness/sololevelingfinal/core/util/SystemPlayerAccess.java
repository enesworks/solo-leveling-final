package dev.eness.sololevelingfinal.core.util;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class SystemPlayerAccess {
   private SystemPlayerAccess() {
   }

   public static boolean hasSystem(Entity entity) {
      if (entity == null) {
         return false;
      } else {
         return DungeonBuilderMode.isActive(entity.level())
            ? false
            : entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(variables -> variables.Player).orElse(false);
      }
   }
}
