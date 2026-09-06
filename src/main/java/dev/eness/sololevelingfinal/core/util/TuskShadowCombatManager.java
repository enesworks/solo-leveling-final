package dev.eness.sololevelingfinal.core.util;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import dev.eness.sololevelingfinal.core.entity.ai.TuskShadowCombatPolicy;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public final class TuskShadowCombatManager {
   private static final String INITIALIZED = "sl_tusk_combat_initialized";
   private static final String NEXT_SOUL_FLAME_AT = "sl_tusk_soul_flame_at";
   private static final String NEXT_CURSE_FIELD_AT = "sl_tusk_curse_field_at";
   private static final String NEXT_GROUND_SMASH_AT = "sl_tusk_ground_smash_at";
   private static final String NEXT_HYMN_AT = "sl_tusk_hymn_at";
   private static final String NEXT_GLOBAL_CAST_AT = "sl_tusk_global_cast_at";
   private static final String CAST_KIND = "sl_tusk_cast_kind";
   private static final String CAST_TARGET = "sl_tusk_cast_target";
   private static final String CAST_RELEASE_AT = "sl_tusk_cast_release_at";
   private static final String CAST_END_AT = "sl_tusk_cast_end_at";
   private static final String CAST_RELEASED = "sl_tusk_cast_released";
   private static final String SOUL_FLAME = "soul_flame";
   private static final String CURSE_FIELD = "curse_field";
   private static final String GROUND_SMASH = "ground_smash";
   private static final String HYMN_DEFENSE = "hymn_defense";
   private static final String HYMN_OFFENSE = "hymn_offense";
   private static final int SOUL_FLAME_CAST_TICKS = 15;
   private static final int SOUL_FLAME_RELEASE_TICK = 10;
   private static final int CURSE_FIELD_CAST_TICKS = 15;
   private static final int CURSE_FIELD_RELEASE_TICK = 11;
   private static final int HYMN_CAST_TICKS = 15;
   private static final int HYMN_RELEASE_TICK = 10;
   private static final int GROUND_SMASH_CAST_TICKS = 25;
   private static final int GROUND_SMASH_RELEASE_TICK = 17;
   private static final int GLOBAL_RECOVERY_TICKS = 4;
   private static final double SOUL_FLAME_RANGE = 24.0;
   private static final double CURSE_FIELD_RANGE = 22.0;
   private static final double CURSE_FIELD_RADIUS = 4.0;
   private static final double GROUND_SMASH_TRIGGER_RANGE = 5.5;
   private static final double GROUND_SMASH_RADIUS = 6.5;
   private static final double OWNER_BUFF_RANGE = 32.0;
   private static final int CURSE_FIELD_MAX_TARGETS = 6;
   private static final int GROUND_SMASH_MAX_TARGETS = 8;

   private TuskShadowCombatManager() {
   }

   public static void tick(TuskShadowEntity tusk) {
      if (tusk != null && tusk.level() instanceof ServerLevel level && tusk.isAlive()) {
         initializeSchedule(tusk, level.getGameTime());
         if (!tickActiveCast(tusk, level)) {
            Player owner = ShadowMonarchManager.getShadowOwnerPlayer(tusk);
            LivingEntity target = tusk.getTarget();
            if (owner != null && owner.isAlive() && isSafeTarget(tusk, target)) {
               if (!(tusk.distanceToSqr(owner) > square(32.0))) {
                  long now = level.getGameTime();
                  if (now >= tusk.getPersistentData().getLong("sl_tusk_global_cast_at")) {
                     double distance = CombatRangeHelper.surfaceDistance(tusk, target);
                     boolean hasLineOfSight = tusk.getSensing().hasLineOfSight(target);
                     CompoundTag data = tusk.getPersistentData();
                     if (now >= data.getLong("sl_tusk_ground_smash_at")) {
                        List<LivingEntity> closeThreats = validTargets(tusk, tusk.position(), 6.5, 2);
                        if (!closeThreats.isEmpty() && (CombatRangeHelper.surfaceDistance(tusk, closeThreats.get(0)) <= 5.5 || closeThreats.size() >= 2)) {
                           beginCast(tusk, closeThreats.get(0), "ground_smash", "groundsmash", 17, 25);
                           data.putLong("sl_tusk_ground_smash_at", now + 280L + tusk.getRandom().nextInt(61));
                           return;
                        }
                     }

                     if (TuskShadowCombatPolicy.isUsefulCastingPosition(distance, hasLineOfSight)) {
                        if (now >= data.getLong("sl_tusk_hymn_at")
                           && tusk.distanceToSqr(owner) <= square(32.0)
                           && owner.getHealth() <= owner.getMaxHealth() * 0.45F) {
                           beginCast(tusk, owner, "hymn_defense", "cast", 10, 15);
                           data.putLong("sl_tusk_hymn_at", now + 400L + tusk.getRandom().nextInt(81));
                        } else if (now >= data.getLong("sl_tusk_curse_field_at") && distance <= 22.0 && countValidTargets(tusk, target.position(), 4.0, 2) >= 2
                           )
                         {
                           beginCast(tusk, target, "curse_field", "cast", 11, 15);
                           data.putLong("sl_tusk_curse_field_at", now + 210L + tusk.getRandom().nextInt(51));
                        } else if (now >= data.getLong("sl_tusk_hymn_at") && tusk.distanceToSqr(owner) <= square(32.0)) {
                           beginCast(tusk, owner, "hymn_offense", "cast", 10, 15);
                           data.putLong("sl_tusk_hymn_at", now + 400L + tusk.getRandom().nextInt(81));
                        } else {
                           if (now >= data.getLong("sl_tusk_soul_flame_at") && distance <= 24.0) {
                              beginCast(tusk, target, "soul_flame", "cast", 10, 15);
                              data.putLong("sl_tusk_soul_flame_at", now + 55L + tusk.getRandom().nextInt(21));
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static boolean isCasting(TuskShadowEntity tusk) {
      if (tusk == null) {
         return false;
      }

      CompoundTag data = tusk.getPersistentData();
      return !data.getString("sl_tusk_cast_kind").isEmpty() && tusk.level().getGameTime() <= data.getLong("sl_tusk_cast_end_at");
   }

   private static void initializeSchedule(TuskShadowEntity tusk, long now) {
      CompoundTag data = tusk.getPersistentData();
      if (!data.getBoolean("sl_tusk_combat_initialized")) {
         data.putBoolean("sl_tusk_combat_initialized", true);
         data.putLong("sl_tusk_soul_flame_at", now);
         data.putLong("sl_tusk_curse_field_at", now + 60L + Math.floorMod(tusk.getId(), 41));
         data.putLong("sl_tusk_ground_smash_at", now + 100L + Math.floorMod(tusk.getId(), 61));
         data.putLong("sl_tusk_hymn_at", now + 80L + Math.floorMod(tusk.getId(), 41));
         data.putLong("sl_tusk_global_cast_at", now);
         clearCast(data);
      }
   }

   private static void beginCast(TuskShadowEntity tusk, LivingEntity target, String kind, String animation, int releaseDelay, int duration) {
      long now = tusk.level().getGameTime();
      CompoundTag data = tusk.getPersistentData();
      data.putString("sl_tusk_cast_kind", kind);
      data.putUUID("sl_tusk_cast_target", target.getUUID());
      data.putLong("sl_tusk_cast_release_at", now + releaseDelay);
      data.putLong("sl_tusk_cast_end_at", now + duration);
      data.putBoolean("sl_tusk_cast_released", false);
      data.putLong("sl_tusk_global_cast_at", now + duration + 4L);
      tusk.getNavigation().stop();
      tusk.setCombatState("casting_" + kind);
      tusk.setAnimation(animation);
      if (tusk.level() instanceof ServerLevel level) {
         level.playSound(
            (Player)null, tusk.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.NEUTRAL, 0.85F, kind.equals("ground_smash") ? 0.72F : 1.08F
         );
      }
   }

   private static boolean tickActiveCast(TuskShadowEntity tusk, ServerLevel level) {
      CompoundTag data = tusk.getPersistentData();
      String kind = data.getString("sl_tusk_cast_kind");
      if (kind.isEmpty()) {
         return false;
      }

      long now = level.getGameTime();
      long endAt = data.getLong("sl_tusk_cast_end_at");
      if (now > endAt + 1L) {
         clearCast(data);
         tusk.setCombatState("idle");
         return false;
      }

      LivingEntity castTarget = resolveCastTarget(level, data);
      if (castTarget != null) {
         tusk.getLookControl().setLookAt(castTarget, 60.0F, 60.0F);
      }

      tusk.getNavigation().stop();
      tusk.setCombatState("casting_" + kind);
      if (!data.getBoolean("sl_tusk_cast_released") && now >= data.getLong("sl_tusk_cast_release_at")) {
         data.putBoolean("sl_tusk_cast_released", true);
         releaseCast(tusk, level, castTarget, kind);
      }

      if (now >= endAt) {
         clearCast(data);
         tusk.setCombatState("holding");
         return false;
      } else {
         return true;
      }
   }

   private static LivingEntity resolveCastTarget(ServerLevel level, CompoundTag data) {
      if (!data.hasUUID("sl_tusk_cast_target")) {
         return null;
      } else {
         return level.getEntity(data.getUUID("sl_tusk_cast_target")) instanceof LivingEntity living ? living : null;
      }
   }

   private static void releaseCast(TuskShadowEntity tusk, ServerLevel level, LivingEntity target, String kind) {
      switch (kind) {
         case "soul_flame":
            releaseSoulFlame(tusk, level, target);
            break;
         case "curse_field":
            releaseCurseField(tusk, level, target);
            break;
         case "ground_smash":
            releaseGroundSmash(tusk, level);
            break;
         case "hymn_defense":
            releaseHymn(tusk, level, target, true);
            break;
         case "hymn_offense":
            releaseHymn(tusk, level, target, false);
      }
   }

   private static void releaseSoulFlame(TuskShadowEntity tusk, ServerLevel level, LivingEntity target) {
      if (canReleaseAtTarget(tusk, target, 24.0)) {
         Vec3 start = tusk.getEyePosition().add(0.0, -0.25, 0.0);
         Vec3 end = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
         Vec3 delta = end.subtract(start);

         for (int point = 0; point <= 8; point++) {
            Vec3 position = start.add(delta.scale(point / 8.0));
            level.sendParticles(SololevelingModParticleTypes.MANA_PURPLE.get(), position.x, position.y, position.z, 1, 0.035, 0.035, 0.035, 0.0);
         }

         level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, end.x, end.y, end.z, 12, 0.35, 0.45, 0.35, 0.035);
         level.sendParticles(ParticleTypes.FLAME, end.x, end.y, end.z, 7, 0.28, 0.35, 0.28, 0.025);
         float damage = TuskShadowCombatPolicy.soulFlameDamage(tusk.getAttributeValue(Attributes.ATTACK_DAMAGE));
         if (target.hurt(magicDamage(level, tusk), damage)) {
            target.setSecondsOnFire(3);
         }

         level.playSound((Player)null, BlockPos.containing(end), SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 0.9F, 0.8F);
      }
   }

   private static void releaseCurseField(TuskShadowEntity tusk, ServerLevel level, LivingEntity target) {
      if (canReleaseAtTarget(tusk, target, 22.0)) {
         Vec3 center = target.position();
         level.sendParticles(SololevelingModParticleTypes.MANA_PURPLE.get(), center.x, center.y + 0.35, center.z, 28, 2.4, 0.35, 2.4, 0.055);
         level.sendParticles(ParticleTypes.WITCH, center.x, center.y + 0.55, center.z, 18, 2.0, 0.3, 2.0, 0.025);
         level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.15, center.z, 12, 2.2, 0.2, 2.2, 0.025);
         float damage = TuskShadowCombatPolicy.curseFieldDamage(tusk.getAttributeValue(Attributes.ATTACK_DAMAGE));

         for (LivingEntity affected : validTargets(tusk, center, 4.0, 6)) {
            affected.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true));
            affected.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, true));
            affected.hurt(magicDamage(level, tusk), damage);
         }
      }
   }

   private static void releaseGroundSmash(TuskShadowEntity tusk, ServerLevel level) {
      Vec3 center = tusk.position();
      level.sendParticles(SololevelingModParticleTypes.IMPACT_22.get(), center.x, center.y + 0.2, center.z, 22, 2.8, 0.18, 2.8, 0.08);
      level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.x, center.y + 0.15, center.z, 24, 3.0, 0.25, 3.0, 0.055);
      level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.2, center.z, 5, 1.8, 0.2, 1.8, 0.0);
      float damage = TuskShadowCombatPolicy.groundSmashDamage(tusk.getAttributeValue(Attributes.ATTACK_DAMAGE));

      for (LivingEntity affected : validTargets(tusk, center, 6.5, 8)) {
         if (affected.hurt(level.damageSources().mobAttack(tusk), damage)) {
            Vec3 away = affected.position().subtract(center).multiply(1.0, 0.0, 1.0);
            if (away.lengthSqr() > 1.0E-4) {
               away = away.normalize().scale(0.72);
               affected.push(away.x, 0.38, away.z);
            }
         }
      }

      level.playSound((Player)null, tusk.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.35F, 0.72F);
   }

   private static void releaseHymn(TuskShadowEntity tusk, ServerLevel level, LivingEntity target, boolean defensive) {
      Player owner = ShadowMonarchManager.getShadowOwnerPlayer(tusk);
      if (owner != null && target == owner && owner.isAlive() && !(tusk.distanceToSqr(owner) > square(36.0))) {
         if (defensive) {
            applyRespectingStronger(owner, MobEffects.DAMAGE_RESISTANCE, 140, 0);
            applyRespectingStronger(owner, MobEffects.ABSORPTION, 140, 0);
            applyRespectingStronger(owner, MobEffects.REGENERATION, 100, 0);
         } else {
            applyRespectingStronger(owner, MobEffects.DAMAGE_BOOST, 120, 0);
            applyRespectingStronger(owner, MobEffects.MOVEMENT_SPEED, 120, 0);
         }

         level.sendParticles(
            SololevelingModParticleTypes.MANA_PURPLE.get(),
            owner.getX(),
            owner.getY() + owner.getBbHeight() * 0.55,
            owner.getZ(),
            22,
            owner.getBbWidth() * 0.7,
            owner.getBbHeight() * 0.35,
            owner.getBbWidth() * 0.7,
            0.04
         );
         level.sendParticles(ParticleTypes.ENCHANT, owner.getX(), owner.getY() + 1.0, owner.getZ(), 14, 0.55, 0.75, 0.55, 0.055);
      }
   }

   private static void applyRespectingStronger(LivingEntity target, MobEffect effect, int duration, int amplifier) {
      MobEffectInstance active = target.getEffect(effect);
      if (active == null || active.getAmplifier() <= amplifier && (active.getAmplifier() != amplifier || active.getDuration() < duration)) {
         target.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true));
      }
   }

   private static boolean canReleaseAtTarget(TuskShadowEntity tusk, LivingEntity target, double range) {
      return isSafeTarget(tusk, target) && CombatRangeHelper.withinSurfaceRange(tusk, target, range) && tusk.getSensing().hasLineOfSight(target);
   }

   private static boolean isSafeTarget(TuskShadowEntity tusk, LivingEntity target) {
      return target != null && target.level() == tusk.level() && ShadowMonarchManager.canShadowDamage(tusk, target);
   }

   private static int countValidTargets(TuskShadowEntity tusk, Vec3 center, double radius, int stopAt) {
      int count = 0;
      AABB area = new AABB(center, center).inflate(radius, 2.5, radius);

      for (LivingEntity candidate : tusk.level()
         .getEntitiesOfClass(LivingEntity.class, area, target -> tusk.getSensing().hasLineOfSight(target) && ShadowMonarchManager.canShadowDamage(tusk, target))) {
         if (++count >= stopAt) {
            return count;
         }
      }

      return count;
   }

   private static List<LivingEntity> validTargets(TuskShadowEntity tusk, Vec3 center, double radius, int maximum) {
      AABB area = new AABB(center, center).inflate(radius, 2.5, radius);
      return tusk.level()
         .getEntitiesOfClass(
            LivingEntity.class,
            area,
            candidate -> candidate.distanceToSqr(center) <= radius * radius + 6.25
               && tusk.getSensing().hasLineOfSight(candidate)
               && ShadowMonarchManager.canShadowDamage(tusk, candidate)
         )
         .stream()
         .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center)))
         .limit(maximum)
         .toList();
   }

   private static DamageSource magicDamage(ServerLevel level, TuskShadowEntity tusk) {
      return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), tusk);
   }

   private static void clearCast(CompoundTag data) {
      data.remove("sl_tusk_cast_kind");
      data.remove("sl_tusk_cast_target");
      data.remove("sl_tusk_cast_release_at");
      data.remove("sl_tusk_cast_end_at");
      data.remove("sl_tusk_cast_released");
   }

   private static double square(double value) {
      return value * value;
   }
}
