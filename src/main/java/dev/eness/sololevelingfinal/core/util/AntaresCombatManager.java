package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.Tags.EntityTypes;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.AntaresVfxEventMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class AntaresCombatManager {
   public static final int JOB_ID = 10;
   public static final String IDENTITY = "antares";
   public static final String DESTRUCTION_CLAW = "Destruction Claw";
   public static final String BREATH_OF_DESTRUCTION = "Breath of Destruction";
   public static final String MONARCHS_DESCENT = "Monarch's Descent";
   public static final String SOVEREIGN_ROAR = "Sovereign Roar";
   public static final String EXTINCTION = "Extinction";
   public static final String MONARCH_MANIFESTATION = "Monarch Manifestation";
   public static final String CLAW_COOLDOWN = "antares_destruction_claw";
   public static final String BREATH_COOLDOWN = "antares_breath_of_destruction";
   public static final String DESCENT_COOLDOWN = "antares_monarchs_descent";
   public static final String ROAR_COOLDOWN = "antares_sovereign_roar";
   public static final String EXTINCTION_COOLDOWN = "antares_extinction";
   public static final String MANIFESTATION_COOLDOWN = "antares_manifestation";
   public static final String MANIFESTATION_AURA = "antares_manifestation";
   private static final int RUIN_DECAY_DELAY = 240;
   private static final int RUIN_DECAY_INTERVAL = 100;
   private static final int BREATH_WINDUP = 8;
   private static final int EXTINCTION_WINDUP = 20;
   private static final String FALL_SAFE_UNTIL = "antares_fall_safe_until";
   private static final UUID MANIFEST_SPEED = UUID.fromString("86b78529-d69c-42d8-a79a-2cf67bf35410");
   private static final UUID MANIFEST_KNOCKBACK = UUID.fromString("38c71319-1d91-448e-a7b2-02673b72a291");
   private static final Map<UUID, AntaresCombatManager.RuinState> RUIN = new HashMap<>();
   private static final Map<UUID, AntaresCombatManager.BreathState> BREATHS = new HashMap<>();
   private static final Map<UUID, AntaresCombatManager.DescentState> DESCENTS = new HashMap<>();
   private static final Map<UUID, AntaresCombatManager.RoarState> ROARS = new HashMap<>();
   private static final Map<UUID, AntaresCombatManager.ExtinctionState> EXTINCTIONS = new HashMap<>();
   private static final Map<UUID, AntaresCombatManager.ManifestationState> MANIFESTATIONS = new HashMap<>();

   private AntaresCombatManager() {
   }

   public static boolean isAntaresVessel(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables data = variables(entity);
      return (int)data.JOB == 10 && ("antares".equals(data.vesselIdentity) || data.vesselIdentity.isBlank());
   }

   public static boolean isManifested(Entity entity) {
      return entity != null && MANIFESTATIONS.containsKey(entity.getUUID());
   }

   public static int ruin(Entity entity) {
      AntaresCombatManager.RuinState state = entity == null ? null : RUIN.get(entity.getUUID());
      return state == null ? 0 : state.charges;
   }

   public static void castDestructionClaw(Entity entity) {
      if (entity instanceof ServerPlayer player && canCast(player) && ready(player, "antares_destruction_claw", "Destruction Claw")) {
         AntaresCombatManager.RuinState ruin = ruinState(player);
         boolean finisher = player.isShiftKeyDown() && AntaresCombatRules.canSpendFullRuin(ruin.charges);
         boolean manifested = isManifested(player);
         int baseMana = finisher ? 260 : 140;
         int mana = VesselManaScaling.strengthScaledCost(player, baseMana, finisher ? 0.32 : 0.22);
         if (consumeMana(player, mana)) {
            Vec3 origin = player.getEyePosition().add(player.getLookAngle().normalize().scale(0.35));
            Vec3 forward = player.getLookAngle().normalize();
            double reach = finisher ? (manifested ? 8.0 : 7.0) : (manifested ? 6.2 : 5.2);
            double halfAngle = finisher ? 0.18 : 0.34;
            double strength = TemporaryStatBonusManager.effectiveStrength(player);
            double intelligence = TemporaryStatBonusManager.effectiveIntelligence(player);
            float damage = (float)(9.0 + strength / 9.0 + intelligence / 28.0);
            damage *= finisher ? 2.15F : (manifested ? 1.18F : 1.0F);
            List<LivingEntity> targets = targetsInCone(player, origin, forward, reach, halfAngle, finisher ? 3.0 : 2.1);
            int hits = 0;

            for (LivingEntity target : targets) {
               float applied = target instanceof Player ? AntaresCombatRules.playerDamage(damage) : damage;
               if (dealDestruction(player, target, applied)) {
                  target.setSecondsOnFire(finisher ? 4 : 2);
                  if (finisher) {
                     Vec3 away = horizontal(target.position().subtract(player.position()));
                     if (away.lengthSqr() > 0.001) {
                        double control = AntaresCombatRules.bossControlScale(isBoss(target));
                        target.setDeltaMovement(target.getDeltaMovement().add(away.normalize().scale(0.65 * control)).add(0.0, 0.16 * control, 0.0));
                        target.hurtMarked = true;
                     }
                  }

                  hits++;
               }
            }

            if (finisher) {
               setRuin(player, 0, true);
            } else if (hits > 0) {
               gainRuin(player, 1);
            }

            CooldownManager.set(player, "antares_destruction_claw", finisher ? 120 : (manifested ? 58 : 72));
            player.swing(InteractionHand.MAIN_HAND, true);
            Vec3 focus = origin.add(forward.scale(reach));
            AbilityDestructionManager.line(
               player, AbilityDestructionManager.Profile.ANTARES_CLAW, origin, focus, strength + intelligence * 0.32, manifested || finisher
            );
            if (finisher) {
               AbilityDestructionManager.impact(player, AbilityDestructionManager.Profile.ANTARES_CLAW_FINISH, focus, strength + intelligence * 0.32, true);
            }

            AntaresVfxEventMessage.sendClaw(player, origin, focus, finisher, hits > 0, visualSeed(player, 17));
            player.level()
               .playSound(
                  (Player)null,
                  player.blockPosition(),
                  finisher ? SoundEvents.TRIDENT_THUNDER : SoundEvents.PLAYER_ATTACK_SWEEP,
                  SoundSource.PLAYERS,
                  finisher ? 1.05F : 0.82F,
                  finisher ? 0.58F : 0.72F
               );
         }
      }
   }

   public static void castBreathOfDestruction(Entity entity) {
      if (entity instanceof ServerPlayer player && canCast(player) && ready(player, "antares_breath_of_destruction", "Breath of Destruction")) {
         boolean manifested = isManifested(player);
         int mana = VesselManaScaling.strengthScaledCost(player, manifested ? 350 : 320, 0.28);
         if (consumeMana(player, mana)) {
            long now = player.level().getGameTime();
            int seed = visualSeed(player, 31);
            BREATHS.put(player.getUUID(), new AntaresCombatManager.BreathState(now, now + (manifested ? 42L : 34L), now + 8L, seed));
            CooldownManager.set(player, "antares_breath_of_destruction", manifested ? 135 : 160);
            Vec3[] beam = clippedBeam(player, manifested ? 23.0 : 19.0);
            AntaresVfxEventMessage.sendBreathCharge(player, beam[0], beam[1], 8, manifested, seed);
            player.level()
               .playSound((Player)null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.72F, manifested ? 0.66F : 0.74F);
         }
      }
   }

   public static void castMonarchsDescent(Entity entity) {
      if (entity instanceof ServerPlayer player && canCast(player) && ready(player, "antares_monarchs_descent", "Monarch's Descent")) {
         boolean manifested = isManifested(player);
         int mana = VesselManaScaling.strengthScaledCost(player, manifested ? 310 : 280, 0.25);
         if (consumeMana(player, mana)) {
            long now = player.level().getGameTime();
            Vec3 look = player.getLookAngle().normalize();
            Vec3 horizontal = horizontal(look);
            if (horizontal.lengthSqr() < 0.001) {
               horizontal = horizontalLook(player);
            }

            horizontal = horizontal.normalize();
            boolean diving = !player.onGround() && look.y < -0.18;
            Vec3 launch = diving
               ? new Vec3(horizontal.x * 1.35, Math.min(-0.48, look.y * 1.1), horizontal.z * 1.35)
               : new Vec3(horizontal.x * (manifested ? 1.4 : 1.2), manifested ? 0.96 : 0.82, horizontal.z * (manifested ? 1.4 : 1.2));
            int seed = visualSeed(player, 43);
            DESCENTS.put(player.getUUID(), new AntaresCombatManager.DescentState(now, now + 42L, horizontal, seed, manifested));
            CooldownManager.set(player, "antares_monarchs_descent", manifested ? 145 : 180);
            player.getPersistentData().putLong("antares_fall_safe_until", now + 85L);
            player.fallDistance = 0.0F;
            player.setDeltaMovement(launch);
            player.hurtMarked = true;
            AntaresVfxEventMessage.sendDescentLaunch(player, manifested, seed);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.92F, manifested ? 0.62F : 0.72F);
         }
      }
   }

   public static void castSovereignRoar(Entity entity) {
      if (entity instanceof ServerPlayer player && canCast(player) && ready(player, "antares_sovereign_roar", "Sovereign Roar")) {
         boolean manifested = isManifested(player);
         int mana = VesselManaScaling.strengthScaledCost(player, manifested ? 455 : 420, 0.3);
         if (consumeMana(player, mana)) {
            long now = player.level().getGameTime();
            int seed = visualSeed(player, 59);
            ROARS.put(player.getUUID(), new AntaresCombatManager.RoarState(now + 6L, seed, manifested));
            CooldownManager.set(player, "antares_sovereign_roar", manifested ? 225 : 280);
            AntaresVfxEventMessage.sendRoarCharge(player, manifested, seed);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.WARDEN_ANGRY, SoundSource.PLAYERS, 0.66F, 0.7F);
         }
      }
   }

   public static void castExtinction(Entity entity) {
      if (entity instanceof ServerPlayer player && canCast(player) && ready(player, "antares_extinction", "Extinction")) {
         AntaresCombatManager.RuinState ruin = ruinState(player);
         if (!AntaresCombatRules.canSpendFullRuin(ruin.charges)) {
            fail(player, "Extinction requires full Ruin.");
         } else {
            boolean manifested = isManifested(player);
            int mana = VesselManaScaling.strengthScaledCost(player, manifested ? 980 : 900, 0.36);
            if (consumeMana(player, mana)) {
               double range = manifested ? 38.0 : 32.0;
               Vec3 direction = player.getLookAngle().normalize();
               Vec3[] beam = clippedBeam(player, range);
               long now = player.level().getGameTime();
               int seed = visualSeed(player, 73);
               EXTINCTIONS.put(player.getUUID(), new AntaresCombatManager.ExtinctionState(beam[0], beam[1], direction, range, now, now + 20L, seed, manifested));
               setRuin(player, 0, true);
               CooldownManager.setFullDuration(player, "antares_extinction", manifested ? 520 : 600);
               AntaresVfxEventMessage.sendExtinctionCharge(player, beam[0], beam[1], manifested, seed);
               player.level().playSound((Player)null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.15F, 0.52F);
               player.displayClientMessage(Component.literal("EXTINCTION - AIM LOCKED").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
            }
         }
      }
   }

   public static void toggleManifestation(Entity entity) {
      if (entity instanceof ServerPlayer player && player.isAlive() && isAntaresVessel(player)) {
         if (isManifested(player)) {
            endManifestation(player, false, true);
         } else if (!isActionLocked(player) && ready(player, "antares_manifestation", "Monarch Manifestation")) {
            int mana = VesselManaScaling.strengthScaledCost(player, 800, 0.12);
            if (consumeMana(player, mana)) {
               long now = player.level().getGameTime();
               int seed = visualSeed(player, 89);
               MANIFESTATIONS.put(player.getUUID(), new AntaresCombatManager.ManifestationState(now + 20L, seed));
               applyManifestationAttributes(player);
               PlayerAuraSystem.setContinuous(player, "antares_manifestation", 1.45F);
               PlayerAuraSystem.burst(player, "antares_manifestation", 28, 1.9F);
               AntaresVfxEventMessage.sendManifestation(player, true, seed);
               player.level().playSound((Player)null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.9F, 0.48F);
               player.displayClientMessage(Component.literal("MONARCH MANIFESTATION - ACTIVE").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.level().isClientSide() && event.player instanceof ServerPlayer player) {
         if (!isAntaresVessel(player)) {
            if (hasRuntime(player)) {
               clearPlayer(player, true);
            }
         } else {
            long now = player.level().getGameTime();
            updateRuin(player, now);
            updateBreath(player, now);
            updateDescent(player, now);
            updateRoar(player, now);
            updateExtinction(player, now);
            updateManifestation(player, now);
            if (player.tickCount % 40 == Math.floorMod(player.getId(), 40)) {
               syncRuin(player, false);
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onAttackEntity(AttackEntityEvent event) {
      if (isActionLocked(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLeftClickBlock(LeftClickBlock event) {
      if (isActionLocked(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onRightClickItem(RightClickItem event) {
      if (isActionLocked(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onRightClickBlock(RightClickBlock event) {
      if (isActionLocked(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onEntityInteract(EntityInteract event) {
      if (isActionLocked(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLivingDamage(LivingDamageEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && isManifested(player)) {
         if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setAmount(event.getAmount() * 0.55F);
         } else if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            event.setAmount(event.getAmount() * 0.78F);
         }
      }
   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player && player.getPersistentData().getLong("antares_fall_safe_until") >= player.level().getGameTime()) {
         event.setDamageMultiplier(0.0F);
         player.fallDistance = 0.0F;
      }
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         clearPlayer(player, true);
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         clearPlayer(player, true);
      }
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         clearPlayer(player, true);
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && isAntaresVessel(player)) {
         syncRuin(player, true);
      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      RUIN.clear();
      BREATHS.clear();
      DESCENTS.clear();
      ROARS.clear();
      EXTINCTIONS.clear();
      MANIFESTATIONS.clear();
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         clearPlayer(player, true);
      }
   }

   private static void updateRuin(ServerPlayer player, long now) {
      AntaresCombatManager.RuinState state = ruinState(player);
      if (state.charges > 0 && !isManifested(player) && now - state.lastCombat >= 240L) {
         if (now >= state.nextDecay) {
            state.charges = AntaresCombatRules.clampRuin(state.charges - 1);
            state.nextDecay = now + 100L;
            syncRuin(player, true);
         }
      }
   }

   private static void updateBreath(ServerPlayer player, long now) {
      AntaresCombatManager.BreathState state = BREATHS.get(player.getUUID());
      if (state != null) {
         if (player.isAlive() && now < state.endTick) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * 0.58, motion.y, motion.z * 0.58);
            if (now >= state.nextPulse) {
               state.nextPulse += 4L;
               boolean manifested = isManifested(player);
               Vec3[] beam = clippedBeam(player, manifested ? 23.0 : 19.0);
               double radius = manifested ? 1.8 : 1.38;
               double strength = TemporaryStatBonusManager.effectiveStrength(player);
               double intelligence = TemporaryStatBonusManager.effectiveIntelligence(player);
               float damage = (float)((3.8 + strength / 38.0 + intelligence / 21.0) * (manifested ? 1.16 : 1.0));
               int hits = 0;

               for (LivingEntity target : targetsAlongSegment(player, beam[0], beam[1], radius)) {
                  float applied = target instanceof Player ? AntaresCombatRules.playerDamage(damage) : damage;
                  if (dealDestruction(player, target, applied)) {
                     target.setSecondsOnFire(manifested ? 4 : 3);
                     hits++;
                  }
               }

               if (hits > 0 && !state.ruinAwarded) {
                  state.ruinAwarded = true;
                  gainRuin(player, 1);
               }

               double beamRange = manifested ? 23.0 : 19.0;
               if (beam[0].distanceTo(beam[1]) < beamRange - 0.2) {
                  Vec3 direction = beam[1].subtract(beam[0]).normalize();
                  Vec3 drillStart = beam[1].subtract(direction.scale(manifested ? 2.6 : 2.0));
                  AbilityDestructionManager.line(
                     player,
                     AbilityDestructionManager.Profile.ANTARES_BREATH,
                     drillStart,
                     beam[1].add(direction.scale(0.65)),
                     intelligence + strength * 0.55,
                     manifested
                  );
               }

               AntaresVfxEventMessage.sendBreathStream(player, beam[0], beam[1], manifested, hits > 0, state.seed + state.pulse++ * 31);
               player.level()
                  .playSound((Player)null, BlockPos.containing(beam[1]), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.42F, 0.46F + state.pulse * 0.025F);
            }
         } else {
            finishBreath(player, state);
         }
      }
   }

   private static void finishBreath(ServerPlayer player, AntaresCombatManager.BreathState state) {
      BREATHS.remove(player.getUUID());
      Vec3[] beam = clippedBeam(player, isManifested(player) ? 23.0 : 19.0);
      AntaresVfxEventMessage.sendBreathEnd(player, beam[0], beam[1], state.seed + 101);
   }

   private static void updateDescent(ServerPlayer player, long now) {
      AntaresCombatManager.DescentState state = DESCENTS.get(player.getUUID());
      if (state != null) {
         long elapsed = now - state.startTick;
         player.fallDistance = 0.0F;
         player.getPersistentData().putLong("antares_fall_safe_until", now + 45L);
         if (elapsed <= 16L && !player.horizontalCollision) {
            Vec3 desired = horizontalLook(player);
            Vec3 blended = state.direction.scale(0.76).add(desired.scale(0.24));
            if (blended.lengthSqr() > 0.001) {
               state.direction = blended.normalize();
            }

            Vec3 movement = player.getDeltaMovement();
            double targetSpeed = state.manifested ? 1.32 : 1.12;
            Vec3 horizontalMotion = state.direction.scale(targetSpeed);
            player.setDeltaMovement(horizontalMotion.x, Math.max(-1.35, movement.y - (elapsed > 8L ? 0.06 : 0.0)), horizontalMotion.z);
            player.hurtMarked = true;
         }

         boolean contactedTerrain = elapsed >= 5L && (player.onGround() || player.horizontalCollision);
         if (contactedTerrain || now >= state.endTick) {
            finishDescent(player, state, contactedTerrain);
         }
      }
   }

   private static void finishDescent(ServerPlayer player, AntaresCombatManager.DescentState state, boolean contactedTerrain) {
      DESCENTS.remove(player.getUUID());
      Vec3 center = player.position().add(0.0, 0.12, 0.0);
      double radius = state.manifested ? 5.8 : 4.6;
      double strength = TemporaryStatBonusManager.effectiveStrength(player);
      double vitality = TemporaryStatBonusManager.effectiveVitality(player);
      float damage = (float)((13.0 + strength / 7.5 + vitality / 24.0) * (state.manifested ? 1.2 : 1.0));
      int hits = 0;

      for (LivingEntity target : targetsInRadius(player, center, radius, 3.6)) {
         float applied = target instanceof Player ? AntaresCombatRules.playerDamage(damage) : damage;
         if (dealDestruction(player, target, applied)) {
            Vec3 away = horizontal(target.position().subtract(center));
            if (away.lengthSqr() > 0.001) {
               double control = AntaresCombatRules.bossControlScale(isBoss(target));
               target.setDeltaMovement(target.getDeltaMovement().add(away.normalize().scale(0.7 * control)).add(0.0, 0.38 * control, 0.0));
               target.hurtMarked = true;
            }

            hits++;
         }
      }

      if (hits > 0) {
         gainRuin(player, 1);
      }

      if (contactedTerrain) {
         AbilityDestructionManager.impact(player, AbilityDestructionManager.Profile.ANTARES_DESCENT, center, strength + vitality * 0.32, state.manifested);
      }

      AntaresVfxEventMessage.sendDescentImpact(player, center, (float)radius, state.manifested, hits > 0, state.seed + 211);
      player.level().playSound((Player)null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2F, state.manifested ? 0.48F : 0.58F);
   }

   private static void updateRoar(ServerPlayer player, long now) {
      AntaresCombatManager.RoarState state = ROARS.get(player.getUUID());
      if (state != null && now >= state.releaseTick) {
         ROARS.remove(player.getUUID());
         Vec3 center = player.position().add(0.0, player.getBbHeight() * 0.48, 0.0);
         double radius = state.manifested ? 10.0 : 8.0;
         double strength = TemporaryStatBonusManager.effectiveStrength(player);
         double intelligence = TemporaryStatBonusManager.effectiveIntelligence(player);
         float damage = (float)((8.0 + strength / 14.0 + intelligence / 30.0) * (state.manifested ? 1.18 : 1.0));
         int hits = 0;

         for (LivingEntity target : targetsInRadius(player, center, radius, radius * 0.72)) {
            float applied = target instanceof Player ? AntaresCombatRules.playerDamage(damage) : damage;
            if (dealDestruction(player, target, applied)) {
               boolean boss = isBoss(target);
               int duration = boss ? 35 : (target instanceof Player ? 50 : 90);
               target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, boss ? 0 : 2, false, true));
               target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, boss ? 0 : 1, false, true));
               Vec3 away = horizontal(target.position().subtract(center));
               if (away.lengthSqr() > 0.001) {
                  double control = AntaresCombatRules.bossControlScale(boss);
                  target.setDeltaMovement(target.getDeltaMovement().add(away.normalize().scale(1.0 * control)).add(0.0, 0.24 * control, 0.0));
                  target.hurtMarked = true;
               }

               AntaresVfxEventMessage.sendOverawedMark(player, target, duration, state.seed + target.getId() * 17);
               hits++;
            }
         }

         int projectiles = repelProjectiles(player, center, radius);
         if (hits > 0) {
            gainRuin(player, 1);
         }

         AbilityDestructionManager.ring(
            player, AbilityDestructionManager.Profile.ANTARES_ROAR, center, radius, strength + intelligence * 0.48, state.manifested
         );
         AntaresVfxEventMessage.sendRoarRelease(player, center, (float)radius, state.manifested, hits > 0, state.seed + 307);
         player.level()
            .playSound((Player)null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.15F, projectiles > 0 ? 0.52F : 0.6F);
      }
   }

   private static void updateExtinction(ServerPlayer player, long now) {
      AntaresCombatManager.ExtinctionState state = EXTINCTIONS.get(player.getUUID());
      if (state != null) {
         Vec3 movement = player.getDeltaMovement();
         player.setDeltaMovement(movement.x * 0.24, movement.y, movement.z * 0.24);
         if (now >= state.nextPulse && state.pulse < 3) {
            fireExtinctionPulse(player, state);
            state.pulse++;
            state.nextPulse += 5L;
         }

         if (state.pulse >= 3 && now >= state.nextPulse + 1L) {
            EXTINCTIONS.remove(player.getUUID());
            AntaresVfxEventMessage.sendExtinctionAftermath(player, state.origin, state.focus, state.manifested, state.seed + 509);
         }
      }
   }

   private static void fireExtinctionPulse(ServerPlayer player, AntaresCombatManager.ExtinctionState state) {
      Vec3 intendedEnd = state.origin.add(state.direction.scale(state.range));
      BlockHitResult blockHit = player.serverLevel().clip(new ClipContext(state.origin, intendedEnd, Block.COLLIDER, Fluid.NONE, player));
      state.focus = blockHit.getType() == Type.BLOCK ? blockHit.getLocation() : intendedEnd;
      double radius = state.manifested ? 2.9 : 2.35;
      double strength = TemporaryStatBonusManager.effectiveStrength(player);
      double intelligence = TemporaryStatBonusManager.effectiveIntelligence(player);
      int hits = 0;

      for (LivingEntity target : targetsAlongSegment(player, state.origin, state.focus, radius)) {
         double health = Math.min(isBoss(target) ? 18.0 : 10.0, target.getMaxHealth() * 0.015);
         float damage = (float)((13.0 + strength / 11.0 + intelligence / 15.0 + health) * (state.manifested ? 1.18 : 1.0));
         if (target instanceof Player) {
            damage = AntaresCombatRules.playerDamage(damage) * 0.76F;
         }

         if (dealDestruction(player, target, damage)) {
            target.setSecondsOnFire(state.manifested ? 7 : 5);
            hits++;
         }
      }

      if (blockHit.getType() == Type.BLOCK) {
         double var10000;
         if (state.manifested) {
            switch (state.pulse) {
               case 0:
                  var10000 = 8.0;
                  break;
               case 1:
                  var10000 = 16.0;
                  break;
               default:
                  var10000 = 24.0;
            }
         } else {
            switch (state.pulse) {
               case 0:
                  var10000 = 6.0;
                  break;
               case 1:
                  var10000 = 12.0;
                  break;
               default:
                  var10000 = 18.0;
            }
         }

         double depth = var10000;
         Vec3 drillStart = state.focus.subtract(state.direction.scale(0.55));
         Vec3 drillEnd = state.focus.add(state.direction.scale(depth));
         AbilityDestructionManager.line(
            player,
            AbilityDestructionManager.Profile.ANTARES_EXTINCTION,
            drillStart,
            drillEnd,
            strength + intelligence * 0.75,
            state.manifested || state.pulse == 2
         );
         if (state.pulse == 2) {
            AbilityDestructionManager.impact(
               player, AbilityDestructionManager.Profile.ANTARES_EXTINCTION_FINISH, drillEnd, strength + intelligence * 0.75, true
            );
         }
      }

      AntaresVfxEventMessage.sendExtinctionPulse(player, state.origin, state.focus, state.pulse, state.manifested, hits > 0, state.seed + state.pulse * 71);
      player.level()
         .playSound((Player)null, BlockPos.containing(state.focus), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.1F, 0.48F + state.pulse * 0.08F);
   }

   private static void updateManifestation(ServerPlayer player, long now) {
      AntaresCombatManager.ManifestationState state = MANIFESTATIONS.get(player.getUUID());
      if (state != null) {
         if (!player.isAlive()) {
            endManifestation(player, true, true);
         } else if (now >= state.nextDrain) {
            state.nextDrain += 20L;
            if (!drainMana(player, 16)) {
               endManifestation(player, true, true);
               player.displayClientMessage(Component.literal("Monarch Manifestation ended: MP depleted.").withStyle(ChatFormatting.RED), true);
            }
         }
      }
   }

   private static void gainRuin(ServerPlayer player, int amount) {
      AntaresCombatManager.RuinState state = ruinState(player);
      int updated = AntaresCombatRules.gainRuin(state.charges, amount);
      if (updated == state.charges) {
         state.lastCombat = player.level().getGameTime();
         state.nextDecay = state.lastCombat + 240L;
      } else {
         setRuin(player, updated, true);
         if (updated == 3) {
            player.displayClientMessage(Component.literal("RUIN COMPLETE").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.52F, 0.66F);
         }
      }
   }

   private static void setRuin(ServerPlayer player, int charges, boolean forceSync) {
      AntaresCombatManager.RuinState state = ruinState(player);
      state.charges = AntaresCombatRules.clampRuin(charges);
      state.lastCombat = player.level().getGameTime();
      state.nextDecay = state.lastCombat + 240L;
      syncRuin(player, forceSync);
   }

   private static void syncRuin(ServerPlayer player, boolean force) {
      AntaresCombatManager.RuinState state = ruinState(player);
      int signature = state.charges | (isManifested(player) ? 16 : 0);
      if (force || signature != state.lastSyncSignature) {
         state.lastSyncSignature = signature;
         AntaresVfxEventMessage.sendRuin(player, state.charges, 3, isManifested(player));
      }
   }

   private static AntaresCombatManager.RuinState ruinState(ServerPlayer player) {
      return RUIN.computeIfAbsent(player.getUUID(), ignored -> {
         AntaresCombatManager.RuinState state = new AntaresCombatManager.RuinState();
         state.lastCombat = player.level().getGameTime();
         state.nextDecay = state.lastCombat + 240L;
         return state;
      });
   }

   private static void endManifestation(ServerPlayer player, boolean silent, boolean sendVisual) {
      AntaresCombatManager.ManifestationState removed = MANIFESTATIONS.remove(player.getUUID());
      if (removed == null) {
         removeManifestationAttributes(player);
      } else {
         removeManifestationAttributes(player);
         PlayerAuraSystem.clearContinuous(player);
         PlayerAuraSystem.burst(player, "antares_manifestation", 14, 0.8F);
         CooldownManager.setFullDuration(player, "antares_manifestation", 80);
         if (sendVisual) {
            AntaresVfxEventMessage.sendManifestation(player, false, removed.seed + 701);
         }

         if (!silent) {
            player.displayClientMessage(Component.literal("MONARCH MANIFESTATION - RELEASED").withStyle(ChatFormatting.GRAY), true);
         }

         syncRuin(player, true);
      }
   }

   private static void applyManifestationAttributes(ServerPlayer player) {
      removeManifestationAttributes(player);
      if (player.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
         player.getAttribute(Attributes.MOVEMENT_SPEED)
            .addTransientModifier(new AttributeModifier(MANIFEST_SPEED, "Antares manifestation speed", 0.12, Operation.MULTIPLY_TOTAL));
      }

      if (player.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) {
         player.getAttribute(Attributes.KNOCKBACK_RESISTANCE)
            .addTransientModifier(new AttributeModifier(MANIFEST_KNOCKBACK, "Antares manifestation stability", 0.15, Operation.ADDITION));
      }
   }

   private static void removeManifestationAttributes(ServerPlayer player) {
      if (player.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
         player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(MANIFEST_SPEED);
      }

      if (player.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) {
         player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(MANIFEST_KNOCKBACK);
      }
   }

   private static int repelProjectiles(ServerPlayer player, Vec3 center, double radius) {
      int removed = 0;

      for (Projectile projectile : player.serverLevel()
         .getEntitiesOfClass(Projectile.class, new AABB(center, center).inflate(radius), candidate -> candidate.isAlive())) {
         Entity owner = projectile.getOwner();
         if (owner != player && (owner == null || !player.isAlliedTo(owner) && !owner.isAlliedTo(player))) {
            projectile.discard();
            removed++;
         }
      }

      return removed;
   }

   private static List<LivingEntity> targetsInCone(ServerPlayer player, Vec3 origin, Vec3 direction, double reach, double minimumDot, double verticalInflation) {
      AABB search = player.getBoundingBox().expandTowards(direction.scale(reach)).inflate(reach * 0.55, verticalInflation, reach * 0.55);
      List<LivingEntity> result = new ArrayList<>();

      for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class, search, candidate -> validTarget(player, candidate))) {
         Vec3 point = target.getBoundingBox().getCenter();
         Vec3 delta = point.subtract(origin);
         double distance = delta.length();
         if (distance <= reach + target.getBbWidth() * 0.5
            && distance > 0.001
            && delta.normalize().dot(direction) >= minimumDot
            && player.hasLineOfSight(target)) {
            result.add(target);
         }
      }

      return result;
   }

   private static List<LivingEntity> targetsAlongSegment(ServerPlayer player, Vec3 start, Vec3 end, double radius) {
      AABB search = new AABB(start, end).inflate(radius + 1.0);
      List<LivingEntity> result = new ArrayList<>();

      for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class, search, candidate -> validTarget(player, candidate))) {
         double allowance = radius + target.getBbWidth() * 0.45;
         if (distanceToSegment(target.getBoundingBox().getCenter(), start, end) <= allowance) {
            result.add(target);
         }
      }

      return result;
   }

   private static List<LivingEntity> targetsInRadius(ServerPlayer player, Vec3 center, double radius, double vertical) {
      AABB search = new AABB(center, center).inflate(radius, vertical, radius);
      List<LivingEntity> result = new ArrayList<>();

      for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class, search, candidate -> validTarget(player, candidate))) {
         if (target.getBoundingBox().getCenter().distanceTo(center) <= radius + target.getBbWidth() * 0.5 && player.hasLineOfSight(target)) {
            result.add(target);
         }
      }

      return result;
   }

   private static Vec3[] clippedBeam(ServerPlayer player, double range) {
      Vec3 direction = player.getLookAngle().normalize();
      Vec3 start = player.getEyePosition().add(direction.scale(0.65));
      Vec3 end = start.add(direction.scale(range));
      BlockHitResult hit = player.serverLevel().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, player));
      if (hit.getType() == Type.BLOCK) {
         end = hit.getLocation();
      }

      return new Vec3[]{start, end};
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

   private static boolean validTarget(Player player, LivingEntity target) {
      if (target == null || target == player || !target.isAlive() || !target.isAttackable() || target.isInvulnerable() || target instanceof ArmorStand) {
         return false;
      } else if (player.isAlliedTo(target) || target.isAlliedTo(player)) {
         return false;
      } else if (ShadowMonarchManager.isOwnedShadow(target, player)) {
         return false;
      } else if (target instanceof TamableAnimal tame && player.getUUID().equals(tame.getOwnerUUID())) {
         return false;
      } else {
         return !(target instanceof Player other) ? true : !other.isCreative() && !other.isSpectator() && player.canHarmPlayer(other);
      }
   }

   private static boolean dealDestruction(ServerPlayer player, LivingEntity target, float damage) {
      if (!validTarget(player, target)) {
         return false;
      }

      target.invulnerableTime = 0;
      boolean hurt = target.hurt(player.damageSources().playerAttack(player), Math.max(0.5F, damage));
      if (hurt) {
         target.setLastHurtByPlayer(player);
      }

      return hurt;
   }

   private static boolean isBoss(LivingEntity target) {
      return !(target instanceof Player) && (target.getType().is(EntityTypes.BOSSES) || target.getMaxHealth() >= 250.0F);
   }

   private static boolean consumeMana(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      } else {
         SololevelingModVariables.PlayerVariables data = variables(player);
         if (data.MP < amount) {
            fail(player, "Not enough MP (" + amount + " required)");
            return false;
         } else {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = Math.max(0.0, capability.MP - amount);
               capability.syncPlayerVariables(player);
            });
            CooldownManager.set(player, "mana_refresh", 35);
            return true;
         }
      }
   }

   private static boolean drainMana(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      }

      SololevelingModVariables.PlayerVariables data = variables(player);
      if (data.MP < amount) {
         return false;
      }

      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.MP = Math.max(0.0, capability.MP - amount);
         capability.syncPlayerVariables(player);
      });
      CooldownManager.set(player, "mana_refresh", 30);
      return true;
   }

   private static boolean ready(ServerPlayer player, String key, String display) {
      if (!CooldownManager.isOnCooldown(player, key)) {
         return true;
      }

      fail(player, display + ": " + CooldownManager.getRemainingSeconds(player, key) + "s");
      return false;
   }

   private static boolean canCast(ServerPlayer player) {
      if (!player.isAlive() || !isAntaresVessel(player)) {
         return false;
      } else if (isActionLocked(player)) {
         fail(player, "Another destruction art is still active.");
         return false;
      } else {
         return true;
      }
   }

   private static boolean isActionLocked(Entity entity) {
      if (entity == null) {
         return false;
      }

      UUID id = entity.getUUID();
      return BREATHS.containsKey(id) || DESCENTS.containsKey(id) || ROARS.containsKey(id) || EXTINCTIONS.containsKey(id);
   }

   private static boolean hasRuntime(ServerPlayer player) {
      UUID id = player.getUUID();
      return RUIN.containsKey(id)
         || BREATHS.containsKey(id)
         || DESCENTS.containsKey(id)
         || ROARS.containsKey(id)
         || EXTINCTIONS.containsKey(id)
         || MANIFESTATIONS.containsKey(id);
   }

   private static void clearPlayer(ServerPlayer player, boolean sendVisual) {
      UUID id = player.getUUID();
      AntaresCombatManager.BreathState breath = BREATHS.remove(id);
      DESCENTS.remove(id);
      ROARS.remove(id);
      EXTINCTIONS.remove(id);
      if (breath != null && sendVisual) {
         AntaresVfxEventMessage.sendBreathEnd(player, player.position(), player.position(), breath.seed + 809);
      }

      if (MANIFESTATIONS.containsKey(id)) {
         endManifestation(player, true, sendVisual);
      } else {
         removeManifestationAttributes(player);
      }

      RUIN.remove(id);
      player.getPersistentData().remove("antares_fall_safe_until");
      if (sendVisual) {
         AntaresVfxEventMessage.sendRuin(player, 0, 3, false);
      }
   }

   private static Vec3 horizontalLook(Entity entity) {
      Vec3 value = horizontal(entity.getLookAngle());
      return value.lengthSqr() < 0.001 ? new Vec3(0.0, 0.0, 1.0) : value.normalize();
   }

   private static Vec3 horizontal(Vec3 value) {
      return new Vec3(value.x, 0.0, value.z);
   }

   private static int visualSeed(ServerPlayer player, int salt) {
      long time = player.level().getGameTime();
      return player.getUUID().hashCode() * 31 + (int)(time ^ time >>> 32) + salt;
   }

   private static void fail(ServerPlayer player, String message) {
      player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.RED), true);
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static final class BreathState {
      private final long startTick;
      private final long endTick;
      private long nextPulse;
      private final int seed;
      private int pulse;
      private boolean ruinAwarded;

      private BreathState(long startTick, long endTick, long nextPulse, int seed) {
         this.startTick = startTick;
         this.endTick = endTick;
         this.nextPulse = nextPulse;
         this.seed = seed;
      }
   }

   private static final class DescentState {
      private final long startTick;
      private final long endTick;
      private Vec3 direction;
      private final int seed;
      private final boolean manifested;

      private DescentState(long startTick, long endTick, Vec3 direction, int seed, boolean manifested) {
         this.startTick = startTick;
         this.endTick = endTick;
         this.direction = direction;
         this.seed = seed;
         this.manifested = manifested;
      }
   }

   private static final class ExtinctionState {
      private final Vec3 origin;
      private Vec3 focus;
      private final Vec3 direction;
      private final double range;
      private final long startTick;
      private long nextPulse;
      private final int seed;
      private final boolean manifested;
      private int pulse;

      private ExtinctionState(Vec3 origin, Vec3 focus, Vec3 direction, double range, long startTick, long nextPulse, int seed, boolean manifested) {
         this.origin = origin;
         this.focus = focus;
         this.direction = direction;
         this.range = range;
         this.startTick = startTick;
         this.nextPulse = nextPulse;
         this.seed = seed;
         this.manifested = manifested;
      }
   }

   private static final class ManifestationState {
      private long nextDrain;
      private final int seed;

      private ManifestationState(long nextDrain, int seed) {
         this.nextDrain = nextDrain;
         this.seed = seed;
      }
   }

   private record RoarState(long releaseTick, int seed, boolean manifested) {
   }

   private static final class RuinState {
      private int charges;
      private long lastCombat;
      private long nextDecay;
      private int lastSyncSignature = Integer.MIN_VALUE;
   }
}
