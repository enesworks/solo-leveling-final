package dev.eness.sololevelingfinal.core.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.TuskShadowCombatManager;

public final class TuskShadowCombatGoal extends Goal {
   private static final double APPROACH_SPEED = 1.18;
   private static final double REPOSITION_SPEED = 1.1;
   private static final double RETREAT_SPEED = 1.26;
   private static final int PATH_INTERVAL_TICKS = 9;
   private static final int REPOSITION_MIN_TICKS = 30;
   private static final int REPOSITION_VARIANCE_TICKS = 31;
   private final TuskShadowEntity tusk;
   private Vec3 lastPosition = Vec3.ZERO;
   private double lastTargetDistanceSqr = Double.POSITIVE_INFINITY;
   private long nextPathTick;
   private long nextRepositionTick;
   private int stalledTicks;
   private boolean regrouping;
   private TuskShadowCombatPolicy.RecoveryStage handledRecoveryStage = TuskShadowCombatPolicy.RecoveryStage.NONE;

   public TuskShadowCombatGoal(TuskShadowEntity tusk) {
      this.tusk = tusk;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      return this.isSafeTarget(this.tusk.getTarget());
   }

   @Override
   public boolean canContinueToUse() {
      return this.isSafeTarget(this.tusk.getTarget()) || TuskShadowCombatManager.isCasting(this.tusk);
   }

   @Override
   public void start() {
      this.tusk.setAggressive(true);
      this.lastPosition = this.tusk.position();
      LivingEntity target = this.tusk.getTarget();
      this.lastTargetDistanceSqr = target == null ? Double.POSITIVE_INFINITY : this.tusk.distanceToSqr(target);
      this.stalledTicks = 0;
      this.regrouping = false;
      this.handledRecoveryStage = TuskShadowCombatPolicy.RecoveryStage.NONE;
      this.nextPathTick = 0L;
      this.nextRepositionTick = this.tusk.level().getGameTime() + 30L + this.tusk.getRandom().nextInt(31);
   }

   @Override
   public void stop() {
      this.tusk.setAggressive(false);
      this.tusk.getNavigation().stop();
      this.stalledTicks = 0;
      this.regrouping = false;
      this.handledRecoveryStage = TuskShadowCombatPolicy.RecoveryStage.NONE;
      if (!TuskShadowCombatManager.isCasting(this.tusk)) {
         this.tusk.setCombatState("idle");
      }
   }

   @Override
   public void tick() {
      LivingEntity target = this.tusk.getTarget();
      if (TuskShadowCombatManager.isCasting(this.tusk)) {
         this.tusk.getNavigation().stop();
         if (target != null) {
            this.resetProgress(target);
         }
      } else if (this.isSafeTarget(target)) {
         this.tusk.getLookControl().setLookAt(target, 45.0F, 45.0F);
         Player owner = ShadowMonarchManager.getShadowOwnerPlayer(this.tusk);
         if (owner != null && this.tusk.distanceToSqr(owner) > square(32.0)) {
            boolean enteringRegroup = !this.regrouping;
            this.regrouping = true;
            this.tusk.setCombatState("regrouping");
            this.requestDirectPath(owner, 1.18, enteringRegroup);
            this.trackProgress(target, true);
         } else {
            this.regrouping = false;
            double surfaceDistance = CombatRangeHelper.surfaceDistance(this.tusk, target);
            boolean hasLineOfSight = this.tusk.getSensing().hasLineOfSight(target);
            boolean expectsMovement;
            if (TuskShadowCombatPolicy.shouldRetreat(surfaceDistance)) {
               this.tusk.setCombatState("retreating");
               expectsMovement = this.retreatFrom(target);
            } else if (TuskShadowCombatPolicy.shouldApproach(surfaceDistance, hasLineOfSight)) {
               this.tusk.setCombatState(hasLineOfSight ? "approaching" : "seeking_line");
               expectsMovement = this.seekCastingLane(target, !hasLineOfSight);
            } else {
               expectsMovement = this.holdAndReposition(target);
            }

            if (this.tusk.isInWaterOrBubble() && expectsMovement && (this.tusk.tickCount & 3) == 0) {
               this.tusk.getJumpControl().jump();
            }

            this.trackProgress(target, expectsMovement);
         }
      }
   }

