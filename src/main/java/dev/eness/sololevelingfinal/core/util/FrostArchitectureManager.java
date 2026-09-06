package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.SkillSlotHelper;

@EventBusSubscriber
public final class FrostArchitectureManager {
   public static final String SKILL = "Frozen Architecture";
   private static final int NORMAL_MANA = 340;
   private static final int MANIFESTED_MANA = 300;
   private static final int COOLDOWN_TICKS = 180;
   private static final int MAX_BLUEPRINT_BLOCKS = 320;
   private static final int BLOCKS_PER_PLAYER_TICK = 28;
   private static final int GLOBAL_BLOCKS_PER_TICK = 84;
   private static final int TARGET_RANGE = 14;
   private static final Map<UUID, FrostArchitectureManager.BuildTask> BUILD_TASKS = new HashMap<>();
   private static long lastServerTick = Long.MIN_VALUE;
   private static int globalBudget;

   private FrostArchitectureManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         BUILD_TASKS.remove(player.getUUID());
      }
   }

   public static void castSelection(ServerPlayer player, int rawBlueprintId) {
      if (validCaster(player)) {
         if (BUILD_TASKS.containsKey(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.sololeveling.frost_architecture.already_building"), true);
         } else if (CooldownManager.isOnCooldown(player, "Frozen Architecture")) {
            player.displayClientMessage(
               Component.translatable("message.sololeveling.frost_architecture.cooldown", CooldownManager.getRemainingSeconds(player, "Frozen Architecture")),
               true
            );
         } else {
            boolean manifested = FrostMonarchManager.isSpiritualized(player);
            int mana = manifested ? 300 : 340;
            SololevelingModVariables.PlayerVariables vars = variables(player);
            if (!player.isCreative() && vars.MP < mana) {
               player.displayClientMessage(Component.translatable("message.sololeveling.frost_architecture.not_enough_mana", mana), true);
            } else {
               FrostArchitectureBlueprint blueprint = FrostArchitectureBlueprint.byId(rawBlueprintId);
               Direction forward = horizontalFacing(player);
               BlockPos anchor = findAnchor(player, forward, blueprint);
               List<BlockPos> plan = createPlan(player.serverLevel(), blueprint, anchor, forward);
               if (plan.size() < 8) {
                  player.displayClientMessage(Component.translatable("message.sololeveling.frost_architecture.no_space"), true);
               } else {
                  if (!player.isCreative()) {
                     player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(data -> {
                        data.MP = Math.max(0.0, data.MP - mana);
                        data.syncPlayerVariables(player);
                     });
                  }

                  CooldownManager.set(player, "Frozen Architecture", 180);
                  CooldownManager.set(player, "mana_refresh", 40);
                  BUILD_TASKS.put(player.getUUID(), new FrostArchitectureManager.BuildTask(player.serverLevel(), blueprint, plan));
                  player.serverLevel()
                     .sendParticles(ParticleTypes.SNOWFLAKE, anchor.getX() + 0.5, anchor.getY() + 0.6, anchor.getZ() + 0.5, 24, 0.8, 0.35, 0.8, 0.04);
                  player.level().playSound((Player)null, anchor, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.9F, 1.65F);
                  player.displayClientMessage(
                     Component.translatable("message.sololeveling.frost_architecture.manifesting", Component.translatable(blueprint.translationKey())), true
                  );
               }
            }
         }
      }
   }

   private static boolean validCaster(ServerPlayer player) {
      if (player != null && player.isAlive() && !player.isSpectator() && FrostMonarchManager.isFrostMonarch(player)) {
         SololevelingModVariables.PlayerVariables vars = variables(player);
         return vars.combatmode
            && "Frozen Architecture".equals(vars.PselectedPower)
            && JobSkillManager.isFrostSkill("Frozen Architecture")
            && isEquipped(vars)
            && player.level().hasChunkAt(player.blockPosition());
      } else {
         return false;
      }
   }

   private static boolean isEquipped(SololevelingModVariables.PlayerVariables vars) {
      for (int slot = 1; slot <= 16; slot++) {
         if ("Frozen Architecture".equals(SkillSlotHelper.getSlot(vars, slot))) {
            return true;
         }
      }

      return false;
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.level().isClientSide() && event.player instanceof ServerPlayer player) {
         FrostArchitectureManager.BuildTask task = BUILD_TASKS.get(player.getUUID());
         if (task != null) {
            if (player.isAlive() && player.serverLevel() == task.level) {
               long gameTime = player.serverLevel().getGameTime();
               if (lastServerTick != gameTime) {
                  lastServerTick = gameTime;
                  globalBudget = 84;
               }

               int budget = Math.min(28, globalBudget);
               int attempted = 0;
               int placed = 0;

               while (attempted < budget && task.index < task.positions.size()) {
                  BlockPos pos = task.positions.get(task.index++);
                  boolean occupied = !task.level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), entity -> entity.isAlive()).isEmpty();
                  if (!occupied && FrostMonarchManager.placeTemporaryIce(player, pos)) {
                     placed++;
                  }

                  attempted++;
               }

               globalBudget -= attempted;
               task.placed += placed;
               if (placed > 0 && player.tickCount % 2 == 0) {
                  BlockPos pos = task.positions.get(Math.max(0, task.index - 1));
                  player.serverLevel()
                     .sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, Math.min(5, placed), 0.32, 0.32, 0.32, 0.02);
               }

               if (task.index >= task.positions.size()) {
                  BUILD_TASKS.remove(player.getUUID());
                  player.level().playSound((Player)null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 0.75F);
               }
            } else {
               BUILD_TASKS.remove(player.getUUID());
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      BUILD_TASKS.remove(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerChangedDimensionEvent event) {
      BUILD_TASKS.remove(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      BUILD_TASKS.remove(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      BUILD_TASKS.clear();
      lastServerTick = Long.MIN_VALUE;
      globalBudget = 0;
   }

   private static List<BlockPos> createPlan(ServerLevel level, FrostArchitectureBlueprint blueprint, BlockPos anchor, Direction forward) {
      Set<BlockPos> positions = new LinkedHashSet<>();
      Direction right = forward.getClockWise();
      switch (blueprint) {
         case CRYSTAL_DOME:
            addDome(positions, anchor, forward, right);
            break;
         case FROZEN_BASTION:
            addBastion(positions, anchor, forward, right);
            break;
         case GLACIER_WALL:
            addWall(positions, anchor, forward, right);
            break;
         case PALE_BRIDGE:
            addBridge(positions, anchor, forward, right);
            break;
         case SPIRE_CAGE:
            addSpireCage(positions, anchor, forward, right);
            break;
         case RAMPART_RING:
            addRampartRing(positions, anchor, forward, right);
      }

      return positions.stream()
         .filter(pos -> validPlanCell(level, pos))
         .sorted(Comparator.<BlockPos>comparingInt(pos -> pos.getY()).thenComparingInt(pos -> pos.distManhattan(anchor)))
         .limit(320L)
         .map(BlockPos::immutable)
         .toList();
   }

   private static void addDome(Set<BlockPos> out, BlockPos center, Direction forward, Direction right) {
      int radius = 6;

      for (int y = 0; y <= radius; y++) {
         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               double distance = Math.sqrt(x * x + z * z + y * y);
               if (!(distance < radius - 0.65) && !(distance > radius + 0.45) && !isDoorway(x, z, y, forward, right, radius)) {
                  out.add(center.offset(x, y, z));
               }
            }
         }
      }
   }

   private static void addBastion(Set<BlockPos> out, BlockPos center, Direction forward, Direction right) {
      int half = 4;
      int height = 6;

      for (int y = 0; y <= height; y++) {
         for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
               if ((y == height || Math.abs(x) == half || Math.abs(z) == half) && !isDoorway(x, z, y, forward, right, half)) {
                  out.add(center.offset(x, y, z));
               }
            }
         }
      }

      for (int side = -half; side <= half; side += 2) {
         out.add(local(center, forward, right, side, -half, height + 1));
         out.add(local(center, forward, right, side, half, height + 1));
         out.add(local(center, forward, right, -half, side, height + 1));
         out.add(local(center, forward, right, half, side, height + 1));
      }
   }

   private static void addWall(Set<BlockPos> out, BlockPos center, Direction forward, Direction right) {
      for (int across = -7; across <= 7; across++) {
         int height = 5 + (Math.abs(across) % 3 == 0 ? 1 : 0);

         for (int y = 0; y <= height; y++) {
            if (Math.abs(across) > 1 || y > 2) {
               out.add(local(center, forward, right, across, 0, y));
            }
         }
      }
   }

   private static void addBridge(Set<BlockPos> out, BlockPos start, Direction forward, Direction right) {
      for (int length = 0; length < 20; length++) {
         for (int across = -1; across <= 1; across++) {
            out.add(local(start, forward, right, across, length, 0));
         }

         if (length % 2 == 0) {
            out.add(local(start, forward, right, -2, length, 1));
            out.add(local(start, forward, right, 2, length, 1));
         }
      }
   }

   private static void addSpireCage(Set<BlockPos> out, BlockPos center, Direction forward, Direction right) {
      int[][] pillars = new int[][]{{-4, -4}, {0, -5}, {4, -4}, {-4, 4}, {0, 5}, {4, 4}};

      for (int[] pillar : pillars) {
         for (int y = 0; y <= 8; y++) {
            out.add(local(center, forward, right, pillar[0], pillar[1], y));
         }

         out.add(local(center, forward, right, pillar[0], pillar[1], 9));
      }

      for (int x = -3; x <= 3; x++) {
         out.add(local(center, forward, right, x, -4, 8));
         out.add(local(center, forward, right, x, 4, 8));
      }

      for (int z = -3; z <= 3; z++) {
         out.add(local(center, forward, right, -4, z, 8));
         out.add(local(center, forward, right, 4, z, 8));
      }
   }

   private static void addRampartRing(Set<BlockPos> out, BlockPos center, Direction forward, Direction right) {
      int radius = 6;

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            double distance = Math.sqrt(x * x + z * z);
            if (!(distance < radius - 0.65) && !(distance > radius + 0.45)) {
               boolean gate = Math.abs(x * right.getStepX() + z * right.getStepZ()) <= 1
                  && Math.abs(x * forward.getStepX() + z * forward.getStepZ()) >= radius - 1;

               for (int y = 0; y <= 3; y++) {
                  if (!gate || y == 3) {
                     out.add(center.offset(x, y, z));
                  }
               }

               if ((x + z & 1) == 0) {
                  out.add(center.offset(x, 4, z));
               }
            }
         }
      }
   }

   private static boolean isDoorway(int x, int z, int y, Direction forward, Direction right, int radius) {
      int forwardOffset = x * forward.getStepX() + z * forward.getStepZ();
      int sideOffset = x * right.getStepX() + z * right.getStepZ();
      return forwardOffset <= -radius + 1 && Math.abs(sideOffset) <= 1 && y <= 2;
   }

   private static BlockPos local(BlockPos origin, Direction forward, Direction right, int across, int ahead, int up) {
      return origin.offset(right.getStepX() * across + forward.getStepX() * ahead, up, right.getStepZ() * across + forward.getStepZ() * ahead);
   }

   private static boolean validPlanCell(ServerLevel level, BlockPos pos) {
      if (level.hasChunkAt(pos)
         && level.getWorldBorder().isWithinBounds(pos)
         && pos.getY() > level.getMinBuildHeight()
         && pos.getY() < level.getMaxBuildHeight() - 1) {
         BlockState state = level.getBlockState(pos);
         return state.isAir() || state.is(Blocks.WATER) || state.is(SololevelingModBlocks.FROST_CAUSEWAY.get());
      } else {
         return false;
      }
   }

   private static BlockPos findAnchor(ServerPlayer player, Direction forward, FrostArchitectureBlueprint blueprint) {
      ServerLevel level = player.serverLevel();
      Vec3 start = player.getEyePosition();
      Vec3 end = start.add(player.getLookAngle().scale(14.0));
      BlockHitResult hit = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.ANY, player));
      BlockPos candidate;
      if (hit.getType() == Type.BLOCK) {
         candidate = hit.getBlockPos().relative(hit.getDirection());
      } else {
         Vec3 projected = player.position().add(Vec3.atLowerCornerOf(forward.getNormal()).scale(8.0));
         candidate = BlockPos.containing(projected);
      }

      return blueprint == FrostArchitectureBlueprint.PALE_BRIDGE ? player.blockPosition().below() : groundAbove(level, candidate);
   }

   private static BlockPos groundAbove(ServerLevel level, BlockPos candidate) {
      MutableBlockPos cursor = candidate.mutable();

      for (int i = 0; i < 10 && cursor.getY() > level.getMinBuildHeight() + 1; i++) {
         BlockPos below = cursor.below();
         if (!level.hasChunkAt(below)) {
            break;
         }

         if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
            return cursor.immutable();
         }

         cursor.move(Direction.DOWN);
      }

      return candidate;
   }

   private static Direction horizontalFacing(ServerPlayer player) {
      return Direction.fromYRot(player.getYRot());
   }

   private static SololevelingModVariables.PlayerVariables variables(ServerPlayer player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static final class BuildTask {
      private final ServerLevel level;
      private final FrostArchitectureBlueprint blueprint;
      private final List<BlockPos> positions;
      private int index;
      private int placed;

      private BuildTask(ServerLevel level, FrostArchitectureBlueprint blueprint, List<BlockPos> positions) {
         this.level = level;
         this.blueprint = blueprint;
         this.positions = new ArrayList<>(positions);
      }
   }
}
