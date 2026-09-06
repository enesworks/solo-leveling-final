package dev.eness.sololevelingfinal.core.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.MoveControl.Operation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;

public final class BeruFlightMoveControl extends MoveControl {
   private static final double ARRIVAL_DISTANCE_SQR = 0.2;
   private static final double MAX_FLIGHT_SPEED = 1.15;
   private final BeruShadowEntity beru;
   private boolean directVelocityThisTick;
   private int blockedTicks;

   public BeruFlightMoveControl(BeruShadowEntity beru) {
      super(beru);
      this.beru = beru;
   }

   @Override
   public void tick() {
      if (this.directVelocityThisTick) {
         this.directVelocityThisTick = false;
         Vec3 velocity = this.beru.getDeltaMovement();
         this.orientToVelocity(velocity);
         this.beru.setSpeed((float)velocity.length());
      } else if (this.operation != Operation.MOVE_TO) {
         this.brake();
      } else {
         this.operation = Operation.WAIT;
         Vec3 offset = new Vec3(this.wantedX - this.beru.getX(), this.wantedY - this.beru.getY(), this.wantedZ - this.beru.getZ());
         double distanceSqr = offset.lengthSqr();
         if (distanceSqr < 0.2) {
            this.blockedTicks = 0;
            this.brake();
         } else {
            double attributeSpeed = this.beru.getAttributeValue(Attributes.FLYING_SPEED);
            double requestedSpeed = Mth.clamp(this.speedModifier * attributeSpeed, 0.22, 1.15);
            double distance = Math.sqrt(distanceSqr);
            Vec3 desired = offset.scale(1.0 / distance).scale(Math.min(requestedSpeed, distance));
            double acceleration = this.beru.isAerialCombatActive() ? 0.48 : 0.36;
            Vec3 next = this.beru.getDeltaMovement().scale(1.0 - acceleration).add(desired.scale(acceleration));
            next = this.collisionSafeVelocity(next, desired);
            this.recordBlockedState(next);
            this.beru.setDeltaMovement(next);
            this.beru.setSpeed((float)requestedSpeed);
            this.beru.hasImpulse = true;
            this.orientToVelocity(next);
         }
      }
   }

   public void setDirectVelocity(Vec3 velocity) {
      this.directVelocityThisTick = true;
      this.operation = Operation.WAIT;
      Vec3 safeVelocity = this.collisionSafeVelocity(velocity, velocity);
      this.recordBlockedState(safeVelocity);
      this.beru.setDeltaMovement(safeVelocity);
      this.beru.hasImpulse = true;
   }

   public boolean isBlockedFor(int ticks) {
      return this.blockedTicks >= ticks;
   }

   public void resetBlockedState() {
      this.blockedTicks = 0;
   }

   private void orientToVelocity(Vec3 velocity) {
      double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
      if (horizontal > 1.0E-4) {
         float desiredYaw = (float)(Mth.atan2(velocity.z, velocity.x) * 180.0F / (float)Math.PI) - 90.0F;
         this.beru.setYRot(this.rotlerp(this.beru.getYRot(), desiredYaw, 22.0F));
         this.beru.yBodyRot = this.beru.getYRot();
         float desiredPitch = (float)(-(Mth.atan2(velocity.y, horizontal) * 180.0F / (float)Math.PI));
         this.beru.setXRot(Mth.lerp(0.24F, this.beru.getXRot(), Mth.clamp(desiredPitch, -45.0F, 45.0F)));
      }
   }

   private Vec3 collisionSafeVelocity(Vec3 proposed, Vec3 desired) {
      if (this.canMove(proposed)) {
         return proposed;
      }

      double speed = Math.max(0.28, Math.min(1.15, proposed.length()));
      Vec3 forward = new Vec3(desired.x, 0.0, desired.z);
      if (forward.lengthSqr() < 1.0E-5) {
         forward = new Vec3(0.0, 0.0, 1.0);
      } else {
         forward = forward.normalize();
      }

      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      if ((this.beru.getId() & 1) != 0) {
         side = side.scale(-1.0);
      }

      double vertical = Mth.clamp(desired.y, -0.42, 0.42);
      Vec3[] alternatives = new Vec3[]{
         forward.scale(speed * 0.72).add(0.0, vertical, 0.0),
         side.scale(speed * 0.82).add(forward.scale(speed * 0.24)).add(0.0, 0.18, 0.0),
         side.scale(-speed * 0.82).add(forward.scale(speed * 0.24)).add(0.0, 0.24, 0.0),
         forward.scale(speed * 0.45).add(0.0, 0.46, 0.0),
         forward.scale(speed * 0.45).add(0.0, -0.36, 0.0),
         forward.scale(-speed * 0.55).add(0.0, 0.28, 0.0)
      };

      for (Vec3 alternative : alternatives) {
         if (this.canMove(alternative)) {
            return alternative;
         }
      }

      return Vec3.ZERO;
   }

   private boolean canMove(Vec3 velocity) {
      if (velocity.lengthSqr() < 1.0E-6) {
         return true;
      }

      int samples = Math.max(1, Mth.ceil(velocity.length() / 0.3));

      for (int sample = 1; sample <= samples; sample++) {
         AABB moved = this.beru.getBoundingBox().move(velocity.scale((double)sample / samples));
         if (!this.beru.level().noCollision(this.beru, moved)) {
            return false;
         }
      }

      return true;
   }

   private void recordBlockedState(Vec3 velocity) {
      if (velocity.lengthSqr() < 1.0E-5) {
         this.blockedTicks++;
      } else {
         this.blockedTicks = Math.max(0, this.blockedTicks - 2);
      }
   }

   private void brake() {
      this.directVelocityThisTick = false;
      Vec3 movement = this.beru.getDeltaMovement();
      if (this.beru.isNoGravity()) {
         movement = movement.scale(0.52);
         if (movement.lengthSqr() < 0.0025) {
            movement = Vec3.ZERO;
         }
      } else {
         movement = new Vec3(movement.x * 0.58, movement.y, movement.z * 0.58);
      }

      this.beru.setDeltaMovement(movement);
      this.beru.setSpeed(0.0F);
   }
}
