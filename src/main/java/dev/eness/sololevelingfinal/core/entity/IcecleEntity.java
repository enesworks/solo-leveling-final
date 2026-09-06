package dev.eness.sololevelingfinal.core.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.procedures.IcecleOnInitialEntitySpawnProcedure;

public class IcecleEntity extends Monster {
   public IcecleEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.ICECLE.get(), world);
   }

   public IcecleEntity(EntityType<IcecleEntity> type, Level world) {
      super(type, world);
      this.setMaxUpStep(0.6F);
      this.xpReward = 0;
      this.setNoAi(false);
      this.setPersistenceRequired();
      this.refreshDimensions();
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
            return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
         }
      });
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
      this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
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
   public SoundEvent getHurtSound(DamageSource ds) {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
   }

   @Override
   public SoundEvent getDeathSound() {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
   }

   @Override
   public boolean hurt(DamageSource damagesource, float amount) {
      if (damagesource.is(DamageTypes.IN_FIRE)) {
         return false;
      } else if (damagesource.getDirectEntity() instanceof AbstractArrow) {
         return false;
      } else if (damagesource.getDirectEntity() instanceof Player) {
         return false;
      } else if (damagesource.getDirectEntity() instanceof ThrownPotion || damagesource.getDirectEntity() instanceof AreaEffectCloud) {
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
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag
   ) {
      SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);
      IcecleOnInitialEntitySpawnProcedure.execute(world, this.getX(), this.getY(), this.getZ(), this);
      return retval;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected void doPush(Entity entityIn) {
   }

   @Override
   protected void pushEntities() {
   }

   @Override
   public EntityDimensions getDimensions(Pose pose) {
      return super.getDimensions(pose).scale(2.0F);
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
      builder = builder.add(Attributes.MAX_HEALTH, 200.0);
      builder = builder.add(Attributes.ARMOR, 100.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 3.0);
      return builder.add(Attributes.FOLLOW_RANGE, 16.0);
   }
}
