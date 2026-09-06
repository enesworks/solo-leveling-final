package dev.eness.sololevelingfinal.core.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
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
import dev.eness.sololevelingfinal.core.util.DaggerThrowManager;
import dev.eness.sololevelingfinal.core.util.EntityHighlightSystem;
import dev.eness.sololevelingfinal.core.util.RulersAuthorityManager;

public class ThrownDaggerEntity extends Projectile {
   private static final String OWNER_GLOW_SOURCE = "dagger:owner";
   private static final int OWNER_GLOW_COLOR = 8382719;
   private static final int OWNER_GLOW_DURATION_TICKS = 18;
   private static final int OWNER_GLOW_PRIORITY = 380;
   private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(ThrownDaggerEntity.class, EntityDataSerializers.ITEM_STACK);
   private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(ThrownDaggerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Optional<UUID>> TOKEN = SynchedEntityData.defineId(ThrownDaggerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Boolean> SPECTRAL = SynchedEntityData.defineId(ThrownDaggerEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(ThrownDaggerEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(ThrownDaggerEntity.class, EntityDataSerializers.INT);
   private final Map<UUID, Integer> hitTicks = new HashMap<>();
   private long rulerControlTick = Long.MIN_VALUE;
   private boolean recoveredDiscard;

   public ThrownDaggerEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.THROWN_DAGGER.get(), level);
   }

   public ThrownDaggerEntity(EntityType<? extends ThrownDaggerEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(ITEM, ItemStack.EMPTY);
      this.entityData.define(OWNER_ID, Optional.empty());
      this.entityData.define(TOKEN, Optional.empty());
      this.entityData.define(SPECTRAL, false);
      this.entityData.define(RETURNING, false);
      this.entityData.define(DELAY, 0);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public static ThrownDaggerEntity createPhysical(ServerPlayer owner, ItemStack item, UUID token, Vec3 origin, Vec3 velocity) {
      ThrownDaggerEntity dagger = base(owner, item, origin, velocity);
      dagger.entityData.set(TOKEN, Optional.of(token));
      return dagger;
   }

   public static ThrownDaggerEntity createSpectral(ServerPlayer owner, ItemStack item, Vec3 origin, Vec3 velocity, int delay) {
      ThrownDaggerEntity dagger = base(owner, item, origin, velocity);
      dagger.entityData.set(SPECTRAL, true);
      dagger.entityData.set(DELAY, Math.max(0, delay));
      return dagger;
   }

   private static ThrownDaggerEntity base(ServerPlayer owner, ItemStack item, Vec3 origin, Vec3 velocity) {
      ThrownDaggerEntity dagger = new ThrownDaggerEntity(SololevelingModEntities.THROWN_DAGGER.get(), owner.level());
      ItemStack visual = item.copy();
      visual.setCount(1);
      dagger.entityData.set(ITEM, visual);
      dagger.entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
      dagger.setOwner(owner);
      dagger.moveTo(origin.x, origin.y, origin.z, owner.getYRot(), owner.getXRot());
      dagger.setDeltaMovement(velocity);
      return dagger;
   }

   public ItemStack getDaggerStack() {
      return this.entityData.get(ITEM);
   }

   public boolean isPhysical() {
      return !this.entityData.get(SPECTRAL);
   }

   public boolean isSpectral() {
      return this.entityData.get(SPECTRAL);
   }

   public boolean isReturning() {
      return this.entityData.get(RETURNING);
   }

   public UUID getEscrowToken() {
      return this.entityData.get(TOKEN).orElse(null);
   }

   public UUID getOwnerId() {
      return this.entityData.get(OWNER_ID).orElse(null);
   }

   public boolean isOwnedBy(ServerPlayer player) {
      return player != null && player.getUUID().equals(this.getOwnerId());
   }

   public boolean beginReturn() {
      if (this.isPhysical() && this.level() instanceof ServerLevel level && !RulersAuthorityManager.hasAuthority(this.owner(level))) {
         return false;
      }

      this.entityData.set(RETURNING, true);
      this.setNoGravity(true);
      return true;
   }

   public void markRulersControlled(long gameTime) {
      this.rulerControlTick = gameTime + 6L;
      this.entityData.set(RETURNING, false);
   }

   public void onRulersReleased() {
      this.rulerControlTick = this.level().getGameTime();
   }

   public void discardAsRecovered() {
      this.recoveredDiscard = true;
      this.discard();
   }

   @Override
   public boolean isPickable() {
      return true;
   }

   @Override
   public float getPickRadius() {
      return 0.65F;
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
         ServerPlayer owner = this.owner(level);
         if (owner == null || !owner.isAlive()) {
            if (this.isSpectral() || this.tickCount > 200) {
               this.discard();
            }

            return;
         }

         this.refreshOwnerGlow(owner);
         if (this.isPhysical()) {
            DaggerThrowManager.register(this);
            if (!DaggerThrowManager.isAuthorized(owner, this.getEscrowToken())) {
               this.discard();
               return;
            }
         }

         if (this.tickCount <= this.entityData.get(DELAY)) {
            return;
         }

         boolean controlled = this.rulerControlTick >= level.getGameTime();
         if (this.isPhysical() && this.isReturning() && !RulersAuthorityManager.hasAuthority(owner)) {
            this.entityData.set(RETURNING, false);
            this.setNoGravity(false);
         }

         if (this.isReturning() && !controlled) {
            Vec3 destination = owner.getEyePosition().add(0.0, -0.25, 0.0);
            Vec3 toOwner = destination.subtract(this.position());
            if (toOwner.lengthSqr() <= 2.25) {
               this.setDeltaMovement(Vec3.ZERO);
               if (this.isSpectral()) {
                  this.discard();
               } else if (this.tickCount % 20 == 0 && DaggerThrowManager.completeReturn(owner, this.getEscrowToken(), this)) {
                  this.discard();
               }

               return;
            }

            this.setDeltaMovement(toOwner.normalize().scale(this.isSpectral() ? 3.0 : 2.5));
         }

         Vec3 start = this.position();
         Vec3 end = start.add(this.getDeltaMovement());
         BlockHitResult blockHit = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this));
         Vec3 travelEnd = !this.isReturning() && blockHit.getType() == Type.BLOCK ? blockHit.getLocation() : end;
         this.hitTargets(level, owner, start, travelEnd, controlled);
         if (blockHit.getType() == Type.BLOCK && !this.isReturning()) {
            this.setPos(travelEnd);
            this.setDeltaMovement(Vec3.ZERO);
            if (this.isSpectral()) {
               this.beginReturn();
            } else if (!controlled && this.tickCount > 80) {
               this.beginReturn();
            }

            return;
         }

         this.setPos(travelEnd);
         if (!controlled && !this.isReturning()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.995).add(0.0, this.isSpectral() ? 0.0 : -0.012, 0.0));
         }

