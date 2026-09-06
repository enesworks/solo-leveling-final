package dev.eness.sololevelingfinal.core.util;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import org.joml.Vector3f;

public final class GrandMarshalAbilityManager {
   public static final String COOLDOWN_KEY = "grand_marshal_authority";
   private static final DustParticleOptions SHADOW_PURPLE = new DustParticleOptions(new Vector3f(0.48F, 0.1F, 0.92F), 1.2F);
   private static final DustParticleOptions CRIMSON = new DustParticleOptions(new Vector3f(0.95F, 0.05F, 0.12F), 1.25F);
   private static final DustParticleOptions SKY_BLUE = new DustParticleOptions(new Vector3f(0.25F, 0.78F, 1.0F), 1.0F);

   private GrandMarshalAbilityManager() {
   }

   public static boolean cast(Entity caster) {
      if (caster instanceof ServerPlayer player) {
         if (!DeveloperModeManager.isEnabled(player)) {
            player.displayClientMessage(Component.literal("WIP (Work in progress)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
            return false;
         }

         if (!VesselProgressionManager.isShadowMonarch(player)) {
            player.displayClientMessage(Component.literal("§cOnly the Shadow Monarch can use this authority."), true);
            return false;
         }

         ShadowMonarchManager.GrandMarshalCommander commander = ShadowMonarchManager.activeGrandMarshal(player);
         if (commander == null) {
            player.displayClientMessage(Component.literal("§cYour Grand Marshal must be summoned and alive."), true);
            return false;
         }

         GrandMarshalAbilityManager.AbilitySpec spec = specification(commander.type());
         if (spec == null) {
            return false;
         }

         if (CooldownManager.isOnCooldown(player, "grand_marshal_authority")) {
            player.displayClientMessage(
               Component.literal("§cGrand Marshal Authority is recharging. §7(" + CooldownManager.getRemainingSeconds(player, "grand_marshal_authority") + "s)"),
               true
            );
            return false;
         }

         if (!consumeMana(player, spec.manaCost())) {
            return false;
         }

         linkCommander(player, commander.entity());
         switch (commander.type()) {
            case "igris":
               castCrimsonCross(player, commander.level());
               break;
            case "beru":
               castKingsRestoration(player, commander);
               break;
            case "tusk":
               castGravitationalRuin(player, commander.level());
               break;
            case "kamish":
               castDragonsDread(player, commander.level());
               break;
            case "kaisel":
               castSkyRend(player, commander.level());
               break;
            default:
               return false;
         }

         CooldownManager.set(player, "grand_marshal_authority", spec.cooldownTicks());
         CooldownManager.set(player, "mana_refresh", 40);
         player.displayClientMessage(
            Component.literal("§5" + commander.name() + "§7 — §d" + ShadowMonarchManager.grandMarshalSignatureName(commander.type())), true
         );
         return true;
      } else {
         return false;
      }
   }

   private static void castCrimsonCross(ServerPlayer player, int shadowLevel) {
      ServerLevel level = player.serverLevel();
      Vec3 origin = player.position().add(0.0, player.getBbHeight() * 0.55, 0.0);
      Vec3 forward = horizontalLook(player);
      float damage = (float)(14.0 + TemporaryStatBonusManager.effectiveStrength(player) / 10.0 + shadowPower(shadowLevel));
      AABB search = player.getBoundingBox().inflate(11.0, 4.0, 11.0);

      for (LivingEntity target : targets(player, search)) {
         Vec3 offset = target.getBoundingBox().getCenter().subtract(origin);
         double along = offset.dot(forward);
         double perpendicular = offset.subtract(forward.scale(along)).length();
         if (along > 0.0 && along <= 10.5 && perpendicular <= 2.25 + target.getBbWidth() * 0.5) {
            dealDamage(player, target, damage);
            target.knockback(0.45, -forward.x, -forward.z);
         }
      }

      AbilityDestructionManager.fissure(
         player,
         AbilityDestructionManager.Profile.LIU_SWORD_CUT,
         player.position(),
         forward,
         10.5,
         TemporaryStatBonusManager.effectiveStrength(player) + shadowPower(shadowLevel) * 10.0,
         shadowLevel >= 4
      );

      for (int i = 1; i <= 26; i++) {
         double distance = i * 0.4;
         Vec3 center = origin.add(forward.scale(distance));
         Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
         double cross = (i - 13) * 0.075;
         level.sendParticles(
            i % 2 == 0 ? CRIMSON : SHADOW_PURPLE, center.x + right.x * cross, center.y + cross, center.z + right.z * cross, 1, 0.02, 0.02, 0.02, 0.0
         );
         level.sendParticles(
            i % 2 == 0 ? SHADOW_PURPLE : CRIMSON, center.x - right.x * cross, center.y + cross, center.z - right.z * cross, 1, 0.02, 0.02, 0.02, 0.0
         );
      }

      level.playSound((Player)null, BlockPos.containing(player.position()), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.65F);
   }

   private static void castKingsRestoration(ServerPlayer player, ShadowMonarchManager.GrandMarshalCommander commander) {
      ServerLevel level = player.serverLevel();
      float playerHealing = player.getMaxHealth() * 0.28F + (float)Math.min(18.0, shadowPower(commander.level()));
      player.heal(playerHealing);
      player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true));
      player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 180, 1, false, true));
      commander.entity().heal(commander.entity().getMaxHealth() * 0.4F);

      for (LivingEntity living : level.getEntitiesOfClass(
         LivingEntity.class, player.getBoundingBox().inflate(16.0), target -> target.isAlive() && ShadowMonarchManager.isOwnedShadow(target, player)
      )) {
         living.heal(living.getMaxHealth() * 0.22F);
      }

      level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 55, 1.3, 1.2, 1.3, 0.05);
      level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.25, player.getZ(), 12, 0.8, 0.7, 0.8, 0.03);
      level.playSound((Player)null, BlockPos.containing(player.position()), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.8F);
   }

   private static void castGravitationalRuin(ServerPlayer player, int shadowLevel) {
      ServerLevel level = player.serverLevel();
      Vec3 center = aimedCenter(player, 9.0);
      double radius = 5.5;
      float damage = (float)(18.0 + TemporaryStatBonusManager.effectiveIntelligence(player) / 9.0 + shadowPower(shadowLevel) * 1.15);

      for (LivingEntity target : targets(player, new AABB(center, center).inflate(radius))) {
         if (!(target.position().distanceToSqr(center) > radius * radius)) {
            dealDamage(player, target, damage);
            Vec3 pull = center.subtract(target.position());
            Vec3 horizontal = new Vec3(pull.x, 0.0, pull.z);
            if (horizontal.lengthSqr() > 0.01) {
               horizontal = horizontal.normalize().scale(0.45);
            }

            target.setDeltaMovement(target.getDeltaMovement().add(horizontal.x, 0.55, horizontal.z));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true));
            target.hurtMarked = true;
         }
      }

      AbilityDestructionManager.impact(
         player,
         AbilityDestructionManager.Profile.GRAND_MARSHAL_GRAVITY,
         center,
         TemporaryStatBonusManager.effectiveIntelligence(player) + shadowPower(shadowLevel) * 10.35,
         shadowLevel >= 4
      );
      drawRing(level, center.add(0.0, 0.15, 0.0), radius, SHADOW_PURPLE, 48);
      drawRing(level, center.add(0.0, 0.45, 0.0), radius * 0.62, SHADOW_PURPLE, 32);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.7, center.z, 70, 2.2, 0.9, 2.2, 0.12);
      level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 4, 1.0, 0.5, 1.0, 0.0);
      level.playSound((Player)null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.15F, 0.7F);
   }

   private static void castDragonsDread(ServerPlayer player, int shadowLevel) {
      ServerLevel level = player.serverLevel();
      Vec3 origin = player.getEyePosition().add(0.0, -0.25, 0.0);
      Vec3 forward = player.getLookAngle().normalize();
      float damage = (float)(
         24.0
            + TemporaryStatBonusManager.effectiveStrength(player) / 16.0
            + TemporaryStatBonusManager.effectiveIntelligence(player) / 11.0
            + shadowPower(shadowLevel) * 1.35
      );

      for (LivingEntity target : targets(player, player.getBoundingBox().inflate(14.0, 8.0, 14.0))) {
         Vec3 offset = target.getBoundingBox().getCenter().subtract(origin);
         double distance = offset.length();
         if (!(distance <= 0.1) && !(distance > 13.0)) {
            double alignment = offset.normalize().dot(forward);
            if (!(alignment < 0.68)) {
               dealDamage(player, target, damage);
               target.setSecondsOnFire(5);
               target.knockback(0.65, -forward.x, -forward.z);
            }
         }
      }

      AbilityDestructionManager.line(
         player,
         AbilityDestructionManager.Profile.GRAND_MARSHAL_DREAD,
         origin,
         origin.add(forward.scale(13.0)),
         TemporaryStatBonusManager.effectiveIntelligence(player)
            + TemporaryStatBonusManager.effectiveStrength(player) * 0.6875
            + shadowPower(shadowLevel) * 14.85,
         shadowLevel >= 4
      );

      for (int step = 1; step <= 18; step++) {
         double distance = step * 0.72;
         Vec3 point = origin.add(forward.scale(distance));
         double spread = 0.08 + distance * 0.055;
         level.sendParticles(step % 3 == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, point.x, point.y, point.z, 5, spread, spread, spread, 0.04);
      }

      level.playSound((Player)null, BlockPos.containing(player.position()), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.1F, 0.82F);
   }

   private static void castSkyRend(ServerPlayer player, int shadowLevel) {
      ServerLevel level = player.serverLevel();
      Vec3 center = aimedCenter(player, 10.0);
      double radius = 4.5;
      float damage = (float)(
         16.0
            + TemporaryStatBonusManager.effectiveAgility(player) / 10.0
            + TemporaryStatBonusManager.effectiveIntelligence(player) / 18.0
            + shadowPower(shadowLevel)
      );

      for (LivingEntity target : targets(player, new AABB(center, center).inflate(radius, 5.0, radius))) {
         if (!(target.position().distanceToSqr(center) > radius * radius * 1.3)) {
            dealDamage(player, target, damage);
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.75, 0.0));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, true));
            target.hurtMarked = true;
         }
      }

      AbilityDestructionManager.impact(
         player,
         AbilityDestructionManager.Profile.GRAND_MARSHAL_SKY_REND,
         center,
         TemporaryStatBonusManager.effectiveAgility(player) + TemporaryStatBonusManager.effectiveIntelligence(player) * 0.556 + shadowPower(shadowLevel) * 10.0,
         shadowLevel >= 4
      );

      for (int i = 0; i < 34; i++) {
         double y = center.y + 7.0 - i * 0.23;
         double angle = i * 1.7;
         double radiusAtPoint = 0.25 + i % 5 * 0.08;
         level.sendParticles(
            i % 3 == 0 ? SKY_BLUE : ParticleTypes.ELECTRIC_SPARK,
            center.x + Math.cos(angle) * radiusAtPoint,
            y,
            center.z + Math.sin(angle) * radiusAtPoint,
            1,
            0.03,
            0.03,
            0.03,
            0.02
         );
      }

      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.5, center.z, 55, 1.8, 1.0, 1.8, 0.18);
      level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.2, center.z, 28, 1.4, 0.35, 1.4, 0.08);
      level.playSound((Player)null, BlockPos.containing(center), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.85F, 1.35F);
   }

   private static List<LivingEntity> targets(ServerPlayer player, AABB area) {
      return player.serverLevel().getEntitiesOfClass(LivingEntity.class, area, target -> validTarget(player, target));
   }

   private static boolean validTarget(ServerPlayer player, LivingEntity target) {
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

   private static boolean dealDamage(ServerPlayer player, LivingEntity target, float damage) {
      if (!validTarget(player, target)) {
         return false;
      }

      target.invulnerableTime = 0;
      return target.hurt(player.damageSources().playerAttack(player), Math.max(0.5F, damage));
   }

   private static boolean consumeMana(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      } else {
         SololevelingModVariables.PlayerVariables variables = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (variables.MP < amount) {
            player.displayClientMessage(Component.literal("§cNot enough MP! §7(" + amount + " required)"), true);
            return false;
         } else {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = Math.max(0.0, capability.MP - amount);
               capability.syncPlayerVariables(player);
            });
            return true;
         }
      }
   }

   private static void linkCommander(ServerPlayer player, LivingEntity commander) {
      ServerLevel level = player.serverLevel();
      Vec3 start = commander.getEyePosition();
      Vec3 end = player.getEyePosition();
      Vec3 route = end.subtract(start);
      int steps = Math.max(5, (int)Math.ceil(route.length() * 2.0));

      for (int i = 0; i <= steps; i++) {
         Vec3 point = start.add(route.scale((double)i / steps));
         level.sendParticles(i % 3 == 0 ? ParticleTypes.SOUL_FIRE_FLAME : SHADOW_PURPLE, point.x, point.y, point.z, 1, 0.03, 0.03, 0.03, 0.0);
      }
   }

   private static Vec3 horizontalLook(ServerPlayer player) {
      Vec3 look = player.getLookAngle();
      Vec3 horizontal = new Vec3(look.x, 0.0, look.z);
      return horizontal.lengthSqr() < 0.001 ? new Vec3(0.0, 0.0, 1.0) : horizontal.normalize();
   }

   private static Vec3 aimedCenter(ServerPlayer player, double distance) {
      Vec3 look = player.getLookAngle();
      return player.getEyePosition().add(look.scale(distance)).add(0.0, -1.0, 0.0);
   }

   private static double shadowPower(int shadowLevel) {
      return Math.min(200, Math.max(1, shadowLevel)) * 0.12;
   }

   private static void drawRing(ServerLevel level, Vec3 center, double radius, DustParticleOptions particle, int points) {
      for (int i = 0; i < points; i++) {
         double angle = (Math.PI * 2) * i / points;
         level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   private static GrandMarshalAbilityManager.AbilitySpec specification(String type) {
      return switch (type) {
         case "igris" -> new GrandMarshalAbilityManager.AbilitySpec(280, 160);
         case "beru" -> new GrandMarshalAbilityManager.AbilitySpec(320, 280);
         case "tusk" -> new GrandMarshalAbilityManager.AbilitySpec(460, 240);
         case "kamish" -> new GrandMarshalAbilityManager.AbilitySpec(600, 360);
         case "kaisel" -> new GrandMarshalAbilityManager.AbilitySpec(340, 200);
         default -> null;
      };
   }

   private record AbilitySpec(int manaCost, int cooldownTicks) {
   }
}
