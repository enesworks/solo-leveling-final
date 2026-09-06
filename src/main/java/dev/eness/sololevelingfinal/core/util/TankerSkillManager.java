package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.FlagOfProtectionEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.ClassPassiveMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.TankerProgressionHelper;
import dev.eness.sololevelingfinal.core.procedures.TankerProgressionRules;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE)
public final class TankerSkillManager {
   public static final String TAUNT = "Taunt";
   public static final String REINFORCEMENT = "Reinforcement";
   public static final String TANK_LEAP = "Tank Leap";
   public static final String SHIELD_BASH = "Shield Bash";
   public static final String WILLPOWER = "Willpower";
   public static final String PROTECTION_MARK = "Protection Mark";
   public static final List<String> UNLOCK_ORDER = TankerProgressionRules.MASTERY_ORDER;
   public static final Set<String> SKILLS = Set.copyOf(UNLOCK_ORDER);
   public static final byte VFX_LEAP_START = 0;
   public static final byte VFX_LEAP_LAND = 1;
   public static final byte VFX_TAUNT_RING = 2;
   public static final byte VFX_BASH_SWEEP = 3;
   public static final byte VFX_BASH_HIT = 4;
   public static final byte VFX_BASH_STRAIN_RELIEF = 5;
   public static final byte VFX_BRACE_START = 6;
   public static final byte VFX_BRACE_HIT = 7;
   public static final byte VFX_STANCE_START = 8;
   public static final byte VFX_STANCE_END = 9;
   public static final byte VFX_WILLPOWER_START = 10;
   public static final byte VFX_WILLPOWER_THRESHOLD = 11;
   public static final byte VFX_WILLPOWER_SETTLE = 12;
   public static final byte VFX_WILLPOWER_BREAK = 13;
   public static final byte VFX_MARK_DEPLOY = 14;
   public static final byte VFX_MARK_THRESHOLD = 15;
   public static final byte VFX_MARK_BREAK = 16;
   public static final byte VFX_MARK_CANCEL = 17;
   private static final int VFX_FLAG_ESSENTIAL = 1;
   private static final int VFX_FLAG_CONFIRMED_HIT = 2;
   private static final int VFX_FLAG_PVP = 4;
   public static final int TAUNT_FLAT_COST = 100;
   public static final double TAUNT_PERCENT_COST = 0.015;
   public static final int TAUNT_COOLDOWN = 240;
   public static final int TAUNT_REGEN_LOCK = 30;
   public static final int SHIELD_BASH_FLAT_COST = 180;
   public static final double SHIELD_BASH_PERCENT_COST = 0.025;
   public static final int SHIELD_BASH_COOLDOWN = 160;
   public static final int SHIELD_BASH_REGEN_LOCK = 30;
   public static final int TANK_LEAP_FLAT_COST = 260;
   public static final double TANK_LEAP_PERCENT_COST = 0.04;
   public static final int TANK_LEAP_COOLDOWN = 280;
   public static final int TANK_LEAP_REGEN_LOCK = 40;
   public static final int REINFORCEMENT_FLAT_COST = 400;
   public static final double REINFORCEMENT_PERCENT_COST = 0.06;
   public static final int REINFORCEMENT_COOLDOWN = 440;
   public static final int REINFORCEMENT_REGEN_LOCK = 50;
   public static final int WILLPOWER_FLAT_COST = 650;
   public static final double WILLPOWER_PERCENT_COST = 0.09;
   public static final int WILLPOWER_COOLDOWN = 900;
   public static final int WILLPOWER_REGEN_LOCK = 60;
   public static final int PROTECTION_MARK_FLAT_COST = 900;
   public static final double PROTECTION_MARK_PERCENT_COST = 0.12;
   public static final int PROTECTION_MARK_COOLDOWN = 1200;
   public static final int PROTECTION_MARK_REGEN_LOCK = 60;
   private static final int TANKER_CLASS = 4;
   private static final int IRON_WALL_MAX = 10;
   private static final int IRON_WALL_DURATION = 200;
   private static final double IRON_WALL_PVE_PER_STACK = 0.02;
   private static final double IRON_WALL_PVP_PER_STACK = 0.01;
   private static final double TAUNT_RANGE_SQR = 144.0;
   public static final int TAUNT_TARGET_CAP = 16;
   public static final int TAUNT_MOB_DURATION = 120;
   public static final int TAUNT_MAINTENANCE_INTERVAL = 10;
   private static final int TAUNT_BOSS_DURATION = 40;
   private static final int CHALLENGED_DURATION = 60;
   private static final double CHALLENGED_DAMAGE_MULTIPLIER = 0.85;
   private static final double BASH_REACH = 3.6;
   private static final int BASH_TRAVEL_TICKS = 4;
   public static final int SHIELD_BASH_TARGET_CAP = 1;
   private static final double LEAP_RANGE = 8.0;
   private static final double LEAP_IMPACT_RADIUS = 5.0;
   public static final int LEAP_DEADLINE = 12;
   public static final int LEAP_TARGET_CAP = 16;
   public static final int PERFECT_BRACE_DURATION = 12;
   public static final int REINFORCED_STANCE_DURATION = 80;
   public static final int MAX_REINFORCEMENT_PHASES = 1;
   public static final int WILLPOWER_DURATION = 160;
   public static final int WILLPOWER_PULSES = 4;
   public static final int WILLPOWER_PULSE_INTERVAL = 10;
   public static final int MAX_WILLPOWER_STATES = 1;
   private static final double WILLPOWER_RELIEF_FRACTION = 0.08;
   private static final double MARK_RADIUS = 6.0;
   private static final double MARK_RADIUS_SQR = 36.0;
   public static final int MARK_DURATION = 240;
   public static final int MARK_MEMBERSHIP_INTERVAL = 10;
   public static final int MARK_BENEFICIARY_CAP = 8;
   public static final int MAX_PROTECTION_MARKS_PER_OWNER = 1;
   private static final double MARK_INTEGRITY_FRACTION = 0.75;
   private static final double EPSILON = 1.0E-7;
   private static final String IRON_STACKS_TAG = "sl_t_stacks";
   private static final String LEGACY_IRON_TIMER_TAG = "sl_t_timer";
   private static final String IRON_EXPIRES_TAG = "slr_tanker_iron_wall_expires_at";
   private static final String IRON_LAST_DAMAGE_TICK_TAG = "slr_tanker_iron_wall_last_damage_tick";
   private static final String MASTERY_LOCK = "mastery";
   private static final String MANA_REGEN_LOCK = "mana_refresh";
   private static final String WP_DEBT_TAG = "slr_tanker_willpower_debt";
   private static final String WP_MAX_HEALTH_TAG = "slr_tanker_willpower_max_health";
   private static final String WP_PULSES_TAG = "slr_tanker_willpower_pulses";
   private static final UUID CONTROL_SLOW_MODIFIER_ID = UUID.fromString("485234e8-34c6-4468-b62c-c1d2dc5650e4");
   private static final Map<String, TankerSkillManager.SkillBalance> BALANCE = Map.of(
      "Taunt",
      new TankerSkillManager.SkillBalance(100, 0.015, 240, 30),
      "Shield Bash",
      new TankerSkillManager.SkillBalance(180, 0.025, 160, 30),
      "Tank Leap",
      new TankerSkillManager.SkillBalance(260, 0.04, 280, 40),
      "Reinforcement",
      new TankerSkillManager.SkillBalance(400, 0.06, 440, 50),
      "Willpower",
      new TankerSkillManager.SkillBalance(650, 0.09, 900, 60),
      "Protection Mark",
      new TankerSkillManager.SkillBalance(900, 0.12, 1200, 60)
   );
   private static final Map<UUID, TankerSkillManager.TankerState> STATES = new HashMap<>();
   private static final Map<UUID, TankerSkillManager.ProtectionZone> MARKS = new HashMap<>();
   private static final Map<UUID, TankerSkillManager.TauntClaim> TAUNT_CLAIMS = new HashMap<>();
   private static final Map<UUID, TankerSkillManager.ChallengedState> CHALLENGED = new HashMap<>();
   private static final Map<UUID, TankerSkillManager.SlowState> ACTIVE_SLOWS = new HashMap<>();
   private static final TankerSkillManager.VfxSink NO_VFX = (level, event) -> {};
   private static volatile TankerSkillManager.VfxSink vfxSink = NO_VFX;

   private TankerSkillManager() {
   }

   public static void installVfxSink(TankerSkillManager.VfxSink sink) {
      vfxSink = sink == null ? NO_VFX : sink;
   }

   public static boolean isTankerSkill(String skill) {
      return TankerProgressionRules.isTankerSkill(skill);
   }

   public static String canonicalName(String skill) {
      return TankerProgressionRules.canonicalName(skill);
   }

   public static String canonicalizeSkillList(String encoded) {
      return TankerProgressionRules.canonicalizeSkillList(encoded);
   }

   public static List<String> entitlementsForRank(int rank) {
      return TankerProgressionRules.entitlementsForRank(rank);
   }

   public static String firstMissingSkill(String encoded) {
      return TankerProgressionRules.firstMissingSkill(encoded);
   }

   public static boolean hasSkill(Entity entity, String requestedSkill) {
      String skill = canonicalName(requestedSkill);
      return entity != null && SKILLS.contains(skill) ? TankerProgressionRules.hasSkill(variables(entity).Plist, skill) : false;
   }

   public static boolean activateSkill(ServerPlayer player, String requestedSkill) {
      if (player != null && player.isAlive()) {
         reconcileTanker(player);
         String skill = canonicalName(requestedSkill);
         if (!SKILLS.contains(skill)) {
            return false;
         }

         if (!isTanker(player)) {
            message(player, "Only Tankers can use this skill.");
            return false;
         }

         if (!hasSkill(player, skill)) {
            message(player, "You have not learned " + skill + ".");
            return false;
         }

         if (CooldownManager.isOnCooldown(player, skill)) {
            message(player, skill + " is on cooldown for " + CooldownManager.getRemainingSeconds(player, skill) + "s.");
            return false;
         }

         return switch (skill) {
            case "Taunt" -> castTaunt(player);
            case "Shield Bash" -> castShieldBash(player);
            case "Tank Leap" -> castTankLeap(player);
            case "Reinforcement" -> castReinforcement(player);
            case "Willpower" -> castWillpower(player);
            case "Protection Mark" -> castProtectionMark(player);
            default -> false;
         };
      } else {
         return false;
      }
   }

