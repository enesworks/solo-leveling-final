package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class FrostBladeLivingEntityIsHitWithToolProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double delay = 0.0;
         if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
         }
      }
   }
}
