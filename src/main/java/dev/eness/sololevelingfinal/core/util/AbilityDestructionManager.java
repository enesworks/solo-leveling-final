package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;

@EventBusSubscriber(modid = "sololeveling")
public final class AbilityDestructionManager {
   public static final TagKey<Block> IMMUNE_BLOCKS = TagKey.create(Registries.BLOCK, new ResourceLocation("sololeveling", "ability_destruction_immune"));
   private static final int MAX_MUTATIONS_PER_TICK = 96;
   private static final int MAX_INSPECTIONS_PER_TICK = 512;
   private static final int MAX_MUTATIONS_PER_JOB_PER_TICK = 48;
   private static final int MAX_QUEUED_POSITIONS = 24576;
   private static final int MAX_QUEUED_JOBS = 128;
   private static final int MAX_QUEUED_POSITIONS_PER_OWNER = 6144;
   private static final int MAX_QUEUED_JOBS_PER_OWNER = 24;
   private static final int MAX_REQUESTS_PER_OWNER_PER_TICK = 4;
   private static final int MAX_REQUESTS_PER_TICK = 24;
   private static final ArrayDeque<AbilityDestructionManager.DestructionJob> JOBS = new ArrayDeque<>();
   private static final Set<AbilityDestructionManager.QueuedBlock> QUEUED_BLOCKS = new HashSet<>();
   private static final Map<UUID, Integer> OWNER_REQUESTS_THIS_TICK = new HashMap<>();
   private static final ThreadLocal<Boolean> POSTING_BREAK_EVENT = ThreadLocal.withInitial(() -> false);
   private static int queuedPositions;
   private static int requestTick = Integer.MIN_VALUE;
   private static int requestsThisTick;

   private AbilityDestructionManager() {
   }

   public static boolean isPostingAbilityBreakEvent() {
      return POSTING_BREAK_EVENT.get();
   }

   public static boolean enabled(ServerLevel level) {
      return level != null && level.getGameRules().getBoolean(SololevelingModGameRules.SOLO_ABILITY_DESTRUCTION) && !DkcFloorRegistry.isDkc(level);
   }

   public static void impact(ServerPlayer player, AbilityDestructionManager.Profile profile, Vec3 center, double drivingAttribute, boolean empowered) {
      if (canSchedule(player, profile, center)) {
         AbilityDestructionManager.ScaledProfile scaled = scale(profile, drivingAttribute, empowered);
         double horizontal = scaled.radius;
         double vertical = Math.max(1.0, horizontal * impactDepthFactor(profile));
         BlockPos min = BlockPos.containing(center.x - horizontal, center.y - vertical, center.z - horizontal);
         BlockPos max = BlockPos.containing(center.x + horizontal, center.y + vertical * 0.38, center.z + horizontal);
         List<AbilityDestructionManager.ScoredPos> candidates = new ArrayList<>();

         for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            BlockPos pos = cursor.immutable();
            double dx = (pos.getX() + 0.5 - center.x) / horizontal;
            double dz = (pos.getZ() + 0.5 - center.z) / horizontal;
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz) * horizontal;
            if (!preservesCasterFooting(profile) || !(pos.getY() + 0.5 < center.y) || !(horizontalDistance < 1.35)) {
               double dyScale = pos.getY() + 0.5 <= center.y ? vertical : Math.max(0.75, vertical * 0.38);
               double dy = (pos.getY() + 0.5 - center.y) / dyScale;
               double radial = dx * dx + dz * dz;
               double distance = radial + dy * dy;
               if (distance <= 1.0) {
                  double depth = Math.max(0.0, (center.y - (pos.getY() + 0.5)) / vertical);
                  candidates.add(new AbilityDestructionManager.ScoredPos(pos, depth * 0.68 + radial * 0.32 + deterministicJitter(pos, player.getUUID()) * 0.12));
               }
            }
         }

