package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.procedures.SkeletonSummonerOnEntityTickUpdateProcedure;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SkeletonSummonerEntity extends Monster implements GeoEntity {
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> DATA_State = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_AttackDuration = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_SummoningCooldown = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_ProjectileCooldown = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_RushCooldown = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_RepulsionCooldown = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_GlobalAttackCooldown = SynchedEntityData.defineId(
      SkeletonSummonerEntity.class, EntityDataSerializers.INT
   );
   public static final EntityDataAccessor<Integer> DATA_MeleeCooldown = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_MeleeType = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_DamageProjectile = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_DamageMelee = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_DamageRepulsion = SynchedEntityData.defineId(SkeletonSummonerEntity.class, EntityDataSerializers.INT);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private boolean lastloop;
   private long lastSwing;
   public String animationprocedure = "empty";

   public SkeletonSummonerEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.SKELETON_SUMMONER.get(), world);
   }

   public SkeletonSummonerEntity(EntityType<SkeletonSummonerEntity> type, Level world) {
      super(type, world);
      this.xpReward = 0;
      this.setNoAi(false);
      this.setPersistenceRequired();
      this.moveControl = new FlyingMoveControl(this, 10, true);
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHOOT, false);
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "skeleton_summoner");
      this.entityData.define(DATA_State, "");
      this.entityData.define(DATA_AttackDuration, 0);
      this.entityData.define(DATA_SummoningCooldown, 0);
      this.entityData.define(DATA_ProjectileCooldown, 0);
      this.entityData.define(DATA_RushCooldown, 0);
      this.entityData.define(DATA_RepulsionCooldown, 0);
      this.entityData.define(DATA_GlobalAttackCooldown, 0);
      this.entityData.define(DATA_MeleeCooldown, 0);
      this.entityData.define(DATA_MeleeType, 0);
      this.entityData.define(DATA_DamageProjectile, 15);
      this.entityData.define(DATA_DamageMelee, 10);
      this.entityData.define(DATA_DamageRepulsion, 15);
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
   protected PathNavigation createNavigation(Level world) {
      return new FlyingPathNavigation(this, world);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, HunterEntity.class, false, false));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, ChoijongEntity.class, false, false));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, BaekYoonhoEntity.class, false, false));
      this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.2, false) {
         @Override
         protected double getAttackReachSqr(LivingEntity entity) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
         }
      });
      this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
      this.targetSelector.addGoal(7, new HurtByTargetGoal(this));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(9, new FloatGoal(this));
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
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither_skeleton.hurt"));
   }

   @Override
   public SoundEvent getDeathSound() {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither_skeleton.death"));
   }

   @Override
   public boolean causeFallDamage(float l, float d, DamageSource source) {
      return false;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
      compound.putString("DataState", this.entityData.get(DATA_State));
      compound.putInt("DataAttackDuration", this.entityData.get(DATA_AttackDuration));
      compound.putInt("DataSummoningCooldown", this.entityData.get(DATA_SummoningCooldown));
      compound.putInt("DataProjectileCooldown", this.entityData.get(DATA_ProjectileCooldown));
      compound.putInt("DataRushCooldown", this.entityData.get(DATA_RushCooldown));
      compound.putInt("DataRepulsionCooldown", this.entityData.get(DATA_RepulsionCooldown));
      compound.putInt("DataGlobalAttackCooldown", this.entityData.get(DATA_GlobalAttackCooldown));
      compound.putInt("DataMeleeCooldown", this.entityData.get(DATA_MeleeCooldown));
      compound.putInt("DataMeleeType", this.entityData.get(DATA_MeleeType));
      compound.putInt("DataDamageProjectile", this.entityData.get(DATA_DamageProjectile));
      compound.putInt("DataDamageMelee", this.entityData.get(DATA_DamageMelee));
      compound.putInt("DataDamageRepulsion", this.entityData.get(DATA_DamageRepulsion));
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Texture")) {
         this.setTexture(compound.getString("Texture"));
      }

      if (compound.contains("DataState")) {
         this.entityData.set(DATA_State, compound.getString("DataState"));
      }

      if (compound.contains("DataAttackDuration")) {
         this.entityData.set(DATA_AttackDuration, compound.getInt("DataAttackDuration"));
      }

      if (compound.contains("DataSummoningCooldown")) {
         this.entityData.set(DATA_SummoningCooldown, compound.getInt("DataSummoningCooldown"));
      }

      if (compound.contains("DataProjectileCooldown")) {
         this.entityData.set(DATA_ProjectileCooldown, compound.getInt("DataProjectileCooldown"));
      }

      if (compound.contains("DataRushCooldown")) {
         this.entityData.set(DATA_RushCooldown, compound.getInt("DataRushCooldown"));
      }

      if (compound.contains("DataRepulsionCooldown")) {
         this.entityData.set(DATA_RepulsionCooldown, compound.getInt("DataRepulsionCooldown"));
      }

      if (compound.contains("DataGlobalAttackCooldown")) {
         this.entityData.set(DATA_GlobalAttackCooldown, compound.getInt("DataGlobalAttackCooldown"));
      }

      if (compound.contains("DataMeleeCooldown")) {
         this.entityData.set(DATA_MeleeCooldown, compound.getInt("DataMeleeCooldown"));
      }

      if (compound.contains("DataMeleeType")) {
         this.entityData.set(DATA_MeleeType, compound.getInt("DataMeleeType"));
      }

      if (compound.contains("DataDamageProjectile")) {
         this.entityData.set(DATA_DamageProjectile, compound.getInt("DataDamageProjectile"));
      }

      if (compound.contains("DataDamageMelee")) {
         this.entityData.set(DATA_DamageMelee, compound.getInt("DataDamageMelee"));
      }

      if (compound.contains("DataDamageRepulsion")) {
         this.entityData.set(DATA_DamageRepulsion, compound.getInt("DataDamageRepulsion"));
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      if (this.isDeathLocked()) {
         this.stopDeathLockedActions();
      } else {
         SkeletonSummonerOnEntityTickUpdateProcedure.execute(this.level(), this);
      }

      this.refreshDimensions();
   }

   @Override
   public boolean canAttack(LivingEntity target) {
      return !this.isDeathLocked() && super.canAttack(target);
   }

   @Override
   public boolean doHurtTarget(Entity target) {
      return this.isDeathLocked() ? false : super.doHurtTarget(target);
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      return super.getDimensions(p_33597_).scale(1.0F);
   }

   @Override
   protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
   }

   @Override
   public void setNoGravity(boolean ignored) {
      super.setNoGravity(true);
   }

   @Override
   public void aiStep() {
      if (this.isDeathLocked()) {
         this.stopDeathLockedActions();
      }

      super.aiStep();
      this.setNoGravity(true);
      if (this.isDeathLocked()) {
         this.stopDeathLockedActions();
      }
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
      builder = builder.add(Attributes.MAX_HEALTH, 100.0);
      builder = builder.add(Attributes.ARMOR, 36.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 3.0);
      builder = builder.add(Attributes.FOLLOW_RANGE, 32.0);
      return builder.add(Attributes.FLYING_SPEED, 0.3);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (this.isDeathLocked()) {
         return event.setAndContinue(RawAnimation.begin().thenPlay("death"));
      } else if (this.animationprocedure.equals("empty")) {
         return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F
            ? event.setAndContinue(RawAnimation.begin().thenLoop("idle"))
            : event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
      } else {
         return PlayState.STOP;
      }
   }

   private PlayState procedurePredicate(AnimationState event) {
      if (this.isDeathLocked()) {
         this.animationprocedure = "empty";
         return PlayState.STOP;
      }

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
      this.stopDeathLockedActions();
      this.deathTime++;
      if (this.deathTime == 135) {
         this.remove(RemovalReason.KILLED);
         this.dropExperience();
      }
   }

   private boolean isDeathLocked() {
      return this.isDeadOrDying() || this.deathTime > 0 || this.getHealth() <= 0.0F;
   }

   private void stopDeathLockedActions() {
      this.getNavigation().stop();
      this.setTarget(null);
      this.setAggressive(false);
      this.swinging = false;
      this.animationprocedure = "empty";
      this.entityData.set(DATA_State, "DEAD");
      this.entityData.set(DATA_AttackDuration, 0);
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
      data.add(new AnimationController<>(this, "procedure", 4, this::procedurePredicate));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
