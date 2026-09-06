package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class ElderBeastOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof LivingEntity _entity) {
            _entity.setHealth((float)((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) - 0.05));
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.GLOW_YELLOW.get(), x, y, z, 2, 1.0, 3.0, 1.0, 1.0);
         }
      }
   }
}
