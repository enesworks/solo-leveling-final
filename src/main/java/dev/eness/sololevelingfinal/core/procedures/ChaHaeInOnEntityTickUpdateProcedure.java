package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.ChaHaeInEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class ChaHaeInOnEntityTickUpdateProcedure {
   private static final int SWORD_DANCE_CYCLE = 160;
   private static final int OVERHEAD_CYCLE = 120;
   private static final int DASH_CHARGE_TICKS = 28;

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity instanceof ChaHaeInEntity cha) {
         LivingEntity target = cha.getTarget();
         if (target != null && target.isAlive()) {
            faceTarget(cha, target);
            double surfaceDistance = CombatRangeHelper.surfaceDistance(cha, target);
            tickSwordDance(cha);
            tickOverhead(world, cha, target, surfaceDistance);
            tickDash(cha, target, surfaceDistance);
         }
      }
   }

   private static void tickSwordDance(ChaHaeInEntity cha) {
      int timer = cha.getEntityData().get(ChaHaeInEntity.DATA_IA) + 1;
      if (timer == 80 && !cha.level().isClientSide()) {
         cha.addEffect(new MobEffectInstance(SololevelingModMobEffects.SWORD_DANCE.get(), 50, 2, false, false));
      }

      cha.getEntityData().set(ChaHaeInEntity.DATA_IA, timer >= 160 ? 0 : timer);
   }

   private static void tickOverhead(LevelAccessor world, ChaHaeInEntity cha, LivingEntity target, double surfaceDistance) {
      int timer = cha.getEntityData().get(ChaHaeInEntity.DATA_OverheadTimer) + 1;
      if (timer == 105) {
         cha.setAnimation("overhead");
      }

      if (timer >= 120) {
         if (surfaceDistance <= 3.25 && !cha.level().isClientSide()) {
            target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), cha), 16.0F);
         }

         timer = 0;
      }

      cha.getEntityData().set(ChaHaeInEntity.DATA_OverheadTimer, timer);
   }

   private static void tickDash(ChaHaeInEntity cha, LivingEntity target, double surfaceDistance) {
      if (!(surfaceDistance < 7.0) && !(surfaceDistance > 22.0) && cha.hasLineOfSight(target)) {
         int timer = cha.getEntityData().get(ChaHaeInEntity.DATA_DashTimer) + 1;
         if (timer >= 28) {
            Vec3 direction = target.getBoundingBox().getCenter().subtract(cha.getBoundingBox().getCenter());
            direction = new Vec3(direction.x, 0.0, direction.z);
            if (direction.lengthSqr() > 1.0E-5) {
               direction = direction.normalize();
               cha.setDeltaMovement(direction.x * 0.9, 0.1, direction.z * 0.9);
               cha.hasImpulse = true;
            }

            timer = 0;
         }

         cha.getEntityData().set(ChaHaeInEntity.DATA_DashTimer, timer);
      } else {
         cha.getEntityData().set(ChaHaeInEntity.DATA_DashTimer, 0);
      }
   }

   private static void faceTarget(Mob mob, LivingEntity target) {
      double deltaX = target.getX() - mob.getX();
      double deltaZ = target.getZ() - mob.getZ();
      float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
      mob.setYRot(yaw);
      mob.yRotO = yaw;
      mob.yBodyRot = yaw;
      mob.yHeadRot = yaw;
      mob.getLookControl().setLookAt(target, 35.0F, 35.0F);
   }
}
