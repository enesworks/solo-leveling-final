package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.ArcaneVfxEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class ArcaneMageSpellManager {
   public static final String AETHER_BOLT = "Aether Bolt";
   public static final String VECTOR_STEP = "Vector Step";
   public static final String POLARITY_SPHERE = "Polarity Sphere";
   public static final String RUNIC_RELAY = "Runic Relay";
   public static final String ASTRAL_ARSENAL = "Astral Arsenal";
   public static final String DIMENSIONAL_REND = "Dimensional Rend";
   public static final String CONVERGENCE = "Grand Formula: Convergence";
   public static final int NORMAL_PRIMARY = 9067775;
   public static final int NORMAL_SECONDARY = 4712191;
   public static final int ORB_PRIMARY = 2118911;
   public static final int ORB_SECONDARY = 13836117;
   public static final Set<String> ARCANE_SKILLS = Set.of(
      "Aether Bolt", "Vector Step", "Polarity Sphere", "Runic Relay", "Astral Arsenal", "Dimensional Rend", "Grand Formula: Convergence"
   );
   public static final Set<String> QTE_SKILLS = Set.of("Astral Arsenal", "Dimensional Rend", "Grand Formula: Convergence");
   public static final Set<String> INSTANT_SKILLS = Set.of("Aether Bolt", "Vector Step", "Polarity Sphere", "Runic Relay");
   private static final double[] COST_MULTIPLIER = new double[]{0.0, 1.0, 1.1, 1.2, 1.3, 1.4};
   private static final int[] FORMULA_DURATION = new int[]{0, 200, 230, 260, 290, 320};
   private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss"));
   private static final Map<UUID, ArcaneMageSpellManager.FormulaState> FORMULAS = new HashMap<>();
   private static final Map<UUID, ArcaneMageSpellManager.AnchorState> ANCHORS = new HashMap<>();
   private static final Map<UUID, ArcaneMageSpellManager.RelayState> RELAYS = new HashMap<>();
   private static final Map<UUID, ArcaneMageSpellManager.ArsenalState> ARSENALS = new HashMap<>();
   private static final List<ArcaneMageSpellManager.BoltCast> ACTIVE_BOLTS = new ArrayList<>();
   private static final List<ArcaneMageSpellManager.PolarityCast> ACTIVE_POLARITIES = new ArrayList<>();
   private static final List<ArcaneMageSpellManager.RendCast> ACTIVE_RENDS = new ArrayList<>();
   private static final List<ArcaneMageSpellManager.ConvergenceCast> ACTIVE_CONVERGENCES = new ArrayList<>();
   private static final List<ArcaneMageSpellManager.DelayedBurst> DELAYED_BURSTS = new ArrayList<>();

   private ArcaneMageSpellManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null && player.getServer() != null) {
         UUID playerId = player.getUUID();
         FORMULAS.remove(playerId);
         ANCHORS.remove(playerId);
         RELAYS.remove(playerId);
         ARSENALS.remove(playerId);
         ACTIVE_BOLTS.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_POLARITIES.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_RENDS.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_CONVERGENCES.removeIf(cast -> playerId.equals(cast.casterId));
         DELAYED_BURSTS.removeIf(cast -> playerId.equals(cast.casterId));
         ArrayList<ArcaneVfxEntity> ownedEffects = new ArrayList<>();

         for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
               if (entity instanceof ArcaneVfxEntity effect && effect.getOwnerId().filter(playerId::equals).isPresent()) {
                  ownedEffects.add(effect);
               }
            }
         }

         ownedEffects.forEach(Entity::discard);
      }
   }

   public static boolean isArcaneSkill(String skill) {
      return ARCANE_SKILLS.contains(skill);
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

   public static String stageName(int stage) {
      return switch (Mth.clamp(stage, 0, 5)) {
         case 1 -> "Trace";
         case 2 -> "Inscribed";
         case 3 -> "Runic Array";
         case 4 -> "Grand Formula";
         case 5 -> "Transcendent";
         default -> "Dormant";
      };
   }

   public static List<Component> tooltip(Entity caster, String skill) {
      int stage = outputStage(caster);
      List<Component> lines = new ArrayList<>();
      lines.add(Component.literal(stageName(stage) + " Output - Stage " + stage).withStyle(style -> style.withColor(9067775).withBold(true)));
      lines.add(Component.literal(description(skill)).withStyle(ChatFormatting.GRAY));
      lines.add(Component.literal(stageEffect(skill, stage)).withStyle(style -> style.withColor(4712191)));
      lines.add(Component.literal("Arcane Formula: alternate spells to assemble 3 runes.").withStyle(ChatFormatting.DARK_PURPLE));
      lines.add(
         Component.literal(
               "Mana: " + manaCost(caster, skill, stage, QTEResult.MISS) + "  |  Cooldown: " + String.format("%.1fs", cooldownTicks(skill, stage) / 20.0)
            )
            .withStyle(ChatFormatting.DARK_GRAY)
      );
      if (OrbOfAvariceManager.isHeldBy(caster)) {
         lines.add(Component.literal("Orb: unstable cobalt-crimson output, x2 magic, +50% mana.").withStyle(ChatFormatting.BLUE));
      }

      return lines;
   }

   private static String description(String skill) {
      return switch (skill) {
         case "Aether Bolt" -> "Launch condensed raw mana with precision, correction, and ricochet growth.";
         case "Vector Step" -> "Rewrite your movement vector in a safe, collision-aware phase step.";
         case "Polarity Sphere" -> "Create a controllable gravity node; sneak to reverse its polarity.";
         case "Runic Relay" -> "Link two runes so later Arcane attacks emerge from the distant glyph.";
         case "Astral Arsenal" -> "Form orbiting mana blades that acquire targets or launch together on recast.";
         case "Dimensional Rend" -> "Cut space with a long plane; sneak for a shorter, wider rupture.";
         case "Grand Formula: Convergence" -> "Assemble a battlefield formula that gathers, strikes, and collapses its center.";
         default -> "Shape raw magical law into a combat formula.";
      };
   }

   private static String stageEffect(String skill, int stage) {
      return switch (skill) {
         case "Aether Bolt" -> {
            switch (stage) {
               case 1:
                  yield "A fast single trace.";
               case 2:
                  yield "Corrects toward nearby targets.";
               case 3:
                  yield "Pierces and ricochets once.";
               case 4:
                  yield "An inscribed triad follows the leading bolt.";
               default:
                  yield "A constellation line corrects through several targets.";
            }
         }
         case "Vector Step" -> stage < 2
            ? "A direct safe phase step."
            : (stage < 4 ? "Leaves a recall anchor; sneak-cast to return." : "Curves toward aimed targets and leaves damaging afterimages.");
         case "Polarity Sphere" -> stage < 3
            ? "Pulls or repels bodies and loose projectiles."
            : (stage < 5 ? "Suspends targets between repeated pressure pulses." : "Transcendent polarity can reverse an entire formation.");
         case "Runic Relay" -> stage < 3
            ? "Routes bolts and cuts through one exit glyph."
            : (stage < 5 ? "Routes the full Arcane set with a widened gate." : "A constellation relay branches attacks across three exits.");
         case "Astral Arsenal" -> "Maintains " + (3 + stage) + " orbiting blades with smarter distribution at higher stages.";
         case "Dimensional Rend" -> stage < 3
            ? "A clean spatial plane."
            : (stage < 5 ? "Adds crossing cuts and a delayed spatial scar." : "The scar persists through a widened transcendent rupture.");
         case "Grand Formula: Convergence" -> stage < 3
            ? "Gather, vertex strikes, then zero-point collapse."
            : (stage < 5 ? "Adds projectile curvature and chained vertex strikes." : "A sky formula crushes the battlefield against the ground array.");
         default -> "";
      };
   }

   public static boolean cast(Entity caster, String skill, QTEResult qteResult) {
      if (!(caster.level() instanceof ServerLevel level && isArcaneSkill(skill))) {
         return false;
      } else if ("Astral Arsenal".equals(skill) && releaseArsenal(level, caster)) {
         return true;
      } else if ("Vector Step".equals(skill) && caster.isShiftKeyDown() && recallAnchor(level, caster)) {
         return true;
      } else if (CooldownManager.isOnCooldown(caster, skill)) {
         message(caster, "Ability on cooldown!");
         return false;
      } else {
         int stage = outputStage(caster);
         QTEResult result = qteResult == null ? QTEResult.MISS : qteResult;
         boolean overcast = formulaReady(level, caster);
         int cost = manaCost(caster, skill, stage, result);
         SololevelingModVariables.PlayerVariables data = variables(caster);
         if (!(caster instanceof Player player && player.isCreative()) && data.MP < cost) {
            message(caster, "Not enough MP! Need " + cost + ".");
            return false;
         } else {
            if (cost > 0) {
               deductMana(caster, cost);
            }

            CooldownManager.set(caster, skill, cooldownTicks(skill, stage));
            CooldownManager.set(caster, "mana_refresh", 40);
            double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));

            boolean started = switch (skill) {
               case "Aether Bolt" -> startAether(level, caster, stage, (float)(1.5 + intelligence * 0.032), overcast);
               case "Vector Step" -> startVector(level, caster, stage, (float)(1.0 + intelligence * 0.015), overcast);
               case "Polarity Sphere" -> startPolarity(level, caster, stage, (float)(2.5 + intelligence * 0.04), overcast);
               case "Runic Relay" -> startRelay(level, caster, stage, overcast);
               case "Astral Arsenal" -> startArsenal(level, caster, stage, (float)(6.0 + intelligence * 0.065), overcast);
               case "Dimensional Rend" -> startRend(level, caster, stage, (float)(7.0 + intelligence * 0.075), overcast);
               case "Grand Formula: Convergence" -> startConvergence(level, caster, stage, (float)(14.0 + intelligence * 0.1), overcast);
               default -> false;
            };
            if (started) {
               recordFormula(level, caster, skill, stage, result, overcast);
            }

            return started;
         }
      }
   }

   public static boolean castNpc(Entity caster, String skill) {
      if (caster.level() instanceof ServerLevel level && isArcaneSkill(skill) && !CooldownManager.isOnCooldown(caster, skill)) {
         Entity target = caster instanceof Mob mob ? mob.getTarget() : null;
         if (target == null && !"Astral Arsenal".equals(skill)) {
            return false;
         }

         int stage = outputStage(caster);
         double intelligence = Math.max(10.0, MageCombatHelper.intelligence(caster));
         boolean overcast = formulaReady(level, caster);

         boolean started = switch (skill) {
            case "Aether Bolt" -> startAether(level, caster, stage, (float)(1.5 + intelligence * 0.032), overcast);
            case "Vector Step" -> startVector(level, caster, stage, (float)(1.0 + intelligence * 0.015), overcast);
            case "Polarity Sphere" -> startPolarity(level, caster, stage, (float)(2.5 + intelligence * 0.04), overcast);
            case "Runic Relay" -> startRelay(level, caster, stage, overcast);
            case "Astral Arsenal" -> startArsenal(level, caster, stage, (float)(6.0 + intelligence * 0.065), overcast);
            case "Dimensional Rend" -> startRend(level, caster, stage, (float)(7.0 + intelligence * 0.075), overcast);
            case "Grand Formula: Convergence" -> startConvergence(level, caster, stage, (float)(14.0 + intelligence * 0.1), overcast);
            default -> false;
         };
         if (started) {
            CooldownManager.set(caster, skill, cooldownTicks(skill, stage));
            recordFormula(level, caster, skill, stage, QTEResult.GOOD, overcast);
         }

         return started;
      } else {
         return false;
      }
   }

   public static int manaCost(Entity caster, String skill, int stage, QTEResult result) {
      if (caster instanceof Player player && player.isCreative()) {
         return 0;
      } else {
         double basePercent = switch (skill) {
            case "Aether Bolt" -> 0.0035;
            case "Vector Step" -> 0.02;
            case "Polarity Sphere" -> 0.04;
            case "Runic Relay" -> 0.045;
            case "Astral Arsenal" -> 0.08;
            case "Dimensional Rend" -> 0.1;
            case "Grand Formula: Convergence" -> 0.18;
            default -> 0.0;
         };
         double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
         double maximumMana = 1000.0 + intelligence * 100.0;
         double qte = isQteSkill(skill) ? MageQTEHelper.getManaCostMultiplier(result == null ? QTEResult.MISS : result, intelligence) : 1.0;
         return Math.max(0, OrbOfAvariceManager.adjustManaCost(caster, maximumMana * basePercent * COST_MULTIPLIER[Mth.clamp(stage, 1, 5)] * qte));
      }
   }

   private static int cooldownTicks(String skill, int stage) {
      return switch (skill) {
         case "Aether Bolt" -> 9;
         case "Vector Step" -> stage >= 5 ? 40 : 80;
         case "Polarity Sphere" -> 160;
         case "Runic Relay" -> 200;
         case "Astral Arsenal" -> 280;
         case "Dimensional Rend" -> 360;
         case "Grand Formula: Convergence" -> 1200;
         default -> 20;
      };
   }

   private static boolean startAether(ServerLevel level, Entity caster, int stage, float damage, boolean overcast) {
      Vec3 direction = safeDirection(caster.getLookAngle());
      Vec3 origin = routedOrigin(level, caster, direction);
      int count = overcast ? 3 : (stage >= 4 ? 3 : 1);
      float splitDamage = damage * (count == 1 ? 1.0F : (overcast ? 0.42F : 0.38F));

      for (int i = 0; i < count; i++) {
         float offset = count == 1 ? 0.0F : (i - 1) * (overcast ? 12.0F : 5.0F);
         Vec3 shotDirection = rotateYaw(direction, offset);
         spawnBolt(
            level,
            caster,
            origin,
            shotDirection,
            stage,
            splitDamage,
            overcast,
            i * (stage >= 4 && !overcast ? 3 : 1),
            0,
            0.3 + stage * 0.045,
            2.45 + stage * 0.18,
            24.0 + stage * 5.0,
            stage >= 5 ? 3 : (stage >= 3 ? 2 : 1)
         );
      }

      ArcaneMageSpellManager.RelayState relay = activeRelay(level, caster);
      if (relay != null && relay.branches > 1) {
         for (int branch = 1; branch < relay.branches; branch++) {
            Vec3 branchDirection = rotateYaw(direction, branch == 1 ? -18.0F : 18.0F);
            spawnBolt(
               level,
               caster,
               relay.exit.add(branchDirection.scale(0.45)),
               branchDirection,
               stage,
               damage * 0.28F,
               false,
               branch * 2,
               0,
               0.28 + stage * 0.04,
               2.4 + stage * 0.16,
               20.0 + stage * 4.0,
               stage >= 3 ? 2 : 1
            );
         }
      }

      play(level, origin, SoundEvents.AMETHYST_BLOCK_CHIME, 0.72F, overcast ? 1.85F : 1.55F);
      return true;
   }

   private static void spawnBolt(
      ServerLevel level,
      Entity caster,
      Vec3 origin,
      Vec3 direction,
      int stage,
      float damage,
      boolean overcast,
      int delay,
      int style,
      double radius,
      double speed,
      double range,
      int maxHits
   ) {
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      ArcaneVfxEntity effect = spawn(
         level,
         origin,
         style,
         stage,
         (float)(radius * (overcast ? 1.35 : 1.0)),
         (float)(2.2 + stage * 0.75),
         (int)Math.ceil(range / speed) + delay + 12,
         yawFor(direction),
         pitchFor(direction),
         caster,
         null,
         orb,
         overcast
      );
      ACTIVE_BOLTS.add(
         new ArcaneMageSpellManager.BoltCast(
            level,
            caster.getUUID(),
            effect.getUUID(),
            stage,
            damage,
            origin,
            safeDirection(direction),
            range,
            speed,
            radius,
            maxHits,
            delay,
            orb,
            style,
            overcast
         )
      );
   }

   private static boolean startVector(ServerLevel level, Entity caster, int stage, float damage, boolean overcast) {
      Vec3 origin = caster.position();
      Vec3 direction = safeDirection(caster.getLookAngle());
      LivingEntity aimed = stage >= 3 ? findLookTarget(level, caster, 13.0 + stage * 2.0) : null;
      if (aimed != null) {
         direction = safeDirection(aimed.getBoundingBox().getCenter().subtract(caster.getEyePosition()));
      }

      double distance = 5.0 + stage * 1.45;
      Vec3 destination = safeTeleportPoint(level, caster, direction, distance);
      if (destination.distanceToSqr(origin) < 0.36) {
         message(caster, "No safe vector can be formed.");
         return false;
      }

      if (stage >= 2) {
         setAnchor(level, caster, origin, stage);
      }

      caster.teleportTo(destination.x, destination.y, destination.z);
      Vec3 delta = destination.subtract(origin);
      Vec3 center = origin.add(destination).scale(0.5).add(0.0, caster.getBbHeight() * 0.45, 0.0);
      spawn(
         level,
         center,
         2,
         stage,
         0.6F + stage * 0.15F,
         (float)delta.length(),
         18,
         yawFor(delta),
         pitchFor(delta),
         caster,
         aimed,
         OrbOfAvariceManager.isHeldBy(caster),
         overcast
      );
      AABB route = new AABB(origin, destination).inflate(1.0 + stage * 0.16);

      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, route, candidate -> MageCombatHelper.isValidTarget(caster, candidate))) {
         MageCombatHelper.hurt(level, caster, target, damage);
         Vec3 push = safeDirection(target.position().subtract(origin));
         target.setDeltaMovement(target.getDeltaMovement().add(push.scale(0.18 * controlFactor(caster, target))));
      }

      if (overcast) {
         damageArea(level, caster, origin.add(0.0, 0.8, 0.0), 2.8, damage * 0.65F, 0.14);
         damageArea(level, caster, destination.add(0.0, 0.8, 0.0), 3.2, damage * 0.85F, 0.22);
         spawn(level, origin.add(0.0, 0.8, 0.0), 1, stage, 2.4F, 1.0F, 18, 0.0F, 0.0F, caster, null, OrbOfAvariceManager.isHeldBy(caster), true);
         spawn(level, destination.add(0.0, 0.8, 0.0), 1, stage, 2.8F, 1.0F, 18, 0.0F, 0.0F, caster, null, OrbOfAvariceManager.isHeldBy(caster), true);
      }

      play(level, center, SoundEvents.ENDERMAN_TELEPORT, 0.72F, 1.55F);
      return true;
   }

   private static boolean startPolarity(ServerLevel level, Entity caster, int stage, float damage, boolean overcast) {
      Vec3 center = aimPoint(level, caster, 12.0 + stage * 3.0);
      float radius = 3.8F + stage * 0.9F + (overcast ? 1.8F : 0.0F);
      int duration = 34 + stage * 7 + (overcast ? 12 : 0);
      boolean repel = caster.isShiftKeyDown();
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      ArcaneVfxEntity effect = spawn(level, center, 4, stage, radius, radius, duration + 8, caster.getYRot(), 0.0F, caster, null, orb, overcast);
      ACTIVE_POLARITIES.add(
         new ArcaneMageSpellManager.PolarityCast(level, caster.getUUID(), effect.getUUID(), stage, center, radius, duration, damage, repel, overcast)
      );
      play(level, center, SoundEvents.RESPAWN_ANCHOR_CHARGE, 0.9F, repel ? 0.62F : 1.15F);
      return true;
   }

   private static boolean startRelay(ServerLevel level, Entity caster, int stage, boolean overcast) {
      ArcaneMageSpellManager.RelayState previous = RELAYS.remove(caster.getUUID());
      if (previous != null) {
         previous.discardEffects();
      }

      Vec3 direction = safeDirection(caster.getLookAngle());
      Vec3 entry = caster.getEyePosition().add(direction.scale(1.45));
      Vec3 exit = aimPoint(level, caster, 18.0 + stage * 4.0);
      if (exit.distanceToSqr(entry) < 4.0) {
         exit = entry.add(direction.scale(4.0));
      }

      int duration = 120 + stage * 30;
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      List<UUID> effects = new ArrayList<>();
      effects.add(
         spawn(level, entry, 5, stage, 1.0F + stage * 0.16F, 1.0F, duration + 8, yawFor(direction), pitchFor(direction), caster, null, orb, overcast).getUUID()
      );
      effects.add(
         spawn(level, exit, 5, stage, 1.25F + stage * 0.2F, 1.0F, duration + 8, yawFor(direction), pitchFor(direction), caster, null, orb, overcast).getUUID()
      );
      Vec3 link = exit.subtract(entry);
      effects.add(
         spawn(level, entry, 6, stage, 0.3F + stage * 0.05F, (float)link.length(), duration + 8, yawFor(link), pitchFor(link), caster, null, orb, overcast)
            .getUUID()
      );
      int branches = overcast ? 3 : (stage >= 5 ? 2 : 1);
      if (branches > 1) {
         for (int branch = 1; branch < branches; branch++) {
            Vec3 branchPos = exit.add(rotateYaw(direction, branch == 1 ? -28.0F : 28.0F).scale(2.2 + stage * 0.25));
            effects.add(
               spawn(level, branchPos, 5, stage, 0.9F + stage * 0.13F, 1.0F, duration + 8, yawFor(direction), pitchFor(direction), caster, null, orb, true)
                  .getUUID()
            );
         }
      }

      RELAYS.put(
         caster.getUUID(), new ArcaneMageSpellManager.RelayState(level, caster.getUUID(), entry, exit, level.getGameTime() + duration, branches, effects)
      );
      play(level, exit, SoundEvents.PORTAL_TRIGGER, 0.65F, 1.55F);
      return true;
   }

   private static boolean startArsenal(ServerLevel level, Entity caster, int stage, float damage, boolean overcast) {
      ArcaneMageSpellManager.ArsenalState previous = ARSENALS.remove(caster.getUUID());
      if (previous != null) {
         previous.discardEffect();
      }

      int blades = 3 + stage + (overcast ? 3 : 0);
      int duration = 180 + stage * 20;
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      ArcaneVfxEntity effect = spawn(
         level,
         caster.position().add(0.0, caster.getBbHeight() * 0.52, 0.0),
         7,
         stage,
         blades,
         1.0F,
         duration + 10,
         caster.getYRot(),
         0.0F,
         caster,
         null,
         orb,
         overcast
      );
      ARSENALS.put(
         caster.getUUID(),
         new ArcaneMageSpellManager.ArsenalState(
            level, caster.getUUID(), effect.getUUID(), stage, blades, damage, level.getGameTime() + duration, orb, overcast
         )
      );
      play(level, caster.position(), SoundEvents.ENCHANTMENT_TABLE_USE, 0.8F, 1.35F);
      return true;
   }

   private static boolean releaseArsenal(ServerLevel level, Entity caster) {
      ArcaneMageSpellManager.ArsenalState arsenal = ARSENALS.get(caster.getUUID());
      if (arsenal != null && arsenal.level == level && arsenal.remaining > 0) {
         arsenal.releaseAll(caster);
         ARSENALS.remove(caster.getUUID());
         return true;
      } else {
         return false;
      }
   }

   private static boolean startRend(ServerLevel level, Entity caster, int stage, float damage, boolean overcast) {
      Vec3 direction = safeDirection(caster.getLookAngle());
      Vec3 origin = routedOrigin(level, caster, direction);
      boolean wide = caster.isShiftKeyDown();
      float width = wide ? 3.0F + stage * 0.65F : 1.0F + stage * 0.38F;
      float length = wide ? 7.0F + stage : 10.0F + stage * 1.5F;
      double range = wide ? 15.0 + stage * 3.0 : 24.0 + stage * 6.0;
      double speed = wide ? 2.8 + stage * 0.16 : 3.8 + stage * 0.2;
      spawnRend(level, caster, origin, direction, stage, width, length, range, speed, damage, overcast, 0);
      if (overcast || stage >= 4) {
         float angle = overcast ? 13.0F : 7.0F;
         spawnRend(
            level,
            caster,
            origin,
            rotateYaw(direction, -angle),
            stage,
            width * 0.76F,
            length * 0.92F,
            range * 0.92,
            speed,
            damage * (overcast ? 0.58F : 0.34F),
            overcast,
            overcast ? 2 : 5
         );
         if (overcast) {
            spawnRend(level, caster, origin, rotateYaw(direction, angle), stage, width * 0.76F, length * 0.92F, range * 0.92, speed, damage * 0.58F, true, 4);
         }
      }

      play(level, origin, SoundEvents.TRIDENT_THROW, 1.0F, overcast ? 0.72F : 1.25F);
      return true;
   }

   private static void spawnRend(
      ServerLevel level,
      Entity caster,
      Vec3 origin,
      Vec3 direction,
      int stage,
      float width,
      float length,
      double range,
      double speed,
      float damage,
      boolean overcast,
      int delay
   ) {
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      ArcaneVfxEntity effect = spawn(
         level,
         origin,
         9,
         stage,
         width,
         length,
         (int)Math.ceil(range / speed) + delay + 14,
         yawFor(direction),
         pitchFor(direction),
         caster,
         null,
         orb,
         overcast
      );
      ACTIVE_RENDS.add(
         new ArcaneMageSpellManager.RendCast(
            level, caster.getUUID(), effect.getUUID(), stage, origin, safeDirection(direction), range, speed, width, damage, delay, orb, overcast
         )
      );
      if (delay == 0 && caster instanceof ServerPlayer player) {
         AbilityDestructionManager.line(
            player,
            AbilityDestructionManager.Profile.ARCANE_IMPACT,
            origin,
            origin.add(safeDirection(direction).scale(Math.min(24.0, range))),
            TemporaryStatBonusManager.effectiveIntelligence(player),
            overcast || stage >= 5
         );
      }
   }

   private static boolean startConvergence(ServerLevel level, Entity caster, int stage, float damage, boolean overcast) {
      Vec3 center = caster.isShiftKeyDown() ? groundBelow(level, caster.position()) : groundPoint(level, aimPoint(level, caster, 18.0 + stage * 4.0));
      float radius = 6.0F + stage * 1.8F + (overcast ? 2.8F : 0.0F);
      int duration = 52 + stage * 6 + (overcast ? 12 : 0);
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      ArcaneVfxEntity ground = spawn(level, center, 11, stage, radius, radius, duration + 12, caster.getYRot(), 0.0F, caster, null, orb, overcast);
      UUID skyId = null;
      if (stage >= 5 || overcast) {
         Vec3 sky = center.add(0.0, 8.0 + stage * 1.6, 0.0);
         skyId = spawn(level, sky, 12, stage, radius * 0.9F, radius, duration + 12, caster.getYRot(), 180.0F, caster, null, orb, overcast).getUUID();
         spawn(level, center, 13, stage, radius * 0.7F, (float)(sky.y - center.y), duration + 12, 0.0F, -90.0F, caster, null, orb, overcast);
      }

      ACTIVE_CONVERGENCES.add(
         new ArcaneMageSpellManager.ConvergenceCast(level, caster.getUUID(), ground.getUUID(), skyId, stage, center, radius, duration, damage, orb, overcast)
      );
      play(level, center, SoundEvents.BEACON_ACTIVATE, 1.2F, 0.65F);
      return true;
   }

   private static boolean formulaReady(ServerLevel level, Entity caster) {
      ArcaneMageSpellManager.FormulaState state = FORMULAS.get(caster.getUUID());
      if (state != null && state.level == level && state.expiresAt >= level.getGameTime()) {
         return state.runes >= 3;
      }

      clearFormula(caster.getUUID());
      return false;
   }

   private static void recordFormula(ServerLevel level, Entity caster, String skill, int stage, QTEResult result, boolean consumedOvercast) {
      UUID casterId = caster.getUUID();
      if (consumedOvercast) {
         clearFormula(casterId);
      } else {
         ArcaneMageSpellManager.FormulaState state = FORMULAS.computeIfAbsent(casterId, id -> new ArcaneMageSpellManager.FormulaState(level, id));
         if (state.level != level) {
            clearFormula(casterId);
            state = new ArcaneMageSpellManager.FormulaState(level, casterId);
            FORMULAS.put(casterId, state);
         }

         int gained = isQteSkill(skill) ? (result == QTEResult.PERFECT ? 2 : (result == QTEResult.GOOD ? 1 : 0)) : 1;
         if (skill.equals(state.lastSkill)) {
            gained = 0;
         }

         state.lastSkill = skill;
         state.stage = stage;
         state.expiresAt = level.getGameTime() + FORMULA_DURATION[stage];
         if (gained > 0) {
            state.runes = Mth.clamp(state.runes + gained, 0, 3);
         }

         refreshFormulaEffect(state, caster);
      }
   }

   private static void refreshFormulaEffect(ArcaneMageSpellManager.FormulaState state, Entity caster) {
      Entity old = state.effectId == null ? null : state.level.getEntity(state.effectId);
      if (old != null) {
         old.discard();
      }

      if (state.runes <= 0) {
         state.effectId = null;
      } else {
         ArcaneVfxEntity effect = spawn(
            state.level,
            caster.position().add(0.0, caster.getBbHeight() * 0.52, 0.0),
            15,
            state.stage,
            state.runes,
            1.0F,
            (int)Math.max(4L, state.expiresAt - state.level.getGameTime()),
            caster.getYRot(),
            0.0F,
            caster,
            null,
            OrbOfAvariceManager.isHeldBy(caster),
            state.runes >= 3
         );
         state.effectId = effect.getUUID();
         if (state.runes >= 3) {
            play(state.level, caster.position(), SoundEvents.ENCHANTMENT_TABLE_USE, 0.72F, 1.75F);
         }
      }
   }

   private static void clearFormula(UUID casterId) {
      ArcaneMageSpellManager.FormulaState removed = FORMULAS.remove(casterId);
      if (removed != null && removed.effectId != null) {
         Entity effect = removed.level.getEntity(removed.effectId);
         if (effect != null) {
            effect.discard();
         }
      }
   }

   private static void setAnchor(ServerLevel level, Entity caster, Vec3 position, int stage) {
      ArcaneMageSpellManager.AnchorState previous = ANCHORS.remove(caster.getUUID());
      if (previous != null) {
         previous.discardEffect();
      }

      int duration = 100 + stage * 10;
      ArcaneVfxEntity effect = spawn(
         level,
         position.add(0.0, 0.08, 0.0),
         3,
         stage,
         1.1F + stage * 0.12F,
         1.0F,
         duration + 5,
         caster.getYRot(),
         0.0F,
         caster,
         null,
         OrbOfAvariceManager.isHeldBy(caster),
         false
      );
      ANCHORS.put(caster.getUUID(), new ArcaneMageSpellManager.AnchorState(level, caster.getUUID(), position, level.getGameTime() + duration, effect.getUUID()));
   }

   private static boolean recallAnchor(ServerLevel level, Entity caster) {
      ArcaneMageSpellManager.AnchorState anchor = ANCHORS.get(caster.getUUID());
      if (anchor != null && anchor.level == level && anchor.expiresAt >= level.getGameTime()) {
         AABB moved = caster.getBoundingBox().move(anchor.position.subtract(caster.position()));
         if (!level.noCollision(caster, moved)) {
            message(caster, "The recall anchor is obstructed.");
            return true;
         } else {
            Vec3 origin = caster.position();
            caster.teleportTo(anchor.position.x, anchor.position.y, anchor.position.z);
            Vec3 link = anchor.position.subtract(origin);
            spawn(
               level,
               origin.add(0.0, caster.getBbHeight() * 0.45, 0.0),
               2,
               outputStage(caster),
               0.8F,
               (float)link.length(),
               18,
               yawFor(link),
               pitchFor(link),
               caster,
               null,
               OrbOfAvariceManager.isHeldBy(caster),
               true
            );
            anchor.discardEffect();
            ANCHORS.remove(caster.getUUID());
            CooldownManager.set(caster, "Vector Step", 30);
            play(level, anchor.position, SoundEvents.ENDERMAN_TELEPORT, 0.8F, 1.7F);
            return true;
         }
      } else {
         if (anchor != null) {
            anchor.discardEffect();
            ANCHORS.remove(caster.getUUID());
         }

         return false;
      }
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         tickList(ACTIVE_BOLTS);
         tickList(ACTIVE_POLARITIES);
         tickList(ACTIVE_RENDS);
         tickList(ACTIVE_CONVERGENCES);
         tickList(DELAYED_BURSTS);
         tickFormulaStates();
         tickAnchors();
         tickRelays();
         tickArsenals();
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      FORMULAS.clear();
      ANCHORS.clear();
      RELAYS.clear();
      ARSENALS.clear();
      ACTIVE_BOLTS.clear();
      ACTIVE_POLARITIES.clear();
      ACTIVE_RENDS.clear();
      ACTIVE_CONVERGENCES.clear();
      DELAYED_BURSTS.clear();
   }

   private static <T extends ArcaneMageSpellManager.ActiveCast> void tickList(List<T> casts) {
      Iterator<T> iterator = casts.iterator();

      while (iterator.hasNext()) {
         if (iterator.next().tick()) {
            iterator.remove();
         }
      }
   }

   private static void tickFormulaStates() {
      Iterator<Entry<UUID, ArcaneMageSpellManager.FormulaState>> iterator = FORMULAS.entrySet().iterator();

      while (iterator.hasNext()) {
         ArcaneMageSpellManager.FormulaState state = iterator.next().getValue();
         Entity caster = state.level.getEntity(state.casterId);
         if (caster == null || !caster.isAlive() || state.expiresAt < state.level.getGameTime()) {
            if (state.effectId != null) {
               Entity effect = state.level.getEntity(state.effectId);
               if (effect != null) {
                  effect.discard();
               }
            }

            iterator.remove();
         }
      }
   }

   private static void tickAnchors() {
      Iterator<Entry<UUID, ArcaneMageSpellManager.AnchorState>> iterator = ANCHORS.entrySet().iterator();

      while (iterator.hasNext()) {
         ArcaneMageSpellManager.AnchorState state = iterator.next().getValue();
         Entity caster = state.level.getEntity(state.casterId);
         if (caster == null || !caster.isAlive() || state.expiresAt < state.level.getGameTime()) {
            state.discardEffect();
            iterator.remove();
         }
      }
   }

   private static void tickRelays() {
      Iterator<Entry<UUID, ArcaneMageSpellManager.RelayState>> iterator = RELAYS.entrySet().iterator();

      while (iterator.hasNext()) {
         ArcaneMageSpellManager.RelayState state = iterator.next().getValue();
         Entity caster = state.level.getEntity(state.casterId);
         if (caster == null || !caster.isAlive() || state.expiresAt < state.level.getGameTime()) {
            state.discardEffects();
            iterator.remove();
         }
      }
   }

   private static void tickArsenals() {
      Iterator<Entry<UUID, ArcaneMageSpellManager.ArsenalState>> iterator = ARSENALS.entrySet().iterator();

      while (iterator.hasNext()) {
         ArcaneMageSpellManager.ArsenalState state = iterator.next().getValue();
         Entity caster = state.level.getEntity(state.casterId);
         if (caster != null && caster.isAlive() && state.expiresAt >= state.level.getGameTime() && state.remaining > 0) {
            state.tick(caster);
         } else {
            state.discardEffect();
            iterator.remove();
         }
      }
   }

   private static ArcaneMageSpellManager.RelayState activeRelay(ServerLevel level, Entity caster) {
      ArcaneMageSpellManager.RelayState relay = RELAYS.get(caster.getUUID());
      return relay != null && relay.level == level && relay.expiresAt >= level.getGameTime() ? relay : null;
   }

   private static Vec3 routedOrigin(ServerLevel level, Entity caster, Vec3 direction) {
      ArcaneMageSpellManager.RelayState relay = activeRelay(level, caster);
      return relay == null ? caster.getEyePosition().add(direction.scale(0.75)) : relay.exit.add(direction.scale(0.55));
   }

   private static Vec3 aimPoint(ServerLevel level, Entity caster, double range) {
      Vec3 start = caster.getEyePosition();
      Vec3 end = start.add(safeDirection(caster.getLookAngle()).scale(range));
      BlockHitResult blockHit = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, caster));
      Vec3 clipped = blockHit.getType() == Type.MISS ? end : blockHit.getLocation();
      AABB search = caster.getBoundingBox().expandTowards(clipped.subtract(start)).inflate(1.2);
      EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
         caster,
         start,
         clipped,
         search,
         target -> target instanceof LivingEntity living && MageCombatHelper.isValidTarget(caster, living),
         start.distanceToSqr(clipped)
      );
      return entityHit == null ? clipped : entityHit.getEntity().getBoundingBox().getCenter();
   }

   private static LivingEntity findLookTarget(ServerLevel level, Entity caster, double range) {
      Vec3 start = caster.getEyePosition();
      Vec3 end = start.add(safeDirection(caster.getLookAngle()).scale(range));
      BlockHitResult blockHit = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, caster));
      if (blockHit.getType() != Type.MISS) {
         end = blockHit.getLocation();
      }

      AABB search = caster.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.5);
      EntityHitResult hit = ProjectileUtil.getEntityHitResult(
         caster,
         start,
         end,
         search,
         target -> target instanceof LivingEntity livingx && MageCombatHelper.isValidTarget(caster, livingx),
         start.distanceToSqr(end)
      );
      return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
   }

   private static LivingEntity nearestTarget(ServerLevel level, Entity caster, Vec3 center, double radius, Set<UUID> excluded) {
      return level.getEntitiesOfClass(
            LivingEntity.class,
            new AABB(center, center).inflate(radius),
            candidate -> MageCombatHelper.isValidTarget(caster, candidate) && (excluded == null || !excluded.contains(candidate.getUUID()))
         )
         .stream()
         .min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center)))
         .orElse(null);
   }

   private static Vec3 safeTeleportPoint(ServerLevel level, Entity caster, Vec3 direction, double distance) {
      Vec3 origin = caster.position();
      Vec3 desired = origin.add(direction.scale(distance));
      BlockHitResult hit = level.clip(
         new ClipContext(
            origin.add(0.0, caster.getBbHeight() * 0.5, 0.0), desired.add(0.0, caster.getBbHeight() * 0.5, 0.0), Block.COLLIDER, Fluid.NONE, caster
         )
      );
      if (hit.getType() != Type.MISS) {
         desired = hit.getLocation().subtract(direction.scale(0.65)).subtract(0.0, caster.getBbHeight() * 0.5, 0.0);
      }

      Vec3 delta = desired.subtract(origin);

      for (int attempt = 0; attempt <= 12; attempt++) {
         Vec3 candidate = desired.subtract(safeDirection(delta).scale(attempt * 0.35));
         AABB moved = caster.getBoundingBox().move(candidate.subtract(origin));
         if (level.noCollision(caster, moved)) {
            return candidate;
         }
      }

      return origin;
   }

   private static Vec3 groundPoint(ServerLevel level, Vec3 point) {
      int startY = Mth.floor(point.y + 3.0);
      MutableBlockPos cursor = new MutableBlockPos(Mth.floor(point.x), startY, Mth.floor(point.z));

      for (int offset = 0; offset < 18; offset++) {
         cursor.setY(startY - offset);
         BlockState state = level.getBlockState(cursor);
         if (!state.isAir() && !state.getCollisionShape(level, cursor).isEmpty()) {
            return new Vec3(point.x, cursor.getY() + 1.015, point.z);
         }
      }

      return point;
   }

   private static Vec3 groundBelow(ServerLevel level, Vec3 point) {
      return groundPoint(level, point.add(0.0, 2.0, 0.0));
   }

   private static void damageArea(ServerLevel level, Entity caster, Vec3 center, double radius, float damage, double push) {
      AABB area = new AABB(center, center).inflate(radius);

      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate))) {
         MageCombatHelper.hurt(level, caster, target, damage);
         if (push > 0.0) {
            Vec3 direction = safeDirection(target.getBoundingBox().getCenter().subtract(center));
            target.setDeltaMovement(target.getDeltaMovement().add(direction.scale(push * controlFactor(caster, target))));
         }
      }
   }

   private static double controlFactor(Entity caster, LivingEntity target) {
      double casterLevel = caster instanceof Player ? variables(caster).Level : caster.getPersistentData().getDouble("Level");
      double targetLevel = target instanceof Player ? variables(target).Level : target.getPersistentData().getDouble("Level");
      double factor = Mth.clamp(1.0 - Math.max(0.0, targetLevel - casterLevel) * 0.025, 0.25, 1.0);
      if (isBoss(target)) {
         factor *= 0.35;
      } else if (target instanceof Player) {
         factor *= 0.55;
      }

      return Mth.clamp(factor, 0.16, 1.0);
   }

   private static boolean isBoss(LivingEntity target) {
      return !(target instanceof Player) && (target.getType().is(BOSS_TAG) || target.getMaxHealth() >= 250.0F);
   }

   private static ArcaneVfxEntity spawn(
      ServerLevel level,
      Vec3 position,
      int style,
      int stage,
      float scale,
      float length,
      int lifetime,
      float yaw,
      float pitch,
      Entity owner,
      Entity target,
      boolean orb,
      boolean overcast
   ) {
      return ArcaneVfxEntity.spawn(level, position, style, stage, scale, length, lifetime, yaw, pitch, owner, target, orb, overcast);
   }

   private static Vec3 safeDirection(Vec3 direction) {
      return direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
   }

   private static Vec3 rotateYaw(Vec3 direction, float degrees) {
      double radians = degrees * (float) (Math.PI / 180.0);
      double cosine = Math.cos(radians);
      double sine = Math.sin(radians);
      return safeDirection(new Vec3(direction.x * cosine - direction.z * sine, direction.y, direction.x * sine + direction.z * cosine));
   }

   private static float yawFor(Vec3 direction) {
      return (float)(Mth.atan2(-direction.x, direction.z) * 180.0F / (float)Math.PI);
   }

   private static float pitchFor(Vec3 direction) {
      return (float)(-Mth.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)) * 180.0F / (float)Math.PI);
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

   private static double distanceToSegmentSqr(Vec3 point, Vec3 start, Vec3 end) {
      Vec3 segment = end.subtract(start);
      double lengthSqr = segment.lengthSqr();
      if (lengthSqr < 1.0E-8) {
         return point.distanceToSqr(start);
      }

      double t = Mth.clamp(point.subtract(start).dot(segment) / lengthSqr, 0.0, 1.0);
      return point.distanceToSqr(start.add(segment.scale(t)));
   }

   private interface ActiveCast {
      boolean tick();
   }

   private static final class AnchorState {
      private final ServerLevel level;
      private final UUID casterId;
      private final Vec3 position;
      private final long expiresAt;
      private final UUID effectId;

      private AnchorState(ServerLevel level, UUID casterId, Vec3 position, long expiresAt, UUID effectId) {
         this.level = level;
         this.casterId = casterId;
         this.position = position;
         this.expiresAt = expiresAt;
         this.effectId = effectId;
      }

      private void discardEffect() {
         Entity effect = this.level.getEntity(this.effectId);
         if (effect != null) {
            effect.discard();
         }
      }
   }

   private static final class ArsenalState {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID effectId;
      private final int stage;
      private final int originalBlades;
      private final float totalDamage;
      private final long expiresAt;
      private final boolean orb;
      private final boolean overcast;
      private int remaining;
      private int age;

      private ArsenalState(
         ServerLevel level, UUID casterId, UUID effectId, int stage, int blades, float totalDamage, long expiresAt, boolean orb, boolean overcast
      ) {
         this.level = level;
         this.casterId = casterId;
         this.effectId = effectId;
         this.stage = stage;
         this.originalBlades = blades;
         this.remaining = blades;
         this.totalDamage = totalDamage;
         this.expiresAt = expiresAt;
         this.orb = orb;
         this.overcast = overcast;
      }

      private void tick(Entity caster) {
         this.age++;
         int interval = Math.max(12, 28 - this.stage * 2);
         if (this.age % interval == 0) {
            LivingEntity target = ArcaneMageSpellManager.findLookTarget(this.level, caster, 24.0 + this.stage * 3.0);
            if (target == null) {
               target = ArcaneMageSpellManager.nearestTarget(this.level, caster, caster.position(), 18.0 + this.stage * 2.0, null);
            }

            if (target != null) {
               this.launchOne(caster, target, 0);
            }
         }
      }

      private void releaseAll(Entity caster) {
         LivingEntity primary = ArcaneMageSpellManager.findLookTarget(this.level, caster, 30.0 + this.stage * 3.0);

         for (int i = 0; this.remaining > 0; i++) {
            LivingEntity target = primary;
            if (this.stage >= 3 && i > 0) {
               LivingEntity alternate = ArcaneMageSpellManager.nearestTarget(this.level, caster, caster.position(), 22.0 + this.stage * 2.0, null);
               if (alternate != null) {
                  target = alternate;
               }
            }

            this.launchOne(caster, target, i);
         }

         if (this.overcast && primary != null) {
            ArcaneMageSpellManager.DELAYED_BURSTS
               .add(
                  new ArcaneMageSpellManager.DelayedBurst(
                     this.level,
                     this.casterId,
                     primary.getBoundingBox().getCenter(),
                     3.0 + this.stage * 0.4,
                     this.totalDamage * 0.38F,
                     14,
                     this.stage,
                     this.orb,
                     true
                  )
               );
         }

         this.discardEffect();
      }

      private void launchOne(Entity caster, LivingEntity target, int delay) {
         if (this.remaining > 0) {
            Vec3 origin = caster.getBoundingBox().getCenter().add(0.0, 0.35 + this.remaining % 3 * 0.24, 0.0);
            Vec3 direction = target == null
               ? ArcaneMageSpellManager.safeDirection(caster.getLookAngle())
               : ArcaneMageSpellManager.safeDirection(target.getBoundingBox().getCenter().subtract(origin));
            float bladeDamage = this.totalDamage / Math.max(1, this.originalBlades);
            ArcaneMageSpellManager.spawnBolt(
               this.level,
               caster,
               origin,
               direction,
               this.stage,
               bladeDamage,
               this.overcast,
               delay,
               8,
               0.34 + this.stage * 0.035,
               2.65 + this.stage * 0.14,
               28.0 + this.stage * 4.0,
               this.stage >= 5 ? 2 : 1
            );
            this.remaining--;
            if (this.level.getEntity(this.effectId) instanceof ArcaneVfxEntity arcane) {
               arcane.setScale(Math.max(0.04F, this.remaining));
            }
         }
      }

      private void discardEffect() {
         Entity effect = this.level.getEntity(this.effectId);
         if (effect != null) {
            effect.discard();
         }
      }
   }

   private static final class BoltCast implements ArcaneMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID effectId;
      private final int stage;
      private final float damage;
      private final double speed;
      private final double radius;
      private final int maxHits;
      private final boolean orb;
      private final int style;
      private final boolean overcast;
      private final Set<UUID> hitTargets = new HashSet<>();
      private Vec3 position;
      private Vec3 direction;
      private double remaining;
      private int delay;

      private BoltCast(
         ServerLevel level,
         UUID casterId,
         UUID effectId,
         int stage,
         float damage,
         Vec3 position,
         Vec3 direction,
         double remaining,
         double speed,
         double radius,
         int maxHits,
         int delay,
         boolean orb,
         int style,
         boolean overcast
      ) {
         this.level = level;
         this.casterId = casterId;
         this.effectId = effectId;
         this.stage = stage;
         this.damage = damage;
         this.position = position;
         this.direction = direction;
         this.remaining = remaining;
         this.speed = speed;
         this.radius = radius;
         this.maxHits = maxHits;
         this.delay = delay;
         this.orb = orb;
         this.style = style;
         this.overcast = overcast;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive() && this.level.getEntity(this.effectId) instanceof ArcaneVfxEntity effect) {
            if (this.delay-- > 0) {
               return false;
            }

            if (this.stage >= 2 || this.style == 8) {
               LivingEntity correction = ArcaneMageSpellManager.nearestTarget(
                  this.level, caster, this.position, this.style == 8 ? 14.0 : 6.0 + this.stage, this.hitTargets
               );
               if (correction != null) {
                  Vec3 wanted = ArcaneMageSpellManager.safeDirection(correction.getBoundingBox().getCenter().subtract(this.position));
                  double strength = this.style == 8 ? 0.28 : 0.055 + this.stage * 0.018;
                  this.direction = ArcaneMageSpellManager.safeDirection(this.direction.scale(1.0 - strength).add(wanted.scale(strength)));
               }
            }

            double travel = Math.min(this.speed, this.remaining);
            Vec3 intended = this.position.add(this.direction.scale(travel));
            BlockHitResult blockHit = this.level.clip(new ClipContext(this.position, intended, Block.COLLIDER, Fluid.NONE, caster));
            boolean struckBlock = blockHit.getType() != Type.MISS;
            Vec3 next = struckBlock ? blockHit.getLocation() : intended;
            AABB area = new AABB(this.position, next).inflate(this.radius);
            List<LivingEntity> targets = this.level
               .getEntitiesOfClass(
                  LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate) && !this.hitTargets.contains(candidate.getUUID())
               );
            targets.sort(Comparator.comparingDouble(targetx -> targetx.distanceToSqr(this.position)));

            for (LivingEntity target : targets) {
               if (!(
                     ArcaneMageSpellManager.distanceToSegmentSqr(target.getBoundingBox().getCenter(), this.position, next)
                        > this.radius * this.radius + target.getBbWidth() * target.getBbWidth() * 0.25
                  )
                  && MageCombatHelper.hurt(this.level, caster, target, this.damage)) {
                  this.hitTargets.add(target.getUUID());
                  ArcaneMageSpellManager.spawn(
                     this.level,
                     target.getBoundingBox().getCenter(),
                     1,
                     this.stage,
                     (float)(0.75 + this.radius * 1.5),
                     1.0F,
                     14,
                     0.0F,
                     0.0F,
                     caster,
                     target,
                     this.orb,
                     this.overcast
                  );
                  if (this.hitTargets.size() >= this.maxHits) {
                     effect.discard();
                     return true;
                  }

                  LivingEntity ricochet = ArcaneMageSpellManager.nearestTarget(
                     this.level, caster, target.getBoundingBox().getCenter(), 8.0 + this.stage * 1.5, this.hitTargets
                  );
                  if (ricochet != null) {
                     this.direction = ArcaneMageSpellManager.safeDirection(ricochet.getBoundingBox().getCenter().subtract(target.getBoundingBox().getCenter()));
                  }
               }
            }

            this.position = next;
            this.remaining -= travel;
            effect.setVisualPose(this.position, ArcaneMageSpellManager.yawFor(this.direction), ArcaneMageSpellManager.pitchFor(this.direction));
            if (!struckBlock && !(this.remaining <= 0.01)) {
               return false;
            }

            ArcaneMageSpellManager.spawn(
               this.level, this.position, 1, this.stage, (float)(0.65 + this.radius * 1.35), 1.0F, 12, 0.0F, 0.0F, caster, null, this.orb, this.overcast
            );
            effect.discard();
            return true;
         } else {
            return true;
         }
      }
   }

   private static final class ConvergenceCast implements ArcaneMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID groundEffectId;
      private final UUID skyEffectId;
      private final int stage;
      private final Vec3 center;
      private final float radius;
      private final int duration;
      private final float damage;
      private final boolean orb;
      private final boolean overcast;
      private int age;

      private ConvergenceCast(
         ServerLevel level,
         UUID casterId,
         UUID groundEffectId,
         UUID skyEffectId,
         int stage,
         Vec3 center,
         float radius,
         int duration,
         float damage,
         boolean orb,
         boolean overcast
      ) {
         this.level = level;
         this.casterId = casterId;
         this.groundEffectId = groundEffectId;
         this.skyEffectId = skyEffectId;
         this.stage = stage;
         this.center = center;
         this.radius = radius;
         this.duration = duration;
         this.damage = damage;
         this.orb = orb;
         this.overcast = overcast;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         Entity ground = this.level.getEntity(this.groundEffectId);
         if (caster != null && caster.isAlive() && ground != null) {
            this.age++;
            AABB area = new AABB(this.center, this.center).inflate(this.radius, this.radius + 8.0, this.radius);
            if ((this.age & 1) == 0) {
               for (LivingEntity target : this.level
                  .getEntitiesOfClass(LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate))) {
                  Vec3 delta = this.center.add(0.0, 0.35, 0.0).subtract(target.getBoundingBox().getCenter());
                  double factor = ArcaneMageSpellManager.controlFactor(caster, target);
                  Vec3 pull = ArcaneMageSpellManager.safeDirection(delta).scale((0.055 + this.stage * 0.009) * factor);
                  if (this.stage >= 5 || this.overcast) {
                     pull = pull.add(0.0, -0.035 * factor, 0.0);
                  }

                  target.setDeltaMovement(target.getDeltaMovement().scale(0.88).add(pull));
                  target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, this.stage >= 4 ? 2 : 1, false, false));
               }

               if (this.stage >= 3) {
                  for (Projectile projectile : this.level.getEntitiesOfClass(Projectile.class, area, candidate -> candidate.isAlive())) {
                     Entity owner = projectile.getOwner();
                     if (owner == null || !MageCombatHelper.areAllied(caster, owner)) {
                        Vec3 curve = ArcaneMageSpellManager.safeDirection(this.center.subtract(projectile.position())).scale(0.045 + this.stage * 0.008);
                        projectile.setDeltaMovement(projectile.getDeltaMovement().add(curve));
                        projectile.hurtMarked = true;
                     }
                  }
               }
            }

            if (this.age % 12 == 0) {
               ArcaneMageSpellManager.damageArea(this.level, caster, this.center, this.radius, this.damage * 0.075F, 0.0);
               List<LivingEntity> candidates = this.level
                  .getEntitiesOfClass(LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate));
               candidates.sort(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(this.center)));
               int strikes = Math.min(this.stage >= 4 ? 4 : 2, candidates.size());

               for (int i = 0; i < strikes; i++) {
                  LivingEntity target = candidates.get(i);
                  MageCombatHelper.hurt(this.level, caster, target, this.damage * 0.055F);
                  ArcaneMageSpellManager.spawn(
                     this.level, target.getBoundingBox().getCenter(), 1, this.stage, 1.1F, 1.0F, 12, 0.0F, 0.0F, caster, target, this.orb, this.overcast
                  );
               }
            }

            if (this.age >= this.duration) {
               ArcaneMageSpellManager.damageArea(this.level, caster, this.center, this.radius * 1.08, this.damage * (this.overcast ? 0.92F : 0.72F), 0.18);
               if (caster instanceof ServerPlayer player) {
                  AbilityDestructionManager.impact(
                     player,
                     AbilityDestructionManager.Profile.ARCANE_CONVERGENCE,
                     this.center,
                     TemporaryStatBonusManager.effectiveIntelligence(player),
                     this.overcast || this.stage >= 5
                  );
               }

               ArcaneMageSpellManager.spawn(
                  this.level,
                  this.center.add(0.0, 0.35, 0.0),
                  14,
                  this.stage,
                  this.radius * 0.92F,
                  this.radius,
                  28,
                  0.0F,
                  0.0F,
                  caster,
                  null,
                  this.orb,
                  this.overcast
               );
               ArcaneMageSpellManager.play(this.level, this.center, SoundEvents.GENERIC_EXPLODE, 1.2F, 0.72F);
               ground.discard();
               if (this.skyEffectId != null) {
                  Entity sky = this.level.getEntity(this.skyEffectId);
                  if (sky != null) {
                     sky.discard();
                  }
               }

               return true;
            } else {
               return false;
            }
         } else {
            return true;
         }
      }
   }

   private static final class DelayedBurst implements ArcaneMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final Vec3 center;
      private final double radius;
      private final float damage;
      private final int stage;
      private final boolean orb;
      private final boolean overcast;
      private int delay;

      private DelayedBurst(ServerLevel level, UUID casterId, Vec3 center, double radius, float damage, int delay, int stage, boolean orb, boolean overcast) {
         this.level = level;
         this.casterId = casterId;
         this.center = center;
         this.radius = radius;
         this.damage = damage;
         this.delay = delay;
         this.stage = stage;
         this.orb = orb;
         this.overcast = overcast;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive()) {
            if (--this.delay > 0) {
               return false;
            }

            ArcaneMageSpellManager.damageArea(this.level, caster, this.center, this.radius, this.damage, 0.12);
            ArcaneMageSpellManager.spawn(
               this.level, this.center, 14, this.stage, (float)this.radius, (float)this.radius, 18, 0.0F, 0.0F, caster, null, this.orb, this.overcast
            );
            ArcaneMageSpellManager.play(this.level, this.center, SoundEvents.AMETHYST_BLOCK_BREAK, 0.9F, 0.72F);
            return true;
         } else {
            return true;
         }
      }
   }

   private static final class FormulaState {
      private final ServerLevel level;
      private final UUID casterId;
      private int runes;
      private int stage = 1;
      private long expiresAt;
      private String lastSkill = "";
      private UUID effectId;

      private FormulaState(ServerLevel level, UUID casterId) {
         this.level = level;
         this.casterId = casterId;
      }
   }

   private static final class PolarityCast implements ArcaneMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID effectId;
      private final int stage;
      private final Vec3 center;
      private final float radius;
      private final int duration;
      private final float damage;
      private final boolean baseRepel;
      private final boolean overcast;
      private int age;

      private PolarityCast(
         ServerLevel level, UUID casterId, UUID effectId, int stage, Vec3 center, float radius, int duration, float damage, boolean baseRepel, boolean overcast
      ) {
         this.level = level;
         this.casterId = casterId;
         this.effectId = effectId;
         this.stage = stage;
         this.center = center;
         this.radius = radius;
         this.duration = duration;
         this.damage = damage;
         this.baseRepel = baseRepel;
         this.overcast = overcast;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         Entity effect = this.level.getEntity(this.effectId);
         if (caster != null && caster.isAlive() && effect != null) {
            this.age++;
            if ((this.age & 1) == 0) {
               boolean repel = this.overcast && this.age > this.duration * 0.7 ? true : this.baseRepel;
               AABB area = new AABB(this.center, this.center).inflate(this.radius);

               for (LivingEntity target : this.level
                  .getEntitiesOfClass(LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate))) {
                  Vec3 delta = this.center.subtract(target.getBoundingBox().getCenter());
                  double distance = Math.max(0.5, delta.length());
                  Vec3 force = ArcaneMageSpellManager.safeDirection(repel ? delta.scale(-1.0) : delta)
                     .scale((0.1 + this.stage * 0.018) * (1.0 - Math.min(0.82, distance / this.radius)) * ArcaneMageSpellManager.controlFactor(caster, target));
                  if (!repel && this.stage >= 3) {
                     force = force.add(0.0, 0.035 * ArcaneMageSpellManager.controlFactor(caster, target), 0.0);
                  }

                  target.setDeltaMovement(target.getDeltaMovement().scale(0.82).add(force));
                  if (this.stage >= 4) {
                     target.fallDistance = 0.0F;
                  }
               }

               if (this.stage >= 2) {
                  for (Projectile projectile : this.level.getEntitiesOfClass(Projectile.class, area, candidate -> candidate.isAlive())) {
                     Entity owner = projectile.getOwner();
                     if (owner == null || !MageCombatHelper.areAllied(caster, owner)) {
                        Vec3 delta = this.center.subtract(projectile.position());
                        Vec3 force = ArcaneMageSpellManager.safeDirection(repel ? delta.scale(-1.0) : delta).scale(0.065 + this.stage * 0.012);
                        projectile.setDeltaMovement(projectile.getDeltaMovement().add(force));
                        projectile.hurtMarked = true;
                     }
                  }
               }
            }

            if (this.age % 10 == 0) {
               ArcaneMageSpellManager.damageArea(this.level, caster, this.center, this.radius, this.damage * 0.16F, this.baseRepel ? 0.08 : 0.0);
            }

            if (this.age >= this.duration) {
               if (this.overcast) {
                  ArcaneMageSpellManager.damageArea(this.level, caster, this.center, this.radius * 1.08, this.damage * 0.85F, 0.45);
                  ArcaneMageSpellManager.spawn(
                     this.level,
                     this.center,
                     14,
                     this.stage,
                     this.radius,
                     this.radius,
                     20,
                     0.0F,
                     0.0F,
                     caster,
                     null,
                     OrbOfAvariceManager.isHeldBy(caster),
                     true
                  );
               }

               effect.discard();
               return true;
            } else {
               return false;
            }
         } else {
            return true;
         }
      }
   }

   private static final class RelayState {
      private final ServerLevel level;
      private final UUID casterId;
      private final Vec3 entry;
      private final Vec3 exit;
      private final long expiresAt;
      private final int branches;
      private final List<UUID> effectIds;

      private RelayState(ServerLevel level, UUID casterId, Vec3 entry, Vec3 exit, long expiresAt, int branches, List<UUID> effectIds) {
         this.level = level;
         this.casterId = casterId;
         this.entry = entry;
         this.exit = exit;
         this.expiresAt = expiresAt;
         this.branches = branches;
         this.effectIds = List.copyOf(effectIds);
      }

      private void discardEffects() {
         for (UUID id : this.effectIds) {
            Entity effect = this.level.getEntity(id);
            if (effect != null) {
               effect.discard();
            }
         }
      }
   }

   private static final class RendCast implements ArcaneMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID effectId;
      private final int stage;
      private final Vec3 direction;
      private final double speed;
      private final float width;
      private final float damage;
      private final boolean orb;
      private final boolean overcast;
      private final Set<UUID> hitTargets = new HashSet<>();
      private Vec3 position;
      private double remaining;
      private int delay;

      private RendCast(
         ServerLevel level,
         UUID casterId,
         UUID effectId,
         int stage,
         Vec3 position,
         Vec3 direction,
         double remaining,
         double speed,
         float width,
         float damage,
         int delay,
         boolean orb,
         boolean overcast
      ) {
         this.level = level;
         this.casterId = casterId;
         this.effectId = effectId;
         this.stage = stage;
         this.position = position;
         this.direction = direction;
         this.remaining = remaining;
         this.speed = speed;
         this.width = width;
         this.damage = damage;
         this.delay = delay;
         this.orb = orb;
         this.overcast = overcast;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive() && this.level.getEntity(this.effectId) instanceof ArcaneVfxEntity effect) {
            if (this.delay-- > 0) {
               return false;
            }

            double travel = Math.min(this.speed, this.remaining);
            Vec3 next = this.position.add(this.direction.scale(travel));
            AABB area = new AABB(this.position, next).inflate(this.width * 0.72, this.width * 0.62, this.width * 0.72);

            for (LivingEntity target : this.level
               .getEntitiesOfClass(
                  LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate) && !this.hitTargets.contains(candidate.getUUID())
               )) {
               if (!(
                     ArcaneMageSpellManager.distanceToSegmentSqr(target.getBoundingBox().getCenter(), this.position, next)
                        > this.width * this.width + target.getBbWidth() * target.getBbWidth() * 0.25
                  )
                  && MageCombatHelper.hurt(this.level, caster, target, this.damage)) {
                  this.hitTargets.add(target.getUUID());
                  if (this.stage >= 4) {
                     target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 18, this.stage >= 5 ? 2 : 1, false, false));
                  }
               }
            }

            this.position = next;
            this.remaining -= travel;
            effect.setVisualPose(this.position, ArcaneMageSpellManager.yawFor(this.direction), ArcaneMageSpellManager.pitchFor(this.direction));
            if (this.remaining <= 0.01) {
               if (this.stage >= 3) {
                  ArcaneMageSpellManager.spawn(
                     this.level,
                     this.position,
                     10,
                     this.stage,
                     this.width * 1.35F,
                     this.width * 2.4F,
                     28,
                     ArcaneMageSpellManager.yawFor(this.direction),
                     ArcaneMageSpellManager.pitchFor(this.direction),
                     caster,
                     null,
                     this.orb,
                     this.overcast
                  );
                  ArcaneMageSpellManager.DELAYED_BURSTS
                     .add(
                        new ArcaneMageSpellManager.DelayedBurst(
                           this.level,
                           this.casterId,
                           this.position,
                           this.width * 1.45,
                           this.damage * (this.overcast ? 0.55F : 0.34F),
                           12,
                           this.stage,
                           this.orb,
                           this.overcast
                        )
                     );
               }

               effect.discard();
               return true;
            } else {
               return false;
            }
         } else {
            return true;
         }
      }
   }
}
