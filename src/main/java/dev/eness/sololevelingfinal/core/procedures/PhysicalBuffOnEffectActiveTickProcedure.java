package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class PhysicalBuffOnEffectActiveTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      if (world instanceof ServerLevel _level) {
         _level.sendParticles(SololevelingModParticleTypes.GLOW_AURA_RED.get(), x, y, z, 2, 0.2, 0.5, 0.2, 1.0);
      }
   }
}
