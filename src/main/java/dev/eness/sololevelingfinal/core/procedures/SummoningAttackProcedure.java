package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.SkeletonSummonerEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class SummoningAttackProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         double attack_duration = 0.0;
         double rx = 0.0;
         double ry = 0.0;
         double rz = 0.0;
         if (entity.isAlive()) {
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3, 100, false, false));
            }

            if (entity instanceof SkeletonSummonerEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(
                     SkeletonSummonerEntity.DATA_AttackDuration,
                     (entity instanceof SkeletonSummonerEntity _datEntI ? _datEntI.getEntityData().get(SkeletonSummonerEntity.DATA_AttackDuration) : 0) + 1
                  );
            }

            attack_duration = entity instanceof SkeletonSummonerEntity _datEntI
               ? _datEntI.getEntityData().get(SkeletonSummonerEntity.DATA_AttackDuration).intValue()
               : 0.0;
            if (attack_duration == 1.0 && entity instanceof SkeletonSummonerEntity) {
               ((SkeletonSummonerEntity)entity).setAnimation("lantern_summoning");
            }

            if (attack_duration >= 26.0 && attack_duration <= 80.0) {
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY(), entity.getZ(), 5, 0.5, 2.0, 0.5, 0.01);
               }

               if (world.dayTime() % 20L == 0L) {
                  rx = entity.getX() + Mth.nextDouble(RandomSource.create(), -6.0, 6.0);
                  ry = entity.getY();
                  rz = entity.getZ() + Mth.nextDouble(RandomSource.create(), -6.0, 6.0);
                  if (world instanceof ServerLevel _level) {
                     Entity _entityToSpawn = SololevelingModEntities.MAGICAL_SKULL.get().create(_level);
                     _entityToSpawn.moveTo(rx, ry + 3.0, rz, world.getRandom().nextFloat() * 360.0F, 0.0F);
                     if (_entityToSpawn instanceof Mob _mobToSpawn) {
                        _mobToSpawn.finalizeSpawn(_level, _level.getCurrentDifficultyAt(_entityToSpawn.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                     }

                     if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null
                        && _entityToSpawn instanceof Mob _entity
                        && (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) instanceof LivingEntity _ent) {
                        _entity.setTarget(_ent);
                     }

                     _level.addFreshEntity(_entityToSpawn);
                  }

                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, rx, ry, rz, 10, 0.3, 0.75, 0.3, 0.01);
                  }
               }
            }

            if (attack_duration >= 104.0) {
               if (entity instanceof SkeletonSummonerEntity _datEntSetS) {
                  _datEntSetS.getEntityData().set(SkeletonSummonerEntity.DATA_State, "TARGETING");
               }

               if (entity instanceof SkeletonSummonerEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(SkeletonSummonerEntity.DATA_SummoningCooldown, 100);
               }
            }
         }
      }
   }
}
