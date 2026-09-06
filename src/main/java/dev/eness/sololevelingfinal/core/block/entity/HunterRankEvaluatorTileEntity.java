package dev.eness.sololevelingfinal.core.block.entity;

import java.util.UUID;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlockEntities;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HunterRankEvaluatorTileEntity extends RandomizableContainerBlockEntity implements GeoBlockEntity, WorldlyContainer {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private NonNullList<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);
   private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
   private UUID publicPulseSession;
   private int publicPulseColor = 4179711;
   private float publicPulseIntensity;
   private int publicPulsePhase;
   private long publicPulseUntil;

   public HunterRankEvaluatorTileEntity(BlockPos pos, BlockState state) {
      super(SololevelingModBlockEntities.HUNTER_RANK_EVALUATOR.get(), pos, state);
   }

   private PlayState predicate(AnimationState event) {
      String animationprocedure = (
            this.getBlockState().getBlock().getStateDefinition().getProperty("animation") instanceof IntegerProperty _getip1
               ? this.getBlockState().getValue(_getip1)
               : 0
         )
         + "";
      return animationprocedure.equals("0") ? event.setAndContinue(RawAnimation.begin().thenLoop(animationprocedure)) : PlayState.STOP;
   }

   private PlayState procedurePredicate(AnimationState event) {
      String animationprocedure = (
            this.getBlockState().getBlock().getStateDefinition().getProperty("animation") instanceof IntegerProperty _getip1
               ? this.getBlockState().getValue(_getip1)
               : 0
         )
         + "";
      if (!animationprocedure.equals("0") && event.getController().getAnimationState() == State.STOPPED) {
         event.getController().setAnimation(RawAnimation.begin().thenPlay(animationprocedure));
         if (event.getController().getAnimationState() == State.STOPPED) {
            if (this.getBlockState().getBlock().getStateDefinition().getProperty("animation") instanceof IntegerProperty _integerProp) {
               this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(_integerProp, 0), 3);
            }

            event.getController().forceAnimationReset();
         }
      } else if (animationprocedure.equals("0")) {
         return PlayState.STOP;
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(ControllerRegistrar data) {
      data.add(new AnimationController<>(this, "controller", 0, this::predicate));
      data.add(new AnimationController<>(this, "procedurecontroller", 0, this::procedurePredicate));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      if (!this.tryLoadLootTable(compound)) {
         this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      }

      ContainerHelper.loadAllItems(compound, this.stacks);
      this.publicPulseSession = compound.hasUUID("EvaluationPulseSession") ? compound.getUUID("EvaluationPulseSession") : null;
      this.publicPulseColor = compound.getInt("EvaluationPulseColor");
      this.publicPulseIntensity = compound.getFloat("EvaluationPulseIntensity");
      this.publicPulsePhase = compound.getInt("EvaluationPulsePhase");
      this.publicPulseUntil = compound.getLong("EvaluationPulseUntil");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      if (!this.trySaveLootTable(compound)) {
         ContainerHelper.saveAllItems(compound, this.stacks);
      }

      if (this.publicPulseSession != null) {
         compound.putUUID("EvaluationPulseSession", this.publicPulseSession);
      }

      compound.putInt("EvaluationPulseColor", this.publicPulseColor);
      compound.putFloat("EvaluationPulseIntensity", this.publicPulseIntensity);
      compound.putInt("EvaluationPulsePhase", this.publicPulsePhase);
      compound.putLong("EvaluationPulseUntil", this.publicPulseUntil);
   }

   public void setPublicPulse(UUID sessionId, int color, float intensity, int phase, long until) {
      this.publicPulseSession = sessionId;
      this.publicPulseColor = color & 16777215;
      this.publicPulseIntensity = Math.max(0.0F, Math.min(1.0F, intensity));
      this.publicPulsePhase = Math.max(0, phase);
      this.publicPulseUntil = Math.max(0L, until);
      this.setChanged();
      if (this.level != null && !this.level.isClientSide()) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public boolean isPublicPulseOwner(UUID sessionId, long gameTime) {
      return sessionId != null && sessionId.equals(this.publicPulseSession) && gameTime <= this.publicPulseUntil;
   }

   public int getPublicPulseColor() {
      return this.publicPulseColor;
   }

   public float getPublicPulseIntensity() {
      return this.publicPulseIntensity;
   }

   public int getPublicPulsePhase() {
      return this.publicPulsePhase;
   }

   public long getPublicPulseUntil() {
      return this.publicPulseUntil;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag() {
      return this.saveWithFullMetadata();
   }

   @Override
   public int getContainerSize() {
      return this.stacks.size();
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack itemstack : this.stacks) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public Component getDefaultName() {
      return Component.literal("hunter_rank_evaluator");
   }

   @Override
   public int getMaxStackSize() {
      return 64;
   }

   @Override
   public AbstractContainerMenu createMenu(int id, Inventory inventory) {
      return ChestMenu.threeRows(id, inventory);
   }

   @Override
   public Component getDisplayName() {
      return Component.literal("Hunter Rank Evaluator");
   }

   @Override
   protected NonNullList<ItemStack> getItems() {
      return this.stacks;
   }

   @Override
   protected void setItems(NonNullList<ItemStack> stacks) {
      this.stacks = stacks;
   }

   @Override
   public boolean canPlaceItem(int index, ItemStack stack) {
      return true;
   }

   @Override
   public int[] getSlotsForFace(Direction side) {
      return IntStream.range(0, this.getContainerSize()).toArray();
   }

   @Override
   public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
      return this.canPlaceItem(index, stack);
   }

   @Override
   public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
      return true;
   }

   @Override
   public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
      return !this.remove && facing != null && capability == ForgeCapabilities.ITEM_HANDLER
         ? this.handlers[facing.ordinal()].cast()
         : super.getCapability(capability, facing);
   }

   @Override
   public void setRemoved() {
      super.setRemoved();

      for (LazyOptional<? extends IItemHandler> handler : this.handlers) {
         handler.invalidate();
      }
   }
}
