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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.AncientGolemEntity;

public class AncientGolemOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.isAlive()) {
            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
               if (world.getEntitiesOfClass(Player.class, AABB.ofSize(new Vec3(x, y, z), 32.0, 32.0, 32.0), e -> true).isEmpty()) {
                  if (entity instanceof AncientGolemEntity _datEntSetI) {
                     _datEntSetI.getEntityData().set(AncientGolemEntity.DATA_IA, 0);
                  }
               } else {
                  if (entity instanceof AncientGolemEntity _datEntSetI) {
                     _datEntSetI.getEntityData()
                        .set(
                           AncientGolemEntity.DATA_IA,
                           (entity instanceof AncientGolemEntity _datEntI ? _datEntI.getEntityData().get(AncientGolemEntity.DATA_IA) : 0) + 1
                        );
                  }

                  if ((entity instanceof AncientGolemEntity _datEntI ? _datEntI.getEntityData().get(AncientGolemEntity.DATA_IA) : 0) == 100) {
                     if (entity instanceof AncientGolemEntity) {
                        ((AncientGolemEntity)entity).setAnimation("slam");
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false));
                     }
                  }

                  if ((entity instanceof AncientGolemEntity _datEntI ? _datEntI.getEntityData().get(AncientGolemEntity.DATA_IA) : 0) == 115) {
                     if (world instanceof Level _level) {
                        if (!_level.isClientSide()) {
                           _level.playSound(
                              (Player)null,
                              BlockPos.containing(x, y, z),
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                              SoundSource.HOSTILE,
                              3.0F,
                              1.0F
                           );
                        } else {
                           _level.playLocalSound(
                              x,
                              y,
                              z,
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                              SoundSource.HOSTILE,
                              3.0F,
                              1.0F,
                              false
                           );
                        }
                     }

                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 32, 1.0, 1.0, 1.0, 1.0);
                     }

                     Vec3 _center = new Vec3(x, y, z);

                     for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12.0), e -> true)
                        .stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList()) {
                        if (!(entityiterator instanceof LivingEntity _livEnt12 && _livEnt12.isBlocking()) && entityiterator != entity) {
                           entityiterator.hurt(
                              new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 7.0F
                           );
                           entityiterator.setDeltaMovement(new Vec3(0.0, 1.0, 0.0));
                           if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                              _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false));
                           }
                        }
                     }
                  }

                  if ((entity instanceof AncientGolemEntity _datEntI ? _datEntI.getEntityData().get(AncientGolemEntity.DATA_IA) : 0) == 122) {
                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 128, 4.0, 1.0, 4.0, 1.0);
                     }

                     Vec3 _center = new Vec3(x, y, z);

                     for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(18.0), e -> true)
                        .stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList()) {
                        if (!(entityiterator instanceof LivingEntity _livEnt21 && _livEnt21.isBlocking())
                           && entityiterator.onGround()
                           && entityiterator != entity) {
                           entityiterator.hurt(
                              new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 7.0F
                           );
                           entityiterator.setDeltaMovement(new Vec3(0.0, 1.0, 0.0));
                           if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                              _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false));
                           }
                        }
                     }
                  }

                  if ((entity instanceof AncientGolemEntity _datEntI ? _datEntI.getEntityData().get(AncientGolemEntity.DATA_IA) : 0) == 129) {
                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 256, 8.0, 1.0, 8.0, 1.0);
                     }

                     Vec3 _center = new Vec3(x, y, z);

                     for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(24.0), e -> true)
                        .stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList()) {
                        if (!(entityiterator instanceof LivingEntity _livEnt31 && _livEnt31.isBlocking())
                           && entityiterator.onGround()
                           && entityiterator != entity) {
                           entityiterator.hurt(
                              new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 7.0F
                           );
                           entityiterator.setDeltaMovement(new Vec3(0.0, 1.0, 0.0));
                           if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                              _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false));
                           }
                        }
                     }
                  }
               }
            } else if (entity instanceof AncientGolemEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(AncientGolemEntity.DATA_IA, 0);
            }

            if ((entity instanceof AncientGolemEntity _datEntI ? _datEntI.getEntityData().get(AncientGolemEntity.DATA_IA) : 0) >= 130
               && entity instanceof AncientGolemEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(AncientGolemEntity.DATA_IA, 0);
            }
         }
      }
   }
}
