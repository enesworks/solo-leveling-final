package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;
import dev.eness.sololevelingfinal.core.entity.RulersAuthorityAuraEntity;
import dev.eness.sololevelingfinal.core.entity.ThrownDaggerEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModSounds;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.StealthBossDetectionHelper;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE)
public final class RulersAuthorityManager {
   private static final String COOLDOWN = "telekinesis";
   private static final String LAUNCH_PROTECTION = "sl_telekinesis_launch_protection_until";
   private static final int HOLD_TICKS = 5;
   private static final double DAGGER_AUTHORITY_INITIAL_BASE = 240.0;
   private static final double DAGGER_HAND_INITIAL_BASE = 170.0;
   private static final double DAGGER_AUTHORITY_DRAIN_BASE = 92.0;
   private static final double DAGGER_HAND_DRAIN_BASE = 64.0;
   private static final double DAGGER_AUTHORITY_INITIAL_MAX_MANA_PERCENT = 0.035;
   private static final double DAGGER_HAND_INITIAL_MAX_MANA_PERCENT = 0.025;
   private static final double DAGGER_AUTHORITY_DRAIN_MAX_MANA_PERCENT = 0.018;
   private static final double DAGGER_HAND_DRAIN_MAX_MANA_PERCENT = 0.012;
   private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss"));
   private static final Map<UUID, RulersAuthorityManager.ControlSession> SESSIONS = new HashMap<>();
   private static final Map<UUID, RulersAuthorityManager.ThrownState> THROWN = new HashMap<>();

   private RulersAuthorityManager() {
   }

   public static boolean hasAbility(Entity entity) {
      if (entity == null) {
         return false;
      }

      String abilities = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(data -> data.abilities).orElse("");
      return containsAbility(abilities, "telekinesis")
         || containsAbility(abilities, "rulers_hand")
         || containsAbility(abilities, "ruler_s_hand")
         || containsAbility(abilities, "rulers_authority")
         || containsAbility(abilities, "ruler_s_authority");
   }

   public static boolean hasAuthority(Entity entity) {
      if (entity == null) {
         return false;
      }

      String abilities = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(data -> data.abilities).orElse("");
      return containsAbility(abilities, "telekinesis") || containsAbility(abilities, "rulers_authority") || containsAbility(abilities, "ruler_s_authority");
   }

   public static void begin(ServerPlayer player) {
      if (canUse(player) && !SESSIONS.containsKey(player.getUUID())) {
         if (CooldownManager.isOnCooldown(player, "telekinesis")) {
            player.displayClientMessage(Component.literal("Ruler's Authority is on cooldown").withStyle(ChatFormatting.RED), true);
         } else {
            boolean authority = hasAuthority(player);
            RulersAuthorityManager.ControlSession session = new RulersAuthorityManager.ControlSession(
               player.level().getGameTime(), player.isShiftKeyDown(), authority, authority ? 9.0 : 7.0
            );
            SESSIONS.put(player.getUUID(), session);
         }
      }
   }

   public static void release(ServerPlayer player, int pressedMs) {
      RulersAuthorityManager.ControlSession session = SESSIONS.remove(player.getUUID());
      if (session != null) {
         Entity controlled = getControlledEntity(player, session);
         if (controlled != null) {
            if (session.bossResistance) {
               finishBossPressure(player, controlled, session);
            } else {
               throwControlled(player, controlled, session, player.isShiftKeyDown());
            }
         } else {
            discardAura(session);
            boolean charged = pressedMs >= 450 || player.level().getGameTime() - session.startedAt >= 9L;
            performTap(player, session.sneakingAtStart || player.isShiftKeyDown(), charged, session.authority);
         }
      }
   }

