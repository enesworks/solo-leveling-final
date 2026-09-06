package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class DetectEyeInvOnInitialEntitySpawnProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 170, 1, false, false));
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.DETECT_EYE.get(), x, y + 1.0, z, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }
}
