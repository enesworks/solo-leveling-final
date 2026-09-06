package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.FireMageVfxEntity;
import dev.eness.sololevelingfinal.core.entity.LiuSwordVfxEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class FireMageSpellManager {
   public static final String FLAME_WEAVING = "Flame Weaving";
   public static final String IGNITION_ORB = "Ignition Orb";
   public static final String INFERNO_LANCE = "Inferno Lance";
   public static final String FLASHFIRE = "Flashfire";
   public static final String CREMATION = "Cremation";
   public static final String FURNACE_DOMINION = "Furnace Dominion";
   public static final String HEAVENFALL = "Heavenfall";
   public static final Set<String> FIRE_SKILLS = Set.of(
      "Flame Weaving", "Ignition Orb", "Inferno Lance", "Flashfire", "Cremation", "Furnace Dominion", "Heavenfall"
   );
   public static final Set<String> QTE_SKILLS = Set.of("Ignition Orb", "Inferno Lance", "Flashfire", "Cremation", "Furnace Dominion", "Heavenfall");
   private static final String SCORCH_DATA = "sl_fire_scorch";
   private static final String FLASHFIRE_GUARD = "sl_flashfire_guard_until";
   private static final String VFX_OWNER = "sl_fire_vfx_owner";
   private static final String FLAME_WEAVING_HITS = "sl_flame_weaving_hits";
   private static final double[] COST_MULTIPLIER = new double[]{0.0, 1.0, 1.1, 1.2, 1.3, 1.4};
   private static final double CREMATION_BASE_MANA_PERCENT = 0.025;
   private static final double CREMATION_MANA_PER_DAMAGE = 4.0;
   private static final List<FireMageSpellManager.FireProjectile> ACTIVE_PROJECTILES = new ArrayList<>();
   private static final List<FireMageSpellManager.FlashfireCast> ACTIVE_DASHES = new ArrayList<>();
   private static final List<FireMageSpellManager.CremationCast> ACTIVE_CREMATIONS = new ArrayList<>();
   private static final List<FireMageSpellManager.FurnaceCast> ACTIVE_FURNACES = new ArrayList<>();
   private static final List<FireMageSpellManager.HeavenfallCast> ACTIVE_HEAVENFALL = new ArrayList<>();
   private static final List<FireMageSpellManager.OrbCollapse> ACTIVE_COLLAPSES = new ArrayList<>();
   private static final List<FireMageSpellManager.DelayedBurst> DELAYED_BURSTS = new ArrayList<>();

   private FireMageSpellManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null && player.getServer() != null) {
         UUID playerId = player.getUUID();
         ACTIVE_PROJECTILES.removeIf(cast -> {
            if (!playerId.equals(cast.casterId)) {
               return false;
            }

            cast.finish(null, false);
            return true;
         });
         ACTIVE_DASHES.removeIf(cast -> {
            if (!playerId.equals(cast.casterId)) {
               return false;
            }

            cast.finish();
            return true;
         });
         ACTIVE_CREMATIONS.removeIf(cast -> playerId.equals(cast.casterId));
         ACTIVE_FURNACES.removeIf(cast -> {
            if (!playerId.equals(cast.casterId)) {
               return false;
            }

            cast.finish();
            return true;
         });
         ACTIVE_HEAVENFALL.removeIf(cast -> {
            if (!playerId.equals(cast.casterId)) {
               return false;
            }

            cast.finish();
            return true;
         });
         ACTIVE_COLLAPSES.removeIf(cast -> playerId.equals(cast.casterId));
         DELAYED_BURSTS.removeIf(cast -> playerId.equals(cast.casterId));
         player.getPersistentData().remove("sl_flashfire_guard_until");
         player.getPersistentData().remove("sl_flame_weaving_hits");
         String scorchKey = playerId.toString();
         ArrayList<FireMageVfxEntity> ownedEffects = new ArrayList<>();

         for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
               if (entity instanceof LivingEntity living) {
                  CompoundTag scorch = living.getPersistentData().getCompound("sl_fire_scorch");
                  if (scorch.contains(scorchKey)) {
                     scorch.remove(scorchKey);
                     if (scorch.isEmpty()) {
                        living.getPersistentData().remove("sl_fire_scorch");
                     } else {
                        living.getPersistentData().put("sl_fire_scorch", scorch);
                     }
                  }
               }

               if (entity instanceof FireMageVfxEntity effect
                  && effect.getPersistentData().hasUUID("sl_fire_vfx_owner")
                  && playerId.equals(effect.getPersistentData().getUUID("sl_fire_vfx_owner"))) {
                  ownedEffects.add(effect);
               }
            }
         }

         ownedEffects.forEach(Entity::discard);
      }
   }

   public static boolean isFireSkill(String skill) {
      return FIRE_SKILLS.contains(skill);
   }

   public static boolean isQteSkill(String skill) {
      return QTE_SKILLS.contains(skill);
   }

   public static int outputStage(Entity caster) {
      if (caster == null) {
         return 0;
      } else {
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
   }

   public static String stageName(int stage) {
      return switch (Mth.clamp(stage, 0, 5)) {
         case 1 -> "Ember";
         case 2 -> "Blaze";
         case 3 -> "Inferno";
         case 4 -> "Cataclysm";
         case 5 -> "Ultimate Weapon";
         default -> "Dormant";
      };
   }

   public static List<Component> tooltip(Entity caster, String skill) {
      int stage = outputStage(caster);
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

      lines.add(Component.literal(switch (skill) {
         case "Flame Weaving" -> "Fast magical fire weaving; every third hit applies Scorch.";
         case "Ignition Orb" -> "A volatile projectile that collapses into an expanding blast.";
         case "Inferno Lance" -> "A piercing solar lance that consumes Scorch for bonus damage.";
         case "Flashfire" -> "Rush as living flame, redirecting and burning enemies in your path.";
         case "Cremation" -> "Consume Scorch to execute linked targets in sequence.";
         case "Furnace Dominion" -> "Create a furnace field that pulls, burns, and finally erupts.";
         case "Heavenfall" -> "Call descending fire; Ultimate output becomes the Day of Ruin.";
         default -> "Fire magic.";
      }).withStyle(ChatFormatting.GRAY));
      lines.add(Component.literal("Output: " + stageName(stage)).withStyle(stage >= 5 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED));
      if (stage > 0 && "Cremation".equals(skill)) {
         lines.add(Component.literal("Mana: Dynamic  |  Cooldown: " + String.format("%.1fs", cooldownTicks(skill) / 20.0)).withStyle(ChatFormatting.DARK_GRAY));
         lines.add(Component.literal("Cost rises with each target and Scorch stack detonated.").withStyle(ChatFormatting.RED));
      } else if (stage > 0) {
         int cost = manaCost(caster, skill, stage, QTEResult.MISS);
         lines.add(
            Component.literal((cost == 0 ? "No mana cost" : "Mana: " + cost) + "  |  Cooldown: " + String.format("%.1fs", cooldownTicks(skill) / 20.0))
               .withStyle(ChatFormatting.DARK_GRAY)
         );
      } else {
         lines.add(Component.literal("No Intelligence data is available.").withStyle(ChatFormatting.DARK_RED));
      }

      return lines;
   }

   public static boolean cast(Entity caster, String skill, QTEResult qteResult) {
      if (caster.level() instanceof ServerLevel level && isFireSkill(skill)) {
         int stage = outputStage(caster);
         if (stage <= 0) {
            message(caster, "Your fire output is too weak to form a spell.");
            return false;
         }

         if (CooldownManager.isOnCooldown(caster, skill)) {
            message(caster, "Ability on cooldown!");
            return false;
         }

         List<UUID> cremationTargets = List.of();
         if ("Cremation".equals(skill)) {
            cremationTargets = findCremationTargets(level, caster, stage);
            if (cremationTargets.isEmpty()) {
               message(caster, "No Scorched target is in range.");
               return false;
            }
         }

         double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
         float cremationInitialDamage = (float)(2.0 + intelligence * 0.03);
         float cremationStackDamage = (float)(4.0 + intelligence * 0.07);
         QTEResult resolvedQte = qteResult == null ? QTEResult.MISS : qteResult;
         int cost = "Cremation".equals(skill)
            ? cremationManaCost(level, caster, stage, resolvedQte, cremationTargets, cremationInitialDamage, cremationStackDamage)
            : manaCost(caster, skill, stage, resolvedQte);
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
            switch (skill) {
               case "Flame Weaving":
                  startWeaving(level, caster, stage, (float)(1.5 + intelligence * 0.035));
                  break;
               case "Ignition Orb":
                  startOrb(level, caster, stage, (float)(4.0 + intelligence * 0.08));
                  break;
               case "Inferno Lance":
                  startLance(level, caster, stage, (float)(6.0 + intelligence * 0.11));
                  break;
               case "Flashfire":
                  ACTIVE_DASHES.add(new FireMageSpellManager.FlashfireCast(level, caster, stage, (float)(3.0 + intelligence * 0.05)));
                  break;
               case "Cremation":
                  ACTIVE_CREMATIONS.add(
                     new FireMageSpellManager.CremationCast(level, caster, stage, cremationInitialDamage, cremationStackDamage, cremationTargets)
                  );
                  break;
               case "Furnace Dominion":
                  ACTIVE_FURNACES.add(
                     new FireMageSpellManager.FurnaceCast(level, caster, stage, (float)(1.2 + intelligence / 50.0), (float)(5.0 + intelligence / 12.0))
                  );
                  break;
               case "Heavenfall":
                  ACTIVE_HEAVENFALL.add(
                     new FireMageSpellManager.HeavenfallCast(level, caster, stage, (float)(24.0 + intelligence / 3.0), (float)(6.0 + intelligence / 10.0))
                  );
                  break;
               default:
                  return false;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean castNpc(Entity caster, String skill) {
      if (caster.level() instanceof ServerLevel level && isFireSkill(skill)) {
         int stage = outputStage(caster);
         if (stage > 0 && !CooldownManager.isOnCooldown(caster, skill)) {
            double intelligence = Math.max(10.0, MageCombatHelper.intelligence(caster));
            boolean cast = true;
            switch (skill) {
               case "Flame Weaving":
                  startWeaving(level, caster, stage, (float)(1.5 + intelligence * 0.035));
                  break;
               case "Ignition Orb":
                  startOrb(level, caster, stage, (float)(4.0 + intelligence * 0.08));
                  break;
               case "Inferno Lance":
                  startLance(level, caster, stage, (float)(6.0 + intelligence * 0.11));
                  break;
               case "Flashfire":
                  ACTIVE_DASHES.add(new FireMageSpellManager.FlashfireCast(level, caster, stage, (float)(3.0 + intelligence * 0.05)));
                  break;
               case "Cremation":
                  List<UUID> targets = findCremationTargets(level, caster, stage);
                  if (targets.isEmpty()) {
                     cast = false;
                  } else {
                     ACTIVE_CREMATIONS.add(
                        new FireMageSpellManager.CremationCast(
                           level, caster, stage, (float)(2.0 + intelligence * 0.03), (float)(4.0 + intelligence * 0.07), targets
                        )
                     );
                  }
                  break;
               case "Furnace Dominion":
                  ACTIVE_FURNACES.add(
                     new FireMageSpellManager.FurnaceCast(level, caster, stage, (float)(1.2 + intelligence / 50.0), (float)(5.0 + intelligence / 12.0))
                  );
                  break;
               case "Heavenfall":
                  ACTIVE_HEAVENFALL.add(
                     new FireMageSpellManager.HeavenfallCast(level, caster, stage, (float)(24.0 + intelligence / 3.0), (float)(6.0 + intelligence / 10.0))
                  );
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
      } else {
         return false;
      }
   }

   public static int manaCost(Entity caster, String skill, int stage, QTEResult result) {
      if (!"Flame Weaving".equals(skill) && !(caster instanceof Player player && player.isCreative())) {
         double basePercent = switch (skill) {
            case "Ignition Orb" -> 0.03;
            case "Inferno Lance" -> 0.045;
            case "Flashfire" -> 0.035;
            case "Cremation" -> 0.025;
            case "Furnace Dominion" -> 0.11;
            case "Heavenfall" -> 0.18;
            default -> 0.0;
         };
         double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
         double maximumMana = 1000.0 + intelligence * 100.0;
         double qte = MageQTEHelper.getManaCostMultiplier(result == null ? QTEResult.MISS : result, intelligence);
         return OrbOfAvariceManager.adjustManaCost(caster, maximumMana * basePercent * COST_MULTIPLIER[Mth.clamp(stage, 1, 5)] * qte);
      } else {
         return 0;
      }
   }

   private static int cremationManaCost(
      ServerLevel level, Entity caster, int stage, QTEResult result, List<UUID> targets, float initialDamage, float stackDamage
   ) {
      double projectedDamage = 0.0;

      for (UUID targetId : targets) {
         if (level.getEntity(targetId) instanceof LivingEntity target && target.isAlive() && MageCombatHelper.isValidTarget(caster, target)) {
            int stacks = getScorch(level, caster, target);
            if (stacks > 0) {
               projectedDamage += initialDamage + stackDamage * stacks;
            }
         }
      }

      double intelligence = Math.max(0.0, MageCombatHelper.intelligence(caster));
      double maximumMana = 1000.0 + intelligence * 100.0;
      double activationCost = maximumMana * 0.025 * COST_MULTIPLIER[Mth.clamp(stage, 1, 5)];
      double outputCost = projectedDamage * 4.0;
      double qte = MageQTEHelper.getManaCostMultiplier(result, intelligence);
      return Math.max(1, OrbOfAvariceManager.adjustManaCost(caster, (activationCost + outputCost) * qte));
   }

   private static int cooldownTicks(String skill) {
      return switch (skill) {
         case "Flame Weaving" -> 9;
         case "Ignition Orb" -> 70;
         case "Inferno Lance" -> 110;
         case "Flashfire" -> 160;
         case "Cremation" -> 240;
         case "Furnace Dominion" -> 480;
         case "Heavenfall" -> 1200;
         default -> 20;
      };
   }

   private static void startWeaving(ServerLevel level, Entity caster, int stage, float damage) {
      Vec3 direction = safeDirection(caster.getLookAngle());
      Vec3 origin = caster.getEyePosition().add(direction.scale(0.65));
      double speed = 4.2 + stage * 0.18;
      double range = 18.0 + stage * 5.0;
      double radius = 0.28 + stage * 0.13;
      ACTIVE_PROJECTILES.add(
         new FireMageSpellManager.FireProjectile(
            level,
            caster,
            FireMageSpellManager.ProjectileKind.WEAVING,
            stage,
            damage,
            origin,
            direction,
            speed,
            range,
            radius,
            0.14F + stage * 0.065F,
            2.7F + stage * 1.05F
         )
      );
      play(level, origin, SoundEvents.BLAZE_SHOOT, 0.55F, 1.45F + stage * 0.05F);
   }

   private static void startOrb(ServerLevel level, Entity caster, int stage, float damage) {
      Vec3 direction = safeDirection(caster.getLookAngle());
      Vec3 origin = caster.getEyePosition().add(direction.scale(0.8));
      double speed = 1.15 + stage * 0.2;
      double range = 20.0 + stage * 7.0;
      double radius = 0.5 + stage * 0.17;
      ACTIVE_PROJECTILES.add(
         new FireMageSpellManager.FireProjectile(
            level,
            caster,
            FireMageSpellManager.ProjectileKind.ORB,
            stage,
            damage,
            origin,
            direction,
            speed,
            range,
            radius,
            0.48F + stage * 0.23F,
            2.5F + stage * 0.65F
         )
      );
      play(level, origin, SoundEvents.FIRECHARGE_USE, 0.85F, 0.82F + stage * 0.05F);
   }

   private static void startLance(ServerLevel level, Entity caster, int stage, float damage) {
      Vec3 direction = safeDirection(caster.getLookAngle());
      Vec3 origin = caster.getEyePosition().add(direction.scale(1.0));
      double speed = 4.1 + stage * 0.42;
      double range = 26.0 + stage * 10.0;
      double radius = 0.48 + stage * 0.34;
      ACTIVE_PROJECTILES.add(
         new FireMageSpellManager.FireProjectile(
            level,
            caster,
            FireMageSpellManager.ProjectileKind.LANCE,
            stage,
            damage,
            origin,
            direction,
            speed,
            range,
            radius,
            0.22F + stage * 0.19F,
            6.5F + stage * 2.45F
         )
      );
      play(level, origin, SoundEvents.BLAZE_SHOOT, 1.0F, 0.58F + stage * 0.05F);
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         tick(ACTIVE_PROJECTILES);
         tick(ACTIVE_DASHES);
         tick(ACTIVE_CREMATIONS);
         tick(ACTIVE_FURNACES);
         tick(ACTIVE_HEAVENFALL);
         tick(ACTIVE_COLLAPSES);
         tick(DELAYED_BURSTS);
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      ACTIVE_PROJECTILES.clear();
      ACTIVE_DASHES.clear();
      ACTIVE_CREMATIONS.clear();
      ACTIVE_FURNACES.clear();
      ACTIVE_HEAVENFALL.clear();
      ACTIVE_COLLAPSES.clear();
      DELAYED_BURSTS.clear();
   }

   private static <T extends FireMageSpellManager.ActiveCast> void tick(List<T> casts) {
      Iterator<T> iterator = casts.iterator();

      while (iterator.hasNext()) {
         T cast = (T)iterator.next();
         if (cast.tick()) {
            iterator.remove();
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void reduceFlashfireDamage(LivingHurtEvent event) {
      LivingEntity target = event.getEntity();
      if (!target.level().isClientSide() && target.getPersistentData().getLong("sl_flashfire_guard_until") >= target.level().getGameTime()) {
         event.setAmount(event.getAmount() * 0.5F);
      }
   }

   private static void resolveOrbImpact(ServerLevel level, Entity caster, Vec3 center, int stage, float damage) {
      double radius = 2.4 + stage * 1.25;
      if (caster instanceof ServerPlayer player) {
         AbilityDestructionManager.impact(
            player, AbilityDestructionManager.Profile.FIRE_ORB, center, TemporaryStatBonusManager.effectiveIntelligence(player), stage >= 5
         );
      }

      int lifetime = stage >= 5 ? 34 : 18 + stage * 2;
      spawnVfx(level, caster, center.x, center.y, center.z, 2, stage, (float)radius, 2.5F + stage * 0.75F, lifetime, 0.0F, 0.0F);
      if (stage >= 5) {
         ACTIVE_COLLAPSES.add(new FireMageSpellManager.OrbCollapse(level, caster, center, stage, radius, damage));
      } else {
         blast(level, caster, center, radius, damage, stage, true);
         if (stage == 4) {
            for (int i = 0; i < 3; i++) {
               double angle = (Math.PI * 2) * i / 3.0;
               Vec3 point = center.add(Math.cos(angle) * radius * 0.58, 0.0, Math.sin(angle) * radius * 0.58);
               DELAYED_BURSTS.add(new FireMageSpellManager.DelayedBurst(level, caster, point, 4 + i * 3, 2.6, damage * 0.2F, stage, 2, false));
            }
         }

         play(level, center, SoundEvents.GENERIC_EXPLODE, 1.0F, 0.72F + stage * 0.045F);
      }
   }

   private static void blast(ServerLevel level, Entity caster, Vec3 center, double radius, float damage, int stage, boolean scorch) {
      spawnExecutionExplosionVfx(level, caster, center, radius, stage);
      AABB area = new AABB(center, center).inflate(radius, Math.max(2.5, radius * 0.72), radius);

      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate))) {
         Vec3 targetCenter = target.getBoundingBox().getCenter();
         double distance = targetCenter.distanceTo(center);
         if (!(distance > radius + Math.max(target.getBbWidth(), target.getBbHeight()) * 0.45)) {
            float falloff = (float)Mth.clamp(1.15 - distance / Math.max(0.1, radius) * 0.35, 0.72, 1.0);
            MageCombatHelper.hurt(level, caster, target, damage * falloff);
            if (scorch) {
               addScorch(level, caster, target, stage >= 5 ? 2 : 1);
            }

            Vec3 push = targetCenter.subtract(center);
            if (push.lengthSqr() > 0.01) {
               target.setDeltaMovement(target.getDeltaMovement().add(push.normalize().scale(0.11 + stage * 0.025)));
               target.hurtMarked = true;
            }
         }
      }
   }

   private static void spawnExecutionExplosionVfx(ServerLevel level, Entity caster, Vec3 center, double radius, int stage) {
      boolean blueFire = OrbOfAvariceManager.isHeldBy(caster);
      int primaryColor = blueFire ? 1527295 : 14105353;
      int secondaryColor = blueFire ? 6940671 : 16765770;
      float blastWidth = Mth.clamp((float)radius * 0.88F, 2.8F, 12.0F);
      float blastHeight = Mth.clamp((float)radius * 1.05F, 3.2F, 14.0F);
      double visualY = center.y + Math.min(1.2, Math.max(0.45, radius * 0.22));
      LiuSwordVfxEntity.spawn(
         level,
         center.x,
         visualY,
         center.z,
         9,
         primaryColor,
         secondaryColor,
         blastWidth,
         blastHeight,
         level.getRandom().nextFloat() * 360.0F,
         18 + Math.min(6, Math.max(1, stage)),
         0.0F,
         0.0F,
         false
      );
   }

   private static List<UUID> findCremationTargets(ServerLevel level, Entity caster, int stage) {
      double radius = 14.0 + stage * 5.0;
      Vec3 eye = caster.getEyePosition();
      Vec3 look = safeDirection(caster.getLookAngle());
      AABB area = caster.getBoundingBox().inflate(radius);
      List<LivingEntity> candidates = level.getEntitiesOfClass(
         LivingEntity.class, area, target -> MageCombatHelper.isValidTarget(caster, target) && getScorch(level, caster, target) > 0
      );
      candidates.sort(Comparator.comparingDouble(target -> aimScore(eye, look, target.getBoundingBox().getCenter())));

      int limit = switch (stage) {
         case 1 -> 1;
         case 2 -> 2;
         case 3 -> 6;
         case 4 -> 10;
         default -> 16;
      };
      return candidates.stream().limit(limit).map(Entity::getUUID).toList();
   }

   private static double aimScore(Vec3 eye, Vec3 look, Vec3 point) {
      Vec3 offset = point.subtract(eye);
      double forward = Math.max(0.0, offset.dot(look));
      Vec3 nearest = eye.add(look.scale(forward));
      return point.distanceToSqr(nearest) * 3.0 + offset.lengthSqr() * 0.018 + (offset.dot(look) < 0.0 ? 10000.0 : 0.0);
   }

   private static void spreadScorch(ServerLevel level, Entity caster, LivingEntity origin, int limit) {
      int spread = 0;

      for (LivingEntity target : level.getEntitiesOfClass(
         LivingEntity.class, origin.getBoundingBox().inflate(5.0), candidate -> MageCombatHelper.isValidTarget(caster, candidate)
      )) {
         if (target != origin && getScorch(level, caster, target) <= 0) {
            addScorch(level, caster, target, 1);
            if (++spread >= limit) {
               break;
            }
         }
      }
   }

   private static FireMageVfxEntity spawnVfx(
      ServerLevel level, Entity caster, double x, double y, double z, int style, int stage, float scale, float length, int lifetime, float yaw, float pitch
   ) {
      return spawnVfx(level, caster, x, y, z, style, stage, scale, length, lifetime, yaw, pitch, 16734730, 16765770);
   }

   private static FireMageVfxEntity spawnVfx(
      ServerLevel level,
      Entity caster,
      double x,
      double y,
      double z,
      int style,
      int stage,
      float scale,
      float length,
      int lifetime,
      float yaw,
      float pitch,
      int primaryColor,
      int secondaryColor
   ) {
      if (OrbOfAvariceManager.isHeldBy(caster)) {
         primaryColor = 1527295;
         secondaryColor = 6940671;
      }

      FireMageVfxEntity effect = FireMageVfxEntity.spawn(level, x, y, z, style, stage, scale, length, lifetime, yaw, pitch, primaryColor, secondaryColor);
      if (caster != null) {
         effect.getPersistentData().putUUID("sl_fire_vfx_owner", caster.getUUID());
      }

      return effect;
   }

   private static void spawnLink(ServerLevel level, Entity caster, Vec3 start, Vec3 end, int stage) {
      Vec3 delta = end.subtract(start);
      if (!(delta.lengthSqr() < 0.04)) {
         Vec3 direction = delta.normalize();
         Vec3 midpoint = start.add(delta.scale(0.5));
         spawnVfx(
            level,
            caster,
            midpoint.x,
            midpoint.y,
            midpoint.z,
            3,
            stage,
            0.12F + stage * 0.035F,
            (float)delta.length() * 0.55F,
            9,
            yawFor(direction),
            pitchFor(direction)
         );
      }
   }

   private static void addScorch(ServerLevel level, Entity caster, LivingEntity target, int amount) {
      if (amount > 0) {
         CompoundTag all = target.getPersistentData().getCompound("sl_fire_scorch");
         String key = caster.getUUID().toString();
         CompoundTag mark = all.getCompound(key);
         int current = mark.getLong("Expiry") >= level.getGameTime() ? mark.getInt("Stacks") : 0;
         mark.putInt("Stacks", Mth.clamp(current + amount, 0, 3));
         mark.putLong("Expiry", level.getGameTime() + 160L);
         all.put(key, mark);
         target.getPersistentData().put("sl_fire_scorch", all);
         spawnVfx(
            level,
            caster,
            target.getX(),
            target.getY() + target.getBbHeight() * 0.55,
            target.getZ(),
            9,
            outputStage(caster),
            Math.max(0.45F, target.getBbWidth() * 0.58F),
            Math.max(0.8F, target.getBbHeight() * 0.45F),
            10,
            0.0F,
            0.0F
         );
      }
   }

   private static int getScorch(ServerLevel level, Entity caster, LivingEntity target) {
      CompoundTag all = target.getPersistentData().getCompound("sl_fire_scorch");
      String key = caster.getUUID().toString();
      if (!all.contains(key)) {
         return 0;
      } else {
         CompoundTag mark = all.getCompound(key);
         if (mark.getLong("Expiry") < level.getGameTime()) {
            all.remove(key);
            target.getPersistentData().put("sl_fire_scorch", all);
            return 0;
         } else {
            return Mth.clamp(mark.getInt("Stacks"), 0, 3);
         }
      }
   }

   private static int consumeScorch(ServerLevel level, Entity caster, LivingEntity target) {
      int stacks = getScorch(level, caster, target);
      if (stacks <= 0) {
         return 0;
      }

      CompoundTag all = target.getPersistentData().getCompound("sl_fire_scorch");
      all.remove(caster.getUUID().toString());
      target.getPersistentData().put("sl_fire_scorch", all);
      return stacks;
   }

   private static Vec3 groundTarget(ServerLevel level, Entity caster, double range) {
      Vec3 eye = caster.getEyePosition();
      Vec3 end = eye.add(safeDirection(caster.getLookAngle()).scale(range));
      BlockHitResult forward = level.clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, caster));
      Vec3 point = forward.getType() == Type.BLOCK ? forward.getLocation() : end;
      BlockHitResult down = level.clip(new ClipContext(point.add(0.0, 5.0, 0.0), point.add(0.0, -24.0, 0.0), Block.COLLIDER, Fluid.NONE, caster));
      return down.getType() == Type.BLOCK ? down.getLocation().add(0.0, 0.04, 0.0) : point;
   }

   private static boolean intersects(LivingEntity target, Vec3 start, Vec3 end, double radius) {
      Vec3 segment = end.subtract(start);
      double lengthSq = segment.lengthSqr();
      Vec3 center = target.getBoundingBox().getCenter();
      double t = lengthSq < 1.0E-7 ? 0.0 : Mth.clamp(center.subtract(start).dot(segment) / lengthSq, 0.0, 1.0);
      Vec3 nearest = start.add(segment.scale(t));
      double allowance = radius + Math.max(target.getBbWidth(), target.getBbHeight() * 0.42);
      return center.distanceToSqr(nearest) <= allowance * allowance;
   }

   private static Vec3 safeDirection(Vec3 direction) {
      return direction != null && !(direction.lengthSqr() < 1.0E-6) ? direction.normalize() : new Vec3(0.0, 0.0, 1.0);
   }

   private static float yawFor(Vec3 direction) {
      return (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
   }

   private static float pitchFor(Vec3 direction) {
      return (float)Math.toDegrees(Math.asin(-Mth.clamp(direction.y, -1.0, 1.0)));
   }

   private static void play(ServerLevel level, Vec3 position, SoundEvent sound, float volume, float pitch) {
      level.playSound(null, position.x, position.y, position.z, sound, SoundSource.PLAYERS, volume, pitch);
   }

   private static void message(Entity caster, String text) {
      if (caster instanceof Player player) {
         player.displayClientMessage(Component.literal(text), true);
      }
   }

   private static void deductMana(Entity caster, int amount) {
      caster.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(data -> {
         data.MP = Math.max(0.0, data.MP - amount);
         data.syncPlayerVariables(caster);
      });
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity caster) {
      return caster.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private interface ActiveCast {
      boolean tick();
   }

   private static final class CremationCast implements FireMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final int stage;
      private final float initialDamage;
      private final float stackDamage;
      private final List<UUID> targets;
      private int age;
      private int index;
      private Vec3 previousPosition;

      private CremationCast(ServerLevel level, Entity caster, int stage, float initialDamage, float stackDamage, List<UUID> targets) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.stage = stage;
         this.initialDamage = initialDamage;
         this.stackDamage = stackDamage;
         this.targets = List.copyOf(targets);
         this.previousPosition = caster.getEyePosition();
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive() && caster.level() == this.level) {
            this.age++;
            int interval = this.stage >= 5 ? 2 : 3;
            if (this.age % interval != 1) {
               return false;
            }

            while (this.index < this.targets.size()) {
               if (this.level.getEntity(this.targets.get(this.index++)) instanceof LivingEntity target
                  && target.isAlive()
                  && MageCombatHelper.isValidTarget(caster, target)) {
                  int stacks = FireMageSpellManager.consumeScorch(this.level, caster, target);
                  if (stacks > 0) {
                     Vec3 targetPosition = target.getBoundingBox().getCenter();
                     FireMageSpellManager.spawnLink(this.level, caster, this.previousPosition, targetPosition, this.stage);
                     this.previousPosition = targetPosition;
                     FireMageSpellManager.spawnVfx(
                        this.level,
                        caster,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        5,
                        this.stage,
                        Math.max(1.0F, target.getBbWidth() * 1.25F),
                        Math.max(2.0F, target.getBbHeight() * 1.45F),
                        18 + this.stage * 2,
                        0.0F,
                        0.0F
                     );
                     MageCombatHelper.hurt(this.level, caster, target, this.initialDamage + this.stackDamage * stacks);
                     if (this.stage >= 4) {
                        FireMageSpellManager.spreadScorch(this.level, caster, target, 2 + (this.stage >= 5 ? 2 : 0));
                     }

                     FireMageSpellManager.play(this.level, targetPosition, SoundEvents.GENERIC_EXPLODE, 0.55F, 1.25F + this.stage * 0.04F);
                     break;
                  }
               }
            }

            return this.index >= this.targets.size();
         } else {
            return true;
         }
      }
   }

   private static final class DelayedBurst implements FireMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final Vec3 center;
      private final int delay;
      private final double radius;
      private final float damage;
      private final int stage;
      private final int style;
      private final boolean scorch;
      private int age;

      private DelayedBurst(ServerLevel level, Entity caster, Vec3 center, int delay, double radius, float damage, int stage, int style, boolean scorch) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.center = center;
         this.delay = Math.max(1, delay);
         this.radius = radius;
         this.damage = damage;
         this.stage = stage;
         this.style = style;
         this.scorch = scorch;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster == null || !caster.isAlive() || caster.level() != this.level) {
            return true;
         }

         if (++this.age < this.delay) {
            return false;
         }

         FireMageSpellManager.blast(this.level, caster, this.center, this.radius, this.damage, this.stage, this.scorch);
         FireMageSpellManager.spawnVfx(
            this.level,
            caster,
            this.center.x,
            this.center.y,
            this.center.z,
            this.style,
            this.stage,
            (float)this.radius,
            3.0F + this.stage * 1.2F,
            18 + this.stage * 2,
            0.0F,
            0.0F
         );
         FireMageSpellManager.play(this.level, this.center, SoundEvents.GENERIC_EXPLODE, 0.75F, 0.78F + this.stage * 0.04F);
         return true;
      }
   }

   private static final class FallingVisual {
      private final FireMageVfxEntity visual;
      private final Vec3 start;
      private final Vec3 target;

      private FallingVisual(FireMageVfxEntity visual, Vec3 start, Vec3 target) {
         this.visual = visual;
         this.start = start;
         this.target = target;
      }
   }

   private static final class FireProjectile implements FireMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final FireMageSpellManager.ProjectileKind kind;
      private final int stage;
      private final float damage;
      private final Vec3 direction;
      private final double speed;
      private final double hitRadius;
      private final Set<UUID> hitTargets = new HashSet<>();
      private final List<Vec3> hitPositions = new ArrayList<>();
      private final FireMageVfxEntity visual;
      private Vec3 position;
      private Vec3 impactPosition;
      private double remaining;
      private boolean finished;

      private FireProjectile(
         ServerLevel level,
         Entity caster,
         FireMageSpellManager.ProjectileKind kind,
         int stage,
         float damage,
         Vec3 origin,
         Vec3 direction,
         double speed,
         double range,
         double hitRadius,
         float visualScale,
         float visualLength
      ) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.kind = kind;
         this.stage = stage;
         this.damage = damage;
         this.position = origin;
         this.direction = FireMageSpellManager.safeDirection(direction);
         this.speed = speed;
         this.remaining = range;
         this.hitRadius = hitRadius;
         int lifetime = Mth.ceil(range / speed) + 8;
         int style = kind == FireMageSpellManager.ProjectileKind.WEAVING ? 0 : (kind == FireMageSpellManager.ProjectileKind.ORB ? 1 : 3);
         this.visual = FireMageSpellManager.spawnVfx(
            level,
            caster,
            origin.x,
            origin.y,
            origin.z,
            style,
            stage,
            visualScale,
            visualLength,
            lifetime,
            FireMageSpellManager.yawFor(direction),
            FireMageSpellManager.pitchFor(direction)
         );
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster == null || !caster.isAlive() || caster.level() != this.level) {
            return this.finish(null, false);
         }

         if (!this.finished && !(this.remaining <= 0.01)) {
            double step = Math.min(this.speed, this.remaining);
            Vec3 start = this.position;
            Vec3 intendedEnd = start.add(this.direction.scale(step));
            if (!this.level.hasChunkAt(BlockPos.containing(intendedEnd))) {
               return this.finish(caster, true);
            }

            BlockHitResult blockHit = this.level.clip(new ClipContext(start, intendedEnd, Block.COLLIDER, Fluid.NONE, caster));
            boolean blocked = blockHit.getType() == Type.BLOCK;
            Vec3 end = blocked ? blockHit.getLocation() : intendedEnd;
            boolean stopOnEntity = this.processTargets(caster, start, end);
            this.position = stopOnEntity && this.impactPosition != null ? this.impactPosition : end;
            this.remaining = this.remaining - start.distanceTo(this.position);
            if (this.visual.isAlive()) {
               this.visual
                  .moveTo(
                     this.position.x,
                     this.position.y,
                     this.position.z,
                     FireMageSpellManager.yawFor(this.direction),
                     FireMageSpellManager.pitchFor(this.direction)
                  );
               this.visual.hasImpulse = true;
            }

            return !blocked && !stopOnEntity && !(this.remaining <= 0.01) ? false : this.finish(caster, true);
         } else {
            return this.finish(caster, true);
         }
      }

      private boolean processTargets(Entity caster, Vec3 start, Vec3 end) {
         AABB search = new AABB(start, end).inflate(this.hitRadius + 2.0);
         List<LivingEntity> targets = this.level.getEntitiesOfClass(LivingEntity.class, search, targetx -> MageCombatHelper.isValidTarget(caster, targetx));
         targets.sort(Comparator.comparingDouble(targetx -> targetx.position().distanceToSqr(start)));
         int pierceLimit = this.kind == FireMageSpellManager.ProjectileKind.WEAVING ? (this.stage < 3 ? 1 : Math.min(4, this.stage - 1)) : Integer.MAX_VALUE;

         for (LivingEntity target : targets) {
            if (!this.hitTargets.contains(target.getUUID()) && FireMageSpellManager.intersects(target, start, end, this.hitRadius)) {
               this.hitTargets.add(target.getUUID());
               this.hitPositions.add(target.getBoundingBox().getCenter());
               if (this.kind == FireMageSpellManager.ProjectileKind.WEAVING) {
                  if (MageCombatHelper.hurt(this.level, caster, target, this.damage)) {
                     int hits = caster.getPersistentData().getInt("sl_flame_weaving_hits") + 1;
                     if (hits >= 3) {
                        FireMageSpellManager.addScorch(this.level, caster, target, 1);
                        hits = 0;
                     }

                     caster.getPersistentData().putInt("sl_flame_weaving_hits", hits);
                  }

                  if (this.hitTargets.size() >= pierceLimit) {
                     return true;
                  }
               } else if (this.kind == FireMageSpellManager.ProjectileKind.ORB) {
                  if (this.stage < 3) {
                     this.impactPosition = target.getBoundingBox().getCenter();
                     return true;
                  }

                  MageCombatHelper.hurt(this.level, caster, target, this.damage * 0.35F);
               } else {
                  int consumed = this.stage >= 3 ? FireMageSpellManager.consumeScorch(this.level, caster, target) : 0;
                  MageCombatHelper.hurt(this.level, caster, target, this.damage * (1.0F + consumed * 0.14F));
                  FireMageSpellManager.addScorch(this.level, caster, target, 1);
                  if (this.stage >= 5) {
                     FireMageSpellManager.DELAYED_BURSTS
                        .add(
                           new FireMageSpellManager.DelayedBurst(
                              this.level, caster, target.getBoundingBox().getCenter(), 8, 2.8, this.damage * 0.38F, this.stage, 2, false
                           )
                        );
                  }
               }
            }
         }

         return false;
      }

      private boolean finish(Entity caster, boolean resolve) {
         if (this.finished) {
            return true;
         }

         this.finished = true;
         if (this.visual.isAlive()) {
            this.visual.discard();
         }

         if (resolve && caster != null) {
            if (this.kind == FireMageSpellManager.ProjectileKind.ORB) {
               FireMageSpellManager.resolveOrbImpact(this.level, caster, this.position, this.stage, this.damage);
            } else if (this.kind == FireMageSpellManager.ProjectileKind.LANCE && this.stage >= 5 && this.hitPositions.isEmpty()) {
               FireMageSpellManager.DELAYED_BURSTS
                  .add(new FireMageSpellManager.DelayedBurst(this.level, caster, this.position, 8, 3.0, this.damage * 0.32F, this.stage, 2, false));
            }
         }

         return true;
      }
   }

   private static final class FlashfireCast implements FireMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final int stage;
      private final float damage;
      private final Set<UUID> hitTargets = new HashSet<>();
      private final FireMageVfxEntity visual;
      private Vec3 direction;
      private Vec3 previous;
      private int age;
      private final int duration;

      private FlashfireCast(ServerLevel level, Entity caster, int stage, float damage) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.stage = stage;
         this.damage = damage;
         this.direction = FireMageSpellManager.safeDirection(caster.getLookAngle());
         this.previous = caster.position();
         this.duration = stage >= 5 ? 22 : (stage >= 3 ? 12 : 7 + stage);
         this.visual = FireMageSpellManager.spawnVfx(
            level,
            caster,
            caster.getX(),
            caster.getY() + caster.getBbHeight() * 0.5,
            caster.getZ(),
            4,
            stage,
            0.65F + stage * 0.12F,
            3.0F + stage * 1.2F,
            this.duration + 7,
            FireMageSpellManager.yawFor(this.direction),
            FireMageSpellManager.pitchFor(this.direction)
         );
         FireMageSpellManager.play(level, caster.position(), SoundEvents.FIRECHARGE_USE, 1.0F, 1.3F);
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster instanceof LivingEntity living && caster.isAlive() && caster.level() == this.level) {
            this.age++;
            if (this.stage >= 5) {
               this.direction = FireMageSpellManager.safeDirection(
                  this.direction.scale(0.72).add(FireMageSpellManager.safeDirection(caster.getLookAngle()).scale(0.28))
               );
            } else if (this.stage >= 3 && this.age == 7) {
               this.direction = FireMageSpellManager.safeDirection(caster.getLookAngle());
            }

            double speed = 0.92 + this.stage * 0.13;
            Vec3 intended = caster.position().add(this.direction.scale(speed));
            BlockHitResult hit = this.level
               .clip(new ClipContext(caster.position().add(0.0, 0.25, 0.0), intended.add(0.0, 0.25, 0.0), Block.COLLIDER, Fluid.NONE, caster));
            if (hit.getType() == Type.BLOCK) {
               return this.finish();
            }

            caster.setDeltaMovement(this.direction.scale(speed).add(0.0, Math.max(-0.08, this.direction.y * 0.35), 0.0));
            living.hurtMarked = true;
            living.fallDistance = 0.0F;
            if (this.stage >= 5) {
               caster.getPersistentData().putLong("sl_flashfire_guard_until", this.level.getGameTime() + 2L);
            }

            this.damageAlongPath(caster, this.previous, intended);
            this.previous = intended;
            if (this.visual.isAlive()) {
               this.visual
                  .moveTo(
                     caster.getX(),
                     caster.getY() + caster.getBbHeight() * 0.5,
                     caster.getZ(),
                     FireMageSpellManager.yawFor(this.direction),
                     FireMageSpellManager.pitchFor(this.direction)
                  );
               this.visual.hasImpulse = true;
            }

            if (this.stage >= 2 && this.age % 3 == 0) {
               FireMageSpellManager.spawnVfx(
                  this.level,
                  caster,
                  caster.getX(),
                  caster.getY() + 0.15,
                  caster.getZ(),
                  4,
                  this.stage,
                  0.34F + this.stage * 0.05F,
                  1.5F + this.stage * 0.35F,
                  8,
                  FireMageSpellManager.yawFor(this.direction),
                  FireMageSpellManager.pitchFor(this.direction)
               );
            }

            return this.age >= this.duration ? this.finish() : false;
         } else {
            return this.finish();
         }
      }

      private void damageAlongPath(Entity caster, Vec3 start, Vec3 end) {
         AABB area = new AABB(start, end).inflate(1.15 + this.stage * 0.16);

         for (LivingEntity target : this.level.getEntitiesOfClass(LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate))) {
            if (this.hitTargets.add(target.getUUID())) {
               MageCombatHelper.hurt(this.level, caster, target, this.damage);
               if (this.stage >= 2) {
                  FireMageSpellManager.addScorch(this.level, caster, target, 1);
               }

               if (this.stage >= 4) {
                  FireMageSpellManager.DELAYED_BURSTS
                     .add(
                        new FireMageSpellManager.DelayedBurst(
                           this.level, caster, target.getBoundingBox().getCenter(), 6, 2.2, this.damage * 0.45F, this.stage, 2, false
                        )
                     );
               }
            }
         }
      }

      private boolean finish() {
         if (this.visual.isAlive()) {
            this.visual.discard();
         }

         return true;
      }
   }

   private static final class FurnaceCast implements FireMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final int stage;
      private final float pulseDamage;
      private final float finalDamage;
      private final Vec3 center;
      private final double radius;
      private final FireMageVfxEntity visual;
      private int age;

      private FurnaceCast(ServerLevel level, Entity caster, int stage, float pulseDamage, float finalDamage) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.stage = stage;
         this.pulseDamage = pulseDamage;
         this.finalDamage = finalDamage;
         this.center = FireMageSpellManager.groundTarget(level, caster, 24.0 + stage * 4.0);
         this.radius = new double[]{0.0, 5.0, 7.0, 9.0, 12.0, 16.0}[stage];
         this.visual = FireMageSpellManager.spawnVfx(
            level, caster, this.center.x, this.center.y, this.center.z, 6, stage, (float)this.radius, 3.2F + stage * 1.15F, 88, 0.0F, 0.0F
         );
         FireMageSpellManager.play(level, this.center, SoundEvents.BLAZE_AMBIENT, 1.0F, 0.55F);
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive() && caster.level() == this.level) {
            this.age++;
            if (this.stage >= 4 && this.age % 2 == 0) {
               this.burnProjectiles(caster);
            }

            if (this.age % 10 == 0 && this.age <= 80) {
               this.pulse(caster, this.age / 10);
            }

            if (this.age >= 82) {
               FireMageSpellManager.blast(this.level, caster, this.center, this.radius * 0.76, this.finalDamage, this.stage, true);
               if (caster instanceof ServerPlayer player) {
                  AbilityDestructionManager.impact(
                     player,
                     AbilityDestructionManager.Profile.FIRE_DOMINION,
                     this.center,
                     TemporaryStatBonusManager.effectiveIntelligence(player),
                     this.stage >= 5
                  );
               }

               FireMageSpellManager.spawnVfx(
                  this.level,
                  caster,
                  this.center.x,
                  this.center.y,
                  this.center.z,
                  8,
                  this.stage,
                  (float)(this.radius * 0.72),
                  5.0F + this.stage * 1.7F,
                  24,
                  0.0F,
                  0.0F
               );
               FireMageSpellManager.play(this.level, this.center, SoundEvents.GENERIC_EXPLODE, 1.35F, 0.72F);
               return this.finish();
            } else {
               return false;
            }
         } else {
            return this.finish();
         }
      }

      private void pulse(Entity caster, int pulse) {
         AABB area = new AABB(this.center, this.center).inflate(this.radius, Math.max(4.0, this.radius * 0.55), this.radius);

         for (LivingEntity target : this.level
            .getEntitiesOfClass(
               LivingEntity.class,
               area,
               candidate -> MageCombatHelper.isValidTarget(caster, candidate) && candidate.distanceToSqr(this.center) <= this.radius * this.radius
            )) {
            MageCombatHelper.hurt(this.level, caster, target, this.pulseDamage);
            if (pulse % 2 == 0) {
               FireMageSpellManager.addScorch(this.level, caster, target, 1);
            }

            if (this.stage >= 3) {
               Vec3 pull = this.center.subtract(target.position());
               if (pull.lengthSqr() > 0.2) {
                  target.setDeltaMovement(target.getDeltaMovement().scale(0.62).add(pull.normalize().scale(0.16 + this.stage * 0.018)));
                  target.hurtMarked = true;
               }
            }
         }

         FireMageSpellManager.play(this.level, this.center, SoundEvents.FIRE_EXTINGUISH, 0.55F, 0.65F + pulse * 0.035F);
      }

      private void burnProjectiles(Entity caster) {
         AABB area = new AABB(this.center, this.center).inflate(this.radius, this.radius, this.radius);

         for (Projectile projectile : this.level.getEntitiesOfClass(Projectile.class, area)) {
            Entity owner = projectile.getOwner();
            if (owner != caster && (owner == null || MageCombatHelper.isValidTarget(caster, owner))) {
               projectile.discard();
            }
         }
      }

      private boolean finish() {
         if (this.visual.isAlive()) {
            this.visual.discard();
         }

         return true;
      }
   }

   private static final class HeavenfallCast implements FireMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final int stage;
      private final float impactDamage;
      private final float followupDamage;
      private final Vec3 center;
      private final double radius;
      private final int delay;
      private final List<FireMageSpellManager.FallingVisual> meteors = new ArrayList<>();
      private int age;

      private HeavenfallCast(ServerLevel level, Entity caster, int stage, float impactDamage, float followupDamage) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.stage = stage;
         this.impactDamage = impactDamage;
         this.followupDamage = followupDamage;
         this.center = FireMageSpellManager.groundTarget(level, caster, 38.0 + stage * 14.0);
         this.radius = new double[]{0.0, 5.0, 7.0, 9.0, 12.0, 18.0}[stage];
         this.delay = new int[]{0, 28, 32, 36, 42, 54}[stage];
         this.createMeteors(caster);
         FireMageSpellManager.play(level, this.center, SoundEvents.WITHER_SPAWN, stage >= 5 ? 1.2F : 0.65F, 1.35F);
      }

      private void createMeteors(Entity caster) {
         int count = switch (this.stage) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 5;
            case 4 -> 1;
            default -> 6;
         };

         for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2) * i / Math.max(1, count) + this.level.getRandom().nextDouble() * 0.35;
            double offset = i != 0 && this.stage != 4 ? 2.0 + this.level.getRandom().nextDouble() * this.radius * 0.48 : 0.0;
            Vec3 target = this.center.add(Math.cos(angle) * offset, 0.0, Math.sin(angle) * offset);
            double height = 22.0 + this.stage * 7.0 + this.level.getRandom().nextDouble() * 8.0;
            Vec3 start = target.add(-10.0 - this.stage * 2.0, height, -6.0 + i * 1.6);
            float scale = (float)(i == 0 ? 0.95 + this.stage * 0.68 : 0.48 + this.stage * 0.22);
            if (this.stage == 5 && i == 0) {
               scale = 6.4F;
            }

            Vec3 direction = FireMageSpellManager.safeDirection(target.subtract(start));
            FireMageVfxEntity visual = FireMageSpellManager.spawnVfx(
               this.level,
               caster,
               start.x,
               start.y,
               start.z,
               7,
               this.stage,
               scale,
               6.0F + this.stage * 2.7F,
               this.delay + 8,
               FireMageSpellManager.yawFor(direction),
               FireMageSpellManager.pitchFor(direction),
               this.stage >= 5 ? 9246472 : 14105353,
               16765770
            );
            this.meteors.add(new FireMageSpellManager.FallingVisual(visual, start, target));
         }
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive() && caster.level() == this.level) {
            this.age++;
            float progress = Mth.clamp((float)this.age / this.delay, 0.0F, 1.0F);
            float eased = progress * progress * (3.0F - 2.0F * progress);

            for (FireMageSpellManager.FallingVisual meteor : this.meteors) {
               Vec3 position = meteor.start.lerp(meteor.target, eased);
               if (meteor.visual.isAlive()) {
                  Vec3 direction = FireMageSpellManager.safeDirection(meteor.target.subtract(position));
                  meteor.visual.moveTo(position.x, position.y, position.z, FireMageSpellManager.yawFor(direction), FireMageSpellManager.pitchFor(direction));
                  meteor.visual.hasImpulse = true;
               }
            }

            if (this.age < this.delay) {
               return false;
            }

            for (FireMageSpellManager.FallingVisual meteor : this.meteors) {
               if (meteor.visual.isAlive()) {
                  meteor.visual.discard();
               }

               FireMageSpellManager.spawnVfx(
                  this.level,
                  caster,
                  meteor.target.x,
                  meteor.target.y,
                  meteor.target.z,
                  8,
                  this.stage,
                  (float)(this.radius * (meteor.target.equals(this.center) ? 1.0 : 0.43)),
                  5.0F + this.stage * 3.0F,
                  26 + this.stage * 4,
                  0.0F,
                  0.0F
               );
            }

            FireMageSpellManager.blast(this.level, caster, this.center, this.radius, this.impactDamage, this.stage, true);
            if (caster instanceof ServerPlayer player) {
               AbilityDestructionManager.impact(
                  player,
                  AbilityDestructionManager.Profile.FIRE_HEAVENFALL,
                  this.center,
                  TemporaryStatBonusManager.effectiveIntelligence(player),
                  this.stage >= 4
               );
            }

            FireMageSpellManager.DELAYED_BURSTS
               .add(new FireMageSpellManager.DelayedBurst(this.level, caster, this.center, 14, this.radius * 1.16, this.followupDamage, this.stage, 8, true));
            if (this.stage >= 5) {
               for (int i = 0; i < 4; i++) {
                  double angle = (Math.PI / 2) * i;
                  Vec3 connected = this.center.add(Math.cos(angle) * this.radius * 0.72, 0.0, Math.sin(angle) * this.radius * 0.72);
                  FireMageSpellManager.DELAYED_BURSTS
                     .add(
                        new FireMageSpellManager.DelayedBurst(
                           this.level, caster, connected, 4 + i * 2, this.radius * 0.42, this.followupDamage * 0.55F, this.stage, 2, false
                        )
                     );
               }
            }

            FireMageSpellManager.play(this.level, this.center, SoundEvents.GENERIC_EXPLODE, this.stage >= 5 ? 2.0F : 1.2F, 0.48F + this.stage * 0.035F);
            return this.finish();
         } else {
            return this.finish();
         }
      }

      private boolean finish() {
         for (FireMageSpellManager.FallingVisual meteor : this.meteors) {
            if (meteor.visual.isAlive()) {
               meteor.visual.discard();
            }
         }

         return true;
      }
   }

   private static final class OrbCollapse implements FireMageSpellManager.ActiveCast {
      private final ServerLevel level;
      private final UUID casterId;
      private final Vec3 center;
      private final int stage;
      private final double radius;
      private final float damage;
      private int age;

      private OrbCollapse(ServerLevel level, Entity caster, Vec3 center, int stage, double radius, float damage) {
         this.level = level;
         this.casterId = caster.getUUID();
         this.center = center;
         this.stage = stage;
         this.radius = radius;
         this.damage = damage;
      }

      @Override
      public boolean tick() {
         Entity caster = this.level.getEntity(this.casterId);
         if (caster != null && caster.isAlive() && caster.level() == this.level) {
            this.age++;
            AABB area = new AABB(this.center, this.center).inflate(this.radius);

            for (LivingEntity target : this.level.getEntitiesOfClass(LivingEntity.class, area, candidate -> MageCombatHelper.isValidTarget(caster, candidate))) {
               Vec3 pull = this.center.subtract(target.getBoundingBox().getCenter());
               if (pull.lengthSqr() > 0.16) {
                  target.setDeltaMovement(target.getDeltaMovement().scale(0.72).add(pull.normalize().scale(0.12)));
                  target.hurtMarked = true;
               }
            }

            if (this.age < 18) {
               return false;
            }

            FireMageSpellManager.blast(this.level, caster, this.center, this.radius, this.damage, this.stage, true);
            FireMageSpellManager.play(this.level, this.center, SoundEvents.GENERIC_EXPLODE, 1.45F, 0.58F);
            return true;
         } else {
            return true;
         }
      }
   }

   private enum ProjectileKind {
      WEAVING,
      ORB,
      LANCE;
   }
}
