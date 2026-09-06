package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

public class RandomHunterHealerTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double Rank = 0.0;
         double rand = 0.0;
         double dmg_modifier = 0.0;
         if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("S")) {
            dmg_modifier = 20.0;
         } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("A")) {
            dmg_modifier = 14.0;
         } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("B")) {
            dmg_modifier = 10.0;
         } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("C")) {
            dmg_modifier = 6.0;
         } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("D")) {
            dmg_modifier = 5.0;
         }

         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            HunterAIHelper.casterBacklineTick(entity);
            HunterAIHelper.healerSupportTick(entity);
            entity.lookAt(
               Anchor.EYES,
               new Vec3(
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY()
                     + (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getBbHeight(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
               )
            );
            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Allies) : "")
                     .contains(entityiterator.getStringUUID())
                  && entity instanceof Mob _entity) {
                  _entity.getNavigation().moveTo(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 2.0);
               }
            }

            if ((entity instanceof HunterEntity _datEntI ? _datEntI.getEntityData().get(HunterEntity.DATA_IA) : 0) <= 70) {
               if (entity instanceof HunterEntity _datEntSetI) {
                  _datEntSetI.getEntityData()
                     .set(HunterEntity.DATA_IA, (entity instanceof HunterEntity _datEntI ? _datEntI.getEntityData().get(HunterEntity.DATA_IA) : 0) + 1);
               }

               if ((entity instanceof HunterEntity _datEntI ? _datEntI.getEntityData().get(HunterEntity.DATA_IA) : 0) == 15) {
                  Vec3 _centerx = new Vec3(x, y, z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_centerx, _centerx).inflate(16.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Allies) : "")
                        .contains(entityiterator.getStringUUID())) {
                        entity.lookAt(Anchor.EYES, new Vec3(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()));
                        if ((entityiterator instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)
                              / (entityiterator instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F)
                           < 0.2) {
                           if (entityiterator instanceof LivingEntity _ally && !_ally.level().isClientSide()) {
                              _ally.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 2));
                           }

                           if (world instanceof ServerLevel _level) {
                              _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, entity.getX(), entity.getY(), entity.getZ(), 15, 1.0, 3.0, 1.0, 1.0);
                           }
                        } else if ((entityiterator instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)
                              / (entityiterator instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F)
                           < 0.6) {
                           if (entityiterator instanceof LivingEntity _ally && !_ally.level().isClientSide()) {
                              _ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 2));
                           }

                           if (world instanceof ServerLevel _level) {
                              _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, entity.getX(), entity.getY(), entity.getZ(), 15, 1.0, 3.0, 1.0, 1.0);
                           }
                        } else if ((entityiterator instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)
                              / (entityiterator instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F)
                           < 0.8) {
                           if (entityiterator instanceof LivingEntity _ally && !_ally.level().isClientSide()) {
                              _ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 1));
                           }

                           if (world instanceof ServerLevel _level) {
                              _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, entity.getX(), entity.getY(), entity.getZ(), 15, 1.0, 3.0, 1.0, 1.0);
                           }
                        } else if ((entityiterator instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)
                              / (entityiterator instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F)
                           < 1.0F) {
                           if (entityiterator instanceof LivingEntity _ally && !_ally.level().isClientSide()) {
                              _ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0));
                           }

                           if (world instanceof ServerLevel _level) {
                              _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, entity.getX(), entity.getY(), entity.getZ(), 15, 1.0, 3.0, 1.0, 1.0);
                           }
                        } else {
                           rand = Mth.nextInt(RandomSource.create(), 1, 2);
                           if (rand == 1.0) {
                              if (entityiterator instanceof LivingEntity _ally && !_ally.level().isClientSide()) {
                                 _ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 2));
                              }
                           } else if (rand == 2.0 && entityiterator instanceof LivingEntity _ally && !_ally.level().isClientSide()) {
                              _ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50, 2));
                           }

                           if (world instanceof ServerLevel _level) {
                              _level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY(), entity.getZ(), 15, 1.0, 3.0, 1.0, 1.0);
                           }
                        }
                     }
                  }
               }
            } else if (entity instanceof HunterEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(HunterEntity.DATA_IA, 0);
            }
         }
      }
   }
}
