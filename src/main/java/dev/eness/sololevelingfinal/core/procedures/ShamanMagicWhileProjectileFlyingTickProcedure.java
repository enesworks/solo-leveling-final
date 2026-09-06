package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class ShamanMagicWhileProjectileFlyingTickProcedure {
   private static final double HOMING_SPEED = 0.25;
   private static final double HOMING_TURN_WEIGHT = 0.35;
   private static final int HOMING_TICKS = 40;
   private static final int MAX_LIFETIME_TICKS = 100;

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity immediatesourceentity) {
      if (entity != null && immediatesourceentity != null) {
         immediatesourceentity.setNoGravity(true);
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(
               SololevelingModParticleTypes.SHAMAN_MAGIC_PARTICLE.get(),
               immediatesourceentity.getX(),
               immediatesourceentity.getY(),
               immediatesourceentity.getZ(),
               2,
               0.05,
               0.05,
               0.05,
               0.0
            );
         }

         immediatesourceentity.getPersistentData().putDouble("life", immediatesourceentity.getPersistentData().getDouble("life") + 1.0);
         LivingEntity target = entity instanceof Mob mob ? mob.getTarget() : null;
         if (immediatesourceentity.getPersistentData().getDouble("life") <= 40.0 && target != null && target.isAlive()) {
            Vec3 toTarget = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.65, target.getZ()).subtract(immediatesourceentity.position());
            if (toTarget.lengthSqr() > 1.0E-6) {
               Vec3 desiredVelocity = toTarget.normalize().scale(0.25);
               Vec3 currentVelocity = immediatesourceentity.getDeltaMovement();
               Vec3 steeredVelocity = currentVelocity.scale(0.65).add(desiredVelocity.scale(0.35));
               if (steeredVelocity.lengthSqr() > 1.0E-6) {
                  immediatesourceentity.setDeltaMovement(steeredVelocity.normalize().scale(0.25));
               }
            }
         }

         if (immediatesourceentity.getPersistentData().getDouble("life") >= 100.0 && !immediatesourceentity.level().isClientSide()) {
            immediatesourceentity.discard();
         }
      }
   }
}
