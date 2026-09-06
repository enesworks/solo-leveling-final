package dev.eness.sololevelingfinal.core.entity.ai;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.projectile.Projectile;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public final class ShadowThreatTargetGoal extends Goal {
   private static final int HITS_TO_PULL_AGGRO = 2;
   private static final int GROUP_HITS_TO_PULL_AGGRO = 3;
   private static final int THREAT_WINDOW_TICKS = 80;
   private static final int TARGET_LOCK_TICKS = 120;
   private static final float MINIMUM_DAMAGE_TO_PULL = 6.0F;
   private static final float MAX_HEALTH_DAMAGE_FRACTION = 0.1F;
   private final Mob mob;
   private final Map<UUID, ShadowThreatTargetGoal.Threat> recentThreat = new HashMap<>();
   private LivingEntity lockedTarget;
   private long lockedUntil;

   public ShadowThreatTargetGoal(Mob mob) {
      this.mob = mob;
      this.setFlags(EnumSet.of(Flag.TARGET));
   }

   public void recordSuccessfulHit(DamageSource source, float dealtDamage) {
      if (source != null && !this.mob.level().isClientSide() && this.mob.isAlive()) {
         LivingEntity attacker = this.findShadowAttacker(source);
         if (this.isUsableTarget(attacker)) {
            long now = this.mob.level().getGameTime();
            this.pruneExpiredThreat(now);
            if (attacker == this.lockedTarget && now < this.lockedUntil) {
               this.lockedUntil = now + 120L;
               this.mob.setTarget(attacker);
            } else {
               ShadowThreatTargetGoal.Threat threat = this.recentThreat
                  .computeIfAbsent(attacker.getUUID(), ignored -> new ShadowThreatTargetGoal.Threat(attacker));
               if (now - threat.lastHitTick > 80L) {
                  threat.reset(attacker);
               }

               threat.attacker = attacker;
               threat.hits++;
               threat.damage = threat.damage + Math.max(0.0F, dealtDamage);
               threat.lastHitTick = now;
               float damageThreshold = Math.max(6.0F, this.mob.getMaxHealth() * 0.1F);
               int groupHits = 0;
               float groupDamage = 0.0F;
               ShadowThreatTargetGoal.Threat highestThreat = threat;

               for (ShadowThreatTargetGoal.Threat candidate : this.recentThreat.values()) {
                  groupHits += candidate.hits;
                  groupDamage += candidate.damage;
                  if (candidate.score() > highestThreat.score()) {
                     highestThreat = candidate;
                  }
               }

               if (threat.hits >= 2 || threat.damage >= damageThreshold || groupHits >= 3 || groupDamage >= damageThreshold * 1.5F) {
                  this.lockedTarget = highestThreat.attacker;
                  this.lockedUntil = now + 120L;
                  this.recentThreat.clear();
                  this.mob.setTarget(this.lockedTarget);
               }
            }
         }
      }
   }

   @Override
   public boolean canUse() {
      return this.hasActiveLock();
   }

   @Override
   public boolean canContinueToUse() {
      return this.hasActiveLock();
   }

   @Override
   public void start() {
      if (this.lockedTarget != null) {
         this.mob.setTarget(this.lockedTarget);
      }
   }

   @Override
   public void tick() {
      if (this.lockedTarget != null) {
         this.mob.setTarget(this.lockedTarget);
      }
   }

   @Override
   public void stop() {
      if (this.mob.getTarget() == this.lockedTarget) {
         this.mob.setTarget(null);
      }

      this.lockedTarget = null;
      this.lockedUntil = 0L;
   }

   private boolean hasActiveLock() {
      return this.mob.level().getGameTime() < this.lockedUntil && this.isUsableTarget(this.lockedTarget);
   }

   private boolean isUsableTarget(LivingEntity target) {
      return target != null
         && target != this.mob
         && target.isAlive()
         && target.isAttackable()
         && !target.isInvulnerable()
         && (ShadowMonarchManager.isShadowEntity(target) || ShadowMonarchManager.isTrackedShadowEntity(target));
   }

   private LivingEntity findShadowAttacker(DamageSource source) {
      LivingEntity attacker = this.asShadow(source.getEntity());
      if (attacker != null) {
         return attacker;
      } else {
         attacker = this.asShadow(source.getDirectEntity());
         if (attacker != null) {
            return attacker;
         } else {
            return source.getDirectEntity() instanceof Projectile projectile ? this.asShadow(projectile.getOwner()) : null;
         }
      }
   }

   private LivingEntity asShadow(Entity candidate) {
      if (!(candidate instanceof LivingEntity living)) {
         return null;
      } else {
         return !ShadowMonarchManager.isShadowEntity(living) && !ShadowMonarchManager.isTrackedShadowEntity(living) ? null : living;
      }
   }

   private void pruneExpiredThreat(long now) {
      Iterator<ShadowThreatTargetGoal.Threat> entries = this.recentThreat.values().iterator();

      while (entries.hasNext()) {
         ShadowThreatTargetGoal.Threat threat = entries.next();
         if (now - threat.lastHitTick > 80L || threat.attacker == null || !threat.attacker.isAlive()) {
            entries.remove();
         }
      }
   }

   private static final class Threat {
      private LivingEntity attacker;
      private int hits;
      private float damage;
      private long lastHitTick;

      private Threat(LivingEntity attacker) {
         this.reset(attacker);
      }

      private void reset(LivingEntity attacker) {
         this.attacker = attacker;
         this.hits = 0;
         this.damage = 0.0F;
         this.lastHitTick = Long.MIN_VALUE;
      }

      private float score() {
         return this.damage + this.hits * 3.0F;
      }
   }
}
