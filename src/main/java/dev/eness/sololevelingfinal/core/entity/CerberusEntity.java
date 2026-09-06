package dev.eness.sololevelingfinal.core.entity;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CerberusEntity extends Monster implements GeoEntity {
   private static final String DEFAULTS_FIXED_TAG = "slr_cerberus_defaults_fixed";
   private static final double BASE_MAX_HEALTH = 260.0;
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(CerberusEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(CerberusEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(CerberusEntity.class, EntityDataSerializers.STRING);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private boolean lastloop;
   private long lastSwing;
   public String animationprocedure = "empty";
   private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), BossBarColor.RED, BossBarOverlay.NOTCHED_10);
   private int fireBreathCooldown = 70;
   private int fireBreathTicks;
   private int dashCooldown = 45;
   private int dashTicks;
   private int slamCooldown = 95;
   private int slamTicks;
   private int dodgeCooldown;
   private int blockTicks;
   private boolean slamLanded;
   private Vec3 dashDirection = Vec3.ZERO;

   public CerberusEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.CERBERUS.get(), world);
   }

   public CerberusEntity(EntityType<CerberusEntity> type, Level world) {
      super(type, world);
      this.xpReward = 0;
      this.setNoAi(false);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHOOT, false);
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "cerberus");
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public String getTexture() {
      return this.entityData.get(TEXTURE);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
         @Override
         protected double getAttackReachSqr(LivingEntity entity) {
            return 9.0;
         }
      });
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(5, new FloatGoal(this));
   }

   @Override
   public MobType getMobType() {
      return MobType.UNDEFINED;
   }

   @Override
   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   @Override
   public SoundEvent getHurtSound(DamageSource ds) {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
   }

   @Override
   public SoundEvent getDeathSound() {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (source.is(DamageTypes.IN_FIRE)) {
         return false;
      }

      if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
         if (this.blockTicks > 0) {
            this.playDefenseEffects();
            return super.hurt(source, amount * 0.35F);
         }

         if (this.dodgeCooldown <= 0 && this.distanceTo(attacker) < 8.0F && this.random.nextFloat() < 0.22F) {
            this.dodgeCooldown = 42;
            this.sideStep(attacker, 1.15);
            this.playDefenseEffects();
            return super.hurt(source, amount * 0.45F);
         }

         if (amount >= 7.0F && this.random.nextFloat() < 0.28F) {
            this.blockTicks = 12;
            this.playDefenseEffects();
            return super.hurt(source, amount * 0.5F);
         }
      }

      return super.hurt(source, amount);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Texture")) {
         this.setTexture(compound.getString("Texture"));
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      this.repairLegacyStructureData();
      this.refreshDimensions();
      this.tickCerberusBossAi();
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      return super.getDimensions(p_33597_).scale(3.0F);
   }

   @Override
   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossInfo.addPlayer(player);
   }

   @Override
   public void stopSeenByPlayer(ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossInfo.removePlayer(player);
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      this.bossInfo.removeAllPlayers();
   }

   @Override
   public void customServerAiStep() {
      super.customServerAiStep();
      this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.34);
      builder = builder.add(Attributes.MAX_HEALTH, 260.0);
      builder = builder.add(Attributes.ARMOR, 28.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 26.0);
      builder = builder.add(Attributes.FOLLOW_RANGE, 48.0);
      return builder.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
   }

   private void repairLegacyStructureData() {
      if (!this.level().isClientSide() && !this.getPersistentData().getBoolean("slr_cerberus_defaults_fixed")) {
         this.getPersistentData().putBoolean("slr_cerberus_defaults_fixed", true);
         AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
         if (maxHealth != null && maxHealth.getBaseValue() < 260.0) {
            maxHealth.setBaseValue(260.0);
         }

         if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
         }
      }
   }

   private void tickCerberusBossAi() {
      if (!this.level().isClientSide && !this.isNoAi()) {
         if (this.fireBreathCooldown > 0) {
            this.fireBreathCooldown--;
         }

         if (this.dashCooldown > 0) {
            this.dashCooldown--;
         }

         if (this.slamCooldown > 0) {
            this.slamCooldown--;
         }

         if (this.dodgeCooldown > 0) {
            this.dodgeCooldown--;
         }

         if (this.blockTicks > 0) {
            this.blockTicks--;
         }

         LivingEntity target = this.getOrFindTarget();
         if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 35.0F);
            if (this.fireBreathTicks > 0) {
               this.tickFireBreath(target);
            } else if (this.dashTicks > 0) {
               this.tickDash();
            } else if (this.slamTicks > 0) {
               this.tickJumpSlam();
            } else {
               double distance = CombatRangeHelper.surfaceDistance(this, target);
               if (this.fireBreathCooldown <= 0 && distance > 4.0 && distance < 18.0 && this.hasLineOfSight(target)) {
                  this.beginFireBreath();
               } else if (this.slamCooldown <= 0 && distance > 5.0 && distance < 16.0) {
                  this.beginJumpSlam(target);
               } else if (this.dashCooldown <= 0 && distance > 7.0) {
                  this.beginDash(target);
               }
            }
         }
      }
   }

   private LivingEntity getOrFindTarget() {
      LivingEntity target = this.getTarget();
      if (target != null && target.isAlive()) {
         return target;
      } else {
         Player nearest = this.level().getNearestPlayer(this, 48.0);
         if (nearest != null && nearest.isAlive()) {
            this.setTarget(nearest);
            return nearest;
         } else {
            return null;
         }
      }
   }

   private void beginFireBreath() {
      this.fireBreathTicks = 42;
      this.fireBreathCooldown = 115 + this.random.nextInt(45);
      this.animationprocedure = "roar";
      this.getNavigation().stop();
      this.level().playSound((Player)null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.8F, 0.55F);
   }

   private void tickFireBreath(LivingEntity target) {
      this.fireBreathTicks--;
      this.getNavigation().stop();
      this.getLookControl().setLookAt(target, 45.0F, 45.0F);
      Vec3 direction = this.horizontalDirectionTo(target);
      if (direction.lengthSqr() < 0.001) {
         direction = this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
      }

      Vec3 origin = this.position().add(direction.scale(2.2)).add(0.0, this.getBbHeight() * 0.62, 0.0);
      if (this.level() instanceof ServerLevel serverLevel) {
         for (int i = 1; i <= 12; i++) {
            Vec3 center = origin.add(direction.scale(i * 0.85));
            double spread = 0.12 + i * 0.035;
            serverLevel.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 3, spread, spread * 0.5, spread, 0.035);
            if (i % 3 == 0) {
               serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, 1, spread, spread * 0.4, spread, 0.01);
            }
         }
      }

      if (this.fireBreathTicks % 4 == 0) {
         this.damageFireCone(origin, direction);
      }
   }

   private void beginDash(LivingEntity target) {
      Vec3 direction = this.horizontalDirectionTo(target);
      if (!(direction.lengthSqr() < 0.001)) {
         this.dashDirection = direction;
         this.dashTicks = 16;
         this.dashCooldown = 70 + this.random.nextInt(35);
         this.animationprocedure = "bite";
         this.level().playSound((Player)null, this.blockPosition(), SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.4F, 0.65F);
      }
   }

   private void tickDash() {
      this.dashTicks--;
      if (this.dashTicks > 4) {
         this.setDeltaMovement(this.dashDirection.x * 1.15, this.getDeltaMovement().y, this.dashDirection.z * 1.15);
         this.hasImpulse = true;
         if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.4, this.getZ(), 5, 0.6, 0.25, 0.6, 0.02);
         }

         this.damageNearbyOnce("cerberus_dash_hit_", 2.4, 18.0F, 0.8, 0.2);
      }

      if (this.dashTicks <= 0) {
         this.clearHitFlags("cerberus_dash_hit_");
      }
   }

   private void beginJumpSlam(LivingEntity target) {
      Vec3 direction = this.horizontalDirectionTo(target);
      this.slamTicks = 28;
      this.slamCooldown = 120 + this.random.nextInt(55);
      this.slamLanded = false;
      this.animationprocedure = "roar";
      this.setDeltaMovement(direction.x * 0.65, 0.82, direction.z * 0.65);
      this.hasImpulse = true;
      this.level().playSound((Player)null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.6F, 0.7F);
   }

   private void tickJumpSlam() {
      int age = 28 - this.slamTicks;
      this.slamTicks--;
      if (age == 10) {
         this.setDeltaMovement(this.getDeltaMovement().x * 0.6, -0.95, this.getDeltaMovement().z * 0.6);
      }

      if (!this.slamLanded && (this.onGround() || age >= 17)) {
         this.slamLanded = true;
         this.dealSlam(6.5, 28.0F, 1.25, 0.65, ParticleTypes.FLAME);
         this.level().playSound((Player)null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.4F, 0.75F);
      }
   }

   private void damageFireCone(Vec3 origin, Vec3 direction) {
      for (LivingEntity nearby : this.level()
         .getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0), entity -> entity != this && entity.isAlive())) {
         Vec3 toEntity = nearby.position().add(0.0, nearby.getBbHeight() * 0.5, 0.0).subtract(origin);
         double distance = toEntity.length();
         if (!(distance > 15.0) && !(distance < 0.2)) {
            Vec3 normalized = toEntity.multiply(1.0, 0.0, 1.0).normalize();
            if (!(normalized.dot(direction) < 0.62)) {
               nearby.hurt(this.damageSources().mobAttack(this), 6.0F);
               nearby.setSecondsOnFire(4);
               this.pushEntity(nearby, direction, 0.22, 0.04);
            }
         }
      }
   }

   private void damageNearbyOnce(String keyPrefix, double radius, float damage, double horizontalKnockback, double verticalKnockback) {
      for (LivingEntity nearby : this.level()
         .getEntitiesOfClass(
            LivingEntity.class,
            this.getBoundingBox().inflate(radius),
            entity -> entity != this && entity.isAlive() && entity.distanceTo(this) <= radius + entity.getBbWidth()
         )) {
         String hitKey = keyPrefix + nearby.getUUID();
         if (!this.getPersistentData().getBoolean(hitKey)) {
            nearby.hurt(this.damageSources().mobAttack(this), damage);
            Vec3 direction = nearby.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (direction.lengthSqr() < 0.001) {
               direction = this.dashDirection;
            }

            this.pushEntity(nearby, direction.normalize(), horizontalKnockback, verticalKnockback);
            this.getPersistentData().putBoolean(hitKey, true);
         }
      }
   }

   private void dealSlam(double radius, float damage, double horizontalKnockback, double verticalKnockback, SimpleParticleType particle) {
      if (this.level() instanceof ServerLevel serverLevel) {
         for (int ring = 0; ring < 3; ring++) {
            double ringRadius = radius * (0.35 + ring * 0.25);

            for (int i = 0; i < 20; i++) {
               double angle = i / 20.0 * Math.PI * 2.0;
               serverLevel.sendParticles(
                  particle,
                  this.getX() + Math.cos(angle) * ringRadius,
                  this.getY() + 0.15,
                  this.getZ() + Math.sin(angle) * ringRadius,
                  2,
                  0.12,
                  0.08,
                  0.12,
                  0.01
               );
            }
         }

         serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.4, this.getZ(), 2, 0.8, 0.25, 0.8, 0.0);
      }

      for (LivingEntity nearby : this.level()
         .getEntitiesOfClass(
            LivingEntity.class, this.getBoundingBox().inflate(radius), entity -> entity != this && entity.isAlive() && entity.distanceTo(this) <= radius
         )) {
         nearby.hurt(this.damageSources().mobAttack(this), damage);
         Vec3 direction = nearby.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
         if (direction.lengthSqr() > 0.001) {
            this.pushEntity(nearby, direction.normalize(), horizontalKnockback, verticalKnockback);
         }
      }
   }

   private Vec3 horizontalDirectionTo(LivingEntity target) {
      Vec3 direction = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
      return direction.lengthSqr() < 0.001 ? Vec3.ZERO : direction.normalize();
   }

   private void pushEntity(LivingEntity entity, Vec3 direction, double horizontal, double vertical) {
      entity.setDeltaMovement(direction.x * horizontal, vertical, direction.z * horizontal);
      entity.hurtMarked = true;
   }

   private void sideStep(LivingEntity attacker, double speed) {
      Vec3 away = this.position().subtract(attacker.position()).multiply(1.0, 0.0, 1.0);
      if (away.lengthSqr() < 0.001) {
         away = this.getLookAngle().multiply(1.0, 0.0, 1.0);
      }

      Vec3 side = new Vec3(-away.z, 0.0, away.x).normalize();
      if (this.random.nextBoolean()) {
         side = side.scale(-1.0);
      }

      this.setDeltaMovement(side.x * speed, 0.18, side.z * speed);
      this.hasImpulse = true;
   }

   private void playDefenseEffects() {
      this.level().playSound((Player)null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 0.65F);
      if (this.level() instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + this.getBbHeight() * 0.55, this.getZ(), 10, 0.7, 0.45, 0.7, 0.04);
      }
   }

   private void clearHitFlags(String prefix) {
      Set<String> toRemove = new HashSet<>();

      for (String key : this.getPersistentData().getAllKeys()) {
         if (key.startsWith(prefix)) {
            toRemove.add(key);
         }
      }

      toRemove.forEach(this.getPersistentData()::remove);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (this.animationprocedure.equals("empty")) {
         return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F
            ? event.setAndContinue(RawAnimation.begin().thenLoop("idle"))
            : event.setAndContinue(RawAnimation.begin().thenLoop("running"));
      } else {
         return PlayState.STOP;
      }
   }

   private PlayState attackingPredicate(AnimationState event) {
      double d1 = this.getX() - this.xOld;
      double d0 = this.getZ() - this.zOld;
      float velocity = (float)Math.sqrt(d1 * d1 + d0 * d0);
      if (this.getAttackAnim(event.getPartialTick()) > 0.0F && !this.swinging) {
         this.swinging = true;
         this.lastSwing = this.level().getGameTime();
      }

      if (this.swinging && this.lastSwing + 7L <= this.level().getGameTime()) {
         this.swinging = false;
      }

      if (this.swinging && event.getController().getAnimationState() == State.STOPPED) {
         event.getController().forceAnimationReset();
         return event.setAndContinue(RawAnimation.begin().thenPlay("bite"));
      } else {
         return PlayState.CONTINUE;
      }
   }

   private PlayState procedurePredicate(AnimationState event) {
      if (!this.animationprocedure.equals("empty") && event.getController().getAnimationState() == State.STOPPED) {
         event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
         if (event.getController().getAnimationState() == State.STOPPED) {
            this.animationprocedure = "empty";
            event.getController().forceAnimationReset();
         }
      } else if (this.animationprocedure.equals("empty")) {
         return PlayState.STOP;
      }

      return PlayState.CONTINUE;
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime == 20) {
         this.remove(RemovalReason.KILLED);
         this.dropExperience();
      }
   }

   public String getSyncedAnimation() {
      return this.entityData.get(ANIMATION);
   }

   public void setAnimation(String animation) {
      this.entityData.set(ANIMATION, animation);
   }

   @Override
   public void registerControllers(ControllerRegistrar data) {
      data.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
      data.add(new AnimationController<>(this, "attacking", 4, this::attackingPredicate));
      data.add(new AnimationController<>(this, "procedure", 4, this::procedurePredicate));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
