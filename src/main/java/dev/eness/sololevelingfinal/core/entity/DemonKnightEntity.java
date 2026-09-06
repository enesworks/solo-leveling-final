package dev.eness.sololevelingfinal.core.entity;

import java.util.UUID;
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
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
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

public class DemonKnightEntity extends Monster implements GeoEntity {
   private static final int TEXTURE_VARIANT_COUNT = 3;
   private static final float MIN_VISUAL_SCALE = 0.94F;
   private static final float MAX_NATURAL_VISUAL_SCALE = 1.08F;
   private static final float MAX_VISUAL_SCALE = 1.5F;
   private static final String VISUAL_SCALE_TAG = "DemonKnightVisualScale";
   public static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(DemonKnightEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Float> VISUAL_SCALE = SynchedEntityData.defineId(DemonKnightEntity.class, EntityDataSerializers.FLOAT);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private long lastSwing;
   public String animationprocedure = "empty";

   public DemonKnightEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.DEMON_KNIGHT.get(), world);
   }

   public DemonKnightEntity(EntityType<DemonKnightEntity> type, Level world) {
      super(type, world);
      this.setMaxUpStep(1.0F);
      this.xpReward = 0;
      this.setNoAi(false);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(VARIANT, 0);
      this.entityData.define(VISUAL_SCALE, 1.0F);
   }

   public int getVariant() {
      return this.entityData.get(VARIANT);
   }

   public void setVariant(int variant) {
      this.entityData.set(VARIANT, Math.max(0, Math.min(2, variant)));
   }

   public float getVisualScale() {
      return this.entityData.get(VISUAL_SCALE);
   }

   public void setVisualScale(float scale) {
      this.entityData.set(VISUAL_SCALE, Math.max(0.94F, Math.min(1.5F, scale)));
   }

   public void randomizeAppearance() {
      this.setVariant(this.random.nextInt(3));
      this.setVisualScale(0.94F + this.random.nextFloat() * 0.14000005F);
   }

   public void randomizeVariant() {
      this.randomizeAppearance();
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, false) {
         @Override
         protected double getAttackReachSqr(LivingEntity entity) {
            return 7.0;
         }
      });
      this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector
         .addGoal(
            2,
            new NearestAttackableTargetGoal<>(
               this,
               Player.class,
               5,
               true,
               false,
               candidate -> !this.getPersistentData().hasUUID("mowf_summon_owner")
                  && candidate instanceof Player player
                  && !player.isCreative()
                  && !player.isSpectator()
            )
         );
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
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
   public boolean isAlliedTo(Entity other) {
      if (super.isAlliedTo(other)) {
         return true;
      }

      if (!this.getPersistentData().hasUUID("mowf_summon_owner")) {
         return false;
      }

      UUID owner = this.getPersistentData().getUUID("mowf_summon_owner");
      return owner.equals(other.getUUID())
         || other.getPersistentData().hasUUID("mowf_summon_owner") && owner.equals(other.getPersistentData().getUUID("mowf_summon_owner"));
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      return source.getEntity() != null && this.isAlliedTo(source.getEntity()) ? false : super.hurt(source, amount);
   }

   @Override
   public boolean doHurtTarget(Entity target) {
      return !this.isAlliedTo(target) && super.doHurtTarget(target);
   }

   @Override
   protected void dropAllDeathLoot(DamageSource source) {
      if (!this.getPersistentData().getBoolean("mowf_no_loot")) {
         super.dropAllDeathLoot(source);
      }
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
      compound.putInt("Variant", this.getVariant());
      compound.putFloat("DemonKnightVisualScale", this.getVisualScale());
      compound.putDouble("dkc_floor_number", this.getPersistentData().getDouble("dkc_floor_number"));
      compound.putString("dkc_spawned_by", this.getPersistentData().getString("dkc_spawned_by"));
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Variant")) {
         this.setVariant(compound.getInt("Variant"));
      } else {
         this.setVariant(this.random.nextInt(3));
      }

      if (compound.contains("DemonKnightVisualScale")) {
         this.setVisualScale(compound.getFloat("DemonKnightVisualScale"));
      } else {
         this.setVisualScale(0.94F + this.random.nextFloat() * 0.14000005F);
      }

      if (compound.contains("dkc_floor_number")) {
         this.getPersistentData().putDouble("dkc_floor_number", compound.getDouble("dkc_floor_number"));
      }

      if (compound.contains("dkc_spawned_by")) {
         this.getPersistentData().putString("dkc_spawned_by", compound.getString("dkc_spawned_by"));
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      this.refreshDimensions();
      DkcWaveRuntime.tick(this);
   }

   @Override
   public EntityDimensions getDimensions(Pose p) {
      float eliteCollisionScale = this.getVisualScale() <= 1.08F ? 1.0F : Math.min(1.25F, this.getVisualScale() / 1.08F);
      return super.getDimensions(p).scale(eliteCollisionScale);
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MAX_HEALTH, 100.0);
      builder = builder.add(Attributes.ARMOR, 12.0);
      builder = builder.add(Attributes.ARMOR_TOUGHNESS, 4.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 20.0);
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
      builder = builder.add(Attributes.FOLLOW_RANGE, 48.0);
      builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
      return builder.add(Attributes.ATTACK_KNOCKBACK, 0.5);
   }

   private PlayState movementPredicate(AnimationState<DemonKnightEntity> event) {
      if (this.animationprocedure.equals("empty")) {
         return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F
            ? event.setAndContinue(RawAnimation.begin().thenLoop("idle"))
            : event.setAndContinue(RawAnimation.begin().thenLoop("run"));
      } else {
         return PlayState.STOP;
      }
   }

   private PlayState attackingPredicate(AnimationState<DemonKnightEntity> event) {
      if (this.getAttackAnim(event.getPartialTick()) > 0.0F && !this.swinging) {
         this.swinging = true;
         this.lastSwing = this.level().getGameTime();
      }

      if (this.swinging && this.lastSwing + 7L <= this.level().getGameTime()) {
         this.swinging = false;
      }

      if (this.swinging && event.getController().getAnimationState() == State.STOPPED) {
         event.getController().forceAnimationReset();
         return event.setAndContinue(RawAnimation.begin().thenPlay("attack"));
      } else {
         return PlayState.CONTINUE;
      }
   }

   private PlayState procedurePredicate(AnimationState<DemonKnightEntity> event) {
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

   @Override
   public void registerControllers(ControllerRegistrar data) {
      data.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
      data.add(new AnimationController<>(this, "attacking", 2, this::attackingPredicate));
      data.add(new AnimationController<>(this, "procedure", 4, this::procedurePredicate));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
