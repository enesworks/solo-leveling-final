package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;

public class FangedKasakaCloseRangeProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
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
            entity.getPersistentData().putDouble("MODE", Mth.nextInt(RandomSource.create(), 1, 4));
         }

         if (entity.getPersistentData().getDouble("MODE") == 1.0) {
            if (entity.getPersistentData().getDouble("IA") == 21.0 && entity instanceof FangedKasakaEntity) {
               ((FangedKasakaEntity)entity).setAnimation("attack_tail");
            }

            if (entity.getPersistentData().getDouble("IA") == 85.0) {
               Vec3 _center = new Vec3(entity.getX(), entity.getY(), entity.getZ());

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator && entityiterator.getY() - entity.getY() < 0.8) {
                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 15.0F
                     );
                     Entity _ent = entityiterator;
                     _ent.teleportTo(entityiterator.getX(), entityiterator.getY() + 0.2, entityiterator.getZ());
                     if (_ent instanceof ServerPlayer _serverPlayer) {
                        _serverPlayer.connection
                           .teleport(entityiterator.getX(), entityiterator.getY() + 0.2, entityiterator.getZ(), _ent.getYRot(), _ent.getXRot());
                     }

                     entityiterator.setDeltaMovement(
                        new Vec3(
                           (entityiterator.getX() - entity.getX())
                              * (
                                 1.0
                                    / Math.sqrt(
                                       Math.pow(entityiterator.getX() - entity.getX(), 2.0)
                                          + Math.pow(entityiterator.getY() - entity.getY(), 2.0)
                                          + Math.pow(entityiterator.getZ() - entity.getZ(), 2.0)
                                    )
                              ),
                           0.5,
                           (entityiterator.getZ() - entity.getZ())
                              * (
                                 1.0
                                    / Math.sqrt(
                                       Math.pow(entityiterator.getX() - entity.getX(), 2.0)
                                          + Math.pow(entityiterator.getY() - entity.getY(), 2.0)
                                          + Math.pow(entityiterator.getZ() - entity.getZ(), 2.0)
                                    )
                              )
                        )
                     );
                  }

                  if (entityiterator.getY() - entity.getY() >= 0.8) {
                     if (entityiterator instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("Jump Dodged!"), true);
                     }

                     if (world instanceof Level _level) {
                        if (!_level.isClientSide()) {
                           _level.playSound(
                              (Player)null,
                              BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()),
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              0.5F
                           );
                        } else {
                           _level.playLocalSound(
                              entityiterator.getX(),
                              entityiterator.getY(),
                              entityiterator.getZ(),
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              0.5F,
                              false
                           );
                        }
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 110.0) {
               entity.getPersistentData().putString("state", "");
               entity.getPersistentData().putDouble("IA", 0.0);
            }
         }

         if (entity.getPersistentData().getDouble("MODE") == 2.0) {
            if (entity.getPersistentData().getDouble("IA") == 21.0 && entity instanceof FangedKasakaEntity) {
               ((FangedKasakaEntity)entity).setAnimation("dash");
            }

            Vec3 _center = new Vec3(entity.getX(), entity.getY(), entity.getZ());

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entity != entityiterator && entityiterator.getY() - entity.getY() < 0.8) {
                  entityiterator.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 4.0F
                  );
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 65.0) {
               _center = new Vec3(
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(16.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getX(),
                  entity.getY(),
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(16.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getZ()
               );

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator) {
                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 12.0F
                     );
                  }
               }

               _center = new Vec3(
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(11.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getX(),
                  entity.getY(),
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(11.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getZ()
               );

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator) {
                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 12.0F
                     );
                  }
               }

               _center = new Vec3(
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(7.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getX(),
                  entity.getY(),
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(7.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getZ()
               );

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator) {
                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 12.0F
                     );
                  }
               }

               _center = new Vec3(
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(3.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getX(),
                  entity.getY(),
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(3.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getZ()
               );

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator) {
                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 12.0F
                     );
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 75.0) {
               entity.getPersistentData().putString("state", "");
               entity.getPersistentData().putDouble("IA", 0.0);
            }
         }

         if (entity.getPersistentData().getDouble("MODE") == 3.0) {
            if (entity.getPersistentData().getDouble("IA") == 21.0 && entity instanceof FangedKasakaEntity) {
               ((FangedKasakaEntity)entity).setAnimation("slash");
            }

            if (entity.getPersistentData().getDouble("IA") == 85.0) {
               Vec3 _center = new Vec3(
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(4.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getX(),
                  entity.getY(),
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(4.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getZ()
               );

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator) {
                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, false));
                     }

                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.BLEED.get(), 60, 0, false, false));
                     }

                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK)), 15.0F
                     );
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 110.0) {
               entity.getPersistentData().putString("state", "");
               entity.getPersistentData().putDouble("IA", 0.0);
               entity.getPersistentData().putDouble("MODE", 0.0);
            }
         }

         if (entity.getPersistentData().getDouble("MODE") == 4.0) {
            if (entity.getPersistentData().getDouble("IA") == 21.0 && entity instanceof FangedKasakaEntity) {
               ((FangedKasakaEntity)entity).setAnimation("breath");
            }

            if (entity.getPersistentData().getDouble("IA") == 100.0) {
               radius = 11.0;
               speed = 15.0;
               particleNum = 200.0;
               arcAngle = 180.0;
               radYaw = Math.toRadians(entity.getYRot() + 90.0F);
               radPitch = Math.toRadians((entity.getXRot() + 90.0F) * -1.0F);

               for (int index0 = 0; index0 < (int)particleNum; index0++) {
                  angle = i * (arcAngle / particleNum);
                  radAngle = Math.toRadians(angle);
                  vX = (Math.sin(radAngle) * Math.sin(radPitch) * Math.cos(radYaw) + Math.cos(radAngle) * Math.sin(radYaw)) * -1.0;
                  vY = Math.sin(radAngle) * Math.cos(radPitch);
                  vZ = Math.sin(radAngle) * Math.sin(radPitch) * Math.sin(radYaw) * -1.0 + Math.cos(radAngle) * Math.cos(radYaw);
                  x_pos = entity.getX() + radius * vX;
                  y_pos = entity.getY() + radius * vY;
                  z_pos = entity.getZ() + radius * vZ;
                  i++;
                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, x_pos, y_pos + 1.8, z_pos, 15, 2.0, 3.0, 2.0, 0.0);
                  }

                  Vec3 _center = new Vec3(x_pos, y_pos + 1.8, z_pos);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entity != entityiterator && !(entityiterator instanceof ExperienceOrb) && !(entityiterator instanceof ItemEntity)) {
                        entityiterator.hurt(
                           new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity),
                           18.0F
                        );
                     }
                  }
               }
            }

            if (entity.getPersistentData().getDouble("IA") == 150.0) {
               entity.getPersistentData().putString("state", "");
               entity.getPersistentData().putDouble("IA", 0.0);
            }
         }
      }
   }
}
