package dev.eness.sololevelingfinal.core.util.daily;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.TrainingBotEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.DailyQuestHelper;
import dev.eness.sololevelingfinal.core.util.AbilityDestructionManager;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;

@EventBusSubscriber(modid = "sololeveling")
public final class DailyQuestObjectiveManager {
   public static final double NORMAL_MINING_TARGET = 32.0;
   public static final double SECRET_MINING_TARGET = 64.0;
   public static final double NORMAL_THREAT_TARGET = 8.0;
   public static final double SECRET_THREAT_TARGET = 16.0;
   public static final double NORMAL_DISTANCE_TARGET = 500.0;
   public static final double SECRET_DISTANCE_TARGET = 1000.0;
   public static final TagKey<Block> DAILY_MINEABLE = TagKey.create(Registries.BLOCK, new ResourceLocation("sololeveling", "daily_mineable"));
   public static final TagKey<EntityType<?>> DAILY_THREAT_EXCLUDED = entityTag("daily_threat_excluded");
   public static final TagKey<EntityType<?>> DAILY_THREAT_ELITES = entityTag("daily_threat_elites");
   public static final TagKey<EntityType<?>> DAILY_THREAT_MINIBOSSES = entityTag("daily_threat_minibosses");
   public static final TagKey<EntityType<?>> DAILY_THREAT_BOSSES = entityTag("daily_threat_bosses");
   private static final String MINED_POSITIONS_TAG = "slr_daily_mined_positions";
   private static final String MINED_DIMENSION_TAG = "Dimension";
   private static final String MINED_POSITION_TAG = "Position";
   private static final String COMPLETION_FIRED_TAG = "slr_daily_objectives_completion_fired";
   private static final String RADIRU_RESIDENT_TAG = "radiru_resident";
   private static final String RADIRU_TRAINING_DUMMY_TAG = "radiru_training_dummy";
   public static final String SYSTEM_TRAINING_OWNER_TAG = "slr_training_owner";
   private static final int SYSTEM_TRAINING_BOT_THREAT_POINTS = 8;
   private static final double MAX_ON_FOOT_DISTANCE_PER_TICK = 2.5;
   private static final long CLIENT_SYNC_INTERVAL = 20L;
   private static final Map<UUID, DailyQuestObjectiveManager.RuntimeState> RUNTIME = new HashMap<>();

