package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class IsNotBerserkProcedure {
   public static boolean execute(Entity entity) {
      if (entity != null && ShadowMonarchManager.shouldFollowOwner(entity)) {
         if (entity instanceof TamableAnimal tame && tame.isTame()) {
            Entity owner = tame.getOwner();
            return owner != null
               && owner.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> !capability.berserk).orElse(false);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
