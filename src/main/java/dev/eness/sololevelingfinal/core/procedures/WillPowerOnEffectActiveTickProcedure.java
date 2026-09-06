package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class WillPowerOnEffectActiveTickProcedure {
   public static void execute(LevelAccessor world, double x, double z, Entity entity) {
      if (entity != null) {
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.FIRE_PARTICLE.get(), x, entity.getY() + 1.0, z, 5, 0.5, 0.5, 0.5, 1.0);
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.FLAME, x, entity.getY() + 1.0, z, 5, 0.5, 0.5, 0.5, 1.0);
         }
      }
   }
}
