package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.ai.ShadowThreatTargetGoal;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PolarBearEntity extends Monster implements GeoEntity {
   private static final String COMBAT_BALANCE_VERSION_TAG = "SLRPolarBearCombatBalanceVersion";
   private static final int COMBAT_BALANCE_VERSION = 1;
   private static final double LEGACY_ATTACK_DAMAGE_REDUCTION = 14.0;
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(PolarBearEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(PolarBearEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(PolarBearEntity.class, EntityDataSerializers.STRING);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private boolean lastloop;
   private long lastSwing;
   private ShadowThreatTargetGoal shadowThreatGoal;
   public String animationprocedure = "empty";

   public PolarBearEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.POLAR_BEAR.get(), world);
   }

   public PolarBearEntity(EntityType<PolarBearEntity> type, Level world) {
      super(type, world);
      this.xpReward = 25;
      this.setNoAi(false);
      this.setCustomName(Component.literal("Polar Bear"));
      this.setCustomNameVisible(true);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHOOT, false);
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "polarbear");
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public String getTexture() {
      return this.entityData.get(TEXTURE);
   }

   @Override
   protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
      return 1.8F;
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
            return 9.0;
         }
      });
      this.targetSelector.addGoal(4, new HurtByTargetGoal(this));
      this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
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
   protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
      super.dropCustomDeathLoot(source, looting, recentlyHitIn);
      this.spawnAtLocation(new ItemStack(SololevelingModItems.MANA_CRYSTAL_A.get()));
   }

   @Override
   public SoundEvent getAmbientSound() {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.polar_bear.ambient"));
   }

   @Override
   public void playStepSound(BlockPos pos, BlockState blockIn) {
      this.playSound(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.polar_bear.step")), 0.15F, 1.0F);
   }

   @Override
   public SoundEvent getHurtSound(DamageSource ds) {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.polar_bear.hurt"));
   }

   @Override
   public SoundEvent getDeathSound() {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.polar_bear.death"));
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
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
      compound.putInt("SLRPolarBearCombatBalanceVersion", 1);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Texture")) {
         this.setTexture(compound.getString("Texture"));
      }

      if (compound.getInt("SLRPolarBearCombatBalanceVersion") < 1 && this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.max(1.0, this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() - 14.0));
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      this.refreshDimensions();
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      return super.getDimensions(p_33597_).scale(1.5F);
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
      builder = builder.add(Attributes.MAX_HEALTH, 60.0);
      builder = builder.add(Attributes.ARMOR, 16.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 8.0);
      return builder.add(Attributes.FOLLOW_RANGE, 16.0);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (this.animationprocedure.equals("empty")) {
         return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F
            ? event.setAndContinue(RawAnimation.begin().thenLoop("idle"))
            : event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
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
