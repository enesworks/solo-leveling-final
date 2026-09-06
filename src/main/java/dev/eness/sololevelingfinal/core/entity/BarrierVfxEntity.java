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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.BarrierMageSpellManager;
import dev.eness.sololevelingfinal.core.util.MageCombatHelper;

public class BarrierVfxEntity extends Entity {
   public static final int FRACTURE_BOLT = 0;
   public static final int FRACTURE_MARK = 1;
   public static final int PRISM_RAMPART = 2;
   public static final int SHARD_PLATE = 3;
   public static final int REPULSION_FRAME = 4;
   public static final int SEALING_PRISM = 5;
   public static final int MIRROR_WARD = 6;
   public static final int RESONANT_COLLAPSE = 7;
   public static final int ABSOLUTE_BASTION = 8;
   public static final int IMPACT = 9;
   public static final int RETURN_SHARD = 10;
   public static final int SILLAD_ICE_PRISON = 11;
   private static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> PRIMARY_COLOR = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SECONDARY_COLOR = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SEED = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> ORB_AMPLIFIED = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Optional<UUID>> TARGET = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Float> INTEGRITY = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> MAX_INTEGRITY = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> RESONANCE = SynchedEntityData.defineId(BarrierVfxEntity.class, EntityDataSerializers.FLOAT);

