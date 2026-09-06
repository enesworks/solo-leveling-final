package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import dev.eness.sololevelingfinal.core.entity.SilladBossEntity;

public final class SilladFrozenDomainManager {
   private static final int MAX_RADIUS = SilladBossRules.frozenDomainRadius(3);
   private static final int WAKE_RADIUS = 5;
   private static final int GLOBAL_INSPECTION_BUDGET = 384;
   private static final int MAIN_COLUMN_BUDGET = 40;
   private static final int WAKE_COLUMN_BUDGET = 8;
   private static final int MAX_PARTICLE_MUTATIONS = 10;
   private static final TagKey<Block> DOMAIN_IMMUNE = TagKey.create(Registries.BLOCK, new ResourceLocation("sololeveling", "sillad_domain_immune"));
   private static final List<SilladFrozenDomainManager.ColumnOffset> OFFSETS = createOffsets(MAX_RADIUS);
   private static final List<SilladFrozenDomainManager.ColumnOffset> WAKE_OFFSETS = createOffsets(5);
   private static final Map<UUID, SilladFrozenDomainManager.DomainState> STATES = new HashMap<>();
   private static long budgetTick = Long.MIN_VALUE;
   private static int globalInspections;
   private static int globalMutations;

   private SilladFrozenDomainManager() {
   }

   public static void tick(SilladBossEntity sillad) {
      if (sillad != null && sillad.isAlive() && sillad.level() instanceof ServerLevel level && ForgeEventFactory.getMobGriefingEvent(level, sillad)) {
         refreshGlobalBudget(level.getGameTime());
         if (globalInspections > 0 && globalMutations > 0) {
            SilladFrozenDomainManager.DomainState state = STATES.computeIfAbsent(
               sillad.getUUID(), ignored -> new SilladFrozenDomainManager.DomainState(sillad.getEncounterHome(), sillad.blockPosition())
            );
            state.requestedRadius = Math.max(state.requestedRadius, SilladBossRules.frozenDomainRadius(sillad.getCombatPhase()));
            BlockPos encounterHome = sillad.getEncounterHome();
            if (!sameHorizontal(state.mainCenter, encounterHome) || Math.abs(state.mainCenter.getY() - encounterHome.getY()) > 2) {
               state.mainCenter = encounterHome.immutable();
               state.mainCursor = 0;
            }

            BlockPos current = sillad.blockPosition();
            if (horizontalDistanceSqr(state.wakeCenter, current) >= 9 || Math.abs(state.wakeCenter.getY() - current.getY()) > 2) {
               state.wakeCenter = current.immutable();
               state.wakeCursor = 0;
            }

            SilladFrozenDomainManager.WorkBudget work = new SilladFrozenDomainManager.WorkBudget(
               Math.min(384, globalInspections), Math.min(48, globalMutations)
            );
            int phase = Math.max(sillad.getCombatPhase(), phaseForRequestedRadius(state.requestedRadius));
            processSweep(level, sillad, state, false, phase, 40, work);
            processSweep(level, sillad, state, true, phase, 8, work);
            globalInspections = globalInspections - work.usedInspections;
            globalMutations = globalMutations - work.usedMutations;
            if (work.usedMutations > 0 && Math.floorMod(sillad.tickCount + sillad.getId(), 8) == 0) {
               level.playSound((Player)null, sillad.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.HOSTILE, 0.55F, 0.62F);
            }

            if (sillad.tickCount % 10 == 0 && state.mainCursor > 0) {
               SilladFrozenDomainManager.ColumnOffset edge = OFFSETS.get(Math.min(state.mainCursor - 1, OFFSETS.size() - 1));
               double radius = Math.min(state.requestedRadius, Math.sqrt(edge.distanceSquared));
               spawnRing(level, Vec3.atBottomCenterOf(state.mainCenter).add(0.0, 0.16, 0.0), Math.max(1.0, radius));
            }
         }
      }
   }

   public static void requestExpansion(SilladBossEntity sillad, int phase) {
      if (sillad != null && !sillad.level().isClientSide()) {
         SilladFrozenDomainManager.DomainState state = STATES.computeIfAbsent(
            sillad.getUUID(), ignored -> new SilladFrozenDomainManager.DomainState(sillad.getEncounterHome(), sillad.blockPosition())
         );
         state.requestedRadius = Math.max(state.requestedRadius, SilladBossRules.frozenDomainRadius(phase));
      }
   }

