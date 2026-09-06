package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.network.StormStateMessage;

@EventBusSubscriber(modid = "sololeveling")
public final class StormMageSpellManager {
   public static final String STATIC_NEEDLE = "Static Needle";
   public static final String SLIPSTREAM = "Slipstream";
   public static final String THUNDERCLAP = "Thunderclap";
   public static final String LIGHTNING_ROD = "Lightning Rod";
   public static final String CHAIN_LIGHTNING = "Chain Lightning";
   public static final String THUNDERHEAD = "Thunderhead";
   public static final String SKYBREAKER = "Skybreaker";
   public static final String TEMPEST_INCARNATE = "Tempest Incarnate";
   public static final Set<String> STORM_SKILLS = Set.of(
      "Static Needle", "Slipstream", "Thunderclap", "Lightning Rod", "Chain Lightning", "Thunderhead", "Skybreaker", "Tempest Incarnate"
   );
   public static final Set<String> QTE_SKILLS = Set.of("Thunderhead", "Skybreaker", "Tempest Incarnate");
   public static final Set<String> INSTANT_SKILLS = Set.of("Static Needle", "Slipstream", "Thunderclap", "Lightning Rod", "Chain Lightning");
   public static final List<String> BARAN_CROSSOVER_SKILL_ORDER = List.of("Static Needle", "Slipstream", "Chain Lightning");
   public static final Set<String> BARAN_CROSSOVER_SKILLS = Set.copyOf(BARAN_CROSSOVER_SKILL_ORDER);
   private static final String VOLTAGE_TAG = "sl_storm_voltage";
   private static final String ROD_HIGHLIGHT_SOURCE = "storm:lightning_rod";
   private static final int ROD_HIGHLIGHT_COLOR = 16766554;
   private static final int ROD_HIGHLIGHT_PRIORITY = 170;
   private static final double[] COST_MULTIPLIER = new double[]{0.0, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5};
   private static final UUID TEMPEST_SPEED_MODIFIER = UUID.fromString("f457662b-592a-47ac-bbdb-b1c524af9ac2");
   private static final int CONDUCTIVE_DURATION = 120;
   private static final int ROD_DURATION = 240;
   private static final int TEMPEST_DURATION = 200;
   private static final double MAX_VOLTAGE = 100.0;
   private static final Map<UUID, StormMageSpellManager.StormState> STATES = new HashMap<>();
   private static final List<StormMageSpellManager.NeedleCast> ACTIVE_NEEDLES = new ArrayList<>();
   private static final List<StormMageSpellManager.SlipstreamCast> ACTIVE_DASHES = new ArrayList<>();
   private static final List<StormMageSpellManager.SkybreakerCast> ACTIVE_SKYBREAKERS = new ArrayList<>();
   private static final List<StormMageSpellManager.EchoCast> ACTIVE_ECHOES = new ArrayList<>();
   private static volatile StormMageSpellManager.EffectiveStageBonusProvider stageBonusProvider = caster -> 0;

