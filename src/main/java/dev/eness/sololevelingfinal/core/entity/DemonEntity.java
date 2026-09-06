package dev.eness.sololevelingfinal.core.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.dkc.DkcWaveRuntime;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DemonEntity extends Monster implements GeoEntity {
   private static final int BODY_BROAD = 0;
   private static final int BODY_THIN = 1;
   private static final int TEXTURE_VARIANT_COUNT = 3;
   private static final float MIN_VISUAL_SCALE = 0.9F;
   private static final float MAX_NATURAL_VISUAL_SCALE = 1.12F;
   private static final float MAX_VISUAL_SCALE = 1.7F;
   private static final String BODY_VARIANT_TAG = "DemonBodyVariant";
   private static final String TEXTURE_VARIANT_TAG = "DemonTextureVariant";
   private static final String VISUAL_SCALE_TAG = "DemonVisualScale";
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(DemonEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(DemonEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> BODY_VARIANT = SynchedEntityData.defineId(DemonEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> TEXTURE_VARIANT = SynchedEntityData.defineId(DemonEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Float> VISUAL_SCALE = SynchedEntityData.defineId(DemonEntity.class, EntityDataSerializers.FLOAT);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private boolean lastloop;
   private long lastSwing;
   public String animationprocedure = "empty";

   public DemonEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.DEMON.get(), world);
   }

   public DemonEntity(EntityType<DemonEntity> type, Level world) {
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
      this.entityData.define(BODY_VARIANT, 0);
      this.entityData.define(TEXTURE_VARIANT, 0);
      this.entityData.define(VISUAL_SCALE, 1.0F);
   }

   public boolean isThinVariant() {
      return this.entityData.get(BODY_VARIANT) == 1;
   }

   public int getTextureVariant() {
      return this.entityData.get(TEXTURE_VARIANT);
   }

   public float getVisualScale() {
      return this.entityData.get(VISUAL_SCALE);
   }

   public void setVisualScale(float scale) {
      this.entityData.set(VISUAL_SCALE, Math.max(0.9F, Math.min(1.7F, scale)));
   }

   public void randomizeAppearance() {
      this.entityData.set(BODY_VARIANT, this.random.nextBoolean() ? 1 : 0);
      this.entityData.set(TEXTURE_VARIANT, this.random.nextInt(3));
      this.setVisualScale(0.9F + this.random.nextFloat() * 0.22000003F);
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
            return 6.25;
         }
      });
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
      this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
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
   protected boolean shouldDespawnInPeaceful() {
      CompoundTag data = this.getPersistentData();
      return !data.getBoolean("radiru_resident") && !data.getBoolean("radiru_training_dummy") ? super.shouldDespawnInPeaceful() : false;
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
      return source.is(DamageTypes.IN_FIRE) ? false : super.hurt(source, amount);
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag
   ) {
      SpawnGroupData spawnData = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);
      this.randomizeAppearance();
      return spawnData;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putInt("DemonBodyVariant", this.entityData.get(BODY_VARIANT));
      compound.putInt("DemonTextureVariant", this.getTextureVariant());
      compound.putFloat("DemonVisualScale", this.getVisualScale());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("DemonBodyVariant") && compound.contains("DemonTextureVariant") && compound.contains("DemonVisualScale")) {
         this.entityData.set(BODY_VARIANT, compound.getInt("DemonBodyVariant") == 1 ? 1 : 0);
         this.entityData.set(TEXTURE_VARIANT, Math.max(0, Math.min(2, compound.getInt("DemonTextureVariant"))));
         this.setVisualScale(compound.getFloat("DemonVisualScale"));
      } else {
         this.randomizeAppearance();
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      this.refreshDimensions();
      DkcWaveRuntime.tick(this);
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      float eliteCollisionScale = this.getVisualScale() <= 1.12F ? 1.0F : Math.min(1.4F, this.getVisualScale() / 1.12F);
      return super.getDimensions(p_33597_).scale(0.8F * eliteCollisionScale);
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
      builder = builder.add(Attributes.MAX_HEALTH, 60.0);
      builder = builder.add(Attributes.ARMOR, 0.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 12.0);
      builder = builder.add(Attributes.FOLLOW_RANGE, 32.0);
      builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
      return builder.add(Attributes.ATTACK_KNOCKBACK, 0.2);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (this.animationprocedure.equals("empty")) {
         if (!event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F) {
            String animation = this.isThinVariant() ? "animation.el_demon_thin.idle" : "animation.el_demon.idle";
            return event.setAndContinue(RawAnimation.begin().thenLoop(animation));
         } else {
            String animation = this.isThinVariant() ? "animation.el_demon_thin.pursuit" : "animation.el_demon.walk";
            return event.setAndContinue(RawAnimation.begin().thenLoop(animation));
         }
      } else {
         return PlayState.STOP;
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
      data.add(new AnimationController<>(this, "procedure", 4, this::procedurePredicate));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
