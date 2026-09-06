package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;

public class ShieldBashAttackProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         double delay = 0.0;
         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entity != entityiterator) {
               for (int index0 = 0; index0 < 10; index0++) {
                  delay += 0.5;
                  SololevelingMod.queueServerWork((int)delay, () -> {
                     entity.setDeltaMovement(new Vec3(0.0, 0.0, 0.0));
                     if (entity instanceof LivingEntity _entityx) {
                        _entityx.swing(InteractionHand.OFF_HAND, true);
                     }
                  });
               }

               entity.setDeltaMovement(new Vec3(0.0, 0.0, 0.0));
               entityiterator.setDeltaMovement(new Vec3(0.0, 0.0, 0.0));
               if (entity instanceof LivingEntity _entity) {
                  _entity.removeEffect(SololevelingModMobEffects.SHIELD_BASH_EFFECT.get());
               }

               entityiterator.hurt(
                  new DamageSource(
                     world.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:tanker"))),
                     entity
                  ),
                  2.0F
               );
               if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 4, false, false));
               }

               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 4, false, false));
               }

               if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10, 4, false, false));
               }

               SololevelingMod.queueServerWork(
                  3,
                  () -> {
                     if (world instanceof Level _level) {
                        if (!_level.isClientSide()) {
                           _level.playSound(
                              (Player)null,
                              BlockPos.containing(x, y, z),
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                              SoundSource.NEUTRAL,
                              0.5F,
                              1.0F
                           );
                        } else {
                           _level.playLocalSound(
                              x,
                              y,
                              z,
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                              SoundSource.NEUTRAL,
                              0.5F,
                              1.0F,
                              false
                           );
                        }
                     }

                     entityiterator.setDeltaMovement(new Vec3(entity.getLookAngle().x * 2.0, entity.getLookAngle().y, entity.getLookAngle().z * 2.0));
                     world.addParticle(ParticleTypes.EXPLOSION, x, y + 1.6, z, 0.0, 1.0, 0.0);
                  }
               );
            }
         }
      }
   }
}
