package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class LightBallWhileProjectileFlyingTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity immediatesourceentity) {
      if (entity != null && immediatesourceentity != null) {
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.GLOW_YELLOW.get(), x, y, z, 10, 0.1, 0.1, 0.1, 0.1);
         }

         immediatesourceentity.setNoGravity(true);
         if (!(entity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(SololevelingModMobEffects.TIEMR.get()))
            && entity instanceof LivingEntity _entity
            && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.TIEMR.get(), 9999, 1, false, false));
         }

         if ((
                  entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(SololevelingModMobEffects.TIEMR.get())
                     ? _livEnt.getEffect(SololevelingModMobEffects.TIEMR.get()).getDuration()
                     : 0
               )
               % 20
            == 0) {
            if (world instanceof Level _level && !_level.isClientSide()) {
               _level.explode(null, x, y, z, 3.0F, ExplosionInteraction.NONE);
            }

            if (!immediatesourceentity.level().isClientSide()) {
               immediatesourceentity.discard();
            }
         }
      }
   }
}
