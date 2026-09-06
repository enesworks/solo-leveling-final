package dev.eness.sololevelingfinal.core.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.ai.BeruFlightMoveControl;
import dev.eness.sololevelingfinal.core.entity.ai.BeruShadowAerialCombatGoal;
import dev.eness.sololevelingfinal.core.entity.ai.ShadowCommandTargetGoal;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.procedures.BeruShadowOnInitialEntitySpawnProcedure;
import dev.eness.sololevelingfinal.core.procedures.CommandCallProcedureProcedure;
import dev.eness.sololevelingfinal.core.procedures.IsBerserkProcedure;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BeruShadowEntity extends TamableAnimal implements GeoEntity {
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_attackmode = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_CooldownTeleport = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_recovery = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<String> DATA_state = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_IA = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_IAI = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_CooldownUpslam = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_CooldownGroundslam = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<String> DATA_available_attacks = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_phase = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_SlamTimer = SynchedEntityData.defineId(BeruShadowEntity.class, EntityDataSerializers.INT);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private static final EntityDimensions SHADOW_BERU_DIMENSIONS = EntityDimensions.scalable(0.72F, 2.25F);
   private static final double FOLLOW_START_DISTANCE_SQR = 64.0;
   private static final double FOLLOW_STOP_DISTANCE_SQR = 16.0;
   private static final double EMERGENCY_RECALL_DISTANCE_SQR = 576.0;
   private static final int COMBAT_RECOVERY_FLIGHT_TICKS = 80;
   private static final int COMBAT_STUCK_TICKS = 32;
   private boolean aerialCombatActive;
   private boolean flightMovementActive;
   private boolean followingOwner;
   private int recallFlightTicks;
   private int followStuckTicks;
   private int nextFollowRepathTick;
   private double previousOwnerDistanceSqr = Double.MAX_VALUE;
   private int combatRecoveryFlightTicks;
   private int combatStuckTicks;
   private int nextCombatRecoveryRepathTick;
   private double previousTargetDistanceSqr = Double.MAX_VALUE;
   public String animationprocedure = "empty";

   public BeruShadowEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.BERU_SHADOW.get(), world);
   }

   public BeruShadowEntity(EntityType<BeruShadowEntity> type, Level world) {
      super(type, world);
      this.xpReward = 0;
      this.setNoAi(false);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHOOT, false);
      this.entityData.define(DATA_FLYING, false);
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "beru_shadow");
      this.entityData.define(DATA_attackmode, 0);
      this.entityData.define(DATA_CooldownTeleport, 0);
      this.entityData.define(DATA_recovery, 0);
      this.entityData.define(DATA_state, "idle");
      this.entityData.define(DATA_IA, 0);
      this.entityData.define(DATA_IAI, 0);
      this.entityData.define(DATA_CooldownUpslam, 0);
      this.entityData.define(DATA_CooldownGroundslam, 0);
      this.entityData.define(DATA_available_attacks, "");
      this.entityData.define(DATA_phase, 1);
      this.entityData.define(DATA_SlamTimer, 0);
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public String getTexture() {
      return this.entityData.get(TEXTURE);
   }

   @Override
   protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
      return Math.min(1.95F, sizeIn.height * 0.86F);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   protected PathNavigation createNavigation(Level world) {
      return this.createGroundNavigation(world);
   }

   private GroundPathNavigation createGroundNavigation(Level world) {
      GroundPathNavigation navigation = new GroundPathNavigation(this, world);
      navigation.setCanOpenDoors(true);
      navigation.setCanPassDoors(true);
      navigation.setCanFloat(true);
      return navigation;
   }

   private FlyingPathNavigation createFlightNavigation(Level world) {
      FlyingPathNavigation navigation = new FlyingPathNavigation(this, world);
      navigation.setCanOpenDoors(true);
      navigation.setCanFloat(true);
      return navigation;
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(0, new ShadowCommandTargetGoal(this));
      this.goalSelector.addGoal(1, new BeruShadowAerialCombatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.35, false) {
         @Override
         public boolean canUse() {
            return !BeruShadowEntity.this.isFlightModeActive() && BeruShadowEntity.this.onGround() && super.canUse();
         }

         @Override
         public boolean canContinueToUse() {
            return !BeruShadowEntity.this.isFlightModeActive() && BeruShadowEntity.this.onGround() && super.canContinueToUse();
         }

         @Override
         protected void checkAndPerformAttack(LivingEntity target, double distanceSqr) {
            if (distanceSqr <= this.getAttackReachSqr(target) && this.isTimeToAttack()) {
               BeruShadowEntity.this.setAnimation(BeruShadowEntity.this.getRandom().nextBoolean() ? "attack" : "attack2");
            }

            super.checkAndPerformAttack(target, distanceSqr);
         }

         @Override
         protected double getAttackReachSqr(LivingEntity target) {
            return 14.44;
         }
      });
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, GoblinArcherEntity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, GoblinClubEntity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, GoblinMageEntity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(6, new NearestAttackableTargetGoal(this, DKnight1Entity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, DKnight2Entity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(8, new NearestAttackableTargetGoal(this, DKnight3Entity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(9, new NearestAttackableTargetGoal(this, OrcEntity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(10, new NearestAttackableTargetGoal(this, KasakaEntity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(11, new NearestAttackableTargetGoal(this, MiniGemGolemEntity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(12, new NearestAttackableTargetGoal(this, GemGolemEntity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(13, new NearestAttackableTargetGoal(this, BeruBossEntity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(14, new NearestAttackableTargetGoal(this, IgrisEntity.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(15, new NearestAttackableTargetGoal(this, Monster.class, false, false) {
         @Override
         public boolean canUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canUse() && IsBerserkProcedure.execute(entity) && !ShadowMonarchManager.isShadowEntity(this.target);
         }

         @Override
         public boolean canContinueToUse() {
            double x = BeruShadowEntity.this.getX();
            double y = BeruShadowEntity.this.getY();
            double z = BeruShadowEntity.this.getZ();
            Entity entity = BeruShadowEntity.this;
            Level world = BeruShadowEntity.this.level();
            return super.canContinueToUse() && IsBerserkProcedure.execute(entity) && !ShadowMonarchManager.isShadowEntity(this.target);
         }
      });
      this.goalSelector.addGoal(20, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(21, new FloatGoal(this));
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
   public SoundEvent getHurtSound(DamageSource ds) {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
   }

   @Override
   public SoundEvent getDeathSound() {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (source.is(DamageTypes.FALL)) {
         return false;
      } else if (source.is(DamageTypes.CACTUS)) {
         return false;
      } else {
         return source.is(DamageTypes.DROWN) ? false : super.hurt(source, amount);
      }
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag
   ) {
      SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);
      BeruShadowOnInitialEntitySpawnProcedure.execute(this);
      return retval;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
      compound.putInt("Dataattackmode", this.entityData.get(DATA_attackmode));
      compound.putInt("DataCooldownTeleport", this.entityData.get(DATA_CooldownTeleport));
      compound.putInt("Datarecovery", this.entityData.get(DATA_recovery));
      compound.putString("Datastate", this.entityData.get(DATA_state));
      compound.putInt("DataIA", this.entityData.get(DATA_IA));
      compound.putInt("DataIAI", this.entityData.get(DATA_IAI));
      compound.putInt("DataCooldownUpslam", this.entityData.get(DATA_CooldownUpslam));
      compound.putInt("DataCooldownGroundslam", this.entityData.get(DATA_CooldownGroundslam));
      compound.putString("Dataavailable_attacks", this.entityData.get(DATA_available_attacks));
      compound.putInt("Dataphase", this.entityData.get(DATA_phase));
      compound.putInt("DataSlamTimer", this.entityData.get(DATA_SlamTimer));
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Texture")) {
         this.setTexture(compound.getString("Texture"));
      }

      if (compound.contains("Dataattackmode")) {
         this.entityData.set(DATA_attackmode, compound.getInt("Dataattackmode"));
      }

      if (compound.contains("DataCooldownTeleport")) {
         this.entityData.set(DATA_CooldownTeleport, compound.getInt("DataCooldownTeleport"));
      }

      if (compound.contains("Datarecovery")) {
         this.entityData.set(DATA_recovery, compound.getInt("Datarecovery"));
      }

      if (compound.contains("Datastate")) {
         this.entityData.set(DATA_state, compound.getString("Datastate"));
      }

      if (compound.contains("DataIA")) {
         this.entityData.set(DATA_IA, compound.getInt("DataIA"));
      }

      if (compound.contains("DataIAI")) {
         this.entityData.set(DATA_IAI, compound.getInt("DataIAI"));
      }

      if (compound.contains("DataCooldownUpslam")) {
         this.entityData.set(DATA_CooldownUpslam, compound.getInt("DataCooldownUpslam"));
      }

      if (compound.contains("DataCooldownGroundslam")) {
         this.entityData.set(DATA_CooldownGroundslam, compound.getInt("DataCooldownGroundslam"));
      }

      if (compound.contains("Dataavailable_attacks")) {
         this.entityData.set(DATA_available_attacks, compound.getString("Dataavailable_attacks"));
      }

      if (compound.contains("Dataphase")) {
         this.entityData.set(DATA_phase, compound.getInt("Dataphase"));
      }

      if (compound.contains("DataSlamTimer")) {
         this.entityData.set(DATA_SlamTimer, compound.getInt("DataSlamTimer"));
      }
   }

   @Override
   public InteractionResult mobInteract(Player sourceentity, InteractionHand hand) {
      ItemStack itemstack = sourceentity.getItemInHand(hand);
      InteractionResult retval = InteractionResult.sidedSuccess(this.level().isClientSide());
      Item item = itemstack.getItem();
      if (itemstack.getItem() instanceof SpawnEggItem) {
         retval = super.mobInteract(sourceentity, hand);
      } else if (this.level().isClientSide()) {
         retval = (!this.isTame() || !this.isOwnedBy(sourceentity)) && !this.isFood(itemstack)
            ? InteractionResult.PASS
            : InteractionResult.sidedSuccess(this.level().isClientSide());
      } else if (this.isTame()) {
         if (this.isOwnedBy(sourceentity)) {
            if (item.isEdible() && this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
               this.usePlayerItem(sourceentity, hand, itemstack);
               this.heal(item.getFoodProperties().getNutrition());
               retval = InteractionResult.sidedSuccess(this.level().isClientSide());
            } else if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
               this.usePlayerItem(sourceentity, hand, itemstack);
               this.heal(4.0F);
               retval = InteractionResult.sidedSuccess(this.level().isClientSide());
            } else {
               retval = super.mobInteract(sourceentity, hand);
            }
         }
      } else if (this.isFood(itemstack)) {
         this.usePlayerItem(sourceentity, hand, itemstack);
         if (this.random.nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(this, sourceentity)) {
            this.tame(sourceentity);
            this.level().broadcastEntityEvent(this, (byte)7);
         } else {
            this.level().broadcastEntityEvent(this, (byte)6);
         }

         this.setPersistenceRequired();
         retval = InteractionResult.sidedSuccess(this.level().isClientSide());
      } else {
         retval = super.mobInteract(sourceentity, hand);
         if (retval == InteractionResult.SUCCESS || retval == InteractionResult.CONSUME) {
            this.setPersistenceRequired();
         }
      }

      return retval;
   }

   @Override
   public void baseTick() {
      super.baseTick();
      CommandCallProcedureProcedure.execute(this.level(), this);
      this.refreshDimensions();
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      return SHADOW_BERU_DIMENSIONS;
   }

   @Override
   public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
      BeruShadowEntity retval = SololevelingModEntities.BERU_SHADOW.get().create(serverWorld);
      retval.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(retval.blockPosition()), MobSpawnType.BREEDING, null, null);
      return retval;
   }

   @Override
   public boolean isFood(ItemStack stack) {
      return List.of().contains(stack.getItem());
   }

   @Override
   public void aiStep() {
      super.aiStep();
      this.updateSwingTime();
      if (!this.level().isClientSide()) {
         this.updateNonCombatMovement();
         this.updateFlightAnimationState();
      }
   }

   public void setAerialCombatActive(boolean active) {
      this.aerialCombatActive = active;
   }

   public boolean isAerialCombatActive() {
      return this.aerialCombatActive;
   }

   public void setFlightMode(boolean active) {
      if (this.flightMovementActive == active) {
         this.setNoGravity(active);
         if (active) {
            this.entityData.set(DATA_FLYING, true);
         }
      } else {
         this.getNavigation().stop();
         this.flightMovementActive = active;
         if (active) {
            this.navigation = this.createFlightNavigation(this.level());
            this.moveControl = new BeruFlightMoveControl(this);
            this.setNoGravity(true);
            this.fallDistance = 0.0F;
            this.entityData.set(DATA_FLYING, true);
         } else {
            this.navigation = this.createGroundNavigation(this.level());
            this.moveControl = new MoveControl(this);
            this.setNoGravity(false);
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(movement.x * 0.35, Math.min(0.0, movement.y), movement.z * 0.35);
            this.setXRot(Mth.lerp(0.35F, this.getXRot(), 0.0F));
         }
      }
   }

   public boolean isFlightModeActive() {
      return this.flightMovementActive;
   }

   public boolean isFlyingAnimationActive() {
      return this.entityData.get(DATA_FLYING);
   }

   private void updateNonCombatMovement() {
      LivingEntity target = this.getTarget();
      if (target != null && !ShadowMonarchManager.canShadowDamage(this, target)) {
         this.setTarget(null);
         target = null;
      }

      if (!this.aerialCombatActive || target == null) {
         if (target != null) {
            this.resetOwnerFollowState(false);
            this.updateCombatTraversalMovement(target);
         } else {
            this.resetCombatTraversalState();
            LivingEntity owner = this.getOwner();
            if (owner != null && owner.level() == this.level() && ShadowMonarchManager.shouldFollowOwner(this)) {
               double ownerDistanceSqr = this.distanceToSqr(owner);
               if (ownerDistanceSqr > 64.0) {
                  this.followingOwner = true;
               } else if (ownerDistanceSqr < 16.0) {
                  this.followingOwner = false;
               }

               if (!this.followingOwner) {
                  if (this.flightMovementActive) {
                     this.setFlightMode(false);
                  }

                  if (ownerDistanceSqr < 16.0) {
                     this.getNavigation().stop();
                  }

                  this.followStuckTicks = 0;
                  this.recallFlightTicks = 0;
                  this.previousOwnerDistanceSqr = ownerDistanceSqr;
               } else {
                  if (this.flightMovementActive) {
                     this.tickEmergencyRecallFlight(owner, ownerDistanceSqr);
                  } else {
                     this.tickGroundedOwnerFollow(owner, ownerDistanceSqr);
                  }

                  this.previousOwnerDistanceSqr = ownerDistanceSqr;
               }
            } else {
               this.resetOwnerFollowState(true);
               if (this.flightMovementActive) {
                  this.setFlightMode(false);
               }
            }
         }
      }
   }

   private void tickGroundedOwnerFollow(LivingEntity owner, double ownerDistanceSqr) {
      if (this.tickCount >= this.nextFollowRepathTick || this.getNavigation().isDone()) {
         boolean foundPath = this.getNavigation().moveTo(owner, 1.25);
         this.nextFollowRepathTick = this.tickCount + 10;
         if (!foundPath) {
            this.followStuckTicks += 5;
         }
      }

      if (ownerDistanceSqr + 0.75 < this.previousOwnerDistanceSqr) {
         this.followStuckTicks = 0;
      } else {
         this.followStuckTicks++;
      }

      boolean separatedVertically = Math.abs(owner.getY() - this.getY()) > 4.5;
      boolean unsafeTraversal = this.isInWaterOrBubble() || !this.onGround() && this.fallDistance > 1.5F && this.getDeltaMovement().y < -0.08;
      if (unsafeTraversal || separatedVertically || ownerDistanceSqr > 576.0 || this.getNavigation().isStuck() || this.followStuckTicks >= 40) {
         this.setFlightMode(true);
         this.recallFlightTicks = 100;
         this.followStuckTicks = 0;
         this.nextFollowRepathTick = 0;
      }
   }

   public void beginTraversalRecoveryFlight(LivingEntity objective) {
      if (objective != null && objective.level() == this.level() && ShadowMonarchManager.canShadowDamage(this, objective)) {
         this.setTarget(objective);
         this.startCombatRecoveryFlight();
      }
   }

   private void updateCombatTraversalMovement(LivingEntity target) {
      double targetDistanceSqr = this.distanceToSqr(target);
      if (this.flightMovementActive && this.combatRecoveryFlightTicks > 0) {
         this.tickCombatRecoveryFlight(target, targetDistanceSqr);
         this.previousTargetDistanceSqr = targetDistanceSqr;
      } else {
         if (this.flightMovementActive) {
            this.setFlightMode(false);
         }

         if (targetDistanceSqr + 0.75 < this.previousTargetDistanceSqr) {
            this.combatStuckTicks = 0;
         } else if (targetDistanceSqr > 16.0) {
            this.combatStuckTicks++;
         } else {
            this.combatStuckTicks = Math.max(0, this.combatStuckTicks - 2);
         }

         boolean falling = !this.onGround() && !this.isInWaterOrBubble() && this.fallDistance > 1.5F && this.getDeltaMovement().y < -0.08;
         boolean verticallyBlocked = Math.abs(target.getY() - this.getY()) > 3.5 && (this.getNavigation().isDone() || !this.hasLineOfSight(target));
         if (this.isInWaterOrBubble()
            || falling
            || verticallyBlocked
            || this.getNavigation().isStuck()
            || this.horizontalCollision && this.combatStuckTicks >= 10
            || this.combatStuckTicks >= 32) {
            this.startCombatRecoveryFlight();
         }

         this.previousTargetDistanceSqr = targetDistanceSqr;
      }
   }

   private void startCombatRecoveryFlight() {
      this.combatRecoveryFlightTicks = Math.max(this.combatRecoveryFlightTicks, 80);
      this.combatStuckTicks = 0;
      this.nextCombatRecoveryRepathTick = 0;
      if (!this.flightMovementActive) {
         this.setFlightMode(true);
         this.setAnimation("start_flying");
      }
   }

   private void tickCombatRecoveryFlight(LivingEntity target, double targetDistanceSqr) {
      this.fallDistance = 0.0F;
      this.combatRecoveryFlightTicks--;
      if (this.tickCount >= this.nextCombatRecoveryRepathTick || this.getNavigation().isDone()) {
         double wantedY = targetDistanceSqr < 25.0 && this.hasLineOfSight(target) ? target.getY() + 0.2 : target.getEyeY() + 0.35;
         boolean foundPath = this.getNavigation().moveTo(target.getX(), wantedY, target.getZ(), 2.2);
         if (!foundPath) {
            this.getMoveControl().setWantedPosition(target.getX(), wantedY, target.getZ(), 2.2);
         }

         this.nextCombatRecoveryRepathTick = this.tickCount + 6;
      }

      boolean landedNearTarget = this.onGround() && !this.isInWaterOrBubble() && targetDistanceSqr < 36.0 && this.hasLineOfSight(target);
      if (landedNearTarget || this.combatRecoveryFlightTicks <= 0) {
         this.setFlightMode(false);
         this.combatRecoveryFlightTicks = 0;
         this.combatStuckTicks = 0;
         this.nextCombatRecoveryRepathTick = 0;
      }
   }

   private void resetCombatTraversalState() {
      this.combatRecoveryFlightTicks = 0;
      this.combatStuckTicks = 0;
      this.nextCombatRecoveryRepathTick = 0;
      this.previousTargetDistanceSqr = Double.MAX_VALUE;
   }

   private void tickEmergencyRecallFlight(LivingEntity owner, double ownerDistanceSqr) {
      this.fallDistance = 0.0F;
      this.recallFlightTicks--;
      if (this.tickCount >= this.nextFollowRepathTick || this.getNavigation().isDone()) {
         double wantedY = owner.getEyeY() + 0.35;
         boolean foundPath = this.getNavigation().moveTo(owner.getX(), wantedY, owner.getZ(), 2.15);
         if (!foundPath) {
            this.getMoveControl().setWantedPosition(owner.getX(), wantedY, owner.getZ(), 2.15);
         }

         this.nextFollowRepathTick = this.tickCount + 6;
      }

      if (ownerDistanceSqr < 16.0 || this.recallFlightTicks <= 0) {
         this.setFlightMode(false);
         this.recallFlightTicks = 0;
         this.followStuckTicks = 0;
      }
   }

   private void resetOwnerFollowState(boolean stopNavigation) {
      this.followingOwner = false;
      this.recallFlightTicks = 0;
      this.followStuckTicks = 0;
      this.nextFollowRepathTick = 0;
      this.previousOwnerDistanceSqr = Double.MAX_VALUE;
      if (stopNavigation) {
         this.getNavigation().stop();
      }
   }

   private void updateFlightAnimationState() {
      boolean airborne = this.flightMovementActive || !this.onGround();
      if (this.entityData.get(DATA_FLYING) != airborne) {
         this.entityData.set(DATA_FLYING, airborne);
      }

      if (!airborne) {
         this.setXRot(Mth.lerp(0.25F, this.getXRot(), 0.0F));
      }
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.25);
      builder = builder.add(Attributes.FLYING_SPEED, 0.35);
      builder = builder.add(Attributes.MAX_HEALTH, 160.0);
      builder = builder.add(Attributes.ARMOR, 100.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 20.0);
      builder = builder.add(Attributes.FOLLOW_RANGE, 128.0);
      builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 0.1);
      return builder.add(Attributes.ATTACK_KNOCKBACK, 1.0);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (this.isFlyingAnimationActive()) {
         return event.setAndContinue(RawAnimation.begin().thenLoop("flying"));
      } else {
         return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F
            ? event.setAndContinue(RawAnimation.begin().thenLoop("idle"))
            : event.setAndContinue(RawAnimation.begin().thenLoop("walking"));
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
      this.entityData.set(ANIMATION, "undefined");
      if (animation != null && !animation.isBlank() && !animation.equals("undefined") && !animation.equals("empty")) {
         this.triggerAnim("procedure", animation);
      }
   }

   @Override
   public void registerControllers(ControllerRegistrar data) {
      data.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
      AnimationController<BeruShadowEntity> procedure = new AnimationController<>(this, "procedure", 0, this::procedurePredicate);
      procedure.triggerableAnim("attack", RawAnimation.begin().thenPlay("attack"));
      procedure.triggerableAnim("attack2", RawAnimation.begin().thenPlay("attack2"));
      procedure.triggerableAnim("start_flying", RawAnimation.begin().thenPlay("start_flying"));
      procedure.triggerableAnim("flyattack", RawAnimation.begin().thenPlay("flyattack"));
      procedure.triggerableAnim("special_attack", RawAnimation.begin().thenPlay("special_attack"));
      procedure.triggerableAnim("hurt", RawAnimation.begin().thenPlay("hurt"));
      procedure.triggerableAnim("death", RawAnimation.begin().thenPlay("death"));
      procedure.triggerableAnim("scream", RawAnimation.begin().thenPlay("scream"));
      data.add(procedure);
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
