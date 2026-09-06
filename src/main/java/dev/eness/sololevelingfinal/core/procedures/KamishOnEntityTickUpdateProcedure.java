package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.DragonBreatheEntity;
import dev.eness.sololevelingfinal.core.entity.KamishEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class KamishOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double Chain = 0.0;
         double ChainWait = 0.0;
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            entity.getPersistentData().putDouble("IA", entity.getPersistentData().getDouble("IA") + 1.0);
         } else {
            entity.getPersistentData().putDouble("IA", 0.0);
         }

         if (entity.getPersistentData().getDouble("IA") == 100.0) {
            if (entity instanceof KamishEntity) {
               ((KamishEntity)entity).setAnimation("empty");
            }

            if (entity instanceof KamishEntity) {
               ((KamishEntity)entity).setAnimation("firebreathing");
            }

            Chain = 20.0;

            for (int index0 = 0; index0 < (int)Chain; index0++) {
               SololevelingMod.queueServerWork(
                  (int)ChainWait,
                  () -> {
                     if ((entity instanceof Mob _mobEntxxxx ? _mobEntxxxx.getTarget() : null) != null) {
                        entity.lookAt(
                           Anchor.EYES,
                           new Vec3(
                              (entity instanceof Mob _mobEntxxx ? _mobEntxxx.getTarget() : null).getX(),
                              (entity instanceof Mob _mobEntxx ? _mobEntxx.getTarget() : null).getY() + 1.6,
                              (entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null).getZ()
                           )
                        );
                     }

                     Entity _shootFrom = entity;
                     Level projectileLevel = _shootFrom.level();
                     if (!projectileLevel.isClientSide()) {
                        Projectile _entityToSpawn = (new Object() {
                           public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                              AbstractArrow entityToSpawn = new DragonBreatheEntity(SololevelingModEntities.DRAGON_BREATHE.get(), level);
                              entityToSpawn.setOwner(shooter);
                              entityToSpawn.setBaseDamage(damage);
                              entityToSpawn.setKnockback(knockback);
                              entityToSpawn.setSilent(true);
                              return entityToSpawn;
                           }
                        }).getArrow(projectileLevel, entity, 5.0F, 0);
                        _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
                        _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 1.0F, 0.0F);
                        projectileLevel.addFreshEntity(_entityToSpawn);
                     }
                  }
               );
               ChainWait += 3.0;
            }
         }

         if (entity.getPersistentData().getDouble("IA") == 240.0) {
            if (entity instanceof KamishEntity) {
               ((KamishEntity)entity).setAnimation("empty");
            }

            if (entity instanceof KamishEntity) {
               ((KamishEntity)entity).setAnimation("ground smash");
            }
         }

         if (entity.getPersistentData().getDouble("IA") == 312.0) {
            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                     SoundSource.HOSTILE,
                     4.0F,
                     1.0F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.HOSTILE, 4.0F, 1.0F, false
                  );
               }
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 10, 0.1, 0.5, 0.1, 1.0);
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.DRAGON_BREATH, x, y, z, 10, 10.0, 2.0, 10.0, 0.0);
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.LAVA, x, y, z, 2, 10.0, 2.0, 10.0, 0.0);
            }

            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entity != entityiterator) {
                  if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 5, false, false));
                  }

                  if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 5, false, false));
                  }

                  entityiterator.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.INDIRECT_MAGIC), entity),
                     25.0F
                  );
               }
            }
         }

         if (entity.getPersistentData().getDouble("IA") == 330.0) {
            entity.getPersistentData().putDouble("IA", 0.0);
            entity.setDeltaMovement(
               new Vec3(
                  ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX() - entity.getX()) * 0.35
                     - ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX() - entity.getX()) * 0.05,
                  ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() - entity.getY()) * 0.35
                     - ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() - entity.getY()) * 0.05
                     + 0.3,
                  ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ() - entity.getZ()) * 0.35
                     - ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ() - entity.getZ()) * 0.05
               )
            );
         }
      }
   }
}
