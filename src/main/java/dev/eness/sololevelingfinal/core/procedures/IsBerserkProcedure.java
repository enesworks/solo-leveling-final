package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class IsBerserkProcedure {
   public static boolean execute(Entity entity) {
      if (entity == null) {
         return false;
      } else if (entity instanceof TamableAnimal tame && tame.isTame()) {
         Entity owner = tame.getOwner();
         return owner != null
            && owner.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.berserk).orElse(false);
      } else {
         return false;
      }
   }
}
