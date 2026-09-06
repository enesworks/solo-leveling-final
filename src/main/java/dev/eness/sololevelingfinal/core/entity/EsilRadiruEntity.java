package dev.eness.sololevelingfinal.core.entity;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.dkc.event.EsilPermitClaimEvent;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class EsilRadiruEntity extends PathfinderMob {
   private static final String NBT_STATE = "RadiruState";
   private static final String NBT_OWNER = "RadiruOwner";
   private static final String NBT_PERMIT_CLAIMED = "RadiruPermitClaimed";
   private static final String NBT_PACT_DIALOGUE_INDEX = "radiru_esil_pact_dialogue_index";
   private static final String NBT_CASTLE_DIALOGUE_INDEX = "radiru_esil_castle_dialogue_index";
   private static final String NBT_DIALOGUE_AFTER = "radiru_esil_dialogue_after";
   private static final String[] PACT_DIALOGUE = new String[]{
      "dialogue.sololeveling.esil.pact.0",
      "dialogue.sololeveling.esil.pact.1",
      "dialogue.sololeveling.esil.pact.2",
      "dialogue.sololeveling.esil.pact.3",
      "dialogue.sololeveling.esil.pact.4",
      "dialogue.sololeveling.esil.pact.5"
   };
   private static final String[] CASTLE_DIALOGUE = new String[]{
      "dialogue.sololeveling.esil.castle.0",
      "dialogue.sololeveling.esil.castle.1",
      "dialogue.sololeveling.esil.castle.2",
      "dialogue.sololeveling.esil.castle.3",
      "dialogue.sololeveling.esil.castle.4",
      "dialogue.sololeveling.esil.castle.5"
   };
   private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(EsilRadiruEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_PERMIT_CLAIMED = SynchedEntityData.defineId(EsilRadiruEntity.class, EntityDataSerializers.BOOLEAN);
   private UUID encounterOwner;

   public EsilRadiruEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.ESIL_RADIRU.get(), level);
   }

   public EsilRadiruEntity(EntityType<? extends EsilRadiruEntity> type, Level level) {
      super(type, level);
      this.setMaxUpStep(0.6F);
      this.xpReward = 0;
      this.setPersistenceRequired();
      this.setCustomName(Component.translatable("entity.sololeveling.esil_radiru").withStyle(ChatFormatting.LIGHT_PURPLE));
      this.setCustomNameVisible(true);
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_STATE, EsilRadiruEntity.EncounterState.SURRENDERED.id());
      this.entityData.define(DATA_PERMIT_CLAIMED, false);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false) {
         @Override
         public boolean canUse() {
            return EsilRadiruEntity.this.isHostile() && super.canUse();
         }

         @Override
         public boolean canContinueToUse() {
            return EsilRadiruEntity.this.isHostile() && super.canContinueToUse();
         }

         @Override
         protected double getAttackReachSqr(LivingEntity target) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() + target.getBbWidth();
         }
      });
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8) {
         @Override
         public boolean canUse() {
            return EsilRadiruEntity.this.isHostile() && super.canUse();
         }

         @Override
         public boolean canContinueToUse() {
            return EsilRadiruEntity.this.isHostile() && super.canContinueToUse();
         }
      });
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<Player>(this, Player.class, true) {
         @Override
         public boolean canUse() {
            return EsilRadiruEntity.this.isHostile() && super.canUse();
         }

         @Override
         public boolean canContinueToUse() {
            return EsilRadiruEntity.this.isHostile() && super.canContinueToUse();
         }
      });
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      boolean initialClaim = this.getEncounterState() == EsilRadiruEntity.EncounterState.SURRENDERED && !this.isPermitClaimed();
      boolean replacementClaim = this.getEncounterState() == EsilRadiruEntity.EncounterState.SANCTUARY && this.isPermitClaimed();
      if (hand == InteractionHand.MAIN_HAND && (initialClaim || replacementClaim)) {
         if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
         }

         if (player instanceof ServerPlayer serverPlayer) {
            EsilPermitClaimEvent event = new EsilPermitClaimEvent(this, serverPlayer);
            boolean canceled = MinecraftForge.EVENT_BUS.post(event);
            if (!canceled && event.decision() == EsilPermitClaimEvent.Decision.GRANT) {
               this.markPermitClaimed();
               return InteractionResult.CONSUME;
            } else if (canceled || event.decision() == EsilPermitClaimEvent.Decision.DENY) {
               return InteractionResult.CONSUME;
            } else if (this.isSanctuaryResident() && this.isPermitClaimed()) {
               this.showSanctuaryDialogue(serverPlayer);
               return InteractionResult.CONSUME;
            } else {
               return InteractionResult.PASS;
            }
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return super.mobInteract(player, hand);
      }
   }

   private void showSanctuaryDialogue(ServerPlayer player) {
      long now = this.level().getGameTime();
      CompoundTag playerData = player.getPersistentData();
      if (now >= playerData.getLong("radiru_esil_dialogue_after")) {
         SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         boolean castleConquered = vars.radiru_side_quest_unlocked;
         String[] dialogue = castleConquered ? CASTLE_DIALOGUE : PACT_DIALOGUE;
         String indexTag = castleConquered ? "radiru_esil_castle_dialogue_index" : "radiru_esil_pact_dialogue_index";
         int index = Math.floorMod(playerData.getInt(indexTag), dialogue.length);
         playerData.putInt(indexTag, index + 1);
         playerData.putLong("radiru_esil_dialogue_after", now + 12L);
         this.getLookControl().setLookAt(player, 30.0F, 30.0F);
         player.displayClientMessage(
            Component.translatable("dialogue.sololeveling.esil.speech", this.getDisplayName(), Component.translatable(dialogue[index])), false
         );
      }
   }

   @Override
   public boolean doHurtTarget(Entity target) {
      return this.isHostile() && super.doHurtTarget(target);
   }

   @Override
   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   @Override
   public boolean canBeLeashed(Player player) {
      return false;
   }

   @Override
   public boolean isPushable() {
      return this.isHostile();
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   public MobType getMobType() {
      return MobType.UNDEFINED;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.PLAYER_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.PLAYER_DEATH;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("RadiruState", this.getEncounterState().id());
      tag.putBoolean("RadiruPermitClaimed", this.isPermitClaimed());
      if (this.encounterOwner != null) {
         tag.putUUID("RadiruOwner", this.encounterOwner);
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setEncounterState(
         tag.contains("RadiruState") ? EsilRadiruEntity.EncounterState.fromId(tag.getInt("RadiruState")) : EsilRadiruEntity.EncounterState.SURRENDERED
      );
      this.setPermitClaimed(tag.getBoolean("RadiruPermitClaimed"));
      this.encounterOwner = tag.hasUUID("RadiruOwner") ? tag.getUUID("RadiruOwner") : null;
   }

   public EsilRadiruEntity.EncounterState getEncounterState() {
      return EsilRadiruEntity.EncounterState.fromId(this.entityData.get(DATA_STATE));
   }

   public void setEncounterState(EsilRadiruEntity.EncounterState state) {
      EsilRadiruEntity.EncounterState next = state == null ? EsilRadiruEntity.EncounterState.SURRENDERED : state;
      this.entityData.set(DATA_STATE, next.id());
      this.setCustomNameVisible(next != EsilRadiruEntity.EncounterState.HOSTILE);
      if (next != EsilRadiruEntity.EncounterState.HOSTILE) {
         this.setTarget(null);
         this.setAggressive(false);
         this.getNavigation().stop();
      }
   }

   public boolean isHostile() {
      return this.getEncounterState() == EsilRadiruEntity.EncounterState.HOSTILE;
   }

   public boolean isSurrendered() {
      return this.getEncounterState() == EsilRadiruEntity.EncounterState.SURRENDERED;
   }

   public boolean isSanctuaryResident() {
      return this.getEncounterState() == EsilRadiruEntity.EncounterState.SANCTUARY;
   }

   public Optional<UUID> getEncounterOwner() {
      return Optional.ofNullable(this.encounterOwner);
   }

   public void setEncounterOwner(UUID owner) {
      this.encounterOwner = owner;
      this.setPersistenceRequired();
   }

   public boolean isOwnedBy(Player player) {
      return player != null && this.encounterOwner != null && this.encounterOwner.equals(player.getUUID());
   }

   public boolean isPermitClaimed() {
      return this.entityData.get(DATA_PERMIT_CLAIMED);
   }

   public void setPermitClaimed(boolean claimed) {
      this.entityData.set(DATA_PERMIT_CLAIMED, claimed);
      if (claimed) {
         this.setEncounterState(EsilRadiruEntity.EncounterState.SANCTUARY);
      }
   }

   public void markPermitClaimed() {
      this.setPermitClaimed(true);
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 240.0)
         .add(Attributes.MOVEMENT_SPEED, 0.32)
         .add(Attributes.ATTACK_DAMAGE, 18.0)
         .add(Attributes.ARMOR, 12.0)
         .add(Attributes.ARMOR_TOUGHNESS, 4.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
         .add(Attributes.FOLLOW_RANGE, 40.0);
   }

   public enum EncounterState {
      HOSTILE(0),
      SURRENDERED(1),
      SANCTUARY(2);

      private final int id;

      EncounterState(int id) {
         this.id = id;
      }

      public int id() {
         return this.id;
      }

      public static EsilRadiruEntity.EncounterState fromId(int id) {
         return switch (id) {
            case 0 -> HOSTILE;
            case 2 -> SANCTUARY;
            default -> SURRENDERED;
         };
      }
   }
}
