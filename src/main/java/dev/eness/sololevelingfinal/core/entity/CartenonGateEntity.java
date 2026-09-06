package dev.eness.sololevelingfinal.core.entity;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.CartenonTempleManager;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CartenonGateEntity extends PathfinderMob implements GeoEntity {
   private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
   private final Set<UUID> allowedPlayers = new LinkedHashSet<>();
   private UUID ownerId;
   private int instanceId;

   public CartenonGateEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.CARTENON_GATE.get(), level);
   }

   public CartenonGateEntity(EntityType<CartenonGateEntity> type, Level level) {
      super(type, level);
      this.setNoAi(true);
      this.setNoGravity(true);
      this.setInvulnerable(true);
      this.setPersistenceRequired();
      this.xpReward = 0;
   }

   public void configure(UUID ownerId, Collection<UUID> allowedPlayers, int instanceId) {
      this.ownerId = ownerId;
      this.instanceId = Math.max(1, instanceId);
      this.allowedPlayers.clear();
      if (allowedPlayers != null) {
         this.allowedPlayers.addAll(allowedPlayers);
      }

      if (ownerId != null) {
         this.allowedPlayers.add(ownerId);
      }
   }

   public boolean isAllowed(UUID playerId) {
      return playerId != null && this.allowedPlayers.contains(playerId);
   }

   public int getInstanceId() {
      return this.instanceId;
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
         CartenonTempleManager.enterGate(serverPlayer, this);
      }

      return InteractionResult.sidedSuccess(this.level().isClientSide());
   }

   @Override
   public void tick() {
      super.tick();
      this.setDeltaMovement(Vec3.ZERO);
      this.fallDistance = 0.0F;
      this.setNoGravity(true);
      if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 4 == 0) {
         double phase = this.tickCount * 0.11;
         serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.35, this.getZ(), 4, 0.58, 1.0, 0.18, 0.02);
         serverLevel.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            this.getX() + Math.sin(phase) * 0.56,
            this.getY() + 0.35 + this.tickCount % 28 * 0.065,
            this.getZ() + Math.cos(phase) * 0.16,
            1,
            0.02,
            0.03,
            0.02,
            0.0
         );
      }
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      return false;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected void doPush(Entity entity) {
   }

   @Override
   protected void pushEntities() {
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      if (this.ownerId != null) {
         tag.putUUID("Owner", this.ownerId);
      }

      tag.putInt("CartenonInstance", this.instanceId);
      ListTag allowed = new ListTag();

      for (UUID playerId : this.allowedPlayers) {
         CompoundTag entry = new CompoundTag();
         entry.putUUID("Player", playerId);
         allowed.add(entry);
      }

      tag.put("AllowedPlayers", allowed);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
      this.instanceId = Math.max(1, tag.getInt("CartenonInstance"));
      this.allowedPlayers.clear();
      ListTag allowed = tag.getList("AllowedPlayers", 10);

      for (int i = 0; i < allowed.size(); i++) {
         CompoundTag entry = allowed.getCompound(i);
         if (entry.hasUUID("Player")) {
            this.allowedPlayers.add(entry.getUUID("Player"));
         }
      }

      if (this.ownerId != null) {
         this.allowedPlayers.add(this.ownerId);
      }
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.FOLLOW_RANGE, 1.0);
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(RawAnimation.begin().thenLoop("idle"))));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.animationCache;
   }
}
