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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class FireMageVfxEntity extends Entity {
   public static final int FLAME_WEAVING = 0;
   public static final int IGNITION_ORB = 1;
   public static final int ORB_IMPACT = 2;
   public static final int INFERNO_LANCE = 3;
   public static final int FLASHFIRE = 4;
   public static final int CREMATION = 5;
   public static final int FURNACE_DOMINION = 6;
   public static final int HEAVENFALL = 7;
   public static final int HEAVENFALL_IMPACT = 8;
   public static final int SCORCH = 9;
   private static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.defineId(FireMageVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(FireMageVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(FireMageVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(FireMageVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(FireMageVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> PRIMARY_COLOR = SynchedEntityData.defineId(FireMageVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SECONDARY_COLOR = SynchedEntityData.defineId(FireMageVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SEED = SynchedEntityData.defineId(FireMageVfxEntity.class, EntityDataSerializers.INT);

   public FireMageVfxEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.FIRE_MAGE_VFX.get(), level);
   }

   public FireMageVfxEntity(EntityType<? extends FireMageVfxEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(STYLE, 0);
      this.entityData.define(STAGE, 1);
      this.entityData.define(SCALE, 1.0F);
      this.entityData.define(LENGTH, 1.0F);
      this.entityData.define(LIFETIME, 12);
      this.entityData.define(PRIMARY_COLOR, 16734730);
      this.entityData.define(SECONDARY_COLOR, 16765770);
      this.entityData.define(SEED, 0);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public static FireMageVfxEntity spawn(
      ServerLevel level, double x, double y, double z, int style, int stage, float scale, float length, int lifetime, float yaw, float pitch
   ) {
      return spawn(level, x, y, z, style, stage, scale, length, lifetime, yaw, pitch, 16734730, 16765770);
   }

   public static FireMageVfxEntity spawn(
      ServerLevel level,
      double x,
      double y,
      double z,
      int style,
      int stage,
      float scale,
      float length,
      int lifetime,
      float yaw,
      float pitch,
      int primaryColor,
      int secondaryColor
   ) {
      FireMageVfxEntity effect = new FireMageVfxEntity(SololevelingModEntities.FIRE_MAGE_VFX.get(), level);
      effect.entityData.set(STYLE, Mth.clamp(style, 0, 9));
      effect.entityData.set(STAGE, Mth.clamp(stage, 1, 5));
      effect.entityData.set(SCALE, Math.max(0.05F, scale));
      effect.entityData.set(LENGTH, Math.max(0.05F, length));
      effect.entityData.set(LIFETIME, Math.max(2, lifetime));
      effect.entityData.set(PRIMARY_COLOR, primaryColor & 16777215);
      effect.entityData.set(SECONDARY_COLOR, secondaryColor & 16777215);
      effect.entityData.set(SEED, level.getRandom().nextInt());
      effect.moveTo(x, y, z, yaw, pitch);
      level.addFreshEntity(effect);
      return effect;
   }

   public int getStyle() {
      return this.entityData.get(STYLE);
   }

   public int getStage() {
      return this.entityData.get(STAGE);
   }

   public float getScale() {
      return this.entityData.get(SCALE);
   }

   public float getLength() {
      return this.entityData.get(LENGTH);
   }

   public int getLifetime() {
      return this.entityData.get(LIFETIME);
   }

   public int getPrimaryColor() {
      return this.entityData.get(PRIMARY_COLOR);
   }

   public int getSecondaryColor() {
      return this.entityData.get(SECONDARY_COLOR);
   }

   public int getSeed() {
      return this.entityData.get(SEED);
   }

   public float getProgress(float partialTick) {
      return Mth.clamp((this.tickCount + partialTick) / Math.max(1.0F, this.getLifetime()), 0.0F, 1.0F);
   }

   public float getFade(float partialTick) {
      float progress = this.getProgress(partialTick);
      float fadeIn = Mth.clamp(progress * 9.0F, 0.0F, 1.0F);
      float fadeOut = Mth.clamp((1.0F - progress) * (this.getStyle() == 6 ? 8.0F : 4.5F), 0.0F, 1.0F);
      return Math.min(fadeIn, fadeOut);
   }

   @Override
   public void tick() {
      super.tick();
      this.noPhysics = true;
      this.setDeltaMovement(0.0, 0.0, 0.0);
      if (this.tickCount >= this.getLifetime()) {
         this.discard();
      }
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(STYLE, tag.getInt("Style"));
      this.entityData.set(STAGE, Mth.clamp(tag.getInt("Stage"), 1, 5));
      this.entityData.set(SCALE, Math.max(0.05F, tag.getFloat("Scale")));
      this.entityData.set(LENGTH, Math.max(0.05F, tag.getFloat("Length")));
      this.entityData.set(LIFETIME, Math.max(2, tag.getInt("Lifetime")));
      this.entityData.set(PRIMARY_COLOR, tag.getInt("PrimaryColor"));
      this.entityData.set(SECONDARY_COLOR, tag.getInt("SecondaryColor"));
      this.entityData.set(SEED, tag.getInt("Seed"));
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putInt("Style", this.getStyle());
      tag.putInt("Stage", this.getStage());
      tag.putFloat("Scale", this.getScale());
      tag.putFloat("Length", this.getLength());
      tag.putInt("Lifetime", this.getLifetime());
      tag.putInt("PrimaryColor", this.getPrimaryColor());
      tag.putInt("SecondaryColor", this.getSecondaryColor());
      tag.putInt("Seed", this.getSeed());
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public AABB getBoundingBoxForCulling() {
      double radius = Math.max(4.0, Math.max(this.getScale(), this.getLength()) * 2.2);
      return new AABB(this.getX() - radius, this.getY() - radius, this.getZ() - radius, this.getX() + radius, this.getY() + radius, this.getZ() + radius);
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double distance) {
      double range = this.getStyle() != 7 && this.getStyle() != 8 ? 256.0 : 512.0;
      return distance < range * range;
   }
}
