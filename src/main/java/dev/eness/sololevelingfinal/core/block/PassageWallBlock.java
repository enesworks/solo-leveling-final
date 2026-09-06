package dev.eness.sololevelingfinal.core.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PassageWallBlock extends Block {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public PassageWallBlock() {
      super(
         Properties.of()
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.STONE)
            .strength(1.0F, 10.0F)
            .noOcclusion()
            .isRedstoneConductor((bs, br, bp) -> false)
      );
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   public void appendHoverText(ItemStack itemstack, BlockGetter world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
   }

   @Override
   public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
      return true;
   }

   @Override
   public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
      return 0;
   }

   @Override
   public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return switch ((Direction)state.getValue(FACING)) {
         case NORTH -> Shapes.or(
            box(-16.0, 0.0, 5.0, 32.0, 16.0, 11.0),
            box(-16.0, -16.0, 5.0, 32.0, 0.0, 11.0),
            box(-16.0, 16.0, 5.0, 32.0, 32.0, 11.0),
            box(0.0, -32.0, 0.0, 16.0, -16.0, 16.0)
         );
         case EAST -> Shapes.or(
            box(5.0, 0.0, -16.0, 11.0, 16.0, 32.0),
            box(5.0, -16.0, -16.0, 11.0, 0.0, 32.0),
            box(5.0, 16.0, -16.0, 11.0, 32.0, 32.0),
            box(0.0, -32.0, 0.0, 16.0, -16.0, 16.0)
         );
         case WEST -> Shapes.or(
            box(5.0, 0.0, -16.0, 11.0, 16.0, 32.0),
            box(5.0, -16.0, -16.0, 11.0, 0.0, 32.0),
            box(5.0, 16.0, -16.0, 11.0, 32.0, 32.0),
            box(0.0, -32.0, 0.0, 16.0, -16.0, 16.0)
         );
         default -> Shapes.or(
            box(-16.0, 0.0, 5.0, 32.0, 16.0, 11.0),
            box(-16.0, -16.0, 5.0, 32.0, 0.0, 11.0),
            box(-16.0, 16.0, 5.0, 32.0, 32.0, 11.0),
            box(0.0, -32.0, 0.0, 16.0, -16.0, 16.0)
         );
      };
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
}
