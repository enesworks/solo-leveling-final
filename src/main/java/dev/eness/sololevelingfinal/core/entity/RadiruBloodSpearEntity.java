package dev.eness.sololevelingfinal.core.entity;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;
import dev.eness.sololevelingfinal.core.util.WhiteFlameMonarchManager;

public class RadiruBloodSpearEntity extends Entity {
   private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(RadiruBloodSpearEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Boolean> MANIFESTED = SynchedEntityData.defineId(RadiruBloodSpearEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(RadiruBloodSpearEntity.class, EntityDataSerializers.FLOAT);
   private final Set<UUID> struck = new HashSet<>();

   public RadiruBloodSpearEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.RADIRU_BLOOD_SPEAR.get(), level);
   }

   public RadiruBloodSpearEntity(EntityType<? extends RadiruBloodSpearEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(OWNER, Optional.empty());
      this.entityData.define(MANIFESTED, false);
      this.entityData.define(DAMAGE, 1.0F);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public static void launch(ServerPlayer owner, float damage, boolean manifested) {
      ServerLevel level = owner.serverLevel();
      RadiruBloodSpearEntity spear = new RadiruBloodSpearEntity(SololevelingModEntities.RADIRU_BLOOD_SPEAR.get(), level);
      spear.entityData.set(OWNER, Optional.of(owner.getUUID()));
      spear.entityData.set(MANIFESTED, manifested);
      spear.entityData.set(DAMAGE, damage);
      Vec3 look = owner.getLookAngle().normalize();
      Vec3 start = owner.getEyePosition().add(look.scale(0.8));
      spear.moveTo(start.x, start.y - 0.18, start.z, owner.getYRot(), owner.getXRot());
      spear.setDeltaMovement(look.scale(manifested ? 3.2 : 2.7));
      level.addFreshEntity(spear);
   }

   public Entity getOwner() {
      return this.level() instanceof ServerLevel level ? this.entityData.get(OWNER).map(level::getEntity).orElse(null) : null;
   }

   public boolean isManifested() {
      return this.entityData.get(MANIFESTED);
   }

   @Override
   public void tick() {
      super.tick();
      Vec3 start = this.position();
      Vec3 velocity = this.getDeltaMovement();
      Vec3 end = start.add(velocity);
      if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
         BlockHitResult blockHit = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this));
         Vec3 travelEnd = blockHit.getType() == Type.MISS ? end : blockHit.getLocation();
         this.hitEntities(level, start, travelEnd);
         if (blockHit.getType() != Type.MISS) {
            this.setPos(travelEnd);
            this.impact(level, travelEnd, true);
            this.discard();
            return;
         }
      }

      this.setPos(end);
      this.setDeltaMovement(velocity.scale(0.995).add(0.0, -0.006, 0.0));
      if (this.tickCount > (this.isManifested() ? 44 : 36)) {
         if (this.level() instanceof ServerLevel level) {
            this.impact(level, this.position(), false);
         }

         this.discard();
      }
   }

   private void hitEntities(ServerLevel level, Vec3 start, Vec3 end) {
      if (this.getOwner() instanceof ServerPlayer owner) {
         AABB path = new AABB(start, end).inflate(this.isManifested() ? 0.9 : 0.65);

         for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, path, candidate -> WhiteFlameMonarchManager.validTarget(owner, candidate))
            .stream()
            .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(start)))
            .toList()) {
            if (this.struck.add(target.getUUID())) {
               WhiteFlameMonarchManager.dealMagic(owner, target, this.entityData.get(DAMAGE));
               WhiteFlameMonarchManager.brand(target, owner, this.isManifested() ? 180 : 120, this.isManifested() ? 2 : 1);
               target.setSecondsOnFire(this.isManifested() ? 5 : 3);
               if (this.struck.size() >= (this.isManifested() ? 7 : 4)) {
                  this.impact(level, target.getBoundingBox().getCenter(), false);
                  this.discard();
                  return;
               }
            }
         }
      }
   }

   private void impact(ServerLevel level, Vec3 point, boolean struckBlock) {
      WhiteFlameVfxEntity.spawn(level, point.x, point.y + 0.05, point.z, 6, this.isManifested() ? 3.6F : 2.5F, 1.0F, 12, 0.0F, 0.0F);
      if (struckBlock && this.getOwner() instanceof ServerPlayer owner) {
         AbilityDestructionManager.impact(
            owner, AbilityDestructionManager.Profile.WHITE_FLAME_SPEAR, point, TemporaryStatBonusManager.effectiveIntelligence(owner), this.isManifested()
         );
      }
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      if (tag.hasUUID("Owner")) {
         this.entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
      }

      this.entityData.set(MANIFESTED, tag.getBoolean("Manifested"));
      this.entityData.set(DAMAGE, tag.getFloat("Damage"));
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
      this.entityData.get(OWNER).ifPresent(uuid -> tag.putUUID("Owner", uuid));
      tag.putBoolean("Manifested", this.isManifested());
      tag.putFloat("Damage", this.entityData.get(DAMAGE));
   }

   @Override
   public boolean isPickable() {
      return false;
   }
}
