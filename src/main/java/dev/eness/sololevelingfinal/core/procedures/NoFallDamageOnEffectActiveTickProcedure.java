package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;

public class NoFallDamageOnEffectActiveTickProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.fallDistance = 0.0F;
         if (entity.onGround() && entity instanceof LivingEntity _entity) {
            _entity.removeEffect(SololevelingModMobEffects.NO_FALL_DAMAGE.get());
         }
      }
   }
}
