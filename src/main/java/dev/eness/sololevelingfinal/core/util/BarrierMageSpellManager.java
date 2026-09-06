package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BarrierVfxEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class BarrierMageSpellManager {
   public static final String FRACTURE_BOLT = "Fracture Bolt";
   public static final String PRISM_RAMPART = "Prism Rampart";
   public static final String REPULSION_FRAME = "Repulsion Frame";
   public static final String SEALING_PRISM = "Sealing Prism";
   public static final String MIRROR_WARD = "Mirror Ward";
   public static final String RESONANT_COLLAPSE = "Resonant Collapse";
   public static final String ABSOLUTE_BASTION = "Absolute Bastion";
   public static final int NORMAL_PRIMARY = 6479871;
   public static final int NORMAL_SECONDARY = 15925247;
   public static final int ORB_PRIMARY = 2377983;
   public static final int ORB_SECONDARY = 11736402;
   public static final Set<String> BARRIER_SKILLS = Set.of(
      "Fracture Bolt", "Prism Rampart", "Repulsion Frame", "Sealing Prism", "Mirror Ward", "Resonant Collapse", "Absolute Bastion"
   );
   public static final Set<String> QTE_SKILLS = Set.of("Sealing Prism", "Resonant Collapse", "Absolute Bastion");
   public static final Set<String> INSTANT_SKILLS = Set.of("Fracture Bolt", "Prism Rampart", "Repulsion Frame", "Mirror Ward");
   private static final String FRACTURE_PREFIX = "sl_barrier_fracture_";
   private static final String FRACTURE_UNTIL_SUFFIX = "_until";
   private static final double[] COST_MULTIPLIER = new double[]{0.0, 1.0, 1.1, 1.2, 1.3, 1.4};
   private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss"));
   private static final List<BarrierMageSpellManager.BoltCast> ACTIVE_BOLTS = new ArrayList<>();
   private static final List<BarrierMageSpellManager.RepulsionCast> ACTIVE_REPULSIONS = new ArrayList<>();
   private static final List<BarrierMageSpellManager.PrisonCast> ACTIVE_PRISONS = new ArrayList<>();
   private static final List<BarrierMageSpellManager.MirrorCast> ACTIVE_MIRRORS = new ArrayList<>();
   private static final List<BarrierMageSpellManager.CollapseCast> ACTIVE_COLLAPSES = new ArrayList<>();
   private static final List<BarrierMageSpellManager.BastionCast> ACTIVE_BASTIONS = new ArrayList<>();

   private BarrierMageSpellManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null && player.getServer() != null) {
         UUID playerId = player.getUUID();
         ACTIVE_BOLTS.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_REPULSIONS.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_PRISONS.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_MIRRORS.removeIf(cast -> playerId.equals(cast.ownerId));
         ACTIVE_COLLAPSES.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_BASTIONS.removeIf(cast -> playerId.equals(cast.casterId));
         String fractureKey = "sl_barrier_fracture_" + playerId.toString().replace("-", "");
         ArrayList<BarrierVfxEntity> ownedEffects = new ArrayList<>();

         for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
               if (entity instanceof LivingEntity living) {
                  living.getPersistentData().remove(fractureKey);
                  living.getPersistentData().remove(fractureKey + "_until");
               }

               if (entity instanceof BarrierVfxEntity effect && effect.getOwnerId().filter(playerId::equals).isPresent()) {
                  ownedEffects.add(effect);
               }
            }
         }

         ownedEffects.forEach(BarrierVfxEntity::dissolve);
      }
   }

   public static boolean isBarrierSkill(String skill) {
      return BARRIER_SKILLS.contains(skill);
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
         case 1 -> "Facet";
         case 2 -> "Reinforced";
         case 3 -> "Prismatic";
         case 4 -> "Citadel";
         case 5 -> "Absolute";
         default -> "Dormant";
      };
   }

   public static List<Component> tooltip(Entity caster, String skill) {
      int stage = outputStage(caster);
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));

      lines.add(Component.literal(switch (skill) {
         case "Fracture Bolt" -> "Launch a fast barrier shard that marks enemies with Fracture.";
         case "Prism Rampart" -> "Raise a breakable wall that intercepts hostile attacks and projectiles.";
         case "Repulsion Frame" -> "Drive a moving barrier frame through enemies and control their position.";
         case "Sealing Prism" -> "Enclose targets inside breakable prisons that consume Fracture for control.";
         case "Mirror Ward" -> "Catch incoming pressure and return it as capped retaliatory shards.";
         case "Resonant Collapse" -> "Collapse constructs and Fracture marks into one controlled detonation.";
         case "Absolute Bastion" -> "Construct a fortress domain that shields allies and corrals enemies.";
         default -> "Barrier magic.";
      }).withStyle(ChatFormatting.GRAY));
      lines.add(Component.literal("Output: " + stageName(stage)).withStyle(stage >= 5 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA));
      if ("Resonant Collapse".equals(skill)) {
         lines.add(Component.literal("Mana: Dynamic  |  Cooldown: 16.0s").withStyle(ChatFormatting.DARK_GRAY));
         lines.add(Component.literal("Cost rises with projected targets and stored Resonance.").withStyle(ChatFormatting.DARK_AQUA));
      } else {
         lines.add(
            Component.literal(
                  "Mana: " + manaCost(caster, skill, stage, QTEResult.MISS) + "  |  Cooldown: " + String.format("%.1fs", cooldownTicks(skill) / 20.0)
               )
               .withStyle(ChatFormatting.DARK_GRAY)
         );
      }

      if (OrbOfAvariceManager.isHeldBy(caster)) {
         lines.add(Component.literal("Orb: hardened construct, amplified output, +50% mana.").withStyle(ChatFormatting.BLUE));
      }

      return lines;
   }

   public static boolean cast(Entity caster, String skill, QTEResult qteResult) {
      if (!(caster.level() instanceof ServerLevel level && isBarrierSkill(skill))) {
         return false;
      } else {
         if (CooldownManager.isOnCooldown(caster, skill)) {
            message(caster, "Ability on cooldown!");
            return false;
         }

         int stage = outputStage(caster);
         QTEResult result = qteResult == null ? QTEResult.MISS : qteResult;
         List<LivingEntity> prisonTargets = List.of();
         BarrierMageSpellManager.CollapseContext collapse = null;
         if ("Sealing Prism".equals(skill)) {
            prisonTargets = findPrisonTargets(level, caster, stage);
            if (prisonTargets.isEmpty()) {
               message(caster, "No valid target can be sealed.");
               return false;
            }
         } else if ("Resonant Collapse".equals(skill)) {
            collapse = findCollapseContext(level, caster, stage);
            if (collapse.isEmpty()) {
               message(caster, "No construct or Fractured target can resonate.");
               return false;
            }
         }

         int cost = collapse == null ? manaCost(caster, skill, stage, result) : collapseManaCost(caster, stage, result, collapse);
         SololevelingModVariables.PlayerVariables data = variables(caster);
         if (!(caster instanceof Player player && player.isCreative()) && data.MP < cost) {
            message(caster, "Not enough MP! Need " + cost + ".");
            return false;
         } else {
            if (cost > 0) {
               deductMana(caster, cost);
            }

            CooldownManager.set(caster, skill, cooldownTicks(skill));
            CooldownManager.set(caster, "mana_refresh", 40);
            double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
            switch (skill) {
               case "Fracture Bolt":
                  startBolt(level, caster, stage, (float)(1.5 + intelligence * 0.035));
                  break;
               case "Prism Rampart":
                  startRampart(level, caster, stage);
                  break;
               case "Repulsion Frame":
                  startRepulsion(level, caster, stage, (float)(3.0 + intelligence * 0.05));
                  break;
               case "Sealing Prism":
                  startPrisons(level, caster, stage, prisonTargets, (float)(3.0 + intelligence * 0.045));
                  break;
               case "Mirror Ward":
                  startMirror(level, caster, stage);
                  break;
               case "Resonant Collapse":
                  startCollapse(level, caster, stage, collapse);
                  break;
               case "Absolute Bastion":
                  startBastion(level, caster, stage, (float)(1.0 + intelligence * 0.012), (float)(6.0 + intelligence * 0.06));
                  break;
               default:
                  return false;
            }

            return true;
         }
      }
   }

   public static boolean castNpc(Entity caster, String skill) {
      if (caster.level() instanceof ServerLevel level && isBarrierSkill(skill) && !CooldownManager.isOnCooldown(caster, skill)) {
         Entity target = caster instanceof Mob mob ? mob.getTarget() : null;
         if (target == null && !"Mirror Ward".equals(skill) && !"Prism Rampart".equals(skill)) {
            return false;
         }

         int stage = outputStage(caster);
         double intelligence = Math.max(10.0, MageCombatHelper.intelligence(caster));
         boolean cast = true;
         switch (skill) {
            case "Fracture Bolt":
               startBolt(level, caster, stage, (float)(1.5 + intelligence * 0.035));
               break;
            case "Prism Rampart":
               startRampart(level, caster, stage);
               break;
            case "Repulsion Frame":
               startRepulsion(level, caster, stage, (float)(3.0 + intelligence * 0.05));
               break;
            case "Sealing Prism":
               List<LivingEntity> targets = findPrisonTargets(level, caster, stage);
               if (targets.isEmpty()) {
                  cast = false;
               } else {
                  startPrisons(level, caster, stage, targets, (float)(3.0 + intelligence * 0.045));
               }
               break;
            case "Mirror Ward":
               startMirror(level, caster, stage);
               break;
            case "Resonant Collapse":
               BarrierMageSpellManager.CollapseContext context = findCollapseContext(level, caster, stage);
               if (context.isEmpty()) {
                  cast = false;
               } else {
                  startCollapse(level, caster, stage, context);
               }
               break;
            case "Absolute Bastion":
               startBastion(level, caster, stage, (float)(1.0 + intelligence * 0.012), (float)(6.0 + intelligence * 0.06));
               break;
            default:
               cast = false;
         }

         if (cast) {
            CooldownManager.set(caster, skill, cooldownTicks(skill));
         }

         return cast;
      } else {
         return false;
      }
   }

   public static int manaCost(Entity caster, String skill, int stage, QTEResult result) {
      if (caster instanceof Player player && player.isCreative()) {
         return 0;
      } else {
         double basePercent = switch (skill) {
            case "Fracture Bolt" -> 0.0035;
            case "Prism Rampart" -> 0.04;
            case "Repulsion Frame" -> 0.0325;
            case "Sealing Prism" -> 0.06;
            case "Mirror Ward" -> 0.075;
            case "Resonant Collapse" -> 0.03;
            case "Absolute Bastion" -> 0.16;
            default -> 0.0;
         };
         double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
         double maximumMana = 1000.0 + intelligence * 100.0;
         double qte = MageQTEHelper.getManaCostMultiplier(result == null ? QTEResult.MISS : result, intelligence);
         return Math.max(0, OrbOfAvariceManager.adjustManaCost(caster, maximumMana * basePercent * COST_MULTIPLIER[Mth.clamp(stage, 1, 5)] * qte));
      }
   }

   private static int collapseManaCost(Entity caster, int stage, QTEResult result, BarrierMageSpellManager.CollapseContext context) {
      if (caster instanceof Player player && player.isCreative()) {
         return 0;
      } else {
         double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
         double maximumMana = 1000.0 + intelligence * 100.0;
         double baseDamage = 3.0 + intelligence * 0.05;
         double stackDamage = 0.75 + intelligence * 0.0125;
         double projectedDamage = context.targetIds.size() * baseDamage;

         for (int stacks : context.targetStacks.values()) {
            projectedDamage += stacks * stackDamage;
         }

         double raw = maximumMana * 0.03 * COST_MULTIPLIER[Mth.clamp(stage, 1, 5)] + projectedDamage * 1.5;
         raw = Math.min(maximumMana * 0.2, raw);
         raw *= MageQTEHelper.getManaCostMultiplier(result, intelligence);
         return Math.max(1, OrbOfAvariceManager.adjustManaCost(caster, raw));
      }
   }

   private static int cooldownTicks(String skill) {
      return switch (skill) {
         case "Fracture Bolt" -> 10;
         case "Prism Rampart" -> 120;
         case "Repulsion Frame" -> 100;
         case "Sealing Prism" -> 240;
         case "Mirror Ward" -> 360;
         case "Resonant Collapse" -> 320;
         case "Absolute Bastion" -> 1000;
         default -> 20;
      };
   }

   private static void startBolt(ServerLevel level, Entity caster, int stage, float damage) {
      Vec3 direction = safeDirection(caster.getLookAngle());
      Vec3 origin = caster.getEyePosition().add(direction.scale(0.75));
      double speed = 3.5 + stage * 0.28;
      double range = 24.0 + stage * 5.0;
      double radius = 0.24 + stage * 0.1;
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      BarrierVfxEntity effect = spawn(
         level,
         origin,
         0,
         stage,
         (float)(radius * (stage >= 5 ? 2.3 : 1.0)),
         (float)(2.8 + stage * 1.2),
         (int)Math.ceil(range / speed) + 8,
         yawFor(direction),
         pitchFor(direction),
         caster,
         null,
         orb,
         0.0F,
         false
      );
      ACTIVE_BOLTS.add(
         new BarrierMageSpellManager.BoltCast(level, caster.getUUID(), effect.getUUID(), stage, damage, origin, direction, range, speed, radius, orb)
      );
      play(level, origin, SoundEvents.AMETHYST_BLOCK_CHIME, 0.75F, 1.55F);
   }

   private static void startRampart(ServerLevel level, Entity caster, int stage) {
      enforceConstructLimit(level, caster, stage);
      Vec3 center = groundTarget(level, caster, 9.0 + stage * 2.0);
      float halfWidth = new float[]{0.0F, 1.5F, 2.5F, 3.0F, 3.6F, 5.0F}[stage];
      float height = new float[]{0.0F, 2.5F, 3.0F, 3.5F, 4.0F, 5.4F}[stage];
      int lifetime = 80 + stage * 20;
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      float integrity = constructIntegrity(caster, 24.0, 0.55, orb);
      spawn(level, center, 2, stage, halfWidth, height, lifetime, caster.getYRot(), 0.0F, caster, null, orb, integrity, true);
      play(level, center, SoundEvents.AMETHYST_CLUSTER_PLACE, 1.0F, 0.72F + stage * 0.04F);
   }

   private static void startRepulsion(ServerLevel level, Entity caster, int stage, float damage) {
      Vec3 direction = safeDirection(caster.getLookAngle().multiply(1.0, 0.35, 1.0));
      Vec3 origin = caster.position().add(0.0, 0.15, 0.0).add(direction.scale(1.2));
      double distance = new double[]{0.0, 6.0, 8.0, 10.0, 12.0, 14.0}[stage];
      float halfWidth = 1.1F + stage * 0.42F;
      float height = 2.0F + stage * 0.34F;
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      BarrierVfxEntity effect = spawn(level, origin, 4, stage, halfWidth, height, 24, yawFor(direction), pitchFor(direction), caster, null, orb, 0.0F, false);
      ACTIVE_REPULSIONS.add(
         new BarrierMageSpellManager.RepulsionCast(
            level, caster.getUUID(), effect.getUUID(), stage, damage, origin, direction, distance, 1.35 + stage * 0.08, halfWidth, orb
         )
      );
      play(level, origin, SoundEvents.IRON_DOOR_CLOSE, 0.7F, 1.55F);
   }

   private static void startPrisons(ServerLevel level, Entity caster, int stage, List<LivingEntity> targets, float totalDamage) {
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);

      for (LivingEntity target : targets) {
         int stacks = consumeFracture(level, caster, target, 3);
         double factor = controlFactor(caster, target);
         int duration = (int)Math.round(new int[]{0, 28, 40, 46, 52, 60}[stage] * factor + stacks * (isBoss(target) ? 2 : 5));
         duration = Math.max(isBoss(target) ? 10 : 14, duration);
         float scale = Math.max(0.85F, target.getBbWidth() * 0.72F + 0.55F);
         float height = Math.max(1.5F, target.getBbHeight() + 0.45F);
         float integrity = constructIntegrity(caster, 12.0, 0.35, orb);
         BarrierVfxEntity effect = spawn(
            level, target.position().add(0.0, 0.04, 0.0), 5, stage, scale, height, duration + 8, target.getYRot(), 0.0F, caster, target, orb, integrity, true
         );
         MageCombatHelper.hurt(level, caster, target, totalDamage * 0.4F);
         ACTIVE_PRISONS.add(
            new BarrierMageSpellManager.PrisonCast(
               level,
               caster.getUUID(),
               target.getUUID(),
               effect.getUUID(),
               stage,
               duration,
               totalDamage * 0.6F,
               target.position(),
               isBoss(target),
               target instanceof Player
            )
         );
      }

      play(level, targets.get(0).position(), SoundEvents.BEACON_ACTIVATE, 0.65F, 1.7F);
   }

   private static void startMirror(ServerLevel level, Entity caster, int stage) {
      for (BarrierMageSpellManager.MirrorCast existing : new ArrayList<>(ACTIVE_MIRRORS)) {
         if (existing.ownerId.equals(caster.getUUID()) && existing.level == level) {
            existing.finishRequested = true;
         }
      }

      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      float integrity = constructIntegrity(caster, 16.0, 0.4, orb);
      int duration = new int[]{0, 20, 25, 30, 35, 45}[stage];
      BarrierVfxEntity effect = spawn(
         level,
         caster.position().add(0.0, 0.04, 0.0),
         6,
         stage,
         1.25F + stage * 0.16F,
         Math.max(2.0F, caster.getBbHeight() + 0.7F),
         duration + 8,
         caster.getYRot(),
         0.0F,
         caster,
         null,
         orb,
         integrity,
         true
      );
      ACTIVE_MIRRORS.add(new BarrierMageSpellManager.MirrorCast(level, caster.getUUID(), effect.getUUID(), stage, duration, orb));
      play(level, caster.position(), SoundEvents.AMETHYST_BLOCK_RESONATE, 0.9F, 1.35F);
   }

   private static void startCollapse(ServerLevel level, Entity caster, int stage, BarrierMageSpellManager.CollapseContext context) {
      for (UUID constructId : context.constructIds) {
         if (level.getEntity(constructId) instanceof BarrierVfxEntity construct) {
            construct.dissolve();
         }
      }

      ACTIVE_COLLAPSES.add(new BarrierMageSpellManager.CollapseCast(level, caster.getUUID(), stage, context));
      play(level, caster.position(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 1.0F, 1.35F);
   }

   private static void startBastion(ServerLevel level, Entity caster, int stage, float pulseDamage, float finalDamage) {
      enforceConstructLimit(level, caster, stage);
      Vec3 center = groundTarget(level, caster, 16.0 + stage * 4.0);
      float radius = new float[]{0.0F, 7.0F, 9.0F, 12.0F, 15.0F, 20.0F}[stage];
      float height = 5.0F + stage * 2.2F;
      int duration = new int[]{0, 120, 140, 160, 180, 200}[stage];
      boolean orb = OrbOfAvariceManager.isHeldBy(caster);
      float integrity = constructIntegrity(caster, 70.0, 1.2, orb);
      BarrierVfxEntity effect = spawn(level, center, 8, stage, radius, height, duration + 10, caster.getYRot(), 0.0F, caster, null, orb, integrity, true);
      ACTIVE_BASTIONS.add(new BarrierMageSpellManager.BastionCast(level, caster.getUUID(), effect.getUUID(), stage, duration, pulseDamage, finalDamage, radius));
      play(level, center, SoundEvents.BEACON_POWER_SELECT, 1.2F, 0.58F);
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         tick(ACTIVE_BOLTS);
         tick(ACTIVE_REPULSIONS);
         tick(ACTIVE_PRISONS);
         tick(ACTIVE_MIRRORS);
         tick(ACTIVE_COLLAPSES);
         tick(ACTIVE_BASTIONS);
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      ACTIVE_BOLTS.clear();
      ACTIVE_REPULSIONS.clear();
      ACTIVE_PRISONS.clear();
      ACTIVE_MIRRORS.clear();
      ACTIVE_COLLAPSES.clear();
      ACTIVE_BASTIONS.clear();
   }

   private static <T extends BarrierMageSpellManager.ActiveCast> void tick(List<T> casts) {
      Iterator<T> iterator = casts.iterator();

      while (iterator.hasNext()) {
         if (iterator.next().tick()) {
            iterator.remove();
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onLivingHurt(LivingHurtEvent event) {
      LivingEntity victim = event.getEntity();
      if (victim.level() instanceof ServerLevel level && !(event.getAmount() <= 0.0F)) {
         Vec3 sourcePosition = event.getSource().getSourcePosition();
         if (sourcePosition == null && event.getSource().getDirectEntity() != null) {
            sourcePosition = event.getSource().getDirectEntity().getBoundingBox().getCenter();
         }

         if (sourcePosition != null) {
            List<BarrierVfxEntity> constructs = level.getEntitiesOfClass(
               BarrierVfxEntity.class, victim.getBoundingBox().inflate(64.0), construct -> construct.isActive() && construct.isBlockingConstruct()
            );
            constructs.sort(Comparator.comparingDouble(construct -> construct.distanceToSqr(victim)));

            for (BarrierVfxEntity construct : constructs) {
               Entity owner = construct.getOwnerEntity(level);
               if (owner != null && MageCombatHelper.areAllied(owner, victim)) {
                  boolean blocks = construct.getStyle() == 8
                     ? !construct.containsInBastion(sourcePosition) && construct.containsInBastion(victim.getBoundingBox().getCenter())
                     : construct.intersectsSegment(sourcePosition, victim.getBoundingBox().getCenter());
                  if (blocks) {
                     float blocked = construct.absorbDamage(event.getAmount());
                     applyBlockedAmount(event, blocked);
                     if (event.getAmount() <= 0.001F) {
                        return;
                     }
                     break;
                  }
               }
            }
         }

         for (BarrierMageSpellManager.MirrorCast mirror : ACTIVE_MIRRORS) {
            if (mirror.level == level && mirror.ownerId.equals(victim.getUUID()) && !mirror.finishRequested && sourcePosition != null) {
               BarrierVfxEntity effect = mirror.effect();
               if (effect != null && effect.isActive() && mirror.facesSource(victim, sourcePosition)) {
                  float requested = event.getAmount() * 0.55F;
                  float blocked = effect.absorbDamage(requested);
                  if (!(blocked <= 0.0F)) {
                     applyBlockedAmount(event, blocked);
                     Entity attacker = event.getSource().getEntity();
                     if (attacker == null) {
                        attacker = event.getSource().getDirectEntity();
                     }

                     if (attacker != null && attacker != victim) {
                        mirror.record(attacker.getUUID(), blocked);
                     }

                     mirror.hits++;
                     if (mirror.hits >= mirror.hitLimit() || !effect.isAlive()) {
                        mirror.finishRequested = true;
                     }

                     return;
                  }
               }
            }
         }
      }
   }

   private static void applyBlockedAmount(LivingHurtEvent event, float blocked) {
      if (!(blocked <= 0.0F)) {
         float remaining = Math.max(0.0F, event.getAmount() - blocked);
         event.setAmount(remaining);
         if (remaining <= 0.001F) {
            event.setCanceled(true);
         }
      }
   }

   public static void onProjectileBlocked(BarrierVfxEntity construct, Vec3 impact) {
      if (construct.level() instanceof ServerLevel level) {
         spawn(
            level,
            impact,
            9,
            construct.getStage(),
            0.55F + construct.getStage() * 0.11F,
            1.0F,
            12,
            construct.getYRot(),
            0.0F,
            construct.getOwnerEntity(level),
            null,
            construct.isOrbAmplified(),
            0.0F,
            false
         );
         play(level, impact, SoundEvents.AMETHYST_BLOCK_HIT, 0.65F, 1.65F);
      }
   }

   public static void onConstructBroken(BarrierVfxEntity construct) {
      if (construct.level() instanceof ServerLevel level) {
         spawn(
            level,
            construct.position(),
            9,
            construct.getStage(),
            Math.max(0.7F, construct.getScale() * 0.65F),
            Math.max(1.0F, construct.getLength() * 0.55F),
            16,
            construct.getYRot(),
            0.0F,
            construct.getOwnerEntity(level),
            null,
            construct.isOrbAmplified(),
            0.0F,
            false
         );
         play(level, construct.position(), SoundEvents.GLASS_BREAK, 1.0F, 0.75F);
      }
   }

   private static List<LivingEntity> findPrisonTargets(ServerLevel level, Entity caster, int stage) {
      LivingEntity primary = findLookTarget(level, caster, 18.0 + stage * 4.0);
      if (primary == null) {
         return List.of();
      }

      ArrayList<LivingEntity> targets = new ArrayList<>();
      targets.add(primary);
      if (stage < 3) {
         return targets;
      }

      int maximum = stage >= 5 ? 6 : 3;
      List<LivingEntity> nearby = level.getEntitiesOfClass(
         LivingEntity.class,
         primary.getBoundingBox().inflate(stage >= 5 ? 6.0 : 4.5),
         targetx -> targetx != primary && MageCombatHelper.isValidTarget(caster, targetx) && (stage >= 5 || getFracture(level, caster, targetx) > 0)
      );
      nearby.sort(Comparator.comparingDouble(primary::distanceToSqr));

      for (LivingEntity target : nearby) {
         if (targets.size() >= maximum) {
            break;
         }

         targets.add(target);
      }

      return targets;
   }

   private static LivingEntity findLookTarget(ServerLevel level, Entity caster, double range) {
      Vec3 start = caster.getEyePosition();
      Vec3 end = start.add(safeDirection(caster.getLookAngle()).scale(range));
      HitResult blockHit = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, caster));
      if (blockHit.getType() != Type.MISS) {
         end = blockHit.getLocation();
      }

      AABB search = caster.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.2);
      EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
         caster,
         start,
         end,
         search,
         target -> target instanceof LivingEntity livingx && MageCombatHelper.isValidTarget(caster, livingx),
         start.distanceToSqr(end)
      );
      return entityHit != null && entityHit.getEntity() instanceof LivingEntity living ? living : null;
   }

   private static BarrierMageSpellManager.CollapseContext findCollapseContext(ServerLevel level, Entity caster, int stage) {
      double range = new double[]{0.0, 12.0, 16.0, 24.0, 32.0, 40.0}[stage];
      int constructLimit = stage == 1 ? 1 : (stage == 2 ? 2 : Integer.MAX_VALUE);
      int targetLimit = stage == 1 ? 2 : (stage == 2 ? 4 : (stage == 3 ? 8 : (stage == 4 ? 14 : 24)));
      List<BarrierVfxEntity> constructs = level.getEntitiesOfClass(
         BarrierVfxEntity.class,
         caster.getBoundingBox().inflate(range),
         construct -> construct.isActive()
            && construct.getOwnerId().filter(caster.getUUID()::equals).isPresent()
            && (construct.countsTowardConstructLimit() || construct.getStyle() == 3)
      );
      constructs.sort(Comparator.comparingDouble(caster::distanceToSqr));
      if (constructs.size() > constructLimit) {
         constructs = new ArrayList<>(constructs.subList(0, constructLimit));
      }

      List<LivingEntity> marked = level.getEntitiesOfClass(
         LivingEntity.class,
         caster.getBoundingBox().inflate(range),
         targetx -> MageCombatHelper.isValidTarget(caster, targetx) && getFracture(level, caster, targetx) > 0
      );
      marked.sort(Comparator.comparingDouble(caster::distanceToSqr));
      if (marked.size() > targetLimit) {
         marked = new ArrayList<>(marked.subList(0, targetLimit));
      }

      ArrayList<UUID> constructIds = new ArrayList<>();
      ArrayList<Vec3> centers = new ArrayList<>();
      float resonance = 0.0F;

      for (BarrierVfxEntity construct : constructs) {
         constructIds.add(construct.getUUID());
         centers.add(construct.position());
         resonance += construct.getResonance();
      }

      LinkedHashSet<UUID> targetIds = new LinkedHashSet<>();
      Map<UUID, Integer> stacks = new HashMap<>();

      for (LivingEntity target : marked) {
         targetIds.add(target.getUUID());
         stacks.put(target.getUUID(), getFracture(level, caster, target));
         if (centers.isEmpty()) {
            centers.add(target.getBoundingBox().getCenter());
         }
      }

      return new BarrierMageSpellManager.CollapseContext(constructIds, centers, targetIds, stacks, resonance);
   }

   private static void enforceConstructLimit(ServerLevel level, Entity caster, int stage) {
      int maximum = new int[]{0, 1, 1, 2, 2, 3}[stage];
      List<BarrierVfxEntity> constructs = level.getEntitiesOfClass(
         BarrierVfxEntity.class,
         caster.getBoundingBox().inflate(160.0),
         construct -> construct.isActive() && construct.countsTowardConstructLimit() && construct.getOwnerId().filter(caster.getUUID()::equals).isPresent()
      );
      constructs.sort(Comparator.<BarrierVfxEntity>comparingInt(construct -> construct.tickCount).reversed());

      while (constructs.size() >= maximum) {
         BarrierVfxEntity oldest = constructs.remove(0);
         oldest.dissolve();
      }
   }

   private static int addFracture(ServerLevel level, Entity caster, LivingEntity target, int amount, int stage) {
      String key = fractureKey(caster);
      int stacks = Mth.clamp(getFracture(level, caster, target) + amount, 0, 3);
      target.getPersistentData().putInt(key, stacks);
      target.getPersistentData().putLong(key + "_until", level.getGameTime() + 160L);
      spawn(
         level,
         target.getBoundingBox().getCenter(),
         1,
         stage,
         0.42F + stacks * 0.12F,
         Math.max(1.0F, target.getBbHeight()),
         18,
         target.getYRot(),
         0.0F,
         caster,
         target,
         OrbOfAvariceManager.isHeldBy(caster),
         0.0F,
         false
      );
      return stacks;
   }

   private static int getFracture(ServerLevel level, Entity caster, LivingEntity target) {
      String key = fractureKey(caster);
      if (target.getPersistentData().getLong(key + "_until") < level.getGameTime()) {
         target.getPersistentData().remove(key);
         target.getPersistentData().remove(key + "_until");
         return 0;
      } else {
         return Mth.clamp(target.getPersistentData().getInt(key), 0, 3);
      }
   }

   private static int consumeFracture(ServerLevel level, Entity caster, LivingEntity target, int maximum) {
      int current = getFracture(level, caster, target);
      int consumed = Math.min(current, Math.max(0, maximum));
      int remaining = current - consumed;
      String key = fractureKey(caster);
      if (remaining <= 0) {
         clearFracture(caster, target);
      } else {
         target.getPersistentData().putInt(key, remaining);
      }

      return consumed;
   }

   private static void clearFracture(Entity caster, LivingEntity target) {
      String key = fractureKey(caster);
      target.getPersistentData().remove(key);
      target.getPersistentData().remove(key + "_until");
   }

   private static String fractureKey(Entity caster) {
      return "sl_barrier_fracture_" + caster.getUUID().toString().replace("-", "");
   }

   private static float constructIntegrity(Entity caster, double base, double coefficient, boolean orb) {
      double value = base + Math.max(0.0, MageCombatHelper.intelligence(caster)) * coefficient;
      return (float)(value * (orb ? 1.5 : 1.0));
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

   private static Vec3 groundTarget(ServerLevel level, Entity caster, double range) {
      Vec3 start = caster.getEyePosition();
      Vec3 intended = start.add(safeDirection(caster.getLookAngle()).scale(range));
      BlockHitResult hit = level.clip(new ClipContext(start, intended, Block.COLLIDER, Fluid.NONE, caster));
      Vec3 point = hit.getType() == Type.MISS ? intended : hit.getLocation();
      int startY = Mth.floor(Math.max(point.y + 2.0, caster.getY() + 1.0));
      MutableBlockPos cursor = new MutableBlockPos(Mth.floor(point.x), startY, Mth.floor(point.z));

      for (int offset = 0; offset < 12; offset++) {
         cursor.setY(startY - offset);
         BlockState state = level.getBlockState(cursor);
         if (!state.isAir() && !state.getCollisionShape(level, cursor).isEmpty()) {
            return new Vec3(point.x, cursor.getY() + 1.01, point.z);
         }
      }

      return new Vec3(point.x, caster.getY(), point.z);
   }

   private static BarrierVfxEntity spawn(
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
      float integrity,
      boolean active
   ) {
      return BarrierVfxEntity.spawn(level, position, style, stage, scale, length, lifetime, yaw, pitch, owner, target, orb, integrity, active);
   }

   private static void hurtWithoutOrbDouble(ServerLevel level, Entity caster, LivingEntity target, float amount) {
      float adjusted = OrbOfAvariceManager.isHeldBy(caster) ? amount * 0.5F : amount;
      MageCombatHelper.hurt(level, caster, target, adjusted);
   }

   private static Vec3 safeDirection(Vec3 direction) {
      return direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
   }

   private static float yawFor(Vec3 direction) {
      return (float)(Mth.atan2(-direction.x, direction.z) * 180.0F / (float)Math.PI);
   }

   private static float pitchFor(Vec3 direction) {
      return (float)(-Mth.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)) * 180.0F / (float)Math.PI);
   }

   private static Vec3 average(List<Vec3> values) {
      Vec3 sum = Vec3.ZERO;

      for (Vec3 value : values) {
         sum = sum.add(value);
      }

      return values.isEmpty() ? sum : sum.scale(1.0 / values.size());
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

   private interface ActiveCast {
      boolean tick();
   }

   private static final class BastionCast implements BarrierMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID effectId;
      private final int stage;
      private final int duration;
      private final float pulseDamage;
      private final float finalDamage;
      private final float radius;
      private final Set<UUID> inside = new HashSet<>();
      private int age;

      private BastionCast(ServerLevel level, UUID casterId, UUID effectId, int stage, int duration, float pulseDamage, float finalDamage, float radius) {
         this.level = level;
         this.casterId = casterId;
         this.effectId = effectId;
         this.stage = stage;
         this.duration = duration;
         this.pulseDamage = pulseDamage;
         this.finalDamage = finalDamage;
         this.radius = radius;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive() && this.level.getEntity(this.effectId) instanceof BarrierVfxEntity effect && effect.isActive()) {
            this.age++;
            if (this.age % 4 == 0 && this.stage >= 3) {
               this.trackEntry(caster, effect);
            }

            if (this.stage >= 2 && this.age % 40 == 0 && this.age < this.duration) {
               this.pulse(caster, effect);
            }

            if (this.age >= this.duration) {
               this.finish(caster, effect);
               return true;
            } else {
               return false;
            }
         } else {
            return true;
         }
      }

      private List<LivingEntity> targets(Entity caster, BarrierVfxEntity effect) {
         return this.level
            .getEntitiesOfClass(
               LivingEntity.class,
               new AABB(effect.position(), effect.position()).inflate(this.radius, effect.getLength(), this.radius),
               target -> effect.containsInBastion(target.getBoundingBox().getCenter()) && MageCombatHelper.isValidTarget(caster, target)
            );
      }

      private void trackEntry(Entity caster, BarrierVfxEntity effect) {
         Set<UUID> now = new HashSet<>();

         for (LivingEntity target : this.targets(caster, effect)) {
            now.add(target.getUUID());
            if (!this.inside.contains(target.getUUID())) {
               BarrierMageSpellManager.addFracture(this.level, caster, target, 1, this.stage);
               Vec3 inward = effect.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
               if (inward.lengthSqr() > 0.01) {
                  target.setDeltaMovement(target.getDeltaMovement().add(inward.normalize().scale(0.18 * BarrierMageSpellManager.controlFactor(caster, target))));
                  target.hurtMarked = true;
               }
            }
         }

         this.inside.clear();
         this.inside.addAll(now);
      }

      private void pulse(Entity caster, BarrierVfxEntity effect) {
         for (LivingEntity target : this.targets(caster, effect)) {
            MageCombatHelper.hurt(this.level, caster, target, this.pulseDamage);
            Vec3 inward = effect.position().subtract(target.position()).multiply(1.0, 0.12, 1.0);
            if (inward.lengthSqr() > 0.01) {
               target.setDeltaMovement(
                  target.getDeltaMovement().scale(0.62).add(inward.normalize().scale(0.24 * BarrierMageSpellManager.controlFactor(caster, target)))
               );
               target.hurtMarked = true;
            }

            if (this.stage >= 4 && BarrierMageSpellManager.getFracture(this.level, caster, target) > 0) {
               target.addEffect(
                  new MobEffectInstance(
                     MobEffects.MOVEMENT_SLOWDOWN,
                     BarrierMageSpellManager.isBoss(target) ? 8 : 14,
                     BarrierMageSpellManager.isBoss(target) ? 2 : 7,
                     false,
                     false
                  )
               );
            }
         }

         BarrierMageSpellManager.spawn(
            this.level,
            effect.position(),
            9,
            this.stage,
            this.radius,
            2.5F + this.stage,
            14,
            effect.getYRot(),
            0.0F,
            caster,
            null,
            effect.isOrbAmplified(),
            0.0F,
            false
         );
         BarrierMageSpellManager.play(this.level, effect.position(), SoundEvents.BEACON_AMBIENT, 0.55F, 1.35F);
      }

      private void finish(Entity caster, BarrierVfxEntity effect) {
         for (LivingEntity target : this.targets(caster, effect)) {
            MageCombatHelper.hurt(this.level, caster, target, this.finalDamage);
         }

         BarrierMageSpellManager.spawn(
            this.level,
            effect.position(),
            7,
            this.stage,
            this.radius,
            effect.getLength(),
            28,
            effect.getYRot(),
            0.0F,
            caster,
            null,
            effect.isOrbAmplified(),
            0.0F,
            false
         );
         effect.dissolve();
         BarrierMageSpellManager.play(this.level, effect.position(), SoundEvents.GENERIC_EXPLODE, 1.3F, 0.68F);
      }
   }

   private static final class BoltCast implements BarrierMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID effectId;
      private final int stage;
      private final float damage;
      private final Set<UUID> hitTargets = new HashSet<>();
      private final double speed;
      private final double radius;
      private final boolean orb;
      private Vec3 position;
      private final Vec3 direction;
      private double remaining;
      private boolean hitPrimary;

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
         boolean orb
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
         this.orb = orb;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive() && this.level.getEntity(this.effectId) instanceof BarrierVfxEntity effect) {
            double travel = Math.min(this.speed, this.remaining);
            Vec3 intended = this.position.add(this.direction.scale(travel));
            BlockHitResult blockHit = this.level.clip(new ClipContext(this.position, intended, Block.COLLIDER, Fluid.NONE, caster));
            boolean struckBlock = blockHit.getType() != Type.MISS;
            Vec3 next = struckBlock ? blockHit.getLocation() : intended;
            AABB area = new AABB(this.position, next).inflate(this.radius);
            List<LivingEntity> targets = this.level
               .getEntitiesOfClass(
                  LivingEntity.class, area, targetx -> MageCombatHelper.isValidTarget(caster, targetx) && !this.hitTargets.contains(targetx.getUUID())
               );
            targets.sort(Comparator.comparingDouble(targetx -> targetx.distanceToSqr(this.position)));
            int limit = this.stage <= 2 ? 1 : (this.stage == 3 ? 3 : (this.stage == 4 ? 5 : 8));

            for (LivingEntity target : targets) {
               if (this.hitTargets.size() >= limit) {
                  break;
               }

               this.hitTarget(caster, target);
               if (this.stage <= 2) {
                  if (this.stage == 2) {
                     this.ricochet(caster, target);
                  }

                  next = target.getBoundingBox().getCenter();
                  struckBlock = true;
                  break;
               }
            }

            this.position = next;
            this.remaining = this.remaining - this.position.distanceTo(effect.position());
            effect.setPos(this.position.x, this.position.y, this.position.z);
            if (!struckBlock && !(this.remaining <= 0.001) && this.hitTargets.size() < limit) {
               return false;
            }

            this.finish(caster, effect);
            return true;
         } else {
            return true;
         }
      }

      private void hitTarget(Entity caster, LivingEntity target) {
         this.hitTargets.add(target.getUUID());
         MageCombatHelper.hurt(this.level, caster, target, this.damage);
         BarrierMageSpellManager.addFracture(this.level, caster, target, this.stage >= 5 && !this.hitPrimary ? 2 : 1, this.stage);
         this.hitPrimary = true;
         BarrierMageSpellManager.spawn(
            this.level,
            target.getBoundingBox().getCenter(),
            9,
            this.stage,
            0.45F + this.stage * 0.1F,
            1.0F,
            11,
            target.getYRot(),
            0.0F,
            caster,
            target,
            this.orb,
            0.0F,
            false
         );
      }

      private void ricochet(Entity caster, LivingEntity first) {
         List<LivingEntity> nearby = this.level
            .getEntitiesOfClass(
               LivingEntity.class,
               first.getBoundingBox().inflate(4.0),
               target -> MageCombatHelper.isValidTarget(caster, target) && target != first && !this.hitTargets.contains(target.getUUID())
            );
         nearby.sort(Comparator.comparingDouble(first::distanceToSqr));
         if (!nearby.isEmpty()) {
            LivingEntity second = nearby.get(0);
            Vec3 start = first.getBoundingBox().getCenter();
            Vec3 end = second.getBoundingBox().getCenter();
            Vec3 delta = end.subtract(start);
            BarrierMageSpellManager.spawn(
               this.level,
               start.add(delta.scale(0.5)),
               10,
               this.stage,
               0.18F,
               (float)delta.length(),
               9,
               BarrierMageSpellManager.yawFor(delta),
               BarrierMageSpellManager.pitchFor(delta),
               caster,
               second,
               this.orb,
               0.0F,
               false
            );
            this.hitTarget(caster, second);
         }
      }

      private void finish(Entity caster, BarrierVfxEntity effect) {
         effect.dissolve();
         if (this.stage >= 4) {
            float integrity = BarrierMageSpellManager.constructIntegrity(caster, 7.0, 0.1, this.orb);
            BarrierMageSpellManager.spawn(
               this.level,
               this.position.add(0.0, -0.65, 0.0),
               3,
               this.stage,
               this.stage >= 5 ? 1.15F : 0.78F,
               this.stage >= 5 ? 2.2F : 1.5F,
               30,
               BarrierMageSpellManager.yawFor(this.direction),
               0.0F,
               caster,
               null,
               this.orb,
               integrity,
               true
            );
         }
      }
   }

   private static final class CollapseCast implements BarrierMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final int stage;
      private final List<Vec3> centers;
      private final Set<UUID> markedTargets;
      private final float resonance;
      private final int delay;
      private int age;

      private CollapseCast(ServerLevel level, UUID casterId, int stage, BarrierMageSpellManager.CollapseContext context) {
         this.level = level;
         this.casterId = casterId;
         this.stage = stage;
         this.centers = List.copyOf(context.centers);
         this.markedTargets = Set.copyOf(context.targetIds);
         this.resonance = context.resonance;
         this.delay = stage >= 5 ? 10 : (stage >= 4 ? 7 : 3);
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive()) {
            this.age++;
            if (this.stage >= 4 && this.age < this.delay) {
               this.pullTargets(caster);
            }

            if (this.age < this.delay) {
               return false;
            }

            this.detonate(caster);
            return true;
         } else {
            return true;
         }
      }

      private void pullTargets(Entity caster) {
         for (LivingEntity target : this.collectTargets(caster)) {
            Vec3 nearest = this.nearestCenter(target.position());
            Vec3 pull = nearest.subtract(target.position()).multiply(1.0, 0.15, 1.0);
            if (pull.lengthSqr() > 0.05) {
               double factor = BarrierMageSpellManager.controlFactor(caster, target);
               target.setDeltaMovement(target.getDeltaMovement().scale(0.72).add(pull.normalize().scale(0.12 * factor)));
               target.hurtMarked = true;
            }
         }
      }

      private void detonate(Entity caster) {
         double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
         float baseDamage = (float)(3.0 + intelligence * 0.05);
         float stackDamage = (float)(0.75 + intelligence * 0.0125);
         float resonanceCap = (float)(4.0 + intelligence * 0.045);
         float resonanceBonus = Math.min(resonanceCap, this.resonance);

         for (LivingEntity target : this.collectTargets(caster)) {
            int stacks = BarrierMageSpellManager.getFracture(this.level, caster, target);
            MageCombatHelper.hurt(this.level, caster, target, baseDamage + stackDamage * stacks);
            if (resonanceBonus > 0.0F) {
               BarrierMageSpellManager.hurtWithoutOrbDouble(this.level, caster, target, resonanceBonus);
            }

            BarrierMageSpellManager.clearFracture(caster, target);
         }

         boolean orb = OrbOfAvariceManager.isHeldBy(caster);
         if (caster instanceof ServerPlayer player) {
            if (this.stage >= 5) {
               AbilityDestructionManager.impact(
                  player,
                  AbilityDestructionManager.Profile.BARRIER_CATASTROPHE,
                  this.centers.isEmpty() ? caster.position() : BarrierMageSpellManager.average(this.centers),
                  intelligence,
                  true
               );
            } else {
               for (int i = 0; i < Math.min(3, this.centers.size()); i++) {
                  AbilityDestructionManager.impact(player, AbilityDestructionManager.Profile.BARRIER_COLLAPSE, this.centers.get(i), intelligence, false);
               }
            }
         }

         if (this.stage >= 5) {
            Vec3 origin = this.centers.isEmpty() ? caster.position() : BarrierMageSpellManager.average(this.centers);
            BarrierMageSpellManager.spawn(this.level, origin, 7, this.stage, 18.0F, 8.0F, 28, caster.getYRot(), 0.0F, caster, null, orb, 0.0F, false);
         }

         int visualLimit = Math.min(12, this.centers.size());

         for (int i = 0; i < visualLimit; i++) {
            BarrierMageSpellManager.spawn(
               this.level,
               this.centers.get(i),
               7,
               this.stage,
               3.0F + this.stage * 0.75F,
               3.0F + this.stage,
               18,
               caster.getYRot(),
               0.0F,
               caster,
               null,
               orb,
               0.0F,
               false
            );
         }

         BarrierMageSpellManager.play(
            this.level,
            this.centers.isEmpty() ? caster.position() : this.centers.get(0),
            SoundEvents.GENERIC_EXPLODE,
            this.stage >= 5 ? 1.6F : 0.9F,
            this.stage >= 5 ? 0.55F : 1.15F
         );
      }

      private Set<LivingEntity> collectTargets(Entity caster) {
         LinkedHashSet<LivingEntity> result = new LinkedHashSet<>();
         double radius = 3.0 + this.stage * 0.75;

         for (Vec3 center : this.centers) {
            result.addAll(
               this.level
                  .getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius), target -> MageCombatHelper.isValidTarget(caster, target))
            );
         }

         for (UUID targetId : this.markedTargets) {
            if (this.level.getEntity(targetId) instanceof LivingEntity living && MageCombatHelper.isValidTarget(caster, living)) {
               result.add(living);
            }
         }

         return result;
      }

      private Vec3 nearestCenter(Vec3 point) {
         return this.centers.stream().min(Comparator.comparingDouble(point::distanceToSqr)).orElse(point);
      }
   }

   private static final class CollapseContext {
      private final List<UUID> constructIds;
      private final List<Vec3> centers;
      private final Set<UUID> targetIds;
      private final Map<UUID, Integer> targetStacks;
      private final float resonance;

      private CollapseContext(List<UUID> constructIds, List<Vec3> centers, Set<UUID> targetIds, Map<UUID, Integer> targetStacks, float resonance) {
         this.constructIds = List.copyOf(constructIds);
         this.centers = List.copyOf(centers);
         this.targetIds = Set.copyOf(targetIds);
         this.targetStacks = Map.copyOf(targetStacks);
         this.resonance = resonance;
      }

      private boolean isEmpty() {
         return this.constructIds.isEmpty() && this.targetIds.isEmpty();
      }
   }

   private static final class MirrorCast implements BarrierMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID ownerId;
      private final UUID effectId;
      private final int stage;
      private final int duration;
      private final boolean orb;
      private final Map<UUID, Float> storedByAttacker = new HashMap<>();
      private int age;
      private int hits;
      private boolean finishRequested;

      private MirrorCast(ServerLevel level, UUID ownerId, UUID effectId, int stage, int duration, boolean orb) {
         this.level = level;
         this.ownerId = ownerId;
         this.effectId = effectId;
         this.stage = stage;
         this.duration = duration;
         this.orb = orb;
      }

      private BarrierVfxEntity effect() {
         return this.level.getEntity(this.effectId) instanceof BarrierVfxEntity effect ? effect : null;
      }

      private int hitLimit() {
         return new int[]{0, 1, 2, 3, 4, 5}[this.stage];
      }

      private boolean facesSource(LivingEntity owner, Vec3 source) {
         if (this.stage >= 4) {
            return true;
         }

         Vec3 toward = source.subtract(owner.getEyePosition()).multiply(1.0, 0.0, 1.0);
         if (toward.lengthSqr() < 0.001) {
            return true;
         }

         double dot = BarrierMageSpellManager.safeDirection(owner.getLookAngle().multiply(1.0, 0.0, 1.0)).dot(toward.normalize());
         return dot >= (this.stage == 1 ? 0.35 : (this.stage == 2 ? 0.0 : -0.15));
      }

      private void record(UUID attacker, float blocked) {
         this.storedByAttacker.merge(attacker, blocked, Float::sum);
      }

      @Override
      public boolean tick() {
         Entity owner = this.level.getEntity(this.ownerId);
         BarrierVfxEntity effect = this.effect();
         if (owner != null && owner.isAlive()) {
            this.age++;
            if (!this.finishRequested && this.age < this.duration && effect != null && effect.isActive()) {
               return false;
            }

            this.release(owner, effect);
            return true;
         } else {
            if (effect != null) {
               effect.dissolve();
            }

            return true;
         }
      }

      private void release(Entity owner, BarrierVfxEntity effect) {
         if (effect != null) {
            effect.dissolve();
         }

         float cap = (float)(4.0 + MageCombatHelper.intelligence(owner) * 0.06);

         for (Entry<UUID, Float> entry : this.storedByAttacker.entrySet()) {
            if (this.level.getEntity(entry.getKey()) instanceof LivingEntity living && living.isAlive() && MageCombatHelper.isValidTarget(owner, living)) {
               float returned = Math.min(cap, entry.getValue() * 0.5F);
               BarrierMageSpellManager.hurtWithoutOrbDouble(this.level, owner, living, returned);
               Vec3 start = owner.getBoundingBox().getCenter();
               Vec3 end = living.getBoundingBox().getCenter();
               Vec3 delta = end.subtract(start);
               BarrierMageSpellManager.spawn(
                  this.level,
                  start.add(delta.scale(0.5)),
                  10,
                  this.stage,
                  0.18F + this.stage * 0.035F,
                  (float)delta.length(),
                  12,
                  BarrierMageSpellManager.yawFor(delta),
                  BarrierMageSpellManager.pitchFor(delta),
                  owner,
                  living,
                  this.orb,
                  0.0F,
                  false
               );
               BarrierMageSpellManager.spawn(this.level, end, 9, this.stage, 0.65F, 1.0F, 11, living.getYRot(), 0.0F, owner, living, this.orb, 0.0F, false);
            }
         }

         BarrierMageSpellManager.play(this.level, owner.position(), SoundEvents.AMETHYST_BLOCK_RESONATE, 0.8F, 1.7F);
      }
   }

   private static final class PrisonCast implements BarrierMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID targetId;
      private final UUID effectId;
      private final int stage;
      private final int duration;
      private final float releaseDamage;
      private final Vec3 anchor;
      private final boolean boss;
      private final boolean player;
      private int age;

      private PrisonCast(
         ServerLevel level,
         UUID casterId,
         UUID targetId,
         UUID effectId,
         int stage,
         int duration,
         float releaseDamage,
         Vec3 anchor,
         boolean boss,
         boolean player
      ) {
         this.level = level;
         this.casterId = casterId;
         this.targetId = targetId;
         this.effectId = effectId;
         this.stage = stage;
         this.duration = duration;
         this.releaseDamage = releaseDamage;
         this.anchor = anchor;
         this.boss = boss;
         this.player = player;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         Entity targetEntity = this.level.getEntity(this.targetId);
         Entity effectEntity = this.level.getEntity(this.effectId);
         if (caster != null && caster.isAlive() && targetEntity instanceof LivingEntity target) {
            if (effectEntity instanceof BarrierVfxEntity effect && effect.isActive()) {
               this.age++;
               if (!this.boss && !this.player) {
                  target.setDeltaMovement(0.0, Math.min(0.0, target.getDeltaMovement().y), 0.0);
                  target.hurtMarked = true;
                  if (this.stage < 4) {
                     double y = target.getY();
                     target.setPos(this.anchor.x, y, this.anchor.z);
                  }

                  target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, 10, false, false));
                  target.addEffect(new MobEffectInstance(MobEffects.JUMP, 6, 128, false, false));
               } else {
                  target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, this.boss ? 2 : 4, false, false));
               }

               return this.age >= this.duration ? this.finish(caster, target, true) : false;
            } else {
               return true;
            }
         } else {
            return this.finish(null, null, false);
         }
      }

      private boolean finish(Entity caster, LivingEntity target, boolean release) {
         if (this.level.getEntity(this.effectId) instanceof BarrierVfxEntity effect) {
            effect.dissolve();
         }

         if (release && caster != null && target != null && target.isAlive()) {
            MageCombatHelper.hurt(this.level, caster, target, this.releaseDamage);
            BarrierMageSpellManager.spawn(
               this.level,
               target.getBoundingBox().getCenter(),
               7,
               this.stage,
               Math.max(1.0F, target.getBbWidth()),
               target.getBbHeight(),
               14,
               target.getYRot(),
               0.0F,
               caster,
               target,
               OrbOfAvariceManager.isHeldBy(caster),
               0.0F,
               false
            );
            BarrierMageSpellManager.play(this.level, target.position(), SoundEvents.GLASS_BREAK, 0.8F, 1.2F);
         }

         return true;
      }
   }

   private static final class RepulsionCast implements BarrierMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final UUID effectId;
      private final int stage;
      private final float damage;
      private final Set<UUID> hitTargets = new HashSet<>();
      private final double speed;
      private final float halfWidth;
      private final boolean orb;
      private Vec3 position;
      private Vec3 direction;
      private double remaining;
      private int age;

      private RepulsionCast(
         ServerLevel level,
         UUID casterId,
         UUID effectId,
         int stage,
         float damage,
         Vec3 position,
         Vec3 direction,
         double remaining,
         double speed,
         float halfWidth,
         boolean orb
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
         this.halfWidth = halfWidth;
         this.orb = orb;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive() && this.level.getEntity(this.effectId) instanceof BarrierVfxEntity effect) {
            this.age++;
            if (this.stage >= 4 && this.age <= 6) {
               Vec3 aimed = BarrierMageSpellManager.safeDirection(caster.getLookAngle().multiply(1.0, 0.35, 1.0));
               this.direction = BarrierMageSpellManager.safeDirection(this.direction.scale(0.68).add(aimed.scale(0.32)));
            }

            double travel = Math.min(this.speed, this.remaining);
            Vec3 intended = this.position.add(this.direction.scale(travel));
            BlockHitResult blockHit = this.level.clip(new ClipContext(this.position, intended, Block.COLLIDER, Fluid.NONE, caster));
            Vec3 next = blockHit.getType() == Type.MISS ? intended : blockHit.getLocation();
            AABB area = new AABB(this.position, next).inflate(this.halfWidth, 1.7 + this.stage * 0.18, this.halfWidth);

            for (LivingEntity target : this.level.getEntitiesOfClass(LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate))) {
               if (this.hitTargets.add(target.getUUID())) {
                  MageCombatHelper.hurt(this.level, caster, target, this.damage);
                  int consumed = this.stage >= 3 ? BarrierMageSpellManager.consumeFracture(this.level, caster, target, 1) : 0;
                  double factor = BarrierMageSpellManager.controlFactor(caster, target);
                  double force = (0.75 + this.stage * 0.13) * factor * (this.orb ? 1.3 : 1.0);
                  target.setDeltaMovement(this.direction.scale(force).add(0.0, 0.1 * factor, 0.0));
                  target.hurtMarked = true;
                  if (consumed > 0) {
                     int duration = BarrierMageSpellManager.isBoss(target) ? 6 : (target instanceof Player ? 8 : 12 + this.stage * 2);
                     target.addEffect(
                        new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, BarrierMageSpellManager.isBoss(target) ? 2 : 8, false, false)
                     );
                  }
               }
            }

            this.remaining = this.remaining - this.position.distanceTo(next);
            this.position = next;
            effect.setPos(this.position.x, this.position.y, this.position.z);
            effect.setYRot(BarrierMageSpellManager.yawFor(this.direction));
            effect.setXRot(BarrierMageSpellManager.pitchFor(this.direction));
            if (blockHit.getType() == Type.MISS && !(this.remaining <= 0.001)) {
               return false;
            }

            BarrierMageSpellManager.spawn(
               this.level,
               this.position,
               9,
               this.stage,
               this.halfWidth * 0.85F,
               2.0F,
               13,
               effect.getYRot(),
               effect.getXRot(),
               caster,
               null,
               this.orb,
               0.0F,
               false
            );
            effect.dissolve();
            return true;
         } else {
            return true;
         }
      }
   }
}
