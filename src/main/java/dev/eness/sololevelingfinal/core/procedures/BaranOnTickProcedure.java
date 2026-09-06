package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;

public class BaranOnTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null && entity instanceof BaranEntity baran) {
         LivingEntity target = entity instanceof Mob mob ? mob.getTarget() : null;
         if (target == null) {
            baran.getPersistentData().putDouble("MF", 0.0);
            baran.setState("idle");
         } else {
            if (!baran.getPersistentData().getBoolean("baran_phase2") && baran.getHealth() <= baran.getMaxHealth() * 0.5F) {
               baran.getPersistentData().putBoolean("baran_phase2", true);
               if (world instanceof ServerLevel serverLevel) {
                  for (int i = 0; i < 6; i++) {
                     double angle = i / 6.0 * Math.PI * 2.0;
                     double lx = x + Math.cos(angle) * 4.0;
                     double lz = z + Math.sin(angle) * 4.0;
                     LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                     if (bolt != null) {
                        bolt.moveTo(lx, y, lz);
                        bolt.setVisualOnly(true);
                        serverLevel.addFreshEntity(bolt);
                     }
                  }
               }

               baran.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.38);
            }

            double MF = baran.getPersistentData().getDouble("MF") + 1.0;
            baran.getPersistentData().putDouble("MF", MF);
            String state = baran.getState();
            boolean phase2 = baran.getPersistentData().getBoolean("baran_phase2");
            int idleThreshold = phase2 ? 6 : 10;
            if (state.equals("idle") && MF >= idleThreshold) {
               BaranStateChangerProcedure.execute(world, x, y, z, entity);
            } else {
               switch (state) {
                  case "magic_blast":
                     BaranMagicBlastProcedure.execute(world, x, y, z, entity);
                     break;
                  case "lightning_storm":
                     BaranLightningStormProcedure.execute(world, x, y, z, entity);
                     break;
                  case "summon":
                     BaranSummonProcedure.execute(world, x, y, z, entity);
                     break;
                  case "ground_slam":
                     BaranGroundSlamProcedure.execute(world, x, y, z, entity);
                     break;
                  case "charge":
                     BaranChargeProcedure.execute(world, x, y, z, entity);
               }
            }
         }
      }
   }
}