   public static void adjustDistance(ServerPlayer player, int direction) {
      RulersAuthorityManager.ControlSession session = SESSIONS.get(player.getUUID());
      if (session != null && session.controlledId != null && !session.bossResistance && direction != 0) {
         double maximum = session.authority ? 22.0 : 14.0;
         session.distance = Mth.clamp(session.distance + Math.signum(direction) * 1.5, 3.0, maximum);
      }
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         UUID ownerId = player.getUUID();
         RulersAuthorityManager.ControlSession session = SESSIONS.remove(ownerId);
         if (session != null) {
            Entity controlled = getControlledEntity(player, session);
            discardAura(session);
            restoreGravity(controlled, session);
         }

         Iterator<Entry<UUID, RulersAuthorityManager.ThrownState>> iterator = THROWN.entrySet().iterator();

         while (iterator.hasNext()) {
            RulersAuthorityManager.ThrownState state = iterator.next().getValue();
            if (state.owner != null && ownerId.equals(state.owner.getUUID())) {
               iterator.remove();
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         RulersAuthorityManager.ControlSession session = SESSIONS.get(player.getUUID());
         if (session != null) {
            if (canUse(player) && !player.isSpectator() && player.isAlive()) {
               long heldTicks = player.level().getGameTime() - session.startedAt;
               if (session.controlledId == null && heldTicks >= 5L) {
                  tryAcquire(player, session);
               }

               Entity controlled = getControlledEntity(player, session);
               if (controlled != null) {
                  if (!controlled.isAlive()
                     || controlled.level() != player.level()
                     || controlled.distanceToSqr(player) > Math.pow(session.authority ? 45.0 : 30.0, 2.0)) {
                     restoreGravity(controlled, session);
                     discardAura(session);
                     session.controlledId = null;
                     session.controlledEntity = null;
                  } else if (!advanceBreakout(player, controlled, session)) {
                     session.drainTicker++;
                     if (session.drainTicker >= 20) {
                        session.drainTicker = 0;
                        double drain = controlDrain(player, controlled, session);
                        if (!consumeMana(player, drain)) {
                           player.displayClientMessage(Component.literal("Ruler's power released: not enough mana").withStyle(ChatFormatting.RED), true);
                           cancelSession(player, session);
                           return;
                        }

                        CooldownManager.set(player, "mana_refresh", 35);
                     }

                     if (session.bossResistance) {
                        applyBossPressure(player, controlled, session);
                     } else {
                        updateControlledPosition(player, controlled, session);
                     }

                     if (session.authority) {
                        deflectNearbyProjectiles(player);
                     }

                     renderControlEffect(player.serverLevel(), player, controlled, session);
                  }
               }
            } else {
               cancelSession(player, session);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END && !THROWN.isEmpty()) {
         Iterator<Entry<UUID, RulersAuthorityManager.ThrownState>> iterator = THROWN.entrySet().iterator();

         while (iterator.hasNext()) {
            RulersAuthorityManager.ThrownState state = iterator.next().getValue();
            state.ticks++;
            Entity target = state.target;
            ServerPlayer owner = state.owner;
            if (target != null && owner != null && target.isAlive() && target.level() == owner.level()) {
               Entity collision = findThrowCollision(owner, target);
               boolean hitSurface = target.horizontalCollision || state.ticks > 4 && target.onGround();
               if (collision != null || hitSurface) {
                  resolveImpact(owner, target, collision, state);
                  iterator.remove();
               } else if (state.ticks > 45) {
                  iterator.remove();
               }
            } else {
               iterator.remove();
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         RulersAuthorityManager.ControlSession session = SESSIONS.remove(player.getUUID());
         if (session != null) {
            discardAura(session);
            restoreGravity(getControlledEntity(player, session), session);
         }

         THROWN.values().removeIf(state -> state.owner.getUUID().equals(player.getUUID()));
      }
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         RulersAuthorityManager.ControlSession session = SESSIONS.remove(player.getUUID());
         if (session != null) {
            discardAura(session);
            restoreGravity(getControlledEntity(player, session), session);
         }
      }
   }

   @SubscribeEvent
   public static void onLivingFall(LivingFallEvent event) {
      LivingEntity entity = event.getEntity();
      long protectedUntil = entity.getPersistentData().getLong("sl_telekinesis_launch_protection_until");
      if (protectedUntil < entity.level().getGameTime()) {
         entity.getPersistentData().remove("sl_telekinesis_launch_protection_until");
      } else {
         event.setDamageMultiplier(event.getDamageMultiplier() * 0.3F);
         entity.getPersistentData().remove("sl_telekinesis_launch_protection_until");
      }
   }

   private static void tryAcquire(ServerPlayer player, RulersAuthorityManager.ControlSession session) {
      double range = session.authority ? 30.0 : 18.0;
      Entity target = findLookTarget(player, range);
      if (target != null) {
         RulersAuthorityManager.ControlProfile control = createControlProfile(player, target, session.authority);
         if (control.negated) {
            consumeMana(player, session.authority ? 42.0 : 30.0);
            renderNegation(player.serverLevel(), target);
            playTelekinesisSound(player, 0.48F);
            player.displayClientMessage(
               Component.literal(target.getDisplayName().getString() + " negated " + (session.authority ? "Ruler's Authority" : "Ruler's Hand"))
                  .withStyle(ChatFormatting.RED),
               true
            );
            if (target instanceof ServerPlayer targetPlayer) {
               targetPlayer.displayClientMessage(
                  Component.literal("You negated " + player.getName().getString() + "'s control").withStyle(ChatFormatting.AQUA), true
               );
            }

            CooldownManager.set(player, "mana_refresh", 20);
            CooldownManager.set(player, "telekinesis", session.authority ? 28 : 38);
            SESSIONS.remove(player.getUUID());
         } else {
            session.casterPower = control.casterPower;
            session.targetPower = control.targetPower;
            session.breakoutLimitTicks = control.breakoutTicks;
            session.unstableControl = control.unstable;
            boolean boss = isBoss(target);
            if (!boss || session.authority) {
               double weight = entityWeight(target);
               if (session.authority || !(weight > 5.5)) {
                  double initialCost = controlInitialCost(player, target, session.authority, weight);
                  if (!consumeMana(player, initialCost)) {
                     player.displayClientMessage(
                        Component.literal("Not enough mana to seize " + target.getDisplayName().getString()).withStyle(ChatFormatting.RED), true
                     );
                     SESSIONS.remove(player.getUUID());
                  } else {
                     session.controlledId = target.getUUID();
                     session.controlledEntity = target;
                     session.bossResistance = boss;
                     session.originalNoGravity = target.isNoGravity();
                     session.weight = weight;
                     session.auraEntity = spawnControlAura(player.serverLevel(), target, session.authority, boss);
                     if (!boss) {
                        target.setNoGravity(true);
                        target.setDeltaMovement(Vec3.ZERO);
                        if (target instanceof Mob mob) {
                           mob.getNavigation().stop();
                        }
                     }

                     CooldownManager.set(player, "mana_refresh", 35);
                     playTelekinesisSound(player, boss ? 0.7F : 1.15F);
                     player.displayClientMessage(
                        Component.literal(
                              boss
                                 ? "Authority pressure: " + target.getDisplayName().getString()
                                 : (session.unstableControl ? "Unstable control: " : "Seized: ") + target.getDisplayName().getString()
                           )
                           .withStyle(session.unstableControl ? ChatFormatting.YELLOW : ChatFormatting.AQUA),
                        true
                     );
                     notifyControlledTarget(player, target);
                  }
               } else if (!consumeMana(player, 55.0)) {
                  player.displayClientMessage(
                     Component.literal("Not enough mana to pressure " + target.getDisplayName().getString()).withStyle(ChatFormatting.RED), true
                  );
                  SESSIONS.remove(player.getUUID());
               } else {
                  player.displayClientMessage(Component.literal("This target is too powerful for Ruler's Hand").withStyle(ChatFormatting.RED), true);
                  session.controlledId = target.getUUID();
                  session.controlledEntity = target;
                  session.bossResistance = true;
                  session.weight = weight;
                  session.auraEntity = spawnControlAura(player.serverLevel(), target, session.authority, true);
                  notifyControlledTarget(player, target);
               }
            } else if (!consumeMana(player, 55.0)) {
               player.displayClientMessage(
                  Component.literal("Not enough mana to pressure " + target.getDisplayName().getString()).withStyle(ChatFormatting.RED), true
               );
               SESSIONS.remove(player.getUUID());
            } else {
               player.displayClientMessage(
                  Component.literal(target.getDisplayName().getString() + " resisted Ruler's Hand").withStyle(ChatFormatting.RED), true
               );
               session.controlledId = target.getUUID();
               session.controlledEntity = target;
               session.bossResistance = true;
               session.weight = entityWeight(target);
               session.auraEntity = spawnControlAura(player.serverLevel(), target, session.authority, true);
               notifyControlledTarget(player, target);
            }
         }
      }
   }

   private static void performTap(ServerPlayer player, boolean pull, boolean charged, boolean authority) {
      if (!CooldownManager.isOnCooldown(player, "telekinesis")) {
         double range = authority ? 28.0 : 17.0;
         Entity directTarget = findLookTarget(player, range);
         double cost = (authority ? 100.0 : 65.0) + (charged ? 40.0 : 0.0);
         if (!consumeMana(player, cost)) {
            player.displayClientMessage(Component.literal("Not enough mana").withStyle(ChatFormatting.RED), true);
            CooldownManager.set(player, "telekinesis", 10);
         } else {
            List<Entity> targets;
            if (directTarget != null) {
               targets = List.of(directTarget);
            } else {
               targets = findConeTargets(player, range, authority ? 8 : 4, pull ? 0.88 : 0.82);
            }

            double strength = (authority ? 1.45 : 1.0) * (charged ? 1.35 : 1.0);

            for (Entity target : targets) {
               RulersAuthorityManager.ControlProfile control = createControlProfile(player, target, authority);
               if (control.negated) {
                  renderNegation(player.serverLevel(), target);
                  if (target == directTarget) {
                     player.displayClientMessage(
                        Component.literal(target.getDisplayName().getString() + " negated the force").withStyle(ChatFormatting.RED), true
                     );
                  }
               } else {
                  double targetStrength = strength * control.effectiveness;
                  if (pull) {
                     pullEntity(player, target, targetStrength);
                  } else {
                     pushEntity(player, target, targetStrength);
                  }
               }
            }

            if (authority && targets.isEmpty() && pull) {
               pullNearbyItems(player, range * 0.7, strength);
            }

            if (!hasDaggerTarget(targets)) {
               renderPressureWave(player.serverLevel(), player, pull, charged, range);
            }

            playTelekinesisSound(player, pull ? 1.35F : 0.95F);
            CooldownManager.set(player, "mana_refresh", 30);
            CooldownManager.set(player, "telekinesis", charged ? (authority ? 32 : 42) : (authority ? 20 : 28));
         }
      }
   }

   private static void pushEntity(ServerPlayer player, Entity target, double strength) {
      if (isBoss(target)) {
         staggerBoss(target, 16);
         strength *= 0.22;
      }

      Vec3 direction = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
      if (direction.lengthSqr() < 0.01) {
         direction = player.getLookAngle();
      }

      direction = direction.normalize();
      target.setDeltaMovement(direction.x * strength, Math.max(0.22, direction.y * strength + 0.28), direction.z * strength);
      if (target instanceof ThrownDaggerEntity dagger) {
         dagger.markRulersControlled(player.level().getGameTime());
      }

      markTelekineticLaunch(target, 120);
      markVelocityChanged(target);
      dealPressureDamage(player, target, isBoss(target) ? 0.35F : 1.0F);
   }

   private static void pullEntity(ServerPlayer player, Entity target, double strength) {
      if (isBoss(target)) {
         staggerBoss(target, 20);
         strength *= 0.18;
      }

      Vec3 destination = player.getEyePosition().add(player.getLookAngle().scale(2.2));
      Vec3 direction = destination.subtract(target.getBoundingBox().getCenter());
      double distanceScale = Mth.clamp(direction.length() / 7.0, 0.75, 1.8);
      Vec3 motion = direction.normalize().scale(strength * distanceScale);
      target.setDeltaMovement(motion);
      if (target instanceof ThrownDaggerEntity dagger) {
         dagger.markRulersControlled(player.level().getGameTime());
      }

      markTelekineticLaunch(target, 120);
      markVelocityChanged(target);
      dealPressureDamage(player, target, isBoss(target) ? 0.25F : 0.65F);
   }

   private static void updateControlledPosition(ServerPlayer player, Entity target, RulersAuthorityManager.ControlSession session) {
      Vec3 desiredCenter = player.getEyePosition().add(player.getLookAngle().scale(session.distance));
      Vec3 currentCenter = target.getBoundingBox().getCenter();
      Vec3 correction = desiredCenter.subtract(currentCenter);
      double maximumSpeed = session.authority ? 2.2 : 1.55;
      Vec3 motion = correction.scale(session.authority ? 0.42 : 0.34);
      if (motion.length() > maximumSpeed) {
         motion = motion.normalize().scale(maximumSpeed);
      }

      target.setDeltaMovement(motion);
      if (target instanceof ThrownDaggerEntity dagger) {
         dagger.markRulersControlled(player.level().getGameTime());
      }

      target.fallDistance = 0.0F;
      if (target instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3, 5, false, false, false));
      }

      if (target instanceof Mob mob) {
         mob.getNavigation().stop();
      }

      markVelocityChanged(target);
   }

   private static void throwControlled(ServerPlayer player, Entity target, RulersAuthorityManager.ControlSession session, boolean slam) {
      discardAura(session);
      restoreGravity(target, session);
      Vec3 look = player.getLookAngle().normalize();
      double speed = session.authority ? 2.45 : 1.85;
      Vec3 velocity = slam ? new Vec3(look.x * speed * 0.6, -Math.max(1.7, speed), look.z * speed * 0.6) : look.scale(speed).add(0.0, 0.12, 0.0);
      target.setDeltaMovement(velocity);
      markTelekineticLaunch(target, 200);
      if (target instanceof Projectile projectile) {
         projectile.setOwner(player);
      }

      if (target instanceof ThrownDaggerEntity dagger) {
         dagger.onRulersReleased();
      }

      markVelocityChanged(target);
      if (target instanceof LivingEntity) {
         float impactDamage = telekineticDamage(player, session.authority) * (slam ? 1.35F : 1.0F);
         THROWN.put(target.getUUID(), new RulersAuthorityManager.ThrownState(target, player, impactDamage, slam, session.authority));
      }

      renderReleaseEffect(player.serverLevel(), target, slam);
      playTelekinesisSound(player, slam ? 0.72F : 1.05F);
      CooldownManager.set(player, "mana_refresh", 35);
      CooldownManager.set(player, "telekinesis", session.authority ? 30 : 42);
   }

   private static void applyBossPressure(ServerPlayer player, Entity target, RulersAuthorityManager.ControlSession session) {
      if (target instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 4, session.authority ? 2 : 0, false, false, false));
         if (target instanceof Mob mob) {
            mob.getNavigation().stop();
         }
      }

      target.setDeltaMovement(target.getDeltaMovement().scale(session.authority ? 0.35 : 0.7));
      markVelocityChanged(target);
   }

