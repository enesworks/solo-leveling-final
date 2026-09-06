package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class ManaBulletWhileProjectileFlyingTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity immediatesourceentity) {
      if (immediatesourceentity != null) {
         immediatesourceentity.setNoGravity(true);
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.END_ROD, x, y, z, 10, 0.2, 0.2, 0.2, 0.0);
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.MANA_RED.get(), x, y, z, 5, 0.05, 0.05, 0.05, 0.0);
         }

         immediatesourceentity.getPersistentData().putDouble("TimerT", immediatesourceentity.getPersistentData().getDouble("TimerT") + 1.0);
         if (immediatesourceentity.getPersistentData().getDouble("TimerT") >= 10.0 && !immediatesourceentity.level().isClientSide()) {
            immediatesourceentity.discard();
         }
      }
   }
}