   private boolean holdAndReposition(LivingEntity target) {
      long now = this.tusk.level().getGameTime();
      if (now < this.nextRepositionTick) {
         if (this.tusk.getNavigation().isDone()) {
            this.tusk.setCombatState("holding");
         }

         return !this.tusk.getNavigation().isDone();
      } else {
         this.nextRepositionTick = now + 30L + this.tusk.getRandom().nextInt(31);
         if (this.tryLateralPath(target, 1.1)) {
            this.tusk.setCombatState("repositioning");
            return true;
         } else {
            this.tusk.getNavigation().stop();
            this.tusk.setCombatState("holding");
            return false;
         }
      }
   }

   private boolean seekCastingLane(LivingEntity target, boolean preferSide) {
      long now = this.tusk.level().getGameTime();
      if (now < this.nextPathTick && !this.tusk.getNavigation().isDone()) {
         return true;
      }

      this.nextPathTick = now + 9L + Math.floorMod(this.tusk.getId(), 3);
      return preferSide && this.tryCastingLane(target) ? true : this.tusk.getNavigation().moveTo(target, 1.18);
   }

   private void requestDirectPath(LivingEntity destination, double speed, boolean force) {
      long now = this.tusk.level().getGameTime();
      if (force || now >= this.nextPathTick || this.tusk.getNavigation().isDone()) {
         this.nextPathTick = now + 9L + Math.floorMod(this.tusk.getId(), 3);
         this.tusk.getNavigation().moveTo(destination, speed);
      }
   }

   private boolean retreatFrom(LivingEntity target) {
      long now = this.tusk.level().getGameTime();
      if (now < this.nextPathTick && !this.tusk.getNavigation().isDone()) {
         return true;
      } else {
         this.nextPathTick = now + 9L;
         Vec3 retreat = DefaultRandomPos.getPosAway(this.tusk, 10, 5, target.position());
         if (retreat != null && retreat.distanceToSqr(target.position()) > this.tusk.distanceToSqr(target) && this.tryPathTo(retreat, 1.26)) {
            return true;
         } else {
            Vec3 away = this.tusk.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
            if (away.lengthSqr() > 1.0E-5) {
               away = away.normalize().scale(0.16);
               this.tusk.setDeltaMovement(this.tusk.getDeltaMovement().add(away.x, 0.0, away.z));
               this.tusk.hasImpulse = true;
               return true;
            } else {
               return false;
            }
         }
      }
   }

   private boolean tryCastingLane(LivingEntity target) {
      Vec3 away = this.tusk.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
      if (away.lengthSqr() < 1.0E-4) {
         away = new Vec3(0.0, 0.0, 1.0);
      } else {
         away = away.normalize();
      }

      Vec3 side = new Vec3(-away.z, 0.0, away.x);
      if ((this.tusk.getId() & 1) != 0) {
         side = side.scale(-1.0);
      }

      double y = this.tusk.getY();
      Vec3 base = target.position().add(away.scale(14.0));
      Vec3[] candidates = new Vec3[]{
         new Vec3(base.x + side.x * 5.0, y, base.z + side.z * 5.0),
         new Vec3(base.x - side.x * 5.0, y, base.z - side.z * 5.0),
         new Vec3(base.x + side.x * 8.0, y, base.z + side.z * 8.0),
         new Vec3(base.x - side.x * 8.0, y, base.z - side.z * 8.0),
         new Vec3(base.x, y, base.z)
      };

      for (Vec3 candidate : candidates) {
         if (this.tryPathTo(candidate, 1.18)) {
            return true;
         }
      }

      return false;
   }

