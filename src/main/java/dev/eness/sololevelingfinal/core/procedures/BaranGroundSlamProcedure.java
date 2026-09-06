package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import dev.eness.sololevelingfinal.core.util.DemonCastleBossDamageRules;

public class BaranGroundSlamProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null && entity instanceof BaranEntity baran) {
         if (baran.getState().equals("ground_slam")) {
            LivingEntity target = entity instanceof Mob mob ? mob.getTarget() : null;
            if (target == null) {
               resetToIdle(baran);
            } else {
               double MF = baran.getPersistentData().getDouble("MF");
               boolean phase2 = baran.getPersistentData().getBoolean("baran_phase2");
               if (MF == 1.0) {
                  baran.animationprocedure = "attack";
                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.CRIT, x, y + 0.1, z, 20, 2.0, 0.1, 2.0, 0.1);
                     sl.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ravager.roar")),
                        SoundSource.HOSTILE,
                        1.5F,
                        0.8F
                     );
                  }
               }

               if (MF == 12.0) {
                  dealShockwave(world, baran, x, y, z, 7.0, DemonCastleBossDamageRules.baranGroundSlam(phase2), true, ParticleTypes.EXPLOSION);
                  if (world instanceof ServerLevel sl) {
                     sl.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                        SoundSource.HOSTILE,
                        2.0F,
                        0.5F
                     );
                  }
               }

               if (MF == 22.0) {
                  dealShockwave(world, baran, x, y, z, 12.0, DemonCastleBossDamageRules.baranGroundRipple(phase2), false, ParticleTypes.LARGE_SMOKE);
                  if (world instanceof ServerLevel sl) {
                     sl.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.stone.break")),
                        SoundSource.HOSTILE,
                        2.0F,
                        0.4F
                     );
                  }
               }

               if (phase2 && MF == 32.0) {
                  dealShockwave(world, baran, x, y, z, 16.0, 6.0F, false, ParticleTypes.CAMPFIRE_COSY_SMOKE);
               }

               int resetAt = phase2 ? 65 : 50;
               if (MF >= resetAt) {
                  resetToIdle(baran);
               }
            }
         }
      }
   }

   private static void dealShockwave(
      LevelAccessor world, BaranEntity baran, double x, double y, double z, double radius, float damage, boolean knockback, SimpleParticleType particle
   ) {
      if (world instanceof ServerLevel sl) {
         DamageSource src = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), baran);

         for (LivingEntity nearby : sl.getEntitiesOfClass(
            LivingEntity.class, baran.getBoundingBox().inflate(radius + 2.0), e -> e != baran && !isWildKaiselin(e) && e.distanceTo(baran) <= radius
         )) {
            nearby.hurt(src, damage);
            if (knockback) {
               Vec3 dir = nearby.position().subtract(baran.position()).normalize();
               nearby.setDeltaMovement(dir.x * 1.2, 0.6, dir.z * 1.2);
               nearby.hurtMarked = true;
            }

            sl.sendParticles(particle, nearby.getX(), nearby.getY() + 0.5, nearby.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
         }

         for (int i = 0; i < 16; i++) {
            double angle = i / 16.0 * Math.PI * 2.0;
            double px = x + Math.cos(angle) * (radius * 0.8);
            double pz = z + Math.sin(angle) * (radius * 0.8);
            sl.sendParticles(particle, px, y + 0.2, pz, 3, 0.2, 0.1, 0.2, 0.0);
         }
      }
   }

   private static boolean isWildKaiselin(LivingEntity entity) {
      return entity instanceof KaiselinEntity && entity.getType() == SololevelingModEntities.KAISELIN.get();
   }

   private static void resetToIdle(BaranEntity baran) {
      baran.setState("idle");
      baran.getPersistentData().putDouble("MF", 0.0);
   }
}
