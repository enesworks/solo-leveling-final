package dev.eness.sololevelingfinal.core.entity;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.MageCombatHelper;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class ManaArrowEntity extends AbstractArrow implements ItemSupplier {
   public static final ItemStack PROJECTILE_ITEM = new ItemStack(Blocks.AIR);
   private static final EntityDataAccessor<Integer> RANGER_STAGE = SynchedEntityData.defineId(ManaArrowEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> ORDINARY_RANGER_ARROW = SynchedEntityData.defineId(ManaArrowEntity.class, EntityDataSerializers.BOOLEAN);
   private UUID rangerTargetId;
   private double rangerInitialDistance;
   private double rangerTravelled;
   private boolean rangerGuidanceActive;

   public ManaArrowEntity(SpawnEntity packet, Level world) {
      super(SololevelingModEntities.MANA_ARROW.get(), world);
      this.pickup = Pickup.DISALLOWED;
   }

   public ManaArrowEntity(EntityType<? extends ManaArrowEntity> type, Level world) {
      super(type, world);
      this.pickup = Pickup.DISALLOWED;
   }

   public ManaArrowEntity(EntityType<? extends ManaArrowEntity> type, double x, double y, double z, Level world) {
      super(type, x, y, z, world);
      this.pickup = Pickup.DISALLOWED;
   }

   public ManaArrowEntity(EntityType<? extends ManaArrowEntity> type, LivingEntity entity, Level world) {
      super(type, entity, world);
      this.pickup = Pickup.DISALLOWED;
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(RANGER_STAGE, 0);
      this.entityData.define(ORDINARY_RANGER_ARROW, false);
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

   @Override
   protected void doPostHurtEffects(LivingEntity entity) {
      super.doPostHurtEffects(entity);
      entity.setArrowCount(entity.getArrowCount() - 1);
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide() && this.getRangerStage() == 3) {
         this.tickSeekingArc();
      }

      if (this.getRangerStage() == 2) {
         this.setNoGravity(true);
      }

      Vec3 before = this.position();
      super.tick();
      this.rangerTravelled = this.rangerTravelled + before.distanceTo(this.position());
      if (this.inGround) {
         this.discard();
      } else if (this.tickCount > 160) {
         this.discard();
      }
   }

   @Override
   public boolean isCritArrow() {
      return !this.level().isClientSide() && super.isCritArrow();
   }

   @Override
   public boolean displayFireAnimation() {
      return false;
   }

   public void configureRangerShot(int stage, UUID targetId, double initialDistance, boolean ordinary) {
      int clampedStage = Math.max(0, Math.min(3, stage));
      this.entityData.set(RANGER_STAGE, clampedStage);
      this.entityData.set(ORDINARY_RANGER_ARROW, ordinary);
      this.rangerTargetId = targetId;
      this.rangerInitialDistance = Math.max(0.0, initialDistance);
      this.rangerTravelled = 0.0;
      this.rangerGuidanceActive = false;
      this.pickup = Pickup.DISALLOWED;
      this.setNoGravity(clampedStage == 2);
   }

   public int getRangerStage() {
      return this.entityData.get(RANGER_STAGE);
   }

   public boolean isOrdinaryRangerArrow() {
      return this.entityData.get(ORDINARY_RANGER_ARROW);
   }

   private void tickSeekingArc() {
      if (this.level() instanceof ServerLevel serverLevel && this.rangerTargetId != null) {
         if (serverLevel.getEntity(this.rangerTargetId) instanceof LivingEntity target
            && target.isAlive()
            && target.level() == this.level()
            && MageCombatHelper.isValidTarget(this.getOwner(), target)) {
            Vec3 current = this.getDeltaMovement();
            Vec3 targetCenter = target.getBoundingBox().getCenter();
            double targetDistance = targetCenter.distanceTo(this.position());
            if (!this.rangerGuidanceActive) {
               double activationDistance = Math.max(2.0, this.rangerInitialDistance * 0.08);
               boolean closeTarget = targetDistance <= 18.0;
               boolean naturalApex = this.tickCount > 0 && current.y <= 0.0;
               boolean launchTravelled = this.rangerTravelled >= activationDistance;
               if (!closeTarget && !naturalApex && !launchTravelled && this.tickCount < 2) {
                  this.setNoGravity(false);
                  return;
               }

               this.rangerGuidanceActive = true;
            }

            this.setNoGravity(true);
            double speed = Mth.clamp(current.length(), 1.6, 4.5);
            double leadTicks = Mth.clamp(targetDistance / Math.max(1.0, speed), 0.0, 5.0);
            double leadScale = targetDistance < 10.0 ? 0.3 : 0.75;
            Vec3 aim = targetCenter.add(target.getDeltaMovement().scale(leadTicks * leadScale));
            if (this.isGuidancePathBlocked(aim)) {
               if (!this.isGuidancePathBlocked(targetCenter)) {
                  aim = targetCenter;
               } else {
                  Vec3 targetEyes = target.getEyePosition();
                  if (this.isGuidancePathBlocked(targetEyes)) {
                     return;
                  }

                  aim = targetEyes;
               }
            }

            Vec3 desired = aim.subtract(this.position());
            if (!(desired.lengthSqr() < 1.0E-4)) {
               desired = desired.normalize();
               Vec3 currentDirection = current.lengthSqr() > 1.0E-4 ? current.normalize() : desired;
               double turn = target instanceof Player ? 0.18 : 0.28;
               if (targetDistance < 12.0) {
                  turn += 0.14;
               }

               if (targetDistance < 6.0) {
                  turn += 0.18;
               }

               if (currentDirection.dot(desired) < 0.65) {
                  turn += 0.12;
               }

               turn = Mth.clamp(turn, 0.0, 0.68);
               double guidedSpeed = speed;
               if (targetDistance < 6.0) {
                  guidedSpeed = Math.max(1.6, Math.min(speed, targetDistance * 0.55 + 0.8));
               }

               Vec3 steered = rotateToward(currentDirection, desired, turn);
               if (steered.lengthSqr() > 1.0E-4) {
                  this.setDeltaMovement(steered.normalize().scale(guidedSpeed));
                  this.hasImpulse = true;
               }
            }
         } else {
            this.rangerTargetId = null;
         }
      }
   }

   private boolean isGuidancePathBlocked(Vec3 aim) {
      BlockHitResult obstruction = this.level().clip(new ClipContext(this.position(), aim, Block.COLLIDER, Fluid.NONE, this));
      return obstruction.getType() == Type.BLOCK && obstruction.getLocation().distanceToSqr(this.position()) + 0.25 < aim.distanceToSqr(this.position());
   }

   private static Vec3 rotateToward(Vec3 current, Vec3 desired, double turnFraction) {
      double dot = Mth.clamp(current.dot(desired), -1.0, 1.0);
      if (dot > 0.9999) {
         return desired;
      }

      double angle = Math.acos(dot);
      double turnAngle = Math.min(angle, Math.max(Math.toRadians(4.0), angle * Mth.clamp(turnFraction, 0.0, 1.0)));
      Vec3 axis = current.cross(desired);
      if (axis.lengthSqr() < 1.0E-6) {
         Vec3 reference = Math.abs(current.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
         axis = current.cross(reference);
      }

      axis = axis.normalize();
      return current.scale(Math.cos(turnAngle)).add(axis.cross(current).scale(Math.sin(turnAngle))).normalize();
   }

   @Override
   protected boolean canHitEntity(Entity entity) {
      Entity owner = this.getOwner();
      if (this.getRangerStage() > 0 && owner == null) {
         return false;
      } else {
         return owner != null && entity instanceof LivingEntity
            ? MageCombatHelper.isValidTarget(owner, entity) && super.canHitEntity(entity)
            : super.canHitEntity(entity);
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("RangerStage", this.getRangerStage());
      tag.putBoolean("OrdinaryRangerArrow", this.isOrdinaryRangerArrow());
      tag.putDouble("RangerInitialDistance", this.rangerInitialDistance);
      tag.putDouble("RangerTravelled", this.rangerTravelled);
      tag.putBoolean("RangerGuidanceActive", this.rangerGuidanceActive);
      if (this.rangerTargetId != null) {
         tag.putUUID("RangerTarget", this.rangerTargetId);
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(RANGER_STAGE, Math.max(0, Math.min(3, tag.getInt("RangerStage"))));
      this.entityData.set(ORDINARY_RANGER_ARROW, tag.getBoolean("OrdinaryRangerArrow"));
      this.rangerInitialDistance = Math.max(0.0, tag.getDouble("RangerInitialDistance"));
      this.rangerTravelled = Math.max(0.0, tag.getDouble("RangerTravelled"));
      this.rangerGuidanceActive = tag.getBoolean("RangerGuidanceActive");
      this.rangerTargetId = tag.hasUUID("RangerTarget") ? tag.getUUID("RangerTarget") : null;
      this.pickup = Pickup.DISALLOWED;
      this.setNoGravity(this.getRangerStage() == 2 || this.getRangerStage() == 3 && this.rangerGuidanceActive);
   }

   public static ManaArrowEntity shoot(Level world, LivingEntity entity, RandomSource source) {
      return shoot(world, entity, source, 2.0F, 3.0, 1);
   }

   public static ManaArrowEntity shoot(Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback) {
      ManaArrowEntity entityarrow = new ManaArrowEntity(SololevelingModEntities.MANA_ARROW.get(), entity, world);
      entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
      entityarrow.setSilent(true);
      entityarrow.setCritArrow(false);
      entityarrow.setBaseDamage(damage);
      entityarrow.setKnockback(knockback);
      world.addFreshEntity(entityarrow);
      world.playSound(
         null,
         entity.getX(),
         entity.getY(),
         entity.getZ(),
         ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.amethyst_block.break")),
         SoundSource.PLAYERS,
         1.0F,
         1.0F / (random.nextFloat() * 0.5F + 1.0F) + power / 2.0F
      );
      return entityarrow;
   }

   public static ManaArrowEntity shoot(LivingEntity entity, LivingEntity target) {
      ManaArrowEntity entityarrow = new ManaArrowEntity(SololevelingModEntities.MANA_ARROW.get(), entity, entity.level());
      double dx = target.getX() - entity.getX();
      double dy = target.getY() + target.getEyeHeight() - 1.1;
      double dz = target.getZ() - entity.getZ();
      entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 4.0F, 12.0F);
      entityarrow.setSilent(true);
      entityarrow.setBaseDamage(3.0);
      entityarrow.setKnockback(1);
      entityarrow.setCritArrow(false);
      entity.level().addFreshEntity(entityarrow);
      entity.level()
         .playSound(
            null,
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.amethyst_block.break")),
            SoundSource.PLAYERS,
            1.0F,
            1.0F / (RandomSource.create().nextFloat() * 0.5F + 1.0F)
         );
      return entityarrow;
   }
}
