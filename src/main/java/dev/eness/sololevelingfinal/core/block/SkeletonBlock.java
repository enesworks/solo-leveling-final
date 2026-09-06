package dev.eness.sololevelingfinal.core.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class SkeletonBlock extends Block {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public SkeletonBlock() {
      super(
         Properties.of()
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.BONE_BLOCK)
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
            box(4.0, 1.8, 9.0, 12.0, 7.8, 15.0),
            box(6.0, -0.2, 9.0, 10.0, 1.8, 12.0),
            box(7.0, 2.59603, 8.77878, 9.0, 4.59603, 9.77878),
            box(14.18009, -0.2, 3.9116, 17.18009, 2.2, 5.9116),
            box(6.68347, 0.0, -1.7661, 17.68347, 2.0, 0.2339),
            box(1.91056, -0.1, -0.9303, 3.91056, 2.1, 2.0697),
            box(3.16965, -0.2, 0.13671, 6.16965, 2.2, 2.13671),
            box(8.90295, -0.1, 9.06209, 10.90295, 2.1, 11.06209)
         );
         case EAST -> Shapes.or(
            box(1.0, 1.8, 4.0, 7.0, 7.8, 12.0),
            box(4.0, -0.2, 6.0, 7.0, 1.8, 10.0),
            box(6.22122, 2.59603, 7.0, 7.22122, 4.59603, 9.0),
            box(10.0884, -0.2, 14.18009, 12.0884, 2.2, 17.18009),
            box(15.7661, 0.0, 6.68347, 17.7661, 2.0, 17.68347),
            box(13.9303, -0.1, 1.91056, 16.9303, 2.1, 3.91056),
            box(13.86329, -0.2, 3.16965, 15.86329, 2.2, 6.16965),
            box(4.93791, -0.1, 8.90295, 6.93791, 2.1, 10.90295)
         );
         case WEST -> Shapes.or(
            box(9.0, 1.8, 4.0, 15.0, 7.8, 12.0),
            box(9.0, -0.2, 6.0, 12.0, 1.8, 10.0),
            box(8.77878, 2.59603, 7.0, 9.77878, 4.59603, 9.0),
            box(3.9116, -0.2, -1.18009, 5.9116, 2.2, 1.81991),
            box(-1.7661, 0.0, -1.68347, 0.2339, 2.0, 9.31653),
            box(-0.9303, -0.1, 12.08944, 2.0697, 2.1, 14.08944),
            box(0.13671, -0.2, 9.83035, 2.13671, 2.2, 12.83035),
            box(9.06209, -0.1, 5.09705, 11.06209, 2.1, 7.09705)
         );
         default -> Shapes.or(
            box(4.0, 1.8, 1.0, 12.0, 7.8, 7.0),
            box(6.0, -0.2, 4.0, 10.0, 1.8, 7.0),
            box(7.0, 2.59603, 6.22122, 9.0, 4.59603, 7.22122),
            box(-1.18009, -0.2, 10.0884, 1.81991, 2.2, 12.0884),
            box(-1.68347, 0.0, 15.7661, 9.31653, 2.0, 17.7661),
            box(12.08944, -0.1, 13.9303, 14.08944, 2.1, 16.9303),
            box(9.83035, -0.2, 13.86329, 12.83035, 2.2, 15.86329),
            box(5.09705, -0.1, 4.93791, 7.09705, 2.1, 6.93791)
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
      return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(Items.BONE));
   }
}
