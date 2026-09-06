package dev.eness.sololevelingfinal.core.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import dev.eness.sololevelingfinal.core.dungeon.DatapackDungeonGateHandler;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class DatapackGateEntity extends Portal1Entity {
   public static final String TEXTURE_NAME = "gate_zero_purple";

   public DatapackGateEntity(SpawnEntity packet, Level level) {
      this(SololevelingModEntities.DATAPACK_GATE.get(), level);
   }

   public DatapackGateEntity(EntityType<? extends DatapackGateEntity> type, Level level) {
      super(type, level);
      this.setTexture("gate_zero_purple");
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag entityTag
   ) {
      SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, entityTag);
      this.setTexture("gate_zero_purple");
      DatapackDungeonGateHandler.initializeSpawn(this, reason);
      return result;
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (this.level().isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      if (player instanceof ServerPlayer serverPlayer) {
         DatapackDungeonGateHandler.interact(serverPlayer, this);
      }

      return InteractionResult.CONSUME;
   }

   @Override
   public void baseTick() {
      super.baseTick();
      if (!this.level().isClientSide() && this.tickCount > 1) {
         DatapackDungeonGateHandler.discardInvalidUnboundSpawn(this);
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      DatapackDungeonGateHandler.writeAdditionalSaveData(this, tag);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setTexture("gate_zero_purple");
      DatapackDungeonGateHandler.readAdditionalSaveData(this, tag);
   }
}
