package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
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
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.ai.ShadowThreatTargetGoal;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.procedures.IceElfEntityIsHurtProcedure;
import dev.eness.sololevelingfinal.core.procedures.IceElfOnEntityTickUpdateProcedure;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class IceElfEntity extends Monster implements GeoEntity {
   private static final String COMBAT_BALANCE_VERSION_TAG = "SLRIceElfCombatBalanceVersion";
   private static final int COMBAT_BALANCE_VERSION = 1;
   private static final double LEGACY_ATTACK_DAMAGE_REDUCTION = 1.0;
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(IceElfEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(IceElfEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(IceElfEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_AI = SynchedEntityData.defineId(IceElfEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_MF = SynchedEntityData.defineId(IceElfEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Boolean> DATA_canshoot = SynchedEntityData.defineId(IceElfEntity.class, EntityDataSerializers.BOOLEAN);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private boolean lastloop;
   private long lastSwing;
   private ShadowThreatTargetGoal shadowThreatGoal;
   public String animationprocedure = "empty";

   public IceElfEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.ICE_ELF.get(), world);
   }

   public IceElfEntity(EntityType<IceElfEntity> type, Level world) {
      super(type, world);
      this.xpReward = 20;
      this.setNoAi(false);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHOOT, false);
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "iceelf");
      this.entityData.define(DATA_AI, 0);
      this.entityData.define(DATA_MF, 0);
      this.entityData.define(DATA_canshoot, false);
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public String getTexture() {
      return this.entityData.get(TEXTURE);
   }

   @Override
   protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
      return 1.9F;
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.shadowThreatGoal = new ShadowThreatTargetGoal(this);
      this.targetSelector.addGoal(0, this.shadowThreatGoal);
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
         @Override
         protected double getAttackReachSqr(LivingEntity entity) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
         }
      });
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
      this.targetSelector.addGoal(4, new HurtByTargetGoal(this).setAlertOthers());
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(7, new FloatGoal(this));
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
      IceElfEntityIsHurtProcedure.execute(this);
      float healthBefore = this.getHealth();
      boolean hurt = super.hurt(source, amount);
      if (hurt && this.shadowThreatGoal != null) {
         this.shadowThreatGoal.recordSuccessfulHit(source, Math.max(0.0F, healthBefore - this.getHealth()));
      }

      return hurt;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
      compound.putInt("DataAI", this.entityData.get(DATA_AI));
      compound.putInt("DataMF", this.entityData.get(DATA_MF));
      compound.putBoolean("Datacanshoot", this.entityData.get(DATA_canshoot));
      compound.putInt("SLRIceElfCombatBalanceVersion", 1);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Texture")) {
         this.setTexture(compound.getString("Texture"));
      }

      if (compound.contains("DataAI")) {
         this.entityData.set(DATA_AI, compound.getInt("DataAI"));
      }

      if (compound.contains("DataMF")) {
         this.entityData.set(DATA_MF, compound.getInt("DataMF"));
      }

      if (compound.contains("Datacanshoot")) {
         this.entityData.set(DATA_canshoot, compound.getBoolean("Datacanshoot"));
      }

      if (compound.getInt("SLRIceElfCombatBalanceVersion") < 1 && this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.max(1.0, this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() - 1.0));
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      IceElfOnEntityTickUpdateProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
      this.refreshDimensions();
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      return super.getDimensions(p_33597_).scale(1.0F);
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
      builder = builder.add(Attributes.MAX_HEALTH, 72.0);
      builder = builder.add(Attributes.ARMOR, 15.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 2.0);
      builder = builder.add(Attributes.FOLLOW_RANGE, 32.0);
      return builder.add(Attributes.ATTACK_KNOCKBACK, 0.1);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (this.animationprocedure.equals("empty")) {
         return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F
            ? event.setAndContinue(RawAnimation.begin().thenLoop("misc.idle"))
            : event.setAndContinue(RawAnimation.begin().thenLoop("move.walk"));
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