   private StormMageSpellManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         UUID playerId = player.getUUID();
         ACTIVE_NEEDLES.removeIf(cast -> playerId.equals(cast.casterId));
         boolean wasDashing = ACTIVE_DASHES.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_SKYBREAKERS.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_ECHOES.removeIf(cast -> playerId.equals(cast.casterId));
         removeState(player, true);
         if (wasDashing) {
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;
            player.fallDistance = 0.0F;
         }
      }
   }

   public static void setEffectiveStageBonusProvider(StormMageSpellManager.EffectiveStageBonusProvider provider) {
      stageBonusProvider = provider == null ? caster -> 0 : provider;
   }

   public static void clearEffectiveStageBonusProvider() {
      stageBonusProvider = caster -> 0;
   }

   public static boolean isStormSkill(String skill) {
      return STORM_SKILLS.contains(skill);
   }

   public static boolean isQteSkill(String skill) {
      return QTE_SKILLS.contains(skill);
   }

   public static boolean isInstantSkill(String skill) {
      return INSTANT_SKILLS.contains(skill);
   }

   public static int outputStage(Entity caster) {
      double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
      if (intelligence >= 110.0) {
         return 5;
      } else if (intelligence >= 80.0) {
         return 4;
      } else if (intelligence >= 55.0) {
         return 3;
      } else {
         return intelligence >= 30.0 ? 2 : 1;
      }
   }

   public static int effectiveOutputStage(Entity caster) {
      int base = outputStage(caster);
      boolean whiteFlame = WhiteFlameMonarchManager.isSpiritualized(caster);
      int bonus = whiteFlame ? 1 : 0;

      try {
         bonus = Math.max(bonus, Math.max(0, stageBonusProvider.bonusStages(caster)));
      } catch (RuntimeException var7) {
      }

      double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
      int maximum = whiteFlame && intelligence > 110.0 ? 6 : 5;
      return Mth.clamp(base + bonus, 1, maximum);
   }

   public static boolean hasStormAccess(Entity caster) {
      return !(caster instanceof Player)
         ? false
         : WhiteFlameMonarchManager.isWhiteFlameVessel(caster) || STORM_SKILLS.stream().anyMatch(skill -> MageSpellProgression.hasSkill(caster, skill));
   }

   public static String stageName(int stage) {
      return switch (Mth.clamp(stage, 0, 6)) {
         case 1 -> "Spark";
         case 2 -> "Current";
         case 3 -> "Surge";
         case 4 -> "Tempest";
         case 5 -> "Stormborn";
         case 6 -> "Sovereign Tempest";
         default -> "Dormant";
      };
   }

   public static List<Component> tooltip(Entity caster, String skill) {
      int base = outputStage(caster);
      int effective = effectiveOutputStage(caster);
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
      lines.add(Component.literal(description(skill)).withStyle(ChatFormatting.GRAY));
      lines.add(
         Component.literal("Output: " + stageName(effective) + (effective > base ? " (Spiritualized)" : ""))
            .withStyle(effective >= 6 ? ChatFormatting.GOLD : ChatFormatting.BLUE)
      );
      lines.add(Component.literal(stageEffect(skill, effective)).withStyle(ChatFormatting.DARK_AQUA));
      lines.add(
         Component.literal(
               "Mana: "
                  + manaCost(caster, skill, effective, QTEResult.MISS)
                  + "  |  Cooldown: "
                  + String.format(Locale.ROOT, "%.1fs", cooldownTicks(skill) / 20.0)
            )
            .withStyle(ChatFormatting.DARK_GRAY)
      );
      return lines;
   }

   private static String description(String skill) {
      return switch (skill) {
         case "Static Needle" -> "Fire a precise bolt that marks enemies Conductive.";
         case "Slipstream" -> "Ride a safe four-tick current in your movement direction.";
         case "Thunderclap" -> "Peel nearby enemies away with a controlled thunder cone.";
         case "Lightning Rod" -> "Privately designate an aimed enemy as your routing root.";
         case "Chain Lightning" -> "Route a bounded lightning chain through visible enemies.";
         case "Thunderhead" -> "Place a short-lived storm that delivers scheduled strikes.";
         case "Skybreaker" -> "Call a telegraphed vertical finisher onto a marked target or point.";
         case "Tempest Incarnate" -> "Wear the storm, accelerating Voltage and creating bounded echoes.";
         default -> "Control motion, marks, and lightning routes.";
      };
   }

   private static String stageEffect(String skill, int stage) {
      return switch (skill) {
         case "Static Needle" -> stage < 2
            ? "Fast single-target mark."
            : (
               stage < 3
                  ? "Extended precision range."
                  : (
                     stage < 4
                        ? "Pierces once."
                        : (stage < 6 ? "Returns a delayed spark after impact." : "White-flame output pierces twice and strengthens its return.")
                  )
            );
         case "Slipstream" -> stage < 2
            ? "A collision-safe directional dash."
            : (
               stage < 4
                  ? "Cleanses Slowness and can harvest a nearby mark."
                  : (stage < 6 ? "Stores one extra dash charge per cooldown cycle." : "Longer white-flame current with two mark harvests.")
            );
         case "Thunderclap" -> stage < 4
            ? "A forward soft-control cone."
            : (stage < 6 ? "Overcharge converts it into a radial peel." : "White-flame output widens the cone without hard-controlling players.");
         case "Lightning Rod" -> stage < 6 ? "A twelve-second private routing priority." : "A sixteen-second white-flame routing priority.";
         case "Chain Lightning" -> stage < 3
            ? "A short controlled chain."
            : (stage < 6 ? "Routes up to five unique visible targets." : "Routes six targets with reduced jump falloff.");
         case "Thunderhead" -> stage < 4
            ? "Five scheduled strikes over seven seconds."
            : (stage < 6 ? "Six smarter strikes and an Overcharged final burst." : "The final scheduled strike may split once.");
         case "Skybreaker" -> stage < 4
            ? "A focused, readable vertical finisher."
            : (stage < 6 ? "Forks into nearby Conductive enemies." : "Leaves one reduced white-flame aftershock.");
         case "Tempest Incarnate" -> stage < 6
            ? "Ten seconds of speed, enhanced Voltage, and every-third-cast echoes."
            : "Twelve seconds of white-flame motion; echo and Overcharge caps remain bounded.";
         default -> "";
      };
   }

   public static boolean cast(Entity caster, String skill, QTEResult qteResult) {
      if (!(caster.level() instanceof ServerLevel level && isStormSkill(skill))) {
         return false;
      } else if (caster instanceof Player && !hasStormAccess(caster)) {
         message(caster, "You do not have access to Storm magic.");
         return false;
      } else if (caster instanceof Player && !MageSpellProgression.canCastLearnedSkill(caster, skill)) {
         message(caster, "You have not learned this Storm skill.");
         return false;
      } else {
         int stage = effectiveOutputStage(caster);
         StormMageSpellManager.StormState state = stateFor(level, caster);
         boolean bonusSlipstream = "Slipstream".equals(skill)
            && CooldownManager.isOnCooldown(caster, skill)
            && state.canSpendSlipstreamBonus(level.getGameTime());
         if (CooldownManager.isOnCooldown(caster, skill) && !bonusSlipstream) {
            message(caster, "Ability on cooldown!");
            return false;
         } else {
            StormMageSpellManager.CastTarget target = preflight(level, caster, skill, stage, state);
            if (!target.valid) {
               message(caster, target.failure);
               return false;
            } else {
               QTEResult result = qteResult == null ? QTEResult.MISS : qteResult;
               int cost = manaCost(caster, skill, stage, result);
               SololevelingModVariables.PlayerVariables data = variables(caster);
               if (!(caster instanceof Player player && player.isCreative()) && data.MP < cost) {
                  message(caster, "Not enough MP! Need " + cost + ".");
                  return false;
               } else {
                  boolean overcharged = isOverchargeEligible(skill) && consumeOvercharge(state, level.getGameTime());
                  boolean started = startSpell(level, caster, skill, stage, state, target, overcharged);
                  if (!started) {
                     return false;
                  }

                  if (cost > 0) {
                     deductMana(caster, cost);
                  }

                  if (bonusSlipstream) {
                     state.slipstreamBonusAvailable = false;
                  } else {
                     CooldownManager.set(caster, skill, cooldownTicks(skill));
                     if ("Slipstream".equals(skill) && (stage >= 4 || state.isTempestActive(level.getGameTime()))) {
                        state.slipstreamBonusAvailable = true;
                     }
                  }

                  CooldownManager.set(caster, "mana_refresh", 40);
                  return true;
               }
            }
         }
      }
   }

   public static boolean castNpc(Entity caster, String skill) {
      if (!(caster.level() instanceof ServerLevel level && isStormSkill(skill) && !CooldownManager.isOnCooldown(caster, skill))) {
         return false;
      } else {
         if (caster instanceof Player && !hasStormAccess(caster)) {
            return false;
         }

         int stage = effectiveOutputStage(caster);
         StormMageSpellManager.StormState state = stateFor(level, caster);
         StormMageSpellManager.CastTarget target = preflight(level, caster, skill, stage, state);
         if (!target.valid) {
            return false;
         }

         boolean overcharged = isOverchargeEligible(skill) && consumeOvercharge(state, level.getGameTime());
         boolean started = startSpell(level, caster, skill, stage, state, target, overcharged);
         if (started) {
            CooldownManager.set(caster, skill, cooldownTicks(skill));
         }

         return started;
      }
   }

   public static int manaCost(Entity caster, String skill, int stage, QTEResult result) {
      if (caster instanceof Player player && player.isCreative()) {
         return 0;
      } else {
         double percent = switch (skill) {
            case "Static Needle" -> 0.0035;
            case "Slipstream" -> 0.02;
            case "Thunderclap" -> 0.0325;
            case "Lightning Rod" -> 0.04;
            case "Chain Lightning" -> 0.08;
            case "Thunderhead" -> 0.11;
            case "Skybreaker" -> 0.13;
            case "Tempest Incarnate" -> 0.18;
            default -> 0.0;
         };
         double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
         double maximumMana = 1000.0 + intelligence * 100.0;
         double qte = isQteSkill(skill) ? MageQTEHelper.getManaCostMultiplier(result == null ? QTEResult.MISS : result, intelligence) : 1.0;
         return Math.max(0, OrbOfAvariceManager.adjustManaCost(caster, maximumMana * percent * COST_MULTIPLIER[Mth.clamp(stage, 1, 6)] * qte));
      }
   }

   public static int cooldownTicks(String skill) {
      return switch (skill) {
         case "Static Needle" -> 9;
         case "Slipstream" -> 80;
         case "Thunderclap" -> 120;
         case "Lightning Rod" -> 200;
         case "Chain Lightning" -> 280;
         case "Thunderhead" -> 480;
         case "Skybreaker" -> 600;
         case "Tempest Incarnate" -> 1200;
         default -> 20;
      };
   }

   public static double getVoltage(Entity caster) {
      if (caster == null) {
         return 0.0;
      }

      StormMageSpellManager.StormState state = STATES.get(caster.getUUID());
      return state == null ? caster.getPersistentData().getDouble("sl_storm_voltage") : state.voltage;
   }

   public static boolean isConductive(Entity caster, Entity target) {
      if (caster != null && target != null) {
         StormMageSpellManager.StormState state = STATES.get(caster.getUUID());
         return state != null && state.isConductive(target.getUUID(), caster.level().getGameTime());
      } else {
         return false;
      }
   }

   public static UUID getLightningRodTarget(Entity caster) {
      if (caster == null) {
         return null;
      }

      StormMageSpellManager.StormState state = STATES.get(caster.getUUID());
      return state != null && state.rodExpiresAt >= caster.level().getGameTime() ? state.rodTarget : null;
   }

   private static boolean startSpell(
      ServerLevel level,
      Entity caster,
      String skill,
      int stage,
      StormMageSpellManager.StormState state,
      StormMageSpellManager.CastTarget target,
      boolean overcharged
   ) {
      double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
      double levelScale = systemLevelDamageFactor(caster);

      return switch (skill) {
         case "Static Needle" -> startNeedle(level, caster, stage, (float)((1.4 + intelligence * 0.03) * levelScale), overcharged);
         case "Slipstream" -> startSlipstream(level, caster, stage);
         case "Thunderclap" -> castThunderclap(level, caster, stage, (float)((3.0 + intelligence * 0.045) * levelScale), state, overcharged);
         case "Lightning Rod" -> setLightningRod(level, caster, stage, state, target.entity);
         case "Chain Lightning" -> castChain(level, caster, stage, (float)((5.0 + intelligence * 0.075) * levelScale), state, target.entity, overcharged);
         case "Thunderhead" -> startThunderhead(level, caster, stage, (float)((3.0 + intelligence * 0.05) * levelScale), state, target.point, overcharged);
         case "Skybreaker" -> startSkybreaker(level, caster, stage, (float)((13.0 + intelligence * 0.11) * levelScale), state, target, overcharged);
         case "Tempest Incarnate" -> startTempest(level, caster, stage, state);
         default -> false;
      };
   }

   private static StormMageSpellManager.CastTarget preflight(ServerLevel level, Entity caster, String skill, int stage, StormMageSpellManager.StormState state) {
      if ("Lightning Rod".equals(skill)) {
         LivingEntity target = findLookTarget(level, caster, 28.0 + stage * 3.0);
         return target == null ? StormMageSpellManager.CastTarget.fail("No visible enemy is under your aim.") : StormMageSpellManager.CastTarget.entity(target);
      }

      if ("Chain Lightning".equals(skill)) {
         LivingEntity root = activeRodTarget(level, caster, state, 48.0);
         if (root == null) {
            root = findLookTarget(level, caster, 26.0 + stage * 3.0);
         }

         return root == null ? StormMageSpellManager.CastTarget.fail("No visible enemy can root the chain.") : StormMageSpellManager.CastTarget.entity(root);
      } else if (!"Thunderhead".equals(skill) && !"Skybreaker".equals(skill)) {
         return StormMageSpellManager.CastTarget.none();
      } else {
         LivingEntity rod = activeRodTarget(level, caster, state, 56.0);
         Vec3 point = rod == null ? groundAimPoint(level, caster, 24.0 + stage * 3.0) : rod.position();
         if (point != null && level.hasChunkAt(BlockPos.containing(point))) {
            return rod == null ? StormMageSpellManager.CastTarget.point(point) : StormMageSpellManager.CastTarget.both(rod, point);
         } else {
            return StormMageSpellManager.CastTarget.fail("The storm cannot form in an unloaded or obstructed area.");
         }
      }
   }

   private static boolean isOverchargeEligible(String skill) {
      return "Static Needle".equals(skill)
         || "Thunderclap".equals(skill)
         || "Chain Lightning".equals(skill)
         || "Thunderhead".equals(skill)
         || "Skybreaker".equals(skill);
   }

   private static boolean consumeOvercharge(StormMageSpellManager.StormState state, long now) {
      if (state.voltage + 1.0E-4 < 100.0) {
         return false;
      }

      if (state.isTempestActive(now) && state.tempestOvercharges >= 2) {
         return false;
      }

      state.voltage = 0.0;
      if (state.isTempestActive(now)) {
         state.tempestOvercharges++;
      }

      state.forceVoltageSync();
      return true;
   }

   private static boolean startNeedle(ServerLevel level, Entity caster, int stage, float damage, boolean overcharged) {
      Vec3 direction = safeDirection(caster.getLookAngle());
      double range = 24.0 + stage * 5.0;
      ACTIVE_NEEDLES.add(
         new StormMageSpellManager.NeedleCast(level, caster, stage, damage, overcharged, caster.getEyePosition().add(direction.scale(0.55)), direction, range)
      );
      play(level, caster.position(), SoundEvents.TRIDENT_THROW, 0.65F, 1.85F);
      return true;
   }

   private static boolean startSlipstream(ServerLevel level, Entity caster, int stage) {
      Vec3 direction = safeHorizontalDirection(caster);
      double distance = 6.0 + stage * 0.7;
      if (!hasSafeDashStep(level, caster, direction, Math.min(1.5, distance / 4.0))) {
         message(caster, "Slipstream is obstructed.");
         return false;
      }

      if (stage >= 2 && caster instanceof LivingEntity living) {
         living.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
      }

      ACTIVE_DASHES.add(new StormMageSpellManager.SlipstreamCast(level, caster, stage, direction, distance));
      play(level, caster.position(), SoundEvents.TRIDENT_RIPTIDE_1, 0.75F, 1.65F);
      return true;
   }

   private static boolean castThunderclap(
      ServerLevel level, Entity caster, int stage, float damage, StormMageSpellManager.StormState state, boolean overcharged
   ) {
      double radius = overcharged ? 6.5 + stage * 0.25 : 4.5 + stage * 0.45;
      Vec3 look = safeHorizontalDirection(caster);
      List<LivingEntity> targets = level.getEntitiesOfClass(
         LivingEntity.class, caster.getBoundingBox().inflate(radius), candidate -> MageCombatHelper.isValidTarget(caster, candidate)
      );
      targets.sort(Comparator.comparingDouble(caster::distanceToSqr));
      ArrayList<LivingEntity> hit = new ArrayList<>();

      for (LivingEntity target : targets) {
         if (hit.size() >= 12) {
            break;
         }

         Vec3 away = target.position().subtract(caster.position());
         Vec3 horizontal = new Vec3(away.x, 0.0, away.z);
         if ((overcharged || !(horizontal.lengthSqr() > 1.0E-5) || !(look.dot(horizontal.normalize()) < Math.cos(Math.toRadians(stage >= 6 ? 60.0 : 50.0))))
            && visible(level, caster.getEyePosition(), target)
            && MageCombatHelper.hurt(level, caster, target, damage)) {
            hit.add(target);
            applyThunderclapControl(caster, target, horizontal, state, level.getGameTime());
            sparkBurst(level, target.getBoundingBox().getCenter(), 5);
         }
      }

      if (!hit.isEmpty()) {
         awardHits(level, caster, state, new StormMageSpellManager.HitLedger(), hit);
         recordDamagingCast(level, caster, state, hit.get(0), damage);
      }

      ringParticles(level, caster.position().add(0.0, 0.7, 0.0), radius, overcharged ? 28 : 18);
      play(level, caster.position(), SoundEvents.GENERIC_EXPLODE, 0.8F, overcharged ? 1.25F : 1.65F);
      return true;
   }

   private static void applyThunderclapControl(Entity caster, LivingEntity target, Vec3 away, StormMageSpellManager.StormState state, long now) {
      boolean player = target instanceof Player;
      boolean boss = isBoss(target);
      if (!player && away.lengthSqr() > 1.0E-5) {
         double strength = boss ? 0.15 : 0.52;
         target.setDeltaMovement(target.getDeltaMovement().add(away.normalize().scale(strength)).add(0.0, boss ? 0.02 : 0.1, 0.0));
         target.hurtMarked = true;
      }

      if (state.isConductive(target.getUUID(), now)) {
         int duration = player ? 8 : (boss ? 7 : 24);
         int amplifier = !player && !boss ? 2 : 0;
         target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier, false, false));
      }
   }

   private static boolean setLightningRod(ServerLevel level, Entity caster, int stage, StormMageSpellManager.StormState state, LivingEntity target) {
      if (target != null && MageCombatHelper.isValidTarget(caster, target)) {
         state.clearRod(caster);
         state.rodTarget = target.getUUID();
         state.rodExpiresAt = level.getGameTime() + (stage >= 6 ? 320L : 240L);
         state.lastCombatAt = level.getGameTime();
         if (caster instanceof ServerPlayer player) {
            EntityHighlightSystem.show(player, target, "storm:lightning_rod", 16766554, (int)(state.rodExpiresAt - level.getGameTime()), 170);
         }

         particleLine(level, caster.getEyePosition(), target.getBoundingBox().getCenter(), ParticleTypes.ELECTRIC_SPARK, 10);
         sparkBurst(level, target.getBoundingBox().getCenter(), 10);
         play(level, target.position(), SoundEvents.TRIDENT_RETURN, 0.8F, 1.7F);
         return true;
      } else {
         return false;
      }
   }

   private static boolean castChain(
      ServerLevel level, Entity caster, int stage, float baseDamage, StormMageSpellManager.StormState state, LivingEntity root, boolean overcharged
   ) {
      if (root != null && MageCombatHelper.isValidTarget(caster, root) && visible(level, caster.getEyePosition(), root)) {
         int maximum = stage >= 6 ? 6 : (stage >= 3 ? 5 : (stage == 2 ? 4 : 3));
         double falloff = stage >= 6 ? 0.88 : 0.82;
         double budget = baseDamage * (stage >= 6 ? 2.95 : 2.65);
         ArrayList<LivingEntity> order = buildChain(level, caster, state, root, maximum, overcharged);
         if (order.isEmpty()) {
            return false;
         }

         double remaining = budget;
         ArrayList<LivingEntity> hit = new ArrayList<>();
         Vec3 previous = caster.getEyePosition();

         for (int index = 0; index < order.size() && remaining > 0.01; index++) {
            LivingEntity target = order.get(index);
            float intended = (float)(baseDamage * Math.pow(falloff, index));
            float dealt = (float)Math.min(remaining, intended);
            dealt = pvpAdjusted(target, dealt);
            if (MageCombatHelper.hurt(level, caster, target, dealt)) {
               hit.add(target);
               remaining -= intended;
               particleLine(level, previous, target.getBoundingBox().getCenter(), index % 2 == 0 ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.END_ROD, 10);
               previous = target.getBoundingBox().getCenter();
            }
         }

         if (!hit.isEmpty()) {
            awardHits(level, caster, state, new StormMageSpellManager.HitLedger(), hit);
            recordDamagingCast(level, caster, state, hit.get(0), baseDamage);
         }

         play(level, root.position(), SoundEvents.TRIDENT_THUNDER, 0.75F, overcharged ? 1.35F : 1.75F);
         return !hit.isEmpty();
      } else {
         return false;
      }
   }

   private static ArrayList<LivingEntity> buildChain(
      ServerLevel level, Entity caster, StormMageSpellManager.StormState state, LivingEntity root, int maximum, boolean branched
   ) {
      ArrayList<LivingEntity> result = new ArrayList<>();
      result.add(root);
      Set<UUID> used = new HashSet<>();
      used.add(root.getUUID());
      LivingEntity firstCursor = root;
      LivingEntity secondCursor = root;

      for (int jump = 1; jump < maximum; jump++) {
         LivingEntity cursor = branched && jump % 2 == 0 ? secondCursor : firstCursor;
         LivingEntity next = bestChainTarget(level, caster, state, cursor, used);
         if (next == null && cursor != root) {
            next = bestChainTarget(level, caster, state, root, used);
         }

         if (next == null) {
            break;
         }

         result.add(next);
         used.add(next.getUUID());
         if (branched && jump % 2 == 0) {
            secondCursor = next;
         } else {
            firstCursor = next;
         }
      }

      return result;
   }

   private static LivingEntity bestChainTarget(ServerLevel level, Entity caster, StormMageSpellManager.StormState state, LivingEntity cursor, Set<UUID> used) {
      long now = level.getGameTime();
      List<LivingEntity> candidates = level.getEntitiesOfClass(
         LivingEntity.class,
         cursor.getBoundingBox().inflate(9.0),
         candidate -> !used.contains(candidate.getUUID())
            && MageCombatHelper.isValidTarget(caster, candidate)
            && visible(level, cursor.getEyePosition(), candidate)
      );
      candidates.sort(
         Comparator.<LivingEntity>comparingInt(candidate -> state.isConductive(candidate.getUUID(), now) ? 0 : 1).thenComparingDouble(cursor::distanceToSqr)
      );
      return candidates.isEmpty() ? null : candidates.get(0);
   }

   private static boolean startThunderhead(
      ServerLevel level, Entity caster, int stage, float strikeDamage, StormMageSpellManager.StormState state, Vec3 center, boolean overcharged
   ) {
      if (center == null) {
         return false;
      }

      state.thunderhead = new StormMageSpellManager.ThunderheadState(level, caster, stage, center, strikeDamage, overcharged);
      cloudParticles(level, center.add(0.0, 6.0, 0.0), 22);
      play(level, center, SoundEvents.BEACON_ACTIVATE, 0.65F, 0.8F);
      return true;
   }

   private static boolean startSkybreaker(
      ServerLevel level,
      Entity caster,
      int stage,
      float damage,
      StormMageSpellManager.StormState state,
      StormMageSpellManager.CastTarget target,
      boolean overcharged
   ) {
      Vec3 center = target.point;
      if (center == null) {
         return false;
      }

      ACTIVE_SKYBREAKERS.add(
         new StormMageSpellManager.SkybreakerCast(level, caster, stage, damage, center, target.entity == null ? null : target.entity.getUUID(), overcharged)
      );
      columnParticles(level, center, 8.0, ParticleTypes.END_ROD, 14);
      play(level, center, SoundEvents.BEACON_POWER_SELECT, 0.8F, 1.25F);
      return true;
   }

   private static boolean startTempest(ServerLevel level, Entity caster, int stage, StormMageSpellManager.StormState state) {
      long now = level.getGameTime();
      state.tempestExpiresAt = now + (stage >= 6 ? 240L : 200L);
      state.tempestDamagingCasts = 0;
      state.tempestEchoes = 0;
      state.tempestOvercharges = 0;
      state.slipstreamBonusAvailable = true;
      if (caster instanceof LivingEntity living) {
         applyTempestSpeed(living, stage >= 6 ? 0.2 : 0.15);
      }

      ringParticles(level, caster.position().add(0.0, 0.8, 0.0), 2.2, 28);
      play(level, caster.position(), SoundEvents.TRIDENT_RIPTIDE_3, 1.0F, 1.35F);
      return true;
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         tickList(ACTIVE_NEEDLES);
         tickList(ACTIVE_DASHES);
         tickList(ACTIVE_SKYBREAKERS);
         tickList(ACTIVE_ECHOES);
         tickStates();
         if ((event.getServer().getTickCount() & 3) == 0) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
               if (hasStormAccess(player)) {
                  StormMageSpellManager.StormState state = stateFor(player.serverLevel(), player);
                  state.syncHud(player, false);
               } else if (STATES.containsKey(player.getUUID())) {
                  removeState(player, true);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (hasStormAccess(player)) {
            StormMageSpellManager.StormState state = stateFor(player.serverLevel(), player);
            state.syncHud(player, true);
         } else {
            sendClearHud(player);
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      removeState(event.getEntity(), true);
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerChangedDimensionEvent event) {
      removeState(event.getEntity(), true);
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      removeState(event.getEntity(), true);
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      STATES.clear();
      ACTIVE_NEEDLES.clear();
      ACTIVE_DASHES.clear();
      ACTIVE_SKYBREAKERS.clear();
      ACTIVE_ECHOES.clear();
   }

   private static <T extends StormMageSpellManager.ActiveCast> void tickList(List<T> casts) {
      Iterator<T> iterator = casts.iterator();

      while (iterator.hasNext()) {
         if (iterator.next().tick()) {
            iterator.remove();
         }
      }
   }

   private static void tickStates() {
      Iterator<Entry<UUID, StormMageSpellManager.StormState>> iterator = STATES.entrySet().iterator();

      while (iterator.hasNext()) {
         StormMageSpellManager.StormState state = iterator.next().getValue();
         Entity caster = state.level.getEntity(state.casterId);
         if (caster == null || !caster.isAlive() || caster.level() != state.level || caster instanceof Player && !hasStormAccess(caster)) {
            if (caster instanceof ServerPlayer player) {
               sendClearHud(player);
            }

            state.deactivate(caster);
            iterator.remove();
         } else {
            state.tick(caster);
         }
      }
   }

   private static StormMageSpellManager.StormState stateFor(ServerLevel level, Entity caster) {
      StormMageSpellManager.StormState current = STATES.get(caster.getUUID());
      if (current != null && current.level != level) {
         current.deactivate(caster);
         STATES.remove(caster.getUUID());
         current = null;
      }

      if (current == null) {
         current = new StormMageSpellManager.StormState(level, caster);
         STATES.put(caster.getUUID(), current);
      }

      return current;
   }

   private static void removeState(Entity entity, boolean clearClient) {
      if (entity != null) {
         StormMageSpellManager.StormState state = STATES.remove(entity.getUUID());
         if (state != null) {
            state.deactivate(entity);
         } else if (entity instanceof LivingEntity living) {
            removeTempestSpeed(living);
         }

         if (entity instanceof ServerPlayer player) {
            EntityHighlightSystem.clearSource(player, "storm:lightning_rod");
            if (clearClient) {
               sendClearHud(player);
            }
         }

         entity.getPersistentData().remove("sl_storm_voltage");
      }
   }

   private static void sendClearHud(ServerPlayer player) {
      if (player != null && !player.hasDisconnected()) {
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new StormStateMessage(false, 0, 1, false, false, false, false));
      }
   }

   private static void awardHits(
      ServerLevel level, Entity caster, StormMageSpellManager.StormState state, StormMageSpellManager.HitLedger ledger, List<? extends LivingEntity> targets
   ) {
      for (LivingEntity target : targets) {
         awardHit(level, caster, state, ledger, target);
      }
   }

   private static void awardHit(
      ServerLevel level, Entity caster, StormMageSpellManager.StormState state, StormMageSpellManager.HitLedger ledger, LivingEntity target
   ) {
      if (ledger.targets.add(target.getUUID())) {
         double raw = ledger.targets.size() == 1 ? 8.0 : 2.0;
         if (!ledger.rodAwarded && target.getUUID().equals(state.rodTarget) && state.rodExpiresAt >= level.getGameTime()) {
            raw += 2.0;
            ledger.rodAwarded = true;
         }

         double accepted = Math.min(raw, 14.0 - ledger.rawVoltage);
         if (!(accepted <= 0.0)) {
            ledger.rawVoltage += accepted;
            state.addVoltage(accepted * (state.isTempestActive(level.getGameTime()) ? 1.5 : 1.0));
            state.lastCombatAt = level.getGameTime();
         }
      }
   }

   private static void recordDamagingCast(
      ServerLevel level, Entity caster, StormMageSpellManager.StormState state, LivingEntity target, float representativeDamage
   ) {
      long now = level.getGameTime();
      state.lastCombatAt = now;
      if (state.isTempestActive(now)) {
         state.tempestDamagingCasts++;
         if (state.tempestDamagingCasts % 3 == 0 && state.tempestEchoes < 5) {
            state.tempestEchoes++;
            ACTIVE_ECHOES.add(new StormMageSpellManager.EchoCast(level, caster, target, Math.max(0.5F, representativeDamage * 0.35F), 6));
         }
      }
   }

   private static LivingEntity activeRodTarget(ServerLevel level, Entity caster, StormMageSpellManager.StormState state, double maximumRange) {
      if (state.rodTarget != null && state.rodExpiresAt >= level.getGameTime()) {
         if (level.getEntity(state.rodTarget) instanceof LivingEntity target
            && MageCombatHelper.isValidTarget(caster, target)
            && !(caster.distanceToSqr(target) > maximumRange * maximumRange)
            && visible(level, caster.getEyePosition(), target)) {
            return target;
         } else {
            state.clearRod(caster);
            return null;
         }
      } else {
         return null;
      }
   }

   private static LivingEntity findLookTarget(ServerLevel level, Entity caster, double range) {
      Vec3 start = caster.getEyePosition();
      Vec3 end = start.add(safeDirection(caster.getLookAngle()).scale(range));
      BlockHitResult blockHit = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, caster));
      if (blockHit.getType() != Type.MISS) {
         end = blockHit.getLocation();
      }

      AABB search = caster.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.25);
      EntityHitResult hit = ProjectileUtil.getEntityHitResult(
         caster,
         start,
         end,
         search,
         target -> target instanceof LivingEntity living && MageCombatHelper.isValidTarget(caster, living),
         start.distanceToSqr(end)
      );
      if (hit != null && hit.getEntity() instanceof LivingEntity living) {
         return living;
      } else {
         return caster instanceof Mob mob
               && mob.getTarget() != null
               && caster.distanceToSqr(mob.getTarget()) <= range * range
               && MageCombatHelper.isValidTarget(caster, mob.getTarget())
               && visible(level, start, mob.getTarget())
            ? mob.getTarget()
            : null;
      }
   }

   private static Vec3 groundAimPoint(ServerLevel level, Entity caster, double range) {
      Vec3 start = caster.getEyePosition();
      Vec3 end = start.add(safeDirection(caster.getLookAngle()).scale(range));
      BlockHitResult sight = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, caster));
      Vec3 point = sight.getType() == Type.MISS ? end : sight.getLocation();
      int startY = Mth.floor(Math.max(point.y + 2.0, caster.getY() + 2.0));
      MutableBlockPos cursor = new MutableBlockPos(Mth.floor(point.x), startY, Mth.floor(point.z));

      for (int offset = 0; offset < 32; offset++) {
         cursor.setY(startY - offset);
         if (!level.hasChunkAt(cursor)) {
            return null;
         }

         if (!level.getBlockState(cursor).isAir() && !level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
            return new Vec3(point.x, cursor.getY() + 1.01, point.z);
         }
      }

      Vec3 fallback = new Vec3(point.x, caster.getY(), point.z);
      return level.hasChunkAt(BlockPos.containing(fallback)) ? fallback : null;
   }

   private static boolean visible(ServerLevel level, Vec3 start, LivingEntity target) {
      Vec3 end = target.getBoundingBox().getCenter();
      BlockHitResult hit = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, target));
      return hit.getType() == Type.MISS || start.distanceToSqr(hit.getLocation()) + 0.2 >= start.distanceToSqr(end);
   }

   private static boolean hasSafeDashStep(ServerLevel level, Entity caster, Vec3 direction, double distance) {
      Vec3 destination = caster.position().add(direction.scale(distance));
      BlockPos targetPos = BlockPos.containing(destination);
      if (level.hasChunkAt(targetPos) && level.getWorldBorder().isWithinBounds(targetPos)) {
         BlockHitResult hit = level.clip(
            new ClipContext(
               caster.position().add(0.0, caster.getBbHeight() * 0.45, 0.0),
               destination.add(0.0, caster.getBbHeight() * 0.45, 0.0),
               Block.COLLIDER,
               Fluid.NONE,
               caster
            )
         );
         return hit.getType() == Type.MISS && level.noCollision(caster, caster.getBoundingBox().move(destination.subtract(caster.position())));
      } else {
         return false;
      }
   }

   private static boolean canMaintainActiveCast(Entity caster, ServerLevel level) {
      return caster != null && caster.isAlive() && caster.level() == level && (!(caster instanceof Player) || hasStormAccess(caster));
   }

   private static float pvpAdjusted(LivingEntity target, float damage) {
      return target instanceof Player ? damage * 0.65F : damage;
   }

   private static double systemLevelDamageFactor(Entity caster) {
      double systemLevel;
      if (caster instanceof Player) {
         systemLevel = variables(caster).Level;
      } else {
         systemLevel = caster.getPersistentData().getDouble("Level");
      }

      return 1.0 + Math.min(0.25, Math.max(0.0, systemLevel - 1.0) * 0.002);
   }

   private static boolean isBoss(LivingEntity target) {
      return !(target instanceof Player) && target.getMaxHealth() >= 250.0F;
   }

   private static Vec3 safeDirection(Vec3 value) {
      return value.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : value.normalize();
   }

   private static Vec3 safeHorizontalDirection(Entity caster) {
      Vec3 movement = caster.getDeltaMovement().multiply(1.0, 0.0, 1.0);
      if (movement.lengthSqr() > 0.015) {
         return movement.normalize();
      }

      Vec3 look = caster.getLookAngle().multiply(1.0, 0.0, 1.0);
      return safeDirection(look);
   }

   private static void applyTempestSpeed(LivingEntity living, double amount) {
      AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         speed.removeModifier(TEMPEST_SPEED_MODIFIER);
         speed.addTransientModifier(new AttributeModifier(TEMPEST_SPEED_MODIFIER, "Storm Mage Tempest speed", amount, Operation.MULTIPLY_TOTAL));
      }
   }

   private static void removeTempestSpeed(LivingEntity living) {
      AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         speed.removeModifier(TEMPEST_SPEED_MODIFIER);
      }
   }

   private static void particleLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int maximumPoints) {
      Vec3 delta = end.subtract(start);
      int points = Mth.clamp((int)Math.ceil(delta.length() * 1.5), 2, Math.max(2, maximumPoints));

      for (int index = 0; index <= points; index++) {
         Vec3 point = start.add(delta.scale((double)index / points));
         level.sendParticles(particle, point.x, point.y, point.z, 1, 0.025, 0.025, 0.025, 0.0);
      }
   }

   private static void columnParticles(ServerLevel level, Vec3 base, double height, ParticleOptions particle, int points) {
      for (int index = 0; index < points; index++) {
         double y = base.y + height * index / Math.max(1.0, points - 1.0);
         level.sendParticles(particle, base.x, y, base.z, 1, 0.08, 0.02, 0.08, 0.0);
      }
   }

   private static void ringParticles(ServerLevel level, Vec3 center, double radius, int points) {
      for (int index = 0; index < points; index++) {
         double angle = (Math.PI * 2) * index / points;
         ParticleOptions particle = index % 3 == 0 ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK;
         level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius, 1, 0.0, 0.02, 0.0, 0.0);
      }
   }

   private static void cloudParticles(ServerLevel level, Vec3 center, int count) {
      level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, Math.min(24, count), 2.2, 0.35, 2.2, 0.015);
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y - 0.2, center.z, Math.min(12, Math.max(3, count / 2)), 2.0, 0.25, 2.0, 0.02);
   }

   private static void sparkBurst(ServerLevel level, Vec3 center, int count) {
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, Math.min(16, count), 0.35, 0.45, 0.35, 0.06);
      level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, Math.min(5, Math.max(1, count / 3)), 0.18, 0.25, 0.18, 0.025);
   }

   private static void play(ServerLevel level, Vec3 position, SoundEvent sound, float volume, float pitch) {
      level.playSound(null, position.x, position.y, position.z, sound, SoundSource.PLAYERS, volume, pitch);
   }

   private static void deductMana(Entity caster, int amount) {
      caster.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(data -> {
         data.MP = Math.max(0.0, data.MP - amount);
         data.syncPlayerVariables(caster);
      });
   }

   private static void message(Entity caster, String text) {
      if (caster instanceof Player player && !player.level().isClientSide()) {
         player.displayClientMessage(Component.literal(text), true);
      }
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static LivingEntity nearestConductive(
      ServerLevel level, Entity caster, StormMageSpellManager.StormState state, Vec3 center, double radius, Set<UUID> excluded
   ) {
      return level.getEntitiesOfClass(
            LivingEntity.class,
            new AABB(center, center).inflate(radius),
            target -> MageCombatHelper.isValidTarget(caster, target)
               && !excluded.contains(target.getUUID())
               && state.isConductive(target.getUUID(), level.getGameTime())
               && visible(level, center, target)
         )
         .stream()
         .min(Comparator.comparingDouble(target -> target.distanceToSqr(center)))
         .orElse(null);
   }

   private interface ActiveCast {
      boolean tick();
   }

   private static final class CastTarget {
      private final boolean valid;
      private final String failure;
      private final LivingEntity entity;
      private final Vec3 point;

      private CastTarget(boolean valid, String failure, LivingEntity entity, Vec3 point) {
         this.valid = valid;
         this.failure = failure;
         this.entity = entity;
         this.point = point;
      }

      private static StormMageSpellManager.CastTarget none() {
         return new StormMageSpellManager.CastTarget(true, "", null, null);
      }

      private static StormMageSpellManager.CastTarget entity(LivingEntity entity) {
         return new StormMageSpellManager.CastTarget(true, "", entity, entity.getBoundingBox().getCenter());
      }

      private static StormMageSpellManager.CastTarget point(Vec3 point) {
         return new StormMageSpellManager.CastTarget(true, "", null, point);
      }

      private static StormMageSpellManager.CastTarget both(LivingEntity entity, Vec3 point) {
         return new StormMageSpellManager.CastTarget(true, "", entity, point);
      }

      private static StormMageSpellManager.CastTarget fail(String failure) {
         return new StormMageSpellManager.CastTarget(false, failure, null, null);
      }
   }

   private static final class EchoCast implements StormMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID targetId;
      private final Vec3 fallback;
      private final float damage;
      private final int delay;
      private int age;

      private EchoCast(ServerLevel level, Entity caster, LivingEntity target, float damage, int delay) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.targetId = target.getUUID();
         this.fallback = target.getBoundingBox().getCenter();
         this.damage = damage;
         this.delay = delay;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (!StormMageSpellManager.canMaintainActiveCast(caster, this.level)) {
            return true;
         }

         this.age++;
         if (this.age < this.delay) {
            return false;
         }

         if (this.level.getEntity(this.targetId) instanceof LivingEntity target && MageCombatHelper.isValidTarget(caster, target)) {
            MageCombatHelper.hurt(this.level, caster, target, StormMageSpellManager.pvpAdjusted(target, this.damage));
            StormMageSpellManager.particleLine(
               this.level, target.getBoundingBox().getCenter().add(0.0, 3.0, 0.0), target.getBoundingBox().getCenter(), ParticleTypes.END_ROD, 6
            );
            StormMageSpellManager.sparkBurst(this.level, target.getBoundingBox().getCenter(), 5);
         } else {
            StormMageSpellManager.sparkBurst(this.level, this.fallback, 3);
         }

         return true;
      }
   }

   @FunctionalInterface
   public interface EffectiveStageBonusProvider {
      int bonusStages(Entity var1);
   }

   private static final class HitLedger {
      private final Set<UUID> targets = new LinkedHashSet<>();
      private double rawVoltage;
      private boolean rodAwarded;
   }

   private static final class NeedleCast implements StormMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final int stage;
      private final float damage;
      private final boolean overcharged;
      private final Vec3 direction;
      private final double maximumRange;
      private final Set<UUID> hit = new HashSet<>();
      private final StormMageSpellManager.HitLedger voltageLedger = new StormMageSpellManager.HitLedger();
      private Vec3 position;
      private double travelled;
      private boolean recordedCast;
      private boolean forked;

      private NeedleCast(ServerLevel level, Entity caster, int stage, float damage, boolean overcharged, Vec3 position, Vec3 direction, double maximumRange) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.stage = stage;
         this.damage = damage;
         this.overcharged = overcharged;
         this.position = position;
         this.direction = direction;
         this.maximumRange = maximumRange;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (!StormMageSpellManager.canMaintainActiveCast(caster, this.level)) {
            return true;
         }

         double speed = 2.7 + this.stage * 0.12;
         Vec3 next = this.position.add(this.direction.scale(speed));
         if (!this.level.hasChunkAt(BlockPos.containing(next))) {
            return true;
         }

         BlockHitResult blockHit = this.level.clip(new ClipContext(this.position, next, Block.COLLIDER, Fluid.NONE, caster));
         Vec3 clipped = blockHit.getType() == Type.MISS ? next : blockHit.getLocation();
         EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            caster,
            this.position,
            clipped,
            new AABB(this.position, clipped).inflate(0.45),
            targetx -> targetx instanceof LivingEntity living && !this.hit.contains(targetx.getUUID()) && MageCombatHelper.isValidTarget(caster, living),
            this.position.distanceToSqr(clipped)
         );
         if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            this.hitTarget(caster, target);
            this.position = entityHit.getLocation().add(this.direction.scale(0.25));
            int allowedHits = this.stage >= 6 ? 3 : (this.stage >= 3 ? 2 : 1);
            if (this.hit.size() >= allowedHits) {
               return true;
            }
         } else {
            this.position = clipped;
         }

         this.level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.position.x, this.position.y, this.position.z, 2, 0.035, 0.035, 0.035, 0.0);
         this.travelled += speed;
         return blockHit.getType() != Type.MISS || this.travelled >= this.maximumRange;
      }

      private void hitTarget(Entity caster, LivingEntity target) {
         if (MageCombatHelper.hurt(this.level, caster, target, this.damage)) {
            this.hit.add(target.getUUID());
            StormMageSpellManager.StormState state = StormMageSpellManager.stateFor(this.level, caster);
            state.markConductive(target.getUUID(), this.level.getGameTime());
            StormMageSpellManager.awardHit(this.level, caster, state, this.voltageLedger, target);
            if (!this.recordedCast) {
               this.recordedCast = true;
               StormMageSpellManager.recordDamagingCast(this.level, caster, state, target, this.damage);
            }

            StormMageSpellManager.sparkBurst(this.level, target.getBoundingBox().getCenter(), 8);
            if (this.stage >= 4) {
               StormMageSpellManager.ACTIVE_ECHOES
                  .add(new StormMageSpellManager.EchoCast(this.level, caster, target, this.damage * (this.stage >= 6 ? 0.55F : 0.42F), 12));
            }

            if (this.overcharged && !this.forked) {
               this.forked = true;
               LivingEntity fork = StormMageSpellManager.nearestConductive(this.level, caster, state, target.position(), 8.0, this.hit);
               if (fork != null && MageCombatHelper.hurt(this.level, caster, fork, this.damage * 0.55F)) {
                  this.hit.add(fork.getUUID());
                  StormMageSpellManager.awardHit(this.level, caster, state, this.voltageLedger, fork);
                  StormMageSpellManager.particleLine(
                     this.level, target.getBoundingBox().getCenter(), fork.getBoundingBox().getCenter(), ParticleTypes.END_ROD, 9
                  );
               }
            }
         }
      }
   }

   private static final class SkybreakerCast implements StormMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final int stage;
      private final float damage;
      private final Vec3 center;
      private final UUID preferredTarget;
      private final boolean overcharged;
      private final StormMageSpellManager.HitLedger voltageLedger = new StormMageSpellManager.HitLedger();
      private int age;

      private SkybreakerCast(ServerLevel level, Entity caster, int stage, float damage, Vec3 center, UUID preferredTarget, boolean overcharged) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.stage = stage;
         this.damage = damage;
         this.center = center;
         this.preferredTarget = preferredTarget;
         this.overcharged = overcharged;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (!StormMageSpellManager.canMaintainActiveCast(caster, this.level)) {
            return true;
         }

         this.age++;
         if (this.age == 8 || this.age == 14) {
            StormMageSpellManager.columnParticles(this.level, this.center, 8.0, ParticleTypes.END_ROD, 10);
         }

         if (this.age == 16) {
            this.detonate(caster);
         }

         if (this.overcharged && (this.age == 24 || this.age == 32)) {
            this.echo(caster);
         }

         if (this.stage >= 6 && !this.overcharged && this.age == 26) {
            this.echo(caster);
         }

         return this.age >= (this.overcharged ? 33 : (this.stage >= 6 ? 27 : 17));
      }

      private void detonate(Entity caster) {
         ArrayList<LivingEntity> hit = new ArrayList<>();
         LivingEntity primary = null;
         if (this.preferredTarget != null
            && this.level.getEntity(this.preferredTarget) instanceof LivingEntity living
            && MageCombatHelper.isValidTarget(caster, living)
            && living.distanceToSqr(this.center) <= 16.0) {
            primary = living;
         }

         if (primary == null) {
            primary = this.level
               .getEntitiesOfClass(
                  LivingEntity.class, new AABB(this.center, this.center).inflate(2.75, 5.0, 2.75), targetx -> MageCombatHelper.isValidTarget(caster, targetx)
               )
               .stream()
               .min(Comparator.comparingDouble(targetx -> targetx.distanceToSqr(this.center)))
               .orElse(null);
         }

         if (primary != null && MageCombatHelper.hurt(this.level, caster, primary, this.damage)) {
            hit.add(primary);
         }

         StormMageSpellManager.StormState state = StormMageSpellManager.stateFor(this.level, caster);
         LivingEntity resolvedPrimary = primary;
         List<LivingEntity> forks = this.level
            .getEntitiesOfClass(
               LivingEntity.class,
               new AABB(this.center, this.center).inflate(6.0),
               targetx -> targetx != resolvedPrimary
                  && MageCombatHelper.isValidTarget(caster, targetx)
                  && state.isConductive(targetx.getUUID(), this.level.getGameTime())
            );
         forks.sort(Comparator.comparingDouble(targetx -> targetx.distanceToSqr(this.center)));
         float forkBudget = this.damage * 0.45F;

         for (LivingEntity target : forks) {
            if (hit.size() >= 4 || forkBudget <= 0.0F) {
               break;
            }

            float forkDamage = Math.min(this.damage * 0.18F, forkBudget);
            forkBudget -= forkDamage;
            if (MageCombatHelper.hurt(this.level, caster, target, forkDamage)) {
               hit.add(target);
               StormMageSpellManager.particleLine(
                  this.level, this.center.add(0.0, 1.0, 0.0), target.getBoundingBox().getCenter(), ParticleTypes.ELECTRIC_SPARK, 8
               );
            }
         }

         if (!hit.isEmpty()) {
            StormMageSpellManager.awardHits(this.level, caster, state, this.voltageLedger, hit);
            StormMageSpellManager.recordDamagingCast(this.level, caster, state, hit.get(0), this.damage);
         }

         StormMageSpellManager.columnParticles(this.level, this.center, 10.0, ParticleTypes.ELECTRIC_SPARK, 18);
         StormMageSpellManager.sparkBurst(this.level, this.center.add(0.0, 1.0, 0.0), 16);
         if (caster instanceof ServerPlayer player) {
            AbilityDestructionManager.impact(
               player,
               AbilityDestructionManager.Profile.STORM_SKYBREAKER,
               this.center,
               TemporaryStatBonusManager.effectiveIntelligence(player),
               this.overcharged || this.stage >= 5
            );
         }

         StormMageSpellManager.play(this.level, this.center, SoundEvents.TRIDENT_THUNDER, 1.25F, 0.85F);
      }

      private void echo(Entity caster) {
         float echoDamage = this.damage * (this.overcharged ? 0.18F : 0.16F);
         List<LivingEntity> targets = this.level
            .getEntitiesOfClass(
               LivingEntity.class, new AABB(this.center, this.center).inflate(2.75, 4.0, 2.75), target -> MageCombatHelper.isValidTarget(caster, target)
            );
         targets.sort(Comparator.comparingDouble(target -> target.distanceToSqr(this.center)));
         if (!targets.isEmpty()) {
            MageCombatHelper.hurt(this.level, caster, targets.get(0), StormMageSpellManager.pvpAdjusted(targets.get(0), echoDamage));
         }

         StormMageSpellManager.columnParticles(this.level, this.center, 7.0, ParticleTypes.END_ROD, 9);
         StormMageSpellManager.play(this.level, this.center, SoundEvents.AMETHYST_BLOCK_CHIME, 0.65F, 1.45F);
      }
   }

   private static final class SlipstreamCast implements StormMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final int stage;
      private final Vec3 direction;
      private final double step;
      private final Set<UUID> harvested = new HashSet<>();
      private int age;

      private SlipstreamCast(ServerLevel level, Entity caster, int stage, Vec3 direction, double distance) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.stage = stage;
         this.direction = direction;
         this.step = distance / 4.0;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster instanceof LivingEntity living && StormMageSpellManager.canMaintainActiveCast(caster, this.level)) {
            this.age++;
            if (!StormMageSpellManager.hasSafeDashStep(this.level, caster, this.direction, this.step)) {
               return true;
            }

            caster.setDeltaMovement(this.direction.scale(this.step));
            living.hurtMarked = true;
            living.fallDistance = 0.0F;
            Vec3 next = caster.position().add(this.direction.scale(this.step));
            StormMageSpellManager.particleLine(this.level, caster.position().add(0.0, 0.7, 0.0), next.add(0.0, 0.7, 0.0), ParticleTypes.ELECTRIC_SPARK, 5);
            this.harvestNearbyMark(caster);
            return this.age >= 4;
         } else {
            return true;
         }
      }

      private void harvestNearbyMark(Entity caster) {
         StormMageSpellManager.StormState state = StormMageSpellManager.stateFor(this.level, caster);
         int maximum = this.stage >= 6 ? 2 : 1;
         if (this.harvested.size() < maximum) {
            List<LivingEntity> nearby = this.level
               .getEntitiesOfClass(
                  LivingEntity.class,
                  caster.getBoundingBox().inflate(2.6),
                  targetx -> MageCombatHelper.isValidTarget(caster, targetx)
                     && !this.harvested.contains(targetx.getUUID())
                     && state.isConductive(targetx.getUUID(), this.level.getGameTime())
               );
            nearby.sort(Comparator.comparingDouble(caster::distanceToSqr));
            if (!nearby.isEmpty()) {
               LivingEntity target = nearby.get(0);
               this.harvested.add(target.getUUID());
               state.addVoltage(4.0 * (state.isTempestActive(this.level.getGameTime()) ? 1.5 : 1.0));
               state.lastCombatAt = this.level.getGameTime();
               StormMageSpellManager.particleLine(
                  this.level, target.getBoundingBox().getCenter(), caster.getBoundingBox().getCenter(), ParticleTypes.END_ROD, 6
               );
            }
         }
      }
   }

   private static final class StormState {
      private final ServerLevel level;
      private final UUID casterId;
      private final Map<UUID, Long> conductiveUntil = new HashMap<>();
      private Vec3 lastPosition;
      private double voltage;
      private double lastSyncedVoltage = -1.0;
      private long lastVoltageSyncAt;
      private int lastHudVoltage = -1;
      private int lastHudStage = -1;
      private boolean lastHudOvercharged;
      private boolean lastHudRod;
      private boolean lastHudTempest;
      private boolean lastHudSpiritualized;
      private long lastHudAt = -4611686018427387904L;
      private long lastCombatAt = -4611686018427387904L;
      private UUID rodTarget;
      private long rodExpiresAt;
      private StormMageSpellManager.ThunderheadState thunderhead;
      private long tempestExpiresAt;
      private int tempestDamagingCasts;
      private int tempestEchoes;
      private int tempestOvercharges;
      private boolean slipstreamBonusAvailable;

      private StormState(ServerLevel level, Entity caster) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.lastPosition = caster.position();
         this.voltage = Mth.clamp(caster.getPersistentData().getDouble("sl_storm_voltage"), 0.0, 100.0);
      }

      private void tick(Entity caster) {
         long now = this.level.getGameTime();
         this.conductiveUntil.entrySet().removeIf(entry -> entry.getValue() < now || this.level.getEntity(entry.getKey()) == null);
         Entity rod = this.rodTarget == null ? null : this.level.getEntity(this.rodTarget);
         if (this.rodExpiresAt < now || this.rodTarget != null && !(rod instanceof LivingEntity living && living.isAlive())) {
            this.clearRod(caster);
         }

         double moved = caster.position().subtract(this.lastPosition).multiply(1.0, 0.0, 1.0).length();
         this.lastPosition = caster.position();
         if (now - this.lastCombatAt <= 80L && moved > 0.015) {
            this.addVoltage(0.4 * (this.isTempestActive(now) ? 1.5 : 1.0));
         } else if (now - this.lastCombatAt > 80L && this.voltage > 0.0) {
            this.addVoltage(-0.3);
         }

         if (this.thunderhead != null && this.thunderhead.tick(caster)) {
            this.thunderhead = null;
         }

         if (this.tempestExpiresAt > 0L) {
            if (now >= this.tempestExpiresAt) {
               this.tempestExpiresAt = 0L;
               this.slipstreamBonusAvailable = false;
               if (caster instanceof LivingEntity living) {
                  StormMageSpellManager.removeTempestSpeed(living);
               }
            } else if (now % 5L == 0L) {
               StormMageSpellManager.ringParticles(this.level, caster.position().add(0.0, 0.9, 0.0), 0.75, 8);
            }
         }

         if (!CooldownManager.isOnCooldown(caster, "Slipstream") && !this.isTempestActive(now)) {
            this.slipstreamBonusAvailable = false;
         }

         this.syncVoltage(caster, false);
      }

      private void addVoltage(double amount) {
         this.voltage = Mth.clamp(this.voltage + amount, 0.0, 100.0);
      }

      private void markConductive(UUID targetId, long now) {
         this.conductiveUntil.put(targetId, now + 120L);
      }

      private boolean isConductive(UUID targetId, long now) {
         Long expires = this.conductiveUntil.get(targetId);
         if (expires == null) {
            return false;
         } else if (expires < now) {
            this.conductiveUntil.remove(targetId);
            return false;
         } else {
            return true;
         }
      }

      private boolean isTempestActive(long now) {
         return this.tempestExpiresAt > now;
      }

      private boolean canSpendSlipstreamBonus(long now) {
         return this.slipstreamBonusAvailable
            && (this.isTempestActive(now) || StormMageSpellManager.effectiveOutputStage(this.level.getEntity(this.casterId)) >= 4);
      }

      private void clearRod(Entity caster) {
         if (this.rodTarget != null && caster instanceof ServerPlayer player) {
            EntityHighlightSystem.hide(player, this.rodTarget, this.level.dimension(), "storm:lightning_rod");
         }

         this.rodTarget = null;
         this.rodExpiresAt = 0L;
      }

      private void forceVoltageSync() {
         this.lastSyncedVoltage = -1.0;
         this.lastVoltageSyncAt = 0L;
      }

      private void syncVoltage(Entity caster, boolean force) {
         long now = this.level.getGameTime();
         boolean thresholdChanged = this.voltage >= 100.0 != this.lastSyncedVoltage >= 100.0;
         if (force || thresholdChanged || !(Math.abs(this.voltage - this.lastSyncedVoltage) < 2.0) || now - this.lastVoltageSyncAt >= 4L) {
            caster.getPersistentData().putDouble("sl_storm_voltage", this.voltage);
            this.lastSyncedVoltage = this.voltage;
            this.lastVoltageSyncAt = now;
         }
      }

      private void syncHud(ServerPlayer player, boolean force) {
         long now = this.level.getGameTime();
         int hudVoltage = Mth.clamp((int)Math.round(this.voltage), 0, 100);
         int hudStage = StormMageSpellManager.effectiveOutputStage(player);
         boolean hudOvercharged = this.voltage + 1.0E-4 >= 100.0;
         boolean hudRod = this.rodTarget != null && this.rodExpiresAt >= now;
         boolean hudTempest = this.isTempestActive(now);
         boolean hudSpiritualized = WhiteFlameMonarchManager.isSpiritualized(player) && hudStage > StormMageSpellManager.outputStage(player);
         boolean changed = Math.abs(hudVoltage - this.lastHudVoltage) >= 2
            || hudOvercharged != this.lastHudOvercharged
            || hudStage != this.lastHudStage
            || hudRod != this.lastHudRod
            || hudTempest != this.lastHudTempest
            || hudSpiritualized != this.lastHudSpiritualized;
         if (force || changed && now - this.lastHudAt >= 4L) {
            SololevelingMod.PACKET_HANDLER
               .send(
                  PacketDistributor.PLAYER.with(() -> player),
                  new StormStateMessage(true, hudVoltage, hudStage, hudOvercharged, hudRod, hudTempest, hudSpiritualized)
               );
            this.lastHudVoltage = hudVoltage;
            this.lastHudStage = hudStage;
            this.lastHudOvercharged = hudOvercharged;
            this.lastHudRod = hudRod;
            this.lastHudTempest = hudTempest;
            this.lastHudSpiritualized = hudSpiritualized;
            this.lastHudAt = now;
         }
      }

      private void deactivate(Entity caster) {
         if (caster instanceof LivingEntity living) {
            StormMageSpellManager.removeTempestSpeed(living);
         }

         if (caster != null) {
            caster.getPersistentData().remove("sl_storm_voltage");
         }

         this.conductiveUntil.clear();
         this.clearRod(caster);
         this.thunderhead = null;
         this.tempestExpiresAt = 0L;
      }
   }

   private static final class ThunderheadState {
      private static final int[] FIVE_STRIKES = new int[]{10, 42, 74, 106, 138};
      private static final int[] SIX_STRIKES = new int[]{8, 34, 60, 86, 112, 138};
      private final ServerLevel level;
      private final UUID casterId;
      private final int stage;
      private final Vec3 center;
      private final float damage;
      private final boolean overcharged;
      private final StormMageSpellManager.HitLedger voltageLedger = new StormMageSpellManager.HitLedger();
      private UUID previousTarget;
      private boolean recordedCast;
      private int age;
      private int strikeIndex;

      private ThunderheadState(ServerLevel level, Entity caster, int stage, Vec3 center, float damage, boolean overcharged) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.stage = stage;
         this.center = center;
         this.damage = damage;
         this.overcharged = overcharged;
      }

      private boolean tick(Entity caster) {
         this.age++;
         if (this.age % 20 == 0) {
            StormMageSpellManager.cloudParticles(this.level, this.center.add(0.0, 6.0, 0.0), 8);
         }

         int[] schedule = this.stage >= 4 ? SIX_STRIKES : FIVE_STRIKES;
         if (this.strikeIndex < schedule.length && this.age >= schedule[this.strikeIndex]) {
            boolean finalStrike = this.strikeIndex == schedule.length - 1;
            this.strike(caster, finalStrike);
            this.strikeIndex++;
         }

         return this.age > schedule[schedule.length - 1] + 4;
      }

      private void strike(Entity caster, boolean finalStrike) {
         StormMageSpellManager.StormState state = StormMageSpellManager.stateFor(this.level, caster);
         List<LivingEntity> candidates = this.level
            .getEntitiesOfClass(
               LivingEntity.class,
               new AABB(this.center, this.center).inflate(5.5, 7.0, 5.5),
               targetx -> MageCombatHelper.isValidTarget(caster, targetx) && StormMageSpellManager.visible(this.level, this.center.add(0.0, 6.0, 0.0), targetx)
            );
         candidates.sort(
            Comparator.<LivingEntity>comparingInt(targetx -> targetx.getUUID().equals(this.previousTarget) ? 1 : 0)
               .thenComparingInt(targetx -> state.isConductive(targetx.getUUID(), this.level.getGameTime()) ? 0 : 1)
               .thenComparingDouble(targetx -> targetx.distanceToSqr(this.center))
         );
         int targetCount = finalStrike && this.stage >= 6 ? Math.min(2, candidates.size()) : Math.min(1, candidates.size());
         ArrayList<LivingEntity> hit = new ArrayList<>();

         for (int index = 0; index < targetCount; index++) {
            LivingEntity target = candidates.get(index);
            float strikeDamage = index == 0 ? this.damage : this.damage * 0.6F;
            if (MageCombatHelper.hurt(this.level, caster, target, strikeDamage)) {
               hit.add(target);
               this.previousTarget = target.getUUID();
               StormMageSpellManager.columnParticles(this.level, target.position(), 7.5, ParticleTypes.ELECTRIC_SPARK, 12);
               StormMageSpellManager.sparkBurst(this.level, target.getBoundingBox().getCenter(), 10);
            }
         }

         if (!hit.isEmpty()) {
            StormMageSpellManager.awardHits(this.level, caster, state, this.voltageLedger, hit);
            if (!this.recordedCast) {
               this.recordedCast = true;
               StormMageSpellManager.recordDamagingCast(this.level, caster, state, hit.get(0), this.damage);
            }
         } else {
            StormMageSpellManager.columnParticles(this.level, this.center, 7.0, ParticleTypes.END_ROD, 8);
         }

         if (finalStrike && this.overcharged) {
            this.finalBurst(caster, state, candidates);
         }

         if (finalStrike && caster instanceof ServerPlayer player) {
            AbilityDestructionManager.impact(
               player,
               AbilityDestructionManager.Profile.STORM_THUNDERCLAP,
               hit.isEmpty() ? this.center : hit.get(0).position(),
               TemporaryStatBonusManager.effectiveIntelligence(player),
               this.overcharged || this.stage >= 5
            );
         }

         StormMageSpellManager.play(this.level, this.center, SoundEvents.TRIDENT_THUNDER, 0.7F, 1.45F);
      }

      private void finalBurst(Entity caster, StormMageSpellManager.StormState state, List<LivingEntity> strikeCandidates) {
         List<LivingEntity> targets = strikeCandidates.stream()
            .filter(targetx -> targetx.distanceToSqr(this.center) <= 18.0625)
            .sorted(Comparator.comparingDouble(targetx -> targetx.distanceToSqr(this.center)))
            .toList();
         ArrayList<LivingEntity> hit = new ArrayList<>();

         for (LivingEntity target : targets) {
            if (hit.size() >= 8) {
               break;
            }

            if (MageCombatHelper.hurt(this.level, caster, target, this.damage * 0.55F)) {
               hit.add(target);
            }
         }

         StormMageSpellManager.awardHits(this.level, caster, state, this.voltageLedger, hit);
         StormMageSpellManager.ringParticles(this.level, this.center.add(0.0, 0.4, 0.0), 4.25, 24);
      }
   }
}
