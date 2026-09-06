package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.StatueOfGodEntity;
import dev.eness.sololevelingfinal.core.entity.StatueaxeEntity;
import dev.eness.sololevelingfinal.core.entity.StatuehammerEntity;
import dev.eness.sololevelingfinal.core.entity.StatueswordEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

@EventBusSubscriber(modid = "sololeveling")
public final class CartenonTempleGenerator {
   private static final int HALF_WIDTH = 77;
   private static final int MAX_Z = 154;
   private static final int ROOM_CENTER_Z = 77;
   private static final double ROOM_RADIUS = 77.0;
   private static final double INNER_WALL_RADIUS = 75.0;
   private static final int MIN_Y = -3;
   private static final int MAX_Y = 52;
   private static final int BLOCK_CHECKS_PER_TICK = 4096;
   private static final int BLOCK_CHANGES_PER_TICK = 512;
   private static final int[] STATUE_X = new int[]{31, 49, 58, 59, 51, 32};
   private static final int[] STATUE_Z = new int[]{25, 43, 63, 86, 108, 128};
   private static final int[] COLUMN_X = new int[]{19, 33, 38, 33, 19, -19, -33, -38, -33, -19};
   private static final int[] COLUMN_Z = new int[]{44, 58, 77, 96, 110, 110, 96, 77, 58, 44};
   private static final int[] BRAZIER_Z = new int[]{30, 54, 78, 102, 122};
   private static final int[][] GOD_LIGHTS = new int[][]{
      {0, 22, 140, 9},
      {-4, 21, 141, 9},
      {4, 21, 141, 9},
      {0, 19, 140, 9},
      {-6, 18, 141, 8},
      {6, 18, 141, 8},
      {0, 16, 139, 9},
      {-5, 15, 140, 8},
      {5, 15, 140, 8},
      {0, 13, 139, 8},
      {-7, 11, 140, 8},
      {7, 11, 140, 8},
      {0, 8, 138, 8}
   };
   private static final Map<CartenonTempleGenerator.BuildKey, CartenonTempleGenerator.BuildJob> ACTIVE_BUILDS = new LinkedHashMap<>();

   private CartenonTempleGenerator() {
   }

   public static boolean start(ServerPlayer target) {
      ServerLevel level = target.serverLevel();
      if (ACTIVE_BUILDS.keySet().stream().anyMatch(key -> key.dimension().equals(level.dimension()))) {
         target.sendSystemMessage(Component.literal("A Cartenon Temple is already being built in this dimension.").withStyle(ChatFormatting.RED));
         return false;
      }

      Direction forward = target.getDirection();
      if (forward.getAxis().isVertical()) {
         forward = Direction.NORTH;
      }

      BlockPos entranceCenter = target.blockPosition().relative(forward, 14).below();
      if (entranceCenter.getY() + -3 >= level.getMinBuildHeight() && entranceCenter.getY() + 52 + 2 < level.getMaxBuildHeight()) {
         if (!startAt(level, entranceCenter, forward, target.getUUID(), target.getGameProfile().getName(), false, null)) {
            return false;
         }

         target.sendSystemMessage(
            Component.literal("Cartenon Temple construction started: 155-block circular chamber, facing " + forward.getName() + ".")
               .withStyle(ChatFormatting.AQUA)
         );
         target.sendSystemMessage(Component.literal("The temple is being placed gradually to protect server TPS.").withStyle(ChatFormatting.DARK_GRAY));
         return true;
      } else {
         target.sendSystemMessage(Component.literal("There is not enough vertical build space for the Cartenon Temple here.").withStyle(ChatFormatting.RED));
         return false;
      }
   }

