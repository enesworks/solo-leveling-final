package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.BeastVfxEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;
import dev.eness.sololevelingfinal.core.network.BeastHuntStatusMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class BeastMonarchManager {
   public static final int JOB_ID = 9;
   public static final String IDENTITY = "rakan";
   public static final String INTERCEPT_COOLDOWN = "beast_predators_intercept";
   public static final String CLAW_RIFT = "Claw-Rift Passage";
   public static final String RUBBLE_JAW = "Rubble Jaw";
   public static final String KINGS_MAUL = "King's Maul";
   public static final String FERAL_RECONSTITUTION = "Feral Reconstitution";
   public static final String WHITE_FANG_SOVEREIGN = "White Fang Sovereign";
   public static final String CLAW_RIFT_COOLDOWN = "beast_claw_rift";
   public static final String RUBBLE_JAW_COOLDOWN = "beast_rubble_jaw";
   public static final String KINGS_MAUL_COOLDOWN = "beast_kings_maul";
   public static final String RECONSTITUTION_COOLDOWN = "beast_feral_reconstitution";
   public static final String SOVEREIGN_COOLDOWN = "beast_white_fang_sovereign";
   public static final String SOVEREIGN_AURA = "beast_white_fang";
   private static final String NEXT_STRIKE = "beast_next_strike";
   private static final String LAST_STRIKE = "beast_last_strike";
   private static final String COMBO = "beast_cadence_combo";
   private static final String FALL_SAFE_UNTIL = "beast_fall_safe_until";
   private static final String AMBUSH_UNTIL = "beast_ambush_until";
   private static final String AMBUSH_TARGET = "beast_ambush_target";
   private static final String AMBUSH_KIND = "beast_ambush_kind";
   private static final String HERD_UNTIL = "beast_herd_until";
   private static final String HERD_TARGET = "beast_herd_target";
   private static final String STATUS_ACTIVE = "beast_status_active";
   private static final String PROVOKED_BY_PREFIX = "slr_beast_provoked_by_";
   private static final int MAX_HUNT = 100;
   private static final int FEAT_COOLDOWN = 120;
   private static final int OPENING_WINDOW = 160;
   private static final int HUNT_DECAY_DELAY = 100;
   private static final int CULL_DECAY_GRACE = 240;
   private static final int PROVOKED_DURATION = 1200;
   private static final UUID WHITE_FANG_SPEED_MODIFIER = UUID.fromString("f4b6d431-11f9-47c9-8d9d-1cc8829d1c56");
   private static final Map<UUID, BeastMonarchManager.HuntState> HUNTS = new HashMap<>();
   private static final Map<UUID, BeastMonarchManager.InterceptCharge> CHARGES = new HashMap<>();
   private static final Map<UUID, BeastMonarchManager.InterceptState> INTERCEPTS = new HashMap<>();
   private static final Map<UUID, BeastMonarchManager.RiftState> RIFTS = new HashMap<>();
   private static final Map<UUID, BeastMonarchManager.JawState> JAWS = new HashMap<>();
   private static final Map<UUID, UUID> PENDING_JAWS = new HashMap<>();
   private static final Map<UUID, Deque<BeastMonarchManager.Wound>> WOUNDS = new HashMap<>();
   private static final Map<UUID, BeastMonarchManager.ReconstitutionState> RECONSTITUTIONS = new HashMap<>();
   private static final Map<UUID, BeastMonarchManager.SovereignChannel> SOVEREIGN_CHANNELS = new HashMap<>();
   private static final Map<UUID, BeastMonarchManager.SovereignState> SOVEREIGNS = new HashMap<>();

   private BeastMonarchManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         clearPlayer(player);
         removeWhiteFangSpeed(player);
         PlayerAuraSystem.clearContinuous(player);
      }
   }

   public static boolean isBeastVessel(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables data = variables(entity);
      return (int)data.JOB == 9 && ("rakan".equals(data.vesselIdentity) || data.vesselIdentity.isBlank());
   }

   public static boolean isFangStance(Entity entity) {
      return isBeastVessel(entity) && variables(entity).combatmode;
   }

   public static boolean isWhiteFangSovereign(Entity entity) {
      return entity != null && SOVEREIGNS.containsKey(entity.getUUID());
   }

   public static void enhancedClawStrike(Player entity) {
      if (entity instanceof ServerPlayer player && isFangStance(player) && player.isAlive() && !isActionLocked(player)) {
         long now = player.level().getGameTime();
         if (now >= player.getPersistentData().getLong("beast_next_strike")) {
            BeastMonarchManager.HuntState state = HUNTS.computeIfAbsent(player.getUUID(), ignored -> new BeastMonarchManager.HuntState());
            boolean sovereign = isWhiteFangSovereign(player);
            int beats = sovereign ? 4 : 3;
            long lastStrike = player.getPersistentData().getLong("beast_last_strike");
            int comboWindow = sovereign ? 22 : 18;
            int combo = now - lastStrike <= comboWindow ? player.getPersistentData().getInt("beast_cadence_combo") % beats + 1 : 1;
            int baseMana = sovereign ? (combo == 4 ? 55 : (combo == 3 ? 45 : 40)) : (combo == 1 ? 40 : (combo == 2 ? 50 : 70));
            int mana = VesselManaScaling.strengthScaledCost(player, baseMana, 0.28);
            if (consumeMana(player, mana)) {
               boolean finisher = combo == beats;
               int recovery = finisher ? 7 : 5;
               player.getPersistentData().putLong("beast_next_strike", now + recovery);
               player.getPersistentData().putLong("beast_last_strike", now);
               player.getPersistentData().putInt("beast_cadence_combo", combo);
               state.combo = combo;
               player.swing(InteractionHand.MAIN_HAND, true);
               Vec3 forward = horizontalLook(player);
               double reach = (combo == 1 ? 4.35 : (combo == 2 ? 4.65 : 5.0)) + (sovereign ? 1.25 : 0.0);
               LivingEntity target = crosshairTarget(player, reach);
               boolean hit = false;
               boolean ambush = false;
               boolean savageRend = false;
               if (target != null) {
                  ambush = isAmbushReady(player, target, now);
                  boolean quarryHit = Objects.equals(state.quarryId, target.getUUID());
                  savageRend = finisher && quarryHit && Integer.bitCount(state.openingMask) >= 2;
                  double strength = TemporaryStatBonusManager.effectiveStrength(player);
                  double baseDamage = 8.0 + strength / 12.0;
                  double ratio = sovereign
                     ? (combo == 1 ? 0.62 : (combo == 2 ? 0.68 : (combo == 3 ? 0.75 : 1.05)))
                     : (combo == 1 ? 0.65 : (combo == 2 ? 0.75 : 1.0));
                  double huntBonus = quarryHit ? 1.0 + state.hunt * 0.002 : 1.0;
                  float damage = (float)(baseDamage * ratio * huntBonus * (ambush ? 1.18 : 1.0) * (savageRend ? 1.35 : 1.0));
                  if (target instanceof Player) {
                     damage *= 0.78F;
                  }

                  hit = dealPhysical(player, target, damage);
                  if (hit) {
                     if (combo == 1) {
                        prepareCadenceAndQuarry(player, target, state, now);
                     }

                     state.lastInteraction = now;
                     applyClawReaction(player, target, combo, forward);
                     spawnScar(player, target, combo, ambush);
                     if (ambush) {
                        int ambushKind = player.getPersistentData().getInt("beast_ambush_kind");
                        consumeAmbush(player);
                        awardFeat(player, state, ambushKind == 2 ? BeastMonarchManager.Feat.RIFT_AMBUSH : BeastMonarchManager.Feat.PURSUIT, now);
                     }

                     if (isHerdReady(player, target, now)) {
                        consumeHerd(player);
                        awardFeat(player, state, BeastMonarchManager.Feat.HERD, now);
                     }

                     if (savageRend) {
                        state.openingMask = 0;
                        BeastVfxEntity.spawnAttached(player.serverLevel(), target, 4, 16733512, 4849664, 1.45F, 1.62F, 0.0F, 18, 2);
                        player.level().playSound((Player)null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.68F, 0.72F);
                     }

                     if (finisher) {
                        checkAngleBreak(player, target, state, now);
                     }
                  }
               }

               applyCadenceMovement(player, state, combo, forward);
               spawnClawArc(player, combo, hit, ambush);
               player.level()
                  .playSound(
                     (Player)null,
                     player.blockPosition(),
                     finisher ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_SWEEP,
                     SoundSource.PLAYERS,
                     finisher ? 0.95F : 0.72F,
                     finisher ? 0.72F : 0.9F + combo * 0.08F
                  );
               syncStatus(player, state, true);
            }
         }
      }
   }

   public static void beginPredatorsIntercept(Entity entity) {
      if (entity instanceof ServerPlayer player
         && isFangStance(player)
         && player.isAlive()
         && !CHARGES.containsKey(player.getUUID())
         && !INTERCEPTS.containsKey(player.getUUID())) {
         if (ready(player, "beast_predators_intercept")) {
            BeastMonarchManager.HuntState state = HUNTS.computeIfAbsent(player.getUUID(), ignored -> new BeastMonarchManager.HuntState());
            LivingEntity quarry = quarry(player, state);
            LivingEntity target = quarry != null && canReadForIntercept(player, quarry) ? quarry : null;
            CHARGES.put(
               player.getUUID(),
               new BeastMonarchManager.InterceptCharge(
                  target == null ? null : target.getUUID(),
                  player.level().getGameTime(),
                  player.position(),
                  target == null ? 0.0 : CombatRangeHelper.surfaceDistance(player, target)
               )
            );
            player.displayClientMessage(
               Component.literal(target == null ? "PREDATOR'S BOUND" : "QUARRY INTERCEPT")
                  .withStyle(target == null ? ChatFormatting.GRAY : ChatFormatting.DARK_RED, ChatFormatting.BOLD),
               true
            );
            if (target != null) {
               BeastVfxEntity.spawnAttached(player.serverLevel(), target, 3, 13637656, 2752512, 0.82F, 1.15F, 0.0F, 15, 0);
            }
         }
      }
   }

   public static void releasePredatorsIntercept(Entity entity, int pressedMs) {
      if (entity instanceof ServerPlayer player && isFangStance(player)) {
         BeastMonarchManager.InterceptCharge charge = CHARGES.remove(player.getUUID());
         if (charge != null && ready(player, "beast_predators_intercept")) {
            BeastMonarchManager.HuntState hunt = HUNTS.computeIfAbsent(player.getUUID(), ignored -> new BeastMonarchManager.HuntState());
            LivingEntity target = charge.targetId == null ? null : livingEntity(player.serverLevel(), charge.targetId);
            if (target == null
               || !validTarget(player, target)
               || !Objects.equals(hunt.quarryId, target.getUUID())
               || CombatRangeHelper.surfaceDistance(player, target) > 55.0) {
               target = null;
            }

            int baseMana = target == null ? 180 : 260;
            int mana = VesselManaScaling.strengthScaledCost(player, baseMana, 0.34);
            if (consumeMana(player, mana)) {
               double power = Mth.clamp(0.55 + Math.max(0, pressedMs) / 1000.0, 0.55, 1.25);
               Vec3 destination;
               Vec3 direction;
               double maximumDistance;
               if (target != null) {
                  Vec3 preyForward = horizontal(target.getDeltaMovement());
                  if (preyForward.lengthSqr() < 0.01) {
                     preyForward = horizontal(target.getLookAngle());
                  }

                  if (preyForward.lengthSqr() < 0.01) {
                     preyForward = horizontalLook(player);
                  }

                  preyForward = preyForward.normalize();
                  double side = interceptSide(player, target, preyForward);
                  Vec3 right = new Vec3(-preyForward.z, 0.0, preyForward.x).scale(side);
                  double leadTicks = 4.0 + power * 5.0;
                  Vec3 predicted = target.position().add(horizontal(target.getDeltaMovement()).scale(leadTicks));
                  destination = predicted.add(right.scale(2.15 + target.getBbWidth() * 0.45));
                  maximumDistance = Math.min(18.0, player.position().distanceTo(destination) + 1.5);
                  direction = interceptDirection(player.position(), destination, 0.28);
               } else {
                  direction = clampVertical(player.getLookAngle(), 0.34).normalize();
                  maximumDistance = 8.0 + power * 6.0;
                  destination = player.position().add(direction.scale(maximumDistance));
               }

               double speed = (target == null ? 1.3 : 1.55) + power * (target == null ? 0.48 : 0.58);
               int lifetime = Mth.clamp((int)Math.ceil(maximumDistance / speed) + 4, 7, 18);
               long now = player.level().getGameTime();
               BeastMonarchManager.InterceptState state = new BeastMonarchManager.InterceptState(
                  target == null ? null : target.getUUID(), direction, destination, player.position(), speed, maximumDistance, now + lifetime
               );
               INTERCEPTS.put(player.getUUID(), state);
               CooldownManager.set(player, "beast_predators_intercept", target == null ? 45 : 65);
               player.getPersistentData().putLong("beast_fall_safe_until", now + lifetime + 45L);
               player.fallDistance = 0.0F;
               player.setDeltaMovement(direction.scale(speed));
               player.hurtMarked = true;
               BeastVfxEntity.spawnAttached(
                  player.serverLevel(), player, 2, 14882852, 4325381, 1.6F + (float)power * 0.35F, 3.4F + (float)power, 0.0F, lifetime, target == null ? 0 : 1
               );
               player.level()
                  .playSound((Player)null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 0.9F, target == null ? 0.7F : 0.58F);
            }
         }
      }
   }

   public static void castClawRift(Entity entity) {
      if (entity instanceof ServerPlayer player && canCastBeastSkill(player) && ready(player, "beast_claw_rift", "Claw-Rift Passage")) {
         boolean sovereign = isWhiteFangSovereign(player);
         int mana = VesselManaScaling.strengthScaledCost(player, sovereign ? 250 : 220, 0.26);
         if (consumeMana(player, mana)) {
            Vec3 direction = horizontalLook(player);
            Vec3 start = player.position();
            Vec3 destination = sweptDestination(player, direction, sovereign ? 12.0 : 9.0);
            double strength = TemporaryStatBonusManager.effectiveStrength(player);
            float damage = (float)((10.0 + strength / 8.0) * (sovereign ? 1.28 : 1.0));
            int hits = damageAlongPath(player, start, destination, sovereign ? 1.65 : 1.35, damage, 10);
            player.teleportTo(destination.x, destination.y, destination.z);
            player.setDeltaMovement(direction.scale(0.48));
            player.hurtMarked = true;
            player.fallDistance = 0.0F;
            player.getPersistentData().putLong("beast_fall_safe_until", player.level().getGameTime() + 35L);
            CooldownManager.set(player, "beast_claw_rift", sovereign ? 90 : 120);
            Vec3 entry = start.add(0.0, player.getBbHeight() * 0.52, 0.0);
            Vec3 exit = destination.add(0.0, player.getBbHeight() * 0.52, 0.0);
            Vec3 middle = entry.add(exit).scale(0.5);
            BeastVfxEntity.spawn(player.serverLevel(), entry, direction, 5, 16744751, 3803136, 1.05F, 2.45F, 0.0F, 15, 0);
            BeastVfxEntity.spawn(player.serverLevel(), exit, direction.scale(-1.0), 5, 16757578, 2884864, 1.1F, 2.55F, 0.0F, 15, 1);
            BeastVfxEntity.spawn(player.serverLevel(), middle, direction, 0, 16747044, 4000000, sovereign ? 3.8F : 3.2F, sovereign ? 3.3F : 2.8F, 0.0F, 14, 3);
            AbilityDestructionManager.fissure(
               player, AbilityDestructionManager.Profile.BEAST_CLAW_RIFT, start, direction, start.distanceTo(destination), strength, sovereign
            );
            player.swing(InteractionHand.MAIN_HAND, true);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2, SoundSource.PLAYERS, 1.0F, hits > 0 ? 0.72F : 0.9F);
         }
      }
   }

   public static void castRubbleJaw(Entity entity) {
      if (entity instanceof ServerPlayer player && canCastBeastSkill(player) && ready(player, "beast_rubble_jaw", "Rubble Jaw")) {
         boolean sovereign = isWhiteFangSovereign(player);
         int mana = VesselManaScaling.strengthScaledCost(player, sovereign ? 340 : 300, 0.28);
         if (consumeMana(player, mana)) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            BlockHitResult hit = player.serverLevel().clip(new ClipContext(eye, eye.add(look.scale(12.0)), Block.COLLIDER, Fluid.NONE, player));
            Vec3 center = hit.getType() == Type.BLOCK
               ? hit.getLocation().add(0.0, 0.12, 0.0)
               : player.position().add(horizontalLook(player).scale(7.0)).add(0.0, 0.2, 0.0);
            double radius = sovereign ? 5.75 : 4.5;
            double strength = TemporaryStatBonusManager.effectiveStrength(player);
            float damage = (float)((12.0 + strength / 7.0) * (sovereign ? 1.25 : 1.0));
            int hits = 0;

            for (LivingEntity target : player.serverLevel()
               .getEntitiesOfClass(
                  LivingEntity.class,
                  new AABB(center.x - radius, center.y - 2.0, center.z - radius, center.x + radius, center.y + 3.5, center.z + radius),
                  candidate -> validTarget(player, candidate)
               )) {
               if (!(target.getBoundingBox().getCenter().distanceTo(center) > radius + target.getBbWidth() * 0.5)
                  && player.hasLineOfSight(target)
                  && dealPhysical(player, target, target instanceof Player ? damage * 0.72F : damage)) {
                  Vec3 away = horizontal(target.position().subtract(center));
                  if (away.lengthSqr() < 0.01) {
                     away = horizontalLook(player);
                  }

                  double resistance = Math.max(0.2, 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                  target.setDeltaMovement(
                     target.getDeltaMovement().add(away.normalize().scale(0.42 * resistance)).add(0.0, (sovereign ? 0.72 : 0.55) * resistance, 0.0)
                  );
                  target.hurtMarked = true;
                  hits++;
               }
            }

            CooldownManager.set(player, "beast_rubble_jaw", sovereign ? 130 : 170);
            BeastVfxEntity.spawn(
               player.serverLevel(),
               center,
               horizontalLook(player),
               6,
               16751150,
               4066304,
               sovereign ? 3.1F : 2.55F,
               sovereign ? 6.2F : 5.1F,
               0.0F,
               20,
               hits > 0 ? 1 : 0
            );
            AbilityDestructionManager.impact(player, AbilityDestructionManager.Profile.BEAST_RUBBLE_JAW, center, strength, sovereign);
            player.level()
               .playSound((Player)null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.15F, sovereign ? 0.58F : 0.68F);
         }
      }
   }

   public static void castKingsMaul(Entity entity) {
      if (entity instanceof ServerPlayer player && canCastBeastSkill(player) && ready(player, "beast_kings_maul", "King's Maul")) {
         boolean sovereign = isWhiteFangSovereign(player);
         int mana = VesselManaScaling.strengthScaledCost(player, sovereign ? 430 : 380, 0.34);
         if (consumeMana(player, mana)) {
            LivingEntity target = crosshairTarget(player, sovereign ? 11.0 : 9.0);
            if (target == null) {
               target = forwardTarget(player, sovereign ? 10.0 : 8.0, 0.58);
            }

            Vec3 direction = target == null ? horizontalLook(player) : horizontal(target.getBoundingBox().getCenter().subtract(player.position())).normalize();
            double lunge = target == null ? 4.5 : Mth.clamp(CombatRangeHelper.surfaceDistance(player, target) - 1.0, 0.0, sovereign ? 6.5 : 5.0);
            Vec3 destination = sweptDestination(player, direction, lunge);
            player.teleportTo(destination.x, destination.y, destination.z);
            player.setDeltaMovement(direction.scale(0.38));
            player.hurtMarked = true;
            if (target == null || !validTarget(player, target) || CombatRangeHelper.surfaceDistance(player, target) > 4.0) {
               target = forwardTarget(player, 4.5, 0.35);
            }

            double strength = TemporaryStatBonusManager.effectiveStrength(player);
            double perception = variables(player).perception;
            float damage = (float)((18.0 + strength / 5.0 + perception / 25.0) * (sovereign ? 1.3 : 1.0));
            if (target != null && dealPhysical(player, target, target instanceof Player ? damage * 0.68F : damage)) {
               target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, sovereign ? 35 : 24, sovereign ? 2 : 1, false, true));
               Vec3 movement = target.getDeltaMovement();
               target.setDeltaMovement(movement.x * 0.35, Math.max(0.16, movement.y), movement.z * 0.35);
               target.hurtMarked = true;
               BeastVfxEntity.spawnAttached(
                  player.serverLevel(),
                  target,
                  7,
                  16751150,
                  4328192,
                  Mth.clamp(target.getBbWidth() * 1.5F, 1.65F, 3.6F),
                  Mth.clamp(target.getBbHeight() * 0.72F, 1.8F, 4.0F),
                  0.0F,
                  18,
                  2
               );
            }

            CooldownManager.set(player, "beast_kings_maul", sovereign ? 150 : 200);
            player.swing(InteractionHand.MAIN_HAND, true);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.RAVAGER_ATTACK, SoundSource.PLAYERS, 1.1F, sovereign ? 0.56F : 0.66F);
         }
      }
   }

   public static void castFeralReconstitution(Entity entity) {
      if (entity instanceof ServerPlayer player && canCastBeastSkill(player) && ready(player, "beast_feral_reconstitution", "Feral Reconstitution")) {
         boolean sovereign = isWhiteFangSovereign(player);
         int mana = VesselManaScaling.strengthScaledCost(player, sovereign ? 300 : 260, 0.2);
         if (consumeMana(player, mana)) {
            double vitality = variables(player).Vitality;
            float missing = player.getMaxHealth() - player.getHealth();
            float heal = (float)Math.min(missing, (6.0 + vitality / 10.0) * (sovereign ? 1.35 : 1.0));
            if (heal > 0.0F) {
               player.heal(heal);
            }

            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, sovereign ? 180 : 120, sovereign ? 1 : 0, false, true));
            CooldownManager.set(player, "beast_feral_reconstitution", sovereign ? 200 : 240);
            BeastVfxEntity.spawnAttached(
               player.serverLevel(), player, 8, 16773841, 13982232, sovereign ? 1.8F : 1.45F, sovereign ? 2.0F : 1.65F, 0.0F, 22, sovereign ? 2 : 0
            );
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.75F, sovereign ? 0.7F : 0.82F);
         }
      }
   }

   public static void castWhiteFangSovereign(Entity entity) {
      if (entity instanceof ServerPlayer player && player.isAlive() && isBeastVessel(player)) {
         if (isWhiteFangSovereign(player)) {
            endSovereign(player, false);
         } else if (canCastBeastSkill(player) && ready(player, "beast_white_fang_sovereign", "White Fang Sovereign")) {
            int mana = VesselManaScaling.strengthScaledCost(player, 600, 0.18);
            if (consumeMana(player, mana)) {
               long now = player.level().getGameTime();
               SOVEREIGNS.put(player.getUUID(), new BeastMonarchManager.SovereignState(now + 400L, now + 400L));
               CooldownManager.set(player, "beast_white_fang_sovereign", 600);
               applyWhiteFangSpeed(player);
               player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.combatmode = true;
                  capability.syncPlayerVariables(player);
               });
               PlayerAuraSystem.setContinuous(player, "beast_white_fang", 1.35F);
               PlayerAuraSystem.burst(player, "beast_white_fang", 30, 1.8F);
               BeastVfxEntity.spawnAttached(player.serverLevel(), player, 9, 16774358, 14703640, 3.0F, 3.4F, 0.0F, 28, 1);
               player.level().playSound((Player)null, player.blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.78F, 1.35F);
               player.displayClientMessage(Component.literal("WHITE FANG SOVEREIGN - ACTIVE").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.level().isClientSide() && event.player instanceof ServerPlayer player) {
         if (isBeastVessel(player)) {
            BeastMonarchManager.HuntState state = HUNTS.computeIfAbsent(player.getUUID(), ignored -> new BeastMonarchManager.HuntState());
            long now = player.level().getGameTime();
            updateQuarry(player, state, now);
            updateHuntDecay(player, state, now);
            updateOpenings(state, now);
            updateCharge(player, now);
            updateIntercept(player, now);
            updateRift(player, state, now);
            updateJaw(player, state, now);
            updateReconstitution(player, state, now);
            updateSovereignChannel(player, state, now);
            updateSovereign(player, now);
            phaseOwnRubble(player, now);
            int comboWindow = isWhiteFangSovereign(player) ? 22 : 18;
            if (state.combo != 0 && now - player.getPersistentData().getLong("beast_last_strike") > comboWindow) {
               state.combo = 0;
            }

            if (!isFangStance(player)) {
               CHARGES.remove(player.getUUID());
               if (INTERCEPTS.containsKey(player.getUUID())) {
                  finishIntercept(player, INTERCEPTS.get(player.getUUID()), false);
               }

               state.combo = 0;
            }

            if (player.getPersistentData().getLong("beast_ambush_until") < now) {
               consumeAmbush(player);
            }

            syncStatus(player, state, false);
         } else {
            if (player.getPersistentData().getBoolean("beast_status_active")
               || HUNTS.containsKey(player.getUUID())
               || CHARGES.containsKey(player.getUUID())
               || INTERCEPTS.containsKey(player.getUUID())
               || RIFTS.containsKey(player.getUUID())
               || JAWS.containsKey(player.getUUID())
               || RECONSTITUTIONS.containsKey(player.getUUID())
               || SOVEREIGNS.containsKey(player.getUUID())
               || SOVEREIGN_CHANNELS.containsKey(player.getUUID())) {
               clearPlayer(player);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onAttackEntity(AttackEntityEvent event) {
      if (isFangStance(event.getEntity()) || isActionLocked(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onLeftClickBlock(LeftClickBlock event) {
      if (isFangStance(event.getEntity()) || isActionLocked(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onRightClickItem(RightClickItem event) {
      Player player = event.getEntity();
      if (isActionLocked(player) || isWhiteFangSovereign(player)) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onRightClickBlock(RightClickBlock event) {
      Player player = event.getEntity();
      if (isActionLocked(player) || isWhiteFangSovereign(player) && event.getItemStack().getItem() instanceof BlockItem) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onEntityInteract(EntityInteract event) {
      if (isActionLocked(event.getEntity()) || isWhiteFangSovereign(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLivingDamage(LivingDamageEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer attacker
         && isBeastVessel(attacker)
         && event.getSource().getDirectEntity() == attacker
         && event.getEntity() != attacker) {
         markDirectlyProvoked(attacker, event.getEntity());
      }

      if (event.getEntity() instanceof ServerPlayer player && !(event.getAmount() <= 0.0F)) {
         if (RECONSTITUTIONS.containsKey(player.getUUID())) {
            interruptReconstitution(player);
         }

         if (SOVEREIGN_CHANNELS.containsKey(player.getUUID())) {
            interruptSovereignChannel(player);
         }

         if (isBeastVessel(player)) {
            BeastMonarchManager.HuntState hunt = HUNTS.get(player.getUUID());
            Entity attacker = event.getSource().getEntity();
            if (hunt != null && hunt.quarryId != null && attacker != null && hunt.quarryId.equals(attacker.getUUID())) {
               double recoverable = event.getAmount();
               if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                  recoverable *= 0.5;
               }

               Deque<BeastMonarchManager.Wound> wounds = WOUNDS.computeIfAbsent(player.getUUID(), ignored -> new ArrayDeque<>());
               wounds.addLast(new BeastMonarchManager.Wound(attacker.getUUID(), recoverable, player.level().getGameTime()));

               while (!wounds.isEmpty() && player.level().getGameTime() - wounds.peekFirst().tick > 80L) {
                  wounds.removeFirst();
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player && player.getPersistentData().getLong("beast_fall_safe_until") >= player.level().getGameTime()) {
         event.setDamageMultiplier(0.15F);
         player.fallDistance *= 0.25F;
      }
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer player && isBeastVessel(player)) {
         BeastMonarchManager.HuntState state = HUNTS.get(player.getUUID());
         if (state != null && Objects.equals(state.quarryId, event.getEntity().getUUID())) {
            long now = player.level().getGameTime();
            awardFeat(player, state, BeastMonarchManager.Feat.CULL, now);
            state.quarryId = null;
            state.lastInteraction = now;
            state.decayGraceUntil = now + 240L;
            syncStatus(player, state, true);
         }
      }

      if (event.getEntity() instanceof ServerPlayer dead) {
         clearPlayer(dead);
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         clearPlayer(player);
      }
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         clearPlayer(player);
      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      HUNTS.clear();
      CHARGES.clear();
      INTERCEPTS.clear();
      RIFTS.clear();
      JAWS.clear();
      PENDING_JAWS.clear();
      WOUNDS.clear();
      RECONSTITUTIONS.clear();
      SOVEREIGN_CHANNELS.clear();
      SOVEREIGNS.clear();
   }

   private static void prepareCadenceAndQuarry(ServerPlayer player, LivingEntity target, BeastMonarchManager.HuntState state, long now) {
      LivingEntity current = quarry(player, state);
      if (current == null) {
         claimQuarry(player, target, state, now, false);
      } else if (current != target && player.isShiftKeyDown()) {
         claimQuarry(player, target, state, now, true);
      }

      if (Objects.equals(state.quarryId, target.getUUID())) {
         state.cadenceTarget = target.getUUID();
         state.cadenceStartVector = relativeAttackVector(player, target);
      }
   }

   private static void claimQuarry(ServerPlayer player, LivingEntity target, BeastMonarchManager.HuntState state, long now, boolean switching) {
      if (validTarget(player, target)) {
         LivingEntity current = quarry(player, state);
         if (switching && current != null && current != target) {
            if (now < state.swapLockUntil) {
               player.displayClientMessage(Component.literal("Quarry swap is locked.").withStyle(ChatFormatting.RED), true);
               return;
            }

            if (state.hunt < 15) {
               player.displayClientMessage(Component.literal("15 Hunt is required to abandon living Quarry.").withStyle(ChatFormatting.RED), true);
               return;
            }

            state.hunt -= 15;
            state.swapLockUntil = now + 80L;
         }

         state.quarryId = target.getUUID();
         state.lastInteraction = now;
         state.lastLineOfSight = now;
         state.cadenceTarget = null;
         state.cadenceStartVector = null;
         BeastVfxEntity.spawnAttached(player.serverLevel(), target, 3, 14818340, 3145728, 0.94F, 1.25F, 0.0F, 22, 0);
         player.level().playSound((Player)null, target.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 0.9F, 0.62F);
         player.displayClientMessage(
            Component.literal("QUARRY: ")
               .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
               .append(target.getDisplayName().copy().withStyle(ChatFormatting.WHITE)),
            true
         );
         if (target instanceof ServerPlayer quarryPlayer) {
            quarryPlayer.displayClientMessage(
               Component.literal("You have been marked as Quarry.").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true
            );
         }
      }
   }

   private static void checkAngleBreak(ServerPlayer player, LivingEntity target, BeastMonarchManager.HuntState state, long now) {
      if (Objects.equals(state.quarryId, target.getUUID()) && Objects.equals(state.cadenceTarget, target.getUUID()) && state.cadenceStartVector != null) {
         Vec3 current = relativeAttackVector(player, target);
         double dot = Mth.clamp(state.cadenceStartVector.dot(current), -1.0, 1.0);
         double angle = Math.toDegrees(Math.acos(dot));
         state.cadenceTarget = null;
         state.cadenceStartVector = null;
         if (angle >= 60.0) {
            awardFeat(player, state, BeastMonarchManager.Feat.ANGLE_BREAK, now);
         }
      }
   }

   private static void awardFeat(ServerPlayer player, BeastMonarchManager.HuntState state, BeastMonarchManager.Feat feat, long now) {
      if (state.quarryId != null && state.lastFeat != feat && now >= state.featReadyAt.getOrDefault(feat, 0L)) {
         if (now >= state.satedUntil) {
            BeastMonarchManager.SovereignState sovereign = SOVEREIGNS.get(player.getUUID());
            if (sovereign != null) {
               state.lastFeat = feat;
               state.featReadyAt.put(feat, now + 120L);
               state.lastInteraction = now;
               if (now > state.openingExpiresAt) {
                  state.openingMask = 0;
                  state.openingExpiresAt = now + 160L;
               }

               if (state.openingMask == 0) {
                  state.openingExpiresAt = now + 160L;
               }

               state.openingMask = state.openingMask | feat.bit;
               if (sovereign.extendedFeats.add(feat) && sovereign.extendedFeats.size() <= 4) {
                  sovereign.expiresAt = Math.min(sovereign.hardExpiresAt, sovereign.expiresAt + 20L);
                  player.displayClientMessage(Component.literal(feat.display + "  SOVEREIGN +1s").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), true);
               }
            } else {
               if (now - state.gainWindowStart >= 20L) {
                  state.gainWindowStart = now;
                  state.gainedThisSecond = 0;
               }

               int gained = Math.min(feat.hunt, Math.max(0, 18 - state.gainedThisSecond));
               if (gained > 0) {
                  state.hunt = Math.min(100, state.hunt + gained);
                  state.gainedThisSecond += gained;
                  state.lastFeat = feat;
                  state.featReadyAt.put(feat, now + 120L);
                  state.lastInteraction = now;
                  state.decayGraceUntil = Math.max(state.decayGraceUntil, now + 100L);
                  if (now > state.openingExpiresAt) {
                     state.openingMask = 0;
                     state.openingExpiresAt = now + 160L;
                  }

                  if (state.openingMask == 0) {
                     state.openingExpiresAt = now + 160L;
                  }

                  state.openingMask = state.openingMask | feat.bit;
                  player.displayClientMessage(
                     Component.literal(feat.display + "  +" + gained + " HUNT").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true
                  );
                  if (Integer.bitCount(state.openingMask) >= 2) {
                     LivingEntity target = quarry(player, state);
                     if (target != null) {
                        BeastVfxEntity.spawnAttached(player.serverLevel(), target, 4, 16726578, 5898240, 1.15F, 1.38F, 0.0F, 16, 0);
                     }
                  }
               }
            }
         }
      }
   }

   private static void updateQuarry(ServerPlayer player, BeastMonarchManager.HuntState state, long now) {
      LivingEntity quarry = quarry(player, state);
      if (quarry == null) {
         state.quarryId = null;
      } else {
         if (player.hasLineOfSight(quarry)) {
            state.lastLineOfSight = now;
         }

         double distance = CombatRangeHelper.surfaceDistance(player, quarry);
         if (distance > 24.0 && now - state.lastLineOfSight > 80L || now - state.lastInteraction > 200L) {
            state.quarryId = null;
            state.cadenceTarget = null;
            state.cadenceStartVector = null;
         }
      }
   }

   private static void updateHuntDecay(ServerPlayer player, BeastMonarchManager.HuntState state, long now) {
      if (state.hunt > 0 && now > state.decayGraceUntil && now - state.lastInteraction > 100L) {
         if (now % 5L == 0L) {
            state.hunt = Math.max(0, state.hunt - 1);
            if (state.hunt == 0) {
               state.openingMask = 0;
               state.lastFeat = null;
            }
         }
      }
   }

   private static void updateOpenings(BeastMonarchManager.HuntState state, long now) {
      if (state.openingMask != 0 && now > state.openingExpiresAt) {
         state.openingMask = 0;
      }
   }

   private static void updateCharge(ServerPlayer player, long now) {
      BeastMonarchManager.InterceptCharge charge = CHARGES.get(player.getUUID());
      if (charge != null && now - charge.startedAt > 100L) {
         CHARGES.remove(player.getUUID());
      }
   }

   private static void updateIntercept(ServerPlayer player, long now) {
      BeastMonarchManager.InterceptState state = INTERCEPTS.get(player.getUUID());
      if (state != null) {
         if (player.isAlive() && isFangStance(player) && now < state.expiresAt && !player.horizontalCollision) {
            Vec3 movement = state.direction.scale(state.speed);
            player.setDeltaMovement(movement);
            player.fallDistance = 0.0F;
            player.hurtMarked = true;
            double traveled = player.position().distanceTo(state.start);
            boolean reachedDestination = player.position().distanceToSqr(state.destination) <= 1.2;
            if (state.targetId != null) {
               LivingEntity target = livingEntity(player.serverLevel(), state.targetId);
               if (target == null || !validTarget(player, target)) {
                  finishIntercept(player, state, false);
                  return;
               }

               if (CombatRangeHelper.surfaceDistance(player, target) <= 1.25) {
                  finishIntercept(player, state, true);
                  return;
               }
            }

            if (traveled >= state.maximumDistance || reachedDestination) {
               finishIntercept(player, state, true);
            }
         } else {
            finishIntercept(player, state, true);
         }
      }
   }

   private static void updateRift(ServerPlayer player, BeastMonarchManager.HuntState hunt, long now) {
      BeastMonarchManager.RiftState rift = RIFTS.get(player.getUUID());
      if (rift != null) {
         if (player.isAlive() && now < rift.expiresAt) {
            if (now == rift.armedAt) {
               player.level()
                  .playSound((Player)null, BlockPos.containing(rift.entry), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.55F, 1.45F);
            }

            if (now >= rift.armedAt) {
               processRiftDirection(
                  player, hunt, rift, rift.entry, rift.entryNormal, rift.exit, rift.exitNormal, rift.forwardSides, rift.usedForward, now, false
               );
               if (rift.twoWay) {
                  processRiftDirection(
                     player,
                     hunt,
                     rift,
                     rift.exit,
                     rift.exitNormal.scale(-1.0),
                     rift.entry,
                     rift.entryNormal.scale(-1.0),
                     rift.reverseSides,
                     rift.usedReverse,
                     now,
                     true
                  );
               }
            }
         } else {
            RIFTS.remove(player.getUUID(), rift);
         }
      }
   }

   private static void processRiftDirection(
      ServerPlayer owner,
      BeastMonarchManager.HuntState hunt,
      BeastMonarchManager.RiftState rift,
      Vec3 source,
      Vec3 sourceNormal,
      Vec3 destination,
      Vec3 destinationNormal,
      Map<UUID, Double> previousSides,
      Set<UUID> used,
      long now,
      boolean reverse
   ) {
      AABB scan = new AABB(source.x - 1.45, source.y - 1.7, source.z - 1.45, source.x + 1.45, source.y + 1.7, source.z + 1.45);
      int checked = 0;

      for (LivingEntity traveller : owner.serverLevel()
         .getEntitiesOfClass(LivingEntity.class, scan, candidate -> canUseRift(candidate) && !used.contains(candidate.getUUID()))) {
         if (++checked > 20) {
            break;
         }

         long immunity = rift.immunityUntil.getOrDefault(traveller.getUUID(), 0L);
         if (now >= immunity) {
            Vec3 relative = traveller.getBoundingBox().getCenter().subtract(source);
            double side = relative.dot(sourceNormal);
            double previous = previousSides.getOrDefault(traveller.getUUID(), side - traveller.getDeltaMovement().dot(sourceNormal));
            previousSides.put(traveller.getUUID(), side);
            Vec3 lateral = relative.subtract(sourceNormal.scale(side));
            if (!(previous > -0.04)
               && !(side < -0.04)
               && !(traveller.getDeltaMovement().dot(sourceNormal) <= 0.025)
               && !(Math.abs(relative.y) > 1.55)
               && !(horizontal(lateral).length() > 1.05)) {
               Vec3 safe = safeRiftExit(owner.serverLevel(), traveller, destination, destinationNormal);
               if (safe != null) {
                  Vec3 velocity = rotateMomentum(traveller.getDeltaMovement(), sourceNormal, destinationNormal);
                  float yawDelta = (float)Math.toDegrees(horizontalAngle(destinationNormal) - horizontalAngle(sourceNormal));
                  traveller.teleportTo(safe.x, safe.y, safe.z);
                  traveller.setYRot(traveller.getYRot() + yawDelta);
                  traveller.setYHeadRot(traveller.getYHeadRot() + yawDelta);
                  traveller.setDeltaMovement(velocity);
                  traveller.hurtMarked = true;
                  used.add(traveller.getUUID());
                  rift.immunityUntil.put(traveller.getUUID(), now + 10L);
                  owner.level()
                     .playSound(
                        (Player)null, BlockPos.containing(destination), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, reverse ? 0.72F : 0.88F
                     );
                  if (traveller == owner && !reverse && source.distanceTo(destination) >= 5.0 && angleDegrees(sourceNormal, destinationNormal) >= 60.0) {
                     LivingEntity quarry = quarry(owner, hunt);
                     if (quarry != null) {
                        owner.getPersistentData().putLong("beast_ambush_until", now + 24L);
                        owner.getPersistentData().putUUID("beast_ambush_target", quarry.getUUID());
                        owner.getPersistentData().putInt("beast_ambush_kind", 2);
                     }
                  }
               }
            }
         }
      }
   }

   private static void placeRubbleJaw(ServerPlayer player, ServerLevel castLevel, UUID castId, BlockPos center, Vec3 facing, List<BlockPos> planned) {
      if (player.isAlive() && isBeastVessel(player) && player.serverLevel() == castLevel && castId.equals(PENDING_JAWS.get(player.getUUID()))) {
         PENDING_JAWS.remove(player.getUUID());
         List<BlockPos> placed = new ArrayList<>();

         for (BlockPos pos : planned) {
            if (placed.size() < 8 && canPlaceRubble(castLevel, pos)) {
               castLevel.setBlock(pos, SololevelingModBlocks.BEAST_RUBBLE.get().defaultBlockState(), 3);
               placed.add(pos.immutable());
            }
         }

         if (placed.isEmpty()) {
            fail(player, "Rubble Jaw was obstructed before it could rise.");
         } else {
            long now = player.level().getGameTime();
            JAWS.put(player.getUUID(), new BeastMonarchManager.JawState(castId, castLevel, placed, Vec3.atCenterOf(center), facing.normalize(), now + 100L));
            player.level().playSound((Player)null, center, SoundEvents.DEEPSLATE_PLACE, SoundSource.PLAYERS, 1.25F, 0.56F);
         }
      }
   }

   private static void updateJaw(ServerPlayer player, BeastMonarchManager.HuntState hunt, long now) {
      BeastMonarchManager.JawState jaw = JAWS.get(player.getUUID());
      if (jaw != null) {
         if (now >= jaw.expiresAt) {
            clearJaw(player);
         } else if ((now & 1L) == 0L) {
            LivingEntity quarry = quarry(player, hunt);
            if (quarry != null) {
               Vec3 heading = horizontal(quarry.getDeltaMovement());
               if (heading.lengthSqr() < 0.01) {
                  heading = horizontal(quarry.getLookAngle());
               }

               if (heading.lengthSqr() > 0.001) {
                  heading = heading.normalize();
               }

               double nearest = jaw.positions.stream().mapToDouble(pos -> Vec3.atCenterOf(pos).distanceTo(quarry.position())).min().orElse(Double.MAX_VALUE);
               if (nearest <= 3.25 && jaw.lastHeading != null && heading.lengthSqr() > 0.001 && angleDegrees(jaw.lastHeading, heading) >= 75.0) {
                  armHerd(player, quarry, now);
               }

               if (heading.lengthSqr() > 0.001) {
                  jaw.lastHeading = heading;
               }

               Vec3 relative = quarry.getBoundingBox().getCenter().subtract(jaw.mouthCenter);
               double side = relative.dot(jaw.facing);
               Vec3 right = new Vec3(-jaw.facing.z, 0.0, jaw.facing.x);
               if (!Double.isNaN(jaw.lastMouthSide) && side * jaw.lastMouthSide <= 0.0 && Math.abs(relative.dot(right)) <= 1.35 && Math.abs(relative.y) <= 2.1) {
                  armHerd(player, quarry, now);
               }

               jaw.lastMouthSide = side;
            }
         }
      }
   }

   private static void phaseOwnRubble(ServerPlayer player, long now) {
      if (isWhiteFangSovereign(player)) {
         BeastMonarchManager.JawState jaw = JAWS.get(player.getUUID());
         if (jaw != null && jaw.level == player.serverLevel()) {
            AABB body = player.getBoundingBox().inflate(0.18);

            for (BlockPos pos : jaw.positions) {
               if (body.intersects(new AABB(pos))
                  && !jaw.phased.contains(pos)
                  && player.serverLevel().getBlockState(pos).is(SololevelingModBlocks.BEAST_RUBBLE.get())) {
                  jaw.phased.add(pos);
                  player.serverLevel().removeBlock(pos, false);
                  SololevelingMod.queueServerWork(
                     5,
                     () -> {
                        BeastMonarchManager.JawState current = JAWS.get(player.getUUID());
                        if (current == jaw
                           && isBeastVessel(player)
                           && player.level().getGameTime() < current.expiresAt
                           && player.serverLevel().getBlockState(pos).isAir()) {
                           player.serverLevel().setBlock(pos, SololevelingModBlocks.BEAST_RUBBLE.get().defaultBlockState(), 3);
                        }

                        jaw.phased.remove(pos);
                     }
                  );
               }
            }
         }
      }
   }

   private static void updateReconstitution(ServerPlayer player, BeastMonarchManager.HuntState hunt, long now) {
      BeastMonarchManager.ReconstitutionState channel = RECONSTITUTIONS.get(player.getUUID());
      if (channel != null) {
         Vec3 velocity = player.getDeltaMovement();
         player.setDeltaMovement(velocity.x * 0.72, velocity.y, velocity.z * 0.72);
         player.hurtMarked = true;
         hunt.lastInteraction = now;
         if (now >= channel.finishesAt) {
            if (hunt.hunt >= 30 && canAffordMana(player, 180) && consumeMana(player, 180)) {
               if (RECONSTITUTIONS.remove(player.getUUID(), channel)) {
                  hunt.hunt -= 30;
                  double maximumHealth = player.getMaxHealth();
                  double vitality = variables(player).Vitality;
                  double heal = channel.pvp
                     ? Math.min(channel.woundAmount * 0.3, Math.min(6.0, maximumHealth * 0.12))
                     : Math.min(channel.woundAmount * 0.45, Math.min(6.0 + vitality / 10.0, maximumHealth * 0.25));
                  player.heal((float)Math.max(0.0, heal));
                  WOUNDS.remove(player.getUUID());
                  CooldownManager.set(player, "beast_feral_reconstitution", 360);
                  BeastVfxEntity.spawnAttached(player.serverLevel(), player, 8, 16777215, 10950944, 1.45F, 1.8F, 0.0F, 12, 2);
                  player.level().playSound((Player)null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.72F, 0.76F);
                  player.displayClientMessage(
                     Component.literal("WOUNDS RECONSTITUTED  +" + String.format(Locale.ROOT, "%.1f", heal))
                        .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
                     true
                  );
                  syncStatus(player, hunt, true);
               }
            } else {
               interruptReconstitution(player);
            }
         }
      }
   }

   private static void interruptReconstitution(ServerPlayer player) {
      if (RECONSTITUTIONS.remove(player.getUUID()) != null) {
         CooldownManager.set(player, "beast_feral_reconstitution", 160);
         player.level().playSound((Player)null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.75F, 0.58F);
         player.displayClientMessage(Component.literal("RECONSTITUTION INTERRUPTED").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
      }
   }

   private static void updateSovereignChannel(ServerPlayer player, BeastMonarchManager.HuntState hunt, long now) {
      BeastMonarchManager.SovereignChannel channel = SOVEREIGN_CHANNELS.get(player.getUUID());
      if (channel != null) {
         Vec3 velocity = player.getDeltaMovement();
         player.setDeltaMovement(velocity.x * 0.78, velocity.y, velocity.z * 0.78);
         player.hurtMarked = true;
         if (now >= channel.finishesAt) {
            if (hunt.hunt >= 100 && canAffordMana(player, 720) && consumeMana(player, 720)) {
               if (SOVEREIGN_CHANNELS.remove(player.getUUID(), channel)) {
                  hunt.hunt = 0;
                  hunt.openingMask = 0;
                  hunt.lastFeat = null;
                  SOVEREIGNS.put(player.getUUID(), new BeastMonarchManager.SovereignState(now + 280L, now + 360L));
                  player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.combatmode = true;
                     capability.syncPlayerVariables(player);
                  });
                  CooldownManager.set(player, "mana_refresh", 360);
                  PlayerAuraSystem.setContinuous(player, "beast_white_fang", 1.25F);
                  PlayerAuraSystem.burst(player, "beast_white_fang", 28, 1.7F);
                  BeastVfxEntity.spawnAttached(player.serverLevel(), player, 9, 16777215, 10817819, 2.8F, 3.2F, 0.0F, 24, 1);
                  player.level().playSound((Player)null, player.blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.72F, 1.28F);
                  player.displayClientMessage(Component.literal("THE WHITE FANG WALKS").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), true);
                  syncStatus(player, hunt, true);
               }
            } else {
               interruptSovereignChannel(player);
            }
         }
      }
   }

   private static void updateSovereign(ServerPlayer player, long now) {
      BeastMonarchManager.SovereignState state = SOVEREIGNS.get(player.getUUID());
      if (state != null) {
         player.fallDistance = Math.min(player.fallDistance, 5.0F);
         if (!player.isAlive() || !isBeastVessel(player) || !isFangStance(player) || now >= state.expiresAt || now >= state.hardExpiresAt) {
            endSovereign(player, false);
         }
      }
   }

   private static void interruptSovereignChannel(ServerPlayer player) {
      if (SOVEREIGN_CHANNELS.remove(player.getUUID()) != null) {
         CooldownManager.set(player, "beast_white_fang_sovereign", 120);
         player.level().playSound((Player)null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.82F, 0.48F);
         player.displayClientMessage(Component.literal("MANIFESTATION INTERRUPTED").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
      }
   }

   private static void endSovereign(ServerPlayer player, boolean silent) {
      BeastMonarchManager.SovereignState removed = SOVEREIGNS.remove(player.getUUID());
      if (removed != null) {
         removeWhiteFangSpeed(player);
         PlayerAuraSystem.clearContinuous(player);
         BeastMonarchManager.HuntState hunt = HUNTS.get(player.getUUID());
         if (hunt != null) {
            hunt.satedUntil = player.level().getGameTime() + 120L;
            hunt.openingMask = 0;
            hunt.combo = 0;
         }

         if (!silent) {
            PlayerAuraSystem.burst(player, "beast_white_fang", 18, 0.9F);
            player.displayClientMessage(Component.literal("THE SOVEREIGN WITHDRAWS").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), true);
         }
      }
   }

   private static void resolveKingsMaul(ServerPlayer player, UUID targetId, Vec3 committedTarget, Vec3 line, boolean sovereign) {
      if (player.isAlive() && isBeastVessel(player)) {
         LivingEntity target = livingEntity(player.serverLevel(), targetId);
         BeastMonarchManager.HuntState hunt = HUNTS.get(player.getUUID());
         if (target != null && hunt != null && Objects.equals(hunt.quarryId, targetId)) {
            Vec3 start = player.position();
            Vec3 landing = start;
            if (!sovereign) {
               for (double distance = 0.25; distance <= 2.5; distance += 0.25) {
                  Vec3 candidate = start.add(line.scale(distance));
                  AABB moved = player.getBoundingBox().move(candidate.subtract(player.position()));
                  if (!player.level().noCollision(player, moved)) {
                     break;
                  }

                  landing = candidate;
               }

               player.teleportTo(landing.x, landing.y, landing.z);
            }

            Vec3 currentTarget = target.getBoundingBox().getCenter();
            double offLine = distanceFromLine(currentTarget, start.add(0.0, target.getBbHeight() * 0.45, 0.0), line);
            boolean crossedCommit = currentTarget.distanceTo(committedTarget) <= 2.35 && offLine <= 1.45;
            if (crossedCommit && !(CombatRangeHelper.surfaceDistance(player, target) > (sovereign ? 2.75 : 3.2)) && player.hasLineOfSight(target)) {
               double strength = TemporaryStatBonusManager.effectiveStrength(player);
               double perception = variables(player).perception;
               float damage = (float)(18.0 + strength / 6.0 + perception / 20.0);
               if (target instanceof Player) {
                  damage *= 0.65F;
               }

               if (!dealPhysical(player, target, damage)) {
                  maulWhiff(player);
               } else {
                  Vec3 away = horizontal(target.position().subtract(player.position()));
                  boolean fleeing = away.lengthSqr() > 0.001 && horizontal(target.getDeltaMovement()).dot(away.normalize()) > 0.08;
                  if (fleeing) {
                     double factor = target instanceof Player ? 0.85 : 0.7;
                     Vec3 movement = target.getDeltaMovement();
                     target.setDeltaMovement(movement.x * factor, movement.y, movement.z * factor);
                     if (target instanceof Player fleeingPlayer) {
                        fleeingPlayer.setSprinting(false);
                     }
                  } else if (target.isBlocking() && !target.getUseItem().isEmpty()) {
                     target.getUseItem().hurtAndBreak(2, target, broken -> {});
                  }

                  BeastVfxEntity.spawnAttached(
                     player.serverLevel(),
                     target,
                     7,
                     16725312,
                     3407877,
                     Mth.clamp(target.getBbWidth() * 1.3F, 1.3F, 3.2F),
                     Mth.clamp(target.getBbHeight() * 0.65F, 1.5F, 3.5F),
                     0.0F,
                     18,
                     2
                  );
                  player.level().playSound((Player)null, target.blockPosition(), SoundEvents.RAVAGER_ATTACK, SoundSource.PLAYERS, 1.15F, 0.62F);
               }
            } else {
               maulWhiff(player);
            }
         } else {
            maulWhiff(player);
         }
      }
   }

   private static void maulWhiff(ServerPlayer player) {
      player.level().playSound((Player)null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 0.85F, 0.58F);
      player.displayClientMessage(Component.literal("KING'S MAUL - WHIFF").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD), true);
   }

   private static void finishIntercept(ServerPlayer player, BeastMonarchManager.InterceptState state, boolean armAmbush) {
      if (state != null && INTERCEPTS.remove(player.getUUID(), state)) {
         Vec3 current = player.getDeltaMovement();
         player.setDeltaMovement(current.x * 0.28, Math.min(0.16, current.y * 0.35), current.z * 0.28);
         player.hurtMarked = true;
         if (armAmbush && state.targetId != null) {
            LivingEntity target = livingEntity(player.serverLevel(), state.targetId);
            if (target != null
               && validTarget(player, target)
               && !(player.position().distanceTo(state.start) < 4.5)
               && !(CombatRangeHelper.surfaceDistance(player, target) > 6.0)) {
               long now = player.level().getGameTime();
               player.getPersistentData().putLong("beast_ambush_until", now + 14L);
               player.getPersistentData().putUUID("beast_ambush_target", target.getUUID());
               player.getPersistentData().putInt("beast_ambush_kind", 1);
               BeastVfxEntity.spawnAttached(player.serverLevel(), player, 4, 16722731, 4849664, 0.85F, 1.0F, 0.0F, 10, 1);
            }
         }
      }
   }

   private static boolean isAmbushReady(ServerPlayer player, LivingEntity target, long now) {
      return now <= player.getPersistentData().getLong("beast_ambush_until")
         && player.getPersistentData().hasUUID("beast_ambush_target")
         && player.getPersistentData().getUUID("beast_ambush_target").equals(target.getUUID());
   }

   private static void consumeAmbush(ServerPlayer player) {
      player.getPersistentData().remove("beast_ambush_until");
      player.getPersistentData().remove("beast_ambush_target");
      player.getPersistentData().remove("beast_ambush_kind");
   }

   private static void armHerd(ServerPlayer player, LivingEntity target, long now) {
      player.getPersistentData().putLong("beast_herd_until", now + 40L);
      player.getPersistentData().putUUID("beast_herd_target", target.getUUID());
   }

   private static boolean isHerdReady(ServerPlayer player, LivingEntity target, long now) {
      return now <= player.getPersistentData().getLong("beast_herd_until")
         && player.getPersistentData().hasUUID("beast_herd_target")
         && player.getPersistentData().getUUID("beast_herd_target").equals(target.getUUID());
   }

   private static void consumeHerd(ServerPlayer player) {
      player.getPersistentData().remove("beast_herd_until");
      player.getPersistentData().remove("beast_herd_target");
   }

   private static void applyCadenceMovement(ServerPlayer player, BeastMonarchManager.HuntState state, int combo, Vec3 forward) {
      int finisher = isWhiteFangSovereign(player) ? 4 : 3;
      Vec3 step;
      if (combo == 2) {
         double side = Math.abs(player.xxa) > 0.05F ? Math.signum(player.xxa) : state.orbitSide;
         state.orbitSide = -side;
         Vec3 lateral = new Vec3(-forward.z, 0.0, forward.x).scale(side * 0.24);
         step = lateral.add(forward.scale(0.08));
      } else {
         step = forward.scale(combo == 1 ? 0.13 : (combo == finisher ? 0.1 : 0.075));
      }

      if (player.level().noCollision(player, player.getBoundingBox().move(step))) {
         player.setDeltaMovement(player.getDeltaMovement().add(step));
         player.hurtMarked = true;
      }
   }

   private static void applyClawReaction(ServerPlayer player, LivingEntity target, int combo, Vec3 forward) {
      int finisher = isWhiteFangSovereign(player) ? 4 : 3;
      double resistance = Math.max(0.15, 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
      double force = (combo == finisher ? 0.38 : (combo == 2 ? 0.17 : 0.11)) * resistance;
      Vec3 push = forward.scale(force);
      target.setDeltaMovement(target.getDeltaMovement().add(push.x, combo == finisher ? 0.08 * resistance : 0.0, push.z));
      target.hurtMarked = true;
   }

   private static void spawnClawArc(ServerPlayer player, int combo, boolean hit, boolean ambush) {
      int finisher = isWhiteFangSovereign(player) ? 4 : 3;
      Vec3 direction = player.getLookAngle().normalize();
      Vec3 origin = player.getEyePosition().add(direction.scale(combo == finisher ? 2.25 : 1.9)).add(0.0, -0.48, 0.0);
      float scale = combo == 1 ? 1.85F : (combo == 2 ? 2.0F : (combo == finisher ? 2.55F : 2.15F));
      float length = combo == finisher ? 2.35F : 1.9F;
      float roll = combo == 1 ? -9.0F : (combo == 2 ? 9.0F : 0.0F);
      BeastVfxEntity.spawn(
         player.serverLevel(),
         origin,
         direction,
         0,
         ambush ? 16728118 : (hit ? 15540011 : 11735579),
         3801093,
         scale,
         length,
         roll,
         combo == finisher ? 12 : 10,
         combo
      );
   }

   private static void spawnScar(ServerPlayer player, LivingEntity target, int combo, boolean ambush) {
      int finisher = isWhiteFangSovereign(player) ? 4 : 3;
      float width = Mth.clamp(target.getBbWidth() * 1.1F, 0.78F, 2.35F);
      float height = Mth.clamp(target.getBbHeight() * 0.52F, 0.9F, 2.7F);
      BeastVfxEntity.spawnAttached(
         player.serverLevel(),
         target,
         1,
         ambush ? 16732488 : 15015209,
         2490368,
         width,
         height,
         combo == 2 ? 12.0F : (combo == finisher ? -5.0F : -14.0F),
         combo == finisher ? 17 : 13,
         combo
      );
   }

   private static Vec3 sweptDestination(ServerPlayer player, Vec3 direction, double maximumDistance) {
      Vec3 start = player.position();
      Vec3 normalized = horizontal(direction);
      if (normalized.lengthSqr() < 0.001) {
         normalized = horizontalLook(player);
      }

      normalized = normalized.normalize();
      Vec3 result = start;

      for (double distance = 0.25; distance <= maximumDistance + 0.001; distance += 0.25) {
         Vec3 candidate = start.add(normalized.scale(distance));
         if (!player.serverLevel().hasChunkAt(BlockPos.containing(candidate))) {
            break;
         }

         AABB moved = player.getBoundingBox().move(candidate.subtract(start));
         if (!player.level().noCollision(player, moved)) {
            break;
         }

         result = candidate;
      }

      return result;
   }

   private static int damageAlongPath(ServerPlayer player, Vec3 start, Vec3 end, double radius, float damage, int maximumTargets) {
      Vec3 strikeStart = start.add(0.0, player.getBbHeight() * 0.48, 0.0);
      Vec3 strikeEnd = end.add(0.0, player.getBbHeight() * 0.48, 0.0);
      AABB search = player.getBoundingBox().move(start.subtract(player.position())).expandTowards(end.subtract(start)).inflate(radius + 0.8);
      List<LivingEntity> targets = new ArrayList<>(
         player.serverLevel().getEntitiesOfClass(LivingEntity.class, search, candidate -> validTarget(player, candidate))
      );
      targets.sort((first, second) -> Double.compare(first.distanceToSqr(player), second.distanceToSqr(player)));
      int hits = 0;

      for (LivingEntity target : targets) {
         if (hits < maximumTargets && !target.getBoundingBox().inflate(radius).clip(strikeStart, strikeEnd).isEmpty()) {
            float applied = target instanceof Player ? damage * 0.72F : damage;
            if (dealPhysical(player, target, applied)) {
               Vec3 direction = horizontal(end.subtract(start));
               if (direction.lengthSqr() > 0.001) {
                  target.setDeltaMovement(target.getDeltaMovement().add(direction.normalize().scale(0.3)));
                  target.hurtMarked = true;
               }

               if (hits < 4) {
                  BeastVfxEntity.spawnAttached(
                     player.serverLevel(),
                     target,
                     1,
                     16751150,
                     3803392,
                     Mth.clamp(target.getBbWidth() * 1.2F, 0.95F, 2.8F),
                     Mth.clamp(target.getBbHeight() * 0.58F, 1.1F, 3.1F),
                     0.0F,
                     15,
                     3
                  );
               }

               hits++;
            }
         }
      }

      return hits;
   }

   private static LivingEntity forwardTarget(ServerPlayer player, double range, double minimumDot) {
      Vec3 look = player.getLookAngle().normalize();
      AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(range * 0.42);
      LivingEntity best = null;
      double bestDistance = Double.MAX_VALUE;

      for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class, search, candidate -> validTarget(player, candidate))) {
         if (!(CombatRangeHelper.surfaceDistance(player, target) > range) && player.hasLineOfSight(target)) {
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
            if (!(toTarget.lengthSqr() < 0.001) && !(look.dot(toTarget.normalize()) < minimumDot)) {
               double distance = target.distanceToSqr(player);
               if (distance < bestDistance) {
                  best = target;
                  bestDistance = distance;
               }
            }
         }
      }

      return best;
   }

   private static void applyWhiteFangSpeed(ServerPlayer player) {
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         speed.removeModifier(WHITE_FANG_SPEED_MODIFIER);
         speed.addTransientModifier(new AttributeModifier(WHITE_FANG_SPEED_MODIFIER, "White Fang Sovereign speed", 0.18, Operation.MULTIPLY_TOTAL));
      }
   }

   private static void removeWhiteFangSpeed(ServerPlayer player) {
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         speed.removeModifier(WHITE_FANG_SPEED_MODIFIER);
      }
   }

   private static boolean validRiftEntry(ServerPlayer player, Vec3 entry) {
      ServerLevel level = player.serverLevel();
      BlockPos pos = BlockPos.containing(entry);
      if (level.hasChunkAt(pos)
         && level.getWorldBorder().isWithinBounds(pos)
         && level.getFluidState(pos).isEmpty()
         && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
         AABB volume = new AABB(entry.x - 0.72, entry.y - 1.18, entry.z - 0.72, entry.x + 0.72, entry.y + 1.18, entry.z + 0.72);
         return level.getEntitiesOfClass(LivingEntity.class, volume, living -> living != player && living.isAlive()).isEmpty();
      } else {
         return false;
      }
   }

   private static Vec3 safeRiftExit(ServerLevel level, LivingEntity entity, Vec3 portalCenter, Vec3 normal) {
      Vec3 outward = normal.normalize();
      Vec3 base = portalCenter.add(outward.scale(0.72 + entity.getBbWidth() * 0.52)).add(0.0, -entity.getBbHeight() * 0.5, 0.0);
      double[] yOffsets = new double[]{0.0, 0.5, 1.0, 1.5, -0.5};

      for (double yOffset : yOffsets) {
         Vec3 candidate = base.add(0.0, yOffset, 0.0);
         BlockPos feet = BlockPos.containing(candidate);
         BlockPos head = BlockPos.containing(candidate.add(0.0, entity.getBbHeight() * 0.82, 0.0));
         if (level.hasChunkAt(feet)
            && level.hasChunkAt(head)
            && level.getWorldBorder().isWithinBounds(feet)
            && level.getFluidState(feet).isEmpty()
            && level.getFluidState(head).isEmpty()) {
            AABB moved = entity.getBoundingBox().move(candidate.subtract(entity.position()));
            if (level.noCollision(entity, moved) && level.getEntitiesOfClass(LivingEntity.class, moved, other -> other != entity && other.isAlive()).isEmpty()) {
               return candidate;
            }
         }
      }

      return null;
   }

   private static boolean canUseRift(LivingEntity entity) {
      return entity.isAlive()
         && !(entity instanceof ArmorStand)
         && !entity.isPassenger()
         && !entity.isVehicle()
         && entity.getBbWidth() <= 2.25F
         && entity.getBbHeight() <= 3.2F;
   }

   private static Vec3 rotateMomentum(Vec3 velocity, Vec3 sourceNormal, Vec3 destinationNormal) {
      double angle = horizontalAngle(destinationNormal) - horizontalAngle(sourceNormal);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double x = velocity.x * cos - velocity.z * sin;
      double z = velocity.x * sin + velocity.z * cos;
      double horizontalSpeed = Math.sqrt(x * x + z * z);
      if (horizontalSpeed > 1.6) {
         x *= 1.6 / horizontalSpeed;
         z *= 1.6 / horizontalSpeed;
      }

      return new Vec3(x, Mth.clamp(velocity.y, -1.6, 1.6), z);
   }

   private static double horizontalAngle(Vec3 direction) {
      return Math.atan2(direction.z, direction.x);
   }

   private static double angleDegrees(Vec3 first, Vec3 second) {
      if (!(first.lengthSqr() < 0.001) && !(second.lengthSqr() < 0.001)) {
         double dot = Mth.clamp(first.normalize().dot(second.normalize()), -1.0, 1.0);
         return Math.toDegrees(Math.acos(dot));
      } else {
         return 0.0;
      }
   }

   private static BlockPos findGround(ServerLevel level, BlockPos seed, int range) {
      for (int offset = range; offset >= -range; offset--) {
         BlockPos candidate = seed.offset(0, offset, 0);
         BlockState state = level.getBlockState(candidate);
         BlockPos floor = candidate.below();
         if ((state.isAir() || state.canBeReplaced())
            && level.getFluidState(candidate).isEmpty()
            && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
            return candidate.immutable();
         }
      }

      return null;
   }

   private static List<BlockPos> rubbleJawPositions(ServerLevel level, BlockPos center, Vec3 facing) {
      Vec3 forward = horizontal(facing).normalize();
      Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
      Vec3 origin = Vec3.atCenterOf(center);
      Set<BlockPos> unique = new HashSet<>();

      for (int depth = 0; depth < 4; depth++) {
         double sideDistance = 1.15 + depth * 0.62;
         Vec3 row = origin.add(forward.scale(-depth * 0.9));

         for (int side = -1; side <= 1; side += 2) {
            BlockPos projected = BlockPos.containing(row.add(right.scale(side * sideDistance)));
            BlockPos grounded = findGround(level, projected, 2);
            if (grounded != null) {
               unique.add(grounded);
            }
         }
      }

      return new ArrayList<>(unique);
   }

   private static boolean canPlaceRubble(ServerLevel level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      BlockPos floor = pos.below();
      return level.hasChunkAt(pos)
         && level.getWorldBorder().isWithinBounds(pos)
         && (state.isAir() || state.canBeReplaced())
         && level.getFluidState(pos).isEmpty()
         && level.getBlockEntity(pos) == null
         && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
         && level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), LivingEntity::isAlive).isEmpty();
   }

   private static void clearJaw(ServerPlayer player) {
      PENDING_JAWS.remove(player.getUUID());
      BeastMonarchManager.JawState jaw = JAWS.remove(player.getUUID());
      if (jaw != null) {
         for (BlockPos pos : jaw.positions) {
            if (jaw.level.getBlockState(pos).is(SololevelingModBlocks.BEAST_RUBBLE.get())) {
               jaw.level.removeBlock(pos, false);
            }
         }
      }
   }

   private static boolean insideOpenFlank(ServerPlayer player, LivingEntity target) {
      Vec3 heading = horizontal(target.getDeltaMovement());
      if (heading.lengthSqr() < 0.015) {
         heading = horizontal(target.getLookAngle());
      }

      if (heading.lengthSqr() < 0.001) {
         return true;
      }

      Vec3 open = heading.normalize().scale(-1.0);
      Vec3 attacker = horizontal(player.position().subtract(target.position()));
      return attacker.lengthSqr() > 0.001 && attacker.normalize().dot(open) >= Math.cos(Math.toRadians(50.0));
   }

   private static double distanceFromLine(Vec3 point, Vec3 origin, Vec3 direction) {
      Vec3 normalized = direction.lengthSqr() < 0.001 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
      Vec3 delta = point.subtract(origin);
      double along = Math.max(0.0, delta.dot(normalized));
      return delta.subtract(normalized.scale(along)).length();
   }

   private static double eligibleWounds(ServerPlayer player, LivingEntity quarry, long now) {
      if (quarry == null) {
         return 0.0;
      }

      Deque<BeastMonarchManager.Wound> wounds = WOUNDS.get(player.getUUID());
      if (wounds == null) {
         return 0.0;
      }

      while (!wounds.isEmpty() && now - wounds.peekFirst().tick > 80L) {
         wounds.removeFirst();
      }

      return wounds.stream().filter(wound -> wound.source.equals(quarry.getUUID())).mapToDouble(wound -> wound.amount).sum();
   }

   private static boolean canCastBeastSkill(ServerPlayer player) {
      return player.isAlive() && isBeastVessel(player) && !isActionLocked(player);
   }

   private static boolean isActionLocked(Entity entity) {
      return entity != null
         && (
            RECONSTITUTIONS.containsKey(entity.getUUID())
               || SOVEREIGN_CHANNELS.containsKey(entity.getUUID())
               || INTERCEPTS.containsKey(entity.getUUID())
               || CHARGES.containsKey(entity.getUUID())
         );
   }

   private static boolean canAffordMana(ServerPlayer player, int amount) {
      return player.isCreative() || variables(player).MP >= amount;
   }

   private static void fail(ServerPlayer player, String message) {
      player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.RED), true);
   }

   private static LivingEntity crosshairTarget(ServerPlayer player, double reach) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      AABB search = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(2.0);
      LivingEntity best = null;
      double bestAlong = Double.MAX_VALUE;

      for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class, search, candidate -> validTarget(player, candidate))) {
         if (!(CombatRangeHelper.surfaceDistance(player, target) > reach)) {
            Vec3 closest = closestPoint(eye, target.getBoundingBox());
            Vec3 to = closest.subtract(eye);
            double along = to.dot(look);
            if (!(along < -0.2) && !(along > reach)) {
               double perpendicular = to.subtract(look.scale(Math.max(0.0, along))).length();
               double allowance = 0.72 + target.getBbWidth() * 0.16;
               if (perpendicular <= allowance && along < bestAlong && player.hasLineOfSight(target)) {
                  best = target;
                  bestAlong = along;
               }
            }
         }
      }

      return best;
   }

   private static boolean canReadForIntercept(ServerPlayer player, LivingEntity target) {
      if (validTarget(player, target) && !(CombatRangeHelper.surfaceDistance(player, target) > 42.0) && player.hasLineOfSight(target)) {
         Vec3 toTarget = target.getBoundingBox().getCenter().subtract(player.getEyePosition()).normalize();
         return player.getLookAngle().normalize().dot(toTarget) >= 0.25;
      } else {
         return false;
      }
   }

   private static boolean validTarget(Player player, LivingEntity target) {
      if (target != null && target != player && target.isAlive() && target.isAttackable() && !target.isInvulnerable() && !(target instanceof ArmorStand)) {
         boolean directlyProvoked = wasDirectlyProvoked(player, target);
         if ((player.isAlliedTo(target) || target.isAlliedTo(player)) && !directlyProvoked) {
            return false;
         } else if (ShadowMonarchManager.isOwnedShadow(target, player)) {
            return false;
         } else if (target instanceof TamableAnimal tame && player.getUUID().equals(tame.getOwnerUUID())) {
            return false;
         } else {
            return !(target instanceof Player other) ? true : !other.isCreative() && !other.isSpectator() && player.canHarmPlayer(other);
         }
      } else {
         return false;
      }
   }

   private static void markDirectlyProvoked(ServerPlayer player, LivingEntity target) {
      target.getPersistentData().putLong("slr_beast_provoked_by_" + player.getUUID(), target.level().getGameTime() + 1200L);
   }

   private static boolean wasDirectlyProvoked(Player player, LivingEntity target) {
      return player.getLastHurtMob() == target
         || target.getLastHurtByMob() == player
         || target.getPersistentData().getLong("slr_beast_provoked_by_" + player.getUUID()) >= target.level().getGameTime();
   }

   private static boolean dealPhysical(ServerPlayer player, LivingEntity target, float damage) {
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

   private static boolean consumeMana(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      } else {
         SololevelingModVariables.PlayerVariables data = variables(player);
         if (data.MP < amount) {
            player.displayClientMessage(Component.literal("Not enough MP (" + amount + " required)").withStyle(ChatFormatting.RED), true);
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

   private static boolean ready(ServerPlayer player, String key) {
      return ready(player, key, "Predator's Intercept");
   }

   private static boolean ready(ServerPlayer player, String key, String displayName) {
      if (!CooldownManager.isOnCooldown(player, key)) {
         return true;
      }

      player.displayClientMessage(
         Component.literal(displayName + ": " + CooldownManager.getRemainingSeconds(player, key) + "s").withStyle(ChatFormatting.RED), true
      );
      return false;
   }

   private static LivingEntity quarry(ServerPlayer player, BeastMonarchManager.HuntState state) {
      if (state != null && state.quarryId != null) {
         LivingEntity target = livingEntity(player.serverLevel(), state.quarryId);
         return validTarget(player, target) ? target : null;
      } else {
         return null;
      }
   }

   private static LivingEntity livingEntity(ServerLevel level, UUID id) {
      return (id == null ? null : level.getEntity(id)) instanceof LivingEntity living ? living : null;
   }

   private static Vec3 relativeAttackVector(ServerPlayer player, LivingEntity target) {
      Vec3 relative = horizontal(player.position().subtract(target.position()));
      return relative.lengthSqr() < 0.001 ? horizontalLook(player).scale(-1.0) : relative.normalize();
   }

   private static double interceptSide(ServerPlayer player, LivingEntity target, Vec3 preyForward) {
      if (Math.abs(player.xxa) > 0.05F) {
         return Math.signum(player.xxa);
      }

      Vec3 toPlayer = horizontal(player.position().subtract(target.position()));
      double cross = preyForward.x * toPlayer.z - preyForward.z * toPlayer.x;
      return cross >= 0.0 ? 1.0 : -1.0;
   }

   private static Vec3 interceptDirection(Vec3 origin, Vec3 destination, double verticalLimit) {
      Vec3 delta = destination.subtract(origin);
      Vec3 flat = horizontal(delta);
      if (flat.lengthSqr() < 0.001) {
         return new Vec3(0.0, Mth.clamp(delta.y, -verticalLimit, verticalLimit), 1.0).normalize();
      }

      double horizontalLength = flat.length();
      double y = Mth.clamp(delta.y / Math.max(1.0, horizontalLength), -verticalLimit, verticalLimit);
      Vec3 normalized = flat.normalize();
      return new Vec3(normalized.x, y, normalized.z).normalize();
   }

   private static Vec3 clampVertical(Vec3 direction, double limit) {
      return new Vec3(direction.x, Mth.clamp(direction.y, -limit, limit), direction.z);
   }

   private static Vec3 closestPoint(Vec3 point, AABB box) {
      return new Vec3(Mth.clamp(point.x, box.minX, box.maxX), Mth.clamp(point.y, box.minY, box.maxY), Mth.clamp(point.z, box.minZ, box.maxZ));
   }

   private static Vec3 horizontalLook(Entity entity) {
      Vec3 look = horizontal(entity.getLookAngle());
      return look.lengthSqr() < 0.001 ? new Vec3(0.0, 0.0, 1.0) : look.normalize();
   }

   private static Vec3 horizontal(Vec3 vector) {
      return new Vec3(vector.x, 0.0, vector.z);
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static void syncStatus(ServerPlayer player, BeastMonarchManager.HuntState state, boolean force) {
      LivingEntity quarry = quarry(player, state);
      String quarryName = quarry == null ? "NO QUARRY" : quarry.getDisplayName().getString();
      int openings = Integer.bitCount(state.openingMask);
      int signature = Objects.hash(state.hunt, state.quarryId, openings, state.combo, isFangStance(player));
      if (force || signature != state.lastSyncSignature) {
         state.lastSyncSignature = signature;
         player.getPersistentData().putBoolean("beast_status_active", true);
         SololevelingMod.PACKET_HANDLER
            .send(
               PacketDistributor.PLAYER.with(() -> player),
               new BeastHuntStatusMessage(true, quarryName, state.hunt, Math.min(2, openings), state.combo, isFangStance(player))
            );
      }
   }

   private static void clearPlayer(ServerPlayer player) {
      clearJaw(player);
      HUNTS.remove(player.getUUID());
      CHARGES.remove(player.getUUID());
      INTERCEPTS.remove(player.getUUID());
      RIFTS.remove(player.getUUID());
      PENDING_JAWS.remove(player.getUUID());
      WOUNDS.remove(player.getUUID());
      RECONSTITUTIONS.remove(player.getUUID());
      SOVEREIGN_CHANNELS.remove(player.getUUID());
      if (SOVEREIGNS.containsKey(player.getUUID())) {
         endSovereign(player, true);
      }

      consumeAmbush(player);
      consumeHerd(player);
      player.getPersistentData().remove("beast_next_strike");
      player.getPersistentData().remove("beast_last_strike");
      player.getPersistentData().remove("beast_cadence_combo");
      player.getPersistentData().remove("beast_fall_safe_until");
      if (player.getPersistentData().getBoolean("beast_status_active")) {
         player.getPersistentData().remove("beast_status_active");
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new BeastHuntStatusMessage(false, "", 0, 0, 0, false));
      }
   }

   private enum Feat {
      PURSUIT(1, 10, "PURSUIT"),
      ANGLE_BREAK(2, 12, "ANGLE BREAK"),
      RIFT_AMBUSH(4, 16, "RIFT AMBUSH"),
      HERD(8, 16, "HERD"),
      CULL(16, 20, "CULL");

      private final int bit;
      private final int hunt;
      private final String display;

      Feat(int bit, int hunt, String display) {
         this.bit = bit;
         this.hunt = hunt;
         this.display = display;
      }
   }

   private static final class HuntState {
      private UUID quarryId;
      private int hunt;
      private long lastInteraction;
      private long lastLineOfSight;
      private long swapLockUntil;
      private long decayGraceUntil;
      private BeastMonarchManager.Feat lastFeat;
      private final Map<BeastMonarchManager.Feat, Long> featReadyAt = new EnumMap<>(BeastMonarchManager.Feat.class);
      private long gainWindowStart;
      private int gainedThisSecond;
      private int openingMask;
      private long openingExpiresAt;
      private UUID cadenceTarget;
      private Vec3 cadenceStartVector;
      private double orbitSide = 1.0;
      private int combo;
      private long satedUntil;
      private int lastSyncSignature = Integer.MIN_VALUE;
   }

   private record InterceptCharge(UUID targetId, long startedAt, Vec3 start, double startingDistance) {
   }

   private record InterceptState(UUID targetId, Vec3 direction, Vec3 destination, Vec3 start, double speed, double maximumDistance, long expiresAt) {
   }

   private static final class JawState {
      private final UUID castId;
      private final ServerLevel level;
      private final List<BlockPos> positions;
      private final Vec3 mouthCenter;
      private final Vec3 facing;
      private final long expiresAt;
      private final Set<BlockPos> phased = new HashSet<>();
      private Vec3 lastHeading;
      private double lastMouthSide = Double.NaN;

      private JawState(UUID castId, ServerLevel level, List<BlockPos> positions, Vec3 mouthCenter, Vec3 facing, long expiresAt) {
         this.castId = castId;
         this.level = level;
         this.positions = positions;
         this.mouthCenter = mouthCenter;
         this.facing = facing;
         this.expiresAt = expiresAt;
      }
   }

   private record ReconstitutionState(UUID quarryId, double woundAmount, boolean pvp, long finishesAt, boolean shedding) {
   }

   private static final class RiftState {
      private final UUID castId;
      private final Vec3 entry;
      private final Vec3 entryNormal;
      private final Vec3 exit;
      private final Vec3 exitNormal;
      private final long armedAt;
      private final long expiresAt;
      private final boolean twoWay;
      private final Map<UUID, Double> forwardSides = new HashMap<>();
      private final Map<UUID, Double> reverseSides = new HashMap<>();
      private final Set<UUID> usedForward = new HashSet<>();
      private final Set<UUID> usedReverse = new HashSet<>();
      private final Map<UUID, Long> immunityUntil = new HashMap<>();

      private RiftState(UUID castId, Vec3 entry, Vec3 entryNormal, Vec3 exit, Vec3 exitNormal, long armedAt, long expiresAt, boolean twoWay) {
         this.castId = castId;
         this.entry = entry;
         this.entryNormal = entryNormal;
         this.exit = exit;
         this.exitNormal = exitNormal;
         this.armedAt = armedAt;
         this.expiresAt = expiresAt;
         this.twoWay = twoWay;
      }
   }

   private record SovereignChannel(long finishesAt) {
   }

   private static final class SovereignState {
      private long expiresAt;
      private final long hardExpiresAt;
      private final Set<BeastMonarchManager.Feat> extendedFeats = new HashSet<>();

      private SovereignState(long expiresAt, long hardExpiresAt) {
         this.expiresAt = expiresAt;
         this.hardExpiresAt = hardExpiresAt;
      }
   }

   private record Wound(UUID source, double amount, long tick) {
   }
}