         schedule(player, profile, scaled, candidates);
      }
   }

   public static void line(ServerPlayer player, AbilityDestructionManager.Profile profile, Vec3 start, Vec3 end, double drivingAttribute, boolean empowered) {
      if (canSchedule(player, profile, start) && finite(end)) {
         Vec3 segment = end.subtract(start);
         double maximumLength = maximumLineLength(profile);
         if (segment.lengthSqr() > maximumLength * maximumLength) {
            end = start.add(segment.normalize().scale(maximumLength));
         }

         AbilityDestructionManager.ScaledProfile scaled = scale(profile, drivingAttribute, empowered);
         double radius = scaled.radius;
         BlockPos min = BlockPos.containing(Math.min(start.x, end.x) - radius, Math.min(start.y, end.y) - radius, Math.min(start.z, end.z) - radius);
         BlockPos max = BlockPos.containing(Math.max(start.x, end.x) + radius, Math.max(start.y, end.y) + radius, Math.max(start.z, end.z) + radius);
         List<AbilityDestructionManager.ScoredPos> candidates = new ArrayList<>();

         for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            BlockPos pos = cursor.immutable();
            Vec3 point = Vec3.atCenterOf(pos);
            double distance = distanceToSegment(point, start, end);
            if (distance <= radius) {
               double along = alongSegment(point, start, end);
               candidates.add(
                  new AbilityDestructionManager.ScoredPos(
                     pos, distance / Math.max(0.25, radius) * 0.55 + deterministicJitter(pos, player.getUUID()) * 0.35 + Math.abs(along - 0.5) * 0.02
                  )
               );
            }
         }

         schedule(player, profile, scaled, candidates);
      }
   }

   public static void fissure(
      ServerPlayer player, AbilityDestructionManager.Profile profile, Vec3 origin, Vec3 direction, double length, double drivingAttribute, boolean empowered
   ) {
      if (finite(direction) && Double.isFinite(length) && !(direction.lengthSqr() < 1.0E-6)) {
         if (canSchedule(player, profile, origin)) {
            length = Mth.clamp(length, 0.5, maximumLineLength(profile));
            Vec3 horizontal = new Vec3(direction.x, 0.0, direction.z);
            if (!(horizontal.lengthSqr() < 1.0E-6)) {
               horizontal = horizontal.normalize();
               AbilityDestructionManager.ScaledProfile scaled = scale(profile, drivingAttribute, empowered);
               double halfWidth = Math.max(0.55, scaled.radius * 0.42);
               double depth = Mth.clamp(0.85 + scaled.radius * 0.25, 1.0, 3.0);
               Vec3 start = origin;
               Vec3 end = start.add(horizontal.scale(length));
               BlockPos min = BlockPos.containing(Math.min(start.x, end.x) - halfWidth, origin.y - depth, Math.min(start.z, end.z) - halfWidth);
               BlockPos max = BlockPos.containing(Math.max(start.x, end.x) + halfWidth, origin.y + 0.15, Math.max(start.z, end.z) + halfWidth);
               List<AbilityDestructionManager.ScoredPos> candidates = new ArrayList<>();
               double segmentLengthSqr = length * length;

               for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
                  BlockPos pos = cursor.immutable();
                  double px = pos.getX() + 0.5 - start.x;
                  double pz = pos.getZ() + 0.5 - start.z;
                  double along = Mth.clamp((px * (end.x - start.x) + pz * (end.z - start.z)) / segmentLengthSqr, 0.0, 1.0);
                  double closestX = start.x + (end.x - start.x) * along;
                  double closestZ = start.z + (end.z - start.z) * along;
                  double lateral = Math.hypot(pos.getX() + 0.5 - closestX, pos.getZ() + 0.5 - closestZ);
                  if (!(lateral > halfWidth)) {
                     double belowSurface = Math.max(0.0, (origin.y - (pos.getY() + 0.5)) / depth);
                     candidates.add(
                        new AbilityDestructionManager.ScoredPos(
                           pos,
                           belowSurface * 0.58 + lateral / halfWidth * 0.24 + Math.abs(along - 0.5) * 0.02 + deterministicJitter(pos, player.getUUID()) * 0.16
                        )
                     );
                  }
               }

               schedule(player, profile, scaled, candidates);
            }
         }
      }
   }

   public static void ring(
      ServerPlayer player, AbilityDestructionManager.Profile profile, Vec3 center, double visualRadius, double drivingAttribute, boolean empowered
   ) {
      if (canSchedule(player, profile, center) && Double.isFinite(visualRadius)) {
         visualRadius = Mth.clamp(visualRadius, 0.5, 24.0);
         AbilityDestructionManager.ScaledProfile scaled = scale(profile, drivingAttribute, empowered);
         double ringRadius = Math.min(24.0, Math.max(scaled.radius, visualRadius));
         double thickness = Mth.clamp(0.75 + scaled.radius * 0.15, 1.0, 3.2);
         BlockPos min = BlockPos.containing(center.x - ringRadius - thickness, center.y - 2.5, center.z - ringRadius - thickness);
         BlockPos max = BlockPos.containing(center.x + ringRadius + thickness, center.y + 0.65, center.z + ringRadius + thickness);
         List<AbilityDestructionManager.ScoredPos> candidates = new ArrayList<>();

         for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            BlockPos pos = cursor.immutable();
            double dx = pos.getX() + 0.5 - center.x;
            double dz = pos.getZ() + 0.5 - center.z;
            double radialDistance = Math.sqrt(dx * dx + dz * dz);
            double radialError = Math.abs(radialDistance - ringRadius);
            double spokeError = Math.abs(Math.sin(Math.atan2(dz, dx) * 4.0));
            boolean ringBlock = radialError <= thickness;
            boolean crackBlock = radialDistance >= Math.max(1.5, ringRadius * 0.18) && radialDistance <= ringRadius && spokeError <= 0.12;
            if (ringBlock || crackBlock) {
               double belowSurface = Math.max(0.0, center.y - (pos.getY() + 0.5));
               double aboveSurface = Math.max(0.0, pos.getY() + 0.5 - center.y);
               double shapeScore = ringBlock ? radialError / thickness * 0.18 : 0.2 + spokeError * 0.55;
               candidates.add(
                  new AbilityDestructionManager.ScoredPos(
                     pos, belowSurface * 0.34 + aboveSurface * 0.62 + shapeScore + deterministicJitter(pos, player.getUUID()) * 0.22
                  )
               );
            }
         }

         schedule(player, profile, scaled, candidates);
      }
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END && !JOBS.isEmpty()) {
         MinecraftServer server = event.getServer();
         int mutationBudget = 96;
         int inspectionBudget = 512;
         int jobsThisTick = JOBS.size();

         while (jobsThisTick-- > 0 && mutationBudget > 0 && inspectionBudget > 0 && !JOBS.isEmpty()) {
            AbilityDestructionManager.DestructionJob job = JOBS.removeFirst();
            ServerPlayer player = server.getPlayerList().getPlayer(job.owner);
            if (player != null
               && player.isAlive()
               && player.getId() == job.ownerEntityId
               && player.serverLevel().dimension().equals(job.dimension)
               && player.serverLevel().getGameTime() <= job.expiresAt
               && enabled(player.serverLevel())) {
               int jobMutations = 0;

               while (job.index < job.positions.size() && job.remaining > 0 && mutationBudget > 0 && inspectionBudget > 0 && jobMutations < 48) {
                  BlockPos pos = job.positions.get(job.index++);
                  QUEUED_BLOCKS.remove(new AbilityDestructionManager.QueuedBlock(job.dimension, pos.asLong()));
                  queuedPositions--;
                  inspectionBudget--;
                  if (destroyOne(player.serverLevel(), player, pos, job.maximumHardness)) {
                     job.remaining--;
                     jobMutations++;
                     mutationBudget--;
                  }
               }

               if (job.index < job.positions.size() && job.remaining > 0) {
                  JOBS.addLast(job);
               } else {
                  discard(job);
               }
            } else {
               discard(job);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      JOBS.clear();
      QUEUED_BLOCKS.clear();
      queuedPositions = 0;
      OWNER_REQUESTS_THIS_TICK.clear();
      requestTick = Integer.MIN_VALUE;
      requestsThisTick = 0;
      POSTING_BREAK_EVENT.remove();
   }

   private static void schedule(
      ServerPlayer player,
      AbilityDestructionManager.Profile profile,
      AbilityDestructionManager.ScaledProfile scaled,
      List<AbilityDestructionManager.ScoredPos> scored
   ) {
      UUID owner = player.getUUID();
      int ownerPositions = queuedPositionsFor(owner);
      if (!scored.isEmpty() && JOBS.size() < 128 && queuedJobsFor(owner) < 24 && queuedPositions < 24576) {
         scored.sort(Comparator.comparingDouble(AbilityDestructionManager.ScoredPos::score));
         int reserve = Mth.clamp(scaled.budget / 3, 12, 256);
         int candidateLimit = Math.min(scored.size(), Math.min(scaled.budget + reserve, Math.min(24576 - queuedPositions, 6144 - ownerPositions)));
         if (candidateLimit > 0) {
            ServerLevel level = player.serverLevel();
            List<BlockPos> positions = new ArrayList<>(candidateLimit);
            Set<Long> unique = new HashSet<>(candidateLimit * 2);

            for (AbilityDestructionManager.ScoredPos candidate : scored) {
               if (positions.size() >= candidateLimit) {
                  break;
               }

               BlockPos pos = candidate.pos;
               AbilityDestructionManager.QueuedBlock queued = new AbilityDestructionManager.QueuedBlock(level.dimension(), pos.asLong());
               if (unique.add(pos.asLong()) && !QUEUED_BLOCKS.contains(queued) && cheapCandidate(level, pos, scaled.maximumHardness)) {
                  positions.add(pos);
                  QUEUED_BLOCKS.add(queued);
               }
            }

            if (!positions.isEmpty()) {
               JOBS.addLast(
                  new AbilityDestructionManager.DestructionJob(
                     owner, player.getId(), level.dimension(), profile, positions, scaled.budget, scaled.maximumHardness, level.getGameTime() + 200L
                  )
               );
               queuedPositions = queuedPositions + positions.size();
            }
         }
      }
   }

   private static boolean destroyOne(ServerLevel level, ServerPlayer player, BlockPos pos, double maximumHardness) {
      if (enabled(level)
         && cheapCandidate(level, pos, maximumHardness)
         && level.mayInteract(player, pos)
         && player.mayUseItemAt(pos, Direction.UP, ItemStack.EMPTY)
         && level.getBlockState(pos).canEntityDestroy(level, pos, player)) {
         BlockState state = level.getBlockState(pos);
         BreakEvent event = new BreakEvent(level, pos, state, player);
         POSTING_BREAK_EVENT.set(true);

         boolean canceled;
         try {
            canceled = MinecraftForge.EVENT_BUS.post(event);
         } finally {
            POSTING_BREAK_EVENT.set(false);
         }

         if (canceled || !level.getBlockState(pos).equals(state)) {
            return false;
         }

         if (!ForgeEventFactory.onEntityDestroyBlock(player, pos, state)) {
            return false;
         }

         if (!level.getBlockState(pos).equals(state) || !cheapCandidate(level, pos, maximumHardness)) {
            return false;
         }

         if (!state.onDestroyedByPlayer(level, pos, player, false, level.getFluidState(pos))) {
            return false;
         }

         state.getBlock().destroy(level, pos, state);
         return true;
      } else {
         return false;
      }
   }

   private static boolean cheapCandidate(ServerLevel level, BlockPos pos, double maximumHardness) {
      if (pos.getY() >= level.getMinBuildHeight()
         && pos.getY() < level.getMaxBuildHeight()
         && level.hasChunkAt(pos)
         && level.getWorldBorder().isWithinBounds(pos)) {
         BlockState state = level.getBlockState(pos);
         if (state.hasBlockEntity() || level.getBlockEntity(pos) != null) {
            return false;
         } else if (state.isAir() || !state.getFluidState().isEmpty() || state.is(IMMUNE_BLOCKS) || state.is(BlockTags.WITHER_IMMUNE)) {
            return false;
         } else if (!state.is(Blocks.NETHER_PORTAL)
            && !state.is(Blocks.END_PORTAL)
            && !state.is(Blocks.END_PORTAL_FRAME)
            && !state.is(Blocks.END_GATEWAY)
            && !state.is(Blocks.BARRIER)
            && !state.is(Blocks.STRUCTURE_BLOCK)
            && !state.is(Blocks.JIGSAW)
            && !state.is(Blocks.COMMAND_BLOCK)
            && !state.is(Blocks.CHAIN_COMMAND_BLOCK)
            && !state.is(Blocks.REPEATING_COMMAND_BLOCK)
            && !state.is(Blocks.SPAWNER)) {
            float hardness = state.getDestroySpeed(level, pos);
            return hardness >= 0.0F && hardness <= maximumHardness;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean canSchedule(ServerPlayer player, AbilityDestructionManager.Profile profile, Vec3 point) {
      return player != null
            && profile != null
            && finite(point)
            && player.isAlive()
            && enabled(player.serverLevel())
            && JOBS.size() < 128
            && queuedPositions < 24576
            && queuedJobsFor(player.getUUID()) < 24
            && queuedPositionsFor(player.getUUID()) < 6144
            && player.serverLevel().hasChunkAt(BlockPos.containing(point))
         ? reserveRequest(player)
         : false;
   }

   private static boolean reserveRequest(ServerPlayer player) {
      int tick = player.server.getTickCount();
      if (requestTick != tick) {
         requestTick = tick;
         requestsThisTick = 0;
         OWNER_REQUESTS_THIS_TICK.clear();
      }

      UUID owner = player.getUUID();
      int ownerRequests = OWNER_REQUESTS_THIS_TICK.getOrDefault(owner, 0);
      if (requestsThisTick < 24 && ownerRequests < 4) {
         requestsThisTick++;
         OWNER_REQUESTS_THIS_TICK.put(owner, ownerRequests + 1);
         return true;
      } else {
         return false;
      }
   }

   private static int queuedJobsFor(UUID owner) {
      int count = 0;

      for (AbilityDestructionManager.DestructionJob job : JOBS) {
         if (job.owner.equals(owner)) {
            count++;
         }
      }

      return count;
   }

   private static int queuedPositionsFor(UUID owner) {
      int count = 0;

      for (AbilityDestructionManager.DestructionJob job : JOBS) {
         if (job.owner.equals(owner)) {
            count += Math.max(0, job.positions.size() - job.index);
         }
      }

      return count;
   }

   private static AbilityDestructionManager.ScaledProfile scale(AbilityDestructionManager.Profile profile, double attribute, boolean empowered) {
      if (!Double.isFinite(attribute)) {
         attribute = 0.0;
      }

      double normalized = Mth.clamp(Math.log1p(Math.max(0.0, attribute)) / Math.log1p(attributeCeiling(profile)), 0.0, 1.0);
      if (empowered) {
         normalized = Math.min(1.0, 0.2 + normalized * 1.02);
      }

      int budget = Mth.clamp((int)Math.round(Mth.lerp(normalized, profile.baseBudget, profile.maximumBudget)), profile.baseBudget, profile.maximumBudget);
      double radius = Mth.lerp(normalized, profile.baseRadius, profile.maximumRadius);
      double hardness = Mth.lerp(normalized, profile.baseHardness, profile.maximumHardness);
      return new AbilityDestructionManager.ScaledProfile(budget, radius, hardness);
   }

   private static double maximumLineLength(AbilityDestructionManager.Profile profile) {
      return switch (profile) {
         case ANTARES_BREATH -> 6.0;
         case ANTARES_EXTINCTION -> 26.0;
         case ANTARES_CLAW -> 12.0;
         case GOLIATH_PURSUIT_PATH -> 5.0;
         case BEAST_CLAW_RIFT, LIU_SWORD_CUT -> 24.0;
         default -> 32.0;
      };
   }

   private static double impactDepthFactor(AbilityDestructionManager.Profile profile) {
      return switch (profile) {
         case ANTARES_DESCENT -> 0.4;
         case ANTARES_EXTINCTION_FINISH -> 0.43;
         case FIRE_HEAVENFALL -> 0.35;
         case GOLIATH_COLLAPSE -> 0.34;
         case ARCANE_CONVERGENCE, BARRIER_CATASTROPHE -> 0.32;
         default -> 0.3;
      };
   }

   private static boolean preservesCasterFooting(AbilityDestructionManager.Profile profile) {
      return profile == AbilityDestructionManager.Profile.GOLIATH_COLLAPSE || profile == AbilityDestructionManager.Profile.ANTARES_DESCENT;
   }

   private static double attributeCeiling(AbilityDestructionManager.Profile profile) {
      return switch (profile) {
         case ANTARES_BREATH, ANTARES_EXTINCTION, ANTARES_CLAW, ANTARES_DESCENT, ANTARES_EXTINCTION_FINISH, ANTARES_CLAW_FINISH, ANTARES_ROAR -> 1200.0;
         case GOLIATH_PURSUIT_PATH, BEAST_CLAW_RIFT, GOLIATH_COLLAPSE, GOLIATH_SMASH, GOLIATH_PURSUIT_IMPACT, BEAST_RUBBLE_JAW -> 600.0;
         default -> 350.0;
         case FIRE_HEAVENFALL, ARCANE_CONVERGENCE, BARRIER_CATASTROPHE, FIRE_DOMINION, FIRE_BEAM_OVERCHARGED, STORM_SKYBREAKER, WHITE_FLAME_HELLSTORM, GRAND_MARSHAL_GRAVITY, GRAND_MARSHAL_DREAD, GRAND_MARSHAL_SKY_REND -> 500.0;
      };
   }

   private static boolean finite(Vec3 point) {
      return point != null && Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
   }

   private static double distanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
      Vec3 segment = end.subtract(start);
      double lengthSqr = segment.lengthSqr();
      if (lengthSqr < 1.0E-8) {
         return point.distanceTo(start);
      }

      double t = Mth.clamp(point.subtract(start).dot(segment) / lengthSqr, 0.0, 1.0);
      return point.distanceTo(start.add(segment.scale(t)));
   }

   private static double alongSegment(Vec3 point, Vec3 start, Vec3 end) {
      Vec3 segment = end.subtract(start);
      double lengthSqr = segment.lengthSqr();
      return lengthSqr < 1.0E-8 ? 0.0 : Mth.clamp(point.subtract(start).dot(segment) / lengthSqr, 0.0, 1.0);
   }

   private static double deterministicJitter(BlockPos pos, UUID owner) {
      long value = pos.asLong() ^ owner.getMostSignificantBits() ^ Long.rotateLeft(owner.getLeastSignificantBits(), 23);
      value ^= value >>> 33;
      value *= -49064778989728563L;
      value ^= value >>> 33;
      return (value & 65535L) / 65535.0;
   }

   private static void discard(AbilityDestructionManager.DestructionJob job) {
      for (int i = job.index; i < job.positions.size(); i++) {
         QUEUED_BLOCKS.remove(new AbilityDestructionManager.QueuedBlock(job.dimension, job.positions.get(i).asLong()));
      }

      int remainingPositions = Math.max(0, job.positions.size() - job.index);
      queuedPositions = Math.max(0, queuedPositions - remainingPositions);
      job.index = job.positions.size();
   }

   private static final class DestructionJob {
      private final UUID owner;
      private final int ownerEntityId;
      private final ResourceKey<Level> dimension;
      private final AbilityDestructionManager.Profile profile;
      private final List<BlockPos> positions;
      private final double maximumHardness;
      private final long expiresAt;
      private int remaining;
      private int index;

      private DestructionJob(
         UUID owner,
         int ownerEntityId,
         ResourceKey<Level> dimension,
         AbilityDestructionManager.Profile profile,
         List<BlockPos> positions,
         int remaining,
         double maximumHardness,
         long expiresAt
      ) {
         this.owner = owner;
         this.ownerEntityId = ownerEntityId;
         this.dimension = dimension;
         this.profile = profile;
         this.positions = positions;
         this.remaining = remaining;
         this.maximumHardness = maximumHardness;
         this.expiresAt = expiresAt;
      }
   }

   public enum Profile {
      FROST_SPEAR(5, 14, 0.7, 1.25, 1.5, 5.0),
      ARCANE_IMPACT(14, 48, 1.3, 2.8, 3.0, 14.0),
      ARCANE_CONVERGENCE(360, 1100, 6.0, 14.0, 18.0, 55.0),
      BARRIER_COLLAPSE(40, 140, 2.5, 4.5, 4.0, 18.0),
      BARRIER_CATASTROPHE(360, 1000, 6.0, 11.0, 18.0, 55.0),
      FIRE_ORB(24, 90, 2.0, 4.2, 4.0, 22.0),
      FIRE_DOMINION(220, 720, 5.0, 10.0, 12.0, 40.0),
      FIRE_HEAVENFALL(500, 1600, 8.0, 18.0, 20.0, 65.0),
      FIRE_BEAM_CHARGED(90, 320, 2.5, 5.0, 10.0, 40.0),
      FIRE_BEAM_OVERCHARGED(180, 600, 3.5, 7.0, 15.0, 60.0),
      STORM_THUNDERCLAP(60, 220, 2.5, 5.0, 8.0, 28.0),
      STORM_SKYBREAKER(240, 780, 5.0, 10.0, 15.0, 50.0),
      WHITE_FLAME_BREATH(70, 240, 2.0, 4.5, 10.0, 45.0),
      WHITE_FLAME_SPEAR(18, 64, 1.2, 2.6, 5.0, 24.0),
      WHITE_FLAME_HELLSTORM(70, 200, 2.5, 5.0, 10.0, 38.0),
      RANKER_IMPACT(8, 28, 1.0, 2.0, 2.0, 8.0),
      FIGHTER_SLAM(36, 120, 2.8, 5.8, 4.0, 20.0),
      TANKER_SLAM(32, 110, 2.6, 5.2, 4.0, 20.0),
      LIU_SWORD_CUT(18, 64, 1.0, 2.2, 4.0, 20.0),
      GOLIATH_SMASH(160, 520, 3.5, 8.5, 12.0, 45.0),
      GOLIATH_COLLAPSE(450, 1400, 8.0, 14.0, 20.0, 65.0),
      GOLIATH_PURSUIT_PATH(12, 40, 0.75, 1.35, 3.0, 14.0),
      GOLIATH_PURSUIT_IMPACT(180, 600, 5.0, 9.5, 12.0, 45.0),
      BEAST_CLAW_RIFT(60, 220, 1.5, 3.5, 10.0, 35.0),
      BEAST_RUBBLE_JAW(180, 600, 4.5, 9.0, 12.0, 45.0),
      GRAND_MARSHAL_GRAVITY(90, 300, 3.5, 6.5, 8.0, 32.0),
      GRAND_MARSHAL_DREAD(50, 180, 1.8, 3.8, 8.0, 32.0),
      GRAND_MARSHAL_SKY_REND(80, 280, 3.5, 6.5, 8.0, 32.0),
      ANTARES_CLAW(80, 280, 2.0, 4.5, 15.0, 55.0),
      ANTARES_CLAW_FINISH(300, 900, 5.0, 10.0, 25.0, 75.0),
      ANTARES_BREATH(100, 360, 2.5, 5.5, 20.0, 70.0),
      ANTARES_DESCENT(500, 1800, 8.0, 16.0, 35.0, 90.0),
      ANTARES_ROAR(320, 1000, 8.0, 14.0, 30.0, 80.0),
      ANTARES_EXTINCTION(260, 700, 3.5, 7.0, 40.0, 100.0),
      ANTARES_EXTINCTION_FINISH(500, 1400, 7.0, 14.0, 45.0, 100.0);

      private final int baseBudget;
      private final int maximumBudget;
      private final double baseRadius;
      private final double maximumRadius;
      private final double baseHardness;
      private final double maximumHardness;

      Profile(int baseBudget, int maximumBudget, double baseRadius, double maximumRadius, double baseHardness, double maximumHardness) {
         this.baseBudget = baseBudget;
         this.maximumBudget = maximumBudget;
         this.baseRadius = baseRadius;
         this.maximumRadius = maximumRadius;
         this.baseHardness = baseHardness;
         this.maximumHardness = maximumHardness;
      }
   }

   private record QueuedBlock(ResourceKey<Level> dimension, long position) {
   }

   private record ScaledProfile(int budget, double radius, double maximumHardness) {
   }

   private record ScoredPos(BlockPos pos, double score) {
   }
}
