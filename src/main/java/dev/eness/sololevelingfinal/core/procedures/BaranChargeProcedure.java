package dev.eness.sololevelingfinal.core.procedures;

import java.util.HashSet;
import java.util.Set;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.KaiselinEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;
import dev.eness.sololevelingfinal.core.util.DemonCastleBossDamageRules;

public class BaranChargeProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null && entity instanceof BaranEntity baran) {
         if (baran.getState().equals("charge")) {
            LivingEntity target = entity instanceof Mob mob ? mob.getTarget() : null;
            if (target == null) {
               resetToIdle(baran);
            } else {
               double MF = baran.getPersistentData().getDouble("MF");
               boolean phase2 = baran.getPersistentData().getBoolean("baran_phase2");
               if (MF == 1.0) {
                  baran.animationprocedure = "attack";
                  int speedLevel = phase2 ? 2 : 1;
                  baran.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, speedLevel, false, false));
                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.CRIT, x, y + 1.0, z, 15, 0.3, 0.3, 0.3, 0.3);
                     sl.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ravager.step")),
                        SoundSource.HOSTILE,
                        1.5F,
                        0.6F
                     );
                  }
               }

               if (MF >= 5.0 && MF <= 18.0) {
                  Vec3 dir = target.position().subtract(baran.position()).normalize();
                  double speed = phase2 ? 1.6 : 1.2;
                  baran.setDeltaMovement(dir.x * speed, baran.getDeltaMovement().y, dir.z * speed);
                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5, z, 3, 0.2, 0.2, 0.2, 0.02);
                     sl.sendParticles(ParticleTypes.CRIT, x, y + 0.5, z, 2, 0.2, 0.2, 0.2, 0.1);
                  }

                  if (world instanceof ServerLevel sl) {
                     float collisionDmg = DemonCastleBossDamageRules.baranChargeCollision(phase2);
                     DamageSource src = new DamageSource(
                        world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), baran
                     );

                     for (LivingEntity nearby : sl.getEntitiesOfClass(
                        LivingEntity.class, baran.getBoundingBox().inflate(1.5), e -> e != baran && !isWildKaiselin(e)
                     )) {
                        String hitKey = "baran_charge_hit_" + nearby.getUUID();
                        if (!baran.getPersistentData().getBoolean(hitKey)) {
                           nearby.hurt(src, collisionDmg);
                           baran.getPersistentData().putBoolean(hitKey, true);
                        }
                     }
                  }
               }

               if (MF == 18.0) {
                  if (CombatRangeHelper.withinSurfaceRange(baran, target, 6.0)) {
                     DamageSource src = new DamageSource(
                        world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), baran
                     );
                     target.hurt(src, DemonCastleBossDamageRules.baranChargeImpact(phase2));
                     Vec3 dir = target.position().subtract(baran.position()).normalize();
                     target.setDeltaMovement(dir.x * 1.5, 0.5, dir.z * 1.5);
                     target.hurtMarked = true;
                  }

                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5, z, 3, 0.3, 0.3, 0.3, 0.0);
                     sl.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                        SoundSource.HOSTILE,
                        1.0F,
                        1.2F
                     );
                  }

                  clearChargeHitFlags(baran);
               }

               if (MF >= 40.0) {
                  baran.removeEffect(MobEffects.MOVEMENT_SPEED);
                  clearChargeHitFlags(baran);
                  resetToIdle(baran);
               }
            }
         }
      }
   }

   private static void clearChargeHitFlags(BaranEntity baran) {
      Set<String> toRemove = new HashSet<>();

      for (String key : baran.getPersistentData().getAllKeys()) {
         if (key.startsWith("baran_charge_hit_")) {
            toRemove.add(key);
         }
      }

      toRemove.forEach(baran.getPersistentData()::remove);
   }

   private static boolean isWildKaiselin(LivingEntity entity) {
      return entity instanceof KaiselinEntity && entity.getType() == SololevelingModEntities.KAISELIN.get();
   }

   private static void resetToIdle(BaranEntity baran) {
      baran.setState("idle");
      baran.getPersistentData().putDouble("MF", 0.0);
   }
}
