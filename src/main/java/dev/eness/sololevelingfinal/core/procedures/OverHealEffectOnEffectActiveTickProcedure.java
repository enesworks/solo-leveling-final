package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

public class OverHealEffectOnEffectActiveTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof Player) {
            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, x, y, z, 1, 0.2, 0.3, 0.2, 1.0);
            }

            if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)
                  < (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F)
               && entity instanceof LivingEntity _entity) {
               _entity.setHealth((float)((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) + 0.1));
            }
         } else {
            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 2, 0.2, 0.3, 0.2, 1.0);
            }

            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false));
            }

            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0, false, false));
            }
         }
      }
   }
}
