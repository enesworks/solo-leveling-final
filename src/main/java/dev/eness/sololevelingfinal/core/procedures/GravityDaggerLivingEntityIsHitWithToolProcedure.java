package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class GravityDaggerLivingEntityIsHitWithToolProcedure {
   public static void execute(Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (sourceentity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get())) {
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0, false, false));
            }

            CooldownManager.set(sourceentity, "lev_dagger", 200);
         }
      }
   }
}
