package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.dungeon.DungeonScalableEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.procedures.GoblinArcherOnEntityTickUpdateProcedure;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GoblinArcherEntity extends Monster implements GeoEntity, DungeonScalableEntity {
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(GoblinArcherEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(GoblinArcherEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(GoblinArcherEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> DATA_state = SynchedEntityData.defineId(GoblinArcherEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_MF = SynchedEntityData.defineId(GoblinArcherEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Float> DATA_DUNGEON_SCALE = SynchedEntityData.defineId(GoblinArcherEntity.class, EntityDataSerializers.FLOAT);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private boolean lastloop;
   private long lastSwing;
   public String animationprocedure = "empty";

   public GoblinArcherEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.GOBLIN_ARCHER.get(), world);
   }

   public GoblinArcherEntity(EntityType<GoblinArcherEntity> type, Level world) {
      super(type, world);
      this.xpReward = 0;
      this.setNoAi(false);
      this.setCustomName(Component.literal("Goblin Archer "));
      this.setCustomNameVisible(true);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHOOT, false);
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "goblin_archer");
      this.entityData.define(DATA_state, "");
      this.entityData.define(DATA_MF, 0);
      this.entityData.define(DATA_DUNGEON_SCALE, 1.0F);
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public String getTexture() {
      return this.entityData.get(TEXTURE);
   }

   @Override
   public float getDungeonScale() {
      return this.entityData.get(DATA_DUNGEON_SCALE);
   }

   @Override
   public void setDungeonScale(float scale) {
      this.entityData.set(DATA_DUNGEON_SCALE, Math.max(0.9F, Math.min(1.68F, scale)));
   }

   @Override
   protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
      return 1.4F * this.getDungeonScale();
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, ServerPlayer.class, false, false));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, false, false));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Pillager.class, false, false));
      this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
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
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
      compound.putString("Datastate", this.entityData.get(DATA_state));
      compound.putInt("DataMF", this.entityData.get(DATA_MF));
      compound.putFloat("DungeonScale", this.getDungeonScale());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Texture")) {
         this.setTexture(compound.getString("Texture"));
      }

      if (compound.contains("Datastate")) {
         this.entityData.set(DATA_state, compound.getString("Datastate"));
      }

      if (compound.contains("DataMF")) {
         this.entityData.set(DATA_MF, compound.getInt("DataMF"));
      }

      if (compound.contains("DungeonScale")) {
         this.setDungeonScale(compound.getFloat("DungeonScale"));
      }
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (DATA_DUNGEON_SCALE.equals(key)) {
         this.refreshDimensions();
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      GoblinArcherOnEntityTickUpdateProcedure.execute(this);
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      return super.getDimensions(p_33597_).scale(this.getDungeonScale());
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
      builder = builder.add(Attributes.MAX_HEALTH, 16.0);
      builder = builder.add(Attributes.ARMOR, 0.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 0.0);
      builder = builder.add(Attributes.FOLLOW_RANGE, 40.0);
      return builder.add(Attributes.ATTACK_KNOCKBACK, 0.8);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (this.animationprocedure.equals("empty")) {
         if (event.isMoving() || !(event.getLimbSwingAmount() > -0.15F) || !(event.getLimbSwingAmount() < 0.15F)) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
         } else if (this.isDeadOrDying()) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("death"));
         } else {
            return this.isSprinting()
               ? event.setAndContinue(RawAnimation.begin().thenLoop("run"))
               : event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
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
      if (this.deathTime == 80) {
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