   private boolean tryLateralPath(LivingEntity target, double speed) {
      Vec3 toward = target.position().subtract(this.tusk.position()).multiply(1.0, 0.0, 1.0);
      if (toward.lengthSqr() < 1.0E-4) {
         return false;
      }

      toward = toward.normalize();
      Vec3 side = new Vec3(-toward.z, 0.0, toward.x);
      if (this.tusk.getRandom().nextBoolean()) {
         side = side.scale(-1.0);
      }

      Vec3 first = this.tusk.position().add(side.scale(4.5));
      Vec3 second = this.tusk.position().add(side.scale(-4.5));
      return this.tryPathTo(first, speed) || this.tryPathTo(second, speed);
   }

   private boolean tryPathTo(Vec3 destination, double speed) {
      BlockPos blockPos = BlockPos.containing(destination);
      if (this.tusk.level().hasChunkAt(blockPos) && this.tusk.level().getWorldBorder().isWithinBounds(blockPos)) {
         Path path = this.tusk.getNavigation().createPath(blockPos, 1);
         return path != null && path.canReach() && this.tusk.getNavigation().moveTo(path, speed);
      } else {
         return false;
      }
   }

   private void trackProgress(LivingEntity target, boolean expectsMovement) {
      double movedSqr = this.tusk.position().distanceToSqr(this.lastPosition);
      double targetDistanceSqr = this.tusk.distanceToSqr(target);
      this.lastPosition = this.tusk.position();
      if (!expectsMovement) {
         this.stalledTicks = 0;
         this.handledRecoveryStage = TuskShadowCombatPolicy.RecoveryStage.NONE;
         this.lastTargetDistanceSqr = targetDistanceSqr;
      } else {
         boolean progressed = movedSqr > 0.025 || targetDistanceSqr + 0.75 < this.lastTargetDistanceSqr;
         this.lastTargetDistanceSqr = targetDistanceSqr;
         if (progressed) {
            this.stalledTicks = Math.max(0, this.stalledTicks - 3);
            if (this.stalledTicks == 0) {
               this.handledRecoveryStage = TuskShadowCombatPolicy.RecoveryStage.NONE;
            }
         } else {
            this.stalledTicks++;
            TuskShadowCombatPolicy.RecoveryStage stage = TuskShadowCombatPolicy.recoveryStage(this.stalledTicks);
            if (stage != this.handledRecoveryStage) {
               this.handledRecoveryStage = stage;
               this.handleRecovery(stage, target);
            }
         }
      }
   }

   private void handleRecovery(TuskShadowCombatPolicy.RecoveryStage stage, LivingEntity target) {
      switch (stage) {
         case REPATH:
            this.tusk.setCombatState("recovering_path");
            this.tusk.getNavigation().stop();
            this.nextPathTick = 0L;
            if (!this.tryCastingLane(target)) {
               this.tusk.getNavigation().moveTo(target, 1.18);
            }
            break;
         case ESCAPE:
            this.tusk.setCombatState("recovering_terrain");
            this.tusk.getJumpControl().jump();
            this.tusk.getNavigation().stop();
            this.nextPathTick = 0L;
            if (!this.tryLateralPath(target, 1.26)) {
               this.tusk.getNavigation().moveTo(target, 1.18);
            }
            break;
         case RECALL:
            this.tusk.setCombatState("recovering_owner");
            if (ShadowMonarchManager.tryRecoverStuckShadowNearOwner(this.tusk, target)) {
               this.resetProgress(target);
            } else {
               this.stalledTicks = 20;
               this.handledRecoveryStage = TuskShadowCombatPolicy.RecoveryStage.REPATH;
               this.nextPathTick = 0L;
            }
         case NONE:
      }
   }

   private void resetProgress(LivingEntity target) {
      this.lastPosition = this.tusk.position();
      this.lastTargetDistanceSqr = this.tusk.distanceToSqr(target);
      this.stalledTicks = 0;
      this.handledRecoveryStage = TuskShadowCombatPolicy.RecoveryStage.NONE;
   }

   private boolean isSafeTarget(LivingEntity target) {
      return target != null && target.level() == this.tusk.level() && ShadowMonarchManager.canShadowDamage(this.tusk, target);
   }

   private static double square(double value) {
      return value * value;
   }
}