   public static boolean startAt(
      ServerLevel level, BlockPos origin, Direction forward, UUID ownerId, String ownerName, boolean sealedEntrance, Runnable onComplete
   ) {
      if (level != null && origin != null && forward != null && !forward.getAxis().isVertical()) {
         if (origin.getY() + -3 >= level.getMinBuildHeight() && origin.getY() + 52 + 2 < level.getMaxBuildHeight()) {
            CartenonTempleGenerator.BuildKey key = new CartenonTempleGenerator.BuildKey(level.dimension(), origin.immutable());
            if (ACTIVE_BUILDS.containsKey(key)) {
               return false;
            }

            CartenonTempleGenerator.BuildJob job = new CartenonTempleGenerator.BuildJob(
               level, origin.immutable(), forward, ownerId, ownerName == null ? "unknown" : ownerName, sealedEntrance, onComplete
            );
            ACTIVE_BUILDS.put(key, job);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isBuildingAt(ServerLevel level, BlockPos origin) {
      return level != null && origin != null && ACTIVE_BUILDS.containsKey(new CartenonTempleGenerator.BuildKey(level.dimension(), origin.immutable()));
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END && !ACTIVE_BUILDS.isEmpty()) {
         int activeCount = Math.max(1, ACTIVE_BUILDS.size());
         int checksPerJob = Math.max(1024, 4096 / activeCount);
         int changesPerJob = Math.max(128, 512 / activeCount);
         List<CartenonTempleGenerator.BuildKey> completed = new ArrayList<>();

         for (Entry<CartenonTempleGenerator.BuildKey, CartenonTempleGenerator.BuildJob> entry : new ArrayList<>(ACTIVE_BUILDS.entrySet())) {
            CartenonTempleGenerator.BuildJob job = entry.getValue();
            if (job.tick(checksPerJob, changesPerJob)) {
               completed.add(entry.getKey());
            }
         }

         completed.forEach(ACTIVE_BUILDS::remove);
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      ACTIVE_BUILDS.clear();
   }

   private static final class BuildJob {
      private final ServerLevel level;
      private final BlockPos origin;
      private final Direction forward;
      private final Direction right;
      private final UUID ownerId;
      private final String ownerName;
      private final boolean sealedEntrance;
      private final Runnable onComplete;
      private final long totalChecks;
      private int localX = -77;
      private int localZ;
      private int localY = -3;
      private long checked;
      private long changed;
      private int lastProgress = -1;
      private boolean blockPassFinished;

      private BuildJob(ServerLevel level, BlockPos origin, Direction forward, UUID ownerId, String ownerName, boolean sealedEntrance, Runnable onComplete) {
         this.level = level;
         this.origin = origin;
         this.forward = forward;
         this.right = forward.getClockWise();
         this.ownerId = ownerId;
         this.ownerName = ownerName;
         this.sealedEntrance = sealedEntrance;
         this.onComplete = onComplete;
         this.totalChecks = 1345400L;
      }

      private boolean tick(int checkBudget, int changeBudget) {
         if (this.blockPassFinished) {
            return this.finish();
         }

         int checksThisTick = 0;
         int changesThisTick = 0;

         while (!this.blockPassFinished && checksThisTick < checkBudget && changesThisTick < changeBudget) {
            BlockState desired = this.desiredState(this.localX, this.localY, this.localZ);
            if (desired != null) {
               BlockPos worldPos = this.toWorld(this.localX, this.localY, this.localZ);
               BlockState existing = this.level.getBlockState(worldPos);
               if (!existing.equals(desired)) {
                  boolean placed = this.level.setBlock(worldPos, desired, 18);
                  if (!placed && !this.level.getBlockState(worldPos).equals(desired)) {
                     return this.abort("block write was rejected at " + worldPos.toShortString(), null);
                  }

                  changesThisTick++;
                  this.changed++;
               }
            }

            checksThisTick++;
            this.checked++;
            this.advanceCursor();
         }

         this.reportProgress();
         return false;
      }

      private void advanceCursor() {
         this.localX++;
         if (this.localX > 77) {
            this.localX = -77;
            this.localZ++;
            if (this.localZ > 154) {
               this.localZ = 0;
               this.localY++;
               if (this.localY > 52) {
                  this.blockPassFinished = true;
               }
            }
         }
      }

      private void reportProgress() {
         int progress = (int)Math.min(100L, this.checked * 100L / this.totalChecks);
         int step = progress / 10;
         if (step > this.lastProgress) {
            this.lastProgress = step;
            ServerPlayer owner = this.ownerId == null ? null : this.level.getServer().getPlayerList().getPlayer(this.ownerId);
            if (owner != null) {
               owner.displayClientMessage(Component.literal("Cartenon Temple  " + Math.min(100, step * 10) + "%").withStyle(ChatFormatting.DARK_AQUA), true);
            }
         }
      }

      private boolean finish() {
         this.removePreviousTempleStatues();
         this.spawnTempleStatues();
         BlockPos completionMarker = this.toWorld(0, -2, 0);
         boolean markerPlaced = this.level.setBlock(completionMarker, Blocks.LODESTONE.defaultBlockState(), 2);
         if (!markerPlaced && !this.level.getBlockState(completionMarker).is(Blocks.LODESTONE)) {
            return this.abort("completion marker write was rejected at " + completionMarker.toShortString(), null);
         }

         BlockPos center = this.toWorld(0, 1, 78);
         this.level.playSound((Player)null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.6F, 0.62F);
         this.level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, 80, 5.0, 1.2, 5.0, 0.025);
         ServerPlayer owner = this.ownerId == null ? null : this.level.getServer().getPlayerList().getPlayer(this.ownerId);
         Component message = Component.literal(
               "Cartenon Temple complete for " + this.ownerName + ": " + this.changed + " blocks changed and 13 statues placed."
            )
            .withStyle(ChatFormatting.GREEN);
         if (owner != null) {
            owner.sendSystemMessage(message);
         } else {
            SololevelingMod.LOGGER.info(message.getString());
         }

         if (this.onComplete != null) {
            try {
               this.onComplete.run();
            } catch (RuntimeException exception) {
               SololevelingMod.LOGGER.error("Cartenon Temple completion callback failed", exception);
            }
         }

         return true;
      }

      private boolean abort(String reason, RuntimeException exception) {
         String message = "Cartenon Temple construction stopped for " + this.ownerName + ": " + reason;
         if (exception == null) {
            SololevelingMod.LOGGER.error(message);
         } else {
            SololevelingMod.LOGGER.error(message, exception);
         }

         ServerPlayer owner = this.ownerId == null ? null : this.level.getServer().getPlayerList().getPlayer(this.ownerId);
         if (owner != null) {
            owner.sendSystemMessage(Component.literal("The Cartenon Temple failed to stabilize. Try the hidden gate again.").withStyle(ChatFormatting.RED));
         }

         return true;
      }

      private BlockPos toWorld(int x, int y, int z) {
         return this.origin.relative(this.right, x).relative(this.forward, z).above(y);
      }

      private BlockState desiredState(int x, int y, int z) {
         if (!this.isInsideFootprint(x, z)) {
            return this.sealedEntrance && Math.abs(x) <= 50 ? Blocks.AIR.defaultBlockState() : null;
         }

         int roof = this.roofHeight(x, z);
         if (y < -3 || y > roof + 1) {
            return null;
         }

         if (y == -3) {
            return Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
         }

         if (y == -2) {
            return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
         }

         if (y == -1) {
            return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
         }

         if (y == 0) {
            return this.floorState(x, z);
         }

         boolean frontOpening = !this.sealedEntrance && z <= 4 && this.isFrontOpening(x, y);
         if (this.isOuterShell(x, z)) {
            return frontOpening ? Blocks.AIR.defaultBlockState() : this.wallState(x, y, z);
         }

         if (y == roof + 1) {
            return Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
         }

         if (y == roof) {
            return this.ceilingState(x, z);
         }

         BlockState godLight = this.statueGodLightState(x, y, z);
         if (godLight != null) {
            return godLight;
         }

         BlockState dais = this.daisState(x, y, z);
         if (dais != null) {
            return dais;
         }

         BlockState throne = this.throneState(x, y, z);
         if (throne != null) {
            return throne;
         }

         BlockState pedestal = this.pedestalState(x, y, z);
         if (pedestal != null) {
            return pedestal;
         }

         BlockState niche = this.nicheState(x, y, z);
         if (niche != null) {
            return niche;
         }

         BlockState column = this.columnState(x, y, z);
         if (column != null) {
            return column;
         }

         BlockState arch = this.archState(x, y, z, 14, 15, 18);
         if (arch == null) {
            arch = this.archState(x, y, z, 124, 19, 21);
         }

         if (arch != null) {
            return arch;
         }

         BlockState brazier = this.brazierState(x, y, z);
         if (brazier != null) {
            return brazier;
         }

         BlockState lamp = this.hangingLampState(x, y, z, roof);
         return lamp != null ? lamp : Blocks.AIR.defaultBlockState();
      }

      private int roofHeight(int x, int z) {
         double normalized = Math.min(1.0, this.roomRadius(x, z) / 75.0);
         double dome = Math.sqrt(Math.max(0.0, 1.0 - normalized * normalized));
         return 34 + (int)Math.round(dome * 16.0);
      }

      private boolean isInsideFootprint(int x, int z) {
         return this.roomRadius(x, z) <= 77.35;
      }

      private boolean isOuterShell(int x, int z) {
         return this.roomRadius(x, z) >= 75.0;
      }

      private double roomRadius(int x, int z) {
         double dz = z - 77;
         return Math.sqrt((double)x * x + dz * dz);
      }

      private boolean isFrontOpening(int x, int y) {
         int absX = Math.abs(x);
         if (y <= 11) {
            return absX <= 7;
         }

         if (y > 19) {
            return false;
         }

         int dy = y - 11;
         return x * x + dy * dy <= 64;
      }

      private BlockState wallState(int x, int y, int z) {
         double radius = this.roomRadius(x, z);
         if (radius >= 76.45) {
            return Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
         } else if (y <= 2) {
            return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
         } else if (y % 9 == 0) {
            return Blocks.POLISHED_ANDESITE.defaultBlockState();
         } else if ((z + y * 3 + Math.abs(x)) % 31 == 0) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
         } else if (y % 9 == 1 && (int)Math.round(radius * 2.0) / 6 % 2 == 0) {
            return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
         } else {
            return radius >= 75.65 ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
         }
      }

      private BlockState ceilingState(int x, int z) {
         double radius = this.roomRadius(x, z);
         if (Math.abs(radius - 58.0) < 0.8 || Math.abs(radius - 38.0) < 0.72 || Math.abs(x) <= 1 || Math.abs(z - 77) <= 1) {
            return Blocks.POLISHED_BASALT.defaultBlockState();
         } else {
            return (Math.abs(x) + z) % 17 == 0 ? Blocks.CHISELED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
         }
      }

      private BlockState floorState(int x, int z) {
         int absX = Math.abs(x);
         double chamberRadius = this.roomRadius(x, z);
         if (chamberRadius >= 73.0) {
            return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
         }

         int circleZ = z - 77;
         double radius = Math.sqrt((double)x * x + (double)circleZ * circleZ);
         if (Math.abs(radius - 60.0) < 0.72
            || Math.abs(radius - 44.0) < 0.7
            || Math.abs(radius - 27.0) < 0.72
            || Math.abs(radius - 18.0) < 0.65
            || Math.abs(radius - 9.0) < 0.58) {
            return Blocks.GILDED_BLACKSTONE.defaultBlockState();
         }

         if (radius < 4.0) {
            return (x + circleZ) % 2 == 0 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState();
         }

         if (!(radius < 66.0) || Math.abs(x) > 1 && Math.abs(circleZ) > 1 && Math.abs(Math.abs(x) - Math.abs(circleZ)) > 1) {
            if (absX <= 5) {
               if (z % 12 == 0 && absX <= 1) {
                  return Blocks.SEA_LANTERN.defaultBlockState();
               } else {
                  return z % 8 <= 1 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.POLISHED_ANDESITE.defaultBlockState();
               }
            } else if (radius >= 36.0 && radius <= 39.0) {
               return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            } else {
               return (Math.floorDiv(x, 7) + Math.floorDiv(z, 9)) % 2 == 0
                  ? Blocks.DEEPSLATE_TILES.defaultBlockState()
                  : Blocks.STONE_BRICKS.defaultBlockState();
            }
         } else {
            return Blocks.DARK_PRISMARINE.defaultBlockState();
         }
      }

      private BlockState pedestalState(int x, int y, int z) {
         for (int i = 0; i < CartenonTempleGenerator.STATUE_Z.length; i++) {
            int centerZ = CartenonTempleGenerator.STATUE_Z[i];

            for (int centerX : new int[]{-CartenonTempleGenerator.STATUE_X[i], CartenonTempleGenerator.STATUE_X[i]}) {
               int dx = Math.abs(x - centerX);
               int dz = Math.abs(z - centerZ);
               if (y == 1 && dx <= 4 && dz <= 4) {
                  return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
               }

               if (y == 2 && dx <= 3 && dz <= 3) {
                  return dx != 3 && dz != 3 ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
               }

               if (y == 3 && dx <= 2 && dz <= 2) {
                  return dx == 0 && dz == 0 ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
               }
            }
         }

         return null;
      }

      private BlockState nicheState(int x, int y, int z) {
         if (y >= 3 && y <= 20) {
            for (int centerZ : CartenonTempleGenerator.STATUE_Z) {
               int dz = Math.abs(z - centerZ);
               if (dz <= 5) {
                  double radialZ = z - 77;
                  double wallXSquared = 5329.0 - radialZ * radialZ;
                  if (!(wallXSquared <= 0.0)) {
                     int wallX = (int)Math.round(Math.sqrt(wallXSquared));
                     if (Math.abs(Math.abs(x) - wallX) <= 1) {
                        if (dz != 5 && y != 3 && y != 20) {
                           if ((y == 10 || y == 16) && dz <= 4) {
                              return Blocks.POLISHED_ANDESITE.defaultBlockState();
                           }

                           if (dz != 0 || y != 8 && y != 15) {
                              return Blocks.DARK_PRISMARINE.defaultBlockState();
                           }

                           return Blocks.SEA_LANTERN.defaultBlockState();
                        }

                        return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                     }
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      }

      private BlockState columnState(int x, int y, int z) {
         for (int i = 0; i < CartenonTempleGenerator.COLUMN_Z.length; i++) {
            int dx = Math.abs(x - CartenonTempleGenerator.COLUMN_X[i]);
            int dz = Math.abs(z - CartenonTempleGenerator.COLUMN_Z[i]);
            if (y <= 2 && dx <= 3 && dz <= 3) {
               return y == 2 ? Blocks.CHISELED_DEEPSLATE.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            }

            if (y >= 3 && y <= 31 && dx <= 1 && dz <= 1) {
               if (y % 8 == 0) {
                  return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
               }

               return Blocks.POLISHED_BASALT.defaultBlockState();
            }

            if (y >= 32 && y <= 34 && dx <= 3 && dz <= 3) {
               return y == 32 ? Blocks.POLISHED_ANDESITE.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
            }
         }

         return null;
      }

      private BlockState archState(int x, int y, int z, int centerZ, int halfWidth, int springY) {
         if (Math.abs(z - centerZ) <= 1 && y >= 1) {
            int absX = Math.abs(x);
            if (y <= springY && absX >= halfWidth - 2 && absX <= halfWidth) {
               return this.archMaterial(y, z);
            } else {
               int dy = y - springY;
               if (dy >= 0 && dy <= halfWidth) {
                  int edge = (int)Math.round(Math.sqrt((double)halfWidth * halfWidth - (double)dy * dy));
                  return Math.abs(absX - edge) <= 1 ? this.archMaterial(y, z) : null;
               } else {
                  return null;
               }
            }
         } else {
            return null;
         }
      }

      private BlockState archMaterial(int y, int z) {
         if ((y + z) % 5 == 0) {
            return Blocks.GILDED_BLACKSTONE.defaultBlockState();
         } else {
            return y % 4 == 0 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
         }
      }

      private BlockState brazierState(int x, int y, int z) {
         if (Math.abs(x) != 19) {
            return null;
         }

         for (int centerZ : CartenonTempleGenerator.BRAZIER_Z) {
            if (z == centerZ) {
               if (y == 1) {
                  return Blocks.CHISELED_DEEPSLATE.defaultBlockState();
               }

               if (y == 2) {
                  return Blocks.SOUL_SOIL.defaultBlockState();
               }

               if (y == 3) {
                  return Blocks.SOUL_FIRE.defaultBlockState();
               }
            }
         }

         return null;
      }

      private BlockState statueGodLightState(int x, int y, int z) {
         if (z >= 138 && z <= 141 && y >= 8 && y <= 22) {
            for (int[] light : CartenonTempleGenerator.GOD_LIGHTS) {
               if (x == light[0] && y == light[1] && z == light[2]) {
                  return Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, light[3]);
               }
            }

            return null;
         } else {
            return null;
         }
      }

      private BlockState hangingLampState(int x, int y, int z, int roof) {
         if (x != 0 && x != -16 && x != 16) {
            return null;
         }

         if (z >= 24 && z <= 132 && z % 18 == 6) {
            int lanternY = roof - 12;
            if (y == lanternY) {
               return Blocks.SOUL_LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING, true);
            } else {
               return y > lanternY && y < roof ? Blocks.CHAIN.defaultBlockState().setValue(BlockStateProperties.AXIS, Axis.Y) : null;
            }
         } else {
            return null;
         }
      }

      private BlockState daisState(int x, int y, int z) {
         int height = this.daisHeight(x, z);
         if (height <= 0 || y < 1 || y > height) {
            return null;
         } else if (y == height) {
            return (Math.abs(x) + z) % 7 == 0 ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
         } else {
            return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
         }
      }

      private int daisHeight(int x, int z) {
         if (z < 126) {
            return 0;
         }

         int tier = Mth.clamp(1 + (z - 126) / 4, 1, 6);
         int width = 31 - tier * 2;
         return Math.abs(x) <= width ? tier : 0;
      }

      private BlockState throneState(int x, int y, int z) {
         int absX = Math.abs(x);
         if (z >= 149 && z <= 151 && y >= 6 && y <= 29) {
            int width = Math.max(3, 10 - (y - 6) / 4);
            if (absX <= width) {
               if (absX != width && y % 6 != 0) {
                  return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
               }

               return Blocks.GILDED_BLACKSTONE.defaultBlockState();
            }
         }

         if (absX >= 9 && absX <= 11 && z >= 140 && z <= 149 && y >= 6 && y <= 12) {
            return y == 12 ? Blocks.CHISELED_DEEPSLATE.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
         } else {
            return z == 148 && y >= 27 && y <= 31 && absX <= 12 - Math.abs(y - 29) * 3 ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : null;
         }
      }

      private void removePreviousTempleStatues() {
         BlockPos first = this.toWorld(-77, 0, 0);
         BlockPos second = this.toWorld(77, 52, 154);
         AABB bounds = new AABB(first, second).inflate(4.0);

         for (Mob mob : this.level.getEntitiesOfClass(Mob.class, bounds, entity -> entity.getPersistentData().getBoolean("CartenonTempleStatue"))) {
            mob.discard();
         }
      }

      private void spawnTempleStatues() {
         EntityType<?>[] types = new EntityType[]{
            SololevelingModEntities.STATUEAXE.get(), SololevelingModEntities.STATUEHAMMER.get(), SololevelingModEntities.STATUESWORD.get()
         };

         for (int i = 0; i < CartenonTempleGenerator.STATUE_Z.length; i++) {
            this.spawnStatue(types[i % types.length], -CartenonTempleGenerator.STATUE_X[i], 4, CartenonTempleGenerator.STATUE_Z[i], 0, 77);
            this.spawnStatue(types[(i + 1) % types.length], CartenonTempleGenerator.STATUE_X[i], 4, CartenonTempleGenerator.STATUE_Z[i], 0, 77);
         }

         this.spawnStatue(SololevelingModEntities.STATUE_OF_GOD.get(), 0, this.daisHeight(0, 145) + 1, 145, 0, 77);
      }

      private void spawnStatue(EntityType<?> type, int x, int y, int z, int lookX, int lookZ) {
         if (type.create(this.level) instanceof Mob statue) {
            BlockPos var19 = this.toWorld(x, y, z);
            BlockPos lookPos = this.toWorld(lookX, y, lookZ);
            double dx = lookPos.getX() - var19.getX();
            double dz = lookPos.getZ() - var19.getZ();
            float yaw = (float)(Mth.atan2(dz, dx) * 180.0F / (float)Math.PI) - 90.0F;
            statue.moveTo(var19.getX() + 0.5, var19.getY(), var19.getZ() + 0.5, yaw, 0.0F);
            statue.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(var19), MobSpawnType.STRUCTURE, null, null);
            statue.setYRot(yaw);
            statue.setYBodyRot(yaw);
            statue.setYHeadRot(yaw);
            statue.getPersistentData().putFloat("CartenonHomeYaw", yaw);
            statue.setNoAi(true);
            statue.setPersistenceRequired();
            statue.getPersistentData().putBoolean("CartenonTempleStatue", true);
            if (statue instanceof StatueaxeEntity axe) {
               axe.setTempleScale(2.0F);
            } else if (statue instanceof StatuehammerEntity hammer) {
               hammer.setTempleScale(2.0F);
            } else if (statue instanceof StatueswordEntity sword) {
               sword.setTempleScale(2.0F);
            } else if (statue instanceof StatueOfGodEntity god) {
               god.getEntityData().set(StatueOfGodEntity.DATA_default_x, var19.getX());
               god.getEntityData().set(StatueOfGodEntity.DATA_default_y, var19.getY());
               god.getEntityData().set(StatueOfGodEntity.DATA_default_z, var19.getZ());
            }

            statue.refreshDimensions();
            this.level.addFreshEntity(statue);
         }
      }
   }

   private record BuildKey(ResourceKey<Level> dimension, BlockPos origin) {
   }
}
