package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class DragonBreatheWhileProjectileFlyingTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity immediatesourceentity) {
      if (entity != null && immediatesourceentity != null) {
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.FLAME, x, y, z, 20, 1.0, 1.0, 1.0, 0.0);
         }

         if (Math.sqrt(
                  Math.pow(entity.getX() - immediatesourceentity.getX(), 2.0)
                     + Math.pow(entity.getY() - immediatesourceentity.getY(), 2.0)
                     + Math.pow(entity.getZ() - immediatesourceentity.getZ(), 2.0)
               )
               >= 20.0
            && !immediatesourceentity.level().isClientSide()) {
            immediatesourceentity.discard();
         }
      }
   }
}
