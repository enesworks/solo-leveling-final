package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import org.joml.Vector3f;

@EventBusSubscriber
public final class GoliathCombatManager {
   public static final String CAPTURE = "Capture";
   public static final String POWER_SMASH = "Power Smash";
   public static final String COLLAPSE = "Collapse";
   private static final String IDENTITY = "thomas_andre";
   private static final String NEXT_STRIKE = "goliath_next_strike";
   private static final String LAST_STRIKE = "goliath_last_strike";
   private static final String COMBO = "goliath_combo";
   private static final String FALL_SAFE_UNTIL = "goliath_fall_safe_until";
   private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.66F, 0.08F), 1.35F);
   private static final DustParticleOptions PALE_GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.9F, 0.48F), 0.9F);
   private static final float ENHANCED_STRIKE_DAMAGE_MULTIPLIER = 0.72F;
   private static final float POWER_SMASH_DAMAGE_MULTIPLIER = 0.78F;
   private static final float CAPTURE_DAMAGE_MULTIPLIER = 0.82F;
   private static final float PURSUIT_DAMAGE_MULTIPLIER = 0.82F;
   private static final float COLLAPSE_DAMAGE_MULTIPLIER = 0.82F;
   private static final Map<UUID, GoliathCombatManager.ChargeState> CHARGES = new HashMap<>();
   private static final Map<UUID, GoliathCombatManager.PursuitState> PURSUITS = new HashMap<>();
   private static final Map<UUID, GoliathCombatManager.CaptureState> CAPTURES = new HashMap<>();

   private GoliathCombatManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         clearState(player);
      }
   }

   public static boolean isGoliathVessel(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = variables(entity);
      return vars.JOB == 5.0 && ("thomas_andre".equals(vars.vesselIdentity) || vars.vesselIdentity.isBlank());
   }

   public static boolean isCombatStance(Entity entity) {
      return isGoliathVessel(entity) && variables(entity).combatmode;
   }

   public static boolean isManifested(Entity entity) {
      return !(entity instanceof LivingEntity living)
         ? false
         : living.getItemBySlot(EquipmentSlot.HEAD).is(SololevelingModItems.GOLIATH_ARMOR_HELMET.get())
            && living.getItemBySlot(EquipmentSlot.CHEST).is(SololevelingModItems.GOLIATH_ARMOR_CHESTPLATE.get())
            && living.getItemBySlot(EquipmentSlot.LEGS).is(SololevelingModItems.GOLIATH_ARMOR_LEGGINGS.get())
            && living.getItemBySlot(EquipmentSlot.FEET).is(SololevelingModItems.GOLIATH_ARMOR_BOOTS.get());
   }

   public static void enhancedStrike(Player player) {
      if (player instanceof ServerPlayer serverPlayer && isCombatStance(player) && player.isAlive()) {
         long now = player.level().getGameTime();
         if (now >= player.getPersistentData().getLong("goliath_next_strike")) {
            boolean manifested = isManifested(player);
            long last = player.getPersistentData().getLong("goliath_last_strike");
            int combo = now - last <= 18L ? player.getPersistentData().getInt("goliath_combo") % 3 + 1 : 1;

            int mana = switch (combo) {
               case 2 -> manifested ? 80 : 55;
               case 3 -> manifested ? 120 : 85;
               default -> manifested ? 65 : 45;
            };
            mana = VesselManaScaling.strengthScaledCost(serverPlayer, mana, 0.55);
            if (consumeMana(serverPlayer, mana)) {
               player.getPersistentData().putLong("goliath_next_strike", now + (manifested ? 4 : 5));
               player.getPersistentData().putLong("goliath_last_strike", now);
               player.getPersistentData().putInt("goliath_combo", combo);
               player.swing(InteractionHand.MAIN_HAND, true);
               double strength = TemporaryStatBonusManager.effectiveStrength(player);
               float damage = (float)((12.0 + strength / 8.0) * (combo == 1 ? 1.0 : (combo == 2 ? 1.22 : 1.68)) * (manifested ? 1.55 : 1.0)) * 0.72F;
               double radius = combo == 1 ? 3.3 : (combo == 2 ? 4.2 : 5.6);
               Vec3 forward = horizontalLook(player);
               Vec3 center = combo == 3 ? player.position() : player.position().add(forward.scale(combo == 1 ? 2.3 : 1.8));

               for (LivingEntity target : targets(serverPlayer, new AABB(center, center).inflate(radius, 2.4, radius))) {
                  Vec3 toward = target.position().subtract(player.position());
                  if ((combo == 3 || !(toward.lengthSqr() > 0.001) || !(horizontal(toward).normalize().dot(forward) < (combo == 1 ? 0.18 : -0.2)))
                     && dealPhysical(serverPlayer, target, damage)) {
                     double force = combo == 3 ? 1.25 : (combo == 2 ? 0.85 : 1.05);
                     pushAway(target, player.position(), force * targetResistance(target), combo == 3 ? 0.5 : 0.2);
                  }
               }

               strikeVfx(serverPlayer.serverLevel(), center, forward, combo, radius, manifested);
            }
         }
      }
   }

   public static void castCapture(Entity entity) {
      if (entity instanceof ServerPlayer player && isGoliathVessel(player)) {
         GoliathCombatManager.CaptureState active = CAPTURES.get(player.getUUID());
         if (active != null && active.manifested) {
            finishCapture(player, active, true);
         } else if (ready(player, "Capture")) {
            boolean manifested = isManifested(player);
            int mana = manifested ? 520 : 330;
            mana = VesselManaScaling.strengthScaledCost(player, mana, 0.4);
            if (consumeMana(player, mana)) {
               double radius = manifested ? 15.0 : 10.0;
               Vec3 center = player.position().add(horizontalLook(player).scale(manifested ? 7.0 : 5.0));
               List<LivingEntity> found = targets(player, new AABB(center, center).inflate(radius, 7.0, radius));
               found.removeIf(target -> !player.hasLineOfSight(target));
               found.sort(Comparator.comparingDouble(player::distanceToSqr));
               if (found.size() > (manifested ? 24 : 14)) {
                  found = new ArrayList<>(found.subList(0, manifested ? 24 : 14));
               }

               List<UUID> ids = found.stream().map(Entity::getUUID).toList();
               CAPTURES.put(player.getUUID(), new GoliathCombatManager.CaptureState(ids, player.level().getGameTime() + (manifested ? 28 : 16), manifested));
               CooldownManager.set(player, "Capture", manifested ? 150 : 100);
               player.level()
                  .playSound((Player)null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.1F, manifested ? 0.55F : 0.72F);
               waveRing(player.serverLevel(), center, manifested ? 8.0 : 5.0, 24, 0.3);
            }
         }
      }
   }

   public static void castPowerSmash(Entity entity) {
      if (entity instanceof ServerPlayer player && isGoliathVessel(player) && ready(player, "Power Smash")) {
         boolean manifested = isManifested(player);
         int mana = VesselManaScaling.strengthScaledCost(player, manifested ? 620 : 390, 0.45);
         if (consumeMana(player, mana)) {
            CooldownManager.set(player, "Power Smash", manifested ? 125 : 85);
            Vec3 forward = horizontalLook(player);
            double reach = manifested ? 10.0 : 6.5;
            double width = manifested ? 5.2 : 3.5;
            Vec3 center = player.position().add(forward.scale(reach * 0.52));
            List<LivingEntity> found = targets(player, new AABB(center, center).inflate(width, 2.7, width));
            found.removeIf(targetx -> horizontal(targetx.position().subtract(player.position())).normalize().dot(forward) < 0.42);
            found.sort(Comparator.comparingDouble(player::distanceToSqr));
            double strength = TemporaryStatBonusManager.effectiveStrength(player);
            float primary = (float)((31.0 + strength / 3.8) * (manifested ? 1.55 : 1.0)) * 0.78F;

            for (int i = 0; i < found.size(); i++) {
               LivingEntity target = found.get(i);
               float damage = i == 0 ? primary : primary * (manifested ? 0.68F : 0.42F);
               if (dealPhysical(player, target, damage)) {
                  pushAway(target, player.position(), (manifested ? 2.4 : 1.7) * targetResistance(target), 0.35);
                  target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, manifested ? 100 : 55, manifested ? 2 : 1, false, false));
               }
            }

            player.swing(InteractionHand.MAIN_HAND, true);
            player.setDeltaMovement(player.getDeltaMovement().add(forward.scale(manifested ? 0.75 : 0.45)));
            player.hurtMarked = true;
            powerSmashVfx(player.serverLevel(), player.position().add(0.0, 1.1, 0.0), forward, reach, manifested);
            AbilityDestructionManager.fissure(player, AbilityDestructionManager.Profile.GOLIATH_SMASH, player.position(), forward, reach, strength, manifested);
         }
      }
   }

   public static void castCollapse(Entity entity) {
      if (entity instanceof ServerPlayer player && isGoliathVessel(player) && ready(player, "Collapse")) {
         boolean manifested = isManifested(player);
         int mana = VesselManaScaling.strengthScaledCost(player, manifested ? 850 : 540, 0.42);
         if (consumeMana(player, mana)) {
            CooldownManager.set(player, "Collapse", manifested ? 220 : 150);
            player.swing(InteractionHand.MAIN_HAND, true);
            collapseImpact(player, manifested, false);
            if (manifested) {
               for (LivingEntity target : targets(player, player.getBoundingBox().inflate(11.0, 5.0, 11.0))) {
                  target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 18, 9, false, false));
                  target.setDeltaMovement(target.getDeltaMovement().scale(0.12));
                  target.hurtMarked = true;
               }

               SololevelingMod.queueServerWork(8, () -> {
                  if (player.isAlive() && isGoliathVessel(player) && isManifested(player)) {
                     collapseImpact(player, true, true);
                  }
               });
            }
         }
      }
   }

   public static void beginPursuit(Entity entity) {
      if (entity instanceof ServerPlayer player && isCombatStance(player) && !PURSUITS.containsKey(player.getUUID())) {
         if (ready(player, "goliath_pursuit")) {
            LivingEntity target = crosshairTarget(player, 46.0);
            CHARGES.put(player.getUUID(), new GoliathCombatManager.ChargeState(target == null ? null : target.getUUID(), player.level().getGameTime()));
            if (target != null) {
               player.displayClientMessage(Component.literal("§6GOLIATH PURSUIT §7- §fTARGET LOCKED"), true);
               targetMarker(player.serverLevel(), target);
            } else {
               player.displayClientMessage(Component.literal("§6GOLIATH LAUNCH §7- §fNO TARGET"), true);
            }
         }
      }
   }

   public static void releasePursuit(Entity entity, int pressedMs) {
      if (entity instanceof ServerPlayer player && isCombatStance(player)) {
         GoliathCombatManager.ChargeState charge = CHARGES.remove(player.getUUID());
         if (charge != null && ready(player, "goliath_pursuit")) {
            LivingEntity target = charge.targetId == null ? null : livingEntity(player.serverLevel(), charge.targetId);
            if (target != null && (!validTarget(player, target) || player.distanceToSqr(target) > 3025.0)) {
               target = null;
            }

            boolean manifested = isManifested(player);
            int mana = target == null ? (manifested ? 260 : 160) : (manifested ? 430 : 280);
            mana = VesselManaScaling.strengthScaledCost(player, mana, 0.45);
            if (consumeMana(player, mana)) {
               double chargePower = Mth.clamp(0.45 + pressedMs / 900.0, 0.55, 1.35);
               Vec3 direction = player.getLookAngle().normalize();
               GoliathCombatManager.PursuitState state = new GoliathCombatManager.PursuitState(
                  target == null ? null : target.getUUID(), direction, manifested, chargePower, player.level().getGameTime(), target == null ? 55 : 22
               );
               PURSUITS.put(player.getUUID(), state);
               CooldownManager.set(player, "goliath_pursuit", manifested ? 70 : 55);
               player.getPersistentData().putLong("goliath_fall_safe_until", player.level().getGameTime() + 90L);
               Vec3 initial = target == null
                  ? direction.scale((manifested ? 2.35 : 1.95) * chargePower).add(0.0, Math.max(0.12, direction.y * 0.35), 0.0)
                  : directionTo(player, target).scale((manifested ? 2.75 : 2.25) * chargePower);
               breakDashBlocks(player, initial, manifested);
               player.setDeltaMovement(initial);
               player.hurtMarked = true;
               player.level()
                  .playSound(
                     (Player)null,
                     player.blockPosition(),
                     SoundEvents.WARDEN_SONIC_BOOM,
                     SoundSource.PLAYERS,
                     manifested ? 1.2F : 0.9F,
                     manifested ? 0.62F : 0.82F
                  );
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.START && !event.player.level().isClientSide() && event.player instanceof ServerPlayer player) {
         if (player.isAlive() && isGoliathVessel(player)) {
            tickCharge(player);
            tickCapture(player);
            tickPursuit(player);
         } else {
            clearState(player);
         }
      }
   }

   @SubscribeEvent
   public static void onAttackEntity(AttackEntityEvent event) {
      if (isCombatStance(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onLeftClickBlock(LeftClickBlock event) {
      if (isCombatStance(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player && player.getPersistentData().getLong("goliath_fall_safe_until") >= player.level().getGameTime()) {
         event.setCanceled(true);
         player.fallDistance = 0.0F;
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      clearState(event.getEntity());
   }

   private static void tickCharge(ServerPlayer player) {
      GoliathCombatManager.ChargeState charge = CHARGES.get(player.getUUID());
      if (charge != null) {
         if (player.level().getGameTime() - charge.startedAt > 80L) {
            CHARGES.remove(player.getUUID());
         } else {
            if (charge.targetId != null && player.tickCount % 3 == 0) {
               LivingEntity target = livingEntity(player.serverLevel(), charge.targetId);
               if (target != null && validTarget(player, target)) {
                  targetMarker(player.serverLevel(), target);
               }
            }
         }
      }
   }

   private static void tickCapture(ServerPlayer player) {
      GoliathCombatManager.CaptureState state = CAPTURES.get(player.getUUID());
      if (state != null) {
         if (player.level().getGameTime() >= state.endTick) {
            finishCapture(player, state, false);
         } else {
            Vec3 anchor = player.position().add(horizontalLook(player).scale(state.manifested ? 3.2 : 2.5)).add(0.0, state.manifested ? 1.8 : 1.0, 0.0);

            for (UUID id : state.targetIds) {
               LivingEntity target = livingEntity(player.serverLevel(), id);
               if (target != null && validTarget(player, target)) {
                  Vec3 pull = anchor.subtract(target.getBoundingBox().getCenter());
                  double resistance = targetResistance(target);
                  double speed = Math.min(state.manifested ? 1.55 : 1.05, 0.18 + pull.length() * 0.115) * resistance;
                  if (pull.lengthSqr() > 0.04) {
                     target.setDeltaMovement(pull.normalize().scale(speed));
                  }

                  if (state.manifested && pull.lengthSqr() < 9.0) {
                     target.setDeltaMovement(target.getDeltaMovement().scale(0.2).add(0.0, 0.035, 0.0));
                  }

                  target.fallDistance = 0.0F;
                  target.hurtMarked = true;
               }
            }

            if (player.tickCount % 2 == 0) {
               captureVfx(player.serverLevel(), anchor, state.manifested);
            }
         }
      }
   }

   private static void finishCapture(ServerPlayer player, GoliathCombatManager.CaptureState state, boolean thrown) {
      CAPTURES.remove(player.getUUID());
      Vec3 look = player.getLookAngle().normalize();
      float damage = physicalDamage(player, state.manifested ? (thrown ? 28.0 : 22.0) : 12.0, state.manifested ? 5.5 : 10.0) * 0.82F;

      for (UUID id : state.targetIds) {
         LivingEntity target = livingEntity(player.serverLevel(), id);
         if (target != null && validTarget(player, target)) {
            dealPhysical(player, target, damage);
            Vec3 launch = thrown
               ? look.scale(2.2 * targetResistance(target)).add(0.0, 0.35, 0.0)
               : target.position().subtract(player.position()).normalize().scale(0.7 * targetResistance(target)).add(0.0, 0.45, 0.0);
            target.setDeltaMovement(launch);
            target.hurtMarked = true;
         }
      }

      Vec3 center = player.position().add(horizontalLook(player).scale(2.7)).add(0.0, 1.0, 0.0);
      waveRing(player.serverLevel(), center, state.manifested ? 7.0 : 4.0, state.manifested ? 32 : 18, 0.1);
      player.level()
         .playSound(
            (Player)null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, state.manifested ? 1.4F : 0.8F, thrown ? 0.65F : 0.9F
         );
   }

   private static void tickPursuit(ServerPlayer player) {
      GoliathCombatManager.PursuitState state = PURSUITS.get(player.getUUID());
      if (state != null) {
         long elapsed = player.level().getGameTime() - state.startedAt;
         LivingEntity target = state.targetId == null ? null : livingEntity(player.serverLevel(), state.targetId);
         if (state.targetId == null || target != null && validTarget(player, target)) {
            if (target != null) {
               suspendTarget(target);
               if (CombatRangeHelper.withinSurfaceRange(player, target, 1.45)) {
                  finishPursuit(player, state, target, true);
                  return;
               }

               Vec3 motion = directionTo(player, target).scale((state.manifested ? 2.75 : 2.25) * state.power);
               if (!player.serverLevel().hasChunkAt(BlockPos.containing(player.position().add(motion)))) {
                  finishPursuit(player, state, target, false);
                  return;
               }

               breakDashBlocks(player, motion, state.manifested);
               if (player.horizontalCollision && !player.serverLevel().noCollision(player, player.getBoundingBox().move(motion.normalize().scale(0.45)))) {
                  finishPursuit(player, state, null, true);
                  return;
               }

               player.setDeltaMovement(motion);
            } else {
               Vec3 motion = player.getDeltaMovement();
               if (motion.lengthSqr() < 0.08) {
                  motion = state.direction.scale((state.manifested ? 2.25 : 1.85) * state.power).add(0.0, -0.08, 0.0);
               }

               breakDashBlocks(player, motion, state.manifested);
               boolean blocked = player.horizontalCollision
                  && !player.serverLevel().noCollision(player, player.getBoundingBox().move(motion.normalize().scale(0.45)));
               if (elapsed > 2L && (player.onGround() || blocked) || elapsed >= state.maxTicks) {
                  finishPursuit(player, state, null, true);
                  return;
               }
            }

            player.fallDistance = 0.0F;
            player.hurtMarked = true;
            damageDashPath(player, state);
            dashTrail(player.serverLevel(), player, state.manifested);
            if (elapsed >= state.maxTicks) {
               finishPursuit(player, state, target, target != null);
            }
         } else {
            finishPursuit(player, state, null, false);
         }
      }
   }

   private static void finishPursuit(ServerPlayer player, GoliathCombatManager.PursuitState state, LivingEntity target, boolean impact) {
      PURSUITS.remove(player.getUUID());
      player.setDeltaMovement(Vec3.ZERO);
      player.hurtMarked = true;
      player.fallDistance = 0.0F;
      if (impact) {
         Vec3 center = target == null ? player.position() : target.position();
         double radius = state.manifested ? 8.5 : 6.0;
         float damage = physicalDamage(player, state.manifested ? 34.0 : 21.0, state.manifested ? 3.7 : 5.5) * 0.82F;

         for (LivingEntity nearby : targets(player, new AABB(center, center).inflate(radius, 4.0, radius))) {
            float dealt = nearby == target ? damage * 1.45F : damage;
            if (dealPhysical(player, nearby, dealt)) {
               pushAway(nearby, center, (state.manifested ? 2.1 : 1.35) * targetResistance(nearby), 0.7);
            }
         }

         impactVfx(player.serverLevel(), center, radius, state.manifested);
         AbilityDestructionManager.impact(
            player, AbilityDestructionManager.Profile.GOLIATH_PURSUIT_IMPACT, center, TemporaryStatBonusManager.effectiveStrength(player), state.manifested
         );
      }
   }

   private static void damageDashPath(ServerPlayer player, GoliathCombatManager.PursuitState state) {
      AABB area = player.getBoundingBox().inflate(state.manifested ? 1.45 : 0.9);
      float damage = physicalDamage(player, state.manifested ? 12.0 : 7.0, 12.0) * 0.82F;

      for (LivingEntity target : targets(player, area)) {
         if (state.hitIds.add(target.getUUID()) && dealPhysical(player, target, damage)) {
            pushAway(target, player.position(), 1.15 * targetResistance(target), 0.25);
         }
      }
   }

   private static void suspendTarget(LivingEntity target) {
      double resistance = targetResistance(target);
      target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, resistance < 0.55 ? 4 : 15, false, false));
      Vec3 movement = target.getDeltaMovement();
      target.setDeltaMovement(movement.x * (1.0 - 0.9 * resistance), Math.max(0.025, movement.y * 0.1), movement.z * (1.0 - 0.9 * resistance));
      target.fallDistance = 0.0F;
      target.hurtMarked = true;
   }

   private static void collapseImpact(ServerPlayer player, boolean manifested, boolean secondWave) {
      double radius = manifested ? (secondWave ? 12.0 : 10.0) : 7.0;
      float damage = physicalDamage(player, manifested ? (secondWave ? 30.0 : 24.0) : 18.0, manifested ? (secondWave ? 4.6 : 5.2) : 7.0) * 0.82F;
      Vec3 center = player.position();

      for (LivingEntity target : targets(player, player.getBoundingBox().inflate(radius, 4.5, radius))) {
         if (dealPhysical(player, target, damage)) {
            double force = (manifested ? 1.65 : 1.05) * targetResistance(target);
            pushAway(target, center, force, secondWave ? 1.0 : 0.65);
            if (manifested) {
               target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, secondWave ? 100 : 60, secondWave ? 2 : 1, false, false));
            }
         }
      }

      impactVfx(player.serverLevel(), center, radius, manifested);
      AbilityDestructionManager.impact(
         player, AbilityDestructionManager.Profile.GOLIATH_COLLAPSE, center, TemporaryStatBonusManager.effectiveStrength(player), manifested || secondWave
      );
      player.level()
         .playSound(
            (Player)null,
            player.blockPosition(),
            secondWave ? SoundEvents.LIGHTNING_BOLT_THUNDER : SoundEvents.GENERIC_EXPLODE,
            SoundSource.PLAYERS,
            manifested ? 1.5F : 1.0F,
            secondWave ? 0.55F : 0.72F
         );
   }

   private static void breakDashBlocks(ServerPlayer player, Vec3 motion, boolean manifested) {
      if (!(motion.lengthSqr() < 1.0E-6) && (player.tickCount & 1) == 0) {
         Vec3 start = player.position().add(0.0, player.getBbHeight() * 0.48, 0.0);
         AbilityDestructionManager.line(
            player,
            AbilityDestructionManager.Profile.GOLIATH_PURSUIT_PATH,
            start,
            start.add(motion),
            TemporaryStatBonusManager.effectiveStrength(player),
            manifested
         );
      }
   }

   private static LivingEntity crosshairTarget(ServerPlayer player, double range) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.5);
      LivingEntity best = null;
      double bestAlong = Double.MAX_VALUE;

      for (LivingEntity target : targets(player, search)) {
         Vec3 to = target.getBoundingBox().getCenter().subtract(eye);
         double along = to.dot(look);
         if (!(along <= 0.0) && !(along > range)) {
            double perpendicular = to.subtract(look.scale(along)).length();
            double allowance = 1.15 + target.getBbWidth() * 0.5;
            if (perpendicular <= allowance && along < bestAlong && player.hasLineOfSight(target)) {
               best = target;
               bestAlong = along;
            }
         }
      }

      return best;
   }

   private static List<LivingEntity> targets(ServerPlayer player, AABB area) {
      return player.serverLevel().getEntitiesOfClass(LivingEntity.class, area, target -> validTarget(player, target));
   }

   private static boolean validTarget(Player player, LivingEntity target) {
      if (target == null || target == player || !target.isAlive() || !target.isAttackable() || target.isInvulnerable() || target instanceof ArmorStand) {
         return false;
      }

      if (!player.isAlliedTo(target) && !target.isAlliedTo(player) && !ShadowMonarchManager.isOwnedShadow(target, player)) {
         if (target instanceof TamableAnimal tame && player.getUUID().equals(tame.getOwnerUUID())) {
            return false;
         } else {
            return !(target instanceof Player other) ? true : !other.isCreative() && !other.isSpectator() && player.canHarmPlayer(other);
         }
      } else {
         return false;
      }
   }

   private static boolean dealPhysical(ServerPlayer player, LivingEntity target, float damage) {
      if (!validTarget(player, target)) {
         return false;
      }

      target.invulnerableTime = 0;
      return target.hurt(player.damageSources().playerAttack(player), Math.max(0.5F, damage));
   }

   private static float physicalDamage(ServerPlayer player, double base, double strengthDivisor) {
      return (float)(base + TemporaryStatBonusManager.effectiveStrength(player) / strengthDivisor);
   }

   private static boolean consumeMana(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      } else {
         SololevelingModVariables.PlayerVariables vars = variables(player);
         if (vars.MP < amount) {
            player.displayClientMessage(Component.literal("§cNot enough MP! §7(" + amount + " required)"), true);
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
      if (!CooldownManager.isOnCooldown(player, key)) {
         return true;
      }

      player.displayClientMessage(Component.literal("§c" + key + " is on cooldown."), true);
      return false;
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static LivingEntity livingEntity(ServerLevel level, UUID id) {
      return level.getEntity(id) instanceof LivingEntity living ? living : null;
   }

   private static Vec3 horizontalLook(Entity entity) {
      Vec3 look = horizontal(entity.getLookAngle());
      return look.lengthSqr() < 0.001 ? new Vec3(0.0, 0.0, 1.0) : look.normalize();
   }

   private static Vec3 horizontal(Vec3 vector) {
      return new Vec3(vector.x, 0.0, vector.z);
   }

   private static Vec3 directionTo(Entity source, LivingEntity target) {
      Vec3 direction = target.getBoundingBox().getCenter().subtract(source.getBoundingBox().getCenter());
      return direction.lengthSqr() < 0.001 ? source.getLookAngle().normalize() : direction.normalize();
   }

   private static double targetResistance(LivingEntity target) {
      if (target.getMaxHealth() >= 500.0F || target.getBbWidth() >= 4.0F) {
         return 0.28;
      } else {
         return !(target.getMaxHealth() >= 220.0F) && !(target.getBbWidth() >= 2.4F) ? 1.0 : 0.5;
      }
   }

   private static void pushAway(LivingEntity target, Vec3 origin, double horizontal, double vertical) {
      Vec3 direction = horizontal(target.position().subtract(origin));
      if (direction.lengthSqr() < 0.001) {
         direction = new Vec3(0.0, 0.0, 1.0);
      }

      direction = direction.normalize();
      target.setDeltaMovement(direction.x * horizontal, Math.max(target.getDeltaMovement().y, vertical), direction.z * horizontal);
      target.hurtMarked = true;
   }

   private static void strikeVfx(ServerLevel level, Vec3 center, Vec3 forward, int combo, double radius, boolean manifested) {
      int points = combo == 3 ? 24 : 14;
      if (combo == 3) {
         waveRing(level, center.add(0.0, 0.18, 0.0), radius, points, 0.08);
      } else {
         for (int i = 1; i <= points; i++) {
            double distance = radius * i / points;
            Vec3 pos = center.add(forward.scale(distance - radius * 0.5));
            level.sendParticles(i % 3 == 0 ? PALE_GOLD : GOLD, pos.x, pos.y + 1.0, pos.z, 1, 0.12, 0.12, 0.12, 0.02);
         }
      }

      level.sendParticles(ParticleTypes.POOF, center.x, center.y + 0.8, center.z, manifested ? 18 : 10, radius * 0.18, 0.4, radius * 0.18, 0.08);
      level.playSound(
         (Player)null,
         BlockPos.containing(center),
         combo == 3 ? SoundEvents.ANVIL_LAND : SoundEvents.IRON_GOLEM_ATTACK,
         SoundSource.PLAYERS,
         manifested ? 1.25F : 0.85F,
         combo == 3 ? 0.58F : 0.82F + combo * 0.06F
      );
   }

   private static void powerSmashVfx(ServerLevel level, Vec3 origin, Vec3 forward, double reach, boolean manifested) {
      Vec3 right = new Vec3(-forward.z, 0.0, forward.x);

      for (int i = 0; i < (manifested ? 34 : 22); i++) {
         double progress = (double)i / (manifested ? 33 : 21);
         double spread = Math.sin(progress * Math.PI) * (manifested ? 2.2 : 1.25);
         Vec3 pos = origin.add(forward.scale(progress * reach)).add(right.scale((i % 2 == 0 ? 1 : -1) * spread));
         level.sendParticles(i % 4 == 0 ? ParticleTypes.END_ROD : GOLD, pos.x, pos.y + Math.sin(progress * Math.PI) * 0.6, pos.z, 1, 0.04, 0.04, 0.04, 0.01);
      }

      level.sendParticles(ParticleTypes.EXPLOSION, origin.x + forward.x * reach, origin.y, origin.z + forward.z * reach, manifested ? 4 : 2, 0.5, 0.5, 0.5, 0.0);
      level.playSound(
         (Player)null, BlockPos.containing(origin), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, manifested ? 1.45F : 1.0F, manifested ? 0.55F : 0.78F
      );
   }

   private static void captureVfx(ServerLevel level, Vec3 anchor, boolean manifested) {
      int points = manifested ? 10 : 6;

      for (int i = 0; i < points; i++) {
         double angle = level.getGameTime() * 0.34 + i * Math.PI * 2.0 / points;
         double radius = manifested ? 2.7 : 1.7;
         Vec3 pos = anchor.add(Math.cos(angle) * radius, Math.sin(angle * 1.7) * 1.2, Math.sin(angle) * radius);
         level.sendParticles(i % 3 == 0 ? PALE_GOLD : GOLD, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   private static void targetMarker(ServerLevel level, LivingEntity target) {
      Vec3 center = target.getBoundingBox().getCenter();
      double radius = Math.max(0.8, target.getBbWidth() * 0.75);

      for (int i = 0; i < 10; i++) {
         double angle = i * Math.PI * 2.0 / 10.0 + level.getGameTime() * 0.18;
         level.sendParticles(
            i % 2 == 0 ? GOLD : PALE_GOLD, center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius, 1, 0.0, 0.0, 0.0, 0.0
         );
      }
   }

   private static void dashTrail(ServerLevel level, Player player, boolean manifested) {
      Vec3 pos = player.position().add(0.0, player.getBbHeight() * 0.55, 0.0);
      level.sendParticles(
         GOLD, pos.x, pos.y, pos.z, manifested ? 12 : 7, player.getBbWidth() * 0.45, player.getBbHeight() * 0.3, player.getBbWidth() * 0.45, 0.02
      );
      level.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y - 0.3, pos.z, manifested ? 7 : 4, 0.2, 0.2, 0.2, 0.04);
   }

   private static void impactVfx(ServerLevel level, Vec3 center, double radius, boolean manifested) {
      waveRing(level, center.add(0.0, 0.15, 0.0), radius, manifested ? 36 : 24, 0.12);
      waveRing(level, center.add(0.0, 0.45, 0.0), radius * 0.68, manifested ? 28 : 18, 0.18);
      level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.8, center.z, manifested ? 8 : 4, radius * 0.22, 0.7, radius * 0.22, 0.0);
      level.sendParticles(ParticleTypes.POOF, center.x, center.y + 0.4, center.z, manifested ? 55 : 30, radius * 0.35, 0.7, radius * 0.35, 0.12);
      level.playSound(
         (Player)null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, manifested ? 1.7F : 1.15F, manifested ? 0.52F : 0.7F
      );
   }

   private static void waveRing(ServerLevel level, Vec3 center, double radius, int points, double yWave) {
      for (int i = 0; i < points; i++) {
         double angle = i * Math.PI * 2.0 / points;
         Vec3 pos = center.add(Math.cos(angle) * radius, Math.sin(angle * 3.0) * yWave, Math.sin(angle) * radius);
         level.sendParticles(i % 4 == 0 ? PALE_GOLD : GOLD, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   private static void clearState(Player player) {
      UUID id = player.getUUID();
      CHARGES.remove(id);
      boolean wasPursuing = PURSUITS.remove(id) != null;
      CAPTURES.remove(id);
      player.getPersistentData().remove("goliath_next_strike");
      player.getPersistentData().remove("goliath_last_strike");
      player.getPersistentData().remove("goliath_combo");
      player.getPersistentData().remove("goliath_fall_safe_until");
      if (wasPursuing) {
         player.setDeltaMovement(Vec3.ZERO);
         player.hurtMarked = true;
         player.fallDistance = 0.0F;
      }
   }

   private record CaptureState(List<UUID> targetIds, long endTick, boolean manifested) {
   }

   private record ChargeState(UUID targetId, long startedAt) {
   }

   private static final class PursuitState {
      private final UUID targetId;
      private final Vec3 direction;
      private final boolean manifested;
      private final double power;
      private final long startedAt;
      private final int maxTicks;
      private final Set<UUID> hitIds = new HashSet<>();

      private PursuitState(UUID targetId, Vec3 direction, boolean manifested, double power, long startedAt, int maxTicks) {
         this.targetId = targetId;
         this.direction = direction;
         this.manifested = manifested;
         this.power = power;
         this.startedAt = startedAt;
         this.maxTicks = maxTicks;
      }
   }
}
