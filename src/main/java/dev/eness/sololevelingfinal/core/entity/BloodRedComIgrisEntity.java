package dev.eness.sololevelingfinal.core.entity;

import java.util.EnumSet;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
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
import dev.eness.sololevelingfinal.core.procedures.BloodRedComIgrisDeathTimeIsReachedProcedure;
import dev.eness.sololevelingfinal.core.procedures.BloodRedComIgrisOnEntityTickUpdateProcedure;
import dev.eness.sololevelingfinal.core.procedures.IgrisEntityDiesProcedure;
import dev.eness.sololevelingfinal.core.procedures.IgrisEntityIsHurtProcedure;
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

public class BloodRedComIgrisEntity extends Monster implements GeoEntity {
   public static final double DUNGEON_MAX_HEALTH = 150.0;
   public static final double DUNGEON_ATTACK_DAMAGE = 14.0;
   public static final double DUNGEON_ARMOR = 18.0;
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(BloodRedComIgrisEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(BloodRedComIgrisEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(BloodRedComIgrisEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> DATA_state = SynchedEntityData.defineId(BloodRedComIgrisEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_IA = SynchedEntityData.defineId(BloodRedComIgrisEntity.class, EntityDataSerializers.INT);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   public String animationprocedure = "empty";

   public BloodRedComIgrisEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.BLOOD_RED_COM_IGRIS.get(), world);
   }

   public BloodRedComIgrisEntity(EntityType<BloodRedComIgrisEntity> type, Level world) {
      super(type, world);
      this.xpReward = 0;
      this.setNoAi(false);
      this.setCustomName(Component.literal("§cBlood Red Commander Igris"));
      this.setCustomNameVisible(true);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHOOT, false);
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "igris_marcus");
      this.entityData.define(DATA_state, "idle");
      this.entityData.define(DATA_IA, 0);
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
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new BloodRedComIgrisEntity.IgrisCircleGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, false) {
         @Override
         protected double getAttackReachSqr(LivingEntity target) {
            return 5.0;
         }
      });
      this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.5));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
   }

   @Override
   public MobType getMobType() {
      return MobType.UNDEFINED;
   }

   @Override
   public boolean removeWhenFarAway(double d) {
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
      IgrisEntityIsHurtProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this, source.getEntity());
      if (source.is(DamageTypes.FALL)) {
         return false;
      } else {
         return source.is(DamageTypes.DROWN) ? false : super.hurt(source, amount);
      }
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      IgrisEntityDiesProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this, source.getEntity());
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
      compound.putString("Datastate", this.entityData.get(DATA_state));
      compound.putInt("DataIA", this.entityData.get(DATA_IA));
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

      if (compound.contains("DataIA")) {
         this.entityData.set(DATA_IA, compound.getInt("DataIA"));
      }

      if (!this.level().isClientSide()) {
         this.enforceDungeonBalance();
      }
   }

   private void enforceDungeonBalance() {
      float savedHealth = this.getHealth();
      if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(150.0);
      }

      if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(14.0);
      }

      if (this.getAttribute(Attributes.ARMOR) != null) {
         this.getAttribute(Attributes.ARMOR).setBaseValue(18.0);
      }

      this.setHealth(Math.max(0.0F, Math.min(savedHealth, this.getMaxHealth())));
   }

   @Override
   public void baseTick() {
      super.baseTick();
      BloodRedComIgrisOnEntityTickUpdateProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
      this.refreshDimensions();
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      return super.getDimensions(p_33597_).scale(1.0F);
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime == 20) {
         this.remove(RemovalReason.KILLED);
         this.dropExperience();
         BloodRedComIgrisDeathTimeIsReachedProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
      }
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.58)
         .add(Attributes.MAX_HEALTH, 150.0)
         .add(Attributes.ARMOR, 18.0)
         .add(Attributes.ATTACK_DAMAGE, 14.0)
         .add(Attributes.FOLLOW_RANGE, 48.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
         .add(Attributes.ATTACK_KNOCKBACK, 1.0);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (this.animationprocedure.equals("empty")) {
         if (this.isAggressive() && event.isMoving()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("run"));
         } else {
            return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F
               ? event.setAndContinue(RawAnimation.begin().thenLoop("idle"))
               : event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
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

   private static final class IgrisCircleGoal extends Goal {
      private final BloodRedComIgrisEntity igris;
      private double strafeSign = 1.0;
      private int recalcTimer = 0;

      IgrisCircleGoal(BloodRedComIgrisEntity igris) {
         this.igris = igris;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         LivingEntity t = this.igris.getTarget();
         if (t == null) {
            return false;
         }

         double dist = CombatRangeHelper.surfaceDistance(this.igris, t);
         return this.igris.getPersistentData().getString("state").equals("idle") && dist > 4.5 && dist < 15.0;
      }

      @Override
      public boolean canContinueToUse() {
         LivingEntity t = this.igris.getTarget();
         return t == null ? false : this.igris.getPersistentData().getString("state").equals("idle") && CombatRangeHelper.surfaceDistance(this.igris, t) < 15.0;
      }

      @Override
      public void start() {
         this.strafeSign = this.igris.getRandom().nextBoolean() ? 1.0 : -1.0;
         this.recalcTimer = 0;
      }

      @Override
      public void tick() {
         LivingEntity target = this.igris.getTarget();
         if (target != null) {
            this.recalcTimer--;
            if (this.recalcTimer <= 0) {
               this.recalcTimer = 10 + this.igris.getRandom().nextInt(8);
               if (this.igris.getRandom().nextFloat() < 0.22F) {
                  this.strafeSign = -this.strafeSign;
               }

               double dist = CombatRangeHelper.surfaceDistance(this.igris, target);
               Vec3 toTargetNorm = target.position().subtract(this.igris.position()).normalize();
               Vec3 lateral = new Vec3(-toTargetNorm.z * this.strafeSign, 0.0, toTargetNorm.x * this.strafeSign);
               Vec3 dest;
               if (dist > 7.0) {
                  dest = this.igris.position().add(toTargetNorm.scale(3.0)).add(lateral.scale(2.0));
               } else {
                  dest = target.position().add(toTargetNorm.scale(-5.5)).add(lateral.scale(3.5));
               }

               this.igris.getNavigation().moveTo(dest.x, dest.y, dest.z, 1.3);
            }
         }
      }
   }
}