         if (!controlled && (this.isSpectral() && this.tickCount > 30 || this.isPhysical() && this.tickCount > 90)) {
            this.beginReturn();
         }

         if (this.tickCount > (this.isSpectral() ? 90 : 600)) {
            if (this.isSpectral()) {
               this.discard();
            } else if (!this.beginReturn()) {
               this.discard();
            }
         }
      } else if (this.tickCount > this.entityData.get(DELAY)) {
         this.setPos(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
      }
   }

   private void hitTargets(ServerLevel level, ServerPlayer owner, Vec3 start, Vec3 end, boolean controlled) {
      AABB path = new AABB(start, end).inflate(this.isSpectral() ? 0.42 : 0.5);

      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, path, candidate -> this.validTarget(owner, candidate))) {
         int lastHit = this.hitTicks.getOrDefault(target.getUUID(), -1073741824);
         if (this.tickCount - lastHit >= (controlled ? 10 : 1000)) {
            this.hitTicks.put(target.getUUID(), this.tickCount);
            DamageSource source = new DamageSource(
               level.registryAccess()
                  .registryOrThrow(Registries.DAMAGE_TYPE)
                  .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin"))),
               this,
               owner
            );
            float damage = this.isSpectral() ? DaggerThrowManager.rushDamage(owner) : DaggerThrowManager.physicalDamage(owner);
            if (target.hurt(source, damage)) {
               target.invulnerableTime = 0;
               level.playSound(
                  (Player)null,
                  BlockPos.containing(target.position()),
                  SoundEvents.PLAYER_ATTACK_CRIT,
                  SoundSource.PLAYERS,
                  0.7F,
                  this.isSpectral() ? 1.45F : 1.1F
               );
               if (this.isPhysical()) {
                  ItemStack dagger = this.getDaggerStack().copy();
                  dagger.hurtAndBreak(1, owner, broken -> {});
                  this.entityData.set(ITEM, dagger);
                  DaggerThrowManager.updateEscrowItem(owner, this.getEscrowToken(), dagger);
                  if (dagger.isEmpty()) {
                     this.discard();
                     return;
                  }
               }
            }

            if (!controlled) {
               this.beginReturn();
            }
         }
      }
   }

   private boolean validTarget(ServerPlayer owner, LivingEntity candidate) {
      if (candidate != owner && candidate.isAlive() && !candidate.isSpectator()) {
         return candidate instanceof TamableAnimal tame && tame.isOwnedBy(owner)
            ? false
            : !(candidate instanceof ServerPlayer other && !owner.canHarmPlayer(other));
      } else {
         return false;
      }
   }

   private ServerPlayer owner(ServerLevel level) {
      if (level == null) {
         return null;
      }

      UUID id = this.getOwnerId();
      ServerPlayer owner = id == null ? null : level.getServer().getPlayerList().getPlayer(id);
      return owner != null && owner.level() == this.level() ? owner : null;
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(ITEM, ItemStack.of(tag.getCompound("Item")));
      if (tag.hasUUID("Owner")) {
         this.entityData.set(OWNER_ID, Optional.of(tag.getUUID("Owner")));
      }

      this.entityData.set(TOKEN, tag.hasUUID("Token") ? Optional.of(tag.getUUID("Token")) : Optional.empty());
      this.entityData.set(SPECTRAL, tag.getBoolean("Spectral"));
      this.entityData.set(RETURNING, tag.getBoolean("Returning"));
      this.entityData.set(DELAY, tag.getInt("Delay"));
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.put("Item", this.getDaggerStack().save(new CompoundTag()));
      UUID owner = this.getOwnerId();
      if (owner != null) {
         tag.putUUID("Owner", owner);
      }

      UUID token = this.getEscrowToken();
      if (token != null) {
         tag.putUUID("Token", token);
      }

      tag.putBoolean("Spectral", this.isSpectral());
      tag.putBoolean("Returning", this.isReturning());
      tag.putInt("Delay", this.entityData.get(DELAY));
   }

   @Override
   public void remove(RemovalReason reason) {
      if (!this.level().isClientSide && !this.recoveredDiscard) {
         DaggerThrowManager.unregister(this);
      }

      if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
         ServerPlayer owner = this.owner(level);
         if (owner != null) {
            EntityHighlightSystem.hide(owner, this, "dagger:owner");
         }
      }

      super.remove(reason);
   }

   private void refreshOwnerGlow(ServerPlayer owner) {
      if (owner != null && this.tickCount % 8 == 0) {
         EntityHighlightSystem.show(owner, this, "dagger:owner", 8382719, 18, 380);
      }
   }
}
