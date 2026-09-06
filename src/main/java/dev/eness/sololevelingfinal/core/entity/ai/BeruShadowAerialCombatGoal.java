package dev.eness.sololevelingfinal.core.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public final class BeruShadowAerialCombatGoal extends Goal {
   private static final double HOVER_HEIGHT = 0.65;
   private static final double CLAW_REACH_SQR = 11.0;
   private static final double DASH_RANGE_SQR = 144.0;
   private static final double TELEPORT_RANGE_SQR = 225.0;
   private static final double SLAM_TRIGGER_RANGE_SQR = 196.0;
   private static final double SLAM_RADIUS = 3.75;
   private static final int CLAW_COOLDOWN_TICKS = 18;
   private static final int BOSS_FLIGHT_DURATION_MIN = 50;
   private static final int BOSS_FLIGHT_DURATION_VARIANCE = 31;
   private static final int BOSS_FLIGHT_COOLDOWN_MIN = 180;
   private static final int BOSS_FLIGHT_COOLDOWN_VARIANCE = 121;
   private static final int BOSS_INITIAL_DELAY_MIN = 80;
   private static final int BOSS_INITIAL_DELAY_VARIANCE = 61;
   private static final int STUCK_TICKS_BEFORE_ESCAPE = 14;
   private static final int ESCAPE_TICKS = 14;
   private final BeruShadowEntity beru;
   private int clawCooldown;
   private int dashTicks;
   private int teleportCooldown = 30;
   private int slamCooldown = 100;
   private int phaseTicks;
   private int bossFlightTicksRemaining;
   private int noProgressTicks;
   private int escapeTicks;
   private int escapeAttempts;
   private boolean bossBurst;
   private LivingEntity scheduledBoss;
   private long nextBossFlightTick = Long.MAX_VALUE;
   private BeruShadowAerialCombatGoal.FlightPhase phase = BeruShadowAerialCombatGoal.FlightPhase.CHASE;
   private Vec3 divePoint = Vec3.ZERO;
   private Vec3 escapePoint = Vec3.ZERO;
   private Vec3 lastProgressPosition = Vec3.ZERO;

   public BeruShadowAerialCombatGoal(BeruShadowEntity beru) {
      this.beru = beru;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      LivingEntity target = this.beru.getTarget();
      if (!this.isSafeTarget(target)) {
         this.clearBossSchedule();
         return false;
      } else if (BeruShadowFlightPolicy.requiresSustainedFlight(target)) {
         return true;
      } else if (!BeruShadowFlightPolicy.isBossTarget(target)) {
         this.clearBossSchedule();
         return false;
      } else {
         long now = this.beru.level().getGameTime();
         if (this.scheduledBoss != target) {
            this.scheduledBoss = target;
            this.nextBossFlightTick = now + 80L + this.beru.getRandom().nextInt(61);
            return false;
         } else {
            return now >= this.nextBossFlightTick;
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      LivingEntity target = this.beru.getTarget();
      if (!this.isSafeTarget(target)) {
         return false;
      } else {
         return BeruShadowFlightPolicy.requiresSustainedFlight(target)
            ? true
            : this.bossBurst && target == this.scheduledBoss && BeruShadowFlightPolicy.isBossTarget(target) && this.bossFlightTicksRemaining > 0;
      }
   }

   @Override
   public void start() {
      LivingEntity target = this.beru.getTarget();
      this.bossBurst = target != null && !BeruShadowFlightPolicy.requiresSustainedFlight(target) && BeruShadowFlightPolicy.isBossTarget(target);
      this.bossFlightTicksRemaining = this.bossBurst ? 50 + this.beru.getRandom().nextInt(31) : 0;
      this.beru.setAerialCombatActive(true);
      this.beru.setFlightMode(true);
      this.beru.setAggressive(true);
      this.beru.getNavigation().stop();
      this.beru.setAnimation("start_flying");
      this.lastProgressPosition = this.beru.position();
      this.noProgressTicks = 0;
      this.escapeTicks = 0;
      this.escapeAttempts = 0;
   }

   @Override
   public void stop() {
      LivingEntity target = this.beru.getTarget();
      if (this.bossBurst && target == this.scheduledBoss && this.isSafeTarget(target)) {
         this.nextBossFlightTick = this.beru.level().getGameTime() + 180L + this.beru.getRandom().nextInt(121);
      }

      this.beru.setAerialCombatActive(false);
      this.beru.setAggressive(false);
      this.beru.getNavigation().stop();
      this.beru.setFlightMode(false);
      this.dashTicks = 0;
      this.phaseTicks = 0;
      this.bossFlightTicksRemaining = 0;
      this.bossBurst = false;
      this.noProgressTicks = 0;
      this.escapeTicks = 0;
      this.escapeAttempts = 0;
      this.phase = BeruShadowAerialCombatGoal.FlightPhase.CHASE;
   }

   @Override
   public void tick() {
      LivingEntity target = this.beru.getTarget();
      if (this.isSafeTarget(target)) {
         this.clawCooldown = Math.max(0, this.clawCooldown - 1);
         this.teleportCooldown = Math.max(0, this.teleportCooldown - 1);
         this.slamCooldown = Math.max(0, this.slamCooldown - 1);
         if (this.bossBurst) {
            this.bossFlightTicksRemaining--;
         }

         this.beru.setFlightMode(true);
         this.beru.getLookControl().setLookAt(target, 60.0F, 60.0F);
         if (!this.tickStuckRecovery(target)) {
            switch (this.phase) {
               case ASCEND:
                  this.tickAscent(target);
                  break;
               case DIVE:
                  this.tickDive(target);
                  break;
               case RECOVER:
                  this.tickRecovery(target);
                  break;
               case CHASE:
                  this.tickChase(target);
            }
         }
      }
   }

   private void tickChase(LivingEntity target) {
      double distanceSqr = this.beru.distanceToSqr(target);
      if (this.slamCooldown <= 0 && distanceSqr <= 196.0 && this.beru.hasLineOfSight(target)) {
         this.beginDiveSlam(target);
      } else {
         if (this.teleportCooldown <= 0 && distanceSqr >= 225.0 && this.tryTeleportBehind(target, true)) {
            this.teleportCooldown = 70 + this.beru.getRandom().nextInt(51);
            distanceSqr = this.beru.distanceToSqr(target);
         }

         if (this.dashTicks > 0) {
            this.tickAerialDash(target);
         } else {
            double targetCenterY = target.getY() + target.getBbHeight() * 0.62;
            double hoverOffset = BeruShadowFlightPolicy.requiresSustainedFlight(target) ? 0.15 : 0.65;
            double hoverY = targetCenterY + hoverOffset + Math.sin((this.beru.tickCount + this.beru.getId()) * 0.16) * 0.25;
            double speed = distanceSqr > 64.0 ? 2.4 : 1.8;
            if (this.beru.getNavigation().isDone() || this.beru.tickCount % 8 == 0) {
               boolean foundPath = this.beru.getNavigation().moveTo(target.getX(), hoverY, target.getZ(), speed);
               if (!foundPath) {
                  this.beru.getMoveControl().setWantedPosition(target.getX(), hoverY, target.getZ(), speed);
               }
            }

            if (this.clawCooldown <= 0) {
               if (distanceSqr <= 11.0) {
                  this.performClawStrike(target);
               } else if (distanceSqr <= 144.0 && this.beru.hasLineOfSight(target)) {
                  this.dashTicks = 7;
                  this.beru.setAnimation("flyattack");
               }
            }
         }
      }
   }

   private void tickAerialDash(LivingEntity target) {
      this.dashTicks--;
      this.beru.getNavigation().stop();
      Vec3 aim = target.getEyePosition().subtract(this.beru.position());
      if (aim.lengthSqr() > 1.0E-4) {
         Vec3 velocity = aim.normalize().scale(1.05);
         this.setDirectVelocity(velocity);
      }

      if (this.beru.getBoundingBox().inflate(0.8).intersects(target.getBoundingBox()) || this.beru.distanceToSqr(target) <= 11.0) {
         this.performClawStrike(target);
         this.dashTicks = 0;
      } else if (this.dashTicks == 0) {
         this.clawCooldown = 7;
      }
   }

   private void performClawStrike(LivingEntity target) {
      if (this.isSafeTarget(target)) {
         this.beru.setAnimation("flyattack");
         this.beru.swing(InteractionHand.MAIN_HAND);
         if (this.beru.doHurtTarget(target)) {
            Vec3 knockback = target.position().subtract(this.beru.position());
            if (knockback.lengthSqr() > 1.0E-4) {
               Vec3 impulse = knockback.normalize().scale(0.55);
               target.push(impulse.x, 0.32, impulse.z);
            }

            this.emitImpact(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), false);
         }

         Vec3 rebound = this.beru.position().subtract(target.position());
         if (rebound.lengthSqr() > 1.0E-4) {
            Vec3 impulse = rebound.normalize().scale(0.42);
            this.setDirectVelocity(new Vec3(impulse.x, 0.45, impulse.z));
         }

         this.clawCooldown = 18;
      }
   }

   private void beginDiveSlam(LivingEntity target) {
      this.phase = BeruShadowAerialCombatGoal.FlightPhase.ASCEND;
      this.phaseTicks = 0;
      this.divePoint = target.position();
      this.slamCooldown = 150 + this.beru.getRandom().nextInt(71);
      this.beru.setAnimation("start_flying");
      this.beru.getNavigation().stop();
   }

   private void tickAscent(LivingEntity target) {
      this.phaseTicks++;
      this.divePoint = target.position();
      double apexY = target.getY() + target.getBbHeight() + 6.0;
      this.beru.getMoveControl().setWantedPosition(target.getX(), apexY, target.getZ(), 2.75);
      if (this.phaseTicks >= 14 || this.beru.getY() >= apexY - 1.0) {
         this.phase = BeruShadowAerialCombatGoal.FlightPhase.DIVE;
         this.phaseTicks = 0;
         this.divePoint = target.position().add(0.0, 0.15, 0.0);
         this.beru.setAnimation("special_attack");
      }
   }

   private void tickDive(LivingEntity target) {
      this.phaseTicks++;
      if (this.phaseTicks <= 4) {
         this.divePoint = target.position().add(0.0, 0.15, 0.0);
      }

      Vec3 diveVector = this.divePoint.subtract(this.beru.position());
      if (diveVector.lengthSqr() > 1.0E-4) {
         Vec3 velocity = diveVector.normalize().scale(1.45);
         this.setDirectVelocity(new Vec3(velocity.x, Math.min(-0.85, velocity.y), velocity.z));
      }

      boolean reachedImpact = this.beru.position().distanceToSqr(this.divePoint) <= 5.0 || this.beru.getY() <= this.divePoint.y + 0.65;
      if (reachedImpact || this.phaseTicks >= 14) {
         this.performDiveSlam();
      }
   }

   private void performDiveSlam() {
      this.beru.setAnimation("special_attack");
      this.beru.setDeltaMovement(Vec3.ZERO);
      double damage = Math.max(6.0, this.beru.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.72);
      AABB impactArea = this.beru.getBoundingBox().inflate(3.75, 2.25, 3.75);

      for (LivingEntity candidate : this.beru
         .level()
         .getEntitiesOfClass(LivingEntity.class, impactArea, target -> ShadowMonarchManager.canShadowDamage(this.beru, target))) {
         if (candidate.hurt(this.beru.level().damageSources().mobAttack(this.beru), (float)damage)) {
            Vec3 away = candidate.position().subtract(this.beru.position());
            if (away.lengthSqr() > 1.0E-4) {
               Vec3 impulse = away.normalize().scale(0.75);
               candidate.push(impulse.x, 0.65, impulse.z);
            }
         }
      }

      this.emitImpact(this.beru.getX(), this.beru.getY() + 0.25, this.beru.getZ(), true);
      this.phase = BeruShadowAerialCombatGoal.FlightPhase.RECOVER;
      this.phaseTicks = 0;
      this.clawCooldown = 16;
   }

   private void tickRecovery(LivingEntity target) {
      this.phaseTicks++;
      this.beru.getMoveControl().setWantedPosition(this.beru.getX(), Math.max(this.beru.getY() + 0.2, target.getEyeY() + 0.65), this.beru.getZ(), 1.15);
      if (this.phaseTicks >= 9) {
         this.phase = BeruShadowAerialCombatGoal.FlightPhase.CHASE;
         this.phaseTicks = 0;
      }
   }

   private boolean tickStuckRecovery(LivingEntity target) {
      if (this.escapeTicks > 0) {
         return this.tickEscape();
      }

      if (this.phase == BeruShadowAerialCombatGoal.FlightPhase.CHASE && this.dashTicks <= 0) {
         double movedSqr = this.beru.position().distanceToSqr(this.lastProgressPosition);
         this.lastProgressPosition = this.beru.position();
         boolean controllerBlocked = this.beru.getMoveControl() instanceof BeruFlightMoveControl control && control.isBlockedFor(5);
         boolean shouldBeClosing = this.beru.distanceToSqr(target) > 14.850000000000001;
         boolean stalled = movedSqr < 0.0025 || (this.beru.horizontalCollision || controllerBlocked) && movedSqr < 0.025;
         if (shouldBeClosing && stalled) {
            this.noProgressTicks++;
         } else {
            this.noProgressTicks = Math.max(0, this.noProgressTicks - 2);
            if (movedSqr > 0.04) {
               this.escapeAttempts = 0;
            }
         }

         if (this.noProgressTicks < 14) {
            return false;
         }

         this.noProgressTicks = 0;
         this.escapeAttempts++;
         if (this.escapeAttempts >= 3) {
            if (this.bossBurst) {
               this.bossFlightTicksRemaining = 0;
               return false;
            }

            if (this.tryTeleportBehind(target, false)) {
               this.escapeAttempts = 0;
               return true;
            }
         }

         Vec3 destination = this.findEscapePoint(target);
         if (destination == null) {
            if (this.bossBurst) {
               this.bossFlightTicksRemaining = 0;
            } else if (this.tryTeleportBehind(target, false)) {
               this.escapeAttempts = 0;
            }

            return false;
         } else {
            this.escapePoint = destination;
            this.escapeTicks = 14;
            this.beru.getNavigation().stop();
            if (this.beru.getMoveControl() instanceof BeruFlightMoveControl control) {
               control.resetBlockedState();
            }

            return this.tickEscape();
         }
      } else {
         this.lastProgressPosition = this.beru.position();
         this.noProgressTicks = 0;
         return false;
      }
   }

   private boolean tickEscape() {
      if (!(this.beru.position().distanceToSqr(this.escapePoint) < 0.45) && --this.escapeTicks > 0) {
         this.beru.getNavigation().stop();
         this.beru.getMoveControl().setWantedPosition(this.escapePoint.x, this.escapePoint.y, this.escapePoint.z, 2.25);
         return true;
      }

      this.escapeTicks = 0;
      this.lastProgressPosition = this.beru.position();
      if (this.beru.getMoveControl() instanceof BeruFlightMoveControl control) {
         control.resetBlockedState();
      }

      return false;
   }

   private Vec3 findEscapePoint(LivingEntity target) {
      Vec3 towardTarget = target.getEyePosition().subtract(this.beru.position());
      Vec3 forward = new Vec3(towardTarget.x, 0.0, towardTarget.z);
      if (forward.lengthSqr() < 1.0E-4) {
         forward = new Vec3(0.0, 0.0, 1.0);
      } else {
         forward = forward.normalize();
      }

      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      if ((this.beru.getId() & 1) != 0) {
         side = side.scale(-1.0);
      }

      double vertical = Mth.clamp(towardTarget.y, -1.8, 2.4);
      Vec3[] offsets = new Vec3[]{
         side.scale(3.0).add(forward.scale(0.8)).add(0.0, 0.8, 0.0),
         side.scale(-3.0).add(forward.scale(0.8)).add(0.0, 1.1, 0.0),
         forward.scale(2.5).add(0.0, Math.max(1.0, vertical), 0.0),
         new Vec3(0.0, 2.6, 0.0),
         forward.scale(1.4).add(0.0, Math.min(-0.8, vertical), 0.0),
         forward.scale(-2.0).add(0.0, 1.4, 0.0)
      };

      for (Vec3 offset : offsets) {
         Vec3 destination = this.beru.position().add(offset);
         if (this.canFlyDirectlyTo(destination)) {
            return destination;
         }
      }

      return null;
   }

   private boolean canFlyDirectlyTo(Vec3 destination) {
      BlockPos blockPos = BlockPos.containing(destination);
      if (this.beru.level().hasChunkAt(blockPos) && this.beru.level().getWorldBorder().isWithinBounds(blockPos)) {
         Vec3 offset = destination.subtract(this.beru.position());
         int samples = Math.max(1, Mth.ceil(offset.length() / 0.35));

         for (int sample = 1; sample <= samples; sample++) {
            AABB bounds = this.beru.getBoundingBox().move(offset.scale((double)sample / samples));
            if (!this.beru.level().noCollision(this.beru, bounds)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean tryTeleportBehind(LivingEntity target, boolean attackCue) {
      Vec3 backwards = target.getLookAngle().multiply(-1.0, 0.0, -1.0);
      if (backwards.lengthSqr() < 1.0E-4) {
         backwards = target.position().subtract(this.beru.position()).multiply(-1.0, 0.0, -1.0);
      }

      if (backwards.lengthSqr() < 1.0E-4) {
         backwards = new Vec3(0.0, 0.0, 1.0);
      }

      backwards = backwards.normalize();
      Vec3 sideways = new Vec3(-backwards.z, 0.0, backwards.x);
      double[] sideOffsets = new double[]{0.0, 1.8, -1.8};
      double[] heightOffsets = new double[]{1.2, 2.8, 0.2};

      for (double height : heightOffsets) {
         for (double side : sideOffsets) {
            Vec3 destination = target.position().add(backwards.scale(2.4)).add(sideways.scale(side)).add(0.0, height, 0.0);
            if (this.canTeleportTo(destination)) {
               this.beru.teleportTo(destination.x, destination.y, destination.z);
               this.beru.setDeltaMovement(Vec3.ZERO);
               if (attackCue) {
                  this.beru.setAnimation("flyattack");
                  this.emitImpact(destination.x, destination.y + 0.8, destination.z, false);
               }

               this.lastProgressPosition = this.beru.position();
               this.noProgressTicks = 0;
               return true;
            }
         }
      }

      return false;
   }

   private boolean canTeleportTo(Vec3 destination) {
      BlockPos blockPos = BlockPos.containing(destination);
      if (!this.beru.level().hasChunkAt(blockPos)) {
         return false;
      }

      WorldBorder border = this.beru.level().getWorldBorder();
      if (!border.isWithinBounds(blockPos)) {
         return false;
      }

      Vec3 offset = destination.subtract(this.beru.position());
      AABB movedBounds = this.beru.getBoundingBox().move(offset);
      return this.beru.level().noCollision(this.beru, movedBounds);
   }

   private boolean isSafeTarget(LivingEntity target) {
      return target != null && target.level() == this.beru.level() && ShadowMonarchManager.canShadowDamage(this.beru, target);
   }

   private void clearBossSchedule() {
      this.scheduledBoss = null;
      this.nextBossFlightTick = Long.MAX_VALUE;
      this.bossFlightTicksRemaining = 0;
      this.bossBurst = false;
   }

   private void setDirectVelocity(Vec3 velocity) {
      if (this.beru.getMoveControl() instanceof BeruFlightMoveControl control) {
         control.setDirectVelocity(velocity);
      } else {
         this.beru.setDeltaMovement(velocity);
         this.beru.hasImpulse = true;
      }
   }

   private void emitImpact(double x, double y, double z, boolean slam) {
      if (this.beru.level() instanceof ServerLevel level) {
         level.sendParticles(
            SololevelingModParticleTypes.IMPACT_22.get(), x, y, z, slam ? 8 : 3, slam ? 0.9 : 0.18, slam ? 0.25 : 0.45, slam ? 0.9 : 0.18, slam ? 0.08 : 0.0
         );
         if (slam) {
            level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 4, 0.75, 0.15, 0.75, 0.0);
         }

         level.playSound(
            (Player)null,
            BlockPos.containing(x, y, z),
            ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
            SoundSource.NEUTRAL,
            slam ? 1.45F : 0.8F,
            slam ? 0.85F : Mth.nextFloat(this.beru.getRandom(), 1.1F, 1.35F)
         );
      }
   }

   private enum FlightPhase {
      CHASE,
      ASCEND,
      DIVE,
      RECOVER;
   }
}