   public static void reconcileTanker(ServerPlayer player) {
      if (player != null) {
         TankerProgressionHelper.reconcileRankEntitlements(player);
         boolean tanker = TankerProgressionHelper.isTanker(player);
         clearLegacyCancellationState(player);
         player.getPersistentData().remove("sl_t_timer");
         if (!tanker) {
            clearIronWall(player);
         }
      }
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         UUID playerId = player.getUUID();
         TankerSkillManager.TankerState state = STATES.remove(playerId);
         if (state != null) {
            clearMovement(player, state);
            clearTaunts(player, state);
            endReinforcement(player, state, player.level().getGameTime(), true);
            state.willpower = null;
         }

         removeMark(playerId, TankerSkillManager.MarkEnd.CANCEL);
         removeChallengesFor(playerId);
         removeOwnedSlows(playerId);
         removeSlow(player);
         clearPersistedWillpower(player);
         clearIronWall(player);
         clearLegacyCancellationState(player);
      }
   }

   private static void clearLegacyCancellationState(ServerPlayer player) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
         boolean changed = false;
         if (vars.inv || vars.leapjump || Math.abs(vars.wp) > 1.0E-7 || vars.shieldbash) {
            vars.inv = false;
            vars.leapjump = false;
            vars.wp = 0.0;
            vars.shieldbash = false;
            changed = true;
         }

         if (changed) {
            vars.syncPlayerVariables(player);
         }
      });
      stripLegacyEffects(player);
   }

   public static boolean learnFromRunestone(Entity entity, ItemStack stack, String requestedSkill) {
      if (entity instanceof ServerPlayer player && stack != null && !stack.isEmpty()) {
         String skill = canonicalName(requestedSkill);
         if (!SKILLS.contains(skill)) {
            return false;
         }

         boolean alreadyKnown = hasSkill(player, skill);
         TankerProgressionHelper.learnFromRunestone(player, stack, skill);
         return !alreadyKnown && hasSkill(player, skill);
      } else {
         return false;
      }
   }

   public static String grantNextMasterySkill(ServerPlayer player) {
      if (player == null) {
         return "";
      }

      reconcileTanker(player);
      return !TankerProgressionHelper.isTanker(player) ? "" : TankerProgressionHelper.grantNextMasterySkill(player);
   }

   public static int manaCost(Entity entity, String requestedSkill) {
      String skill = canonicalName(requestedSkill);
      TankerSkillManager.SkillBalance balance = BALANCE.get(skill);
      if (balance != null && !(entity instanceof Player player && player.isCreative())) {
         double maximumMana = Math.max(1000.0, variables(entity).Mana);
         return beaconCost(maximumMana, balance.flatFloor, balance.maximumManaFraction);
      } else {
         return 0;
      }
   }

   public static int beaconCost(double maximumMana, int flatFloor, double maximumManaFraction) {
      return (int)Math.ceil(Math.max(flatFloor, Math.max(1000.0, maximumMana) * maximumManaFraction));
   }

   public static List<UUID> boundedTargetIds(List<TankerSkillManager.TargetOrder> candidates, int maximum) {
      return candidates != null && !candidates.isEmpty() && maximum > 0
         ? candidates.stream()
            .filter(Objects::nonNull)
            .filter(candidate -> candidate.targetId() != null)
            .sorted(
               Comparator.comparingDouble(TankerSkillManager.TargetOrder::primaryOrder)
                  .thenComparingDouble(TankerSkillManager.TargetOrder::squaredDistance)
                  .thenComparing(TankerSkillManager.TargetOrder::targetId)
            )
            .limit(maximum)
            .map(TankerSkillManager.TargetOrder::targetId)
            .toList()
         : List.of();
   }

   public static TankerSkillManager.CleanupAction cleanupAction(
      TankerSkillManager.TransientState state, TankerSkillManager.CleanupReason reason, double unpaidStrain
   ) {
      if (state == TankerSkillManager.TransientState.WILLPOWER && unpaidStrain > 1.0E-7) {
         return switch ((TankerSkillManager.CleanupReason)Objects.requireNonNull(reason, "reason")) {
            case DEATH -> TankerSkillManager.CleanupAction.CLEAR;
            case DIMENSION_CHANGE, CLASS_CHANGE -> TankerSkillManager.CleanupAction.START_STRAIN_SETTLEMENT;
            case LOGOUT, SERVER_STOP -> TankerSkillManager.CleanupAction.PERSIST_STRAIN_SETTLEMENT;
         };
      } else {
         return TankerSkillManager.CleanupAction.CLEAR;
      }
   }

   private static boolean castTaunt(ServerPlayer player) {
      ServerLevel level = player.serverLevel();
      Vec3 center = player.position();
      List<LivingEntity> accepted = level.getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(12.0),
            target -> validTauntTarget(player, target) && target.distanceToSqr(center) <= 144.0 && player.hasLineOfSight(target)
         )
         .stream()
         .sorted(distanceThenUuid(center))
         .limit(16L)
         .toList();
      if (accepted.isEmpty()) {
         message(player, "No valid targets are in Taunt range.");
         return false;
      }

      if (!canAfford(player, "Taunt")) {
         return false;
      }

      long now = level.getGameTime();
      TankerSkillManager.TankerState state = state(player);
      return commitCast(player, "Taunt", () -> {
         clearTaunts(player, state);

         for (LivingEntity target : accepted) {
            TankerSkillManager.CombatCategory category = offensiveCategory(target);

            long expiry = now + switch (category) {
               case NORMAL -> 120L;
               case BOSS -> 40L;
               case PVP -> 60L;
            };
            TankerSkillManager.TauntEntry entry = new TankerSkillManager.TauntEntry(target.getUUID(), category, expiry);
            state.taunts.put(target.getUUID(), entry);
            TAUNT_CLAIMS.put(target.getUUID(), new TankerSkillManager.TauntClaim(target.getUUID(), player.getUUID(), level, expiry));
            if (target instanceof ServerPlayer challenged) {
               CHALLENGED.put(challenged.getUUID(), new TankerSkillManager.ChallengedState(player.getUUID(), level, now + 60L));
            } else if (target instanceof Mob mob) {
               mob.setTarget(player);
            }
         }

         state.nextTauntMaintenance = now + 10L;
         int ironGrant = Math.min(3, 1 + (accepted.size() - 1) / 4);
         addIronWall(player, ironGrant, now);
      }, () -> {
         tryAwardMastery(player, TankerSkillManager.MasteryTrigger.TAUNT);
         emit(player, (byte)2, null, center, now, 120, accepted.size(), 1);
      });
   }

   public static boolean hasActiveTauntClaim(LivingEntity target) {
      if (target == null) {
         return false;
      }

      TankerSkillManager.TauntClaim claim = TAUNT_CLAIMS.get(target.getUUID());
      if (claim == null) {
         return false;
      }

      long now = target.level().getGameTime();
      if (claim.level == target.level() && now < claim.expiresAt) {
         return true;
      }

      TAUNT_CLAIMS.remove(target.getUUID(), claim);
      return false;
   }

   private static boolean castShieldBash(ServerPlayer player) {
      if (!player.onGround()) {
         message(player, "Shield Bash requires solid ground.");
         return false;
      }

      if (!player.getOffhandItem().is(TankerSkillManager.RuntimeKeys.SHIELDS)) {
         message(player, "Equip a shield in your offhand.");
         return false;
      }

      TankerSkillManager.TankerState state = state(player);
      if (state.leap == null && state.bash == null) {
         ServerLevel level = player.serverLevel();
         Vec3 direction = horizontalDirection(player);
         Vec3 start = player.position();
         Vec3 end = findSafeHorizontalEndpoint(level, player, start, direction, 3.6);
         if (end != null && !(end.distanceToSqr(start) < 0.04)) {
            AABB swept = player.getBoundingBox().minmax(player.getBoundingBox().move(end.subtract(start))).inflate(0.45, 0.25, 0.45);
            LivingEntity selected = level.getEntitiesOfClass(
                  LivingEntity.class,
                  swept,
                  target -> validEnemy(player, target, false)
                     && forwardProjection(start, direction, target.position()) >= -0.25
                     && forwardProjection(start, direction, target.position()) <= start.distanceTo(end) + 1.0
               )
               .stream()
               .sorted(
                  Comparator.<LivingEntity>comparingDouble(target -> forwardProjection(start, direction, target.position()))
                     .thenComparingDouble(target -> target.distanceToSqr(start))
                     .thenComparing(target -> target.getUUID().toString())
               )
               .findFirst()
               .orElse(null);
            if (!canAfford(player, "Shield Bash")) {
               return false;
            }

            long now = level.getGameTime();
            UUID targetId = selected == null ? null : selected.getUUID();
            return commitCast(
               player,
               "Shield Bash",
               () -> state.bash = new TankerSkillManager.BashState(now, start, end, direction, targetId),
               () -> emit(player, (byte)3, selected, start, now, 4, 100, 1)
            );
         } else {
            message(player, "There is no safe space to Shield Bash.");
            return false;
         }
      } else {
         message(player, "You are already committed to a Tanker movement skill.");
         return false;
      }
   }

   private static boolean castTankLeap(ServerPlayer player) {
      if (!player.onGround()) {
         message(player, "Tank Leap requires solid ground.");
         return false;
      }

      TankerSkillManager.TankerState state = state(player);
      if (state.leap == null && state.bash == null) {
         ServerLevel level = player.serverLevel();
         Vec3 direction = horizontalDirection(player);
         Vec3 start = player.position();
         Vec3 firstStep = direction.scale(0.6666666666666666);
         if (!isLoadedAndWithinBorder(level, player.getBoundingBox().move(firstStep)) || !level.noCollision(player, player.getBoundingBox().move(firstStep))) {
            message(player, "There is no safe path for Tank Leap.");
            return false;
         }

         if (!canAfford(player, "Tank Leap")) {
            return false;
         }

         long now = level.getGameTime();
         return commitCast(player, "Tank Leap", () -> {
            state.leap = new TankerSkillManager.LeapState(level.dimension(), now, now + 12L, start, start, direction);
            player.fallDistance = 0.0F;
            player.setDeltaMovement(direction.scale(0.6666666666666666).add(0.0, 0.58, 0.0));
            player.hurtMarked = true;
         }, () -> emit(player, (byte)0, null, start, now, 12, 100, 1));
      } else {
         message(player, "You are already committed to a Tanker movement skill.");
         return false;
      }
   }

   private static boolean castReinforcement(ServerPlayer player) {
      TankerSkillManager.TankerState state = state(player);
      long now = player.level().getGameTime();
      expireReinforcement(player, state, now);
      if (state.reinforcementPhase != TankerSkillManager.ReinforcementPhase.NONE) {
         message(player, "Reinforcement is already active.");
         return false;
      }

      if (state.willpower != null && state.willpower.active) {
         message(player, "Reinforcement cannot start during Willpower.");
         return false;
      }

      if (!canAfford(player, "Reinforcement")) {
         return false;
      }

      ServerLevel level = player.serverLevel();
      Vec3 origin = player.position();
      return commitCast(player, "Reinforcement", () -> {
         state.reinforcementPhase = TankerSkillManager.ReinforcementPhase.PERFECT;
         state.reinforcementExpiresAt = now + 12L;
      }, () -> emit(player, (byte)6, null, origin, now, 12, 100, 1));
   }

   private static boolean castWillpower(ServerPlayer player) {
      TankerSkillManager.TankerState state = state(player);
      long now = player.level().getGameTime();
      expireReinforcement(player, state, now);
      if (state.reinforcementPhase != TankerSkillManager.ReinforcementPhase.NONE) {
         message(player, "Willpower cannot start during Reinforcement.");
         return false;
      }

      if (state.willpower == null || !state.willpower.active && !state.willpower.settling) {
         if (!canAfford(player, "Willpower")) {
            return false;
         }

         ServerLevel level = player.serverLevel();
         Vec3 origin = player.position();
         double maximumHealth = Math.max(1.0, player.getMaxHealth());
         return commitCast(player, "Willpower", () -> {
            state.willpower = TankerSkillManager.WillpowerState.active(now + 160L, maximumHealth);
            persistWillpower(player, state.willpower);
         }, () -> emit(player, (byte)10, null, origin, now, 160, 0, 1));
      } else {
         message(player, "Willpower is already active or settling.");
         return false;
      }
   }

   private static boolean castProtectionMark(ServerPlayer player) {
      ServerLevel level = player.serverLevel();
      Vec3 center = findGroundBelow(level, player);
      if (center == null) {
         message(player, "Protection Mark needs collision-safe ground.");
         return false;
      }

      if (!canAfford(player, "Protection Mark")) {
         return false;
      }

      long now = level.getGameTime();
      return commitCast(
         player,
         "Protection Mark",
         () -> {
            removeMark(player.getUUID(), TankerSkillManager.MarkEnd.CANCEL);
            TankerSkillManager.ProtectionZone zone = new TankerSkillManager.ProtectionZone(
               player.getUUID(),
               player.getId(),
               level,
               level.dimension(),
               center,
               now,
               now + 240L,
               protectionMarkInitialIntegrity(Math.max(1.0, player.getMaxHealth())),
               player.getYRot(),
               player.getXRot()
            );
            MARKS.put(player.getUUID(), zone);
            refreshMarkBeneficiaries(player, zone, now);
         },
         () -> emit(player, (byte)14, null, center, now, 240, 0, 1)
      );
   }

   private static boolean canAfford(ServerPlayer player, String skill) {
      int cost = manaCost(player, skill);
      if (!player.isCreative() && !(variables(player).MP + 1.0E-7 >= cost)) {
         message(player, "Not enough MP. " + skill + " needs " + cost + ".");
         return false;
      } else {
         return true;
      }
   }

   private static boolean commitCast(ServerPlayer player, String skill, Runnable stateCommit, Runnable feedbackCommit) {
      TankerSkillManager.SkillBalance balance = BALANCE.get(skill);
      if (balance == null) {
         return false;
      }

      int cost = manaCost(player, skill);
      stateCommit.run();
      if (!player.isCreative() && cost > 0) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            vars.MP = Math.max(0.0, vars.MP - cost);
            vars.syncPlayerVariables(player);
         });
      }

      CooldownManager.setFullDuration(player, skill, balance.cooldownTicks);
      extendManaRegenLock(player, balance.regenLockTicks);
      feedbackCommit.run();
      message(player, "Using " + skill);
      return true;
   }

   private static void extendManaRegenLock(ServerPlayer player, int ticks) {
      int remaining = CooldownManager.getRemainingTicks(player, "mana_refresh");
      if (remaining < ticks) {
         CooldownManager.setFullDuration(player, "mana_refresh", ticks);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLivingHurt(LivingHurtEvent event) {
      if (!event.getEntity().level().isClientSide() && !(event.getAmount() <= 0.0F)) {
         applyChallengedOutgoing(event);
         if (event.getEntity() instanceof ServerPlayer victim) {
            if (!event.getSource().is(TankerSkillManager.RuntimeKeys.WILLPOWER_STRAIN_DAMAGE)) {
               TankerSkillManager.CombatContext context = combatContext(victim, event.getSource());
               if (context != null) {
                  boolean tanker = isTanker(victim);
                  TankerSkillManager.MarkSelection mark = selectProtectionMark(victim, context.category);
                  if (tanker || mark != null) {
                     long now = victim.level().getGameTime();
                     TankerSkillManager.TankerState state = tanker ? state(victim) : null;
                     if (state != null) {
                        expireReinforcement(victim, state, now);
                     }

                     int priorIronStacks = tanker ? currentIronWall(victim, now) : 0;
                     TankerSkillManager.ReinforcementPhase phaseForHit = state == null ? TankerSkillManager.ReinforcementPhase.NONE : state.reinforcementPhase;
                     double original = event.getAmount();
                     double retainedWithoutMark = retainedDamage(original, context.category, priorIronStacks, 0.0, phaseForHit);
                     double retained = retainedWithoutMark;
                     if (mark != null) {
                        double requestedPrevention = protectionMarkRequestedPrevention(original, context.category, priorIronStacks, mark.reduction, phaseForHit);
                        TankerSkillManager.ProtectionFundingResult funding = calculateProtectionFunding(
                           requestedPrevention, mark.zone.integrity, context.category
                        );
                        if (funding.preventedDamage() > 1.0E-7) {
                           retained -= funding.preventedDamage();
                           drainMark(mark, victim, funding);
                        }
                     }

                     if (state != null && phaseForHit != TankerSkillManager.ReinforcementPhase.NONE) {
                        state.pendingKnockback = new TankerSkillManager.PendingKnockback(now, knockbackMultiplier(phaseForHit, context.category));
                        if (phaseForHit == TankerSkillManager.ReinforcementPhase.PERFECT) {
                           state.reinforcementPhase = TankerSkillManager.ReinforcementPhase.STANCE;
                           state.reinforcementExpiresAt = now + 80L;
                           emit(victim, (byte)7, context.owner, victim.position(), now, 6, 100, confirmedHitFlags(context.category));
                           emit(victim, (byte)8, null, victim.position(), now, 80, 100, 1 | pvpFlag(context.category));
                        }
                     }

                     double finalAmount = retained;
                     if (state != null && state.willpower != null && state.willpower.active) {
                        finalAmount = applyWillpower(victim, state.willpower, retained, context.category, now);
                     }

                     event.setAmount((float)Math.max(0.0, finalAmount));
                     if (tanker && original > 1.0E-7) {
                        grantDamageIronWall(victim, now);
                        if (phaseForHit == TankerSkillManager.ReinforcementPhase.PERFECT) {
                           addIronWall(victim, 2, now);
                           tryAwardMastery(victim, TankerSkillManager.MasteryTrigger.REINFORCEMENT_BRACE);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLivingAttack(LivingAttackEvent event) {
      if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
         clearLegacyCancellationState(player);
      }
   }

   @SubscribeEvent
   public static void onKnockback(LivingKnockBackEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide()) {
         TankerSkillManager.TankerState state = STATES.get(player.getUUID());
         if (state != null && state.pendingKnockback != null) {
            long now = player.level().getGameTime();
            if (state.pendingKnockback.gameTick != now) {
               state.pendingKnockback = null;
            } else {
               event.setStrength((float)(event.getStrength() * state.pendingKnockback.multiplier));
               state.pendingKnockback = null;
            }
         }
      }
   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide()) {
         TankerSkillManager.TankerState state = STATES.get(player.getUUID());
         if (state != null && state.leap != null && state.leap.dimension.equals(player.level().dimension())) {
            event.setCanceled(true);
            player.fallDistance = 0.0F;
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         long var7 = player.level().getGameTime();
         boolean tanker = isTanker(player);
         if (player.tickCount % 20 == 0) {
            reconcileTanker(player);
         }

         if (tanker) {
            currentIronWall(player, var7);
         } else {
            clearIronWall(player);
         }

         TankerSkillManager.TankerState state = STATES.get(player.getUUID());
         if (state != null) {
            if (!tanker) {
               clearActiveCombatForClassChange(player, state, var7);
            }

            tickLeap(player, state, var7);
            tickBash(player, state, var7);
            tickTaunts(player, state, var7);
            expireReinforcement(player, state, var7);
            tickWillpower(player, state, var7);
            if (state.pendingKnockback != null && state.pendingKnockback.gameTick < var7) {
               state.pendingKnockback = null;
            }

            if (state.isEmpty()) {
               STATES.remove(player.getUUID());
            }
         }

         TankerSkillManager.ProtectionZone zone = MARKS.get(player.getUUID());
         if (zone != null) {
            if (!tanker || zone.level != player.level() || var7 >= zone.expiresAt || !player.isAlive()) {
               removeMark(player.getUUID(), TankerSkillManager.MarkEnd.CANCEL);
            } else if (var7 >= zone.nextMembershipUpdate) {
               refreshMarkBeneficiaries(player, zone, var7);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         tickSlows(event.getServer());
         pruneClaims(event.getServer());
         pruneChallenges(event.getServer());
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         reconcileTanker(player);
         loadPendingSettlement(player);
         if (isTanker(player)) {
            syncIronWall(player, currentIronWall(player, player.level().getGameTime()));
         } else {
            syncIronWall(player, 0);
         }
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         reconcileTanker(player);
         clearPersistedWillpower(player);
         if (isTanker(player)) {
            syncIronWall(player, currentIronWall(player, player.level().getGameTime()));
         } else {
            syncIronWall(player, 0);
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         TankerSkillManager.TankerState state = STATES.remove(player.getUUID());
         if (state != null) {
            if (state.willpower != null && state.willpower.active) {
               beginSettlement(player, state.willpower, player.level().getGameTime(), false);
            }

            if (state.willpower != null) {
               persistWillpower(player, state.willpower);
            }

            clearTaunts(player, state);
         }

         removeMark(player.getUUID(), TankerSkillManager.MarkEnd.CANCEL);
         removeChallengesFor(player.getUUID());
         removeSlow(player);
      }
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         removeSlow(player);
         long var5 = player.level().getGameTime();
         TankerSkillManager.TankerState state = STATES.get(player.getUUID());
         if (state != null) {
            clearMovement(player, state);
            clearTaunts(player, state);
            endReinforcement(player, state, var5, true);
            if (state.willpower != null && state.willpower.active) {
               beginSettlement(player, state.willpower, var5, false);
            }
         }

         removeMark(player.getUUID(), TankerSkillManager.MarkEnd.CANCEL);
         removeChallengesFor(player.getUUID());
         clearIronWall(player);
      }
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      LivingEntity entity = event.getEntity();
      removeSlow(entity);
      if (entity instanceof ServerPlayer player) {
         TankerSkillManager.TankerState state = STATES.remove(player.getUUID());
         if (state != null) {
            clearTaunts(player, state);
         }

         removeMark(player.getUUID(), TankerSkillManager.MarkEnd.CANCEL);
         removeChallengesFor(player.getUUID());
         clearPersistedWillpower(player);
         clearIronWall(player);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLegacyFlagLoad(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide() && event.getEntity() instanceof FlagOfProtectionEntity) {
         event.setCanceled(true);
         event.getEntity().discard();
      }

      if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living) {
         TankerSkillManager.SlowState slow = ACTIVE_SLOWS.get(living.getUUID());
         if (slow == null || slow.level != event.getLevel() || !refreshSlowModifier(living, slow, event.getLevel().getGameTime())) {
            ACTIVE_SLOWS.remove(living.getUUID());
            removeSlowModifier(living);
         }
      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      for (Entry<UUID, TankerSkillManager.TankerState> entry : STATES.entrySet()) {
         TankerSkillManager.WillpowerState willpower = entry.getValue().willpower;
         if (willpower != null) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
               if (willpower.active) {
                  beginSettlement(player, willpower, player.level().getGameTime(), false);
               }

               persistWillpower(player, willpower);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      for (TankerSkillManager.SlowState slow : ACTIVE_SLOWS.values()) {
         if (slow.level.getEntity(slow.targetId) instanceof LivingEntity living) {
            removeSlowModifier(living);
         }
      }

      STATES.clear();
      MARKS.clear();
      TAUNT_CLAIMS.clear();
      CHALLENGED.clear();
      ACTIVE_SLOWS.clear();
   }

   private static void applyChallengedOutgoing(LivingHurtEvent event) {
      if (resolveSourceOwner(event.getSource()) instanceof ServerPlayer attacker) {
         TankerSkillManager.ChallengedState challenged = CHALLENGED.get(attacker.getUUID());
         if (challenged != null) {
            long now = attacker.level().getGameTime();
            ServerPlayer taunter = attacker.server.getPlayerList().getPlayer(challenged.taunterId);
            if (now < challenged.expiresAt
               && challenged.level == attacker.level()
               && taunter != null
               && taunter.isAlive()
               && taunter.level() == attacker.level()
               && !MageCombatHelper.areAllied(attacker, taunter)) {
               if (!event.getEntity().getUUID().equals(taunter.getUUID())) {
                  event.setAmount((float)(event.getAmount() * 0.85));
               }
            } else {
               CHALLENGED.remove(attacker.getUUID());
            }
         }
      }
   }

   private static double applyWillpower(
      ServerPlayer player, TankerSkillManager.WillpowerState willpower, double retained, TankerSkillManager.CombatCategory category, long now
   ) {
      TankerSkillManager.WillpowerHitResult result = calculateWillpowerHit(
         retained, willpower.strain, willpower.currentCap, willpower.maxHealthAtActivation, category
      );
      willpower.currentCap = result.activeCap();
      willpower.strain = result.resultingStrain();
      if (result.strainAdded() <= 1.0E-7) {
         if (result.endsWillpower()) {
            beginSettlement(player, willpower, now, true);
         }

         return result.immediateDamage();
      } else {
         willpower.totalRetained += retained;
         willpower.totalImmediate = willpower.totalImmediate + result.immediateDamage();
         persistWillpower(player, willpower);
         updateWillpowerThreshold(player, willpower, now);
         if (!willpower.masteryAwarded && willpower.strain + willpower.shieldBashStrainRelief >= willpower.maxHealthAtActivation * 0.1) {
            willpower.masteryAwarded = true;
            tryAwardMastery(player, TankerSkillManager.MasteryTrigger.WILLPOWER_THRESHOLD);
         }

         if (result.endsWillpower()) {
            beginSettlement(player, willpower, now, true);
         }

         return result.immediateDamage();
      }
   }

   private static void beginSettlement(ServerPlayer player, TankerSkillManager.WillpowerState willpower, long now, boolean broken) {
      if (willpower.active) {
         willpower.active = false;
         willpower.settling = willpower.strain > 1.0E-7;
         willpower.pulsesRemaining = willpower.settling ? 4 : 0;
         willpower.nextPulseAt = willpower.settling ? now + 10L : 0L;
         persistWillpower(player, willpower);
         if (broken) {
            emit(player, (byte)13, null, player.position(), now, 8, vfxIntensityBand(thresholdIntensity(willpower)), 1);
         }
      }
   }

   private static void tickWillpower(ServerPlayer player, TankerSkillManager.TankerState state, long now) {
      TankerSkillManager.WillpowerState willpower = state.willpower;
      if (willpower != null) {
         if (willpower.active && now >= willpower.activeExpiresAt) {
            beginSettlement(player, willpower, now, false);
         }

         if (!willpower.settling) {
            if (!willpower.active) {
               clearPersistedWillpower(player);
               state.willpower = null;
            }
         } else if (now >= willpower.nextPulseAt) {
            if (willpower.pulsesRemaining > 0 && !(willpower.strain <= 1.0E-7)) {
               double pulse = willpower.pulsesRemaining == 1 ? willpower.strain : willpower.strain / willpower.pulsesRemaining;
               float enginePulse = (float)pulse;
               if (!player.hurt(strainDamageSource(player.serverLevel()), enginePulse)) {
                  willpower.nextPulseAt = now + 10L;
               } else {
                  willpower.strain = Math.max(0.0, willpower.strain - enginePulse);
                  willpower.pulsesRemaining--;
                  willpower.nextPulseAt = now + 10L;
                  if (player.isAlive()) {
                     persistWillpower(player, willpower);
                     emit(player, (byte)12, null, player.position(), now, 10, vfxIntensityBand(4 - willpower.pulsesRemaining), 1);
                     if (willpower.pulsesRemaining == 0 || willpower.strain <= 1.0E-7) {
                        willpower.strain = 0.0;
                        willpower.settling = false;
                        clearPersistedWillpower(player);
                        state.willpower = null;
                     }
                  }
               }
            } else {
               willpower.strain = 0.0;
               willpower.settling = false;
               clearPersistedWillpower(player);
               state.willpower = null;
            }
         }
      }
   }

   private static void updateWillpowerThreshold(ServerPlayer player, TankerSkillManager.WillpowerState willpower, long now) {
      int threshold = thresholdIntensity(willpower);

      while (willpower.lastThreshold < threshold) {
         willpower.lastThreshold++;
         emit(player, (byte)11, null, player.position(), now, 8, vfxIntensityBand(willpower.lastThreshold), 0);
      }
   }

   private static int thresholdIntensity(TankerSkillManager.WillpowerState willpower) {
      return willpower.currentCap <= 1.0E-7 ? 4 : Mth.clamp((int)Math.floor(willpower.strain / willpower.currentCap * 4.0 + 1.0E-7), 0, 4);
   }

   private static void tickLeap(ServerPlayer player, TankerSkillManager.TankerState state, long now) {
      TankerSkillManager.LeapState leap = state.leap;
      if (leap != null) {
         if (player.isAlive() && isTanker(player) && leap.dimension.equals(player.level().dimension())) {
            player.fallDistance = 0.0F;
            double horizontalDistance = horizontalDistance(player.position(), leap.start);
            boolean landed = now > leap.startedAt && player.onGround();
            boolean collision = player.horizontalCollision;
            boolean timeout = now >= leap.deadline || horizontalDistance >= 7.95;
            if (landed || timeout) {
               landLeap(player, state, now);
            } else if (collision) {
               restoreLeapLastSafe(player, leap);
               landLeap(player, state, now);
            } else {
               Vec3 horizontalStep = leap.direction.scale(0.6666666666666666);
               AABB nextBox = player.getBoundingBox().move(horizontalStep).move(0.0, Math.min(0.6, player.getDeltaMovement().y), 0.0);
               if (isLoadedAndWithinBorder(player.serverLevel(), nextBox) && player.serverLevel().noCollision(player, nextBox)) {
                  leap.lastSafe = player.position();
                  double remaining = Math.max(0.0, 8.0 - horizontalDistance);
                  Vec3 step = leap.direction.scale(Math.min(0.6666666666666666, remaining));
                  player.setDeltaMovement(step.x, player.getDeltaMovement().y, step.z);
                  player.hurtMarked = true;
               } else {
                  restoreLeapLastSafe(player, leap);
                  landLeap(player, state, now);
               }
            }
         } else {
            clearLeap(player, state);
         }
      }
   }

   private static void restoreLeapLastSafe(ServerPlayer player, TankerSkillManager.LeapState leap) {
      if (player != null && leap != null && leap.lastSafe != null) {
         player.setPos(leap.lastSafe.x, leap.lastSafe.y, leap.lastSafe.z);
         player.hurtMarked = true;
      }
   }

   private static void landLeap(ServerPlayer player, TankerSkillManager.TankerState state, long now) {
      TankerSkillManager.LeapState leap = state.leap;
      if (leap != null) {
         state.leap = null;
         player.fallDistance = 0.0F;
         player.setDeltaMovement(0.0, Math.min(0.0, player.getDeltaMovement().y), 0.0);
         player.hurtMarked = true;
         ServerLevel level = player.serverLevel();
         Vec3 origin = player.position();
         List<LivingEntity> targets = level.getEntitiesOfClass(
               LivingEntity.class, new AABB(origin, origin).inflate(5.0), targetx -> validEnemy(player, targetx, true) && targetx.distanceToSqr(origin) <= 25.0
            )
            .stream()
            .sorted(distanceThenUuid(origin))
            .limit(16L)
            .toList();
         int hits = 0;

         for (LivingEntity target : targets) {
            TankerSkillManager.CombatCategory category = offensiveCategory(target);

            float damage = (float)(tankerPower(player) * switch (category) {
               case NORMAL -> 1.1;
               case BOSS -> 0.8;
               case PVP -> 0.6;
            });
            if (dealTankerDamage(player, target, damage)) {
               leap.hitLedger.add(target.getUUID());
               hits++;
               if (category == TankerSkillManager.CombatCategory.NORMAL) {
                  Vec3 pull = origin.subtract(target.position());
                  Vec3 horizontal = new Vec3(pull.x, 0.0, pull.z);
                  if (horizontal.lengthSqr() > 1.0E-7) {
                     double strength = Math.min(1.1, horizontal.length());
                     Vec3 impulse = horizontal.normalize().scale(strength);
                     target.push(impulse.x, 0.15, impulse.z);
                     target.hurtMarked = true;
                  }
               } else if (category == TankerSkillManager.CombatCategory.PVP) {
                  applySlow(player, target, 0.2, 8, level);
               }
            }
         }

         if (hits > 0) {
            addIronWall(player, 1, now);
         }

         SololevelingModVariables.PlayerVariables variables = variables(player);
         AbilityDestructionManager.impact(
            player,
            AbilityDestructionManager.Profile.TANKER_SLAM,
            origin,
            TemporaryStatBonusManager.effectiveStrength(player) + variables.Vitality * 0.5 + player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 14.0,
            false
         );
         emit(player, (byte)1, null, origin, now, 10, hits, 1 | (hits > 0 ? 2 : 0));
      }
   }

   private static void clearLeap(ServerPlayer player, TankerSkillManager.TankerState state) {
      if (state.leap != null) {
         state.leap = null;
         player.fallDistance = 0.0F;
      }
   }

   private static void tickBash(ServerPlayer player, TankerSkillManager.TankerState state, long now) {
      TankerSkillManager.BashState bash = state.bash;
      if (bash != null) {
         if (player.isAlive() && isTanker(player)) {
            int elapsed = (int)Math.max(1L, now - bash.startedAt + 1L);
            double progress = Mth.clamp(elapsed / 4.0, 0.0, 1.0);
            Vec3 desired = bash.start.lerp(bash.end, progress);
            Vec3 movement = new Vec3(desired.x - player.getX(), 0.0, desired.z - player.getZ());
            AABB next = player.getBoundingBox().move(movement);
            boolean safe = isLoadedAndWithinBorder(player.serverLevel(), next) && player.serverLevel().noCollision(player, next);
            if (safe && movement.lengthSqr() > 1.0E-7) {
               player.move(MoverType.SELF, movement);
               player.hurtMarked = true;
            }

            if (!bash.hit
               && bash.targetId != null
               && player.serverLevel().getEntity(bash.targetId) instanceof LivingEntity target
               && validEnemy(player, target, false)
               && (player.getBoundingBox().inflate(0.7).intersects(target.getBoundingBox()) || progress >= 1.0 && player.distanceToSqr(target) <= 4.0)) {
               performBashHit(player, state, bash, target, now);
            }

            if (!safe || progress >= 1.0) {
               state.bash = null;
            }
         } else {
            state.bash = null;
         }
      }
   }

   private static void performBashHit(
      ServerPlayer player, TankerSkillManager.TankerState state, TankerSkillManager.BashState bash, LivingEntity target, long now
   ) {
      bash.hit = true;
      TankerSkillManager.CombatCategory category = offensiveCategory(target);

      float damage = (float)(tankerPower(player) * switch (category) {
         case NORMAL -> 0.7;
         case BOSS -> 0.5;
         case PVP -> 0.4;
      });
      if (dealTankerDamage(player, target, damage)) {
         switch (category) {
            case NORMAL:
               if (target instanceof Mob mob) {
                  target.stopUsingItem();
                  mob.getNavigation().stop();
                  mob.setAggressive(false);
               }

               applySlow(player, target, 0.8, 18, player.serverLevel());
               target.knockback(0.75, player.getX() - target.getX(), player.getZ() - target.getZ());
               break;
            case BOSS:
               applySlow(player, target, 0.2, 6, player.serverLevel());
               break;
            case PVP:
               applySlow(player, target, 0.25, 8, player.serverLevel());
               target.knockback(0.25, player.getX() - target.getX(), player.getZ() - target.getZ());
         }

         if (isTauntedBy(player, state, target, now)) {
            refreshIronWall(player, now);
         }

         double relief = 0.0;
         TankerSkillManager.WillpowerState willpower = state.willpower;
         if (willpower != null && willpower.active) {
            TankerSkillManager.StrainReliefResult reliefResult = calculateShieldBashRelief(
               willpower.strain, willpower.maxHealthAtActivation, willpower.reliefUsed
            );
            relief = reliefResult.removedStrain();
            willpower.strain = reliefResult.remainingStrain();
            willpower.reliefUsed = reliefResult.reliefUsed();
            if (relief > 1.0E-7) {
               willpower.shieldBashStrainRelief += relief;
               persistWillpower(player, willpower);
            }
         }

         emit(player, (byte)4, target, target.position(), now, 6, 100, confirmedHitFlags(category));
         if (relief > 1.0E-7) {
            emit(player, (byte)5, target, target.position(), now, 8, vfxIntensityBand(thresholdIntensity(willpower)), 2);
         }
      }
   }

   private static void tickTaunts(ServerPlayer player, TankerSkillManager.TankerState state, long now) {
      if (!state.taunts.isEmpty() && now >= state.nextTauntMaintenance) {
         state.nextTauntMaintenance = now + 10L;
         Iterator<Entry<UUID, TankerSkillManager.TauntEntry>> iterator = state.taunts.entrySet().iterator();

         while (iterator.hasNext()) {
            TankerSkillManager.TauntEntry entry = iterator.next().getValue();
            TankerSkillManager.TauntClaim claim = TAUNT_CLAIMS.get(entry.targetId);
            if (claim != null && claim.taunterId.equals(player.getUUID()) && claim.level == player.level() && now < entry.expiresAt) {
               if (!(
                  player.serverLevel().getEntity(entry.targetId) instanceof LivingEntity target
                     && target.isAlive()
                     && !(target.distanceToSqr(player) > 144.0)
                     && validTauntTarget(player, target)
               )) {
                  iterator.remove();
                  TAUNT_CLAIMS.remove(entry.targetId, claim);
                  if (entry.category == TankerSkillManager.CombatCategory.PVP) {
                     CHALLENGED.remove(entry.targetId);
                  }
               } else if (target instanceof Mob mob) {
                  if (entry.category == TankerSkillManager.CombatCategory.NORMAL) {
                     mob.setTarget(player);
                  } else if (entry.category == TankerSkillManager.CombatCategory.BOSS) {
                     LivingEntity current = mob.getTarget();
                     if (current == null || !current.isAlive() || !mob.canAttack(current)) {
                        mob.setTarget(player);
                     }
                  }
               }
            } else {
               iterator.remove();
            }
         }
      }
   }

   private static void clearTaunts(ServerPlayer player, TankerSkillManager.TankerState state) {
      if (state != null && !state.taunts.isEmpty()) {
         for (UUID targetId : state.taunts.keySet()) {
            TankerSkillManager.TauntClaim claim = TAUNT_CLAIMS.get(targetId);
            if (claim != null && claim.taunterId.equals(player.getUUID())) {
               TAUNT_CLAIMS.remove(targetId);
            }

            TankerSkillManager.ChallengedState challenged = CHALLENGED.get(targetId);
            if (challenged != null && challenged.taunterId.equals(player.getUUID())) {
               CHALLENGED.remove(targetId);
            }
         }

         state.taunts.clear();
      }
   }

   private static boolean isTauntedBy(ServerPlayer player, TankerSkillManager.TankerState state, LivingEntity target, long now) {
      TankerSkillManager.TauntEntry entry = state.taunts.get(target.getUUID());
      TankerSkillManager.TauntClaim claim = TAUNT_CLAIMS.get(target.getUUID());
      return entry != null && now < entry.expiresAt && claim != null && claim.taunterId.equals(player.getUUID());
   }

   private static void expireReinforcement(ServerPlayer player, TankerSkillManager.TankerState state, long now) {
      if (state.reinforcementPhase != TankerSkillManager.ReinforcementPhase.NONE && now >= state.reinforcementExpiresAt) {
         endReinforcement(player, state, now, false);
      }
   }

   private static void endReinforcement(ServerPlayer player, TankerSkillManager.TankerState state, long now, boolean canceled) {
      TankerSkillManager.ReinforcementPhase old = state.reinforcementPhase;
      state.reinforcementPhase = TankerSkillManager.ReinforcementPhase.NONE;
      state.reinforcementExpiresAt = 0L;
      state.pendingKnockback = null;
      if (old == TankerSkillManager.ReinforcementPhase.STANCE) {
         emit(player, (byte)9, null, player.position(), now, 6, canceled ? 0 : 100, 1);
      }
   }

   private static void clearActiveCombatForClassChange(ServerPlayer player, TankerSkillManager.TankerState state, long now) {
      clearMovement(player, state);
      clearTaunts(player, state);
      endReinforcement(player, state, now, true);
      if (state.willpower != null && state.willpower.active) {
         beginSettlement(player, state.willpower, now, false);
      }

      removeMark(player.getUUID(), TankerSkillManager.MarkEnd.CANCEL);
      removeChallengesFor(player.getUUID());
   }

   private static void clearMovement(ServerPlayer player, TankerSkillManager.TankerState state) {
      clearLeap(player, state);
      state.bash = null;
   }

   private static void refreshMarkBeneficiaries(ServerPlayer owner, TankerSkillManager.ProtectionZone zone, long now) {
      zone.nextMembershipUpdate = now + 10L;
      LinkedHashSet<UUID> beneficiaries = new LinkedHashSet<>();
      if (validMarkPlayer(owner, owner, zone)) {
         beneficiaries.add(owner.getUUID());
      }

      String party = party(owner);
      if (!party.isBlank()) {
         for (ServerPlayer candidate : zone.level
            .getEntitiesOfClass(
               ServerPlayer.class,
               new AABB(zone.center, zone.center).inflate(6.0),
               player -> player != owner && validMarkPlayer(owner, player, zone) && party.equals(party(player))
            )
            .stream()
            .sorted(Comparator.<ServerPlayer>comparingDouble(player -> player.distanceToSqr(zone.center)).thenComparing(player -> player.getUUID().toString()))
            .limit(7L)
            .toList()) {
            beneficiaries.add(candidate.getUUID());
         }
      }

      zone.beneficiaries = beneficiaries;
   }

   private static TankerSkillManager.MarkSelection selectProtectionMark(ServerPlayer victim, TankerSkillManager.CombatCategory category) {
      List<TankerSkillManager.MarkSelection> eligible = new ArrayList<>();

      for (TankerSkillManager.ProtectionZone zone : MARKS.values()) {
         if (zone.level == victim.level()
            && !(zone.integrity <= 1.0E-7)
            && victim.level().getGameTime() < zone.expiresAt
            && zone.beneficiaries.contains(victim.getUUID())) {
            ServerPlayer owner = victim.server.getPlayerList().getPlayer(zone.ownerId);
            if (owner != null && isTanker(owner) && validMarkPlayer(owner, victim, zone)) {
               boolean isOwner = victim.getUUID().equals(zone.ownerId);
               double reduction = markReduction(isOwner, category);
               eligible.add(new TankerSkillManager.MarkSelection(zone, reduction, isOwner));
            }
         }
      }

      return eligible.stream()
         .sorted(
            Comparator.<TankerSkillManager.MarkSelection>comparingDouble(selection -> selection.reduction)
               .reversed()
               .thenComparing(Comparator.<TankerSkillManager.MarkSelection>comparingDouble(selection -> selection.zone.integrity).reversed())
               .thenComparing(selection -> selection.zone.ownerId.toString())
         )
         .findFirst()
         .orElse(null);
   }

   private static void drainMark(TankerSkillManager.MarkSelection mark, ServerPlayer victim, TankerSkillManager.ProtectionFundingResult funding) {
      TankerSkillManager.ProtectionZone zone = mark.zone;
      double before = zone.integrity;
      zone.integrity = funding.remainingIntegrity();
      if (!mark.owner && !zone.allyMasteryTriggered) {
         zone.allyMasteryTriggered = true;
         ServerPlayer owner = victim.server.getPlayerList().getPlayer(zone.ownerId);
         if (owner != null) {
            tryAwardMastery(owner, TankerSkillManager.MasteryTrigger.PROTECTION_MARK_ALLY);
         }
      }

      double maximum = zone.initialIntegrity;
      if (maximum > 1.0E-7) {
         int previousBand = integrityBand(before / maximum);
         int currentBand = integrityBand(zone.integrity / maximum);

         while (previousBand < currentBand) {
            emit(zone, (byte)15, victim, victim.level().getGameTime(), 8, vfxIntensityBand(++previousBand), 0);
         }
      }

      if (zone.integrity <= 1.0E-7) {
         removeMark(zone.ownerId, TankerSkillManager.MarkEnd.BREAK);
      }
   }

   private static int integrityBand(double ratio) {
      if (ratio <= 0.2500001) {
         return 3;
      } else if (ratio <= 0.5000001) {
         return 2;
      } else {
         return ratio <= 0.7500001 ? 1 : 0;
      }
   }

   private static void removeMark(UUID ownerId, TankerSkillManager.MarkEnd reason) {
      TankerSkillManager.ProtectionZone removed = MARKS.remove(ownerId);
      if (removed != null) {
         byte type = (byte)(reason == TankerSkillManager.MarkEnd.BREAK ? 16 : 17);
         emit(removed, type, null, removed.level.getGameTime(), 8, reason == TankerSkillManager.MarkEnd.BREAK ? 255 : 0, 1);
      }
   }

   private static void applySlow(ServerPlayer owner, LivingEntity target, double fraction, int durationTicks, ServerLevel level) {
      if (owner != null && target != null && durationTicks > 0 && !(fraction <= 0.0)) {
         long now = level.getGameTime();
         long expiry = level.getGameTime() + durationTicks;
         TankerSkillManager.SlowState state = ACTIVE_SLOWS.get(target.getUUID());
         if (state == null || state.level != level) {
            if (state != null) {
               removeSlowModifier(target);
            }

            state = new TankerSkillManager.SlowState(target.getUUID(), level);
            ACTIVE_SLOWS.put(target.getUUID(), state);
         }

         TankerSkillManager.SlowContribution old = state.contributions.get(owner.getUUID());
         if (old != null && old.expiresAt > now) {
            fraction = Math.max(fraction, old.fraction);
            expiry = Math.max(expiry, old.expiresAt);
         }

         state.contributions.put(owner.getUUID(), new TankerSkillManager.SlowContribution(expiry, fraction));
         if (!refreshSlowModifier(target, state, now)) {
            ACTIVE_SLOWS.remove(target.getUUID());
         }
      }
   }

   private static void tickSlows(MinecraftServer server) {
      Iterator<Entry<UUID, TankerSkillManager.SlowState>> iterator = ACTIVE_SLOWS.entrySet().iterator();

      while (iterator.hasNext()) {
         TankerSkillManager.SlowState slow = iterator.next().getValue();
         Entity entity = slow.level.getEntity(slow.targetId);
         long now = slow.level.getGameTime();
         if (entity instanceof LivingEntity living && living.isAlive()) {
            if (!refreshSlowModifier(living, slow, now)) {
               iterator.remove();
            }
         } else {
            if (entity instanceof LivingEntity living) {
               removeSlowModifier(living);
            }

            iterator.remove();
         }
      }
   }

   private static void removeOwnedSlows(UUID ownerId) {
      Iterator<Entry<UUID, TankerSkillManager.SlowState>> iterator = ACTIVE_SLOWS.entrySet().iterator();

      while (iterator.hasNext()) {
         TankerSkillManager.SlowState slow = iterator.next().getValue();
         if (slow.contributions.remove(ownerId) != null) {
            if (slow.level.getEntity(slow.targetId) instanceof LivingEntity living) {
               if (!refreshSlowModifier(living, slow, slow.level.getGameTime())) {
                  iterator.remove();
               }
            } else if (slow.contributions.isEmpty()) {
               iterator.remove();
            }
         }
      }
   }

   private static void removeSlow(Entity entity) {
      if (entity instanceof LivingEntity living) {
         ACTIVE_SLOWS.remove(entity.getUUID());
         removeSlowModifier(living);
      }
   }

   private static void removeSlowModifier(LivingEntity living) {
      AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         speed.removeModifier(CONTROL_SLOW_MODIFIER_ID);
      }
   }

   private static boolean refreshSlowModifier(LivingEntity living, TankerSkillManager.SlowState state, long now) {
      state.contributions.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
      if (state.contributions.isEmpty()) {
         removeSlowModifier(living);
         return false;
      }

      double fraction = state.contributions.values().stream().mapToDouble(TankerSkillManager.SlowContribution::fraction).max().orElse(0.0);
      AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed == null) {
         return false;
      }

      AttributeModifier current = speed.getModifier(CONTROL_SLOW_MODIFIER_ID);
      double clamped = Mth.clamp(fraction, 0.0, 0.95);
      if (current == null || Math.abs(current.getAmount() + clamped) > 1.0E-7) {
         speed.removeModifier(CONTROL_SLOW_MODIFIER_ID);
         speed.addTransientModifier(new AttributeModifier(CONTROL_SLOW_MODIFIER_ID, "Tanker control slow", -clamped, Operation.MULTIPLY_TOTAL));
      }

      return true;
   }

   private static void pruneClaims(MinecraftServer server) {
      Iterator<Entry<UUID, TankerSkillManager.TauntClaim>> iterator = TAUNT_CLAIMS.entrySet().iterator();

      while (iterator.hasNext()) {
         TankerSkillManager.TauntClaim claim = iterator.next().getValue();
         if (claim.level.getGameTime() >= claim.expiresAt || claim.level.getEntity(claim.targetId) == null) {
            iterator.remove();
         }
      }
   }

   private static void pruneChallenges(MinecraftServer server) {
      Iterator<Entry<UUID, TankerSkillManager.ChallengedState>> iterator = CHALLENGED.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<UUID, TankerSkillManager.ChallengedState> entry = iterator.next();
         TankerSkillManager.ChallengedState challenged = entry.getValue();
         ServerPlayer target = server.getPlayerList().getPlayer(entry.getKey());
         ServerPlayer taunter = server.getPlayerList().getPlayer(challenged.taunterId);
         if (challenged.level.getGameTime() >= challenged.expiresAt
            || target == null
            || taunter == null
            || !target.isAlive()
            || !taunter.isAlive()
            || target.level() != challenged.level
            || taunter.level() != challenged.level) {
            iterator.remove();
         }
      }
   }

   private static void removeChallengesFor(UUID playerId) {
      CHALLENGED.entrySet().removeIf(entry -> entry.getKey().equals(playerId) || entry.getValue().taunterId.equals(playerId));
      TAUNT_CLAIMS.entrySet().removeIf(entry -> entry.getValue().taunterId.equals(playerId));
   }

   private static boolean dealTankerDamage(ServerPlayer player, LivingEntity target, float amount) {
      if (!(amount <= 0.0F) && validEnemy(player, target, false)) {
         boolean masteryAvailable = !CooldownManager.isOnCooldown(player, "mastery");
         if (masteryAvailable) {
            CooldownManager.setFullDuration(player, "mastery", 10);
         }

         boolean damaged = target.hurt(tankerDamageSource(player.serverLevel(), player), amount);
         if (masteryAvailable) {
            if (damaged) {
               addMasteryCredit(player, TankerSkillManager.MasteryTrigger.TANKER_DAMAGE);
            } else {
               CooldownManager.clear(player, "mastery");
            }
         }

         return damaged;
      } else {
         return false;
      }
   }

   private static boolean tryAwardMastery(ServerPlayer player, TankerSkillManager.MasteryTrigger trigger) {
      if (player != null && isTanker(player) && !CooldownManager.isOnCooldown(player, "mastery")) {
         CooldownManager.setFullDuration(player, "mastery", 10);
         addMasteryCredit(player, trigger);
         return true;
      } else {
         return false;
      }
   }

   private static void addMasteryCredit(ServerPlayer player, TankerSkillManager.MasteryTrigger trigger) {
      boolean[] unlock = new boolean[]{false};
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
         vars.progression_tanker++;
         double threshold = vars.progression_multiplier_tanker * 7.0;
         if (vars.progression_tanker > threshold) {
            vars.progression_tanker = 0.0;
            vars.progression_multiplier_tanker++;
            unlock[0] = true;
         }

         vars.syncPlayerVariables(player);
      });
      if (unlock[0]) {
         grantNextMasterySkill(player);
      }
   }

   private static void stripLegacyEffects(ServerPlayer player) {
      removeEffect(player, SololevelingModMobEffects.FORTIFY.get());
      removeEffect(player, SololevelingModMobEffects.WILL_POWER.get());
      removeEffect(player, SololevelingModMobEffects.SHIELD_BASH_EFFECT.get());
      removeEffect(player, SololevelingModMobEffects.WILLPOWER_COOLDOWN.get());
      removeEffect(player, SololevelingModMobEffects.SHIELD_BASH_COOLDOWN.get());
      removeEffect(player, SololevelingModMobEffects.TAUNT_COOLDOWN.get());
   }

   private static void removeEffect(LivingEntity entity, MobEffect effect) {
      if (effect != null && entity.hasEffect(effect)) {
         entity.removeEffect(effect);
      }
   }

   private static int currentIronWall(ServerPlayer player, long now) {
      CompoundTag data = player.getPersistentData();
      int stacks = Mth.clamp(data.getInt("sl_t_stacks"), 0, 10);
      if (stacks > 0 && now >= data.getLong("slr_tanker_iron_wall_expires_at")) {
         data.putInt("sl_t_stacks", 0);
         data.remove("slr_tanker_iron_wall_expires_at");
         data.remove("slr_tanker_iron_wall_last_damage_tick");
         syncIronWall(player, 0);
         return 0;
      }

      if (data.getInt("sl_t_stacks") != stacks) {
         data.putInt("sl_t_stacks", stacks);
      }

      return stacks;
   }

   private static void grantDamageIronWall(ServerPlayer player, long now) {
      CompoundTag data = player.getPersistentData();
      if (!data.contains("slr_tanker_iron_wall_last_damage_tick") || data.getLong("slr_tanker_iron_wall_last_damage_tick") != now) {
         data.putLong("slr_tanker_iron_wall_last_damage_tick", now);
         addIronWall(player, 1, now);
      }
   }

   private static void addIronWall(ServerPlayer player, int amount, long now) {
      if (amount > 0 && isTanker(player)) {
         CompoundTag data = player.getPersistentData();
         int oldStacks = currentIronWall(player, now);
         int stacks = Mth.clamp(oldStacks + amount, 0, 10);
         data.putInt("sl_t_stacks", stacks);
         data.putLong("slr_tanker_iron_wall_expires_at", now + 200L);
         if (stacks != oldStacks) {
            syncIronWall(player, stacks);
         }
      }
   }

   private static void refreshIronWall(ServerPlayer player, long now) {
      CompoundTag data = player.getPersistentData();
      if (currentIronWall(player, now) > 0) {
         data.putLong("slr_tanker_iron_wall_expires_at", now + 200L);
      }
   }

   private static void clearIronWall(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      boolean changed = data.getInt("sl_t_stacks") != 0;
      data.putInt("sl_t_stacks", 0);
      data.remove("slr_tanker_iron_wall_expires_at");
      data.remove("slr_tanker_iron_wall_last_damage_tick");
      data.remove("sl_t_timer");
      if (changed) {
         syncIronWall(player, 0);
      }
   }

   private static void syncIronWall(ServerPlayer player, int stacks) {
      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClassPassiveMessage(2, Mth.clamp(stacks, 0, 10)));
   }

   private static void persistWillpower(ServerPlayer player, TankerSkillManager.WillpowerState willpower) {
      CompoundTag data = player.getPersistentData();
      if (willpower != null && (!(willpower.strain <= 1.0E-7) || willpower.active || willpower.settling)) {
         data.putDouble("slr_tanker_willpower_debt", Math.max(0.0, willpower.strain));
         data.putDouble("slr_tanker_willpower_max_health", willpower.maxHealthAtActivation);
         data.putInt("slr_tanker_willpower_pulses", willpower.settling ? Mth.clamp(willpower.pulsesRemaining, 1, 4) : 4);
      } else {
         clearPersistedWillpower(player);
      }
   }

   private static void loadPendingSettlement(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      double debt = Math.max(0.0, data.getDouble("slr_tanker_willpower_debt"));
      if (debt <= 1.0E-7) {
         clearPersistedWillpower(player);
      } else {
         int pulses = Mth.clamp(data.getInt("slr_tanker_willpower_pulses"), 1, 4);
         double maximumHealth = Math.max(1.0, data.getDouble("slr_tanker_willpower_max_health"));
         TankerSkillManager.TankerState state = state(player);
         state.willpower = TankerSkillManager.WillpowerState.settling(debt, maximumHealth, pulses, player.level().getGameTime() + 10L);
      }
   }

   private static void clearPersistedWillpower(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      data.remove("slr_tanker_willpower_debt");
      data.remove("slr_tanker_willpower_max_health");
      data.remove("slr_tanker_willpower_pulses");
   }

   private static TankerSkillManager.CombatContext combatContext(ServerPlayer victim, DamageSource source) {
      if (source != null
         && !source.is(TankerSkillManager.RuntimeKeys.WILLPOWER_STRAIN_DAMAGE)
         && !source.is(DamageTypes.FALL)
         && !source.is(DamageTypes.DROWN)
         && !source.is(DamageTypes.STARVE)
         && !source.is(DamageTypes.FELL_OUT_OF_WORLD)
         && !source.is(DamageTypes.GENERIC_KILL)) {
         Entity owner = resolveSourceOwner(source);
         if (owner != null && owner != victim && !owner.getUUID().equals(victim.getUUID()) && !MageCombatHelper.areAllied(owner, victim)) {
            if (owner instanceof Player attacker && !attacker.canHarmPlayer(victim)) {
               return null;
            } else {
               TankerSkillManager.CombatCategory category = owner instanceof Player
                  ? TankerSkillManager.CombatCategory.PVP
                  : (owner instanceof LivingEntity living && isBoss(living) ? TankerSkillManager.CombatCategory.BOSS : TankerSkillManager.CombatCategory.NORMAL);
               return new TankerSkillManager.CombatContext(owner, category);
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Entity resolveSourceOwner(DamageSource source) {
      if (source == null) {
         return null;
      }

      Entity entity = source.getEntity();
      if (entity == null) {
         entity = source.getDirectEntity();
      }

      return resolveOwner(entity);
   }

   private static Entity resolveOwner(Entity entity) {
      Entity current = entity;

      for (int depth = 0; depth < 4 && current != null; depth++) {
         Entity owner = null;
         if (current instanceof Projectile projectile) {
            owner = projectile.getOwner();
         }

         if (owner == null && current instanceof OwnableEntity ownable) {
            owner = ownable.getOwner();
         }

         if (owner == null) {
            UUID shadowOwner = ShadowMonarchManager.getShadowOwnerUUID(current);
            if (shadowOwner != null && current.getServer() != null) {
               owner = current.getServer().getPlayerList().getPlayer(shadowOwner);
            }
         }

         if (owner == null || owner == current) {
            break;
         }

         current = owner;
      }

      return current;
   }

   private static boolean validEnemy(ServerPlayer source, LivingEntity target, boolean requireLineOfSight) {
      if (source == null
         || target == null
         || target == source
         || target instanceof ArmorStand
         || !target.isAlive()
         || !target.isAttackable()
         || target.isInvulnerable()) {
         return false;
      } else {
         return !MageCombatHelper.isValidTarget(source, target) ? false : !requireLineOfSight || source.hasLineOfSight(target);
      }
   }

   private static boolean validTauntTarget(ServerPlayer source, LivingEntity target) {
      if (!validEnemy(source, target, false)) {
         return false;
      } else {
         return target instanceof ServerPlayer ? true : target instanceof Mob mob && mob.canAttack(source);
      }
   }

   private static boolean validMarkPlayer(ServerPlayer owner, ServerPlayer candidate, TankerSkillManager.ProtectionZone zone) {
      if (owner == null
         || candidate == null
         || zone == null
         || !candidate.isAlive()
         || candidate.isCreative()
         || candidate.isSpectator()
         || candidate.level() != zone.level
         || candidate.distanceToSqr(zone.center) > 36.0) {
         return false;
      }

      if (candidate == owner) {
         return true;
      }

      String ownerParty = party(owner);
      return !ownerParty.isBlank() && ownerParty.equals(party(candidate));
   }

   private static boolean isBoss(LivingEntity entity) {
      return !(entity instanceof Player) && (entity.getType().is(TankerSkillManager.RuntimeKeys.BOSS_TAG) || entity.getMaxHealth() >= 250.0F);
   }

   private static TankerSkillManager.CombatCategory offensiveCategory(LivingEntity target) {
      if (target instanceof Player) {
         return TankerSkillManager.CombatCategory.PVP;
      } else {
         return isBoss(target) ? TankerSkillManager.CombatCategory.BOSS : TankerSkillManager.CombatCategory.NORMAL;
      }
   }

   private static double tankerPower(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables vars = variables(player);
      return Math.max(
         4.0, player.getAttributeValue(Attributes.ATTACK_DAMAGE) + TemporaryStatBonusManager.effectiveStrength(player) / 14.0 + vars.Vitality / 28.0
      );
   }

   public static double ironWallReduction(int stacks, TankerSkillManager.CombatCategory category) {
      Objects.requireNonNull(category, "category");
      double perStack = category == TankerSkillManager.CombatCategory.PVP ? 0.01 : 0.02;
      double maximum = category == TankerSkillManager.CombatCategory.PVP ? 0.1 : 0.2;
      return Math.min(maximum, Mth.clamp(stacks, 0, 10) * perStack);
   }

   public static double reinforcementReduction(TankerSkillManager.ReinforcementPhase phase, TankerSkillManager.CombatCategory category) {
      Objects.requireNonNull(category, "category");
      if (phase == null) {
         phase = TankerSkillManager.ReinforcementPhase.NONE;
      }
      return switch (phase) {
         case PERFECT -> {
            switch (category) {
               case NORMAL:
                  yield 0.6;
               case BOSS:
                  yield 0.5;
               case PVP:
                  yield 0.35;
               default:
                  throw new IncompatibleClassChangeError();
            }
         }
         case STANCE -> {
            switch (category) {
               case NORMAL:
                  yield 0.25;
               case BOSS:
                  yield 0.2;
               case PVP:
                  yield 0.15;
               default:
                  throw new IncompatibleClassChangeError();
            }
         }
         case NONE -> 0.0;
      };
   }

   public static double knockbackMultiplier(TankerSkillManager.ReinforcementPhase phase, TankerSkillManager.CombatCategory category) {
      Objects.requireNonNull(category, "category");
      if (phase == TankerSkillManager.ReinforcementPhase.PERFECT) {
         return category == TankerSkillManager.CombatCategory.PVP ? 0.5 : 0.2;
      } else if (phase == TankerSkillManager.ReinforcementPhase.STANCE) {
         return category == TankerSkillManager.CombatCategory.PVP ? 0.7 : 0.5;
      } else {
         return 1.0;
      }
   }

   public static double customReductionCap(TankerSkillManager.CombatCategory category) {
      Objects.requireNonNull(category, "category");

      return switch (category) {
         case NORMAL -> 0.65;
         case BOSS -> 0.55;
         case PVP -> 0.45;
      };
   }

   public static double retainedDamage(
      double incomingDamage,
      TankerSkillManager.CombatCategory category,
      int ironWallStacks,
      double protectionMarkReduction,
      TankerSkillManager.ReinforcementPhase reinforcementPhase
   ) {
      double incoming = finiteNonNegative(incomingDamage);
      double mark = Mth.clamp(finiteNonNegative(protectionMarkReduction), 0.0, 1.0);
      double layered = incoming
         * (1.0 - ironWallReduction(ironWallStacks, category))
         * (1.0 - mark)
         * (1.0 - reinforcementReduction(reinforcementPhase, category));
      return Math.max(incoming * (1.0 - customReductionCap(category)), layered);
   }

   private static double delayedShare(TankerSkillManager.CombatCategory category) {
      return switch (category) {
         case NORMAL -> 0.5;
         case BOSS -> 0.4;
         case PVP -> 0.3;
      };
   }

   private static double strainCapFraction(TankerSkillManager.CombatCategory category) {
      return switch (category) {
         case NORMAL -> 0.4;
         case BOSS -> 0.35;
         case PVP -> 0.25;
      };
   }

   public static TankerSkillManager.WillpowerHitResult calculateWillpowerHit(
      double retainedDamage, double currentStrain, double currentCap, double maxHealthAtActivation, TankerSkillManager.CombatCategory category
   ) {
      Objects.requireNonNull(category, "category");
      double retained = finiteNonNegative(retainedDamage);
      double strain = finiteNonNegative(currentStrain);
      double maximumHealth = finiteNonNegative(maxHealthAtActivation);
      double priorCap = Double.isFinite(currentCap) ? Math.max(0.0, currentCap) : Double.POSITIVE_INFINITY;
      double activeCap = Math.min(priorCap, maximumHealth * strainCapFraction(category));
      if (strain + 1.0E-7 >= activeCap) {
         return new TankerSkillManager.WillpowerHitResult(retained, 0.0, strain, activeCap, true);
      }

      double requestedDelay = retained * delayedShare(category);
      double delayed = Math.min(requestedDelay, Math.max(0.0, activeCap - strain));
      double resultingStrain = strain + delayed;
      return new TankerSkillManager.WillpowerHitResult(retained - delayed, delayed, resultingStrain, activeCap, resultingStrain + 1.0E-7 >= activeCap);
   }

   public static TankerSkillManager.StrainReliefResult calculateShieldBashRelief(double currentStrain, double maxHealthAtActivation, boolean reliefAlreadyUsed) {
      double strain = finiteNonNegative(currentStrain);
      if (!reliefAlreadyUsed && !(strain <= 1.0E-7)) {
         double relief = Math.min(strain, finiteNonNegative(maxHealthAtActivation) * 0.08);
         return relief <= 1.0E-7
            ? new TankerSkillManager.StrainReliefResult(0.0, strain, false)
            : new TankerSkillManager.StrainReliefResult(relief, strain - relief, true);
      } else {
         return new TankerSkillManager.StrainReliefResult(0.0, strain, reliefAlreadyUsed);
      }
   }

   public static double[] settlementPulses(double unpaidStrain) {
      double debt = finiteNonNegative(unpaidStrain);
      double[] pulses = new double[4];
      double regularPulse = debt / 4.0;

      for (int index = 0; index < 3; index++) {
         pulses[index] = regularPulse;
      }

      pulses[3] = Math.max(0.0, debt - regularPulse * 3.0);
      return pulses;
   }

   public static double markReduction(boolean owner, TankerSkillManager.CombatCategory category) {
      Objects.requireNonNull(category, "category");
      if (owner) {
         return switch (category) {
            case NORMAL -> 0.12;
            case BOSS -> 0.1;
            case PVP -> 0.08;
         };
      } else {
         return switch (category) {
            case NORMAL -> 0.18;
            case BOSS -> 0.15;
            case PVP -> 0.1;
         };
      }
   }

   public static double protectionMarkInitialIntegrity(double ownerMaxHealth) {
      return finiteNonNegative(ownerMaxHealth) * 0.75;
   }

   public static double protectionMarkDrainMultiplier(TankerSkillManager.CombatCategory category) {
      Objects.requireNonNull(category, "category");
      return category == TankerSkillManager.CombatCategory.BOSS ? 1.5 : 1.0;
   }

   public static double protectionMarkRequestedPrevention(
      double incomingDamage,
      TankerSkillManager.CombatCategory category,
      int ironWallStacks,
      double nominalMarkReduction,
      TankerSkillManager.ReinforcementPhase reinforcementPhase
   ) {
      double withoutMark = retainedDamage(incomingDamage, category, ironWallStacks, 0.0, reinforcementPhase);
      double withMark = retainedDamage(incomingDamage, category, ironWallStacks, nominalMarkReduction, reinforcementPhase);
      return Math.max(0.0, withoutMark - withMark);
   }

   public static TankerSkillManager.ProtectionFundingResult calculateProtectionFunding(
      double requestedPrevention, double remainingIntegrity, TankerSkillManager.CombatCategory category
   ) {
      double requested = finiteNonNegative(requestedPrevention);
      double integrity = finiteNonNegative(remainingIntegrity);
      double multiplier = protectionMarkDrainMultiplier(category);
      double prevented = Math.min(requested, integrity / multiplier);
      double spent = Math.min(integrity, prevented * multiplier);
      double remaining = Math.max(0.0, integrity - spent);
      return new TankerSkillManager.ProtectionFundingResult(prevented, spent, remaining, requested > 1.0E-7 && remaining <= 1.0E-7);
   }

   private static double finiteNonNegative(double value) {
      return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
   }

   private static Vec3 horizontalDirection(Entity entity) {
      Vec3 look = entity.getLookAngle();
      Vec3 horizontal = new Vec3(look.x, 0.0, look.z);
      if (horizontal.lengthSqr() <= 1.0E-7) {
         double radians = Math.toRadians(entity.getYRot());
         horizontal = new Vec3(-Math.sin(radians), 0.0, Math.cos(radians));
      }

      return horizontal.normalize();
   }

   private static Vec3 findSafeHorizontalEndpoint(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 direction, double maximumDistance) {
      Vec3 lastSafe = start;

      for (double distance = 0.15; distance <= maximumDistance + 1.0E-7; distance += 0.15) {
         Vec3 candidate = start.add(direction.scale(Math.min(distance, maximumDistance)));
         AABB moved = player.getBoundingBox().move(candidate.subtract(start));
         if (!isLoadedAndWithinBorder(level, moved) || !level.noCollision(player, moved)) {
            break;
         }

         lastSafe = candidate;
      }

      return lastSafe;
   }

   private static boolean isLoadedAndWithinBorder(ServerLevel level, AABB box) {
      BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
      BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
      return level.hasChunkAt(min) && level.hasChunkAt(max) && level.getWorldBorder().isWithinBounds(min) && level.getWorldBorder().isWithinBounds(max);
   }

   private static Vec3 findGroundBelow(ServerLevel level, ServerPlayer player) {
      MutableBlockPos cursor = new MutableBlockPos(Mth.floor(player.getX()), Mth.floor(player.getY() - 0.05), Mth.floor(player.getZ()));

      for (int offset = 0; offset <= 8; offset++) {
         cursor.setY(Mth.floor(player.getY() - 0.05) - offset);
         if (!level.hasChunkAt(cursor) || !level.getWorldBorder().isWithinBounds(cursor)) {
            return null;
         }

         BlockState block = level.getBlockState(cursor);
         VoxelShape shape = block.getCollisionShape(level, cursor);
         if (!shape.isEmpty()) {
            double top = cursor.getY() + shape.max(Axis.Y);
            Vec3 center = new Vec3(player.getX(), top + 0.01, player.getZ());
            AABB markerSpace = new AABB(center.x - 0.25, center.y, center.z - 0.25, center.x + 0.25, center.y + 1.0, center.z + 0.25);
            return level.noCollision(markerSpace) ? center : null;
         }
      }

      return null;
   }

   private static double forwardProjection(Vec3 start, Vec3 direction, Vec3 point) {
      return point.subtract(start).dot(direction);
   }

   private static double horizontalDistance(Vec3 first, Vec3 second) {
      double x = first.x - second.x;
      double z = first.z - second.z;
      return Math.sqrt(x * x + z * z);
   }

   private static Comparator<LivingEntity> distanceThenUuid(Vec3 origin) {
      return Comparator.<LivingEntity>comparingDouble(target -> target.distanceToSqr(origin)).thenComparing(target -> target.getUUID().toString());
   }

   private static DamageSource tankerDamageSource(ServerLevel level, ServerPlayer player) {
      return new DamageSource(
         level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TankerSkillManager.RuntimeKeys.TANKER_DAMAGE), player
      );
   }

   private static DamageSource strainDamageSource(ServerLevel level) {
      return new DamageSource(
         level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TankerSkillManager.RuntimeKeys.WILLPOWER_STRAIN_DAMAGE)
      );
   }

   private static TankerSkillManager.TankerState state(ServerPlayer player) {
      return STATES.computeIfAbsent(player.getUUID(), ignored -> new TankerSkillManager.TankerState());
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static boolean isTanker(Entity entity) {
      return entity != null && isTanker(variables(entity));
   }

   private static boolean isTanker(SololevelingModVariables.PlayerVariables vars) {
      return vars != null && (int)Math.round(vars.Classes) == 4;
   }

   private static String party(Entity entity) {
      String party = variables(entity).party;
      return party == null ? "" : party.trim();
   }

   private static void message(ServerPlayer player, String text) {
      player.displayClientMessage(Component.literal(text), true);
   }

   private static int pvpFlag(TankerSkillManager.CombatCategory category) {
      return category == TankerSkillManager.CombatCategory.PVP ? 4 : 0;
   }

   private static int confirmedHitFlags(TankerSkillManager.CombatCategory category) {
      return 3 | pvpFlag(category);
   }

   private static int vfxIntensityBand(int band) {
      return Mth.clamp(band, 0, 4) == 4 ? 255 : Mth.clamp(band, 0, 4) * 64;
   }

   private static void emit(ServerPlayer owner, byte type, Entity target, Vec3 origin, long startTick, int duration, int intensity, int flags) {
      if (owner != null && owner.level() instanceof ServerLevel level) {
         int targetId = target == null ? -1 : target.getId();
         vfxSink.emit(
            level,
            new TankerSkillManager.VfxEvent(
               type,
               owner.getId(),
               targetId,
               origin.x,
               origin.y,
               origin.z,
               packRotation(owner.getYRot()),
               packRotation(owner.getXRot()),
               startTick,
               Math.max(0, duration),
               stableSeed(owner.getUUID(), type, startTick, targetId),
               Mth.clamp(intensity, 0, 255),
               flags & 0xFF
            )
         );
      }
   }

   private static void emit(TankerSkillManager.ProtectionZone zone, byte type, Entity target, long startTick, int duration, int intensity, int flags) {
      int targetId = target == null ? -1 : target.getId();
      vfxSink.emit(
         zone.level,
         new TankerSkillManager.VfxEvent(
            type,
            zone.ownerEntityId,
            targetId,
            zone.center.x,
            zone.center.y,
            zone.center.z,
            packRotation(zone.yaw),
            packRotation(zone.pitch),
            startTick,
            Math.max(0, duration),
            stableSeed(zone.ownerId, type, startTick, targetId),
            Mth.clamp(intensity, 0, 255),
            flags & 0xFF
         )
      );
   }

   private static short packRotation(float degrees) {
      return (short)Mth.floor(Mth.wrapDegrees(degrees) * 65536.0F / 360.0F);
   }

   private static int stableSeed(UUID owner, byte type, long tick, int targetId) {
      return Objects.hash(owner.getMostSignificantBits(), owner.getLeastSignificantBits(), type, tick, targetId);
   }

   private static final class BashState {
      private final long startedAt;
      private final Vec3 start;
      private final Vec3 end;
      private final Vec3 direction;
      private final UUID targetId;
      private boolean hit;

      private BashState(long startedAt, Vec3 start, Vec3 end, Vec3 direction, UUID targetId) {
         this.startedAt = startedAt;
         this.start = start;
         this.end = end;
         this.direction = direction;
         this.targetId = targetId;
      }
   }

   private record ChallengedState(UUID taunterId, ServerLevel level, long expiresAt) {
   }

   public enum CleanupAction {
      CLEAR,
      START_STRAIN_SETTLEMENT,
      PERSIST_STRAIN_SETTLEMENT;
   }

   public enum CleanupReason {
      DEATH,
      DIMENSION_CHANGE,
      CLASS_CHANGE,
      LOGOUT,
      SERVER_STOP;
   }

   public enum CombatCategory {
      NORMAL,
      BOSS,
      PVP;
   }

   private record CombatContext(Entity owner, TankerSkillManager.CombatCategory category) {
   }

   private static final class LeapState {
      private final ResourceKey<Level> dimension;
      private final long startedAt;
      private final long deadline;
      private final Vec3 start;
      private Vec3 lastSafe;
      private final Vec3 direction;
      private final LinkedHashSet<UUID> hitLedger = new LinkedHashSet<>();

      private LeapState(ResourceKey<Level> dimension, long startedAt, long deadline, Vec3 start, Vec3 lastSafe, Vec3 direction) {
         this.dimension = dimension;
         this.startedAt = startedAt;
         this.deadline = deadline;
         this.start = start;
         this.lastSafe = lastSafe;
         this.direction = direction;
      }
   }

   private enum MarkEnd {
      BREAK,
      CANCEL;
   }

   private record MarkSelection(TankerSkillManager.ProtectionZone zone, double reduction, boolean owner) {
   }

   private enum MasteryTrigger {
      TAUNT,
      TANKER_DAMAGE,
      REINFORCEMENT_BRACE,
      WILLPOWER_THRESHOLD,
      PROTECTION_MARK_ALLY;
   }

   private record PendingKnockback(long gameTick, double multiplier) {
   }

   public record ProtectionFundingResult(double preventedDamage, double integritySpent, double remainingIntegrity, boolean breaksField) {
   }

   private static final class ProtectionZone {
      private final UUID ownerId;
      private final int ownerEntityId;
      private final ServerLevel level;
      private final ResourceKey<Level> dimension;
      private final Vec3 center;
      private final long startedAt;
      private final long expiresAt;
      private final double initialIntegrity;
      private final float yaw;
      private final float pitch;
      private double integrity;
      private long nextMembershipUpdate;
      private LinkedHashSet<UUID> beneficiaries = new LinkedHashSet<>();
      private boolean allyMasteryTriggered;

      private ProtectionZone(
         UUID ownerId,
         int ownerEntityId,
         ServerLevel level,
         ResourceKey<Level> dimension,
         Vec3 center,
         long startedAt,
         long expiresAt,
         double integrity,
         float yaw,
         float pitch
      ) {
         this.ownerId = ownerId;
         this.ownerEntityId = ownerEntityId;
         this.level = level;
         this.dimension = dimension;
         this.center = center;
         this.startedAt = startedAt;
         this.expiresAt = expiresAt;
         this.integrity = integrity;
         this.initialIntegrity = integrity;
         this.yaw = yaw;
         this.pitch = pitch;
      }
   }

   public enum ReinforcementPhase {
      NONE,
      PERFECT,
      STANCE;
   }

   private static final class RuntimeKeys {
      private static final ResourceKey<DamageType> TANKER_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling", "tanker"));
      private static final ResourceKey<DamageType> WILLPOWER_STRAIN_DAMAGE = ResourceKey.create(
         Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling", "willpower_strain")
      );
      private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss"));
      private static final TagKey<Item> SHIELDS = ItemTags.create(new ResourceLocation("minecraft", "shields"));
   }

   private record SkillBalance(int flatFloor, double maximumManaFraction, int cooldownTicks, int regenLockTicks) {
   }

   private record SlowContribution(long expiresAt, double fraction) {
   }

   private static final class SlowState {
      private final UUID targetId;
      private final ServerLevel level;
      private final Map<UUID, TankerSkillManager.SlowContribution> contributions = new HashMap<>();

      private SlowState(UUID targetId, ServerLevel level) {
         this.targetId = targetId;
         this.level = level;
      }
   }

   public record StrainReliefResult(double removedStrain, double remainingStrain, boolean reliefUsed) {
   }

   private static final class TankerState {
      private TankerSkillManager.LeapState leap;
      private TankerSkillManager.BashState bash;
      private final LinkedHashMap<UUID, TankerSkillManager.TauntEntry> taunts = new LinkedHashMap<>();
      private long nextTauntMaintenance;
      private TankerSkillManager.ReinforcementPhase reinforcementPhase = TankerSkillManager.ReinforcementPhase.NONE;
      private long reinforcementExpiresAt;
      private TankerSkillManager.PendingKnockback pendingKnockback;
      private TankerSkillManager.WillpowerState willpower;

      private boolean isEmpty() {
         return this.leap == null
            && this.bash == null
            && this.taunts.isEmpty()
            && this.reinforcementPhase == TankerSkillManager.ReinforcementPhase.NONE
            && this.pendingKnockback == null
            && this.willpower == null;
      }
   }

   public record TargetOrder(UUID targetId, double primaryOrder, double squaredDistance) {
   }

   private static final class TauntClaim {
      private final UUID targetId;
      private final UUID taunterId;
      private final ServerLevel level;
      private final long expiresAt;

      private TauntClaim(UUID targetId, UUID taunterId, ServerLevel level, long expiresAt) {
         this.targetId = targetId;
         this.taunterId = taunterId;
         this.level = level;
         this.expiresAt = expiresAt;
      }
   }

   private record TauntEntry(UUID targetId, TankerSkillManager.CombatCategory category, long expiresAt) {
   }

   public enum TransientState {
      LEAP,
      SHIELD_BASH,
      TAUNT,
      REINFORCEMENT,
      WILLPOWER,
      PROTECTION_MARK,
      CHALLENGED,
      CONTROL_SLOW;
   }

   public record VfxEvent(
      byte eventType,
      int ownerEntityId,
      int targetEntityId,
      double x,
      double y,
      double z,
      short yaw,
      short pitch,
      long serverStartTick,
      int duration,
      int seed,
      int intensity,
      int flags
   ) {
   }

   @FunctionalInterface
   public interface VfxSink {
      void emit(ServerLevel var1, TankerSkillManager.VfxEvent var2);
   }

   public record WillpowerHitResult(double immediateDamage, double strainAdded, double resultingStrain, double activeCap, boolean endsWillpower) {
   }

   private static final class WillpowerState {
      private boolean active;
      private boolean settling;
      private long activeExpiresAt;
      private double maxHealthAtActivation;
      private double currentCap;
      private double strain;
      private boolean reliefUsed;
      private double shieldBashStrainRelief;
      private boolean masteryAwarded;
      private int lastThreshold;
      private int pulsesRemaining;
      private long nextPulseAt;
      private double totalRetained;
      private double totalImmediate;

      private static TankerSkillManager.WillpowerState active(long expiresAt, double maximumHealth) {
         TankerSkillManager.WillpowerState state = new TankerSkillManager.WillpowerState();
         state.active = true;
         state.activeExpiresAt = expiresAt;
         state.maxHealthAtActivation = maximumHealth;
         state.currentCap = maximumHealth * 0.4;
         return state;
      }

      private static TankerSkillManager.WillpowerState settling(double debt, double maximumHealth, int pulses, long nextPulseAt) {
         TankerSkillManager.WillpowerState state = new TankerSkillManager.WillpowerState();
         state.settling = true;
         state.strain = debt;
         state.maxHealthAtActivation = maximumHealth;
         state.currentCap = maximumHealth * 0.4;
         state.pulsesRemaining = pulses;
         state.nextPulseAt = nextPulseAt;
         return state;
      }
   }
}
