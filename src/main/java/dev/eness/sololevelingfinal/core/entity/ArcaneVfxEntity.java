package dev.eness.sololevelingfinal.core.entity;

import java.util.Optional;
import java.util.UUID;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class ArcaneVfxEntity extends Entity {
   public static final int AETHER_BOLT = 0;
   public static final int AETHER_IMPACT = 1;
   public static final int VECTOR_TRAIL = 2;
   public static final int VECTOR_ANCHOR = 3;
   public static final int POLARITY_SPHERE = 4;
   public static final int RUNIC_RELAY = 5;
   public static final int RELAY_BEAM = 6;
   public static final int ASTRAL_ARSENAL = 7;
   public static final int ASTRAL_BLADE = 8;
   public static final int DIMENSIONAL_REND = 9;
   public static final int SPATIAL_SCAR = 10;
   public static final int CONVERGENCE_GROUND = 11;
   public static final int CONVERGENCE_SKY = 12;
   public static final int CONVERGENCE_TETHER = 13;
   public static final int ZERO_POINT = 14;
   public static final int FORMULA_RUNES = 15;
   private static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> PRIMARY_COLOR = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SECONDARY_COLOR = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SEED = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> ORB_AMPLIFIED = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> OVERCAST = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Optional<UUID>> TARGET = SynchedEntityData.defineId(ArcaneVfxEntity.class, EntityDataSerializers.OPTIONAL_UUID);

   public ArcaneVfxEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.ARCANE_VFX.get(), level);
   }

   public ArcaneVfxEntity(EntityType<? extends ArcaneVfxEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(STYLE, 0);
      this.entityData.define(STAGE, 1);
      this.entityData.define(SCALE, 1.0F);
      this.entityData.define(LENGTH, 1.0F);
      this.entityData.define(LIFETIME, 20);
      this.entityData.define(PRIMARY_COLOR, 9067775);
      this.entityData.define(SECONDARY_COLOR, 4712191);
      this.entityData.define(SEED, 0);
      this.entityData.define(ORB_AMPLIFIED, false);
      this.entityData.define(OVERCAST, false);
      this.entityData.define(OWNER, Optional.empty());
      this.entityData.define(TARGET, Optional.empty());
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public static ArcaneVfxEntity spawn(
      ServerLevel level,
      Vec3 position,
      int style,
      int stage,
      float scale,
      float length,
      int lifetime,
      float yaw,
      float pitch,
      Entity owner,
      Entity target,
      boolean orbAmplified,
      boolean overcast
   ) {
      ArcaneVfxEntity effect = new ArcaneVfxEntity(SololevelingModEntities.ARCANE_VFX.get(), level);
      effect.entityData.set(STYLE, Mth.clamp(style, 0, 15));
      effect.entityData.set(STAGE, Mth.clamp(stage, 1, 5));
      effect.entityData.set(SCALE, Math.max(0.04F, scale));
      effect.entityData.set(LENGTH, Math.max(0.04F, length));
      effect.entityData.set(LIFETIME, Math.max(2, lifetime));
      effect.entityData.set(PRIMARY_COLOR, orbAmplified ? 2118911 : 9067775);
      effect.entityData.set(SECONDARY_COLOR, orbAmplified ? 13836117 : 4712191);
      effect.entityData.set(SEED, level.getRandom().nextInt());
      effect.entityData.set(ORB_AMPLIFIED, orbAmplified);
      effect.entityData.set(OVERCAST, overcast);
      effect.entityData.set(OWNER, owner == null ? Optional.empty() : Optional.of(owner.getUUID()));
      effect.entityData.set(TARGET, target == null ? Optional.empty() : Optional.of(target.getUUID()));
      effect.moveTo(position.x, position.y, position.z, yaw, pitch);
      level.addFreshEntity(effect);
      return effect;
   }

   @Override
   public void tick() {
      super.tick();
      this.noPhysics = true;
      this.setDeltaMovement(Vec3.ZERO);
      if (!this.level().isClientSide()) {
         this.followOwner();
      }

      if (this.tickCount >= this.getLifetime()) {
         this.discard();
      }
   }

   private void followOwner() {
      if (this.level() instanceof ServerLevel serverLevel && (this.getStyle() == 15 || this.getStyle() == 7)) {
         Entity owner = this.getOwnerEntity(serverLevel);
         if (owner != null && owner.isAlive()) {
            this.setPos(owner.getX(), owner.getY() + owner.getBbHeight() * 0.52, owner.getZ());
            this.setYRot(owner.getYRot());
         } else {
            this.discard();
         }
      }
   }

   public void setVisualPose(Vec3 position, float yaw, float pitch) {
      this.setPos(position.x, position.y, position.z);
      this.setYRot(yaw);
      this.setXRot(pitch);
   }

   public void setScale(float scale) {
      this.entityData.set(SCALE, Math.max(0.04F, scale));
   }

   public Entity getOwnerEntity(ServerLevel level) {
      return this.getOwnerId().map(level::getEntity).orElse(null);
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

   public boolean isOrbAmplified() {
      return this.entityData.get(ORB_AMPLIFIED);
   }

   public boolean isOvercast() {
      return this.entityData.get(OVERCAST);
   }

   public Optional<UUID> getOwnerId() {
      return this.entityData.get(OWNER);
   }

   public Optional<UUID> getTargetId() {
      return this.entityData.get(TARGET);
   }

   public float getProgress(float partialTick) {
      return Mth.clamp((this.tickCount + partialTick) / Math.max(1.0F, this.getLifetime()), 0.0F, 1.0F);
   }

   public float getFade(float partialTick) {
      float progress = this.getProgress(partialTick);
      float fadeIn = Mth.clamp(progress * 9.0F, 0.0F, 1.0F);
      float fadeOut = Mth.clamp((1.0F - progress) * 5.0F, 0.0F, 1.0F);
      return Math.min(fadeIn, fadeOut);
   }

   @Override
   public AABB getBoundingBoxForCulling() {
      double radius = Math.max(3.0, Math.max(this.getScale(), this.getLength()) * 1.55);
      return new AABB(this.getX() - radius, this.getY() - radius, this.getZ() - radius, this.getX() + radius, this.getY() + radius, this.getZ() + radius);
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double distance) {
      double range = this.getStyle() != 11 && this.getStyle() != 12 && this.getStyle() != 9 ? 224.0 : 384.0;
      return distance < range * range;
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(STYLE, Mth.clamp(tag.getInt("Style"), 0, 15));
      this.entityData.set(STAGE, Mth.clamp(tag.getInt("Stage"), 1, 5));
      this.entityData.set(SCALE, Math.max(0.04F, tag.getFloat("Scale")));
      this.entityData.set(LENGTH, Math.max(0.04F, tag.getFloat("Length")));
      this.entityData.set(LIFETIME, Math.max(2, tag.getInt("Lifetime")));
      this.entityData.set(PRIMARY_COLOR, tag.getInt("PrimaryColor"));
      this.entityData.set(SECONDARY_COLOR, tag.getInt("SecondaryColor"));
      this.entityData.set(SEED, tag.getInt("Seed"));
      this.entityData.set(ORB_AMPLIFIED, tag.getBoolean("OrbAmplified"));
      this.entityData.set(OVERCAST, tag.getBoolean("Overcast"));
      this.entityData.set(OWNER, tag.hasUUID("Owner") ? Optional.of(tag.getUUID("Owner")) : Optional.empty());
      this.entityData.set(TARGET, tag.hasUUID("Target") ? Optional.of(tag.getUUID("Target")) : Optional.empty());
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
      tag.putBoolean("OrbAmplified", this.isOrbAmplified());
      tag.putBoolean("Overcast", this.isOvercast());
      this.getOwnerId().ifPresent(id -> tag.putUUID("Owner", id));
      this.getTargetId().ifPresent(id -> tag.putUUID("Target", id));
   }
}
