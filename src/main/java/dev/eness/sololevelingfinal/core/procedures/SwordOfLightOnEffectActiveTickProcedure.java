package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class SwordOfLightOnEffectActiveTickProcedure {
   public static void execute(LevelAccessor world, double x, double z, Entity entity) {
      if (entity != null) {
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(
               SololevelingModParticleTypes.GLOW_AURA_YELLOW.get(),
               x,
               entity.getY() + entity.getBbHeight() / 2.0F,
               z,
               2,
               entity.getBbWidth() / 1.5,
               entity.getBbHeight() / 2.0F,
               entity.getBbWidth() / 1.5,
               0.0
            );
         }
      }
   }
}
