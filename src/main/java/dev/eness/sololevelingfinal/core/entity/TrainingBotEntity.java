package dev.eness.sololevelingfinal.core.entity;

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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.procedures.TrainingBotEntityDiesProcedure;
import dev.eness.sololevelingfinal.core.procedures.TrainingBotEntityIsHurtProcedure;
import dev.eness.sololevelingfinal.core.procedures.TrainingBotOnInitialEntitySpawnProcedure;

public class TrainingBotEntity extends Monster {
   public static final EntityDataAccessor<Boolean> DATA_set = SynchedEntityData.defineId(TrainingBotEntity.class, EntityDataSerializers.BOOLEAN);

   public TrainingBotEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.TRAINING_BOT.get(), world);
   }

   public TrainingBotEntity(EntityType<TrainingBotEntity> type, Level world) {
      super(type, world);
      this.setMaxUpStep(0.6F);
      this.xpReward = 0;
      this.setNoAi(false);
      this.setPersistenceRequired();
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_set, false);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.6, false) {
         @Override
         protected double getAttackReachSqr(LivingEntity entity) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
         }
      });
      this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(5, new FloatGoal(this));
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
   public double getMyRidingOffset() {
      return -0.35;
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
   public boolean hurt(DamageSource damagesource, float amount) {
      double x = this.getX();
      double y = this.getY();
      double z = this.getZ();
      Level world = this.level();
      Entity entity = this;
      Entity sourceentity = damagesource.getEntity();
      Entity immediatesourceentity = damagesource.getDirectEntity();
      TrainingBotEntityIsHurtProcedure.execute(world, x, y, z, entity, sourceentity);
      if (damagesource.is(DamageTypes.IN_FIRE)) {
         return false;
      } else if (damagesource.getDirectEntity() instanceof AbstractArrow) {
         return false;
      } else if (damagesource.is(DamageTypes.FALL)) {
         return false;
      } else if (damagesource.is(DamageTypes.CACTUS)) {
         return false;
      } else if (damagesource.is(DamageTypes.DROWN)) {
         return false;
      } else if (damagesource.is(DamageTypes.LIGHTNING_BOLT)) {
         return false;
      } else if (damagesource.is(DamageTypes.EXPLOSION)) {
         return false;
      } else if (damagesource.is(DamageTypes.TRIDENT)) {
         return false;
      } else if (damagesource.is(DamageTypes.FALLING_ANVIL)) {
         return false;
      } else if (damagesource.is(DamageTypes.DRAGON_BREATH)) {
         return false;
      } else if (damagesource.is(DamageTypes.WITHER)) {
         return false;
      } else {
         return damagesource.is(DamageTypes.WITHER_SKULL) ? false : super.hurt(damagesource, amount);
      }
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      TrainingBotEntityDiesProcedure.execute(this.level(), source.getEntity());
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag
   ) {
      SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);
      TrainingBotOnInitialEntitySpawnProcedure.execute(world, this.getX(), this.getY(), this.getZ(), this);
      return retval;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean("Dataset", this.entityData.get(DATA_set));
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Dataset")) {
         this.entityData.set(DATA_set, compound.getBoolean("Dataset"));
      }
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.35);
      builder = builder.add(Attributes.MAX_HEALTH, 20.0);
      builder = builder.add(Attributes.ARMOR, 5.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 5.0);
      return builder.add(Attributes.FOLLOW_RANGE, 256.0);
   }
}
