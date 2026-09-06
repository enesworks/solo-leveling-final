package dev.eness.sololevelingfinal.core.dkc;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import dev.eness.sololevelingfinal.core.procedures.DKCDemonSpawnerProcedure;

public final class DkcWaveRuntime {
   private static final String SAMPLE_VALID_TAG = "dkc_progress_sample_valid";
   private static final String SAMPLE_DISTANCE_TAG = "dkc_progress_sample_distance";
   private static final String STUCK_SAMPLES_TAG = "dkc_stuck_samples";
   private static final double MIN_PROGRESS_TOWARD_OWNER = 1.0;
   private static final double MELEE_REACHABLE_SQUARED = 9.0;
   private static final double CLOSE_TO_OWNER_SQUARED = 144.0;

   private DkcWaveRuntime() {
   }

   public static void tick(Mob mob) {
      if (mob.level() instanceof ServerLevel level) {
         long gameTime = level.getGameTime();
         if (Math.floorMod(gameTime + mob.getId(), 20L) == 0L) {
            CompoundTag tag = mob.getPersistentData();
            if ("floor_wave".equals(tag.getString("dkc_encounter_role"))) {
               int floor = (int)tag.getDouble("dkc_floor_number");
               ServerPlayer owner = owner(level, tag.getString("dkc_spawned_by"));
               if (floor >= 2
                  && floor <= 19
                  && owner != null
                  && owner.serverLevel() == level
                  && DkcSpatialLayout.isPlayerInFloor(owner, floor)
                  && DKCDemonSpawnerProcedure.isCurrentWaveMob(mob, owner, floor)
                  && !owner.getPersistentData().getBoolean("dkc_floor_" + floor + "_complete")) {
                  if (mob.getTarget() != owner) {
                     mob.setTarget(owner);
                  }

                  boolean ownerInsideCombatArea = DkcFloorBuilder.isInsideCombatArea(owner, floor, owner.blockPosition());
                  if (!DkcFloorBuilder.isInsideCombatArea(owner, floor, mob.blockPosition())) {
                     if (ownerInsideCombatArea) {
                        recoverOrDiscard(level, owner, floor, mob);
                     } else {
                        clearSamples(tag);
                     }
                  } else if (!ownerInsideCombatArea) {
                     clearSamples(tag);
                  } else if (Math.floorMod(gameTime + mob.getId(), 100L) == 0L) {
                     tickProgress(level, owner, floor, mob, tag);
                  }
               } else {
                  mob.discard();
               }
            }
         }
      }
   }

   private static void tickProgress(ServerLevel level, ServerPlayer owner, int floor, Mob mob, CompoundTag tag) {
      double distanceSquared = mob.distanceToSqr(owner);
      double distance = Math.sqrt(distanceSquared);
      boolean scheduledAudit = Math.floorMod(level.getGameTime() + mob.getId(), 300L) == 0L;
      if (distanceSquared <= 9.0 && mob.hasLineOfSight(owner)) {
         storeSample(tag, distance, 0);
      } else {
         if (distanceSquared <= 144.0) {
            storeSample(tag, distance, 0);
            if (!scheduledAudit) {
               return;
            }
         } else if (!tag.getBoolean("dkc_progress_sample_valid")) {
            storeSample(tag, distance, 0);
            if (!scheduledAudit) {
               return;
            }
         } else {
            double progressTowardOwner = tag.getDouble("dkc_progress_sample_distance") - distance;
            int stuckSamples = progressTowardOwner < 1.0 ? tag.getInt("dkc_stuck_samples") + 1 : 0;
            storeSample(tag, distance, stuckSamples);
            if (stuckSamples < 2 && !scheduledAudit) {
               return;
            }
         }

         Path path = mob.getNavigation().createPath(owner.blockPosition(), 0);
         if (path != null && path.canReach()) {
            storeSample(tag, distance, 0);
         } else {
            recoverOrDiscard(level, owner, floor, mob);
         }
      }
   }

   private static void recoverOrDiscard(ServerLevel level, ServerPlayer owner, int floor, Mob mob) {
      clearSamples(mob.getPersistentData());
      if (!DkcFloorBuilder.recoverWaveMob(level, owner, floor, mob)) {
         mob.discard();
      }
   }

   private static void storeSample(CompoundTag tag, double distance, int stuckSamples) {
      tag.putBoolean("dkc_progress_sample_valid", true);
      tag.putDouble("dkc_progress_sample_distance", distance);
      tag.putInt("dkc_stuck_samples", Math.max(0, stuckSamples));
   }

   private static void clearSamples(CompoundTag tag) {
      tag.remove("dkc_progress_sample_valid");
      tag.remove("dkc_progress_sample_distance");
      tag.remove("dkc_stuck_samples");
   }

   private static ServerPlayer owner(ServerLevel level, String ownerText) {
      try {
         return level.getServer().getPlayerList().getPlayer(UUID.fromString(ownerText));
      } catch (IllegalArgumentException exception) {
         return null;
      }
   }
}
