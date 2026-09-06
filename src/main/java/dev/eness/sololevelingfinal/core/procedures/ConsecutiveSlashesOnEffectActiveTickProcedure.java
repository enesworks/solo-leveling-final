package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class ConsecutiveSlashesOnEffectActiveTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 5, false, false));
         }

         CooldownManager.set(entity, "mana_refresh", 40);
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP > 100.0
            )
          {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .MP
               - 5.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = _setval;
               capability.syncPlayerVariables(entity);
            });
         } else {
            if (entity instanceof LivingEntity _entity) {
               _entity.removeEffect(SololevelingModMobEffects.CONSECUTIVE_SLASHES.get());
            }

            CooldownManager.set(entity, "mana_refresh", 60);
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.GOODSLASH_1.get(), x, y, z, 4, 5.0, 5.0, 5.0, 0.0);
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.GOOD_SLASH_2.get(), x, y, z, 12, 5.0, 5.0, 5.0, 0.0);
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 4, 5.0, 5.0, 5.0, 0.0);
         }

         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(5.0), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entity != entityiterator) {
               DamageSource _damageSource = new DamageSource(
                  world.registryAccess()
                     .registryOrThrow(Registries.DAMAGE_TYPE)
                     .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin"))),
                  entity
               );
               if (_damageSource != null) {
                  entityiterator.hurt(
                     new DamageSource(
                        world.registryAccess()
                           .registryOrThrow(Registries.DAMAGE_TYPE)
                           .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin"))),
                        entity
                     ),
                     (float)(TemporaryStatBonusManager.effectiveStrength(entity) / 50.0 + 2.0)
                  );
               }
            }
         }
      }
   }
}
