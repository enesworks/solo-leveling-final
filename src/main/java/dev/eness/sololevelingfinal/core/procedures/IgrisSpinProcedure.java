package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;

public class IgrisSpinProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            if (entity.getPersistentData().getString("state").equals("spin")) {
               if (entity.getPersistentData().getDouble("MF") == 1.0) {
                  if (entity instanceof BloodRedComIgrisEntity) {
                     ((BloodRedComIgrisEntity)entity).setAnimation("empty");
                  }

                  if (entity instanceof BloodRedComIgrisEntity) {
                     ((BloodRedComIgrisEntity)entity).setAnimation("attack_spin");
                  }

                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 55, 5, false, false));
                  }
               }

               if (entity.getPersistentData().getDouble("MF") == 18.0) {
                  Vec3 _center = new Vec3(x, y, z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(5.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entityiterator != entity && entityiterator instanceof LivingEntity) {
                        if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                           _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false));
                        }

                        entityiterator.hurt(
                           new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity),
                           scaledDamage(entity, 0.75F, 11.2F)
                        );
                        if (world instanceof ServerLevel _level) {
                           _level.sendParticles(
                              ParticleTypes.SWEEP_ATTACK,
                              entityiterator.getX(),
                              entityiterator.getY() + entity.getBbHeight() / 2.0F,
                              entityiterator.getZ(),
                              3,
                              0.1,
                              0.1,
                              0.1,
                              0.0
                           );
                        }
                     }
                  }
               }

               if (entity.getPersistentData().getDouble("MF") == 37.0) {
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           0.5F
                        );
                     } else {
                        _level.playLocalSound(
                           x,
                           y,
                           z,
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           0.5F,
                           false
                        );
                     }
                  }

                  Vec3 _center = new Vec3(x, y, z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entityiterator != entity && entityiterator.onGround() && entityiterator instanceof LivingEntity) {
                        entityiterator.hurt(
                           new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity),
                           scaledDamage(entity, 1.1F, 15.4F)
                        );
                        if (world instanceof ServerLevel _level) {
                           _level.sendParticles(
                              ParticleTypes.EXPLOSION,
                              entityiterator.getX(),
                              entityiterator.getY() + entity.getBbHeight() / 2.0F,
                              entityiterator.getZ(),
                              1,
                              0.1,
                              0.1,
                              0.1,
                              0.0
                           );
                        }
                     }
                  }
               }

               if (entity.getPersistentData().getDouble("MF") >= 62.0) {
                  entity.getPersistentData().putString("state", "idle");
                  entity.getPersistentData().putDouble("MF", 0.0);
               }
            }
         } else {
            entity.getPersistentData().putString("state", "idle");
            entity.getPersistentData().putDouble("MF", 0.0);
         }
      }
   }

   private static float scaledDamage(Entity entity, float multiplier, float minimum) {
      return entity instanceof LivingEntity living && living.getAttribute(Attributes.ATTACK_DAMAGE) != null
         ? (float)Math.max(minimum, living.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier)
         : minimum;
   }
}
