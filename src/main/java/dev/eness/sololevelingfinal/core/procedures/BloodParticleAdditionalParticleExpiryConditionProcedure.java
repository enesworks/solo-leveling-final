package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class BloodParticleAdditionalParticleExpiryConditionProcedure {
   public static boolean execute(LevelAccessor world, double x, double y, double z, boolean onGround) {
      if (onGround) {
         world.addParticle(SololevelingModParticleTypes.BLOOD_PARTICLE_LAND.get(), x, y, z, 0.0, 0.0, 0.0);
         return true;
      } else {
         return false;
      }
   }
}
