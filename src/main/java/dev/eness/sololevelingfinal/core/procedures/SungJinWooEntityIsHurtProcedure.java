package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class SungJinWooEntityIsHurtProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!entity.level().isClientSide()) {
            entity.discard();
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 75, 1.0, 1.0, 1.0, 1.0);
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.MANA_PURPLE.get(), x, y, z, 50, 1.0, 1.0, 1.0, 1.0);
         }
      }
   }
}
