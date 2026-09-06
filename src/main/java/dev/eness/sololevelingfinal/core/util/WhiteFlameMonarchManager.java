package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.DemonKnightEntity;
import dev.eness.sololevelingfinal.core.entity.RadiruBloodSpearEntity;
import dev.eness.sololevelingfinal.core.entity.WhiteFlameVfxEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class WhiteFlameMonarchManager {
   public static final String LIGHTNING_BREATH = "Lightning Breath";
   public static final String HELLSTORM_DOMINION = "Hellstorm Dominion";
   public static final String RADIRU_BLOOD_SPEAR = "Radiru Blood Spear";
   public static final String DOPPELGANGER = "Doppelganger";
   public static final String HELLS_ARMY = "Hell's Army";
   public static final String SPIRITUALIZATION = "White Flame Spiritualization";
   public static final String AURA_ID = "white_flame_spiritualization";
   public static final String DOPPELGANGER_AURA_ID = "white_flame_doppelganger";
   private static final String SPIRITUALIZED = "mowf_spiritualized";
   private static final String CHAIN_DODGE_UNTIL = "mowf_chain_dodge_until";
   private static final String LAST_DODGE_MOVE = "mowf_last_dodge_move";
   private static final String BREATH_UNTIL = "mowf_breath_until";
   private static final String DOMAIN_UNTIL = "mowf_domain_until";
   private static final String DOMAIN_NEXT = "mowf_domain_next";
   private static final String DOMAIN_LAST_TARGET = "mowf_domain_last_target";
   private static final String DOMAIN_REPEAT_HITS = "mowf_domain_repeat_hits";
   private static final String DOMAIN_TERRAIN_STRIKES = "mowf_domain_terrain_strikes";
   private static final String DOPPEL_CHARGES = "mowf_doppel_charges";
   private static final String DOPPEL_UNTIL = "mowf_doppel_until";
   private static final String BRAND_UNTIL = "mowf_brand_until";
   private static final String BRAND_STACKS = "mowf_brand_stacks";
   private static final String BRAND_OWNER = "mowf_brand_owner";
   public static final String SUMMON_OWNER = "mowf_summon_owner";
   public static final String SUMMON_UNTIL = "mowf_summon_until";
   private static final TagKey<EntityType<?>> SHADOWS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows"));

   private WhiteFlameMonarchManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null && player.server != null) {
         UUID ownerId = player.getUUID();
         List<DemonKnightEntity> summons = new ArrayList<>();

         for (ServerLevel level : player.server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
               if (entity instanceof DemonKnightEntity knight
                  && knight.getPersistentData().hasUUID("mowf_summon_owner")
                  && ownerId.equals(knight.getPersistentData().getUUID("mowf_summon_owner"))) {
                  summons.add(knight);
               }

               if (entity instanceof LivingEntity living
                  && living.getPersistentData().hasUUID("mowf_brand_owner")
                  && ownerId.equals(living.getPersistentData().getUUID("mowf_brand_owner"))) {
                  clearBrand(living);
               }
            }
         }

         for (DemonKnightEntity summon : summons) {
            summon.discard();
         }

         PlayerAuraSystem.clearContinuous(player);
      }
   }

   public static boolean isWhiteFlameVessel(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = variables(entity);
      return vars.JOB == 4.0 && (vars.vesselIdentity.isBlank() || "baran".equals(vars.vesselIdentity));
   }

   public static boolean isSpiritualized(Entity entity) {
      return isWhiteFlameVessel(entity) && entity.getPersistentData().getBoolean("mowf_spiritualized");
   }

   public static void castLightningBreath(Entity entity) {
      if (entity instanceof ServerPlayer player && isWhiteFlameVessel(player) && ready(player, "Lightning Breath")) {
         boolean spiritualized = isSpiritualized(player);
         if (consumeMana(player, spiritualized ? 300 : 220)) {
            CooldownManager.set(player, "Lightning Breath", spiritualized ? 62 : 78);
            player.getPersistentData().putLong("mowf_breath_until", player.level().getGameTime() + (spiritualized ? 18 : 14));
            player.level()
               .playSound((Player)null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.55F, spiritualized ? 1.75F : 1.45F);
         }
      }
   }

   public static void castHellstormDominion(Entity entity) {
      if (entity instanceof ServerPlayer player && isWhiteFlameVessel(player) && ready(player, "Hellstorm Dominion")) {
         boolean spiritualized = isSpiritualized(player);
         if (consumeMana(player, spiritualized ? 1050 : 850)) {
            long now = player.level().getGameTime();
            CooldownManager.set(player, "Hellstorm Dominion", spiritualized ? 330 : 390);
            player.getPersistentData().putLong("mowf_domain_until", now + (spiritualized ? 220 : 160));
            player.getPersistentData().putLong("mowf_domain_next", now + 4L);
            player.getPersistentData().remove("mowf_domain_last_target");
            player.getPersistentData().remove("mowf_domain_repeat_hits");
            player.getPersistentData().remove("mowf_domain_terrain_strikes");

            for (int wave = 0; wave < 3; wave++) {
               int delay = wave * 3;
               SololevelingMod.queueServerWork(delay, () -> {
                  if (player.isAlive() && player.getPersistentData().getLong("mowf_domain_until") >= player.level().getGameTime()) {
                     spawnDomainVolley(player.serverLevel(), player.position(), spiritualized ? 4 : 3, spiritualized ? 13.0 : 10.0);
                  }
               });
            }

            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.55F, 1.15F);
         }
      }
   }

   public static void castRadiruBloodSpear(Entity entity) {
      if (entity instanceof ServerPlayer player && isWhiteFlameVessel(player) && ready(player, "Radiru Blood Spear")) {
         boolean spiritualized = isSpiritualized(player);
         if (consumeMana(player, spiritualized ? 390 : 300)) {
            CooldownManager.set(player, "Radiru Blood Spear", spiritualized ? 72 : 96);
            float damage = magicDamage(player, spiritualized ? 38.0 : 28.0, spiritualized ? 5.6 : 7.0);
            RadiruBloodSpearEntity.launch(player, damage, spiritualized);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.1F, spiritualized ? 0.72F : 0.9F);
         }
      }
   }

   public static void castDoppelganger(Entity entity) {
      if (entity instanceof ServerPlayer player && isWhiteFlameVessel(player) && ready(player, "Doppelganger")) {
         boolean spiritualized = isSpiritualized(player);
         if (consumeMana(player, spiritualized ? 720 : 590)) {
            long now = player.level().getGameTime();
            CooldownManager.set(player, "Doppelganger", spiritualized ? 220 : 280);
            player.getPersistentData().putInt("mowf_doppel_charges", spiritualized ? 4 : 3);
            player.getPersistentData().putLong("mowf_doppel_until", now + (spiritualized ? 220 : 160));
            PlayerAuraSystem.burst(player, "white_flame_doppelganger", spiritualized ? 220 : 160, spiritualized ? 1.2F : 1.0F);
            WhiteFlameVfxEntity.spawn(player.serverLevel(), player.getX(), player.getY(), player.getZ(), 3, 1.1F, 2.4F, 24, player.getYRot(), 0.0F);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.35F);
         }
      }
   }

   public static void castHellsArmy(Entity entity) {
      if (entity instanceof ServerPlayer player && isWhiteFlameVessel(player) && ready(player, "Hell's Army")) {
         boolean spiritualized = isSpiritualized(player);
         if (consumeMana(player, spiritualized ? 1450 : 1200)) {
            CooldownManager.set(player, "Hell's Army", spiritualized ? 520 : 650);
            ServerLevel level = player.serverLevel();
            int count = spiritualized ? 7 : 5;
            long expiry = level.getGameTime() + (spiritualized ? 520 : 400);
            WhiteFlameVfxEntity.spawn(
               level, player.getX(), player.getY() + 0.05, player.getZ(), 4, spiritualized ? 5.5F : 4.2F, spiritualized ? 5.5F : 4.4F, 28, 0.0F, 0.0F
            );

            for (int i = 0; i < count; i++) {
               double angle = (Math.PI * 2) * i / count;
               BlockPos pos = BlockPos.containing(player.getX() + Math.cos(angle) * 3.2, player.getY(), player.getZ() + Math.sin(angle) * 3.2);
               DemonKnightEntity knight = SololevelingModEntities.DEMON_KNIGHT.get().spawn(level, pos, MobSpawnType.MOB_SUMMONED);
               if (knight != null) {
                  knight.randomizeVariant();
                  knight.getPersistentData().putUUID("mowf_summon_owner", player.getUUID());
                  knight.getPersistentData().putLong("mowf_summon_until", expiry);
                  knight.getPersistentData().putBoolean("mowf_no_loot", true);
                  knight.setCustomName(Component.literal("Radiru Royal Guard").withStyle(ChatFormatting.WHITE));
                  knight.setCustomNameVisible(false);
                  if (knight.getAttribute(Attributes.MAX_HEALTH) != null) {
                     double max = spiritualized ? 100.0 : 72.0;
                     knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(max);
                     knight.setHealth((float)max);
                  }

                  if (knight.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                     knight.getAttribute(Attributes.ATTACK_DAMAGE)
                        .setBaseValue(Math.min(34.0, (spiritualized ? 15.0 : 11.0) + TemporaryStatBonusManager.effectiveIntelligence(player) / 28.0));
                  }

                  knight.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int)(expiry - level.getGameTime()), 0, false, false));
               }
            }

            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.75F, 1.55F);
         }
      }
   }

   public static void toggleSpiritualization(Entity entity) {
      if (entity instanceof ServerPlayer player && isWhiteFlameVessel(player)) {
         if (isSpiritualized(player)) {
            disableSpiritualization(player, false);
         } else if (ready(player, "White Flame Spiritualization") && consumeMana(player, 800)) {
            player.getPersistentData().putBoolean("mowf_spiritualized", true);
            player.getPersistentData().remove("mowf_chain_dodge_until");
            PlayerAuraSystem.setContinuous(player, "white_flame_spiritualization", 1.3F);
            WhiteFlameVfxEntity.spawn(player.serverLevel(), player.getX(), player.getY() + 0.05, player.getZ(), 4, 3.8F, 5.2F, 30, 0.0F, 0.0F);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.65F);
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.level().isClientSide() && event.player instanceof ServerPlayer player) {
         long var4 = player.level().getGameTime();
         if (player.getPersistentData().getBoolean("mowf_spiritualized")) {
            if (!isWhiteFlameVessel(player)) {
               disableSpiritualization(player, true);
            } else if (player.tickCount % 20 == 0 && !drainMana(player, 14)) {
               disableSpiritualization(player, true);
            }
         }

         if (player.getPersistentData().getLong("mowf_doppel_until") < var4) {
            player.getPersistentData().remove("mowf_doppel_charges");
         }

         if (player.getPersistentData().getLong("mowf_breath_until") >= var4 && player.tickCount % 2 == 0) {
            breathPulse(player);
         }

         if (player.getPersistentData().getLong("mowf_domain_until") >= var4 && player.getPersistentData().getLong("mowf_domain_next") <= var4) {
            player.getPersistentData().putLong("mowf_domain_next", var4 + (isSpiritualized(player) ? 11 : 14));
            domainStrike(player);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
   public static void onPlayerAttacked(LivingAttackEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && isWhiteFlameVessel(player) && canDodge(event.getSource())) {
         long now = player.level().getGameTime();
         if (event.isCanceled()) {
            if (isSpiritualized(player)) {
               player.getPersistentData().putLong("mowf_chain_dodge_until", now + 10L);
               performDodge(player, event.getSource().getEntity(), false);
            }
         } else {
            boolean chain = isSpiritualized(player) && player.getPersistentData().getLong("mowf_chain_dodge_until") >= now;
            int echoes = player.getPersistentData().getInt("mowf_doppel_charges");
            boolean doppel = echoes > 0 && player.getPersistentData().getLong("mowf_doppel_until") >= now;
            boolean spiritualRoll = isSpiritualized(player) && player.getRandom().nextFloat() < 0.25F;
            if (chain || doppel || spiritualRoll && drainMana(player, 55)) {
               event.setCanceled(true);
               if (doppel) {
                  player.getPersistentData().putInt("mowf_doppel_charges", echoes - 1);
                  WhiteFlameVfxEntity.spawn(player.serverLevel(), player.getX(), player.getY(), player.getZ(), 3, 1.15F, 2.5F, 12, player.getYRot(), 0.0F);
                  if (event.getSource().getEntity() instanceof LivingEntity living && validTarget(player, living)) {
                     dealMagic(player, living, magicDamage(player, 13.0, 15.0));
                     brand(living, player, 80, 1);
                  }
               }

               if (isSpiritualized(player)) {
                  player.getPersistentData().putLong("mowf_chain_dodge_until", now + 10L);
               }

               performDodge(player, event.getSource().getEntity(), doppel || !chain);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onSummonTick(LivingTickEvent event) {
      if (event.getEntity() instanceof DemonKnightEntity knight
         && !knight.level().isClientSide()
         && knight.getPersistentData().hasUUID("mowf_summon_owner")
         && knight.level() instanceof ServerLevel level) {
         UUID var7 = knight.getPersistentData().getUUID("mowf_summon_owner");
         ServerPlayer owner = level.getServer().getPlayerList().getPlayer(var7);
         if (owner != null && owner.isAlive() && owner.level() == level && level.getGameTime() <= knight.getPersistentData().getLong("mowf_summon_until")) {
            LivingEntity target = knight.getTarget();
            if (target == null || !validTarget(owner, target)) {
               target = owner.getLastHurtMob();
               if (target == null || !validTarget(owner, target)) {
                  target = owner.getLastHurtByMob();
               }

               if (target == null || !validTarget(owner, target)) {
                  target = nearestHostileTarget(level, owner, knight.position(), 20.0);
               }

               knight.setTarget(target);
            }

            if (knight.distanceToSqr(owner) > 900.0) {
               knight.teleportTo(owner.getX() + 1.0, owner.getY(), owner.getZ() + 1.0);
            } else if (target == null && knight.distanceToSqr(owner) > 64.0) {
               knight.getNavigation().moveTo(owner, 1.15);
            }
         } else {
            knight.discard();
         }
      }
   }

   private static void breathPulse(ServerPlayer player) {
      ServerLevel level = player.serverLevel();
      boolean spiritualized = isSpiritualized(player);
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      double range = spiritualized ? 24.0 : 18.0;
      WhiteFlameVfxEntity.spawn(level, eye.x, eye.y - 0.12, eye.z, 0, spiritualized ? 2.6F : 1.9F, (float)range, 5, player.getYRot(), player.getXRot());
      float damage = magicDamage(player, spiritualized ? 7.0 : 5.0, spiritualized ? 24.0 : 30.0);

      for (LivingEntity target : level.getEntitiesOfClass(
         LivingEntity.class, player.getBoundingBox().inflate(range), candidate -> validTarget(player, candidate)
      )) {
         Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eye);
         double distance = toTarget.length();
         if (!(distance > range) && !(distance < 0.2)) {
            double projection = toTarget.dot(look);
            double sideDistance = toTarget.subtract(look.scale(projection)).length();
            if (projection > 0.0 && sideDistance <= (spiritualized ? 2.8 : 2.0) + distance * 0.06) {
               dealMagic(player, target, damage);
               brand(target, player, spiritualized ? 130 : 90, 1);
               target.setSecondsOnFire(spiritualized ? 4 : 2);
            }
         }
      }

      Vec3 intendedEnd = eye.add(look.scale(range));
      BlockHitResult terrainHit = level.clip(new ClipContext(eye, intendedEnd, Block.COLLIDER, Fluid.NONE, player));
      Vec3 terrainEnd = terrainHit.getType() == Type.BLOCK ? terrainHit.getLocation() : intendedEnd;
      AbilityDestructionManager.line(
         player, AbilityDestructionManager.Profile.WHITE_FLAME_BREATH, eye, terrainEnd, TemporaryStatBonusManager.effectiveIntelligence(player), spiritualized
      );
   }

   private static void domainStrike(ServerPlayer player) {
      ServerLevel level = player.serverLevel();
      boolean spiritualized = isSpiritualized(player);
      LivingEntity target = nearestTarget(level, player, player.position(), spiritualized ? 22.0 : 18.0, true);
      Vec3 point;
      if (target != null) {
         point = findDomainStrikePoint(level, target.getX(), target.getY(), target.getZ());
         float damage = magicDamage(player, spiritualized ? 13.0 : 10.0, spiritualized ? 26.0 : 32.0);
         float repeatMultiplier = domainRepeatMultiplier(player, target);
         dealMagic(player, target, damage * repeatMultiplier * (brandStacks(target, player) > 0 ? 1.12F : 1.0F));
         brand(target, player, spiritualized ? 150 : 110, 1);
      } else {
         player.getPersistentData().remove("mowf_domain_last_target");
         player.getPersistentData().remove("mowf_domain_repeat_hits");
         double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
         double radius = 4.0 + player.getRandom().nextDouble() * 10.0;
         point = findDomainStrikePoint(level, player.getX() + Math.cos(angle) * radius, player.getY(), player.getZ() + Math.sin(angle) * radius);
      }

      spawnVisualLightning(level, point);
      int terrainStrikes = player.getPersistentData().getInt("mowf_domain_terrain_strikes");
      if (terrainStrikes < (spiritualized ? 6 : 4)) {
         player.getPersistentData().putInt("mowf_domain_terrain_strikes", terrainStrikes + 1);
         AbilityDestructionManager.impact(
            player, AbilityDestructionManager.Profile.WHITE_FLAME_HELLSTORM, point, TemporaryStatBonusManager.effectiveIntelligence(player), spiritualized
         );
      }

      spawnDomainVolley(level, player.position(), spiritualized ? 2 : 1, spiritualized ? 15.0 : 11.0);
      level.playSound(
         (Player)null,
         BlockPos.containing(point),
         SoundEvents.LIGHTNING_BOLT_IMPACT,
         SoundSource.PLAYERS,
         0.65F,
         1.05F + player.getRandom().nextFloat() * 0.18F
      );
   }

   private static float domainRepeatMultiplier(ServerPlayer player, LivingEntity target) {
      boolean sameTarget = player.getPersistentData().hasUUID("mowf_domain_last_target")
         && target.getUUID().equals(player.getPersistentData().getUUID("mowf_domain_last_target"));
      int repeatHits = sameTarget ? player.getPersistentData().getInt("mowf_domain_repeat_hits") : 0;
      player.getPersistentData().putUUID("mowf_domain_last_target", target.getUUID());
      player.getPersistentData().putInt("mowf_domain_repeat_hits", repeatHits + 1);

      return switch (repeatHits) {
         case 0 -> 1.0F;
         case 1 -> 0.78F;
         case 2 -> 0.62F;
         default -> 0.5F;
      };
   }

   private static void spawnDomainVolley(ServerLevel level, Vec3 center, int count, double radius) {
      for (int i = 0; i < count; i++) {
         double angle = level.random.nextDouble() * Math.PI * 2.0;
         double distance = 2.0 + level.random.nextDouble() * Math.max(1.0, radius - 2.0);
         Vec3 point = findDomainStrikePoint(level, center.x + Math.cos(angle) * distance, center.y, center.z + Math.sin(angle) * distance);
         spawnVisualLightning(level, point);
      }
   }

   private static void spawnVisualLightning(ServerLevel level, Vec3 point) {
      LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
      if (bolt != null) {
         bolt.moveTo(point.x, point.y, point.z);
         bolt.setVisualOnly(true);
         bolt.setSilent(true);
         level.addFreshEntity(bolt);
      }
   }

   private static Vec3 findDomainStrikePoint(ServerLevel level, double x, double y, double z) {
      int bx = (int)Math.floor(x);
      int bz = (int)Math.floor(z);
      int top = Math.min(level.getMaxBuildHeight() - 2, (int)Math.floor(y) + 4);
      int bottom = Math.max(level.getMinBuildHeight() + 1, (int)Math.floor(y) - 14);
      MutableBlockPos cursor = new MutableBlockPos();

      for (int by = top; by >= bottom; by--) {
         cursor.set(bx, by, bz);
         BlockPos below = cursor.below();
         if (level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty() && !level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
            return Vec3.atBottomCenterOf(cursor);
         }
      }

      return new Vec3(x, y, z);
   }

   private static void performDodge(ServerPlayer player, Entity attacker, boolean move) {
      long now = player.level().getGameTime();
      if (move && player.getPersistentData().getLong("mowf_last_dodge_move") + 4L <= now) {
         Vec3 threat = attacker == null ? player.getLookAngle().scale(-1.0) : player.position().subtract(attacker.position()).normalize();
         Vec3 side = new Vec3(-threat.z, 0.0, threat.x).normalize().scale(player.getRandom().nextBoolean() ? 1.65 : -1.65);
         AABB moved = player.getBoundingBox().move(side.x, 0.15, side.z);
         if (player.level().noCollision(player, moved)) {
            player.connection.teleport(player.getX() + side.x, player.getY() + 0.15, player.getZ() + side.z, player.getYRot(), player.getXRot());
            player.getPersistentData().putLong("mowf_last_dodge_move", now);
         }
      }

      WhiteFlameVfxEntity.spawn(
         player.serverLevel(), player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(), 5, 1.35F, 1.0F, 8, player.getYRot(), 0.0F
      );
      player.level().playSound((Player)null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.45F, 1.8F);
   }

   private static boolean canDodge(DamageSource source) {
      return source.is(DamageTypes.PLAYER_ATTACK)
         || source.is(DamageTypes.MOB_ATTACK)
         || source.is(DamageTypes.MOB_PROJECTILE)
         || source.is(DamageTypes.MAGIC)
         || source.is(DamageTypes.INDIRECT_MAGIC)
         || source.is(DamageTypes.ARROW)
         || source.is(DamageTypes.TRIDENT)
         || source.is(DamageTypes.EXPLOSION);
   }

   private static void disableSpiritualization(ServerPlayer player, boolean exhausted) {
      player.getPersistentData().remove("mowf_spiritualized");
      player.getPersistentData().remove("mowf_chain_dodge_until");
      PlayerAuraSystem.clearContinuous(player);
      CooldownManager.set(player, "White Flame Spiritualization", exhausted ? 180 : 80);
      player.level().playSound((Player)null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.25F);
      if (exhausted) {
         player.displayClientMessage(Component.literal("White Flame Spiritualization ended: insufficient MP").withStyle(ChatFormatting.RED), true);
      }
   }

   private static LivingEntity nearestTarget(ServerLevel level, ServerPlayer owner, Vec3 center, double radius, boolean preferBranded) {
      List<LivingEntity> targets = level.getEntitiesOfClass(
         LivingEntity.class, new AABB(center, center).inflate(radius), candidate -> validTarget(owner, candidate) && owner.hasLineOfSight(candidate)
      );
      return targets.stream()
         .min(
            Comparator.<LivingEntity>comparingInt(target -> preferBranded && brandStacks(target, owner) > 0 ? 0 : 1)
               .thenComparingDouble(target -> target.distanceToSqr(center))
         )
         .orElse(null);
   }

   private static LivingEntity nearestHostileTarget(ServerLevel level, ServerPlayer owner, Vec3 center, double radius) {
      return level.getEntitiesOfClass(Monster.class, new AABB(center, center).inflate(radius), candidate -> validTarget(owner, candidate))
         .stream()
         .min(Comparator.comparingDouble(target -> target.distanceToSqr(center)))
         .orElse(null);
   }

   public static boolean validTarget(ServerPlayer owner, LivingEntity target) {
      if (target != null && target.isAlive() && target != owner && !target.isAlliedTo(owner) && !owner.isAlliedTo(target)) {
         if (target.getType().is(SHADOWS)) {
            return false;
         } else if (target instanceof TamableAnimal tame && owner.getUUID().equals(tame.getOwnerUUID())) {
            return false;
         } else if (target.getPersistentData().hasUUID("mowf_summon_owner") && owner.getUUID().equals(target.getPersistentData().getUUID("mowf_summon_owner"))) {
            return false;
         } else {
            return !(target instanceof Player other) ? true : !other.isCreative() && !other.isSpectator() && owner.canHarmPlayer(other);
         }
      } else {
         return false;
      }
   }

   public static boolean dealMagic(ServerPlayer owner, LivingEntity target, float amount) {
      if (!validTarget(owner, target)) {
         return false;
      }

      DamageSource source = new DamageSource(owner.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), owner);
      target.invulnerableTime = 0;
      boolean hurt = target.hurt(source, Math.max(0.5F, amount));
      if (hurt) {
         target.setLastHurtByPlayer(owner);
      }

      return hurt;
   }

   public static void brand(LivingEntity target, ServerPlayer owner, int duration, int stacks) {
      long now = target.level().getGameTime();
      if (!target.getPersistentData().hasUUID("mowf_brand_owner")
         || !owner.getUUID().equals(target.getPersistentData().getUUID("mowf_brand_owner"))
         || target.getPersistentData().getLong("mowf_brand_until") < now) {
         target.getPersistentData().putInt("mowf_brand_stacks", 0);
      }

      target.getPersistentData().putUUID("mowf_brand_owner", owner.getUUID());
      target.getPersistentData().putLong("mowf_brand_until", now + duration);
      target.getPersistentData().putInt("mowf_brand_stacks", Math.min(3, target.getPersistentData().getInt("mowf_brand_stacks") + Math.max(1, stacks)));
   }

   private static void clearBrand(LivingEntity target) {
      target.getPersistentData().remove("mowf_brand_until");
      target.getPersistentData().remove("mowf_brand_stacks");
      target.getPersistentData().remove("mowf_brand_owner");
   }

   private static int brandStacks(LivingEntity target, ServerPlayer owner) {
      return target.getPersistentData().hasUUID("mowf_brand_owner")
            && owner.getUUID().equals(target.getPersistentData().getUUID("mowf_brand_owner"))
            && target.getPersistentData().getLong("mowf_brand_until") >= target.level().getGameTime()
         ? target.getPersistentData().getInt("mowf_brand_stacks")
         : 0;
   }

   private static float magicDamage(ServerPlayer player, double base, double intelligenceDivisor) {
      return (float)(base + TemporaryStatBonusManager.effectiveIntelligence(player) / intelligenceDivisor);
   }

   private static boolean consumeMana(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      } else if (variables(player).MP < amount) {
         player.displayClientMessage(Component.literal("Not enough MP (" + amount + " required)").withStyle(ChatFormatting.RED), true);
         return false;
      } else {
         return drainMana(player, amount);
      }
   }

   private static boolean drainMana(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      }

      if (variables(player).MP < amount) {
         return false;
      }

      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.MP = Math.max(0.0, capability.MP - amount);
         capability.syncPlayerVariables(player);
      });
      CooldownManager.set(player, "mana_refresh", 35);
      return true;
   }

   private static boolean ready(ServerPlayer player, String skill) {
      if (!CooldownManager.isOnCooldown(player, skill)) {
         return true;
      }

      player.displayClientMessage(Component.literal(skill + " is on cooldown").withStyle(ChatFormatting.RED), true);
      return false;
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }
}
