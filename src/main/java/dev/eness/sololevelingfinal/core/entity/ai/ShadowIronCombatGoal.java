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
import dev.eness.sololevelingfinal.core.entity.ShadowIronEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;
import dev.eness.sololevelingfinal.core.util.ShadowIronCombatManager;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public final class ShadowIronCombatGoal extends Goal {
   private static final double APPROACH_SPEED = 1.16;
   private static final double INTERCEPT_SPEED = 1.28;
   private static final int PATH_INTERVAL_TICKS = 8;
   private final ShadowIronEntity iron;
   private Vec3 lastPosition = Vec3.ZERO;
   private double lastTargetDistanceSqr = Double.POSITIVE_INFINITY;
   private long nextPathTick;
   private int stalledTicks;
   private ShadowIronCombatPolicy.RecoveryStage handledRecovery = ShadowIronCombatPolicy.RecoveryStage.NONE;

   public ShadowIronCombatGoal(ShadowIronEntity iron) {
      this.iron = iron;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      return this.iron.isActing() || this.isSafeTarget(this.iron.getTarget()) || ShadowIronCombatManager.findGuardianThreat(this.iron) != null;
   }

   @Override
   public boolean canContinueToUse() {
      return this.canUse();
   }

   @Override
   public void start() {
      this.iron.setAggressive(true);
      this.resetProgress(this.objective());
      this.nextPathTick = 0L;
   }

   @Override
   public void stop() {
      this.iron.setAggressive(false);
      this.iron.getNavigation().stop();
      this.stalledTicks = 0;
      this.handledRecovery = ShadowIronCombatPolicy.RecoveryStage.NONE;
   }

   @Override
   public void tick() {
      if (this.iron.isActing()) {
         this.iron.getNavigation().stop();
         LivingEntity current = this.objective();
         if (current != null) {
            this.iron.getLookControl().setLookAt(current, 45.0F, 45.0F);
         }
      } else {
         Player owner = ShadowMonarchManager.getShadowOwnerPlayer(this.iron);
         if (owner != null) {
            if (this.iron.distanceToSqr(owner) > square(28.0)) {
               this.requestPath(owner, 1.28, true);
               this.trackProgress(owner, true);
            } else {
               LivingEntity target = this.objective();
               if (this.isSafeTarget(target)) {
                  if (this.iron.getTarget() != target) {
                     this.iron.setTarget(target);
                  }

                  this.iron.getLookControl().setLookAt(target, 45.0F, 45.0F);
                  if (Math.floorMod(this.iron.tickCount + this.iron.getId(), 10) == 0) {
                     ShadowIronCombatManager.tryGuardianChallenge(this.iron, target);
                  }

                  if (this.iron.canRoarNow() && ShadowIronCombatManager.shouldRoar(this.iron) && this.iron.beginRoar()) {
                     this.iron.getNavigation().stop();
                  } else if (this.iron.canBlockNow() && ShadowIronCombatManager.shouldBrace(this.iron, target) && this.iron.beginBlock(false)) {
                     this.iron.getNavigation().stop();
                  } else {
                     double surfaceDistance = CombatRangeHelper.surfaceDistance(this.iron, target);
                     if (surfaceDistance <= 3.4 && this.iron.getSensing().hasLineOfSight(target)) {
                        this.iron.getNavigation().stop();
                        if (this.iron.canAttackNow()) {
                           this.iron.beginAttack(target, false);
                        }

                        this.trackProgress(target, false);
                     } else {
                        this.requestPath(target, 1.16, false);
                        if (this.iron.isInWaterOrBubble() && (this.iron.tickCount & 3) == 0) {
                           this.iron.getJumpControl().jump();
                        }

                        this.trackProgress(target, true);
                     }
                  }
               }
            }
         }
      }
   }

   private LivingEntity objective() {
      LivingEntity target = this.iron.getTarget();
      return this.isSafeTarget(target) ? target : ShadowIronCombatManager.findGuardianThreat(this.iron);
   }

   private void requestPath(LivingEntity target, double speed, boolean force) {
      long now = this.iron.level().getGameTime();
      if (force || now >= this.nextPathTick || this.iron.getNavigation().isDone()) {
         this.nextPathTick = now + 8L + Math.floorMod(this.iron.getId(), 3);
         this.iron.getNavigation().moveTo(target, speed);
      }
   }

   private void trackProgress(LivingEntity target, boolean expectsMovement) {
      if (target != null) {
         double movedSqr = this.iron.position().distanceToSqr(this.lastPosition);
         double targetDistanceSqr = this.iron.distanceToSqr(target);
         this.lastPosition = this.iron.position();
         if (!expectsMovement) {
            this.stalledTicks = 0;
            this.handledRecovery = ShadowIronCombatPolicy.RecoveryStage.NONE;
            this.lastTargetDistanceSqr = targetDistanceSqr;
         } else {
            boolean progressed = movedSqr > 0.025 || targetDistanceSqr + 0.75 < this.lastTargetDistanceSqr;
            this.lastTargetDistanceSqr = targetDistanceSqr;
            if (progressed) {
               this.stalledTicks = Math.max(0, this.stalledTicks - 3);
               if (this.stalledTicks == 0) {
                  this.handledRecovery = ShadowIronCombatPolicy.RecoveryStage.NONE;
               }
            } else {
               this.stalledTicks++;
               ShadowIronCombatPolicy.RecoveryStage stage = ShadowIronCombatPolicy.recoveryStage(this.stalledTicks);
               if (stage != this.handledRecovery) {
                  this.handledRecovery = stage;
                  this.handleRecovery(stage, target);
               }
            }
         }
      }
   }

   private void handleRecovery(ShadowIronCombatPolicy.RecoveryStage stage, LivingEntity target) {
      switch (stage) {
         case REPATH:
            this.iron.getNavigation().stop();
            this.nextPathTick = 0L;
            this.requestPath(target, 1.16, true);
            break;
         case ESCAPE:
            this.iron.getJumpControl().jump();
            Vec3 escape = DefaultRandomPos.getPosAway(this.iron, 7, 4, target.position());
            if (escape != null && this.tryPathTo(escape, 1.28)) {
               return;
            }

            this.requestPath(target, 1.16, true);
            break;
         case RECALL:
            if (ShadowMonarchManager.tryRecoverStuckShadowNearOwner(this.iron, target)) {
               this.resetProgress(target);
            } else {
               this.stalledTicks = 20;
               this.handledRecovery = ShadowIronCombatPolicy.RecoveryStage.REPATH;
            }
         case NONE:
      }
   }

   private boolean tryPathTo(Vec3 destination, double speed) {
      BlockPos pos = BlockPos.containing(destination);
      if (this.iron.level().hasChunkAt(pos) && this.iron.level().getWorldBorder().isWithinBounds(pos)) {
         Path path = this.iron.getNavigation().createPath(pos, 1);
         return path != null && path.canReach() && this.iron.getNavigation().moveTo(path, speed);
      } else {
         return false;
      }
   }

   private void resetProgress(LivingEntity target) {
      this.lastPosition = this.iron.position();
      this.lastTargetDistanceSqr = target == null ? Double.POSITIVE_INFINITY : this.iron.distanceToSqr(target);
      this.stalledTicks = 0;
      this.handledRecovery = ShadowIronCombatPolicy.RecoveryStage.NONE;
   }

   private boolean isSafeTarget(LivingEntity target) {
      return target != null && target.level() == this.iron.level() && ShadowMonarchManager.canShadowDamage(this.iron, target);
   }

   private static double square(double value) {
      return value * value;
   }
}
