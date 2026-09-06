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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.procedures.KangTaeshikOnEntityTickUpdateProcedure;

public class KangTaeshikEntity extends Monster {
   public static final EntityDataAccessor<Integer> DATA_IA = SynchedEntityData.defineId(KangTaeshikEntity.class, EntityDataSerializers.INT);

   public KangTaeshikEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.KANG_TAESHIK.get(), world);
   }

   public KangTaeshikEntity(EntityType<KangTaeshikEntity> type, Level world) {
      super(type, world);
      this.setMaxUpStep(0.6F);
      this.xpReward = 15;
      this.setNoAi(false);
      this.setPersistenceRequired();
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SololevelingModItems.DAGGER_HEAT_A.get()));
      this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(SololevelingModItems.DAGGER_DUOLITY_A.get()));
      this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(SololevelingModItems.KANG_HAIR_HELMET.get()));
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_IA, 0);
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
      return damagesource.is(DamageTypes.FALL) ? false : super.hurt(damagesource, amount);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putInt("DataIA", this.entityData.get(DATA_IA));
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("DataIA")) {
         this.entityData.set(DATA_IA, compound.getInt("DataIA"));
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      KangTaeshikOnEntityTickUpdateProcedure.execute(this.level(), this);
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.45);
      builder = builder.add(Attributes.MAX_HEALTH, 60.0);
      builder = builder.add(Attributes.ARMOR, 12.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 8.0);
      return builder.add(Attributes.FOLLOW_RANGE, 64.0);
   }
}