   private DailyQuestObjectiveManager() {
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onBlockBroken(BreakEvent event) {
      if (event.getPlayer() instanceof ServerPlayer player
         && !(player instanceof FakePlayer)
         && !AbilityDestructionManager.isPostingAbilityBreakEvent()
         && isSurvival(player)
         && isQuestActive(player)) {
         BlockState state = event.getState();
         if (isDailyMineable(state) && hasCorrectMiningTool(player, state)) {
            recordMinedBlock(player, event.getPos());
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLivingDeath(LivingDeathEvent event) {
      if (event.getEntity().level() instanceof ServerLevel) {
         ServerPlayer player = creditedPlayer(event.getSource());
         if (player != null && !(player instanceof FakePlayer) && isSurvival(player) && isQuestActive(player)) {
            LivingEntity target = event.getEntity();
            boolean systemTrainingBot = isOwnedSystemTrainingBot(target, player);
            if (systemTrainingBot || isEligibleThreat(target, player)) {
               int points = systemTrainingBot ? 8 : threatWeight(target);
               if (points > 0) {
                  recordThreatPoints(player, points);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         SololevelingModVariables.PlayerVariables variables = variables(player);
         if (variables != null && variables.ActiveDaily && SystemPlayerAccess.hasSystem(player)) {
            DailyQuestObjectiveManager.RuntimeState runtime = RUNTIME.computeIfAbsent(
               player.getUUID(), ignored -> new DailyQuestObjectiveManager.RuntimeState()
            );
            if (variables.dailyMinedBlocks <= 0.0 && variables.dailyThreatPoints <= 0.0 && variables.RUN <= 0.0) {
               resetFreshQuestMarkers(player);
            }

            trackOnFootDistance(player, variables, runtime);
            boolean complete = evaluateCompletion(player, variables);
            if (runtime.dirty && (complete || runtime.lastSyncAt == Long.MIN_VALUE || player.level().getGameTime() - runtime.lastSyncAt >= 20L)) {
               syncNow(player, variables, runtime);
            }
         } else {
            clearInactiveQuestRuntime(player);
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         resetMovementAnchor(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         RUNTIME.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawned(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         resetMovementAnchor(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         resetMovementAnchor(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerTeleported(EntityTeleportEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         resetMovementAnchor(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerClone(Clone event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CompoundTag var4 = event.getOriginal().getPersistentData();
         CompoundTag newData = player.getPersistentData();
         if (var4.contains("slr_daily_mined_positions", 9)) {
            newData.put("slr_daily_mined_positions", var4.getList("slr_daily_mined_positions", 10).copy());
         }

         if (var4.getBoolean("slr_daily_objectives_completion_fired")) {
            newData.putBoolean("slr_daily_objectives_completion_fired", true);
         }

         resetMovementAnchor(player);
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      RUNTIME.clear();
   }

   public static boolean isQuestActive(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables variables = variables(player);
      return variables != null && variables.ActiveDaily && SystemPlayerAccess.hasSystem(player);
   }

   public static boolean isSecretQuest(ServerPlayer player) {
      return DailyQuestHelper.isSecretQuest(player);
   }

   public static double miningTarget(ServerPlayer player) {
      return isSecretQuest(player) ? 64.0 : 32.0;
   }

   public static double threatTarget(ServerPlayer player) {
      return isSecretQuest(player) ? 16.0 : 8.0;
   }

   public static double distanceTarget(ServerPlayer player) {
      return isSecretQuest(player) ? 1000.0 : 500.0;
   }

   public static boolean isThreatObjectiveRequired(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables variables = variables(player);
      return player != null && variables != null && !variables.dailyCombatWaived;
   }

   public static boolean isDailyMineable(BlockState state) {
      return state != null && state.is(DAILY_MINEABLE);
   }

   public static boolean hasCorrectMiningTool(ServerPlayer player, BlockState state) {
      if (player != null && state != null) {
         ItemStack tool = player.getMainHandItem();
         return !tool.isEmpty() && tool.isCorrectToolForDrops(state);
      } else {
         return false;
      }
   }

   public static boolean recordMinedBlock(ServerPlayer player, BlockPos position) {
      if (player != null && position != null && isSurvival(player) && isQuestActive(player)) {
         SololevelingModVariables.PlayerVariables variables = variables(player);
         if (variables != null && !(variables.dailyMinedBlocks >= miningTarget(player)) && rememberMinedPosition(player, position, variables.dailyMinedBlocks)) {
            double previous = variables.dailyMinedBlocks;
            variables.dailyMinedBlocks = Math.min(miningTarget(player), previous + 1.0);
            markDirty(player);
            DailyQuestHelper.checkSecretTransition(player, previous, variables.dailyMinedBlocks, 32.0);
            evaluateCompletion(player, variables);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean recordThreatPoints(ServerPlayer player, int points) {
      if (player != null && points > 0 && isSurvival(player) && isQuestActive(player) && isThreatObjectiveRequired(player)) {
         SololevelingModVariables.PlayerVariables variables = variables(player);
         if (variables != null && !(variables.dailyThreatPoints >= threatTarget(player))) {
            double previous = variables.dailyThreatPoints;
            variables.dailyThreatPoints = Math.min(threatTarget(player), previous + points);
            markDirty(player);
            DailyQuestHelper.checkSecretTransition(player, previous, variables.dailyThreatPoints, 8.0);
            evaluateCompletion(player, variables);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean recordDistance(ServerPlayer player, double blocks) {
      if (player != null && Double.isFinite(blocks) && !(blocks <= 0.0) && isSurvival(player) && isQuestActive(player)) {
         SololevelingModVariables.PlayerVariables variables = variables(player);
         if (variables != null && !(variables.RUN >= distanceTarget(player))) {
            double previous = variables.RUN;
            variables.RUN = Math.min(distanceTarget(player), previous + blocks);
            markDirty(player);
            DailyQuestHelper.checkSecretTransition(player, previous, variables.RUN, 500.0);
            evaluateCompletion(player, variables);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isComplete(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables variables = variables(player);
      return variables != null && variables.ActiveDaily && objectivesComplete(player, variables);
   }

   public static DailyQuestObjectiveManager.ProgressSnapshot snapshot(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables variables = variables(player);
      boolean secret = isSecretQuest(player);
      double mined = variables == null ? 0.0 : Math.max(0.0, variables.dailyMinedBlocks);
      double threat = variables == null ? 0.0 : Math.max(0.0, variables.dailyThreatPoints);
      double distance = variables == null ? 0.0 : Math.max(0.0, variables.RUN);
      return new DailyQuestObjectiveManager.ProgressSnapshot(
         secret, mined, secret ? 64.0 : 32.0, threat, secret ? 16.0 : 8.0, distance, secret ? 1000.0 : 500.0, isThreatObjectiveRequired(player)
      );
   }

   public static boolean evaluateCompletion(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables variables = variables(player);
      return variables != null && evaluateCompletion(player, variables);
   }

   public static void resetQuestRuntime(ServerPlayer player) {
      if (player != null) {
         RUNTIME.remove(player.getUUID());
         CompoundTag data = player.getPersistentData();
         data.remove("slr_daily_mined_positions");
         data.remove("slr_daily_objectives_completion_fired");
      }
   }

   public static boolean isEligibleThreat(LivingEntity target, ServerPlayer player) {
      if (target != null && player != null && target instanceof Enemy && !target.getType().is(DAILY_THREAT_EXCLUDED)) {
         CompoundTag data = target.getPersistentData();
         if (!data.getBoolean("radiru_training_dummy") && !data.getBoolean("radiru_resident") && !ShadowMonarchManager.isShadowEntity(target)) {
            if (target instanceof TamableAnimal tame && tame.isTame()) {
               return false;
            } else {
               return target instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null
                  ? false
                  : !player.isAlliedTo(target) && !target.isAlliedTo(player);
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isOwnedSystemTrainingBot(LivingEntity target, ServerPlayer player) {
      if (target instanceof TrainingBotEntity && player != null) {
         CompoundTag targetData = target.getPersistentData();
         if (!targetData.getBoolean("radiru_training_dummy")
            && !targetData.getBoolean("radiru_resident")
            && targetData.hasUUID("slr_training_owner")
            && player.getUUID().equals(targetData.getUUID("slr_training_owner"))) {
            SololevelingModVariables.PlayerVariables variables = variables(player);
            return variables != null && variables.istraining;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static int threatWeight(LivingEntity target) {
      if (target == null) {
         return 0;
      }

      EntityType<?> type = target.getType();
      if (type.is(DAILY_THREAT_BOSSES)) {
         return 8;
      }

      if (type.is(DAILY_THREAT_MINIBOSSES)) {
         return 4;
      }

      if (type.is(DAILY_THREAT_ELITES)) {
         return 3;
      }

      CompoundTag data = target.getPersistentData();
      String role = data.getString("slr_dungeon_role").trim().toLowerCase(Locale.ROOT);
      if (!role.isEmpty()) {
         if (role.equals("boss")) {
            return 8;
         }

         if (role.equals("miniboss") || role.equals("mini_boss")) {
            return 4;
         }

         if (role.equals("elite")) {
            return 3;
         }

         if (role.equals("normal")) {
            return 1;
         }
      }

      if (target instanceof EnderDragon || target instanceof WitherBoss || booleanTag(data, "Boss", "boss", "slr_boss")) {
         return 8;
      }

      if (booleanTag(data, "Miniboss", "miniboss", "mini_boss", "slr_miniboss")) {
         return 4;
      }

      if (booleanTag(data, "Elite", "elite", "slr_elite")) {
         return 3;
      }

      ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
      String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
      if (path.contains("boss")) {
         return 8;
      }

      if (!path.contains("miniboss") && !path.contains("mini_boss")) {
         double maximumHealth = target.getMaxHealth();
         if (maximumHealth >= 300.0) {
            return 8;
         } else if (maximumHealth >= 160.0) {
            return 4;
         } else {
            return maximumHealth >= 60.0 ? 3 : 1;
         }
      } else {
         return 4;
      }
   }

   @Nullable
   public static ServerPlayer creditedPlayer(DamageSource source) {
      if (source == null) {
         return null;
      }

      Set<UUID> visited = new HashSet<>();
      ServerPlayer credited = creditedPlayer(source.getEntity(), visited);
      return credited != null ? credited : creditedPlayer(source.getDirectEntity(), visited);
   }

   private static boolean evaluateCompletion(ServerPlayer player, SololevelingModVariables.PlayerVariables variables) {
      if (variables.ActiveDaily && objectivesComplete(player, variables)) {
         CompoundTag data = player.getPersistentData();
         if (data.getBoolean("slr_daily_objectives_completion_fired")) {
            return true;
         }

         data.putBoolean("slr_daily_objectives_completion_fired", true);
         DailyQuestObjectiveManager.RuntimeState runtime = RUNTIME.computeIfAbsent(player.getUUID(), ignored -> new DailyQuestObjectiveManager.RuntimeState());
         if (runtime.dirty) {
            syncNow(player, variables, runtime);
         }

         MinecraftForge.EVENT_BUS.post(new DailyQuestObjectivesCompletedEvent(player, snapshot(player)));
         return true;
      } else {
         return false;
      }
   }

   private static boolean objectivesComplete(ServerPlayer player, SololevelingModVariables.PlayerVariables variables) {
      return variables.dailyMinedBlocks >= miningTarget(player)
         && (!isThreatObjectiveRequired(player) || variables.dailyThreatPoints >= threatTarget(player))
         && variables.RUN >= distanceTarget(player);
   }

   private static void trackOnFootDistance(
      ServerPlayer player, SololevelingModVariables.PlayerVariables variables, DailyQuestObjectiveManager.RuntimeState runtime
   ) {
      long gameTime = player.level().getGameTime();
      ResourceKey<Level> dimension = player.level().dimension();
      Vec3 position = player.position();
      boolean eligible = isOnFoot(player);
      DailyQuestObjectiveManager.MovementAnchor previous = runtime.anchor;
      runtime.anchor = new DailyQuestObjectiveManager.MovementAnchor(dimension, position, gameTime, eligible);
      if (previous != null
         && previous.dimension.equals(dimension)
         && previous.gameTime + 1L == gameTime
         && previous.eligible
         && eligible
         && !(variables.RUN >= distanceTarget(player))) {
         double dx = position.x - previous.position.x;
         double dz = position.z - previous.position.z;
         double horizontal = Math.sqrt(dx * dx + dz * dz);
         if (!(horizontal <= 0.0) && !(horizontal > 2.5)) {
            recordDistance(player, horizontal);
         }
      }
   }

   private static boolean isOnFoot(ServerPlayer player) {
      return isSurvival(player)
         && player.isAlive()
         && !player.isSleeping()
         && !player.isPassenger()
         && !player.isFallFlying()
         && !player.getAbilities().flying
         && !player.isSwimming()
         && !player.isInWaterOrBubble()
         && !player.isInLava();
   }

   private static boolean isSurvival(ServerPlayer player) {
      return player != null && !player.isCreative() && !player.isSpectator();
   }

   private static void resetMovementAnchor(ServerPlayer player) {
      if (player != null) {
         DailyQuestObjectiveManager.RuntimeState runtime = RUNTIME.computeIfAbsent(player.getUUID(), ignored -> new DailyQuestObjectiveManager.RuntimeState());
         runtime.anchor = null;
      }
   }

   private static void markDirty(ServerPlayer player) {
      RUNTIME.computeIfAbsent(player.getUUID(), ignored -> new DailyQuestObjectiveManager.RuntimeState()).dirty = true;
   }

   private static void syncNow(ServerPlayer player, SololevelingModVariables.PlayerVariables variables, DailyQuestObjectiveManager.RuntimeState runtime) {
      variables.syncPlayerVariables(player);
      runtime.dirty = false;
      runtime.lastSyncAt = player.level().getGameTime();
   }

   private static void clearInactiveQuestRuntime(ServerPlayer player) {
      RUNTIME.remove(player.getUUID());
      CompoundTag data = player.getPersistentData();
      data.remove("slr_daily_mined_positions");
      data.remove("slr_daily_objectives_completion_fired");
   }

   private static void resetFreshQuestMarkers(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      data.remove("slr_daily_mined_positions");
      data.remove("slr_daily_objectives_completion_fired");
   }

   private static boolean rememberMinedPosition(ServerPlayer player, BlockPos position, double currentProgress) {
      CompoundTag data = player.getPersistentData();
      if (currentProgress <= 0.0) {
         data.remove("slr_daily_mined_positions");
      }

      ListTag positions = data.getList("slr_daily_mined_positions", 10);
      String dimension = player.level().dimension().location().toString();
      long packedPosition = position.asLong();

      for (Tag entry : positions) {
         if (entry instanceof CompoundTag mined && packedPosition == mined.getLong("Position") && dimension.equals(mined.getString("Dimension"))) {
            return false;
         }
      }

      CompoundTag mined = new CompoundTag();
      mined.putString("Dimension", dimension);
      mined.putLong("Position", packedPosition);
      positions.add(mined);
      data.put("slr_daily_mined_positions", positions);
      return true;
   }

   @Nullable
   private static ServerPlayer creditedPlayer(@Nullable Entity source, Set<UUID> visited) {
      if (source == null || !visited.add(source.getUUID())) {
         return null;
      } else if (source instanceof ServerPlayer player) {
         return player instanceof FakePlayer ? null : player;
      } else {
         if (source instanceof Projectile projectile) {
            ServerPlayer owner = creditedPlayer(projectile.getOwner(), visited);
            if (owner != null) {
               return owner;
            }
         }

         if (source instanceof TamableAnimal tame) {
            ServerPlayer owner = creditedPlayer(tame.getOwner(), visited);
            if (owner != null) {
               return owner;
            }
         }

         if (source instanceof OwnableEntity ownable) {
            ServerPlayer owner = creditedPlayer(ownable.getOwner(), visited);
            if (owner != null) {
               return owner;
            }
         }

         if (source.getServer() != null) {
            UUID ownerId = ShadowMonarchManager.getShadowOwnerUUID(source);
            if (ownerId != null) {
               return source.getServer().getPlayerList().getPlayer(ownerId);
            }
         }

         return null;
      }
   }

   private static boolean booleanTag(CompoundTag data, String... keys) {
      for (String key : keys) {
         if (data.getBoolean(key)) {
            return true;
         }
      }

      return false;
   }

   private static TagKey<EntityType<?>> entityTag(String path) {
      return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("sololeveling", path));
   }

   @Nullable
   private static SololevelingModVariables.PlayerVariables variables(@Nullable ServerPlayer player) {
      return player == null ? null : player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
   }

   private record MovementAnchor(ResourceKey<Level> dimension, Vec3 position, long gameTime, boolean eligible) {
   }

   public record ProgressSnapshot(
      boolean secretQuest,
      double minedBlocks,
      double miningTarget,
      double threatPoints,
      double threatTarget,
      double distance,
      double distanceTarget,
      boolean threatRequired
   ) {
      public boolean complete() {
         return this.minedBlocks >= this.miningTarget
            && (!this.threatRequired || this.threatPoints >= this.threatTarget)
            && this.distance >= this.distanceTarget;
      }
   }

   private static final class RuntimeState {
      private DailyQuestObjectiveManager.MovementAnchor anchor;
      private long lastSyncAt = Long.MIN_VALUE;
      private boolean dirty;
   }
}
