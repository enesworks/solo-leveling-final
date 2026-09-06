package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;

public class FireflyuEntityIsHurtProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      if (world instanceof ServerLevel _level) {
         _level.sendParticles(ParticleTypes.FLAME, x, y, z, 5, 0.125, 0.125, 0.125, 0.0);
      }
   }
}
