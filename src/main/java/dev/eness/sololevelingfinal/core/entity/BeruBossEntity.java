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
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.procedures.BeruBossDeathTimeIsReachedProcedure;
import dev.eness.sololevelingfinal.core.procedures.BeruBossOnEntityTickUpdateProcedure;
import dev.eness.sololevelingfinal.core.procedures.BeruEntityDiesProcedure;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BeruBossEntity extends Monster implements GeoEntity {
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_CooldownTeleport = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<String> DATA_state = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_IA = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<String> DATA_available_attacks = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_CooldownUpslam = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_CooldownGroundslam = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_IAI = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_phase = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_SlamTimer = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_recovery = SynchedEntityData.defineId(BeruBossEntity.class, EntityDataSerializers.INT);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private boolean lastloop;
   private long lastSwing;
   public String animationprocedure = "empty";

   public BeruBossEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.BERU_BOSS.get(), world);
   }

   public BeruBossEntity(EntityType<BeruBossEntity> type, Level world) {
      super(type, world);
      this.xpReward = 50;
      this.setNoAi(false);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHOOT, false);
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "beru_base");
      this.entityData.define(DATA_CooldownTeleport, 0);
      this.entityData.define(DATA_state, "idle");
      this.entityData.define(DATA_IA, 0);
      this.entityData.define(DATA_available_attacks, "");
      this.entityData.define(DATA_CooldownUpslam, 0);
      this.entityData.define(DATA_CooldownGroundslam, 0);
      this.entityData.define(DATA_IAI, 0);
      this.entityData.define(DATA_phase, 1);
      this.entityData.define(DATA_SlamTimer, 0);
      this.entityData.define(DATA_recovery, 0);
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public String getTexture() {
      return this.entityData.get(TEXTURE);
   }

   @Override
   protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
      return 4.0F;
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
            return 4.0;
         }
      });
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Slime.class, false, true));
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
   public double getPassengersRidingOffset() {
      return super.getPassengersRidingOffset() + 0.2;
   }

   @Override
   protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
      super.dropCustomDeathLoot(source, looting, recentlyHitIn);
      this.spawnAtLocation(new ItemStack(SololevelingModItems.RUNESTONE_PHYSICAL.get()));
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
      this.entityData.set(DATA_recovery, 5);
      if (!this.level().isClientSide && this.random.nextInt(3) == 2) {
         this.setAnimation("attack2");
      }

      if (source.is(DamageTypes.FALL)) {
         return false;
      } else {
         return source.is(DamageTypes.DROWN) ? false : super.hurt(source, amount);
      }
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      BeruEntityDiesProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this, source.getEntity());
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
      compound.putInt("DataCooldownTeleport", this.entityData.get(DATA_CooldownTeleport));
      compound.putString("Datastate", this.entityData.get(DATA_state));
      compound.putInt("DataIA", this.entityData.get(DATA_IA));
      compound.putString("Dataavailable_attacks", this.entityData.get(DATA_available_attacks));
      compound.putInt("DataCooldownUpslam", this.entityData.get(DATA_CooldownUpslam));
      compound.putInt("DataCooldownGroundslam", this.entityData.get(DATA_CooldownGroundslam));
      compound.putInt("DataIAI", this.entityData.get(DATA_IAI));
      compound.putInt("Dataphase", this.entityData.get(DATA_phase));
      compound.putInt("DataSlamTimer", this.entityData.get(DATA_SlamTimer));
      compound.putInt("Datarecovery", this.entityData.get(DATA_recovery));
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Texture")) {
         this.setTexture(compound.getString("Texture"));
      }

      if (compound.contains("DataCooldownTeleport")) {
         this.entityData.set(DATA_CooldownTeleport, compound.getInt("DataCooldownTeleport"));
      }

      if (compound.contains("Datastate")) {
         this.entityData.set(DATA_state, compound.getString("Datastate"));
      }

      if (compound.contains("DataIA")) {
         this.entityData.set(DATA_IA, compound.getInt("DataIA"));
      }

      if (compound.contains("Dataavailable_attacks")) {
         this.entityData.set(DATA_available_attacks, compound.getString("Dataavailable_attacks"));
      }

      if (compound.contains("DataCooldownUpslam")) {
         this.entityData.set(DATA_CooldownUpslam, compound.getInt("DataCooldownUpslam"));
      }

      if (compound.contains("DataCooldownGroundslam")) {
         this.entityData.set(DATA_CooldownGroundslam, compound.getInt("DataCooldownGroundslam"));
      }

      if (compound.contains("DataIAI")) {
         this.entityData.set(DATA_IAI, compound.getInt("DataIAI"));
      }

      if (compound.contains("Dataphase")) {
         this.entityData.set(DATA_phase, compound.getInt("Dataphase"));
      }

      if (compound.contains("DataSlamTimer")) {
         this.entityData.set(DATA_SlamTimer, compound.getInt("DataSlamTimer"));
      }

      if (compound.contains("Datarecovery")) {
         this.entityData.set(DATA_recovery, compound.getInt("Datarecovery"));
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      BeruBossOnEntityTickUpdateProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
      this.refreshDimensions();
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      return super.getDimensions(p_33597_).scale(0.8F);
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.4);
      builder = builder.add(Attributes.MAX_HEALTH, 200.0);
      builder = builder.add(Attributes.ARMOR, 90.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 18.0);
      builder = builder.add(Attributes.FOLLOW_RANGE, 128.0);
      builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 0.1);
      return builder.add(Attributes.ATTACK_KNOCKBACK, 1.0);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (!this.animationprocedure.equals("empty")) {
         return PlayState.STOP;
      } else if ((event.isMoving() || !(event.getLimbSwingAmount() > -0.15F) || !(event.getLimbSwingAmount() < 0.15F))
         && this.onGround()
         && !this.isAggressive()) {
         return event.setAndContinue(RawAnimation.begin().thenLoop("walking"));
      } else if (!this.onGround()) {
         return event.setAndContinue(RawAnimation.begin().thenLoop("flying"));
      } else {
         return this.isAggressive() && event.isMoving()
            ? event.setAndContinue(RawAnimation.begin().thenLoop("flying"))
            : event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
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
         return event.setAndContinue(RawAnimation.begin().thenPlay("flyattack"));
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
         BeruBossDeathTimeIsReachedProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
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