   public static void cleanup(SilladBossEntity sillad) {
      if (sillad != null) {
         STATES.remove(sillad.getUUID());
      }
   }

   private static void processSweep(
      ServerLevel level,
      SilladBossEntity sillad,
      SilladFrozenDomainManager.DomainState state,
      boolean wake,
      int phase,
      int columnBudget,
      SilladFrozenDomainManager.WorkBudget work
   ) {
      List<SilladFrozenDomainManager.ColumnOffset> offsets = wake ? WAKE_OFFSETS : OFFSETS;
      BlockPos center = wake ? state.wakeCenter : state.mainCenter;
      int cursor = wake ? state.wakeCursor : state.mainCursor;
      int radius = wake ? 5 : state.requestedRadius;
      int attempted = 0;

      while (cursor < offsets.size() && attempted < columnBudget && work.canInspect() && work.canMutate()) {
         SilladFrozenDomainManager.ColumnOffset offset = offsets.get(cursor);
         if (offset.distanceSquared > radius * radius) {
            break;
         }

         cursor++;
         attempted++;
         freezeColumn(level, sillad, center.offset(offset.x, 0, offset.z), wake ? 6 : 10, wake ? 6 : 8, phase, work);
      }

      if (wake) {
         state.wakeCursor = cursor >= offsets.size() ? 0 : cursor;
      } else {
         state.mainCursor = cursor;
      }
   }

