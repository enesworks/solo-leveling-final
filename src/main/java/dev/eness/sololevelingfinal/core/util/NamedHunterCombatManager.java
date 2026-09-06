package dev.eness.sololevelingfinal.core.util;

import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BaekYoonhoEntity;
import dev.eness.sololevelingfinal.core.entity.ChaHaeInEntity;
import dev.eness.sololevelingfinal.core.entity.ChoijongEntity;

@EventBusSubscriber
public final class NamedHunterCombatManager {
   private static final String PROFILE_VERSION = "slr_named_hunter_profile";
   private static final int CURRENT_PROFILE_VERSION = 2;
   private static final String RETALIATION_TARGET = "slr_named_hunter_retaliation_target";
   private static final String RETALIATION_UNTIL = "slr_named_hunter_retaliation_until";
   private static final String DEFENSE_READY_AT = "slr_named_hunter_defense_ready";
   private static final long RETALIATION_TICKS = 600L;

   private NamedHunterCombatManager() {
   }

   public static boolean isNamedHunter(Entity entity) {
      return entity instanceof ChoijongEntity || entity instanceof ChaHaeInEntity || entity instanceof BaekYoonhoEntity;
   }

   public static void tick(PathfinderMob hunter) {
      if (!hunter.level().isClientSide()) {
         ensureFixedProfile(hunter);
         CompoundTag data = hunter.getPersistentData();
         long now = hunter.level().getGameTime();
         if (data.hasUUID("slr_named_hunter_retaliation_target") && data.getLong("slr_named_hunter_retaliation_until") >= now) {
            if (hunter.level() instanceof ServerLevel serverLevel) {
               if (serverLevel.getEntity(data.getUUID("slr_named_hunter_retaliation_target")) instanceof LivingEntity target
                  && canRetaliateAgainst(hunter, target)
                  && !(hunter.distanceToSqr(target) > 16384.0)) {
                  if (hunter.getTarget() != target) {
                     hunter.setTarget(target);
                  }
               } else {
                  clearRetaliation(data);
               }
            }
         } else {
            clearRetaliation(data);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onNamedHunterAttacked(LivingAttackEvent event) {
      if (event != null && !event.isCanceled() && isNamedHunter(event.getEntity()) && !event.getEntity().level().isClientSide()) {
         LivingEntity attacker = resolveAttacker(event.getEntity(), event.getSource().getEntity(), event.getSource().getDirectEntity());
         if (attacker != null && canRetaliateAgainst(event.getEntity(), attacker)) {
            PathfinderMob hunter = (PathfinderMob)event.getEntity();
            hunter.setLastHurtByMob(attacker);
            hunter.setTarget(attacker);
            CompoundTag data = hunter.getPersistentData();
            data.putUUID("slr_named_hunter_retaliation_target", attacker.getUUID());
            data.putLong("slr_named_hunter_retaliation_until", hunter.level().getGameTime() + 600L);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onNamedHunterHurt(LivingHurtEvent event) {
      if (event != null
         && !event.isCanceled()
         && !(event.getAmount() <= 0.0F)
         && isNamedHunter(event.getEntity())
         && !event.getEntity().level().isClientSide()
         && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
         && !SilladDamageTypes.isTrueFrost(event.getSource())) {
         LivingEntity hunter = event.getEntity();
         LivingEntity attacker = resolveAttacker(hunter, event.getSource().getEntity(), event.getSource().getDirectEntity());
         if (attacker != null && canRetaliateAgainst(hunter, attacker)) {
            long now = hunter.level().getGameTime();
            CompoundTag data = hunter.getPersistentData();
            if (data.getLong("slr_named_hunter_defense_ready") <= now) {
               double attributeThreat = combatPower(attacker) / Math.max(1.0, combatPower(hunter));
               double strikeThreat = event.getAmount() / Math.max(4.0, hunter.getMaxHealth() * 0.08);
               double threat = Mth.clamp(Math.max(attributeThreat, strikeThreat), 0.35, 2.5);
               boolean reacted = react(event, hunter, attacker, threat);
               long delay = hunter instanceof ChaHaeInEntity ? (reacted ? 30L : 8L) : (reacted ? 14L : 4L);
               data.putLong("slr_named_hunter_defense_ready", now + delay);
            }
         }
      }
   }

   private static boolean react(LivingHurtEvent event, LivingEntity hunter, LivingEntity attacker, double threat) {
      double roll = hunter.getRandom().nextDouble();
      if (hunter instanceof ChoijongEntity) {
         double dodgeChance = Mth.clamp(0.16 + threat * 0.18, 0.22, 0.58);
         return roll < dodgeChance && dodge(event, hunter, attacker, 1.0);
      }

      if (hunter instanceof ChaHaeInEntity) {
         double dodgeChance = threat >= 1.0 ? Mth.clamp(0.12 + (threat - 1.0) * 0.08, 0.12, 0.24) : 0.06;
         double blockChance = threat >= 1.0 ? 0.1 : 0.18;
         if (threat >= 1.0) {
            return roll < dodgeChance ? dodge(event, hunter, attacker, 0.82) : roll < dodgeChance + blockChance && block(event, hunter, 0.7F);
         } else {
            return roll < blockChance ? block(event, hunter, 0.7F) : roll < blockChance + dodgeChance && dodge(event, hunter, attacker, 0.76);
         }
      } else {
         double dodgeChance = threat >= 1.35 ? Mth.clamp(0.14 + (threat - 1.35) * 0.1, 0.14, 0.27) : 0.06;
         double blockChance = threat >= 1.35 ? 0.34 : 0.43;
         if (threat >= 1.35) {
            return roll < dodgeChance ? dodge(event, hunter, attacker, 0.68) : roll < dodgeChance + blockChance && block(event, hunter, 0.42F);
         } else {
            return roll < blockChance ? block(event, hunter, 0.42F) : roll < blockChance + dodgeChance && dodge(event, hunter, attacker, 0.62);
         }
      }
   }

   private static boolean dodge(LivingHurtEvent event, LivingEntity hunter, LivingEntity attacker, double strength) {
      event.setCanceled(true);
      Vec3 away = hunter.position().subtract(attacker.position());
      away = new Vec3(away.x, 0.0, away.z);
      if (away.lengthSqr() < 1.0E-5) {
         away = new Vec3(hunter.getLookAngle().x, 0.0, hunter.getLookAngle().z).scale(-1.0);
      }

      away = away.normalize();
      Vec3 side = new Vec3(-away.z, 0.0, away.x).scale(hunter.getRandom().nextBoolean() ? 1.0 : -1.0);
      Vec3 movement = away.scale(0.62).add(side.scale(0.78)).normalize().scale(strength);
      hunter.setDeltaMovement(movement.x, Math.max(0.1, hunter.getDeltaMovement().y), movement.z);
      hunter.hasImpulse = true;
      if (hunter instanceof PathfinderMob pathfinder) {
         pathfinder.getNavigation().stop();
      }

      particles(hunter, ParticleTypes.POOF, 10, 0.04);
      hunter.level().playSound((Player)null, hunter.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.55F, 1.25F);
      return true;
   }

   private static boolean block(LivingHurtEvent event, LivingEntity hunter, float damageMultiplier) {
      event.setAmount(Math.max(0.5F, event.getAmount() * damageMultiplier));
      hunter.setDeltaMovement(hunter.getDeltaMovement().multiply(0.35, 1.0, 0.35));
      particles(hunter, ParticleTypes.CRIT, 9, 0.02);
      hunter.level().playSound((Player)null, hunter.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.7F, 0.9F);
      return true;
   }

   private static void ensureFixedProfile(PathfinderMob hunter) {
      CompoundTag data = hunter.getPersistentData();
      if (data.getInt("slr_named_hunter_profile") != 2) {
         NamedHunterCombatManager.Profile profile = profile(hunter);
         if (profile != null) {
            float healthRatio = hunter.getMaxHealth() > 0.0F ? Mth.clamp(hunter.getHealth() / hunter.getMaxHealth(), 0.0F, 1.0F) : 1.0F;
            setBase(hunter, Attributes.MAX_HEALTH, profile.health());
            setBase(hunter, Attributes.ARMOR, profile.armor());
            setBase(hunter, Attributes.ATTACK_DAMAGE, profile.damage());
            setBase(hunter, Attributes.MOVEMENT_SPEED, profile.speed());
            setBase(hunter, Attributes.FOLLOW_RANGE, 64.0);
            setBase(hunter, Attributes.KNOCKBACK_RESISTANCE, profile.knockbackResistance());
            setBase(hunter, Attributes.ATTACK_KNOCKBACK, profile.attackKnockback());
            hunter.setHealth(Math.max(1.0F, (float)(profile.health() * healthRatio)));
            hunter.setCustomName(profile.name());
            hunter.setCustomNameVisible(true);
            data.remove("Level");
            data.remove("SLRLevelStatMultiplier");
            if (hunter instanceof ChoijongEntity) {
               data.putDouble("int", 95.0);
            }

            data.putInt("slr_named_hunter_profile", 2);
         }
      }
   }

   private static NamedHunterCombatManager.Profile profile(PathfinderMob hunter) {
      if (hunter instanceof ChoijongEntity) {
         return new NamedHunterCombatManager.Profile(240.0, 8.0, 12.0, 0.36, 0.2, 0.0, Component.literal("Choi Jong-In"));
      } else if (hunter instanceof ChaHaeInEntity) {
         return new NamedHunterCombatManager.Profile(260.0, 16.0, 22.0, 0.39, 0.25, 0.45, Component.literal("Cha Hae-In"));
      } else {
         return hunter instanceof BaekYoonhoEntity
            ? new NamedHunterCombatManager.Profile(340.0, 34.0, 27.0, 0.41, 0.55, 1.0, Component.literal("Baek Yoonho"))
            : null;
      }
   }

   private static void setBase(LivingEntity entity, Attribute attribute, double value) {
      if (entity.getAttribute(attribute) != null) {
         entity.getAttribute(attribute).setBaseValue(value);
      }
   }

   private static double combatPower(LivingEntity entity) {
      AttributeInstance attack = entity.getAttribute(Attributes.ATTACK_DAMAGE);
      return entity.getMaxHealth() * 0.08 + entity.getArmorValue() * 1.2 + Math.max(0.0, attack == null ? 0.0 : attack.getValue()) * 4.0;
   }

   private static LivingEntity resolveAttacker(LivingEntity victim, Entity source, Entity directSource) {
      Entity resolved = source != null ? source : directSource;
      if (resolved instanceof Projectile projectile && projectile.getOwner() != null) {
         resolved = projectile.getOwner();
      }

      if (resolved instanceof TamableAnimal tame && tame.isTame() && tame.getOwner() != null) {
         resolved = tame.getOwner();
      }

      UUID shadowOwner = resolved == null ? null : ShadowMonarchManager.getShadowOwnerUUID(resolved);
      if (shadowOwner != null && victim.level() instanceof ServerLevel serverLevel) {
         Player owner = serverLevel.getPlayerByUUID(shadowOwner);
         if (owner != null) {
            resolved = owner;
         }
      }

      return resolved instanceof LivingEntity living ? living : null;
   }

   private static boolean canRetaliateAgainst(LivingEntity hunter, LivingEntity target) {
      return target != hunter && target.isAlive() && !isNamedHunter(target)
         ? !(target instanceof Player player && (player.isCreative() || player.isSpectator()))
         : false;
   }

   private static void clearRetaliation(CompoundTag data) {
      data.remove("slr_named_hunter_retaliation_target");
      data.remove("slr_named_hunter_retaliation_until");
   }

   private static void particles(LivingEntity entity, ParticleOptions type, int count, double speed) {
      if (entity.level() instanceof ServerLevel level) {
         level.sendParticles(type, entity.getX(), entity.getY() + entity.getBbHeight() * 0.55, entity.getZ(), count, 0.4, 0.55, 0.4, speed);
      }
   }

   private record Profile(double health, double armor, double damage, double speed, double knockbackResistance, double attackKnockback, Component name) {
   }
}
