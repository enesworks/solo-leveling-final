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

public class WoodenPassageOpenBlock extends Block {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public WoodenPassageOpenBlock() {
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
            box(-8.7, 27.0, 4.0, 26.3, 32.0, 12.0),
            box(-5.07989, 6.0573, 5.0, 0.92011, 17.0573, 11.0),
            box(-8.55668, -1.42173, 5.1, -2.55668, 8.57827, 10.9),
            box(-8.45, -5.88539, 5.0, -2.45, -0.88539, 11.0),
            box(-8.45, 24.0, 5.0, -2.45, 27.0, 11.0),
            box(-9.70474, 14.30798, 5.1, -3.70474, 24.30798, 10.9),
            box(20.17011, -5.88539, 5.0, 26.17011, -0.88539, 11.0),
            box(20.39847, -2.03344, 5.1, 26.39847, 7.96656, 10.9),
            box(16.8, 6.0573, 5.0, 22.8, 17.0573, 11.0),
            box(21.16384, 14.99581, 5.1, 27.16384, 24.99581, 10.9),
            box(20.17011, 24.0, 5.0, 26.17011, 27.0, 11.0)
         );
         case EAST -> Shapes.or(
            box(4.0, 27.0, -8.7, 12.0, 32.0, 26.3),
            box(5.0, 6.0573, -5.07989, 11.0, 17.0573, 0.92011),
            box(5.1, -1.42173, -8.55668, 10.9, 8.57827, -2.55668),
            box(5.0, -5.88539, -8.45, 11.0, -0.88539, -2.45),
            box(5.0, 24.0, -8.45, 11.0, 27.0, -2.45),
            box(5.1, 14.30798, -9.70474, 10.9, 24.30798, -3.70474),
            box(5.0, -5.88539, 20.17011, 11.0, -0.88539, 26.17011),
            box(5.1, -2.03344, 20.39847, 10.9, 7.96656, 26.39847),
            box(5.0, 6.0573, 16.8, 11.0, 17.0573, 22.8),
            box(5.1, 14.99581, 21.16384, 10.9, 24.99581, 27.16384),
            box(5.0, 24.0, 20.17011, 11.0, 27.0, 26.17011)
         );
         case WEST -> Shapes.or(
            box(4.0, 27.0, -10.3, 12.0, 32.0, 24.7),
            box(5.0, 6.0573, 15.07989, 11.0, 17.0573, 21.07989),
            box(5.1, -1.42173, 18.55668, 10.9, 8.57827, 24.55668),
            box(5.0, -5.88539, 18.45, 11.0, -0.88539, 24.45),
            box(5.0, 24.0, 18.45, 11.0, 27.0, 24.45),
            box(5.1, 14.30798, 19.70474, 10.9, 24.30798, 25.70474),
            box(5.0, -5.88539, -10.17011, 11.0, -0.88539, -4.17011),
            box(5.1, -2.03344, -10.39847, 10.9, 7.96656, -4.39847),
            box(5.0, 6.0573, -6.8, 11.0, 17.0573, -0.8),
            box(5.1, 14.99581, -11.16384, 10.9, 24.99581, -5.16384),
            box(5.0, 24.0, -10.17011, 11.0, 27.0, -4.17011)
         );
         default -> Shapes.or(
            box(-10.3, 27.0, 4.0, 24.7, 32.0, 12.0),
            box(15.07989, 6.0573, 5.0, 21.07989, 17.0573, 11.0),
            box(18.55668, -1.42173, 5.1, 24.55668, 8.57827, 10.9),
            box(18.45, -5.88539, 5.0, 24.45, -0.88539, 11.0),
            box(18.45, 24.0, 5.0, 24.45, 27.0, 11.0),
            box(19.70474, 14.30798, 5.1, 25.70474, 24.30798, 10.9),
            box(-10.17011, -5.88539, 5.0, -4.17011, -0.88539, 11.0),
            box(-10.39847, -2.03344, 5.1, -4.39847, 7.96656, 10.9),
            box(-6.8, 6.0573, 5.0, -0.8, 17.0573, 11.0),
            box(-11.16384, 14.99581, 5.1, -5.16384, 24.99581, 10.9),
            box(-10.17011, 24.0, 5.0, -4.17011, 27.0, 11.0)
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
