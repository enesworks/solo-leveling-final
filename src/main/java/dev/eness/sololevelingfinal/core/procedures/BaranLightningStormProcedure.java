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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;

public class BaranLightningStormProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null && entity instanceof BaranEntity baran) {
         if (baran.getState().equals("lightning_storm")) {
            LivingEntity target = entity instanceof Mob mob ? mob.getTarget() : null;
            if (target == null) {
               resetToIdle(baran);
            } else {
               double MF = baran.getPersistentData().getDouble("MF");
               boolean phase2 = baran.getPersistentData().getBoolean("baran_phase2");
               if (MF == 1.0) {
                  baran.animationprocedure = "attack";
                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 2.0, target.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                     sl.playSound(
                        (Player)null,
                        BlockPos.containing(target.getX(), target.getY(), target.getZ()),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.lightning_bolt.thunder")),
                        SoundSource.HOSTILE,
                        1.2F,
                        1.5F
                     );
                  }
               }

               if (MF == 10.0) {
                  spawnLightningCluster(world, baran, target, 3, 6.0);
               }

               if (MF == 20.0) {
                  spawnLightningCluster(world, baran, target, 3, 4.0);
               }

               if (MF == 30.0) {
                  int count = phase2 ? 6 : 4;
                  spawnLightningRing(world, baran, target, count, 5.0);
               }

               if (MF == 40.0 && world instanceof ServerLevel sl) {
                  spawnLightningAt(sl, target.getX(), target.getY(), target.getZ(), false);
                  target.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), baran), 5.0F
                  );
               }

               if (phase2 && MF == 50.0) {
                  spawnLightningCluster(world, baran, target, 5, 7.0);
               }

               int resetAt = phase2 ? 80 : 65;
               if (MF >= resetAt) {
                  resetToIdle(baran);
               }
            }
         }
      }
   }

   private static void spawnLightningCluster(LevelAccessor world, BaranEntity baran, LivingEntity target, int count, double radius) {
      if (world instanceof ServerLevel sl) {
         double tx = target.getX();
         double ty = target.getY();
         double tz = target.getZ();

         for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2.0;
            double dist = 1.0 + Math.random() * radius;
            double lx = tx + Math.cos(angle) * dist;
            double lz = tz + Math.sin(angle) * dist;
            spawnLightningAt(sl, lx, ty, lz, false);
         }
      }
   }

   private static void spawnLightningRing(LevelAccessor world, BaranEntity baran, LivingEntity target, int count, double radius) {
      if (world instanceof ServerLevel sl) {
         double tx = target.getX();
         double ty = target.getY();
         double tz = target.getZ();

         for (int i = 0; i < count; i++) {
            double angle = (double)i / count * Math.PI * 2.0;
            double lx = tx + Math.cos(angle) * radius;
            double lz = tz + Math.sin(angle) * radius;
            spawnLightningAt(sl, lx, ty, lz, false);
         }
      }
   }

   private static void spawnLightningAt(ServerLevel sl, double lx, double ly, double lz, boolean visualOnly) {
      LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
      if (bolt != null) {
         bolt.moveTo(lx, ly, lz);
         bolt.setVisualOnly(visualOnly);
         sl.addFreshEntity(bolt);
      }
   }

   private static void resetToIdle(BaranEntity baran) {
      baran.setState("idle");
      baran.getPersistentData().putDouble("MF", 0.0);
   }
}
