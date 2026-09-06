package dev.eness.sololevelingfinal.core.util.daily;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

public final class DailyPunishmentManager {
   public static final double PUNISHMENT_SECONDS = 120.0;
   public static final ResourceKey<Level> SURVIVAL_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling", "survival_dimension")
   );
   private static final String RETURN_X = "punX";
   private static final String RETURN_Y = "punY";
   private static final String RETURN_Z = "punZ";
   private static final String RETURN_YAW = "slrPunYaw";
   private static final String RETURN_PITCH = "slrPunPitch";
   private static final String RETURN_DIMENSION = "slrPunDimension";
   private static final String ANCHOR_X = "slrPunSafeX";
   private static final String ANCHOR_Y = "slrPunSafeY";
   private static final String ANCHOR_Z = "slrPunSafeZ";
   private static final int SURFACE_SEARCH_RADIUS = 12;
   private static final int EMERGENCY_PLATFORM_RADIUS = 4;
   private static final int VOID_RECOVERY_CLEARANCE = 4;

   private DailyPunishmentManager() {
   }

   public static boolean enter(ServerPlayer player) {
      if (player != null && player.server != null) {
         ServerLevel punishmentLevel = player.server.getLevel(SURVIVAL_DIMENSION);
         if (punishmentLevel == null) {
            return false;
         }

         int targetX = Mth.floor(player.getX());
         int targetZ = Mth.floor(player.getZ());
         BlockPos arrival = prepareSafeArrival(punishmentLevel, targetX, targetZ);
         rememberReturnPoint(player);
         rememberAnchor(player, arrival);
         setPunishmentState(player, 120.0, false);
         teleport(player, punishmentLevel, arrival.getX() + 0.5, arrival.getY(), arrival.getZ() + 0.5, player.getYRot(), player.getXRot());
         SystemNotifications.showNegativeTitleUnder(
            player,
            -49859,
            100,
            Component.literal("PENALTY QUEST").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
            Component.literal("Survive for 120 seconds.").withStyle(ChatFormatting.RED)
         );
         return player.level().dimension() == SURVIVAL_DIMENSION;
      } else {
         return false;
      }
   }

   public static void tick(ServerPlayer player) {
      if (player != null && player.level().dimension() == SURVIVAL_DIMENSION) {
         SololevelingModVariables.PlayerVariables variables = variables(player);
         if (variables != null) {
            if (variables.punishment > 120.0) {
               setPunishmentState(player, 120.0, variables.giftstatus);
               variables = variables(player);
               if (variables == null) {
                  return;
               }
            }

            if (variables.punishment > 0.0) {
               rescueFromVoid(player);
            }

            if (player.serverLevel().getGameTime() % 20L == 0L) {
               if (variables.punishment <= 0.0) {
                  finish(player);
               } else {
                  double remaining = Math.max(0.0, variables.punishment - 1.0);
                  setPunishmentState(player, remaining, variables.giftstatus);
                  int wholeSeconds = (int)Math.ceil(remaining);
                  if (wholeSeconds == 110 || wholeSeconds == 90 || wholeSeconds == 70) {
                     spawnCentipede(player);
                  }

                  if (remaining <= 0.0) {
                     finish(player);
                  }
               }
            }
         }
      }
   }

   public static boolean rescueFromVoid(ServerPlayer player) {
      if (player != null && player.level().dimension() == SURVIVAL_DIMENSION) {
         ServerLevel level = player.serverLevel();
         double rescueY = level.getMinBuildHeight() + 4;
         if (Double.isFinite(player.getX()) && Double.isFinite(player.getY()) && Double.isFinite(player.getZ()) && player.getY() >= rescueY) {
            return false;
         }

         BlockPos anchor = readAnchor(player);
         if (anchor == null || !isSafeStandingPosition(level, anchor)) {
            int targetX = anchor == null ? Mth.floor(player.getX()) : anchor.getX();
            int targetZ = anchor == null ? Mth.floor(player.getZ()) : anchor.getZ();
            if (!Double.isFinite(player.getX()) || !Double.isFinite(player.getZ())) {
               targetX = 0;
               targetZ = 0;
            }

            anchor = prepareSafeArrival(level, targetX, targetZ);
            rememberAnchor(player, anchor);
         }

         teleport(player, level, anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, player.getYRot(), player.getXRot());
         player.displayClientMessage(Component.literal("The System restored you to the punishment arena.").withStyle(ChatFormatting.RED), true);
         return true;
      } else {
         return false;
      }
   }

   private static BlockPos prepareSafeArrival(ServerLevel level, int targetX, int targetZ) {
      level.getChunk(targetX >> 4, targetZ >> 4);
      BlockPos exact = safeSurfaceAt(level, targetX, targetZ);
      if (exact != null) {
         return exact;
      }

      for (int radius = 1; radius <= 12; radius++) {
         for (int offset = -radius; offset <= radius; offset++) {
            BlockPos candidate = safeSurfaceAt(level, targetX + offset, targetZ - radius);
            if (candidate != null) {
               return candidate;
            }

            candidate = safeSurfaceAt(level, targetX + offset, targetZ + radius);
            if (candidate != null) {
               return candidate;
            }
         }

         for (int offset = -radius + 1; offset < radius; offset++) {
            BlockPos candidate = safeSurfaceAt(level, targetX - radius, targetZ + offset);
            if (candidate != null) {
               return candidate;
            }

            candidate = safeSurfaceAt(level, targetX + radius, targetZ + offset);
            if (candidate != null) {
               return candidate;
            }
         }
      }

      return createEmergencyPlatform(level, targetX, targetZ);
   }

   @Nullable
   private static BlockPos safeSurfaceAt(ServerLevel level, int x, int z) {
      int minimum = level.getMinBuildHeight() + 1;
      int maximum = level.getMaxBuildHeight() - 2;
      int surface = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
      if (surface >= minimum && surface <= maximum) {
         for (int offset = 0; offset <= 4; offset++) {
            BlockPos candidate = new BlockPos(x, surface + offset, z);
            if (candidate.getY() <= maximum && isSafeStandingPosition(level, candidate)) {
               return candidate;
            }
         }

         for (int offset = 1; offset <= 12; offset++) {
            BlockPos candidate = new BlockPos(x, surface - offset, z);
            if (candidate.getY() >= minimum && isSafeStandingPosition(level, candidate)) {
               return candidate;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean isSafeStandingPosition(ServerLevel level, BlockPos feet) {
      if (feet.getY() > level.getMinBuildHeight() && feet.getY() < level.getMaxBuildHeight() - 1) {
         BlockPos floorPos = feet.below();
         BlockState floor = level.getBlockState(floorPos);
         return floor.isFaceSturdy(level, floorPos, Direction.UP) && canOccupy(level, feet) && canOccupy(level, feet.above());
      } else {
         return false;
      }
   }

   private static boolean canOccupy(ServerLevel level, BlockPos position) {
      BlockState state = level.getBlockState(position);
      return state.getCollisionShape(level, position).isEmpty()
         && state.getFluidState().isEmpty()
         && !state.is(Blocks.POWDER_SNOW)
         && !state.is(Blocks.FIRE)
         && !state.is(Blocks.SOUL_FIRE);
   }

   private static BlockPos createEmergencyPlatform(ServerLevel level, int x, int z) {
      int minimumFloor = level.getMinBuildHeight() + 8;
      int maximumFloor = level.getMaxBuildHeight() - 4;
      int surface = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
      int preferredFloor = surface > level.getMinBuildHeight() + 1 ? surface - 1 : Math.max(64, level.getSeaLevel());
      int floorY = Mth.clamp(preferredFloor, minimumFloor, maximumFloor);

      for (int dx = -4; dx <= 4; dx++) {
         for (int dz = -4; dz <= 4; dz++) {
            level.setBlock(new BlockPos(x + dx, floorY, z + dz), Blocks.RED_SANDSTONE.defaultBlockState(), 3);
         }
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 1; dy <= 3; dy++) {
               level.setBlock(new BlockPos(x + dx, floorY + dy, z + dz), Blocks.AIR.defaultBlockState(), 3);
            }
         }
      }

      return new BlockPos(x, floorY + 1, z);
   }

   private static void spawnCentipede(ServerPlayer player) {
      ServerLevel level = player.serverLevel();
      BlockPos position = player.blockPosition().offset(3, 0, 3);
      Entity spawned = SololevelingModEntities.CENTIPEDE.get().spawn(level, position, MobSpawnType.MOB_SUMMONED);
      if (spawned != null) {
         spawned.fallDistance = 0.0F;
      }
   }

   private static void finish(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      ServerLevel destination = returnLevel(player, data);
      double x;
      double y;
      double z;
      if (hasReturnCoordinates(data)) {
         x = data.getDouble("punX");
         y = data.getDouble("punY");
         z = data.getDouble("punZ");
      } else {
         BlockPos spawn = destination.getSharedSpawnPos();
         x = spawn.getX() + 0.5;
         y = destination.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
         z = spawn.getZ() + 0.5;
      }

      if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
         BlockPos spawn = destination.getSharedSpawnPos();
         x = spawn.getX() + 0.5;
         y = destination.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
         z = spawn.getZ() + 0.5;
      }

      y = Mth.clamp(y, destination.getMinBuildHeight() + 1.0, destination.getMaxBuildHeight() - 2.0);
      destination.getChunk(Mth.floor(x) >> 4, Mth.floor(z) >> 4);
      float yaw = data.contains("slrPunYaw", 5) ? data.getFloat("slrPunYaw") : player.getYRot();
      float pitch = data.contains("slrPunPitch", 5) ? data.getFloat("slrPunPitch") : player.getXRot();
      teleport(player, destination, x, y, z, yaw, pitch);
      setPunishmentState(player, 0.0, true);
      clearTravelData(data);
      SystemNotifications.showTitleUnder(
         player,
         -10158198,
         80,
         Component.literal("SURVIVED!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
         Component.literal("Punishment complete.").withStyle(ChatFormatting.GRAY)
      );
   }

   private static ServerLevel returnLevel(ServerPlayer player, CompoundTag data) {
      if (data.contains("slrPunDimension", 8)) {
         ResourceLocation location = ResourceLocation.tryParse(data.getString("slrPunDimension"));
         if (location != null) {
            ServerLevel stored = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, location));
            if (stored != null && stored.dimension() != SURVIVAL_DIMENSION) {
               return stored;
            }
         }
      }

      return player.server.overworld();
   }

   private static void rememberReturnPoint(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      data.putDouble("punX", player.getX());
      data.putDouble("punY", player.getY());
      data.putDouble("punZ", player.getZ());
      data.putFloat("slrPunYaw", player.getYRot());
      data.putFloat("slrPunPitch", player.getXRot());
      data.putString("slrPunDimension", player.level().dimension().location().toString());
   }

   private static boolean hasReturnCoordinates(CompoundTag data) {
      return data.contains("punX", 6) && data.contains("punY", 6) && data.contains("punZ", 6);
   }

   private static void rememberAnchor(ServerPlayer player, BlockPos anchor) {
      CompoundTag data = player.getPersistentData();
      data.putInt("slrPunSafeX", anchor.getX());
      data.putInt("slrPunSafeY", anchor.getY());
      data.putInt("slrPunSafeZ", anchor.getZ());
   }

   @Nullable
   private static BlockPos readAnchor(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      return data.contains("slrPunSafeX", 3) && data.contains("slrPunSafeY", 3) && data.contains("slrPunSafeZ", 3)
         ? new BlockPos(data.getInt("slrPunSafeX"), data.getInt("slrPunSafeY"), data.getInt("slrPunSafeZ"))
         : null;
   }

   private static void clearTravelData(CompoundTag data) {
      for (String key : new String[]{"punX", "punY", "punZ", "slrPunYaw", "slrPunPitch", "slrPunDimension", "slrPunSafeX", "slrPunSafeY", "slrPunSafeZ"}) {
         data.remove(key);
      }
   }

   private static void setPunishmentState(ServerPlayer player, double remaining, boolean giftStatus) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.punishment = remaining;
         capability.giftstatus = giftStatus;
         capability.syncPlayerVariables(player);
      });
   }

   private static void teleport(ServerPlayer player, ServerLevel destination, double x, double y, double z, float yaw, float pitch) {
      boolean dimensionChange = player.serverLevel() != destination;
      player.stopRiding();
      player.setDeltaMovement(Vec3.ZERO);
      player.fallDistance = 0.0F;
      if (dimensionChange) {
         player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0.0F));
         player.teleportTo(destination, x, y, z, yaw, pitch);
         player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));

         for (MobEffectInstance effect : player.getActiveEffects()) {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effect));
         }

         player.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
      } else {
         player.connection.teleport(x, y, z, yaw, pitch);
      }

      player.setDeltaMovement(Vec3.ZERO);
      player.fallDistance = 0.0F;
      player.setOnGround(true);
   }

   @Nullable
   private static SololevelingModVariables.PlayerVariables variables(ServerPlayer player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
   }
}
