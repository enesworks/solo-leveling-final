package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;
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

public class KaiselinEntity extends Monster implements GeoEntity {
   public static final String DKC_SOUL_SPAWNED = "dkc_floor_20_kaisel_soul_spawned";
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(KaiselinEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(KaiselinEntity.class, EntityDataSerializers.STRING);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("fly");
   private final KaiselinEntity.KaiselinHeadPart headPart = new KaiselinEntity.KaiselinHeadPart(this, 2.15F, 2.15F);
   private final PartEntity<?>[] parts = new PartEntity[]{this.headPart};
   public String animationprocedure = "empty";
   private final ServerBossEvent bossInfo = new ServerBossEvent(Component.literal("Kaiselin"), BossBarColor.BLUE, BossBarOverlay.NOTCHED_10);
   private int swoopCooldown = 35;
   private int breathCooldown = 95;
   private int wingBurstCooldown = 65;
   private int attackState;
   private int stateTicks;
   private boolean stateHit;
   private Vec3 attackVector = Vec3.ZERO;
   private Vec3 hoverOffset = Vec3.ZERO;

   public KaiselinEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.KAISELIN.get(), world);
   }

   public KaiselinEntity(EntityType<? extends KaiselinEntity> type, Level world) {
      super(type, world);
      this.xpReward = 40;
      this.setNoAi(false);
      this.setNoGravity(true);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "kaiselin");
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public String getTexture() {
      return this.entityData.get(TEXTURE);
   }

   protected void setKaiselinVariant(String texture, String bossName) {
      this.setTexture(texture);
      this.bossInfo.setName(Component.literal(bossName));
   }

   public String getSyncedAnimation() {
      return this.entityData.get(ANIMATION);
   }

   public void setAnimation(String animation) {
      String nextAnimation = normalizeAnimation(animation);
      this.entityData.set(ANIMATION, nextAnimation);
      this.animationprocedure = nextAnimation;
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   public boolean isMultipartEntity() {
      return true;
   }

   @Override
   public PartEntity<?>[] getParts() {
      return this.parts;
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isValidCombatTarget));
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
      this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(4, new FloatGoal(this));
   }

   @Override
   public boolean canAttack(LivingEntity target) {
      return this.isValidCombatTarget(target) && super.canAttack(target);
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
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.hurt"));
   }

   @Override
   public SoundEvent getDeathSound() {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.growl"));
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (!source.is(DamageTypes.IN_FIRE) && !source.is(DamageTypes.FALL)) {
         return this.getType() != SololevelingModEntities.KAISELIN.get()
               || !(source.getEntity() instanceof BaranEntity) && !(source.getDirectEntity() instanceof BaranEntity)
            ? super.hurt(source, amount)
            : false;
      } else {
         return false;
      }
   }

   protected boolean hurtHeadPart(DamageSource source, float amount) {
      return this.hurt(source, amount);
   }

   @Override
   public void baseTick() {
      super.baseTick();
      this.setNoGravity(true);
      this.refreshDimensions();
      this.tickMultipartHitboxes();
      if (!this.level().isClientSide()) {
         this.tickFlightBossAi();
      }
   }

   private void tickMultipartHitboxes() {
      this.movePart(this.headPart, 0.0, 2.35, 3.55);
   }

   private void movePart(PartEntity<?> part, double sideOffset, double yOffset, double forwardOffset) {
      float yaw = this.yHeadRot * (float) (Math.PI / 180.0);
      double forwardX = -Mth.sin(yaw);
      double forwardZ = Mth.cos(yaw);
      double sideX = Mth.cos(yaw);
      double sideZ = Mth.sin(yaw);
      part.setPos(
         this.getX() + forwardX * forwardOffset + sideX * sideOffset, this.getY() + yOffset, this.getZ() + forwardZ * forwardOffset + sideZ * sideOffset
      );
   }

   private void tickFlightBossAi() {
      if (this.swoopCooldown > 0) {
         this.swoopCooldown--;
      }

      if (this.breathCooldown > 0) {
         this.breathCooldown--;
      }

      if (this.wingBurstCooldown > 0) {
         this.wingBurstCooldown--;
      }

      LivingEntity target = this.getOrFindTarget();
      if (target == null) {
         if (this.attackState != 0) {
            this.resetAttackState();
         }

         this.setDeltaMovement(this.getDeltaMovement().scale(0.86));
      } else {
         this.getLookControl().setLookAt(target, 35.0F, 25.0F);
         this.turnToward(target.position().add(0.0, 1.2, 0.0), 12.0F);
         if (this.attackState != 0) {
            this.tickAttackState(target);
         } else {
            double distance = CombatRangeHelper.surfaceDistance(this, target);
            if (this.wingBurstCooldown <= 0 && distance < 7.0) {
               this.beginWingBurst();
            } else if (this.breathCooldown <= 0 && distance >= 8.0 && distance <= 30.0 && this.hasLineOfSight(target)) {
               this.beginBreath(target);
            } else if (this.swoopCooldown <= 0 && distance >= 5.5) {
               this.beginSwoop(target);
            } else {
               this.hoverAround(target);
            }
         }
      }
   }

   protected LivingEntity getOrFindTarget() {
      LivingEntity target = this.getTarget();
      if (this.isValidCombatTarget(target)) {
         return target;
      } else {
         this.setTarget(null);
         Player nearest = this.level()
            .getNearestPlayer(this.getX(), this.getY(), this.getZ(), 56.0, entity -> entity instanceof LivingEntity living && this.isValidCombatTarget(living));
         if (nearest != null) {
            this.setTarget(nearest);
            return nearest;
         } else {
            return null;
         }
      }
   }

   protected boolean isValidCombatTarget(LivingEntity target) {
      if (target == null || !target.isAlive()) {
         return false;
      } else {
         return !(target instanceof Player player) ? false : !player.isCreative() && !player.isSpectator();
      }
   }

   private void hoverAround(LivingEntity target) {
      if (this.hoverOffset == Vec3.ZERO || this.tickCount % 55 == 0) {
         double angle = this.random.nextDouble() * Math.PI * 2.0;
         double radius = 9.0 + this.random.nextDouble() * 5.0;
         this.hoverOffset = new Vec3(Math.cos(angle) * radius, 5.0 + this.random.nextDouble() * 3.5, Math.sin(angle) * radius);
      }

      Vec3 desired = target.position().add(this.hoverOffset);
      this.flyToward(desired, 0.09, 0.82);
      if (this.tickCount % 12 == 0 && this.level() instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 3, 0.7, 0.2, 0.7, 0.01);
      }
   }

   private void beginSwoop(LivingEntity target) {
      Vec3 targetPos = target.position().add(0.0, 1.0, 0.0);
      Vec3 direction = targetPos.subtract(this.position());
      if (!(direction.lengthSqr() < 0.001)) {
         this.setAnimation("attack_1");
         this.attackState = 1;
         this.stateTicks = 0;
         this.stateHit = false;
         this.attackVector = direction.normalize();
         this.swoopCooldown = 70 + this.random.nextInt(35);
         this.level().playSound((Player)null, this.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 1.4F, 0.8F);
      }
   }

   private void beginWingBurst() {
      this.setAnimation("attack_2");
      this.attackState = 2;
      this.stateTicks = 0;
      this.stateHit = false;
      this.wingBurstCooldown = 95 + this.random.nextInt(35);
      this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 0.2, 0.2));
      this.level().playSound((Player)null, this.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 2.0F, 0.55F);
   }

   private void beginBreath(LivingEntity target) {
      this.setAnimation("attack_1");
      this.attackState = 3;
      this.stateTicks = 0;
      this.stateHit = false;
      this.attackVector = target.position().add(0.0, 1.0, 0.0).subtract(this.position()).normalize();
      this.breathCooldown = 135 + this.random.nextInt(45);
      this.setDeltaMovement(this.getDeltaMovement().scale(0.35));
      this.level().playSound((Player)null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.1F, 1.3F);
   }

   private void tickAttackState(LivingEntity target) {
      this.stateTicks++;
      if (this.attackState == 1) {
         this.tickSwoop(target);
      } else if (this.attackState == 2) {
         this.tickWingBurst();
      } else if (this.attackState == 3) {
         this.tickBreath(target);
      }
   }

   private void tickSwoop(LivingEntity target) {
      if (this.stateTicks <= 20) {
         this.setDeltaMovement(this.attackVector.scale(0.88));
         this.hasImpulse = true;
         this.turnToward(this.position().add(this.attackVector), 18.0F);
         if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.8, this.getZ(), 5, 0.8, 0.25, 0.8, 0.03);
         }

         for (LivingEntity nearby : this.level()
            .getEntitiesOfClass(
               LivingEntity.class,
               this.getBoundingBox().inflate(2.2),
               entity -> entity != this && this.isValidCombatTarget(entity) && entity.distanceTo(this) < 4.0
            )) {
            if (!this.stateHit) {
               this.stateHit = true;
               nearby.hurt(this.damageSources().mobAttack(this), 22.0F);
               this.pushEntity(nearby, this.attackVector, 1.2, 0.35);
               this.level().playSound((Player)null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.4F, 0.65F);
            }
         }
      }

      if (this.stateTicks > 24) {
         this.resetAttackState();
      }
   }

   private void tickWingBurst() {
      this.setDeltaMovement(this.getDeltaMovement().scale(0.55).add(0.0, 0.06, 0.0));
      if (this.level() instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0, this.getZ(), 3, 2.6, 0.35, 2.6, 0.02);
         if (this.stateTicks % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0, this.getZ(), 12, 3.0, 0.45, 3.0, 0.05);
         }
      }

      if (!this.stateHit && this.stateTicks >= 8) {
         this.stateHit = true;

         for (LivingEntity nearby : this.level()
            .getEntitiesOfClass(
               LivingEntity.class,
               this.getBoundingBox().inflate(8.0),
               entity -> entity != this && this.isValidCombatTarget(entity) && entity.distanceTo(this) <= 8.0
            )) {
            Vec3 direction = nearby.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (direction.lengthSqr() < 0.001) {
               direction = this.getLookAngle().multiply(1.0, 0.0, 1.0);
            }

            nearby.hurt(this.damageSources().mobAttack(this), 12.0F);
            this.pushEntity(nearby, direction.normalize(), 1.5, 0.55);
         }
      }

      if (this.stateTicks >= 18) {
         this.resetAttackState();
      }
   }

   private void tickBreath(LivingEntity target) {
      if (target != null && this.isValidCombatTarget(target) && this.stateTicks <= 8) {
         this.attackVector = target.position().add(0.0, 1.0, 0.0).subtract(this.position()).normalize();
      }

      this.setDeltaMovement(this.getDeltaMovement().scale(0.65));
      this.turnToward(this.position().add(this.attackVector), 14.0F);
      if (this.stateTicks >= 8 && this.stateTicks <= 32) {
         Vec3 origin = this.position().add(0.0, 1.4, 0.0).add(this.attackVector.scale(2.5));
         if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 10; i++) {
               Vec3 point = origin.add(this.attackVector.scale(i * 1.45));
               serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z, 4, 0.45, 0.2, 0.45, 0.01);
               serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z, 2, 0.35, 0.15, 0.35, 0.015);
            }
         }

         this.damageBreath(origin, 15.0);
      }

      if (this.stateTicks >= 38) {
         this.resetAttackState();
      }
   }

   private void damageBreath(Vec3 origin, double length) {
      for (LivingEntity nearby : this.level()
         .getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(length + 5.0), entity -> entity != this && this.isValidCombatTarget(entity))) {
         Vec3 toEntity = nearby.position().add(0.0, nearby.getBbHeight() * 0.5, 0.0).subtract(origin);
         double forward = toEntity.dot(this.attackVector);
         if (!(forward < 0.0) && !(forward > length)) {
            double sideDistance = toEntity.subtract(this.attackVector.scale(forward)).length();
            if (sideDistance <= 2.4) {
               nearby.hurt(this.damageSources().mobAttack(this), 5.0F);
               nearby.setSecondsOnFire(3);
               this.pushEntity(nearby, this.attackVector, 0.35, 0.08);
            }
         }
      }
   }

   private void resetAttackState() {
      this.attackState = 0;
      this.stateTicks = 0;
      this.stateHit = false;
      this.attackVector = Vec3.ZERO;
      this.setAnimation("empty");
   }

   protected void flyToward(Vec3 desired, double acceleration, double drag) {
      Vec3 delta = desired.subtract(this.position());
      if (delta.lengthSqr() > 0.05) {
         this.setDeltaMovement(this.getDeltaMovement().scale(drag).add(delta.normalize().scale(acceleration)));
      } else {
         this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
      }

      this.hasImpulse = true;
      Vec3 lookPoint = this.getDeltaMovement().lengthSqr() > 0.002 ? this.position().add(this.getDeltaMovement()) : desired;
      this.turnToward(lookPoint, 8.0F);
   }

   protected void turnToward(Vec3 point, float maxTurn) {
      Vec3 delta = point.subtract(this.position());
      if (!(delta.lengthSqr() < 0.001)) {
         float targetYaw = (float)(Mth.atan2(delta.z, delta.x) * 180.0F / (float)Math.PI) - 90.0F;
         float yaw = Mth.approachDegrees(this.getYRot(), targetYaw, maxTurn);
         this.setYRot(yaw);
         this.yBodyRot = yaw;
         this.yHeadRot = yaw;
         this.setYHeadRot(yaw);
      }
   }

   private void pushEntity(LivingEntity entity, Vec3 direction, double horizontal, double vertical) {
      entity.setDeltaMovement(entity.getDeltaMovement().add(direction.x * horizontal, vertical, direction.z * horizontal));
      entity.hurtMarked = true;
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
      if (!this.level().isClientSide()
         && this.getType() == SololevelingModEntities.KAISELIN.get()
         && !this.getPersistentData().getBoolean("kaiselin_soul_spawned")) {
         this.getPersistentData().putBoolean("kaiselin_soul_spawned", true);
         boolean dkcObjective = (int)this.getPersistentData().getDouble("dkc_floor_number") == 20
            && !this.getPersistentData().getString("dkc_spawned_by").isBlank();
         if (!dkcObjective) {
            this.spawnKaiselSoulOnGround();
         }
      }

      super.die(source);
      this.bossInfo.removeAllPlayers();
   }

   private void spawnKaiselSoulOnGround() {
      if (this.level() instanceof ServerLevel serverLevel) {
         BlockPos var6 = this.findSoulGroundPos(serverLevel);
         Entity soul = SololevelingModEntities.SHADOW_SOUL.get().spawn(serverLevel, var6, MobSpawnType.MOB_SUMMONED);
         if (soul != null) {
            soul.moveTo(this.getX(), var6.getY() + 0.05, this.getZ(), this.getYRot(), 0.0F);
            soul.getPersistentData().putString("soultype", "kaisel");
            double targetLevel = DungeonLevelHelper.levelOf(this);
            if (targetLevel > 0.0) {
               soul.getPersistentData().putDouble("slr_arise_target_level", targetLevel);
            }
         }
      }
   }

   private BlockPos findSoulGroundPos(ServerLevel serverLevel) {
      BlockPos pos = BlockPos.containing(this.getX(), this.getY(), this.getZ());

      while (pos.getY() > serverLevel.getMinBuildHeight() + 1 && serverLevel.isEmptyBlock(pos.below())) {
         pos = pos.below();
      }

      while (pos.getY() < serverLevel.getMaxBuildHeight() - 2 && !serverLevel.isEmptyBlock(pos)) {
         pos = pos.above();
      }

      return pos;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
      compound.putDouble("dkc_floor_number", this.getPersistentData().getDouble("dkc_floor_number"));
      compound.putString("dkc_spawned_by", this.getPersistentData().getString("dkc_spawned_by"));
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Texture")) {
         this.setTexture(compound.getString("Texture"));
      }

      if (compound.contains("dkc_floor_number")) {
         this.getPersistentData().putDouble("dkc_floor_number", compound.getDouble("dkc_floor_number"));
      }

      if (compound.contains("dkc_spawned_by")) {
         this.getPersistentData().putString("dkc_spawned_by", compound.getString("dkc_spawned_by"));
      }
   }

   @Override
   public void customServerAiStep() {
      super.customServerAiStep();
      this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime == 30) {
         this.remove(RemovalReason.KILLED);
         this.dropExperience();
      }
   }

   @Override
   public EntityDimensions getDimensions(Pose pose) {
      return super.getDimensions(pose);
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.28)
         .add(Attributes.MAX_HEALTH, 280.0)
         .add(Attributes.ARMOR, 12.0)
         .add(Attributes.ARMOR_TOUGHNESS, 1.0)
         .add(Attributes.ATTACK_DAMAGE, 18.0)
         .add(Attributes.FOLLOW_RANGE, 56.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
         .add(Attributes.FLYING_SPEED, 0.35);
   }

   private PlayState movementPredicate(AnimationState<KaiselinEntity> event) {
      event.getController().setAnimationSpeed(this.getFlightAnimationSpeed());
      return event.setAndContinue(FLY_ANIMATION);
   }

   private PlayState procedurePredicate(AnimationState<KaiselinEntity> event) {
      String syncedAnimation = normalizeAnimation(this.getSyncedAnimation());
      if (!syncedAnimation.equals(this.animationprocedure)) {
         this.animationprocedure = syncedAnimation;
         event.getController().forceAnimationReset();
      }

      if (!this.animationprocedure.equals("empty") && event.getController().getAnimationState() == State.STOPPED) {
         event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
         if (event.getController().getAnimationState() == State.STOPPED) {
            this.setAnimation("empty");
            event.getController().forceAnimationReset();
         }
      } else if (this.animationprocedure.equals("empty")) {
         return PlayState.STOP;
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(ControllerRegistrar data) {
      data.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
      data.add(new AnimationController<>(this, "procedure", 4, this::procedurePredicate));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   private double getFlightAnimationSpeed() {
      if (this.isVehicle()) {
         return 1.15;
      } else if (this.attackState != 0) {
         return 1.12;
      } else {
         return this.getDeltaMovement().lengthSqr() > 0.015 ? 1.08 : 1.0;
      }
   }

   private static String normalizeAnimation(String animation) {
      return animation != null && !animation.isEmpty() && !"undefined".equals(animation) ? animation : "empty";
   }

   private static class KaiselinHeadPart extends PartEntity<KaiselinEntity> {
      private final EntityDimensions dimensions;

      private KaiselinHeadPart(KaiselinEntity parent, float width, float height) {
         super(parent);
         this.dimensions = EntityDimensions.scalable(width, height);
         this.refreshDimensions();
      }

      @Override
      protected void defineSynchedData() {
      }

      @Override
      protected void readAdditionalSaveData(CompoundTag compound) {
      }

      @Override
      protected void addAdditionalSaveData(CompoundTag compound) {
      }

      @Override
      public boolean isPickable() {
         return true;
      }

      @Override
      public boolean hurt(DamageSource source, float amount) {
         return this.getParent().hurtHeadPart(source, amount);
      }

      @Override
      public boolean is(Entity entity) {
         return this == entity || this.getParent() == entity;
      }

      @Override
      public EntityDimensions getDimensions(Pose pose) {
         return this.dimensions;
      }

      @Override
      public boolean shouldBeSaved() {
         return false;
      }
   }
}