   private static void freezeColumn(
      ServerLevel level, SilladBossEntity sillad, BlockPos column, int above, int below, int phase, SilladFrozenDomainManager.WorkBudget work
   ) {
      BlockPos decoration = null;

      for (int y = column.getY() + above; y >= column.getY() - below && work.canInspect(); y--) {
         BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
         work.inspect();
         if (validPosition(level, pos)) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
               boolean rawWater = state.is(Blocks.WATER) || state.getFluidState().is(FluidTags.WATER) && state.getBlock() == Blocks.WATER;
               boolean rawLava = state.is(Blocks.LAVA) || state.getFluidState().is(FluidTags.LAVA) && state.getBlock() == Blocks.LAVA;
               if (rawWater || rawLava || state.is(Blocks.POWDER_SNOW)) {
                  replace(level, sillad, pos, state, frozenState(phase, pos, rawLava), work);
                  return;
               }

               if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                  return;
               }

               if (!state.getCollisionShape(level, pos).isEmpty()) {
                  if (state.isCollisionShapeFullBlock(level, pos) && canConvert(level, sillad, pos, state)) {
                     BlockState replacement = frozenState(phase, pos, false);
                     if (replace(level, sillad, pos, state, replacement, work)
                        && decoration != null
                        && work.canMutate()
                        && snowCell(pos, phase)
                        && level.getEntitiesOfClass(LivingEntity.class, new AABB(decoration), LivingEntity::isAlive).isEmpty()) {
                        BlockState snow = Blocks.SNOW.defaultBlockState();
                        if (snow.canSurvive(level, decoration)) {
                           replace(level, sillad, decoration, level.getBlockState(decoration), snow, work);
                        }
                     }

                     return;
                  }

                  return;
               }

               if (state.canBeReplaced() && decoration == null) {
                  decoration = pos.immutable();
               }
            }
         }
      }
   }

   private static boolean replace(
      ServerLevel level, SilladBossEntity sillad, BlockPos pos, BlockState previous, BlockState replacement, SilladFrozenDomainManager.WorkBudget work
   ) {
      if (work.canMutate()
         && previous != replacement
         && !previous.equals(replacement)
         && level.getBlockEntity(pos) == null
         && !previous.is(DOMAIN_IMMUNE)
         && !previous.is(BlockTags.WITHER_IMMUNE)
         && !(previous.getDestroySpeed(level, pos) < 0.0F)
         && ForgeHooks.canEntityDestroy(level, pos, sillad)) {
         BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
         if (!level.setBlock(pos, replacement, 3)) {
            return false;
         }

         if (ForgeEventFactory.onBlockPlace(sillad, snapshot, Direction.UP)) {
            snapshot.restore(true, false);
            return false;
         }

         work.mutate();
         if (work.usedMutations <= 10) {
            level.sendParticles(
               new BlockParticleOption(ParticleTypes.BLOCK, replacement), pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5, 4, 0.3, 0.24, 0.3, 0.045
            );
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean canConvert(ServerLevel level, SilladBossEntity sillad, BlockPos pos, BlockState state) {
      return !state.is(Blocks.PACKED_ICE)
            && !state.is(Blocks.BLUE_ICE)
            && !state.is(DOMAIN_IMMUNE)
            && !state.is(BlockTags.WITHER_IMMUNE)
            && level.getBlockEntity(pos) == null
            && !(state.getDestroySpeed(level, pos) < 0.0F)
         ? ForgeHooks.canEntityDestroy(level, pos, sillad)
         : false;
   }

   private static boolean validPosition(ServerLevel level, BlockPos pos) {
      return pos.getY() > level.getMinBuildHeight()
         && pos.getY() < level.getMaxBuildHeight() - 1
         && level.getWorldBorder().isWithinBounds(pos)
         && level.hasChunkAt(pos);
   }

   private static BlockState frozenState(int phase, BlockPos pos, boolean lava) {
      long mixed = pos.asLong() * -7046029254386353131L;
      int divisor = phase >= 3 ? 2 : (phase >= 2 ? 4 : 9);
      boolean blue = lava || Math.floorMod(Long.hashCode(mixed), divisor) == 0;
      return (blue ? Blocks.BLUE_ICE : Blocks.PACKED_ICE).defaultBlockState();
   }

   private static boolean snowCell(BlockPos pos, int phase) {
      int divisor = phase >= 3 ? 5 : 8;
      return Math.floorMod(Long.hashCode(pos.asLong() ^ -4126379630918251389L), divisor) == 0;
   }

   private static List<SilladFrozenDomainManager.ColumnOffset> createOffsets(int radius) {
      List<SilladFrozenDomainManager.ColumnOffset> result = new ArrayList<>();

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            int distance = x * x + z * z;
            if (distance <= radius * radius) {
               result.add(new SilladFrozenDomainManager.ColumnOffset(x, z, distance));
            }
         }
      }

      result.sort(
         Comparator.comparingInt(SilladFrozenDomainManager.ColumnOffset::distanceSquared)
            .thenComparingInt(SilladFrozenDomainManager.ColumnOffset::x)
            .thenComparingInt(SilladFrozenDomainManager.ColumnOffset::z)
      );
      return List.copyOf(result);
   }

   private static void spawnRing(ServerLevel level, Vec3 center, double radius) {
      int points = Math.max(16, Math.min(48, (int)Math.ceil(radius * 2.4)));

      for (int index = 0; index < points; index++) {
         double angle = (Math.PI * 2) * index / points;
         level.sendParticles(
            ParticleTypes.SNOWFLAKE, center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius, 1, 0.04, 0.03, 0.04, 0.005
         );
      }
   }

   private static int phaseForRequestedRadius(int radius) {
      if (radius >= SilladBossRules.frozenDomainRadius(3)) {
         return 3;
      } else {
         return radius >= SilladBossRules.frozenDomainRadius(2) ? 2 : 1;
      }
   }

   private static void refreshGlobalBudget(long gameTime) {
      if (budgetTick != gameTime) {
         budgetTick = gameTime;
         globalInspections = 384;
         globalMutations = 48;
      }
   }

   private static boolean sameHorizontal(BlockPos first, BlockPos second) {
      return first.getX() == second.getX() && first.getZ() == second.getZ();
   }

   private static int horizontalDistanceSqr(BlockPos first, BlockPos second) {
      int x = first.getX() - second.getX();
      int z = first.getZ() - second.getZ();
      return x * x + z * z;
   }

   private record ColumnOffset(int x, int z, int distanceSquared) {
   }

   private static final class DomainState {
      private BlockPos mainCenter;
      private BlockPos wakeCenter;
      private int requestedRadius = SilladBossRules.frozenDomainRadius(1);
      private int mainCursor;
      private int wakeCursor;

      private DomainState(BlockPos mainCenter, BlockPos wakeCenter) {
         this.mainCenter = mainCenter.immutable();
         this.wakeCenter = wakeCenter.immutable();
      }
   }

   private static final class WorkBudget {
      private final int inspectionLimit;
      private final int mutationLimit;
      private int usedInspections;
      private int usedMutations;

      private WorkBudget(int inspectionLimit, int mutationLimit) {
         this.inspectionLimit = Math.max(0, inspectionLimit);
         this.mutationLimit = Math.max(0, mutationLimit);
      }

      private boolean canInspect() {
         return this.usedInspections < this.inspectionLimit;
      }

      private boolean canMutate() {
         return this.usedMutations < this.mutationLimit;
      }

      private void inspect() {
         this.usedInspections++;
      }

      private void mutate() {
         this.usedMutations++;
      }
   }
}
