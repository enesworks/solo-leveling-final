package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class IgrisStateChangerProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         Mob mob = entity instanceof Mob m ? m : null;
         if (mob != null && mob.getTarget() != null) {
            LivingEntity target = mob.getTarget();
            boolean enraged = entity.getPersistentData().getBoolean("enraged");
            RandomSource rng = entity.level().getRandom();
            double dist = CombatRangeHelper.surfaceDistance(entity, target);
            if (dist > 5.5) {
               entity.getPersistentData().putString("state", "idle");
               entity.getPersistentData().putDouble("MF", 0.0);
               entity.getPersistentData().putDouble("nextAttackMF", 10.0);
            } else {
               if (dist > 3.5 && !entity.level().isClientSide()) {
                  Vec3 dir = target.position().subtract(entity.position()).normalize();
                  entity.setDeltaMovement(dir.x * 0.8, 0.1, dir.z * 0.8);
               }

               int roll = rng.nextInt(100);
               String nextState;
               int slowdownTicks;
               if (!enraged) {
                  if (roll < 30) {
                     nextState = "spin";
                     slowdownTicks = 40;
                  } else if (roll < 65) {
                     nextState = "stab";
                     slowdownTicks = 40;
                  } else if (roll < 90) {
                     nextState = "slam";
                     slowdownTicks = 40;
                  } else {
                     nextState = "scream";
                     slowdownTicks = 30;
                  }
               } else if (roll < 28) {
                  nextState = "spin";
                  slowdownTicks = 35;
               } else if (roll < 55) {
                  nextState = "stab";
                  slowdownTicks = 35;
               } else if (roll < 75) {
                  nextState = "slam";
                  slowdownTicks = 35;
               } else {
                  nextState = "scream";
                  slowdownTicks = 25;
               }

               if (entity instanceof LivingEntity le && !le.level().isClientSide()) {
                  le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowdownTicks, 10));
               }

               int nextAttackMF = enraged ? 10 + rng.nextInt(11) : 20 + rng.nextInt(16);
               entity.getPersistentData().putString("state", nextState);
               entity.getPersistentData().putDouble("MF", 0.0);
               entity.getPersistentData().putDouble("nextAttackMF", nextAttackMF);
            }
         }
      }
   }
}
