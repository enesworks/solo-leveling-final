package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ShadowIgrisEntityDiesProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof TamableAnimal tame && tame.isTame()) {
            Entity owner = tame.getOwner();
            if (owner != null) {
               owner.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.IgrisSpawned = 0.0;
                  capability.syncPlayerVariables(owner);
               });
            }
         }
      }
   }
}
