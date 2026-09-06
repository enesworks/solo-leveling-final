package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.IceElfEntity;
import dev.eness.sololevelingfinal.core.entity.ManaArrowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class IceElfOnEntityTickUpdateProcedure {
   private static final double ARROW_BASE_DAMAGE = 1.5;
   private static final float ARROW_SPEED = 2.35F;

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!world.isClientSide() && world.getLevelData().getGameTime() % 5L == 0L) {
            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
               if (entity instanceof IceElfEntity _datEntSetI) {
                  _datEntSetI.getEntityData()
                     .set(IceElfEntity.DATA_AI, (entity instanceof IceElfEntity _datEntI ? _datEntI.getEntityData().get(IceElfEntity.DATA_AI) : 0) + 1);
               }

               if (entity instanceof IceElfEntity _datEntL6 && _datEntL6.getEntityData().get(IceElfEntity.DATA_canshoot)) {
                  if (entity instanceof IceElfEntity _datEntSetI) {
                     _datEntSetI.getEntityData()
                        .set(IceElfEntity.DATA_MF, (entity instanceof IceElfEntity _datEntI ? _datEntI.getEntityData().get(IceElfEntity.DATA_MF) : 0) + 1);
                  }

                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999, 90, false, false));
                  }
               } else {
                  if (entity instanceof IceElfEntity _datEntSetI) {
                     _datEntSetI.getEntityData().set(IceElfEntity.DATA_MF, 0);
                  }

                  if (entity instanceof LivingEntity _entity) {
                     _entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                  }
               }

               entity.lookAt(
                  Anchor.EYES,
                  new Vec3(
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + 1.2,
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
                  )
               );
               Entity target = entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null;
               CombatRangeHelper.maintainRangedBand(entity, target, 6.0, 16.0, 1.0);
               if (CombatRangeHelper.withinSurfaceRange(entity, target, 20.0)
                  && entity instanceof Mob mob
                  && target instanceof LivingEntity livingTarget
                  && mob.getSensing().hasLineOfSight(livingTarget)) {
                  if (entity instanceof IceElfEntity _datEntSetL) {
                     _datEntSetL.getEntityData().set(IceElfEntity.DATA_canshoot, true);
                  }
               } else if (entity instanceof IceElfEntity _datEntSetL) {
                  _datEntSetL.getEntityData().set(IceElfEntity.DATA_canshoot, false);
               }
            } else {
               if (entity instanceof IceElfEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(IceElfEntity.DATA_MF, 0);
               }

               if (entity instanceof IceElfEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(IceElfEntity.DATA_AI, 0);
               }
            }

            if ((entity instanceof IceElfEntity _datEntI ? _datEntI.getEntityData().get(IceElfEntity.DATA_MF) : 0) == 2) {
               if (entity instanceof IceElfEntity) {
                  ((IceElfEntity)entity).setAnimation("empty");
               }

               if (entity instanceof IceElfEntity) {
                  ((IceElfEntity)entity).setAnimation("attack.bow");
               }
            }

            if ((entity instanceof IceElfEntity _datEntI ? _datEntI.getEntityData().get(IceElfEntity.DATA_MF) : 0) == 5) {
               Entity _shootFrom = entity;
               Level projectileLevel = _shootFrom.level();
               if (!projectileLevel.isClientSide()) {
                  Projectile _entityToSpawn = (new Object() {
                     public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new ManaArrowEntity(SololevelingModEntities.MANA_ARROW.get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        entityToSpawn.setKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        entityToSpawn.setCritArrow(false);
                        return entityToSpawn;
                     }
                  }).getArrow(projectileLevel, entity, 1.5F, 0);
                  _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
                  _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 2.35F, 5.0F);
                  projectileLevel.addFreshEntity(_entityToSpawn);
               }
            }

            if ((entity instanceof IceElfEntity _datEntI ? _datEntI.getEntityData().get(IceElfEntity.DATA_MF) : 0) == 11
               && entity instanceof IceElfEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(IceElfEntity.DATA_MF, 0);
            }

            if ((entity instanceof IceElfEntity _datEntI ? _datEntI.getEntityData().get(IceElfEntity.DATA_AI) : 0) == 12) {
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 120, 3.0, 1.8, 60.0, 0.0);
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.snow.step")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.snow.step")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }

               IceElfTeleportProcedure.execute(world, entity);
            }

            if ((entity instanceof IceElfEntity _datEntI ? _datEntI.getEntityData().get(IceElfEntity.DATA_AI) : 0) == 24) {
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 120, 3.0, 1.8, 60.0, 0.0);
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.snow.step")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.snow.step")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }

               IceElfTeleportProcedure.execute(world, entity);
            }

            if ((entity instanceof IceElfEntity _datEntI ? _datEntI.getEntityData().get(IceElfEntity.DATA_AI) : 0) == 64
               && entity instanceof IceElfEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(IceElfEntity.DATA_AI, 0);
            }
         }
      }
   }
}
