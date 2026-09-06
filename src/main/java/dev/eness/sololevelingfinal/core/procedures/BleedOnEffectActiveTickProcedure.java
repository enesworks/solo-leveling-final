package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;

public class BleedOnEffectActiveTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.DRIPPING_LAVA, x, y, z, 2, 0.0, 0.0, 0.0, 1.0);
         }

         if ((
                  entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(SololevelingModMobEffects.BLEED.get())
                     ? _livEnt.getEffect(SololevelingModMobEffects.BLEED.get()).getDuration()
                     : 0
               )
               % 30
            == 0) {
            entity.hurt(
               new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)),
               (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F) / 100.0F
            );
         }
      }
   }
}
