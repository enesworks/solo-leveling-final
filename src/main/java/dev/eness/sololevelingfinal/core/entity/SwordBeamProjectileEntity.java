package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.AbilityDestructionManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class SwordBeamProjectileEntity extends AbstractArrow implements ItemSupplier {
   public static final ItemStack PROJECTILE_ITEM = new ItemStack(Blocks.AIR);
   private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(SwordBeamProjectileEntity.class, EntityDataSerializers.FLOAT);
   private static final int LIFETIME = 42;
   private double originX;
   private double originY;
   private double originZ;

   public SwordBeamProjectileEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.SWORD_BEAM_PROJECTILE.get(), world);
   }

   public SwordBeamProjectileEntity(EntityType<? extends SwordBeamProjectileEntity> type, Level world) {
      super(type, world);
      this.setNoGravity(true);
   }

   public SwordBeamProjectileEntity(EntityType<? extends SwordBeamProjectileEntity> type, LivingEntity owner, Level world) {
      super(type, owner, world);
      this.setNoGravity(true);
      this.setOrigin(owner.getX(), owner.getEyeY(), owner.getZ());
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ROLL, 0.0F);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ItemStack getItem() {
      return PROJECTILE_ITEM;
   }

   @Override
   protected ItemStack getPickupItem() {
      return PROJECTILE_ITEM;
   }

   public float getFade(float partialTick) {
      float age = this.tickCount + partialTick;
      float in = Math.min(1.0F, age / 3.0F);
      float out = Math.max(0.0F, 1.0F - age / 42.0F);
      return in * out;
   }

   public void setOrigin(double x, double y, double z) {
      this.originX = x;
      this.originY = y;
      this.originZ = z;
   }

   public float getRoll() {
      return this.entityData.get(ROLL);
   }

   public void setRoll(float roll) {
      this.entityData.set(ROLL, roll);
   }

   @Override
   protected void doPostHurtEffects(LivingEntity entity) {
      super.doPostHurtEffects(entity);
      entity.setArrowCount(Math.max(0, entity.getArrowCount() - 1));
   }

   @Override
   protected void onHitEntity(EntityHitResult entityHitResult) {
      super.onHitEntity(entityHitResult);
      if (!this.level().isClientSide()) {
         this.discard();
      }
   }

   @Override
   protected void onHitBlock(BlockHitResult blockHitResult) {
      super.onHitBlock(blockHitResult);
      if (!this.level().isClientSide()) {
         if (this.getOwner() instanceof ServerPlayer owner) {
            Vec3 direction = this.getDeltaMovement().lengthSqr() < 1.0E-6 ? owner.getLookAngle().normalize() : this.getDeltaMovement().normalize();
            Vec3 point = blockHitResult.getLocation();
            AbilityDestructionManager.line(
               owner,
               AbilityDestructionManager.Profile.RANKER_IMPACT,
               point.subtract(direction.scale(1.0)),
               point.add(direction.scale(0.55)),
               TemporaryStatBonusManager.effectiveStrength(owner) + owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * 10.0,
               false
            );
         }

         this.discard();
      }
   }

   @Override
   public void tick() {
      this.setNoGravity(true);
      super.tick();
      if (!this.level().isClientSide()) {
         double rangeSqr = this.distanceToSqr(this.originX, this.originY, this.originZ);
         if (this.tickCount >= 42 || rangeSqr >= 576.0 || this.inGround) {
            this.discard();
         }
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putDouble("OriginX", this.originX);
      compound.putDouble("OriginY", this.originY);
      compound.putDouble("OriginZ", this.originZ);
      compound.putFloat("Roll", this.getRoll());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.originX = compound.getDouble("OriginX");
      this.originY = compound.getDouble("OriginY");
      this.originZ = compound.getDouble("OriginZ");
      this.setRoll(compound.getFloat("Roll"));
   }
}
