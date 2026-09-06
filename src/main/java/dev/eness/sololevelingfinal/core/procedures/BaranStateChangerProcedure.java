package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class BaranStateChangerProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null && entity instanceof BaranEntity baran) {
         LivingEntity target = entity instanceof Mob mob ? mob.getTarget() : null;
         if (target != null) {
            boolean phase2 = baran.getPersistentData().getBoolean("baran_phase2");
            double surfaceDistance = CombatRangeHelper.surfaceDistance(baran, target);
            double distSq = surfaceDistance * surfaceDistance;
            boolean closeRange = distSq <= 36.0;
            int rand = Mth.nextInt(RandomSource.create(), 1, phase2 ? 12 : 10);
            String newState;
            if (closeRange) {
               if (rand <= 2) {
                  newState = "ground_slam";
               } else if (rand <= 4) {
                  newState = "charge";
               } else if (rand <= 6) {
                  newState = "magic_blast";
               } else if (rand <= 8) {
                  newState = "lightning_storm";
               } else {
                  newState = "summon";
               }
            } else if (rand <= 3) {
               newState = "magic_blast";
            } else if (rand <= 6) {
               newState = "lightning_storm";
            } else if (rand <= 8) {
               newState = "summon";
            } else if (rand <= 10) {
               newState = "charge";
            } else {
               newState = "lightning_storm";
            }

            if (!baran.level().isClientSide()) {
               baran.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 2, false, false));
            }

            baran.setState(newState);
            baran.getPersistentData().putDouble("MF", 0.0);
         }
      }
   }
}
