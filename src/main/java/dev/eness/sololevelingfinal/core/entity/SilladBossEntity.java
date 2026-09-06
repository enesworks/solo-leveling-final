package dev.eness.sololevelingfinal.core.entity;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.SilladBossCombatManager;

public final class SilladBossEntity extends Monster {
   private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(SilladBossEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> ACTION_TICK = SynchedEntityData.defineId(SilladBossEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(SilladBossEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> SPIRITUALIZED = SynchedEntityData.defineId(SilladBossEntity.class, EntityDataSerializers.BOOLEAN);
   private final ServerBossEvent bossInfo = new ServerBossEvent(
      Component.translatable("entity.sololeveling.sillad_boss"), BossBarColor.BLUE, BossBarOverlay.NOTCHED_10
   );
   private int engagedPlayerCount = 1;
   private boolean absoluteZeroUsed;
   private long phaseThreeStartedAt = -1L;
   private BlockPos encounterHome;

   public SilladBossEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.SILLAD_BOSS.get(), level);
   }

   public SilladBossEntity(EntityType<? extends SilladBossEntity> type, Level level) {
      super(type, level);
      this.xpReward = 250;
      this.setMaxUpStep(1.25F);
      this.setPersistenceRequired();
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 0.0F);
      this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, 0.0F);
      this.ensureFrostSpear();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ACTION, SilladBossEntity.Action.IDLE.ordinal());
      this.entityData.define(ACTION_TICK, 0);
      this.entityData.define(PHASE, 1);
      this.entityData.define(SPIRITUALIZED, false);
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
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.4)
         .add(Attributes.MAX_HEALTH, 900.0)
         .add(Attributes.ARMOR, 28.0)
         .add(Attributes.ARMOR_TOUGHNESS, 12.0)
         .add(Attributes.ATTACK_DAMAGE, 24.0)
         .add(Attributes.ATTACK_KNOCKBACK, 1.0)
         .add(Attributes.FOLLOW_RANGE, 64.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
   }

   public static void init() {
   }

   public SilladBossEntity.Action getCombatAction() {
      int index = this.entityData.get(ACTION);
      return index >= 0 && index < SilladBossEntity.Action.values().length ? SilladBossEntity.Action.values()[index] : SilladBossEntity.Action.IDLE;
   }

   public int getActionTick() {
      return this.entityData.get(ACTION_TICK);
   }

   public boolean isActing() {
      return this.getCombatAction() != SilladBossEntity.Action.IDLE;
   }

   public void beginCombatAction(SilladBossEntity.Action action) {
      if (!this.level().isClientSide() && action != null) {
         this.entityData.set(ACTION, action.ordinal());
         this.entityData.set(ACTION_TICK, 0);
         this.getNavigation().stop();
         this.setAggressive(true);
         this.updateBossBarName();
      }
   }

   public int advanceCombatAction() {
      int next = this.getActionTick() + 1;
      this.entityData.set(ACTION_TICK, next);
      return next;
   }

   public void finishCombatAction() {
      this.entityData.set(ACTION, SilladBossEntity.Action.IDLE.ordinal());
      this.entityData.set(ACTION_TICK, 0);
      this.updateBossBarName();
   }

   public int getCombatPhase() {
      return this.entityData.get(PHASE);
   }

   public void setCombatPhase(int phase) {
      int safe = Math.max(1, Math.min(3, phase));
      this.entityData.set(PHASE, safe);
      if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
         this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(safe >= 3 ? 0.46 : (safe == 2 ? 0.43 : 0.4));
      }

      if (safe >= 3) {
         this.entityData.set(SPIRITUALIZED, true);
         if (this.phaseThreeStartedAt < 0L) {
            this.phaseThreeStartedAt = this.level().getGameTime();
         }
      }

      this.updateBossBarName();
   }

   public boolean isSpiritualized() {
      return this.entityData.get(SPIRITUALIZED);
   }

   public int getEngagedPlayerCount() {
      return this.engagedPlayerCount;
   }

   public void lockEngagedPlayerCount(int count) {
      this.engagedPlayerCount = Math.max(this.engagedPlayerCount, Math.max(1, Math.min(4, count)));
   }

   public boolean hasUsedAbsoluteZero() {
      return this.absoluteZeroUsed;
   }

   public void markAbsoluteZeroUsed() {
      this.absoluteZeroUsed = true;
   }

   public long getPhaseThreeStartedAt() {
      return this.phaseThreeStartedAt;
   }

   public BlockPos getEncounterHome() {
      if (this.encounterHome == null) {
         this.encounterHome = this.blockPosition();
      }

      return this.encounterHome;
   }

   public void setEncounterHome(BlockPos home) {
      if (home != null) {
         this.encounterHome = home.immutable();
      }
   }

   @Override
   public boolean canAttack(LivingEntity target) {
      if (target instanceof Player player) {
         return player.isAlive() && !player.isCreative() && !player.isSpectator() && super.canAttack(target);
      } else {
         boolean ownedCompanion = ShadowMonarchManager.getShadowOwnerUUID(target) != null;
         boolean attackingSillad = target instanceof Mob mob && mob.getTarget() == this;
         boolean retaliating = target == this.getLastHurtByMob();
         return (ownedCompanion || attackingSillad || retaliating) && target.isAlive() && super.canAttack(target);
      }
   }

   @Override
   public boolean doHurtTarget(Entity target) {
      return false;
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (source == null) {
         return false;
      }

      if (source.getEntity() != this && source.getDirectEntity() != this && !source.is(DamageTypes.FREEZE)) {
         float adjusted = amount;
         if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            adjusted = SilladBossCombatManager.modifyIncomingDamage(this, source, amount);
         }

         if (adjusted <= 0.0F) {
            return false;
         }

         boolean hurt = super.hurt(source, adjusted);
         if (hurt && !this.level().isClientSide()) {
            SilladBossCombatManager.recordIncomingDamage(this, source, adjusted);
         }

         return hurt;
      } else {
         return false;
      }
   }

   @Override
   public boolean canFreeze() {
      return false;
   }

   @Override
   public boolean canChangeDimensions() {
      return false;
   }

   @Override
   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   @Override
   protected boolean shouldDespawnInPeaceful() {
      return false;
   }

   @Override
   public MobType getMobType() {
      return MobType.UNDEFINED;
   }

   @Override
   protected void customServerAiStep() {
      super.customServerAiStep();
      SilladBossCombatManager.tick(this);
      this.bossInfo.setProgress(Math.max(0.0F, this.getHealth() / Math.max(1.0F, this.getMaxHealth())));
   }

   @Override
   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossInfo.addPlayer(player);
   }

   @Override
   public void stopSeenByPlayer(ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossInfo.removePlayer(player);
   }

   @Override
   public void die(DamageSource source) {
      SilladBossCombatManager.cleanup(this);
      this.bossInfo.removeAllPlayers();
      super.die(source);
   }

   @Override
   public void remove(RemovalReason reason) {
      SilladBossCombatManager.cleanup(this);
      this.bossInfo.removeAllPlayers();
      super.remove(reason);
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.STRAY_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.STRAY_DEATH;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.STRAY_AMBIENT;
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.POWDER_SNOW_STEP, 0.35F, 0.72F + this.random.nextFloat() * 0.12F);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("SilladPhase", this.getCombatPhase());
      tag.putInt("SilladEngagedPlayers", this.engagedPlayerCount);
      tag.putBoolean("SilladAbsoluteZeroUsed", this.absoluteZeroUsed);
      tag.putLong("SilladPhaseThreeStarted", this.phaseThreeStartedAt);
      if (this.encounterHome != null) {
         tag.putLong("SilladEncounterHome", this.encounterHome.asLong());
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setCombatPhase(tag.contains("SilladPhase") ? tag.getInt("SilladPhase") : 1);
      this.engagedPlayerCount = Math.max(1, Math.min(4, tag.getInt("SilladEngagedPlayers")));
      this.absoluteZeroUsed = tag.getBoolean("SilladAbsoluteZeroUsed");
      this.phaseThreeStartedAt = tag.contains("SilladPhaseThreeStarted") ? tag.getLong("SilladPhaseThreeStarted") : -1L;
      if (tag.contains("SilladEncounterHome")) {
         this.encounterHome = BlockPos.of(tag.getLong("SilladEncounterHome"));
      }

      this.finishCombatAction();
      this.ensureFrostSpear();
   }

   private void ensureFrostSpear() {
      if (SololevelingModItems.ICE_SPEAR.isPresent() && !this.getMainHandItem().is(SololevelingModItems.ICE_SPEAR.get())) {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SololevelingModItems.ICE_SPEAR.get()));
      }

      this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
   }

   private void updateBossBarName() {
      if (!this.level().isClientSide()) {
         Component name = this.hasCustomName() ? this.getCustomName() : Component.translatable("entity.sololeveling.sillad_boss");
         if (this.getCombatAction() != SilladBossEntity.Action.IDLE) {
            name = name.copy().append(Component.literal("  |  ")).append(Component.translatable(this.getCombatAction().translationKey));
         }

         this.bossInfo.setName(name);
      }
   }

   @Override
   public void setCustomName(@Nullable Component name) {
      super.setCustomName(name);
      this.updateBossBarName();
   }

   public enum Action {
      IDLE("idle"),
      PHASE_TRANSITION("phase_transition"),
      FROST_CLEAVE("frost_cleave"),
      ICE_SPEAR("ice_spear"),
      FLASH_FREEZE("flash_freeze"),
      FROZEN_PATH("frozen_path"),
      FROST_COUNTER("frost_counter"),
      STILLNESS_DECREE("stillness_decree"),
      SPIRE_CAGE("spire_cage"),
      WHITEOUT_PROCESSION("whiteout_procession"),
      WINTER_REMEMBERS("winter_remembers"),
      CROWN_OF_WINTER("crown_of_winter"),
      ABSOLUTE_ZERO("absolute_zero"),
      GLACIAL_EXECUTION("glacial_execution"),
      FROST_STEP("frost_step");

      private final String serializedName;
      private final String translationKey;

      Action(String serializedName) {
         this.serializedName = serializedName;
         this.translationKey = "entity.sololeveling.sillad_boss.action." + serializedName;
      }

      public String serializedName() {
         return this.serializedName;
      }

      public static SilladBossEntity.Action byName(String value) {
         if (value == null) {
            return IDLE;
         }

         String normalized = value.trim().toLowerCase(Locale.ROOT);

         for (SilladBossEntity.Action action : values()) {
            if (action.serializedName.equals(normalized)) {
               return action;
            }
         }

         return IDLE;
      }
   }
}
