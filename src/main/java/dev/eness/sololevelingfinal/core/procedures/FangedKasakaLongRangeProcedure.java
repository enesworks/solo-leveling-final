package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.entity.FxPuddleEntity;
import dev.eness.sololevelingfinal.core.entity.FxspikEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class FangedKasakaLongRangeProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         double particleNum = 0.0;
         double vX = 0.0;
         double vY = 0.0;
         double vZ = 0.0;
         double i = 0.0;
         double x_pos = 0.0;
         double z_pos = 0.0;
         double speed = 0.0;
         double arcAngle = 0.0;
         double radAngle = 0.0;
         double radYaw = 0.0;
         double radPitch = 0.0;
         double angle = 0.0;
         double y_pos = 0.0;
         double radius = 0.0;
         if (entity.getPersistentData().getDouble("IA") == 20.0) {
            entity.getPersistentData().putDouble("MODE", Mth.nextInt(RandomSource.create(), 1, 2));
         }

         if (entity.getPersistentData().getDouble("MODE") == 1.0) {
            if (entity.getPersistentData().getDouble("IA") == 21.0 && entity instanceof FangedKasakaEntity) {
               ((FangedKasakaEntity)entity).setAnimation("tail_spike");
            }

            if (entity.getPersistentData().getDouble("IA") == 95.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FXSPIK
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 105.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FXSPIK
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 115.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FXSPIK
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 125.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FXSPIK
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 135.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FXSPIK
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 145.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FXSPIK
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 155.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FXSPIK
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 165.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FXSPIK
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 180.0) {
               entity.getPersistentData().putString("state", "");
               entity.getPersistentData().putDouble("IA", 0.0);
            }
         }

         if (entity.getPersistentData().getDouble("MODE") == 2.0) {
            if (entity.getPersistentData().getDouble("IA") == 21.0 && entity instanceof FangedKasakaEntity) {
               ((FangedKasakaEntity)entity).setAnimation("breath_spit");
            }

            if (entity.getPersistentData().getDouble("IA") == 95.0 && world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, entity.getX(), entity.getY() + 11.0, entity.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
            }

            if (entity.getPersistentData().getDouble("IA") == 130.0) {
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, entity.getX(), entity.getY() + 11.0, entity.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FX_PUDDLE
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 150.0) {
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, entity.getX(), entity.getY() + 11.0, entity.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FX_PUDDLE
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 170.0) {
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, entity.getX(), entity.getY() + 9.0, entity.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FX_PUDDLE
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 190.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(48.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof FxspikEntity)
                     && !(entityiterator instanceof ItemEntity)
                     && !(entityiterator instanceof ItemFrame)
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof FxPuddleEntity)) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.FX_PUDDLE
                           .get()
                           .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                     }

                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, false, false));
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 200.0) {
               entity.getPersistentData().putString("state", "");
               entity.getPersistentData().putDouble("IA", 0.0);
            }
         }
      }
   }
}
