package dev.eness.sololevelingfinal.core.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.eness.sololevelingfinal.core.block.entity.CustomPortalBlockEntity;

public class CustomPortalBlock extends Block implements EntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public CustomPortalBlock() {
      super(Properties.of().sound(SoundType.EMPTY).strength(-1.0F, 3600000.0F).lightLevel(s -> 7).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   public void appendHoverText(ItemStack itemstack, BlockGetter world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
   }

   @Override
   public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
      return 15;
   }

   @Override
   public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return context.getClickedFace().getAxis() == Axis.Y
         ? this.defaultBlockState().setValue(FACING, Direction.NORTH)
         : this.defaultBlockState().setValue(FACING, context.getClickedFace());
   }

   @Override
   public BlockState rotate(BlockState state, Rotation rot) {
      return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState state, Mirror mirrorIn) {
      return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      List<ItemStack> dropsOriginal = super.getDrops(state, builder);
      return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
   }

   @Override
   public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
      return worldIn.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CustomPortalBlockEntity(pos, state);
   }

   @Override
   public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
      super.triggerEvent(state, world, pos, eventID, eventParam);
      BlockEntity blockEntity = world.getBlockEntity(pos);
      return blockEntity == null ? false : blockEntity.triggerEvent(eventID, eventParam);
   }

   @Override
   public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
         if (world.getBlockEntity(pos) instanceof CustomPortalBlockEntity be) {
            Containers.dropContents(world, pos, be);
            world.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, world, pos, newState, isMoving);
      }
   }

   @Override
   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   @Override
   public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
      return world.getBlockEntity(pos) instanceof CustomPortalBlockEntity be ? AbstractContainerMenu.getRedstoneSignalFromContainer(be) : 0;
   }
}
