package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class SlashWhileProjectileFlyingTickProcedure {
   public static void execute(Entity entity, Entity immediatesourceentity) {
      if (entity != null && immediatesourceentity != null) {
         immediatesourceentity.setNoGravity(true);
         if (Math.sqrt(
                  Math.pow(immediatesourceentity.getX() - entity.getX(), 2.0)
                     + Math.pow(immediatesourceentity.getY() - entity.getY(), 2.0)
                     + Math.pow(immediatesourceentity.getZ() - entity.getZ(), 2.0)
               )
               >= 16.0
            && !immediatesourceentity.level().isClientSide()) {
            immediatesourceentity.discard();
         }
      }
   }
}
