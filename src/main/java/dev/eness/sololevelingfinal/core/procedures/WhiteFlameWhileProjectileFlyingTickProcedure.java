package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class WhiteFlameWhileProjectileFlyingTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity immediatesourceentity) {
      if (immediatesourceentity != null) {
         if (Math.random() < 0.5 && world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.WHITE_FLAMES.get(), x, y, z, 1, 0.02, 0.02, 0.02, 0.1);
         }

         if (Math.random() < 0.2 && world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
         }

         Vec3 motion = immediatesourceentity.getDeltaMovement().scale(1.0);
         immediatesourceentity.setDeltaMovement(motion);
         immediatesourceentity.setNoGravity(true);
         if (immediatesourceentity.getPersistentData().getDouble("Timer") < 10.0) {
            immediatesourceentity.getPersistentData().putDouble("Timer", immediatesourceentity.getPersistentData().getDouble("Timer") + 1.0);
         } else if (!immediatesourceentity.level().isClientSide()) {
            immediatesourceentity.discard();
         }
      }
   }
}
