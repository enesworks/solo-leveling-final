package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.KaiselinEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;
import dev.eness.sololevelingfinal.core.util.DemonCastleBossDamageRules;

public class BaranMagicBlastProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null && entity instanceof BaranEntity baran) {
         if (baran.getState().equals("magic_blast")) {
            LivingEntity target = entity instanceof Mob mob ? mob.getTarget() : null;
            if (target == null) {
               resetToIdle(baran);
            } else {
               double MF = baran.getPersistentData().getDouble("MF");
               boolean phase2 = baran.getPersistentData().getBoolean("baran_phase2");
               if (MF == 1.0) {
                  baran.animationprocedure = "attack";
                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.ENCHANT, x, y + 1.5, z, 30, 1.0, 1.0, 1.0, 0.3);
                  }
               }

               if (MF == 15.0 && world instanceof ServerLevel sl) {
                  sl.sendParticles(ParticleTypes.WITCH, x, y + 1.5, z, 40, 0.5, 0.5, 0.5, 0.1);
                  sl.sendParticles(ParticleTypes.PORTAL, x, y + 1.5, z, 20, 0.8, 0.8, 0.8, 0.2);
               }

               if (phase2 && MF == 20.0) {
                  double tx = target.getX();
                  double ty = target.getY();
                  double tz = target.getZ();
                  target.hurt(magicDamage(world, baran), 8.0F);
                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.DRAGON_BREATH, tx, ty + 0.5, tz, 15, 0.3, 0.3, 0.3, 0.05);
                  }
               }

               if (MF == 25.0) {
                  double tx = target.getX();
                  double ty = target.getY();
                  double tz = target.getZ();
                  target.hurt(magicDamage(world, baran), DemonCastleBossDamageRules.baranMagicBlast(phase2));
                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.DRAGON_BREATH, tx, ty + 1.0, tz, 50, 0.8, 0.8, 0.8, 0.1);
                     sl.sendParticles(ParticleTypes.FLASH, tx, ty + 1.0, tz, 3, 0.2, 0.2, 0.2, 0.0);
                     sl.playSound(
                        (Player)null,
                        BlockPos.containing(tx, ty, tz),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                        SoundSource.HOSTILE,
                        1.5F,
                        0.6F
                     );
                  }
               }

               if (MF == 35.0) {
                  double tx = target.getX();
                  double ty = target.getY();
                  double tz = target.getZ();
                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.LARGE_SMOKE, tx, ty + 0.5, tz, 30, 2.5, 0.5, 2.5, 0.0);

                     for (LivingEntity nearby : sl.getEntitiesOfClass(
                        LivingEntity.class,
                        target.getBoundingBox().inflate(5.0),
                        e -> e != baran && !isWildKaiselin(e) && CombatRangeHelper.withinSurfaceRange(e, target, 5.0)
                     )) {
                        nearby.hurt(magicDamage(world, baran), 8.0F);
                     }

                     sl.playSound(
                        (Player)null,
                        BlockPos.containing(tx, ty, tz),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")),
                        SoundSource.HOSTILE,
                        1.5F,
                        1.2F
                     );
                  }
               }

               if (MF >= 55.0) {
                  resetToIdle(baran);
               }
            }
         }
      }
   }

   private static DamageSource magicDamage(LevelAccessor world, BaranEntity baran) {
      return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), baran);
   }

   private static boolean isWildKaiselin(LivingEntity entity) {
      return entity instanceof KaiselinEntity && entity.getType() == SololevelingModEntities.KAISELIN.get();
   }

   private static void resetToIdle(BaranEntity baran) {
      baran.setState("idle");
      baran.getPersistentData().putDouble("MF", 0.0);
   }
}
