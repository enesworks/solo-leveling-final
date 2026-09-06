package dev.eness.sololevelingfinal.core.entity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.AbilityDestructionManager;
import dev.eness.sololevelingfinal.core.util.LiuZhigangCombatManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class LiuSwordBeamEntity extends Entity {
   private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Integer> TIER = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DUAL = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> PRIMARY_COLOR = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SECONDARY_COLOR = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> MAX_RANGE = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DIRECTION_X = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DIRECTION_Y = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DIRECTION_Z = SynchedEntityData.defineId(LiuSwordBeamEntity.class, EntityDataSerializers.FLOAT);
   private final Set<UUID> touched = new HashSet<>();
   private float travelled;
   private boolean finishing;
   private boolean executionRegistered;

   public LiuSwordBeamEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.LIU_SWORD_BEAM.get(), level);
   }

   public LiuSwordBeamEntity(EntityType<? extends LiuSwordBeamEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(OWNER, Optional.empty());
      this.entityData.define(TIER, 0);
      this.entityData.define(DUAL, false);
      this.entityData.define(PRIMARY_COLOR, 16765774);
      this.entityData.define(SECONDARY_COLOR, 16765774);
      this.entityData.define(WIDTH, 4.0F);
      this.entityData.define(MAX_RANGE, 20.0F);
      this.entityData.define(SPEED, 3.2F);
      this.entityData.define(DAMAGE, 10.0F);
      this.entityData.define(DIRECTION_X, 0.0F);
      this.entityData.define(DIRECTION_Y, 0.0F);
      this.entityData.define(DIRECTION_Z, 1.0F);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public static LiuSwordBeamEntity spawn(
      ServerLevel level,
      ServerPlayer owner,
      Vec3 origin,
      Vec3 direction,
      int tier,
      boolean dual,
      int primaryColor,
      int secondaryColor,
      float width,
      float range,
      float speed,
      float damage
   ) {
      LiuSwordBeamEntity beam = new LiuSwordBeamEntity(SololevelingModEntities.LIU_SWORD_BEAM.get(), level);
      beam.entityData.set(OWNER, Optional.of(owner.getUUID()));
      beam.entityData.set(TIER, Mth.clamp(tier, 0, 3));
      beam.entityData.set(DUAL, dual);
      beam.entityData.set(PRIMARY_COLOR, primaryColor & 16777215);
      beam.entityData.set(SECONDARY_COLOR, secondaryColor & 16777215);
      beam.entityData.set(WIDTH, Math.max(1.0F, width));
      beam.entityData.set(MAX_RANGE, Math.max(2.0F, range));
      beam.entityData.set(SPEED, Math.max(0.2F, speed));
      beam.entityData.set(DAMAGE, Math.max(0.5F, damage));
      Vec3 normalized = direction.lengthSqr() < 0.001 ? owner.getLookAngle().normalize() : direction.normalize();
      beam.entityData.set(DIRECTION_X, (float)normalized.x);
      beam.entityData.set(DIRECTION_Y, (float)normalized.y);
      beam.entityData.set(DIRECTION_Z, (float)normalized.z);
      beam.setDeltaMovement(normalized.scale(speed));
      beam.setRot(yawFor(normalized), pitchFor(normalized));
      beam.moveTo(origin.x, origin.y, origin.z, beam.getYRot(), beam.getXRot());
      level.addFreshEntity(beam);
      return beam;
   }

   public int getTier() {
      return this.entityData.get(TIER);
   }

   public boolean isDual() {
      return this.entityData.get(DUAL);
   }

   public int getPrimaryColor() {
      return this.entityData.get(PRIMARY_COLOR);
   }

   public int getSecondaryColor() {
      return this.entityData.get(SECONDARY_COLOR);
   }

   public float getBeamWidth() {
      return this.entityData.get(WIDTH);
   }

   public float getMaxRange() {
      return this.entityData.get(MAX_RANGE);
   }

   public float getBeamSpeed() {
      return this.entityData.get(SPEED);
   }

   public float getDamage() {
      return this.entityData.get(DAMAGE);
   }

   public Vec3 getBeamDirection() {
      Vec3 direction = new Vec3(
         this.entityData.get(DIRECTION_X).floatValue(), this.entityData.get(DIRECTION_Y).floatValue(), this.entityData.get(DIRECTION_Z).floatValue()
      );
      return direction.lengthSqr() < 0.001 ? Vec3.directionFromRotation(this.getXRot(), this.getYRot()) : direction.normalize();
   }

   public boolean isExecutionBeam() {
      return this.getTier() >= 3;
   }

   public float getVisualScale() {
      return this.isExecutionBeam() ? 2.2F : 1.0F;
   }

   public int getTravelDurationTicks() {
      return Math.max(1, Mth.ceil(this.getMaxRange() / Math.max(0.2F, this.getBeamSpeed())));
   }

   public float getRenderFade(float partialTick) {
      float progress = Mth.clamp((this.tickCount + partialTick) / this.getTravelDurationTicks(), 0.0F, 1.0F);
      return 1.0F - progress;
   }

   @Override
   public void tick() {
      super.tick();
      this.noPhysics = true;
      if (this.level().isClientSide()) {
         Vec3 motion = this.getBeamDirection().scale(this.getBeamSpeed());
         this.setDeltaMovement(motion);
         this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
      } else if (!(this.level() instanceof ServerLevel level)) {
         this.finish(null);
      } else {
         ServerPlayer owner = this.getOwner(level);
         if (owner != null && owner.isAlive()) {
            if (this.tickCount > this.getTravelDurationTicks() + 4) {
               this.finish(owner);
            } else {
               Vec3 direction = this.getBeamDirection();
               if (direction.lengthSqr() < 0.001) {
                  this.finish(owner);
               } else {
                  direction = direction.normalize();
                  float remaining = this.getMaxRange() - this.travelled;
                  if (remaining <= 0.01F) {
                     this.finish(owner);
                  } else {
                     double step = Math.min(this.getBeamSpeed(), remaining);
                     Vec3 start = this.position();
                     Vec3 intendedEnd = start.add(direction.scale(step));
                     if (!level.hasChunkAt(BlockPos.containing(intendedEnd))) {
                        this.finish(owner);
                     } else {
                        Vec3 end = intendedEnd;
                        boolean blocked = false;
                        if (!this.isExecutionBeam()) {
                           BlockHitResult blockHit = level.clip(new ClipContext(start, intendedEnd, Block.COLLIDER, Fluid.NONE, this));
                           if (blockHit.getType() == Type.BLOCK) {
                              end = blockHit.getLocation();
                              blocked = true;
                           }
                        }

                        this.processTargets(level, owner, start, end);
                        if (blocked) {
                           AbilityDestructionManager.line(
                              owner,
                              AbilityDestructionManager.Profile.LIU_SWORD_CUT,
                              end.subtract(direction.scale(1.4)),
                              end.add(direction.scale(0.6)),
                              TemporaryStatBonusManager.effectiveStrength(owner),
                              this.getTier() >= 2 || this.isDual()
                           );
                        }

                        this.setPos(end.x, end.y, end.z);
                        this.setRot(yawFor(direction), pitchFor(direction));
                        this.setDeltaMovement(direction.scale(this.getBeamSpeed()));
                        this.travelled = this.travelled + (float)start.distanceTo(end);
                        this.hasImpulse = true;
                        if (blocked || this.travelled + 0.01F >= this.getMaxRange()) {
                           this.finish(owner);
                        }
                     }
                  }
               }
            }
         } else {
            this.finish(owner);
         }
      }
   }

   private void processTargets(ServerLevel level, ServerPlayer owner, Vec3 start, Vec3 end) {
      double halfWidth = this.getBeamWidth() * (this.isExecutionBeam() ? 1.3 : 1.18);
      double halfHeight = Math.max(1.5, this.getBeamWidth() * (this.isExecutionBeam() ? (this.isDual() ? 0.95 : 0.78) : (this.isDual() ? 0.78 : 0.58)));
      AABB swept = new AABB(start, end).inflate(halfWidth + 4.0, halfHeight + 4.0, halfWidth + 4.0);

      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, swept, candidate -> LiuZhigangCombatManager.isValidTarget(owner, candidate))) {
         UUID targetId = target.getUUID();
         if (!this.touched.contains(targetId) && this.intersectsSlashVolume(target, start, end, halfWidth, halfHeight)) {
            this.touched.add(targetId);
            if (this.isExecutionBeam()) {
               LiuSwordVfxEntity marker = LiuSwordVfxEntity.spawnAttached(
                  level,
                  target,
                  6,
                  this.getPrimaryColor(),
                  this.getSecondaryColor(),
                  Math.max(1.2F, target.getBbWidth() * 1.45F),
                  Math.max(1.8F, target.getBbHeight() * 1.1F),
                  0.0F,
                  120,
                  this.isDual()
               );
               boolean registered = LiuZhigangCombatManager.registerExecutionTarget(
                  owner,
                  this.getUUID(),
                  target,
                  marker.getUUID(),
                  this.getDamage(),
                  this.getPrimaryColor(),
                  this.getSecondaryColor(),
                  this.isDual(),
                  !this.executionRegistered
               );
               if (registered) {
                  this.executionRegistered = true;
               } else {
                  marker.discard();
               }
            } else {
               LiuZhigangCombatManager.hitBySwordBeam(owner, target, this.getDamage(), this.getTier());
            }
         }
      }
   }

   private boolean intersectsSlashVolume(LivingEntity target, Vec3 start, Vec3 end, double halfWidth, double halfHeight) {
      Vec3 segment = end.subtract(start);
      double lengthSq = segment.lengthSqr();
      Vec3 forward = lengthSq < 1.0E-6 ? this.getBeamDirection() : segment.scale(1.0 / Math.sqrt(lengthSq));
      Vec3 referenceUp = Math.abs(forward.y) > 0.96 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 right = forward.cross(referenceUp).normalize();
      Vec3 up = right.cross(forward).normalize();
      AABB bounds = target.getBoundingBox();
      Vec3 center = bounds.getCenter();
      double t = lengthSq < 1.0E-6 ? 0.0 : Mth.clamp(center.subtract(start).dot(segment) / lengthSq, 0.0, 1.0);
      Vec3 nearest = start.add(segment.scale(t));
      Vec3 offset = center.subtract(nearest);
      double padding = this.isExecutionBeam() ? 1.5 : 0.7;
      double forwardAllowance = projectedHalfExtent(bounds, forward) + 0.75;
      double widthAllowance = halfWidth + projectedHalfExtent(bounds, right) + padding;
      double heightAllowance = halfHeight + projectedHalfExtent(bounds, up) + padding;
      return Math.abs(offset.dot(forward)) <= forwardAllowance && Math.abs(offset.dot(right)) <= widthAllowance && Math.abs(offset.dot(up)) <= heightAllowance;
   }

   private static double projectedHalfExtent(AABB bounds, Vec3 axis) {
      return Math.abs(axis.x) * bounds.getXsize() * 0.5 + Math.abs(axis.y) * bounds.getYsize() * 0.5 + Math.abs(axis.z) * bounds.getZsize() * 0.5;
   }

   private void finish(ServerPlayer owner) {
      if (!this.finishing) {
         this.finishing = true;
         this.discard();
      }
   }

   private ServerPlayer getOwner(ServerLevel level) {
      return this.entityData.get(OWNER).map(level::getPlayerByUUID).filter(ServerPlayer.class::isInstance).map(ServerPlayer.class::cast).orElse(null);
   }

   private static float yawFor(Vec3 direction) {
      return (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
   }

   private static float pitchFor(Vec3 direction) {
      return (float)Math.toDegrees(Math.asin(-Mth.clamp(direction.y, -1.0, 1.0)));
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      if (tag.hasUUID("Owner")) {
         this.entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
      }

      this.entityData.set(TIER, tag.getInt("Tier"));
      this.entityData.set(DUAL, tag.getBoolean("Dual"));
      this.entityData.set(PRIMARY_COLOR, tag.getInt("PrimaryColor"));
      this.entityData.set(SECONDARY_COLOR, tag.getInt("SecondaryColor"));
      this.entityData.set(WIDTH, tag.getFloat("Width"));
      this.entityData.set(MAX_RANGE, tag.getFloat("MaxRange"));
      this.entityData.set(SPEED, tag.getFloat("Speed"));
      this.entityData.set(DAMAGE, tag.getFloat("Damage"));
      if (tag.contains("DirectionX")) {
         this.entityData.set(DIRECTION_X, tag.getFloat("DirectionX"));
         this.entityData.set(DIRECTION_Y, tag.getFloat("DirectionY"));
         this.entityData.set(DIRECTION_Z, tag.getFloat("DirectionZ"));
      }

      this.travelled = tag.getFloat("Travelled");
      this.executionRegistered = tag.getBoolean("ExecutionRegistered");
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
      this.entityData.get(OWNER).ifPresent(uuid -> tag.putUUID("Owner", uuid));
      tag.putInt("Tier", this.getTier());
      tag.putBoolean("Dual", this.isDual());
      tag.putInt("PrimaryColor", this.getPrimaryColor());
      tag.putInt("SecondaryColor", this.getSecondaryColor());
      tag.putFloat("Width", this.getBeamWidth());
      tag.putFloat("MaxRange", this.getMaxRange());
      tag.putFloat("Speed", this.getBeamSpeed());
      tag.putFloat("Damage", this.getDamage());
      tag.putFloat("DirectionX", this.entityData.get(DIRECTION_X));
      tag.putFloat("DirectionY", this.entityData.get(DIRECTION_Y));
      tag.putFloat("DirectionZ", this.entityData.get(DIRECTION_Z));
      tag.putFloat("Travelled", this.travelled);
      tag.putBoolean("ExecutionRegistered", this.executionRegistered);
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public AABB getBoundingBoxForCulling() {
      double visualScale = this.getVisualScale();
      double horizontal = Math.max(2.0, this.getBeamWidth() * visualScale * 1.55);
      double vertical = Math.max(2.0, this.getBeamWidth() * visualScale * (this.isDual() ? 1.15 : 0.92));
      return new AABB(
         this.getX() - horizontal, this.getY() - vertical, this.getZ() - horizontal, this.getX() + horizontal, this.getY() + vertical, this.getZ() + horizontal
      );
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double distance) {
      double range = Math.max(256.0, this.getMaxRange() + 128.0);
      return distance < range * range;
   }
}
