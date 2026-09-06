package dev.eness.sololevelingfinal.core.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.procedures.StatueOfGodOnEntityTickUpdateProcedure;
import dev.eness.sololevelingfinal.core.procedures.StatueOfGodOnInitialEntitySpawnProcedure;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class StatueOfGodEntity extends Monster implements GeoEntity {
   private static final String STORY_STATUE_TAG = "slr_story_intro_statue";
   private static final String STORY_INSTANCE_TAG = "slr_story_intro_instance";
   private static final String STORY_OWNER_TAG = "slr_story_intro_owner";
   private static final String STORY_HUNTER_TAG = "slr_story_intro_hunter";
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> DATA_state = SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Boolean> DATA_smiled = SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Boolean> DATA_story_upright = SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Integer> DATA_default_x = SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_default_y = SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_default_z = SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.INT);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private boolean lastloop;
   private boolean dimensionsInitialized;
   private long lastSwing;
   public String animationprocedure = "empty";

   public StatueOfGodEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.STATUE_OF_GOD.get(), world);
   }

   public StatueOfGodEntity(EntityType<StatueOfGodEntity> type, Level world) {
      super(type, world);
      this.xpReward = 100;
      this.setNoAi(false);
      this.setPersistenceRequired();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHOOT, false);
      this.entityData.define(ANIMATION, "undefined");
      this.entityData.define(TEXTURE, "statue_of_god");
      this.entityData.define(DATA_state, "throne");
      this.entityData.define(DATA_smiled, true);
      this.entityData.define(DATA_story_upright, false);
      this.entityData.define(DATA_default_x, 0);
      this.entityData.define(DATA_default_y, 0);
      this.entityData.define(DATA_default_z, 0);
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
      super.registerGoals();
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
         @Override
         public boolean canUse() {
            return !StatueOfGodEntity.this.isStoryIntroStatue() && super.canUse();
         }

         @Override
         public boolean canContinueToUse() {
            return !StatueOfGodEntity.this.isStoryIntroStatue() && super.canContinueToUse();
         }

         @Override
         protected double getAttackReachSqr(LivingEntity entity) {
            return 25.0;
         }
      });
   }

   public boolean isStoryIntroStatue() {
      return this.getPersistentData().getBoolean("slr_story_intro_statue");
   }

   private boolean isValidCombatTarget(LivingEntity candidate) {
      return this.getPersistentData().getBoolean("slr_story_intro_statue")
         ? this.isValidStoryCombatTarget(candidate)
         : candidate instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator();
   }

   private boolean isValidStoryCombatTarget(LivingEntity candidate) {
      if (candidate != null && candidate.isAlive()) {
         CompoundTag statueData = this.getPersistentData();
         if (!(candidate instanceof HunterEntity hunter)) {
            return candidate instanceof Player player && !player.isCreative() && !player.isSpectator() && statueData.hasUUID("slr_story_intro_owner")
               ? player.getUUID().equals(statueData.getUUID("slr_story_intro_owner"))
               : false;
         } else {
            CompoundTag hunterData = hunter.getPersistentData();
            return hunterData.getBoolean("slr_story_intro_hunter")
               && hunterData.getInt("slr_story_intro_instance") == statueData.getInt("slr_story_intro_instance");
         }
      } else {
         return false;
      }
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
   public boolean hurt(DamageSource source, float amount) {
      return !source.is(DamageTypes.CACTUS) && !(amount < 25.0F) && !isSharpDamage(source) ? super.hurt(source, amount) : false;
   }

   private static boolean isSharpDamage(DamageSource source) {
      if (source.getDirectEntity() instanceof AbstractArrow) {
         return true;
      } else {
         return !(source.getEntity() instanceof LivingEntity attacker)
            ? false
            : isSharpWeapon(attacker.getMainHandItem()) || isSharpWeapon(attacker.getOffhandItem());
      }
   }

   private static boolean isSharpWeapon(ItemStack stack) {
      if (!(stack.getItem() instanceof SwordItem) && !(stack.getItem() instanceof AxeItem) && !(stack.getItem() instanceof TridentItem)) {
         ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
         if (itemId == null) {
            return false;
         }

         String path = itemId.getPath();
         return path.contains("sword")
            || path.contains("dagger")
            || path.contains("knife")
            || path.contains("blade")
            || path.contains("katana")
            || path.contains("spear")
            || path.contains("scythe")
            || path.contains("sai")
            || path.contains("trident");
      } else {
         return true;
      }
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag
   ) {
      SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);
      StatueOfGodOnInitialEntitySpawnProcedure.execute(this);
      return retval;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
      compound.putString("Datastate", this.entityData.get(DATA_state));
      compound.putBoolean("Datasmiled", this.entityData.get(DATA_smiled));
      compound.putBoolean("DataStoryUpright", this.entityData.get(DATA_story_upright));
      compound.putInt("Datadefault_x", this.entityData.get(DATA_default_x));
      compound.putInt("Datadefault_y", this.entityData.get(DATA_default_y));
      compound.putInt("Datadefault_z", this.entityData.get(DATA_default_z));
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

      if (compound.contains("Datasmiled")) {
         this.entityData.set(DATA_smiled, compound.getBoolean("Datasmiled"));
      }

      if (compound.contains("DataStoryUpright")) {
         this.entityData.set(DATA_story_upright, compound.getBoolean("DataStoryUpright"));
      }

      if (compound.contains("Datadefault_x")) {
         this.entityData.set(DATA_default_x, compound.getInt("Datadefault_x"));
      }

      if (compound.contains("Datadefault_y")) {
         this.entityData.set(DATA_default_y, compound.getInt("Datadefault_y"));
      }

      if (compound.contains("Datadefault_z")) {
         this.entityData.set(DATA_default_z, compound.getInt("Datadefault_z"));
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      if (!this.dimensionsInitialized) {
         this.refreshDimensions();
         this.dimensionsInitialized = true;
      }

      StatueOfGodOnEntityTickUpdateProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide() && this.getPersistentData().getString("state").equals("aggresive") && this.isValidCombatTarget(this.getTarget())) {
         this.faceTarget(this.getTarget());
      }
   }

   public void faceTarget(LivingEntity target) {
      if (target != null) {
         double dx = target.getX() - this.getX();
         double dz = target.getZ() - this.getZ();
         if (!(dx * dx + dz * dz < 1.0E-6)) {
            this.faceYaw((float)(Mth.atan2(dz, dx) * 180.0F / (float)Math.PI) - 90.0F);
         }
      }
   }

   public void faceYaw(float yaw) {
      float wrappedYaw = Mth.wrapDegrees(yaw);
      this.setYRot(wrappedYaw);
      this.yRotO = wrappedYaw;
      this.yBodyRot = wrappedYaw;
      this.yBodyRotO = wrappedYaw;
      this.yHeadRot = wrappedYaw;
      this.yHeadRotO = wrappedYaw;
   }

   @Override
   public EntityDimensions getDimensions(Pose p_33597_) {
      return EntityDimensions.scalable(5.25F, 23.25F);
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.4);
      builder = builder.add(Attributes.MAX_HEALTH, 700.0);
      builder = builder.add(Attributes.ARMOR, 0.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 85.0);
      return builder.add(Attributes.FOLLOW_RANGE, 256.0);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (!this.animationprocedure.equals("empty")) {
         return PlayState.STOP;
      } else {
         String state = this.entityData.get(DATA_state);
         boolean storyWakeFinished = state.equals("waking") && this.entityData.get(DATA_story_upright);
         if (!state.equals("aggresive") && !storyWakeFinished) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("sitting"));
         } else {
            return this.isActuallyMoving()
               ? event.setAndContinue(RawAnimation.begin().thenLoop("walk"))
               : event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
         }
      }
   }

   private boolean isActuallyMoving() {
      double dx = this.getX() - this.xOld;
      double dz = this.getZ() - this.zOld;
      return dx * dx + dz * dz > 1.0E-6 || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5;
   }

   private PlayState procedurePredicate(AnimationState event) {
      if (this.animationprocedure.equals("empty")) {
         this.lastloop = false;
         return PlayState.STOP;
      } else if (!this.lastloop) {
         this.lastloop = true;
         event.getController().forceAnimationReset();
         return event.setAndContinue(RawAnimation.begin().thenPlay(this.animationprocedure));
      } else if (event.getController().getAnimationState() == State.STOPPED) {
         this.animationprocedure = "empty";
         this.lastloop = false;
         event.getController().forceAnimationReset();
         return PlayState.STOP;
      } else {
         return PlayState.CONTINUE;
      }
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime == 1) {
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
