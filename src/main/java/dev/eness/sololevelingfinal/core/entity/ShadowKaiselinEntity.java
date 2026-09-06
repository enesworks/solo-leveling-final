package dev.eness.sololevelingfinal.core.entity;

import java.util.Comparator;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class ShadowKaiselinEntity extends KaiselinEntity implements OwnableEntity {
   private static final String OWNER_TAG = "sl_shadow_owner";
   private static final String COMMAND_TAG = "sl_shadow_command";
   private static final TagKey<EntityType<?>> SHADOWS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows"));
   private static final EntityDimensions SHADOW_KAISEL_BODY_DIMENSIONS = EntityDimensions.scalable(2.25F, 1.85F);

   public ShadowKaiselinEntity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.SHADOW_KAISELIN.get(), world);
   }

   public ShadowKaiselinEntity(EntityType<ShadowKaiselinEntity> type, Level world) {
      super(type, world);
      this.setKaiselinVariant("shadow_kaiselin", "Kaisel");
      this.refreshDimensions();
   }

   @Override
   protected LivingEntity getOrFindTarget() {
      if (this.isVehicle()) {
         return null;
      }

      Player owner = this.getOwnerPlayer();
      LivingEntity ownerPriority = ShadowMonarchManager.findOwnerCombatPriorityTarget(this, owner);
      if (ownerPriority != null) {
         this.setTarget(ownerPriority);
         return ownerPriority;
      }

      String command = this.getPersistentData().getString("sl_shadow_command");
      boolean clearDungeon = "clear_dungeon".equals(command);
      if (command.isEmpty() || "default".equals(command)) {
         LivingEntity target = ShadowMonarchManager.findDefaultCommandTarget(this, owner);
         this.setTarget(target);
         return target;
      }

      if ("follow".equals(command)) {
         this.setTarget(null);
         return null;
      }

      if (clearDungeon && owner != null && !ShadowMonarchManager.isInDungeon(owner)) {
         this.setTarget(null);
         return null;
      }

      if (clearDungeon) {
         LivingEntity current = this.getTarget();
         if (owner != null && ShadowMonarchManager.isValidClearDungeonTarget(current, this, owner) && this.hasLineOfSight(current)) {
            return current;
         }

         this.setTarget(null);
         return null;
      } else {
         if ("protect".equals(command)) {
            LivingEntity threat = this.findOwnerThreat(owner);
            this.setTarget(threat);
            return threat;
         }

         LivingEntity ownerTarget = owner == null ? null : owner.getLastHurtMob();
         if (!this.isValidCombatTarget(ownerTarget) || clearDungeon && !this.hasLineOfSight(ownerTarget)) {
            LivingEntity ownerAttacker = owner == null ? null : owner.getLastHurtByMob();
            if (!this.isValidCombatTarget(ownerAttacker) || clearDungeon && !this.hasLineOfSight(ownerAttacker)) {
               LivingEntity current = this.getTarget();
               if (!this.isValidCombatTarget(current) || clearDungeon && !this.hasLineOfSight(current)) {
                  this.setTarget(null);
                  double range = clearDungeon ? 64.0 : 42.0;
                  LivingEntity nearest = this.level()
                     .getEntitiesOfClass(
                        LivingEntity.class,
                        this.getBoundingBox().inflate(range),
                        target -> this.isValidCombatTarget(target) && (!clearDungeon || this.hasLineOfSight(target))
                     )
                     .stream()
                     .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this)))
                     .orElse(null);
                  if (nearest != null) {
                     this.setTarget(nearest);
                  }

                  return nearest;
               } else {
                  return current;
               }
            } else {
               this.setTarget(ownerAttacker);
               return ownerAttacker;
            }
         } else {
            this.setTarget(ownerTarget);
            return ownerTarget;
         }
      }
   }

   private LivingEntity findOwnerThreat(Player owner) {
      if (owner == null) {
         return null;
      } else {
         LivingEntity current = this.getTarget();
         if (current instanceof Mob mob && mob.getTarget() == owner && this.isValidCombatTarget(current)) {
            return current;
         } else {
            LivingEntity attacker = owner.getLastHurtByMob();
            return this.isValidCombatTarget(attacker)
               ? attacker
               : this.level()
                  .getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(48.0), mob -> mob.getTarget() == owner && this.isValidCombatTarget(mob))
                  .stream()
                  .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this)))
                  .orElse(null);
         }
      }
   }

   @Override
   protected boolean isValidCombatTarget(LivingEntity target) {
      if (target == null || !target.isAlive() || target == this || target == this.getControllingPassenger()) {
         return false;
      } else if (target instanceof Player) {
         return false;
      } else if (target.getType().is(SHADOWS)) {
         return false;
      } else {
         return target instanceof TamableAnimal tame && this.getOwnerUUID() != null && this.getOwnerUUID().equals(tame.getOwnerUUID())
            ? false
            : target instanceof Monster || super.isValidCombatTarget(target);
      }
   }

   @Override
   public void baseTick() {
      super.baseTick();
      if (!this.level().isClientSide() && !this.isVehicle() && this.getTarget() == null && ShadowMonarchManager.shouldFollowOwner(this)) {
         Player owner = this.getOwnerPlayer();
         if (owner != null && owner.isAlive()) {
            double distance = this.distanceTo(owner);
            if (distance > 48.0) {
               this.teleportTo(owner.getX(), owner.getY() + 2.0, owner.getZ());
            } else if (distance > 8.0) {
               this.flyToward(owner.position().add(0.0, 4.0, 0.0), 0.12, 0.86);
               this.turnToward(owner.position(), 10.0F);
            }
         }
      }
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (this.isOwnedBy(player) && !player.isShiftKeyDown()) {
         if (!this.level().isClientSide()) {
            player.startRiding(this);
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide());
      } else {
         return super.mobInteract(player, hand);
      }
   }

   @Override
   public void startSeenByPlayer(ServerPlayer player) {
   }

   @Override
   public void stopSeenByPlayer(ServerPlayer player) {
   }

   @Override
   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengers().isEmpty() && passenger instanceof Player player && this.isOwnedBy(player);
   }

   @Override
   public LivingEntity getControllingPassenger() {
      return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
   }

   @Override
   public double getPassengersRidingOffset() {
      return 2.15;
   }

   @Override
   protected void positionRider(Entity passenger, MoveFunction moveFunction) {
      if (this.hasPassenger(passenger)) {
         moveFunction.accept(passenger, this.getX(), this.getY() + this.getPassengersRidingOffset() + passenger.getMyRidingOffset(), this.getZ());
      }
   }

   @Override
   public void travel(Vec3 travelVector) {
      if (this.getControllingPassenger() instanceof Player rider) {
         this.setNoGravity(true);
         this.setYRot(rider.getYRot());
         this.setXRot(Mth.clamp(rider.getXRot(), -55.0F, 55.0F) * 0.45F);
         this.yBodyRot = this.getYRot();
         this.yHeadRot = this.getYRot();
         this.setYHeadRot(this.getYRot());
         float forwardInput = rider.zza;
         float strafeInput = rider.xxa;
         Vec3 look = rider.getLookAngle();
         Vec3 flatForward = new Vec3(look.x, 0.0, look.z);
         if (flatForward.lengthSqr() < 0.001) {
            flatForward = Vec3.directionFromRotation(0.0F, rider.getYRot()).multiply(1.0, 0.0, 1.0);
         }

         flatForward = flatForward.normalize();
         Vec3 right = new Vec3(-flatForward.z, 0.0, flatForward.x);
         double pitchLift = -Mth.sin(rider.getXRot() * (float) (Math.PI / 180.0)) * Math.max(0.0F, forwardInput) * 0.58;
         double descend = rider.isShiftKeyDown() ? -0.42 : 0.0;
         Vec3 desired = flatForward.scale(forwardInput * 0.95).add(right.scale(-strafeInput * 0.55)).add(0.0, pitchLift + descend, 0.0);
         Vec3 next = desired.lengthSqr() > 0.001 ? this.getDeltaMovement().scale(0.68).add(desired.scale(0.32)) : this.getDeltaMovement().scale(0.82);
         this.setDeltaMovement(next);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.hasImpulse = true;
      } else {
         super.travel(travelVector);
      }
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      Entity attacker = source.getEntity();
      if (attacker instanceof Player player && this.isOwnedBy(player)) {
         return false;
      } else {
         return ShadowMonarchManager.haveSameShadowOwner(this, attacker) ? false : super.hurt(source, amount);
      }
   }

   @Override
   public boolean isAlliedTo(Entity entity) {
      if (super.isAlliedTo(entity)) {
         return true;
      } else {
         return entity instanceof Player player && this.isOwnedBy(player) ? true : ShadowMonarchManager.haveSameShadowOwner(this, entity);
      }
   }

   @Override
   public void die(DamageSource source) {
      ShadowMonarchManager.dropStoredShadowInventory(this);
      Player owner = this.getOwnerPlayer();
      if (owner != null) {
         owner.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.KaiselSpawned = Math.max(0.0, capability.KaiselSpawned - 1.0);
            capability.syncPlayerVariables(owner);
         });
      }

      super.die(source);
   }

   @Override
   public EntityDimensions getDimensions(Pose pose) {
      return SHADOW_KAISEL_BODY_DIMENSIONS;
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.setKaiselinVariant("shadow_kaiselin", "Kaisel");
   }

   private boolean isOwnedBy(Player player) {
      UUID owner = this.getOwnerUUID();
      return owner != null && player != null && owner.equals(player.getUUID());
   }

   @Override
   public UUID getOwnerUUID() {
      CompoundTag data = this.getPersistentData();
      return data.hasUUID("sl_shadow_owner") ? data.getUUID("sl_shadow_owner") : null;
   }

   @Override
   public LivingEntity getOwner() {
      UUID owner = this.getOwnerUUID();
      return owner == null ? null : this.level().getPlayerByUUID(owner);
   }

   private Player getOwnerPlayer() {
      return this.getOwner() instanceof Player player ? player : null;
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.MAX_HEALTH, 240.0)
         .add(Attributes.ARMOR, 10.0)
         .add(Attributes.ARMOR_TOUGHNESS, 1.0)
         .add(Attributes.ATTACK_DAMAGE, 15.0)
         .add(Attributes.FOLLOW_RANGE, 48.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.65)
         .add(Attributes.FLYING_SPEED, 0.45);
   }
}
