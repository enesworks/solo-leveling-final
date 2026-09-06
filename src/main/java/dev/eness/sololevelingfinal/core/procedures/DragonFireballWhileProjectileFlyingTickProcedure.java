package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DragonFireballWhileProjectileFlyingTickProcedure {
   private static final double BASE_SPEED = 0.85;
   private static final double HOMING_STRENGTH = 1.0;
   private static final double MAX_LIFETIME = 80.0;
   private static final int HOMING_DELAY = 5;

   public static void execute(LevelAccessor world, double x, double y, double z, Entity projectile) {
      if (projectile != null) {
         if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 20, 0.5, 0.5, 0.5, 0.0);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 5, 0.5, 0.5, 0.5, 0.0);
         }

         projectile.setNoGravity(true);
         projectile.getPersistentData().putDouble("life", projectile.getPersistentData().getDouble("life") + 1.0);
         double life = projectile.getPersistentData().getDouble("life");
         if (life >= 5.0) {
            Vec3 projectilePos = projectile.position();
            Entity shooter = null;
            Entity owner = null;
            if (projectile instanceof Projectile proj) {
               shooter = proj.getOwner();
            }

            if (shooter instanceof OwnableEntity ownable) {
               owner = ownable.getOwner();
            }

            Entity target = findTargetWithPriority(world, projectilePos, 60.0, shooter, owner);
            if (target != null) {
               Vec3 targetPos = target.position().add(0.0, 1.0, 0.0);
               Vec3 direction = targetPos.subtract(projectilePos).normalize();
               Vec3 currentVelocity = projectile.getDeltaMovement();
               Vec3 newVelocity = currentVelocity.lerp(direction.scale(0.85), 1.0);
               projectile.setDeltaMovement(newVelocity);
            }
         }

         if (life >= 80.0 && !projectile.level().isClientSide()) {
            projectile.discard();
         }
      }
   }

   private static Entity findTargetWithPriority(LevelAccessor world, Vec3 pos, double range, Entity shooter, Entity owner) {
      if (world instanceof ServerLevel serverLevel) {
         if (owner instanceof Mob ownerMob && ownerMob.getTarget() != null) {
            Entity ownerTarget = ownerMob.getTarget();
            if (ownerTarget.isAlive() && ownerTarget.distanceToSqr(pos) <= range * range) {
               return ownerTarget;
            }
         }

         if (shooter instanceof Mob shooterMob && shooter != owner && shooterMob.getTarget() != null) {
            Entity shooterTarget = shooterMob.getTarget();
            if (shooterTarget.isAlive() && shooterTarget.distanceToSqr(pos) <= range * range) {
               return shooterTarget;
            }
         }

         return serverLevel.getEntitiesOfClass(Entity.class, new AABB(pos, pos).inflate(range), entity -> {
            if (entity instanceof Projectile) {
               return false;
            } else if (shooter != null && entity.equals(shooter)) {
               return false;
            } else {
               return owner != null && entity.equals(owner) ? false : entity.isAlive();
            }
         }).stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(pos))).orElse(null);
      } else {
         return null;
      }
   }
}
