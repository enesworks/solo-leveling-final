package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class NecroBlastWhileProjectileFlyingTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity immediatesourceentity) {
      if (immediatesourceentity != null) {
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 15, 0.2, 0.2, 0.2, 0.0);
         }

         immediatesourceentity.setNoGravity(true);
      }
   }
}