   private static void finishBossPressure(ServerPlayer player, Entity target, RulersAuthorityManager.ControlSession session) {
      discardAura(session);
      if (target instanceof LivingEntity living) {
         float damage = telekineticDamage(player, session.authority) * (session.authority ? 0.65F : 0.2F);
         living.hurt(player.damageSources().playerAttack(player), damage);
         living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, session.authority ? 45 : 15, session.authority ? 2 : 0, false, false, false));
      }

      renderReleaseEffect(player.serverLevel(), target, true);
      playTelekinesisSound(player, 0.65F);
      CooldownManager.set(player, "telekinesis", session.authority ? 45 : 60);
   }

   private static void deflectNearbyProjectiles(ServerPlayer player) {
      for (Projectile projectile : player.level()
         .getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(3.0), projectilex -> projectilex.isAlive() && projectilex.getOwner() != player)) {
         Vec3 away = projectile.position().subtract(player.getEyePosition());
         if (away.lengthSqr() < 0.01) {
            away = player.getLookAngle();
         }

         double speed = Math.max(1.1, projectile.getDeltaMovement().length());
         projectile.setOwner(player);
         projectile.setDeltaMovement(away.normalize().scale(speed * 1.2));
         markVelocityChanged(projectile);
         player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 4, 0.08, 0.08, 0.08, 0.08);
      }
   }

   private static boolean advanceBreakout(ServerPlayer controller, Entity target, RulersAuthorityManager.ControlSession session) {
      if (target instanceof LivingEntity living && session.breakoutLimitTicks > 0) {
         double struggle = 1.0;
         if (target instanceof ServerPlayer targetPlayer) {
            if (targetPlayer.isShiftKeyDown()) {
               struggle *= 2.6;
            }

            if (targetPlayer.isSprinting()) {
               struggle *= 1.25;
            }
         } else if (target instanceof Mob mob && mob.isAggressive()) {
            struggle *= 1.12;
         }

         if (living.hurtTime > 0) {
            struggle *= 1.35;
         }

         if (session.unstableControl) {
            struggle *= 1.2;
         }

         session.breakoutProgress = session.breakoutProgress + struggle / session.breakoutLimitTicks;
         if (session.breakoutProgress < 1.0) {
            return false;
         }

         discardAura(session);
         restoreGravity(target, session);
         if (!session.bossResistance) {
            Vec3 away = target.position().subtract(controller.position());
            if (away.lengthSqr() < 0.01) {
               away = controller.getLookAngle().scale(-1.0);
            }

            away = away.normalize().scale(0.62);
            target.setDeltaMovement(away.x, 0.28, away.z);
            markTelekineticLaunch(target, 50);
            markVelocityChanged(target);
         }

         renderNegation(controller.serverLevel(), target);
         playTelekinesisSound(controller, 0.56F);
         controller.displayClientMessage(Component.literal(target.getDisplayName().getString() + " broke free").withStyle(ChatFormatting.RED), true);
         if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.displayClientMessage(
               Component.literal("You broke free from " + controller.getName().getString()).withStyle(ChatFormatting.GREEN), true
            );
         }

         CooldownManager.set(controller, "telekinesis", session.authority ? 42 : 55);
         SESSIONS.remove(controller.getUUID());
         return true;
      } else {
         return false;
      }
   }

   private static RulersAuthorityManager.ControlProfile createControlProfile(ServerPlayer controller, Entity target, boolean authority) {
      if (!(target instanceof LivingEntity)) {
         return new RulersAuthorityManager.ControlProfile(1.0, 1.0, false, false, 0, 1.0);
      }

      double casterPower = playerPower(controller) + (authority ? 12.0 : 0.0);
      double targetPower = entityPower(target) * healthControlFactor(target);
      double gap = targetPower - casterPower;
      int negateGap = target instanceof Player ? 26 : (isBoss(target) ? 24 : 34);
      boolean negated = gap >= negateGap;
      boolean unstable = gap >= 10.0;
      double breakoutTicks;
      if (target instanceof Player) {
         breakoutTicks = Mth.clamp(84.0 - gap * 2.0, 18.0, 150.0);
      } else {
         breakoutTicks = Mth.clamp(130.0 - gap * 2.2, 28.0, 240.0);
         if (isBoss(target)) {
            breakoutTicks = Math.min(breakoutTicks, 65.0);
         }
      }

      double effectiveness = Mth.clamp(1.0 - gap * 0.018, 0.18, 1.35);
      return new RulersAuthorityManager.ControlProfile(casterPower, targetPower, negated, unstable, (int)Math.round(breakoutTicks), effectiveness);
   }

   private static double playerPower(Player player) {
      SololevelingModVariables.PlayerVariables variables = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      return Math.max(1.0, Math.max(variables.Level, rankLevel((int)Math.round(variables.HunterRank))));
   }

   private static double entityPower(Entity target) {
      if (target instanceof Player player) {
         return playerPower(player);
      } else if (target instanceof HunterEntity hunter) {
         return switch (hunter.getEntityData().get(HunterEntity.DATA_Rank).toUpperCase()) {
            case "S" -> 110.0;
            case "A" -> 82.0;
            case "B" -> 58.0;
            case "C" -> 38.0;
            case "D" -> 22.0;
            case "E" -> 10.0;
            default -> estimateLivingPower(hunter);
         };
      } else {
         String[] levelKeys = new String[]{"sl_level", "entity_level", "shadow_level", "level", "Level"};

         for (String key : levelKeys) {
            if (target.getPersistentData().contains(key, 99)) {
               double storedLevel = target.getPersistentData().getDouble(key);
               if (storedLevel > 0.0) {
                  return storedLevel;
               }
            }
         }

         if (target.getCustomName() != null) {
            String name = target.getCustomName().getString();
            int marker = name.lastIndexOf("Lv.");
            if (marker >= 0) {
               int start = marker + 3;
               int end = start;

               while (end < name.length() && Character.isDigit(name.charAt(end))) {
                  end++;
               }

               if (end > start) {
                  try {
                     return Double.parseDouble(name.substring(start, end));
                  } catch (NumberFormatException var8) {
                  }
               }
            }
         }

         return target instanceof LivingEntity living ? estimateLivingPower(living) : 1.0;
      }
   }

   private static double estimateLivingPower(LivingEntity living) {
      AttributeInstance attack = living.getAttribute(Attributes.ATTACK_DAMAGE);
      AttributeInstance armor = living.getAttribute(Attributes.ARMOR);
      double attackValue = attack == null ? 0.0 : attack.getValue();
      double armorValue = armor == null ? 0.0 : armor.getValue();
      double estimate = living.getMaxHealth() * 0.24 + attackValue * 1.4 + armorValue * 0.55;
      if (isBoss(living)) {
         estimate += 18.0;
      }

      return Mth.clamp(estimate, 1.0, 200.0);
   }

   private static double healthControlFactor(Entity target) {
      if (target instanceof LivingEntity living && !(living.getMaxHealth() <= 0.0F)) {
         double healthRatio = Mth.clamp(living.getHealth() / living.getMaxHealth(), 0.0, 1.0);
         return 0.52 + Math.sqrt(healthRatio) * 0.48;
      } else {
         return 1.0;
      }
   }

   private static double rankLevel(int rank) {
      return switch (rank) {
         case 1 -> 10.0;
         case 2 -> 22.0;
         case 3 -> 38.0;
         case 4 -> 58.0;
         case 5 -> 82.0;
         case 6 -> 110.0;
         default -> 1.0;
      };
   }

   private static void notifyControlledTarget(ServerPlayer controller, Entity target) {
      if (target instanceof ServerPlayer targetPlayer) {
         targetPlayer.displayClientMessage(
            Component.literal("Held by " + controller.getName().getString() + " - hold Sneak to break free").withStyle(ChatFormatting.AQUA), true
         );
      }
   }

   private static RulersAuthorityAuraEntity spawnControlAura(ServerLevel level, Entity target, boolean authority, boolean resisted) {
      return suppressDaggerRulerEffects(target) ? null : RulersAuthorityAuraEntity.spawn(level, target, authority, resisted);
   }

   private static void renderNegation(ServerLevel level, Entity target) {
      if (!suppressDaggerRulerEffects(target)) {
         Vec3 center = target.getBoundingBox().getCenter();
         level.sendParticles(
            ParticleTypes.ELECTRIC_SPARK,
            center.x,
            center.y,
            center.z,
            30,
            target.getBbWidth() * 0.48,
            target.getBbHeight() * 0.35,
            target.getBbWidth() * 0.48,
            0.18
         );
         level.sendParticles(
            ParticleTypes.ENCHANTED_HIT,
            center.x,
            center.y,
            center.z,
            18,
            target.getBbWidth() * 0.35,
            target.getBbHeight() * 0.28,
            target.getBbWidth() * 0.35,
            0.08
         );
      }
   }

   private static Entity findLookTarget(ServerPlayer player, double range) {
      ThrownDaggerEntity priorityDagger = findPriorityDagger(player, range);
      if (priorityDagger != null) {
         return priorityDagger;
      }

      Vec3 start = player.getEyePosition();
      Vec3 end = start.add(player.getLookAngle().scale(range));
      HitResult blockHit = player.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, player));
      if (blockHit.getType() != Type.MISS) {
         end = blockHit.getLocation();
      }

      AABB search = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.2);
      EntityHitResult hit = ProjectileUtil.getEntityHitResult(
         player, start, end, search, candidate -> isValidTarget(player, candidate), start.distanceToSqr(end)
      );
      return hit == null ? null : hit.getEntity();
   }

   private static List<Entity> findConeTargets(ServerPlayer player, double range, int maximum, double minimumDot) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      List<Entity> targets = new ArrayList<>();

      for (Entity candidate : player.level().getEntities(player, player.getBoundingBox().inflate(range), entity -> isValidTarget(player, entity))) {
         Vec3 direction = candidate.getBoundingBox().getCenter().subtract(eye);
         if (!(direction.lengthSqr() > range * range)
            && !(direction.lengthSqr() < 0.01)
            && !(look.dot(direction.normalize()) < minimumDot)
            && player.hasLineOfSight(candidate)) {
            targets.add(candidate);
         }
      }

      targets.sort(Comparator.comparingDouble(player::distanceToSqr));
      return targets.size() > maximum ? new ArrayList<>(targets.subList(0, maximum)) : targets;
   }

   private static boolean isValidTarget(ServerPlayer player, Entity candidate) {
      if (candidate == null
         || candidate == player
         || !candidate.isAlive()
         || candidate.isSpectator()
         || candidate.isPassengerOfSameVehicle(player)
         || candidate == player.getVehicle()) {
         return false;
      } else if (candidate instanceof ThrownDaggerEntity dagger) {
         return dagger.isPhysical() && dagger.isOwnedBy(player);
      } else if (!(candidate instanceof LivingEntity) && !(candidate instanceof ItemEntity) && !(candidate instanceof Projectile)) {
         return false;
      } else if (candidate instanceof TamableAnimal tame && tame.isOwnedBy(player)) {
         return false;
      } else {
         return candidate instanceof Projectile projectile && projectile.getOwner() == player
            ? false
            : !(candidate instanceof Player other && sameParty(player, other));
      }
   }

   private static ThrownDaggerEntity findPriorityDagger(ServerPlayer player, double range) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      return player.level()
         .getEntitiesOfClass(
            ThrownDaggerEntity.class,
            player.getBoundingBox().inflate(range),
            dagger -> dagger.isAlive() && dagger.isPhysical() && dagger.isOwnedBy(player) && dagger.distanceToSqr(player) <= range * range
         )
         .stream()
         .filter(dagger -> {
            Vec3 direction = dagger.getBoundingBox().getCenter().subtract(eye);
            return direction.lengthSqr() > 0.01 && look.dot(direction.normalize()) >= 0.35 && player.hasLineOfSight(dagger);
         })
         .max(Comparator.comparingDouble(dagger -> look.dot(dagger.getBoundingBox().getCenter().subtract(eye).normalize())))
         .orElse(null);
   }

   private static boolean sameParty(ServerPlayer player, Player other) {
      String playerParty = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(data -> data.party).orElse("");
      String otherParty = other.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(data -> data.party).orElse("");
      return !playerParty.isBlank() && playerParty.equals(otherParty);
   }

   private static boolean isBoss(Entity entity) {
      return entity instanceof LivingEntity && (entity.getType().is(BOSS_TAG) || StealthBossDetectionHelper.seesThroughStealth(entity));
   }

   private static double entityWeight(Entity entity) {
      double volume = Math.max(0.25, entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight());
      double health = entity instanceof LivingEntity living ? living.getMaxHealth() / 45.0 : 0.5;
      return Mth.clamp(0.5 + volume * 0.35 + health, 0.75, 10.0);
   }

   private static double controlInitialCost(ServerPlayer player, Entity target, boolean authority, double weight) {
      if (target instanceof ThrownDaggerEntity dagger) {
         double meleeDamage = daggerMeleeDamage(dagger.getDaggerStack());
         double maxMana = maximumMana(player);
         double base = authority ? 240.0 : 170.0;
         double damageScale = authority ? 14.0 : 10.0;
         double percent = authority ? 0.035 : 0.025;
         return base + meleeDamage * damageScale + maxMana * percent;
      } else {
         return (authority ? 120.0 : 80.0) + weight * 8.0;
      }
   }

   private static double controlDrain(ServerPlayer player, Entity target, RulersAuthorityManager.ControlSession session) {
      if (target instanceof ThrownDaggerEntity dagger) {
         double meleeDamage = daggerMeleeDamage(dagger.getDaggerStack());
         double maxMana = maximumMana(player);
         double base = session.authority ? 92.0 : 64.0;
         double damageScale = session.authority ? 8.0 : 5.5;
         double percent = session.authority ? 0.018 : 0.012;
         return base + meleeDamage * damageScale + maxMana * percent;
      } else {
         return (session.authority ? 18.0 : 12.0) + session.weight * 3.0;
      }
   }

   private static double daggerMeleeDamage(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         double addition = 0.0;
         double multiplier = 1.0;

         for (AttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getOperation() == Operation.ADDITION) {
               addition += modifier.getAmount();
            } else if (modifier.getOperation() == Operation.MULTIPLY_BASE) {
               multiplier += modifier.getAmount();
            } else if (modifier.getOperation() == Operation.MULTIPLY_TOTAL) {
               multiplier *= 1.0 + modifier.getAmount();
            }
         }

         double enchantment = EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
         return Mth.clamp(addition * multiplier + enchantment, 4.0, 40.0);
      } else {
         return 4.0;
      }
   }

   private static double maximumMana(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables variables = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
      if (variables == null) {
         return 1000.0;
      }

      double fallback = 1000.0 + Math.max(0.0, TemporaryStatBonusManager.effectiveIntelligence(player)) * 100.0;
      return Math.max(1.0, Math.max(Math.max(variables.Mana, variables.MP), fallback));
   }

   private static void dealPressureDamage(ServerPlayer player, Entity target, float multiplier) {
      if (target instanceof LivingEntity living) {
         living.hurt(player.damageSources().playerAttack(player), telekineticDamage(player, hasAuthority(player)) * 0.35F * multiplier);
      }
   }

   private static float telekineticDamage(ServerPlayer player, boolean authority) {
      double intelligence = TemporaryStatBonusManager.effectiveIntelligence(player);
      return (float)((authority ? 7.0 : 4.0) + Math.min(authority ? 24.0 : 12.0, intelligence * (authority ? 0.065 : 0.04)));
   }

   private static Entity findThrowCollision(ServerPlayer owner, Entity thrown) {
      AABB area = thrown.getBoundingBox().inflate(0.45);
      List<LivingEntity> hits = thrown.level()
         .getEntitiesOfClass(LivingEntity.class, area, candidate -> candidate != thrown && candidate != owner && isValidTarget(owner, candidate));
      return hits.isEmpty() ? null : hits.get(0);
   }

   private static void resolveImpact(ServerPlayer owner, Entity thrown, Entity collision, RulersAuthorityManager.ThrownState state) {
      Vec3 impact = thrown.getBoundingBox().getCenter();
      if (thrown instanceof LivingEntity living) {
         living.hurt(owner.damageSources().playerAttack(owner), state.damage);
      }

      if (collision instanceof LivingEntity living) {
         living.hurt(owner.damageSources().playerAttack(owner), state.damage);
      }

      double radius = state.authority ? 3.0 : 2.0;

      for (LivingEntity nearby : thrown.level()
         .getEntitiesOfClass(
            LivingEntity.class,
            new AABB(impact, impact).inflate(radius),
            entity -> entity != thrown && entity != collision && entity != owner && isValidTarget(owner, entity)
         )) {
         nearby.hurt(owner.damageSources().playerAttack(owner), state.damage * 0.55F);
         Vec3 knockback = nearby.position().subtract(impact);
         if (knockback.lengthSqr() > 0.01) {
            knockback = knockback.normalize().scale(state.authority ? 0.8 : 0.5);
            nearby.setDeltaMovement(nearby.getDeltaMovement().add(knockback.x, 0.25, knockback.z));
            markVelocityChanged(nearby);
         }
      }

      if (!suppressDaggerRulerEffects(thrown) && thrown.level() instanceof ServerLevel level) {
         level.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y, impact.z, state.slammed ? 3 : 1, 0.25, 0.2, 0.25, 0.02);
         level.sendParticles(ParticleTypes.ELECTRIC_SPARK, impact.x, impact.y, impact.z, state.authority ? 24 : 14, radius * 0.45, 0.35, radius * 0.45, 0.18);
      }

      playTelekinesisSound(owner, state.slammed ? 0.58F : 0.78F);
   }

   private static void pullNearbyItems(ServerPlayer player, double range, double strength) {
      for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(range), Entity::isAlive)) {
         Vec3 direction = player.getEyePosition().subtract(item.position());
         if (direction.lengthSqr() > 0.01) {
            item.setDeltaMovement(direction.normalize().scale(strength * 1.2));
            item.hasImpulse = true;
         }
      }
   }

   private static void staggerBoss(Entity target, int duration) {
      if (target instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, false, false));
      }
   }

   private static void renderControlEffect(ServerLevel level, ServerPlayer player, Entity target, RulersAuthorityManager.ControlSession session) {
      if (!suppressDaggerRulerEffects(target)) {
         if (player.tickCount % 2 == 0) {
            Vec3 start = player.getEyePosition().add(player.getLookAngle().scale(0.45));
            Vec3 end = target.getBoundingBox().getCenter();
            Vec3 span = end.subtract(start);
            int points = Math.max(3, Math.min(9, (int)(span.length() / 2.0)));

            for (int i = 1; i <= points; i++) {
               Vec3 point = start.add(span.scale((double)i / (points + 1)));
               level.sendParticles(i % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 1, 0.015, 0.015, 0.015, 0.0);
            }

            level.sendParticles(
               session.bossResistance ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.ELECTRIC_SPARK,
               end.x,
               end.y,
               end.z,
               session.authority ? 3 : 1,
               target.getBbWidth() * 0.25,
               target.getBbHeight() * 0.2,
               target.getBbWidth() * 0.25,
               0.03
            );
         }
      }
   }

   private static void renderPressureWave(ServerLevel level, ServerPlayer player, boolean pull, boolean charged, double range) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      Vec3 side = look.cross(new Vec3(0.0, 1.0, 0.0));
      if (side.lengthSqr() < 0.01) {
         side = new Vec3(1.0, 0.0, 0.0);
      }

      side = side.normalize();
      Vec3 up = side.cross(look).normalize();
      int rings = charged ? 5 : 3;

      for (int ring = 1; ring <= rings; ring++) {
         double distance = Math.min(range, ring * (charged ? 3.0 : 2.4));
         double radius = distance * 0.18;

         for (int point = 0; point < 10; point++) {
            double angle = (Math.PI * 2) * point / 10.0;
            Vec3 offset = side.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius));
            Vec3 position = eye.add(look.scale(distance)).add(offset);
            level.sendParticles(pull ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.ELECTRIC_SPARK, position.x, position.y, position.z, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }

   private static void renderReleaseEffect(ServerLevel level, Entity target, boolean slam) {
      if (!suppressDaggerRulerEffects(target)) {
         Vec3 center = target.getBoundingBox().getCenter();
         level.sendParticles(
            slam ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.ELECTRIC_SPARK,
            center.x,
            center.y,
            center.z,
            16,
            target.getBbWidth() * 0.3,
            target.getBbHeight() * 0.25,
            target.getBbWidth() * 0.3,
            0.12
         );
      }
   }

   private static boolean hasDaggerTarget(List<Entity> targets) {
      for (Entity target : targets) {
         if (suppressDaggerRulerEffects(target)) {
            return true;
         }
      }

      return false;
   }

   private static boolean suppressDaggerRulerEffects(Entity target) {
      return target instanceof ThrownDaggerEntity;
   }

   private static void playTelekinesisSound(ServerPlayer player, float pitch) {
      player.level().playSound((Player)null, player.blockPosition(), SololevelingModSounds.TELEPUSH.get(), SoundSource.PLAYERS, 0.72F, pitch);
   }

   private static boolean consumeMana(ServerPlayer player, double amount) {
      if (player.isCreative()) {
         return true;
      } else {
         SololevelingModVariables.PlayerVariables variables = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
         if (variables != null && !(variables.MP < amount)) {
            variables.MP = Math.max(0.0, variables.MP - amount);
            variables.syncPlayerVariables(player);
            return true;
         } else {
            return false;
         }
      }
   }

   private static boolean canUse(ServerPlayer player) {
      return player != null && hasAbility(player)
         ? player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(data -> data.combatmode).orElse(false)
         : false;
   }

   private static Entity getControlledEntity(ServerPlayer player, RulersAuthorityManager.ControlSession session) {
      if (session == null || session.controlledId == null) {
         return null;
      }

      if (session.controlledEntity != null && !session.controlledEntity.isRemoved()) {
         return session.controlledEntity;
      }

      if (player.level() instanceof ServerLevel level) {
         session.controlledEntity = level.getEntity(session.controlledId);
      }

      return session.controlledEntity;
   }

   private static void cancelSession(ServerPlayer player, RulersAuthorityManager.ControlSession session) {
      discardAura(session);
      restoreGravity(getControlledEntity(player, session), session);
      SESSIONS.remove(player.getUUID());
      if (session.controlledId != null) {
         CooldownManager.set(player, "telekinesis", 20);
      }
   }

   private static void restoreGravity(Entity target, RulersAuthorityManager.ControlSession session) {
      if (target != null && !session.bossResistance) {
         target.setNoGravity(session.originalNoGravity);
      }
   }

   private static void discardAura(RulersAuthorityManager.ControlSession session) {
      if (session != null && session.auraEntity != null && !session.auraEntity.isRemoved()) {
         session.auraEntity.discard();
      }

      if (session != null) {
         session.auraEntity = null;
      }
   }

   private static void markVelocityChanged(Entity target) {
      target.hasImpulse = true;
      if (target instanceof ServerPlayer serverPlayer) {
         serverPlayer.hurtMarked = true;
      }
   }

   private static void markTelekineticLaunch(Entity target, int durationTicks) {
      if (target instanceof LivingEntity) {
         long expiry = target.level().getGameTime() + Math.max(1, durationTicks);
         target.getPersistentData().putLong("sl_telekinesis_launch_protection_until", expiry);
      }
   }

   private static boolean containsAbility(String abilities, String id) {
      if (abilities != null && !abilities.isBlank()) {
         for (String token : abilities.replace('"', ' ').trim().split("\\s+")) {
            if (id.equalsIgnoreCase(token)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static final class ControlProfile {
      private final double casterPower;
      private final double targetPower;
      private final boolean negated;
      private final boolean unstable;
      private final int breakoutTicks;
      private final double effectiveness;

      private ControlProfile(double casterPower, double targetPower, boolean negated, boolean unstable, int breakoutTicks, double effectiveness) {
         this.casterPower = casterPower;
         this.targetPower = targetPower;
         this.negated = negated;
         this.unstable = unstable;
         this.breakoutTicks = breakoutTicks;
         this.effectiveness = effectiveness;
      }
   }

   private static final class ControlSession {
      private final long startedAt;
      private final boolean sneakingAtStart;
      private final boolean authority;
      private double distance;
      private UUID controlledId;
      private Entity controlledEntity;
      private RulersAuthorityAuraEntity auraEntity;
      private boolean bossResistance;
      private boolean originalNoGravity;
      private boolean unstableControl;
      private double weight = 1.0;
      private double casterPower;
      private double targetPower;
      private double breakoutProgress;
      private int breakoutLimitTicks;
      private int drainTicker;

      private ControlSession(long startedAt, boolean sneakingAtStart, boolean authority, double distance) {
         this.startedAt = startedAt;
         this.sneakingAtStart = sneakingAtStart;
         this.authority = authority;
         this.distance = distance;
      }
   }

   private static final class ThrownState {
      private final Entity target;
      private final ServerPlayer owner;
      private final float damage;
      private final boolean slammed;
      private final boolean authority;
      private int ticks;

      private ThrownState(Entity target, ServerPlayer owner, float damage, boolean slammed, boolean authority) {
         this.target = target;
         this.owner = owner;
         this.damage = damage;
         this.slammed = slammed;
         this.authority = authority;
      }
   }
}