   public BarrierVfxEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.BARRIER_VFX.get(), level);
   }

   public BarrierVfxEntity(EntityType<? extends BarrierVfxEntity> type, Level level) {
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
      this.entityData.define(PRIMARY_COLOR, 6479871);
      this.entityData.define(SECONDARY_COLOR, 15925247);
      this.entityData.define(SEED, 0);
      this.entityData.define(ORB_AMPLIFIED, false);
      this.entityData.define(ACTIVE, false);
      this.entityData.define(OWNER, Optional.empty());
      this.entityData.define(TARGET, Optional.empty());
      this.entityData.define(INTEGRITY, 0.0F);
      this.entityData.define(MAX_INTEGRITY, 0.0F);
      this.entityData.define(RESONANCE, 0.0F);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public static BarrierVfxEntity spawn(
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
      float integrity,
      boolean active
   ) {
      BarrierVfxEntity effect = new BarrierVfxEntity(SololevelingModEntities.BARRIER_VFX.get(), level);
      effect.entityData.set(STYLE, Mth.clamp(style, 0, 11));
      effect.entityData.set(STAGE, Mth.clamp(stage, 1, 5));
      effect.entityData.set(SCALE, Math.max(0.04F, scale));
      effect.entityData.set(LENGTH, Math.max(0.04F, length));
      effect.entityData.set(LIFETIME, Math.max(2, lifetime));
      effect.entityData.set(PRIMARY_COLOR, orbAmplified ? 2377983 : 6479871);
      effect.entityData.set(SECONDARY_COLOR, orbAmplified ? 11736402 : 15925247);
      effect.entityData.set(SEED, level.getRandom().nextInt());
      effect.entityData.set(ORB_AMPLIFIED, orbAmplified);
      effect.entityData.set(ACTIVE, active);
      effect.entityData.set(OWNER, owner == null ? Optional.empty() : Optional.of(owner.getUUID()));
      effect.entityData.set(TARGET, target == null ? Optional.empty() : Optional.of(target.getUUID()));
      effect.entityData.set(INTEGRITY, Math.max(0.0F, integrity));
      effect.entityData.set(MAX_INTEGRITY, Math.max(0.0F, integrity));
      effect.entityData.set(RESONANCE, 0.0F);
      effect.moveTo(position.x, position.y, position.z, yaw, pitch);
      effect.refreshDimensions();
      effect.refreshBarrierBounds();
      level.addFreshEntity(effect);
      return effect;
   }

   @Override
   public void tick() {
      super.tick();
      this.noPhysics = true;
      this.setDeltaMovement(Vec3.ZERO);
      if (!this.level().isClientSide()) {
         this.followLinkedEntity();
         if (this.isBlockingConstruct() && this.isActive()) {
            this.interceptProjectiles();
         }
      }

      this.refreshBarrierBounds();
      if (this.tickCount >= this.getLifetime()) {
         this.discard();
      }
   }

   private void followLinkedEntity() {
      if (this.level() instanceof ServerLevel serverLevel) {
         Entity var5 = null;
         if (this.getStyle() == 6) {
            var5 = this.getOwnerEntity(serverLevel);
         } else if (this.getStyle() == 1 || this.getStyle() == 5 || this.getStyle() == 11) {
            var5 = this.getTargetEntity(serverLevel);
         }

         if (var5 != null) {
            if (!var5.isAlive()) {
               this.discard();
            } else {
               double y = this.getStyle() == 1 ? var5.getY() + var5.getBbHeight() * 0.58 : var5.getY() + 0.04;
               this.setPos(var5.getX(), y, var5.getZ());
               if (this.getStyle() == 6) {
                  this.setYRot(var5.getYRot());
               }
            }
         }
      }
   }

   private void interceptProjectiles() {
      if (this.level() instanceof ServerLevel serverLevel) {
         Entity var10 = this.getOwnerEntity(serverLevel);
         AABB search = this.getBoundingBoxForCulling().inflate(1.5);

         for (Projectile projectile : serverLevel.getEntitiesOfClass(Projectile.class, search, candidate -> candidate.isAlive())) {
            Entity source = projectile.getOwner();
            if (source == null || var10 == null || !MageCombatHelper.areAllied(var10, source)) {
               Vec3 previous = new Vec3(projectile.xo, projectile.yo, projectile.zo);
               if (this.intersectsSegment(previous, projectile.position())) {
                  float pressure = (float)Mth.clamp(4.0 + projectile.getDeltaMovement().length() * 5.0, 4.0, 24.0);
                  Vec3 impact = projectile.position();
                  projectile.discard();
                  this.absorbDamage(pressure);
                  if (this.isOrbAmplified() && this.getStyle() == 8) {
                     this.repairFromProjectile(pressure * 0.35F);
                  }

                  BarrierMageSpellManager.onProjectileBlocked(this, impact);
                  if (!this.isAlive()) {
                     break;
                  }
               }
            }
         }
      }
   }

   public boolean intersectsSegment(Vec3 from, Vec3 to) {
      if (this.getStyle() == 8) {
         boolean fromInside = this.containsInBastion(from);
         boolean toInside = this.containsInBastion(to);
         return fromInside != toInside;
      }

      if (this.getStyle() != 2 && this.getStyle() != 3) {
         return false;
      }

      Vec3 origin = this.position();
      Vec3 normal = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0, 0.0, 1.0).normalize();
      Vec3 right = new Vec3(-normal.z, 0.0, normal.x);
      double fromDistance = from.subtract(origin).dot(normal);
      double toDistance = to.subtract(origin).dot(normal);
      if (!(fromDistance * toDistance > 0.0) && !(Math.abs(fromDistance - toDistance) < 1.0E-5)) {
         double t = fromDistance / (fromDistance - toDistance);
         if (!(t < 0.0) && !(t > 1.0)) {
            Vec3 hit = from.add(to.subtract(from).scale(t));
            Vec3 local = hit.subtract(origin);
            return Math.abs(local.dot(right)) <= this.getScale() && local.y >= -0.2 && local.y <= this.getLength() + 0.2;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean containsInBastion(Vec3 point) {
      if (this.getStyle() != 8) {
         return false;
      }

      Vec3 local = point.subtract(this.position());
      double horizontal = local.x * local.x + local.z * local.z;
      return horizontal <= this.getScale() * this.getScale() && local.y >= -1.0 && local.y <= this.getLength();
   }

   public float absorbDamage(float amount) {
      if (this.isActive() && !(amount <= 0.0F) && !(this.getIntegrity() <= 0.0F)) {
         float accepted = this.getStyle() == 11 ? Math.min(amount, 16.0F) : amount;
         float blocked = Math.min(accepted, this.getIntegrity());
         this.setIntegrity(this.getIntegrity() - blocked);
         this.setResonance(Math.min(this.getMaxIntegrity() * 1.5F, this.getResonance() + blocked * 0.35F));
         if (this.getIntegrity() <= 0.001F) {
            this.breakConstruct();
         }

         return blocked;
      } else {
         return 0.0F;
      }
   }

   private void repairFromProjectile(float amount) {
      float repairLimit = this.getMaxIntegrity() * 0.1F;
      float repaired = this.getPersistentData().getFloat("OrbRepair");
      float accepted = Math.min(Math.max(0.0F, amount), Math.max(0.0F, repairLimit - repaired));
      if (!(accepted <= 0.0F)) {
         this.getPersistentData().putFloat("OrbRepair", repaired + accepted);
         this.setIntegrity(Math.min(this.getMaxIntegrity(), this.getIntegrity() + accepted));
      }
   }

   public void breakConstruct() {
      if (this.isActive()) {
         this.entityData.set(ACTIVE, false);
         if (!this.level().isClientSide()) {
            BarrierMageSpellManager.onConstructBroken(this);
         }

         this.discard();
      }
   }

   public void dissolve() {
      this.entityData.set(ACTIVE, false);
      this.discard();
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (!this.isGameplayConstruct() || !this.isActive() || amount <= 0.0F) {
         return false;
      }

      if (this.level().isClientSide()) {
         return true;
      }

      if (this.level() instanceof ServerLevel serverLevel) {
         Entity owner = this.getOwnerEntity(serverLevel);
         Entity attacker = source.getEntity();
         if (owner != null && attacker != null && MageCombatHelper.areAllied(owner, attacker)) {
            return false;
         }
      }

      this.absorbDamage(amount);
      return true;
   }

   public Entity getOwnerEntity(ServerLevel level) {
      return this.getOwnerId().map(level::getEntity).orElse(null);
   }

   public Entity getTargetEntity(ServerLevel level) {
      return this.getTargetId().map(level::getEntity).orElse(null);
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

   public boolean isActive() {
      return this.entityData.get(ACTIVE);
   }

   public Optional<UUID> getOwnerId() {
      return this.entityData.get(OWNER);
   }

   public Optional<UUID> getTargetId() {
      return this.entityData.get(TARGET);
   }

   public float getIntegrity() {
      return this.entityData.get(INTEGRITY);
   }

   public float getMaxIntegrity() {
      return this.entityData.get(MAX_INTEGRITY);
   }

   public float getResonance() {
      return this.entityData.get(RESONANCE);
   }

   public void setIntegrity(float value) {
      this.entityData.set(INTEGRITY, Math.max(0.0F, value));
   }

   public void setResonance(float value) {
      this.entityData.set(RESONANCE, Math.max(0.0F, value));
   }

   public float getProgress(float partialTick) {
      return Mth.clamp((this.tickCount + partialTick) / Math.max(1.0F, this.getLifetime()), 0.0F, 1.0F);
   }

   public float getFade(float partialTick) {
      float progress = this.getProgress(partialTick);
      float fadeIn = Mth.clamp(progress * 10.0F, 0.0F, 1.0F);
      float fadeOut = Mth.clamp((1.0F - progress) * (this.isGameplayConstruct() ? 10.0F : 4.5F), 0.0F, 1.0F);
      return Math.min(fadeIn, fadeOut);
   }

   public boolean isBlockingConstruct() {
      return this.getStyle() == 2 || this.getStyle() == 3 || this.getStyle() == 8;
   }

   public boolean countsTowardConstructLimit() {
      return this.getStyle() == 2 || this.getStyle() == 8;
   }

   public boolean isGameplayConstruct() {
      return this.isBlockingConstruct() || this.getStyle() == 5 || this.getStyle() == 6 || this.getStyle() == 11;
   }

   @Override
   public boolean isPickable() {
      return this.isGameplayConstruct() && this.getStyle() != 8 && this.isActive();
   }

   @Override
   public EntityDimensions getDimensions(Pose pose) {
      return switch (this.getStyle()) {
         case 2, 3 -> EntityDimensions.scalable(Math.max(0.25F, this.getScale() * 2.0F), Math.max(0.25F, this.getLength()));
         default -> super.getDimensions(pose);
         case 5, 11 -> EntityDimensions.scalable(Math.max(0.25F, this.getScale() * 2.15F), Math.max(0.25F, this.getLength()));
         case 6 -> EntityDimensions.scalable(Math.max(0.25F, this.getScale() * 1.8F), Math.max(0.25F, this.getLength()));
      };
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (STYLE.equals(key) || SCALE.equals(key) || LENGTH.equals(key)) {
         this.refreshDimensions();
      }

      this.refreshBarrierBounds();
   }

   private void refreshBarrierBounds() {
      if (this.getStyle() == 2 || this.getStyle() == 3) {
         Vec3 normal = Vec3.directionFromRotation(0.0F, this.getYRot()).multiply(1.0, 0.0, 1.0).normalize();
         Vec3 right = new Vec3(-normal.z, 0.0, normal.x);
         double extentX = Math.abs(right.x) * this.getScale() + Math.abs(normal.x) * 0.14;
         double extentZ = Math.abs(right.z) * this.getScale() + Math.abs(normal.z) * 0.14;
         this.setBoundingBox(
            new AABB(
               this.getX() - extentX, this.getY() - 0.05, this.getZ() - extentZ, this.getX() + extentX, this.getY() + this.getLength(), this.getZ() + extentZ
            )
         );
      }
   }

   @Override
   public AABB getBoundingBoxForCulling() {
      double radius = Math.max(2.0, this.getScale() * 1.35);
      double below = this.getStyle() == 6 ? this.getLength() * 0.5 : 1.0;
      return new AABB(
         this.getX() - radius, this.getY() - below, this.getZ() - radius, this.getX() + radius, this.getY() + this.getLength() + 1.0, this.getZ() + radius
      );
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double distance) {
      double range = this.getStyle() != 8 && this.getStyle() != 7 ? 192.0 : 384.0;
      return distance < range * range;
   }

   @Override
   public boolean shouldBeSaved() {
      return this.getStyle() != 11 && super.shouldBeSaved();
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(STYLE, Mth.clamp(tag.getInt("Style"), 0, 11));
      this.entityData.set(STAGE, Mth.clamp(tag.getInt("Stage"), 1, 5));
      this.entityData.set(SCALE, Math.max(0.04F, tag.getFloat("Scale")));
      this.entityData.set(LENGTH, Math.max(0.04F, tag.getFloat("Length")));
      this.entityData.set(LIFETIME, Math.max(2, tag.getInt("Lifetime")));
      this.entityData.set(PRIMARY_COLOR, tag.getInt("PrimaryColor"));
      this.entityData.set(SECONDARY_COLOR, tag.getInt("SecondaryColor"));
      this.entityData.set(SEED, tag.getInt("Seed"));
      this.entityData.set(ORB_AMPLIFIED, tag.getBoolean("OrbAmplified"));
      this.entityData.set(ACTIVE, tag.getBoolean("Active"));
      this.entityData.set(OWNER, tag.hasUUID("Owner") ? Optional.of(tag.getUUID("Owner")) : Optional.empty());
      this.entityData.set(TARGET, tag.hasUUID("Target") ? Optional.of(tag.getUUID("Target")) : Optional.empty());
      this.entityData.set(INTEGRITY, Math.max(0.0F, tag.getFloat("Integrity")));
      this.entityData.set(MAX_INTEGRITY, Math.max(0.0F, tag.getFloat("MaxIntegrity")));
      this.entityData.set(RESONANCE, Math.max(0.0F, tag.getFloat("Resonance")));
      this.refreshDimensions();
      this.refreshBarrierBounds();
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
      tag.putBoolean("Active", this.isActive());
      this.getOwnerId().ifPresent(uuid -> tag.putUUID("Owner", uuid));
      this.getTargetId().ifPresent(uuid -> tag.putUUID("Target", uuid));
      tag.putFloat("Integrity", this.getIntegrity());
      tag.putFloat("MaxIntegrity", this.getMaxIntegrity());
      tag.putFloat("Resonance", this.getResonance());
   }
}
