package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;

public class DKCPathTeleportProcedure {
   private static final String RETURN_DIMENSION = "dkc_return_dimension";
   private static final String RETURN_YAW = "dkc_return_yaw";
   private static final String RETURN_PITCH = "dkc_return_pitch";
   private static final TagKey<EntityType<?>> SHADOWS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows"));

   public static boolean isFloorAvailable(Entity entity, int floor) {
      if (entity != null && floor >= 1 && floor <= 20) {
         SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (entity instanceof ServerPlayer player && player.server != null) {
            return (floor != 1 || vars.dkc_started || vars.dkc_cleared > 0.0) && DkcRunSavedData.get(player.server).isUnlocked(player, floor);
         } else {
            return floor == 1 ? vars.dkc_started || vars.dkc_cleared > 0.0 : vars.dkc_cleared >= floor - 1;
         }
      } else {
         return false;
      }
   }

   public static void execute(Player entity, int floor) {
      if (entity instanceof ServerPlayer player && player.server != null) {
         boolean alreadyInDkc = DkcFloorRegistry.isDkc(player.level());
         if (canTravelToFloor(player, floor, true)) {
            PointSetProcedure.execute(player);
            if (!alreadyInDkc) {
               saveReturnPosition(player);
               discardOwnedShadows(player);
            }

            DkcFloorBuilder.teleportToFloor(player, floor);
         }
      }
   }

   public static boolean canTravelToFloor(ServerPlayer player, int floor, boolean sendFeedback) {
      if (player != null && player.server != null && floor >= 1 && floor <= 20) {
         boolean alreadyInDkc = DkcFloorRegistry.isDkc(player.level());
         if (!alreadyInDkc && !player.level().dimension().equals(Level.OVERWORLD)) {
            if (sendFeedback) {
               player.displayClientMessage(Component.literal("§4The Demon King's Castle can only be entered from the Overworld."), true);
            }

            return false;
         } else if (alreadyInDkc && DkcSpatialLayout.floor(player) == floor) {
            if (sendFeedback) {
               player.displayClientMessage(Component.literal("§4You are already on that floor."), true);
            }

            return false;
         } else if (!DKCCombatTrackerProcedure.canEnterCastle(player)) {
            if (sendFeedback) {
               DKCCombatTrackerProcedure.sendCombatBlockedMessage(player);
            }

            return false;
         } else if (!isFloorAvailable(player, floor)) {
            if (sendFeedback) {
               player.displayClientMessage(Component.literal("§4That floor is still sealed."), true);
            }

            return false;
         } else {
            SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables());
            if (vars.dkc_cleared >= 20.0) {
               if (sendFeedback) {
                  player.displayClientMessage(Component.literal("§5The Demon King's Castle is already conquered. No path remains."), true);
               }

               return false;
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public static void enterRadiruCastle(ServerPlayer player) {
      if (player != null && player.server != null && DkcQuestManager.hasRadiruCastleAccess(player)) {
         if (!player.level().dimension().equals(Level.OVERWORLD)) {
            player.displayClientMessage(Component.literal("§4Radiru Castle can only be entered from the Overworld."), true);
         } else if (!DKCCombatTrackerProcedure.canEnterCastle(player)) {
            DKCCombatTrackerProcedure.sendCombatBlockedMessage(player);
         } else {
            PointSetProcedure.execute(player);
            saveReturnPosition(player);
            discardOwnedShadows(player);
            DkcFloorBuilder.teleportToFloor(player, 15);
         }
      }
   }

   public static void returnToSavedOverworld(ServerPlayer player) {
      if (player != null && player.server != null) {
         ServerLevel destination = resolveReturnLevel(player);
         if (destination == null) {
            player.displayClientMessage(Component.literal("§4The way back from the castle is lost."), true);
         } else {
            SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables());
            double targetX = vars.DunX;
            double targetY = vars.DunY;
            double targetZ = vars.DunZ;
            if (targetX == 0.0 && targetY == 0.0 && targetZ == 0.0) {
               BlockPos spawn = destination.getSharedSpawnPos();
               targetX = spawn.getX() + 0.5;
               targetY = spawn.getY() + 1.0;
               targetZ = spawn.getZ() + 0.5;
            }

            float yaw = player.getPersistentData().contains("dkc_return_yaw") ? player.getPersistentData().getFloat("dkc_return_yaw") : player.getYRot();
            float pitch = player.getPersistentData().contains("dkc_return_pitch") ? player.getPersistentData().getFloat("dkc_return_pitch") : player.getXRot();
            player.setDeltaMovement(0.0, 0.0, 0.0);
            player.fallDistance = 0.0F;
            destination.getChunk(BlockPos.containing(targetX, targetY, targetZ));
            player.teleportTo(destination, targetX, targetY, targetZ, yaw, pitch);
            player.getPersistentData().putBoolean("dkc_inside_castle", false);
            destination.playSound((Player)null, BlockPos.containing(targetX, targetY, targetZ), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.45F, 1.15F);
            player.displayClientMessage(Component.literal("§5The key drags you back to where the castle found you."), true);
         }
      }
   }

   private static ServerLevel resolveReturnLevel(ServerPlayer player) {
      String stored = player.getPersistentData().getString("dkc_return_dimension");
      ResourceLocation id = ResourceLocation.tryParse(stored);
      if (id != null) {
         ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
         if (level != null && !DkcFloorRegistry.isDkc(level)) {
            return level;
         }
      }

      return player.server.getLevel(Level.OVERWORLD);
   }

   private static void saveReturnPosition(ServerPlayer player) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.DunX = player.getX();
         capability.DunY = player.getY();
         capability.DunZ = player.getZ();
         capability.syncPlayerVariables(player);
      });
      player.getPersistentData().putString("dkc_return_dimension", player.level().dimension().location().toString());
      player.getPersistentData().putFloat("dkc_return_yaw", player.getYRot());
      player.getPersistentData().putFloat("dkc_return_pitch", player.getXRot());
   }

   public static void saveReturnPositionForDebug(ServerPlayer player) {
      if (player != null && !DkcFloorRegistry.isDkc(player.level())) {
         saveReturnPosition(player);
      }
   }

   private static void discardOwnedShadows(ServerPlayer player) {
      Vec3 center = player.position();

      for (Entity foundEntity : player.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(250.0))) {
         if (foundEntity.getType().is(SHADOWS_TAG) && foundEntity instanceof TamableAnimal tamable && tamable.isOwnedBy(player)) {
            foundEntity.discard();
         }
      }
   }
}
