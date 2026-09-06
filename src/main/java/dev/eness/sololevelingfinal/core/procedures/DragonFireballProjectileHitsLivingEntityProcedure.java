package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.SololevelingMod;

public class DragonFireballProjectileHitsLivingEntityProcedure {
   public static void execute(LevelAccessor world, Entity immediatesourceentity) {
      if (immediatesourceentity != null) {
         SololevelingMod.queueServerWork(2, () -> {
            if (!immediatesourceentity.level().isClientSide()) {
               immediatesourceentity.discard();
            }
         });
      }
   }
}
