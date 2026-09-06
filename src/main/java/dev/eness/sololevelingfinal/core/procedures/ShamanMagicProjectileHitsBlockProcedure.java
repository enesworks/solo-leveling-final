package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ShamanMagicProjectileHitsBlockProcedure {
   public static void execute(Entity immediatesourceentity) {
      if (immediatesourceentity != null) {
         if (!immediatesourceentity.level().isClientSide()) {
            immediatesourceentity.discard();
         }
      }
   }
}
