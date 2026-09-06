package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import dev.eness.sololevelingfinal.core.entity.SkeletonSummonerEntity;

public class RepulsionAttackProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         double attack_duration = 0.0;
         Entity target = null;
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

            target = entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null;
            attack_duration = entity instanceof SkeletonSummonerEntity _datEntI
               ? _datEntI.getEntityData().get(SkeletonSummonerEntity.DATA_AttackDuration).intValue()
               : 0.0;
            if (attack_duration == 1.0 && entity instanceof SkeletonSummonerEntity) {
               ((SkeletonSummonerEntity)entity).setAnimation("repulsion");
            }

            if (attack_duration == 25.0) {
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY(), entity.getZ(), 30, 0.5, 2.0, 0.5, 0.01);
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 3.0, target.getZ(), 30, 0.5, 2.0, 0.5, 0.01);
               }

               Entity _ent = entity;
               _ent.teleportTo(target.getX(), target.getY() + 3.0, target.getZ());
               if (_ent instanceof ServerPlayer _serverPlayer) {
                  _serverPlayer.connection.teleport(target.getX(), target.getY() + 3.0, target.getZ(), _ent.getYRot(), _ent.getXRot());
               }
            }

            if (attack_duration == 43.0) {
               entity.setDeltaMovement(new Vec3(0.0, -6.0, 0.0));
            }

            if (attack_duration == 45.0) {
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + 3.0, entity.getZ(), 5, 2.0, 0.0, 2.0, 0.01);
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F,
                        false
                     );
                  }
               }

               Vec3 _center = new Vec3(entity.getX(), entity.getY(), entity.getZ());

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator != entity) {
                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)),
                        entity instanceof SkeletonSummonerEntity _datEntI ? _datEntI.getEntityData().get(SkeletonSummonerEntity.DATA_DamageRepulsion) : 0
                     );
                  }
               }
            }

            if (attack_duration >= 104.0) {
               if (entity instanceof SkeletonSummonerEntity _datEntSetS) {
                  _datEntSetS.getEntityData().set(SkeletonSummonerEntity.DATA_State, "TARGETING");
               }

               if (entity instanceof SkeletonSummonerEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(SkeletonSummonerEntity.DATA_RepulsionCooldown, 80);
               }
            }
         }
      }
   }
}
