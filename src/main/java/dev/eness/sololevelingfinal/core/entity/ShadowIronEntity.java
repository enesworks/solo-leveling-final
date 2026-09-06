package dev.eness.sololevelingfinal.core.entity;

import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.entity.ai.ShadowCommandTargetGoal;
import dev.eness.sololevelingfinal.core.entity.ai.ShadowFollowOwnerGoal;
import dev.eness.sololevelingfinal.core.entity.ai.ShadowIronCombatGoal;
import dev.eness.sololevelingfinal.core.entity.ai.ShadowIronCombatPolicy;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.util.ShadowIronCombatManager;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ShadowIronEntity extends TamableAnimal implements GeoEntity {
   private static final EntityDataAccessor<String> ACTION = SynchedEntityData.defineId(ShadowIronEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Boolean> DOMAIN_BOOSTED = SynchedEntityData.defineId(ShadowIronEntity.class, EntityDataSerializers.BOOLEAN);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private int actionTick;
   private UUID actionTargetId;
   private UUID blockedAttackerId;
   private boolean counterAttack;
   private boolean blockConnected;
   private boolean rescueBlock;
   private long nextAttackAt;
   private long nextBlockAt;
   private long nextRoarAt;
   private long nextInterceptAt;
   private long fortifyUntil;
   private int fortifyTargets;

   public ShadowIronEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.SHADOW_IRON.get(), level);
   }

   public ShadowIronEntity(EntityType<? extends ShadowIronEntity> type, Level level) {
      super(type, level);
      this.setPersistenceRequired();
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 0.0F);
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ACTION, ShadowIronEntity.Action.NONE.serializedName);
      this.entityData.define(DOMAIN_BOOSTED, false);
   }

   @Override
   protected PathNavigation createNavigation(Level level) {
      GroundPathNavigation navigation = new GroundPathNavigation(this, level);
      navigation.setCanOpenDoors(true);
      navigation.setCanPassDoors(true);
      navigation.setCanFloat(true);
      return navigation;
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(0, new ShadowCommandTargetGoal(this));
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new ShadowIronCombatGoal(this));
      this.goalSelector.addGoal(2, new ShadowFollowOwnerGoal(this));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.28)
         .add(Attributes.MAX_HEALTH, 140.0)
         .add(Attributes.ARMOR, 18.0)
         .add(Attributes.ARMOR_TOUGHNESS, 4.0)
         .add(Attributes.ATTACK_DAMAGE, 8.0)
         .add(Attributes.FOLLOW_RANGE, 40.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
         .add(Attributes.ATTACK_KNOCKBACK, 0.7);
   }

   public static void init() {
   }

   public ShadowIronEntity.Action getCombatAction() {
      return ShadowIronEntity.Action.fromSerializedName(this.entityData.get(ACTION));
   }

   public boolean isActionIdle() {
      return this.getCombatAction() == ShadowIronEntity.Action.NONE;
   }

   public boolean isActing() {
      return !this.isActionIdle();
   }

   public boolean isDomainBoosted() {
      return this.entityData.get(DOMAIN_BOOSTED);
   }

   public void setDomainBoosted(boolean boosted) {
      this.entityData.set(DOMAIN_BOOSTED, boosted);
   }

   public int getActionTick() {
      return this.actionTick;
   }

   public boolean beginAttack(LivingEntity target, boolean counter) {
      if (!this.level().isClientSide() && this.isActionIdle() && target != null && target.isAlive() && this.canAttackNow()) {
         this.counterAttack = counter;
         this.actionTargetId = target.getUUID();
         this.nextAttackAt = this.level().getGameTime() + 26L;
         this.beginAction(ShadowIronEntity.Action.ATTACK);
         return true;
      } else {
         return false;
      }
   }

   public boolean beginBlock(boolean rescue) {
      if (!this.level().isClientSide() && this.canBlockNow() && (this.isActionIdle() || rescue && this.getCombatAction() != ShadowIronEntity.Action.BLOCK)) {
         if (rescue && !this.isActionIdle()) {
            this.clearAction();
         }

         this.rescueBlock = rescue;
         this.blockConnected = false;
         this.blockedAttackerId = null;
         this.nextBlockAt = this.level().getGameTime() + 80L;
         this.beginAction(ShadowIronEntity.Action.BLOCK);
         return true;
      } else {
         return false;
      }
   }

   public boolean beginRoar() {
      if (!this.level().isClientSide() && this.isActionIdle() && this.canRoarNow()) {
         this.nextRoarAt = this.level().getGameTime() + 360L;
         this.beginAction(ShadowIronEntity.Action.ROAR);
         return true;
      } else {
         return false;
      }
   }

   private void beginAction(ShadowIronEntity.Action action) {
      this.actionTick = 0;
      this.entityData.set(ACTION, action.serializedName);
      this.getNavigation().stop();
      this.triggerAnim("action", action.serializedName);
   }

   private void clearAction() {
      this.entityData.set(ACTION, ShadowIronEntity.Action.NONE.serializedName);
      this.actionTick = 0;
      this.actionTargetId = null;
      this.counterAttack = false;
      this.rescueBlock = false;
   }

   private void tickCombatAction() {
      if (!this.level().isClientSide() && !this.isActionIdle()) {
         this.actionTick++;
         ShadowIronEntity.Action action = this.getCombatAction();
         if (action == ShadowIronEntity.Action.ATTACK && this.actionTick == 10) {
            LivingEntity target = this.findLiving(this.actionTargetId);
            ShadowIronCombatManager.performCleave(this, target, this.counterAttack);
         }

         if (action == ShadowIronEntity.Action.ROAR && this.actionTick == 12) {
            ShadowIronCombatManager.performRoar(this);
         }

         if (action == ShadowIronEntity.Action.ATTACK && this.actionTick >= 20) {
            this.clearAction();
         } else if (action == ShadowIronEntity.Action.ROAR && this.actionTick >= 35) {
            this.clearAction();
         } else {
            if (action == ShadowIronEntity.Action.BLOCK && this.actionTick >= 25) {
               UUID counterTarget = this.blockConnected ? this.blockedAttackerId : null;
               this.clearAction();
               this.blockConnected = false;
               this.blockedAttackerId = null;
               LivingEntity attacker = this.findLiving(counterTarget);
               if (attacker != null && this.canAttackNow()) {
                  this.beginAttack(attacker, true);
               }
            }
         }
      }
   }

   @Nullable
   private LivingEntity findLiving(@Nullable UUID id) {
      if (id != null && this.level() instanceof ServerLevel serverLevel) {
         return serverLevel.getEntity(id) instanceof LivingEntity living && living.isAlive() ? living : null;
      } else {
         return null;
      }
   }

   public boolean canAttackNow() {
      return this.level().getGameTime() >= this.nextAttackAt;
   }

   public boolean canBlockNow() {
      return this.level().getGameTime() >= this.nextBlockAt;
   }

   public boolean canRoarNow() {
      return this.level().getGameTime() >= this.nextRoarAt;
   }

   public boolean canInterceptNow() {
      return this.level().getGameTime() >= this.nextInterceptAt;
   }

   public void setNextInterceptAt(long gameTick) {
      this.nextInterceptAt = Math.max(this.nextInterceptAt, gameTick);
   }

   public void fortifyFromTaunt(int targets) {
      this.fortifyTargets = Math.max(0, Math.min(8, targets));
      this.fortifyUntil = this.level().getGameTime() + 100L;
   }

   public float fortificationReduction() {
      return this.level().getGameTime() >= this.fortifyUntil ? 0.0F : ShadowIronCombatPolicy.fortificationReduction(this.fortifyTargets);
   }

   public boolean isShieldActive() {
      return this.getCombatAction() != ShadowIronEntity.Action.BLOCK ? false : this.rescueBlock || this.actionTick >= 4 && this.actionTick <= 12;
   }

   public boolean canBlockSource(DamageSource source) {
      if (this.isShieldActive() && source != null && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         Vec3 sourcePosition = source.getSourcePosition();
         if (sourcePosition == null) {
            return false;
         }

         Vec3 towardSource = sourcePosition.subtract(this.position()).multiply(1.0, 0.0, 1.0);
         if (towardSource.lengthSqr() < 1.0E-5) {
            return true;
         }

         Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0, 0.0, 1.0).normalize();
         return forward.dot(towardSource.normalize()) >= 0.35;
      } else {
         return false;
      }
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (!source.is(DamageTypes.FALL) && !source.is(DamageTypes.DROWN)) {
         float adjusted = Math.max(0.0F, amount);
         boolean blocked = this.canBlockSource(source);
         if (blocked) {
            boolean projectile = source.getDirectEntity() instanceof Projectile;
            boolean boss = ShadowIronCombatManager.isBossDamageSource(source);
            adjusted *= 1.0F - ShadowIronCombatPolicy.blockReduction(projectile, boss);
         }

         adjusted *= 1.0F - this.fortificationReduction();
         boolean hurt = super.hurt(source, adjusted);
         if (hurt && blocked && !this.level().isClientSide()) {
            this.blockConnected = true;
            if (source.getEntity() instanceof LivingEntity attacker) {
               this.blockedAttackerId = attacker.getUUID();
            }

            ShadowIronCombatManager.onShieldBlock(this, source);
         }

         return hurt;
      } else {
         return false;
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      this.updateSwingTime();
      if (!this.level().isClientSide()) {
         boolean domainBoosted = this.hasEffect(SololevelingModMobEffects.DOMAIN_BOOST.get());
         if (this.isDomainBoosted() != domainBoosted) {
            this.setDomainBoosted(domainBoosted);
         }

         this.tickCombatAction();
         ShadowIronCombatManager.tickIron(this);
      }
   }

   @Override
   protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
      return 3.05F;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.IRON_GOLEM_DAMAGE;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.IRON_GOLEM_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.38F, 0.72F + this.random.nextFloat() * 0.12F);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   public boolean isFood(ItemStack stack) {
      return false;
   }

   @Nullable
   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      return SololevelingModEntities.SHADOW_IRON.get().create(level);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putLong("IronNextAttack", this.nextAttackAt);
      tag.putLong("IronNextBlock", this.nextBlockAt);
      tag.putLong("IronNextRoar", this.nextRoarAt);
      tag.putLong("IronNextIntercept", this.nextInterceptAt);
      tag.putLong("IronFortifyUntil", this.fortifyUntil);
      tag.putInt("IronFortifyTargets", this.fortifyTargets);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.nextAttackAt = tag.getLong("IronNextAttack");
      this.nextBlockAt = tag.getLong("IronNextBlock");
      this.nextRoarAt = tag.getLong("IronNextRoar");
      this.nextInterceptAt = tag.getLong("IronNextIntercept");
      this.fortifyUntil = tag.getLong("IronFortifyUntil");
      this.fortifyTargets = tag.getInt("IronFortifyTargets");
      this.clearAction();
   }

   private PlayState movementPredicate(AnimationState<ShadowIronEntity> state) {
      if (!this.isActionIdle()) {
         return PlayState.STOP;
      } else {
         return !state.isMoving() && !(Math.abs(state.getLimbSwingAmount()) > 0.15F)
            ? state.setAndContinue(RawAnimation.begin().thenLoop("idle"))
            : state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
      }
   }

   private PlayState actionPredicate(AnimationState<ShadowIronEntity> state) {
      return PlayState.STOP;
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
      AnimationController<ShadowIronEntity> actionController = new AnimationController<>(this, "action", 0, this::actionPredicate);
      actionController.triggerableAnim(ShadowIronEntity.Action.ATTACK.serializedName, RawAnimation.begin().thenPlay("attack"));
      actionController.triggerableAnim(ShadowIronEntity.Action.BLOCK.serializedName, RawAnimation.begin().thenPlay("block"));
      actionController.triggerableAnim(ShadowIronEntity.Action.ROAR.serializedName, RawAnimation.begin().thenPlay("roar"));
      controllers.add(actionController);
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public enum Action {
      NONE("none"),
      ATTACK("attack"),
      BLOCK("block"),
      ROAR("roar");

      private final String serializedName;

      Action(String serializedName) {
         this.serializedName = serializedName;
      }

      private static ShadowIronEntity.Action fromSerializedName(String value) {
         if (value == null) {
            return NONE;
         }

         String normalized = value.trim().toLowerCase(Locale.ROOT);

         for (ShadowIronEntity.Action action : values()) {
            if (action.serializedName.equals(normalized)) {
               return action;
            }
         }

         return NONE;
      }
   }
}
