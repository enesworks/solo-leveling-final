package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;
import dev.eness.sololevelingfinal.core.entity.StatueOfGodEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class StatueOfGodOnEntityTickUpdateProcedure {
   private static final String AGGRESSIVE_STATE = "aggresive";
   private static final String WAKING_STATE = "waking";
   private static final String THRONE_STATE = "throne";
   private static final int PLAYER_SCAN_INTERVAL = 10;
   private static final int WAKE_ANIMATION_TICKS = 72;
   private static final double CHASE_SPEED = 1.2;
   private static final double DIRECT_CHASE_SPEED = 0.22;
   private static final double STOP_CHASING_DISTANCE_SQR = 36.0;
   private static final double ACTIVATION_RANGE_SQR = 1024.0;
   private static final double LEASH_RANGE_SQR = 9216.0;
   private static final double STORY_ACTIVATION_RANGE_SQR = 16384.0;
   private static final double STORY_TARGET_SCAN_RANGE = 256.0;
   private static final double STORY_MELEE_RANGE = 2.0;
   private static final int STORY_MELEE_COOLDOWN_TICKS = 20;
   private static final String STORY_STATUE_TAG = "slr_story_intro_statue";
   private static final String STORY_INSTANCE_TAG = "slr_story_intro_instance";
   private static final String STORY_OWNER_TAG = "slr_story_intro_owner";
   private static final String STORY_LASER_DONE_TAG = "slr_story_intro_laser_done";
   private static final String STORY_ACTIVATION_AT_TAG = "slr_story_intro_activation_at";
   private static final String STORY_HUNTER_TAG = "slr_story_intro_hunter";
   private static final String STORY_NEXT_MELEE_AT_TAG = "slr_story_intro_next_melee_at";

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (world instanceof ServerLevel level && entity instanceof StatueOfGodEntity statue && statue.isAlive()) {
         CompoundTag data = statue.getPersistentData();
         String state = data.getString("state");
         if (!state.isEmpty() && !state.equals(statue.getEntityData().get(StatueOfGodEntity.DATA_state))) {
            statue.getEntityData().set(StatueOfGodEntity.DATA_state, state);
         }

         if (data.getBoolean("slr_story_intro_statue")) {
            tickStoryIntro(level, statue, data, state);
         } else if ("waking".equals(state)) {
            tickWaking(level, statue, data);
         } else if (!"aggresive".equals(state)) {
            if (!statue.isNoAi()) {
               statue.setNoAi(true);
            }

            if (shouldScan(statue)) {
               ServerPlayer player = findNearestPlayer(level, statue, 1024.0, true);
               if (player != null) {
                  activate(statue, player);
               }
            }
         } else {
            if (statue.isNoAi()) {
               statue.setNoAi(false);
            }

            if (shouldScan(statue)) {
               ServerPlayer nearest = findNearestPlayer(level, statue, 9216.0, false);
               if (nearest == null) {
                  resetToThrone(statue);
                  return;
               }

               LivingEntity currentTarget = statue.getTarget();
               if (!isValidPlayer(currentTarget) || statue.distanceToSqr(currentTarget) > 9216.0) {
                  statue.setTarget(nearest);
               }

               statue.getNavigation().moveTo(statue.getTarget(), 1.2);
            }

            LivingEntity target = statue.getTarget();
            if (isValidPlayer(target)) {
               statue.faceTarget(target);
               chaseTarget(statue, target);
            }
         }
      }
   }

   private static void tickStoryIntro(ServerLevel level, StatueOfGodEntity statue, CompoundTag data, String state) {
      boolean activationReady = isStoryActivationReady(level, data);
      if (activationReady || !"waking".equals(state) && !"aggresive".equals(state)) {
         if ("waking".equals(state)) {
            tickStoryWaking(level, statue, data);
         } else if (!"aggresive".equals(state)) {
            holdStill(statue);
            if (activationReady && shouldScan(statue)) {
               ServerPlayer owner = findStoryOwner(level, data);
               if (owner != null && owner.isShiftKeyDown() && statue.distanceToSqr(owner) <= 16384.0) {
                  LivingEntity target = findStoryTarget(level, statue, data);
                  if (target != null) {
                     activate(statue, target);
                  }
               }
            }
         } else {
            LivingEntity target = findStoryTarget(level, statue, data);
            if (target == null) {
               resetToThrone(statue);
            } else {
               if (statue.isNoAi()) {
                  statue.setNoAi(false);
               }

               if (statue.getTarget() != target) {
                  statue.setTarget(target);
               }

               statue.faceTarget(target);
               chaseStoryTarget(statue, target);
            }
         }
      } else {
         resetToThrone(statue);
      }
   }

   private static void tickStoryWaking(ServerLevel level, StatueOfGodEntity statue, CompoundTag data) {
      holdStill(statue);
      if ("empty".equals(statue.animationprocedure) && "undefined".equals(statue.getSyncedAnimation())) {
         statue.setAnimation("standing and smiling");
      }

      LivingEntity target = findStoryTarget(level, statue, data);
      if (target == null) {
         resetToThrone(statue);
      } else {
         if (statue.getTarget() != target) {
            statue.setTarget(target);
         }

         statue.faceTarget(target);
         int wakeTicks = data.getInt("IA") + 1;
         data.putInt("IA", wakeTicks);
         if (wakeTicks >= 72) {
            statue.getEntityData().set(StatueOfGodEntity.DATA_story_upright, true);
            data.putString("state", "aggresive");
            data.putInt("IA", 0);
            statue.getEntityData().set(StatueOfGodEntity.DATA_state, "aggresive");
            statue.setNoAi(false);
            statue.setTarget(target);
            chaseStoryTarget(statue, target);
         }
      }
   }

   private static boolean isStoryActivationReady(ServerLevel level, CompoundTag data) {
      return data.getBoolean("slr_story_intro_laser_done")
         && data.contains("slr_story_intro_activation_at", 4)
         && level.getGameTime() >= data.getLong("slr_story_intro_activation_at");
   }

   private static LivingEntity findStoryTarget(ServerLevel level, StatueOfGodEntity statue, CompoundTag data) {
      LivingEntity currentTarget = statue.getTarget();
      int instance = data.getInt("slr_story_intro_instance");
      if (currentTarget instanceof HunterEntity currentHunter && isMatchingStoryHunter(currentHunter, instance)) {
         return currentHunter;
      } else {
         HunterEntity nearestHunter = null;
         double nearestDistance = Double.MAX_VALUE;

         for (HunterEntity hunter : level.getEntitiesOfClass(
            HunterEntity.class, statue.getBoundingBox().inflate(256.0), candidate -> isMatchingStoryHunter(candidate, instance)
         )) {
            double distance = statue.distanceToSqr(hunter);
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearestHunter = hunter;
            }
         }

         return nearestHunter != null ? nearestHunter : findStoryOwner(level, data);
      }
   }

   private static void chaseStoryTarget(StatueOfGodEntity statue, LivingEntity target) {
      if (!statue.getNavigation().isDone()) {
         statue.getNavigation().stop();
      }

      statue.faceTarget(target);
      Vec3 movement = statue.getDeltaMovement();
      double surfaceDistance = CombatRangeHelper.horizontalSurfaceDistance(statue, target);
      if (surfaceDistance <= 2.0) {
         statue.setDeltaMovement(movement.x * 0.2, movement.y(), movement.z * 0.2);
         tryStoryMeleeAttack(statue, target);
      } else {
         double dx = target.getX() - statue.getX();
         double dz = target.getZ() - statue.getZ();
         double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
         if (!(horizontalDistance <= 1.0E-5)) {
            statue.setDeltaMovement(dx / horizontalDistance * 0.22, movement.y(), dz / horizontalDistance * 0.22);
            statue.hasImpulse = true;
         }
      }
   }

   private static void tryStoryMeleeAttack(StatueOfGodEntity statue, LivingEntity target) {
      CompoundTag data = statue.getPersistentData();
      long gameTime = statue.level().getGameTime();
      if (gameTime >= data.getLong("slr_story_intro_next_melee_at")) {
         data.putLong("slr_story_intro_next_melee_at", gameTime + 20L);
         statue.swing(InteractionHand.MAIN_HAND);
         statue.doHurtTarget(target);
      }
   }

   private static boolean isMatchingStoryHunter(HunterEntity hunter, int instance) {
      CompoundTag hunterData = hunter.getPersistentData();
      return hunter.isAlive()
         && !hunter.isRemoved()
         && hunterData.getBoolean("slr_story_intro_hunter")
         && hunterData.getInt("slr_story_intro_instance") == instance;
   }

   private static ServerPlayer findStoryOwner(ServerLevel level, CompoundTag data) {
      if (!data.hasUUID("slr_story_intro_owner")) {
         return null;
      } else {
         return level.getPlayerByUUID(data.getUUID("slr_story_intro_owner")) instanceof ServerPlayer owner && isValidPlayer(owner) ? owner : null;
      }
   }

   private static void tickWaking(ServerLevel level, StatueOfGodEntity statue, CompoundTag data) {
      holdStill(statue);
      LivingEntity target = statue.getTarget();
      if (!isValidPlayer(target) || statue.distanceToSqr(target) > 9216.0) {
         target = findNearestPlayer(level, statue, 9216.0, false);
         if (target == null) {
            resetToThrone(statue);
            return;
         }

         statue.setTarget(target);
      }

      statue.faceTarget(target);
      int wakeTicks = data.getInt("IA") + 1;
      data.putInt("IA", wakeTicks);
      if (wakeTicks >= 72) {
         data.putString("state", "aggresive");
         data.putInt("IA", 0);
         statue.getEntityData().set(StatueOfGodEntity.DATA_state, "aggresive");
         statue.setNoAi(false);
         statue.setTarget(target);
         statue.getNavigation().moveTo(target, 1.2);
         chaseTarget(statue, target);
      }
   }

   private static void holdStill(StatueOfGodEntity statue) {
      statue.setNoAi(true);
      statue.getNavigation().stop();
      statue.setDeltaMovement(Vec3.ZERO);
      statue.fallDistance = 0.0F;
   }

   private static void chaseTarget(StatueOfGodEntity statue, LivingEntity target) {
      double dx = target.getX() - statue.getX();
      double dz = target.getZ() - statue.getZ();
      double horizontalDistanceSqr = dx * dx + dz * dz;
      if (!(horizontalDistanceSqr <= 36.0)) {
         statue.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.2);
         Vec3 movement = statue.getDeltaMovement();
         if (!(movement.horizontalDistanceSqr() > 1.0E-5)) {
            double horizontalDistance = Math.sqrt(horizontalDistanceSqr);
            statue.setDeltaMovement(dx / horizontalDistance * 0.22, movement.y(), dz / horizontalDistance * 0.22);
         }
      }
   }

   private static boolean shouldScan(StatueOfGodEntity statue) {
      return Math.floorMod(statue.tickCount + statue.getId(), 10) == 0;
   }

   private static ServerPlayer findNearestPlayer(ServerLevel level, StatueOfGodEntity statue, double rangeSqr, boolean requireBowing) {
      ServerPlayer nearest = null;
      double nearestDistance = rangeSqr;

      for (ServerPlayer player : level.players()) {
         if (isValidPlayer(player) && (!requireBowing || player.isShiftKeyDown())) {
            double distance = statue.distanceToSqr(player);
            if (distance <= nearestDistance) {
               nearestDistance = distance;
               nearest = player;
            }
         }
      }

      return nearest;
   }

   private static boolean isValidPlayer(LivingEntity entity) {
      return entity instanceof ServerPlayer player && player.isAlive() && !player.isCreative() && !player.isSpectator();
   }

   private static void activate(StatueOfGodEntity statue, LivingEntity target) {
      statue.getEntityData().set(StatueOfGodEntity.DATA_story_upright, false);
      statue.getPersistentData().remove("slr_story_intro_next_melee_at");
      statue.getPersistentData().putString("state", "waking");
      statue.getPersistentData().putInt("IA", 0);
      statue.getEntityData().set(StatueOfGodEntity.DATA_state, "waking");
      statue.setNoAi(true);
      statue.setTarget(target);
      statue.faceTarget(target);
      statue.getNavigation().stop();
      statue.setDeltaMovement(Vec3.ZERO);
      statue.setAnimation("standing and smiling");
   }

   private static void resetToThrone(StatueOfGodEntity statue) {
      CompoundTag data = statue.getPersistentData();
      statue.getEntityData().set(StatueOfGodEntity.DATA_story_upright, false);
      data.remove("slr_story_intro_next_melee_at");
      data.putString("state", "throne");
      data.putInt("IA", 0);
      statue.getEntityData().set(StatueOfGodEntity.DATA_state, "throne");
      statue.setNoAi(true);
      statue.setTarget(null);
      statue.getNavigation().stop();
      statue.setDeltaMovement(Vec3.ZERO);
      statue.fallDistance = 0.0F;
      int homeX = statue.getEntityData().get(StatueOfGodEntity.DATA_default_x);
      int homeY = statue.getEntityData().get(StatueOfGodEntity.DATA_default_y);
      int homeZ = statue.getEntityData().get(StatueOfGodEntity.DATA_default_z);
      statue.teleportTo(homeX + 0.5, homeY, homeZ + 0.5);
      statue.setXRot(0.0F);
      statue.xRotO = 0.0F;
      statue.faceYaw(data.contains("CartenonHomeYaw") ? data.getFloat("CartenonHomeYaw") : 180.0F);
   }
}
