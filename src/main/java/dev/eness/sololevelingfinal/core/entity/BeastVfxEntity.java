package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class BeastVfxEntity extends Entity {
   public static final int CLAW = 0;
   public static final int SCAR = 1;
   public static final int INTERCEPT = 2;
   public static final int QUARRY = 3;
   public static final int OPENING = 4;
   public static final int RIFT = 5;
   public static final int RUBBLE_JAW = 6;
   public static final int KINGS_MAUL = 7;
   public static final int RECONSTITUTION = 8;
   public static final int WHITE_FANG = 9;
   private static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.defineId(BeastVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> PRIMARY_COLOR = SynchedEntityData.defineId(BeastVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SECONDARY_COLOR = SynchedEntityData.defineId(BeastVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(BeastVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(BeastVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(BeastVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(BeastVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(BeastVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(BeastVfxEntity.class, EntityDataSerializers.INT);

   public BeastVfxEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.BEAST_VFX.get(), level);
   }

   public BeastVfxEntity(EntityType<? extends BeastVfxEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(STYLE, 0);
      this.entityData.define(PRIMARY_COLOR, 15015209);
      this.entityData.define(SECONDARY_COLOR, 2752512);
      this.entityData.define(SCALE, 1.0F);
      this.entityData.define(LENGTH, 1.0F);
      this.entityData.define(ROLL, 0.0F);
      this.entityData.define(LIFETIME, 10);
      this.entityData.define(TARGET_ID, -1);
      this.entityData.define(VARIANT, 0);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public static BeastVfxEntity spawn(
      ServerLevel level,
      Vec3 origin,
      Vec3 direction,
      int style,
      int primaryColor,
      int secondaryColor,
      float scale,
      float length,
      float roll,
      int lifetime,
      int variant
   ) {
      Vec3 normalized = direction.lengthSqr() < 0.001 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
      return spawn(
         level,
         origin.x,
         origin.y,
         origin.z,
         style,
         primaryColor,
         secondaryColor,
         scale,
         length,
         roll,
         lifetime,
         yawFor(normalized),
         pitchFor(normalized),
         -1,
         variant
      );
   }

   public static BeastVfxEntity spawnAttached(
      ServerLevel level, Entity target, int style, int primaryColor, int secondaryColor, float scale, float length, float roll, int lifetime, int variant
   ) {
      return spawn(
         level,
         target.getX(),
         target.getY() + target.getBbHeight() * 0.52,
         target.getZ(),
         style,
         primaryColor,
         secondaryColor,
         scale,
         length,
         roll,
         lifetime,
         target.getYRot(),
         target.getXRot(),
         target.getId(),
         variant
      );
   }

   private static BeastVfxEntity spawn(
      ServerLevel level,
      double x,
      double y,
      double z,
      int style,
      int primaryColor,
      int secondaryColor,
      float scale,
      float length,
      float roll,
      int lifetime,
      float yaw,
      float pitch,
      int targetId,
      int variant
   ) {
      BeastVfxEntity effect = new BeastVfxEntity(SololevelingModEntities.BEAST_VFX.get(), level);
      effect.entityData.set(STYLE, style);
      effect.entityData.set(PRIMARY_COLOR, primaryColor & 16777215);
      effect.entityData.set(SECONDARY_COLOR, secondaryColor & 16777215);
      effect.entityData.set(SCALE, Math.max(0.08F, scale));
      effect.entityData.set(LENGTH, Math.max(0.08F, length));
      effect.entityData.set(ROLL, roll);
      effect.entityData.set(LIFETIME, Math.max(2, lifetime));
      effect.entityData.set(TARGET_ID, targetId);
      effect.entityData.set(VARIANT, variant);
      effect.moveTo(x, y, z, yaw, pitch);
      level.addFreshEntity(effect);
      return effect;
   }

   public int getStyle() {
      return this.entityData.get(STYLE);
   }

   public int getPrimaryColor() {
      return this.entityData.get(PRIMARY_COLOR);
   }

   public int getSecondaryColor() {
      return this.entityData.get(SECONDARY_COLOR);
   }

   public float getScale() {
      return this.entityData.get(SCALE);
   }

   public float getLength() {
      return this.entityData.get(LENGTH);
   }

   public float getRoll() {
      return this.entityData.get(ROLL);
   }

   public int getLifetime() {
      return this.entityData.get(LIFETIME);
   }

   public int getTargetId() {
      return this.entityData.get(TARGET_ID);
   }

   public int getVariant() {
      return this.entityData.get(VARIANT);
   }

   public float getProgress(float partialTick) {
      return Math.min(1.0F, (this.tickCount + partialTick) / Math.max(1.0F, this.getLifetime()));
   }

   public float getFade(float partialTick) {
      float progress = this.getProgress(partialTick);
      float fadeIn = Math.min(1.0F, progress * 9.0F);
      float fadeOut = Math.min(1.0F, (1.0F - progress) * (this.getStyle() == 2 ? 2.6F : 5.0F));
      return Math.max(0.0F, Math.min(fadeIn, fadeOut));
   }

   @Override
   public void tick() {
      super.tick();
      this.noPhysics = true;
      this.setDeltaMovement(Vec3.ZERO);
      if (this.getTargetId() >= 0) {
         Entity target = this.level().getEntity(this.getTargetId());
         if (target == null || !target.isAlive()) {
            this.discard();
            return;
         }

         this.setPos(target.getX(), target.getY() + target.getBbHeight() * 0.52, target.getZ());
         if (this.getStyle() == 2) {
            Vec3 movement = target.getDeltaMovement();
            if (movement.horizontalDistanceSqr() > 0.002) {
               this.setYRot(yawFor(movement.normalize()));
               this.setXRot(pitchFor(movement.normalize()));
            } else {
               this.setYRot(target.getYRot());
               this.setXRot(target.getXRot());
            }
         }
      }

      if (this.tickCount >= this.getLifetime()) {
         this.discard();
      }
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(STYLE, tag.getInt("Style"));
      this.entityData.set(PRIMARY_COLOR, tag.getInt("PrimaryColor"));
      this.entityData.set(SECONDARY_COLOR, tag.getInt("SecondaryColor"));
      this.entityData.set(SCALE, tag.getFloat("Scale"));
      this.entityData.set(LENGTH, tag.getFloat("Length"));
      this.entityData.set(ROLL, tag.getFloat("Roll"));
      this.entityData.set(LIFETIME, tag.getInt("Lifetime"));
      this.entityData.set(TARGET_ID, tag.getInt("TargetId"));
      this.entityData.set(VARIANT, tag.getInt("Variant"));
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putInt("Style", this.getStyle());
      tag.putInt("PrimaryColor", this.getPrimaryColor());
      tag.putInt("SecondaryColor", this.getSecondaryColor());
      tag.putFloat("Scale", this.getScale());
      tag.putFloat("Length", this.getLength());
      tag.putFloat("Roll", this.getRoll());
      tag.putInt("Lifetime", this.getLifetime());
      tag.putInt("TargetId", this.getTargetId());
      tag.putInt("Variant", this.getVariant());
   }

   private static float yawFor(Vec3 direction) {
      return (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
   }

   private static float pitchFor(Vec3 direction) {
      return (float)Math.toDegrees(Math.asin(-Math.max(-1.0, Math.min(1.0, direction.y))));
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public AABB getBoundingBoxForCulling() {
      double radius = Math.max(2.0, Math.max(this.getScale(), this.getLength()) * 2.2);
      return new AABB(this.getX() - radius, this.getY() - radius, this.getZ() - radius, this.getX() + radius, this.getY() + radius, this.getZ() + radius);
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 36864.0;
   }
}
