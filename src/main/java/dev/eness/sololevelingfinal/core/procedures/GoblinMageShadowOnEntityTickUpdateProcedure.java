package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.GoblinMageShadowEntity;
import dev.eness.sololevelingfinal.core.entity.ShamanMagicEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class GoblinMageShadowOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!ShadowMonarchManager.handleUnavailableShadowOwner(entity)) {
            double hei = 0.0;
            double delay = 0.0;
            if ((entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null) != null) {
               if (!(entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null).isAlive()) {
                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY(), entity.getZ(), 30, 0.05, 0.05, 0.05, 1.0);
                  }

                  if (!entity.level().isClientSide()) {
                     ShadowMonarchManager.dropStoredShadowInventory(entity);
                     entity.discard();
                  }
               }

               if (!(entity instanceof TamableAnimal _tamEnt && _tamEnt.isTame())
                  && entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
                  && !entity.level().isClientSide()) {
                  ShadowMonarchManager.dropStoredShadowInventory(entity);
                  entity.discard();
               }

               hei = entity.getBbHeight();
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(
                     SololevelingModParticleTypes.MANA_BLUE.get(),
                     entity.getX(),
                     entity.getY() + hei / 2.0,
                     entity.getZ(),
                     2,
                     entity.getBbWidth() * 0.5,
                     hei / 2.0,
                     entity.getBbWidth() * 0.5,
                     0.05
                  );
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(
                     ParticleTypes.SMOKE,
                     entity.getX(),
                     entity.getY() + hei / 2.0,
                     entity.getZ(),
                     2,
                     entity.getBbWidth() * 0.75,
                     hei / 2.0,
                     entity.getBbWidth() * 0.75,
                     0.05
                  );
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(
                     ParticleTypes.LARGE_SMOKE,
                     entity.getX(),
                     entity.getY() + hei / 2.0,
                     entity.getZ(),
                     2,
                     entity.getBbWidth() * 0.75,
                     hei / 2.0,
                     entity.getBbWidth() * 0.75,
                     0.05
                  );
               }

               if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) > 0.05) {
                  if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
                     entity.getPersistentData().putDouble("AL", entity.getPersistentData().getDouble("AL") + 1.0);
                     if (entity.getPersistentData().getBoolean("CanShoot")) {
                        entity.getPersistentData().putDouble("MF", entity.getPersistentData().getDouble("MF") + 1.0);
                        if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                           _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999, 90, false, false));
                        }
                     } else {
                        entity.getPersistentData().putDouble("MF", 0.0);
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
                     CombatRangeHelper.maintainRangedBand(entity, target, 5.0, 16.0, 1.0);
                     if (CombatRangeHelper.withinSurfaceRange(entity, target, 19.0)
                        && entity instanceof Mob mob
                        && target instanceof LivingEntity livingTarget
                        && mob.getSensing().hasLineOfSight(livingTarget)) {
                        entity.getPersistentData().putBoolean("CanShoot", true);
                     } else {
                        entity.getPersistentData().putBoolean("CanShoot", false);
                     }
                  } else {
                     entity.getPersistentData().putDouble("MF", 0.0);
                  }

                  if (entity.getPersistentData().getDouble("MF") == 5.0 && entity instanceof GoblinMageShadowEntity) {
                     ((GoblinMageShadowEntity)entity).setAnimation("attack");
                  }

                  if (entity.getPersistentData().getDouble("MF") == 13.0) {
                     Entity _shootFrom = entity;
                     Level projectileLevel = _shootFrom.level();
                     if (!projectileLevel.isClientSide()) {
                        Projectile _entityToSpawn = (new Object() {
                           public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                              AbstractArrow entityToSpawn = new ShamanMagicEntity(SololevelingModEntities.SHAMAN_MAGIC.get(), level);
                              entityToSpawn.setOwner(shooter);
                              entityToSpawn.setBaseDamage(damage);
                              entityToSpawn.setKnockback(knockback);
                              entityToSpawn.setSilent(true);
                              return entityToSpawn;
                           }
                        }).getArrow(projectileLevel, entity, 5.0F, 0);
                        _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
                        _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 0.25F, 0.0F);
                        projectileLevel.addFreshEntity(_entityToSpawn);
                     }

                     if (world instanceof Level _level) {
                        if (!_level.isClientSide()) {
                           _level.playSound(
                              (Player)null,
                              BlockPos.containing(x, y, z),
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              0.5F
                           );
                        } else {
                           _level.playLocalSound(
                              x,
                              y,
                              z,
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              0.5F,
                              false
                           );
                        }
                     }
                  }

                  if (entity.getPersistentData().getDouble("MF") == 60.0) {
                     entity.getPersistentData().putDouble("MF", 0.0);
                  }
               }
            }
         }
      }
   }
}
