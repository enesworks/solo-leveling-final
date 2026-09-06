package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class DualWieldFlurryEntity extends Entity {
   private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(DualWieldFlurryEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> SEED = SynchedEntityData.defineId(DualWieldFlurryEntity.class, EntityDataSerializers.INT);
   private static final int LIFETIME = 15;

   public DualWieldFlurryEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.DUAL_WIELD_FLURRY.get(), world);
   }

   public DualWieldFlurryEntity(EntityType<? extends DualWieldFlurryEntity> type, Level world) {
      super(type, world);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(YAW, 0.0F);
      this.entityData.define(SEED, 0);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public static void spawn(LevelAccessor world, LivingEntity owner) {
      if (world instanceof ServerLevel level) {
         Vec3 look = owner.getLookAngle();
         DualWieldFlurryEntity flurry = new DualWieldFlurryEntity(SololevelingModEntities.DUAL_WIELD_FLURRY.get(), level);
         flurry.setYaw(owner.getYRot());
         flurry.setSeed(owner.getRandom().nextInt());
         flurry.moveTo(
            owner.getX() + look.x * 4.4, owner.getY() + 1.25 + Mth.clamp(look.y, -0.45, 0.45) * 0.65, owner.getZ() + look.z * 4.4, owner.getYRot(), 0.0F
         );
         level.addFreshEntity(flurry);
      }
   }

   public float getYaw() {
      return this.entityData.get(YAW);
   }

   private void setYaw(float yaw) {
      this.entityData.set(YAW, yaw);
   }

   public int getSeed() {
      return this.entityData.get(SEED);
   }

   private void setSeed(int seed) {
      this.entityData.set(SEED, seed);
   }

   public float getFade(float partialTick) {
      float age = this.tickCount + partialTick;
      return Math.max(0.0F, 1.0F - age / 15.0F);
   }

   @Override
   public void tick() {
      super.tick();
      this.noPhysics = true;
      this.setDeltaMovement(Vec3.ZERO);
      if (this.tickCount >= 15) {
         this.discard();
      }
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag compound) {
      this.setYaw(compound.getFloat("Yaw"));
      this.setSeed(compound.getInt("Seed"));
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag compound) {
      compound.putFloat("Yaw", this.getYaw());
      compound.putInt("Seed", this.getSeed());
   }

   @Override
   public boolean isPickable() {
      return false;
   }
}
