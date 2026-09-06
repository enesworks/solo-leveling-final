package dev.eness.sololevelingfinal.core.util;

import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class MurderousIntentFearGoal extends Goal {
   private static final double FLEE_SPEED = 1.65;
   private static final double MAX_CASTER_DISTANCE_SQR = 2304.0;
   private final PathfinderMob mob;
   private final UUID casterId;
   private final long expiresAt;
   private long nextPathTick;

   private MurderousIntentFearGoal(PathfinderMob mob, ServerPlayer caster, int durationTicks) {
      this.mob = mob;
      this.casterId = caster.getUUID();
      this.expiresAt = mob.level().getGameTime() + durationTicks;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
   }

   public static boolean apply(ServerPlayer caster, PathfinderMob mob, int durationTicks) {
      if (caster != null
         && mob != null
         && durationTicks > 0
         && !mob.isNoAi()
         && !caster.isAlliedTo(mob)
         && !mob.isAlliedTo(caster)
         && !ShadowMonarchManager.isOwnedShadow(mob, caster)
         && !(mob instanceof TamableAnimal tame && caster.getUUID().equals(tame.getOwnerUUID()))) {
         SololevelingModVariables.PlayerVariables vars = caster.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         double casterLevel = DungeonLevelHelper.playerLevel(caster);
         double targetLevel = DungeonLevelHelper.levelOf(mob);
         boolean weaker = casterLevel > 0.0 && targetLevel > 0.0
            ? targetLevel < casterLevel
            : CombatRankHelper.rankOf(mob) < Math.max(0, Math.min(6, (int)Math.round(vars.HunterRank)));
         if (!weaker) {
            return false;
         }

         MurderousIntentFearGoal goal = new MurderousIntentFearGoal(mob, caster, durationTicks);
         mob.goalSelector.addGoal(0, goal);
         SololevelingMod.queueServerWork(caster.server, durationTicks, () -> mob.goalSelector.removeGoal(goal));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean canUse() {
      ServerPlayer caster = this.caster();
      return caster != null && this.mob.isAlive() && this.mob.distanceToSqr(caster) <= 2304.0 && this.chooseEscape(caster);
   }

   @Override
   public boolean canContinueToUse() {
      ServerPlayer caster = this.caster();
      return caster != null && this.mob.isAlive() && this.mob.level().getGameTime() < this.expiresAt && this.mob.distanceToSqr(caster) <= 2304.0;
   }

   @Override
   public void start() {
      this.dropCombatTarget();
      this.nextPathTick = 0L;
   }

   @Override
   public void tick() {
      ServerPlayer caster = this.caster();
      if (caster != null) {
         this.dropCombatTarget();
         long now = this.mob.level().getGameTime();
         if (this.mob.getNavigation().isDone() || now >= this.nextPathTick) {
            this.nextPathTick = now + 8L + Math.floorMod(this.mob.getId(), 4);
            this.chooseEscape(caster);
         }
      }
   }

   @Override
   public void stop() {
      this.mob.getNavigation().stop();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   private void dropCombatTarget() {
      this.mob.setTarget(null);
      this.mob.setAggressive(false);
   }

   private boolean chooseEscape(LivingEntity caster) {
      double currentDistance = this.mob.distanceToSqr(caster);

      for (int attempt = 0; attempt < 4; attempt++) {
         Vec3 escape = DefaultRandomPos.getPosAway(this.mob, 16, 7, caster.position());
         if (escape != null && escape.distanceToSqr(caster.position()) > currentDistance && this.mob.getNavigation().moveTo(escape.x, escape.y, escape.z, 1.65)
            )
          {
            return true;
         }
      }

      Vec3 away = this.mob.position().subtract(caster.position());
      away = new Vec3(away.x, 0.0, away.z);
      if (away.lengthSqr() < 1.0E-5) {
         away = new Vec3(1.0, 0.0, 0.0);
      }

      Vec3 fallback = this.mob.position().add(away.normalize().scale(12.0));
      this.mob.getMoveControl().setWantedPosition(fallback.x, fallback.y, fallback.z, 1.65);
      return true;
   }

   private ServerPlayer caster() {
      if (this.mob.level() instanceof ServerLevel level && this.mob.level().getGameTime() < this.expiresAt) {
         ServerPlayer caster = level.getServer().getPlayerList().getPlayer(this.casterId);
         return caster != null && caster.level() == level && caster.isAlive() && !caster.isSpectator() ? caster : null;
      } else {
         return null;
      }
   }
}
