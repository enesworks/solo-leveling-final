package dev.eness.sololevelingfinal.core.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.eness.sololevelingfinal.core.procedures.CellDoorOpenOnBlockRightClickedProcedure;

public class CellDoorOpenBlock extends Block {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public CellDoorOpenBlock() {
      super(Properties.of().sound(SoundType.METAL).strength(1.0F, 10.0F).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
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
            box(0.0, 27.0, 6.0, 16.0, 30.0, 10.0),
            box(0.0, 30.0, 5.0, 16.0, 32.0, 11.0),
            box(14.2, -16.0, -5.8, 15.2, 27.0, -4.8),
            box(14.2, -16.0, -1.8, 15.2, 27.0, -0.8),
            box(14.2, -16.0, 2.2, 15.2, 27.0, 3.2),
            box(14.2, -16.0, 6.2, 15.2, 27.0, 7.2),
            box(15.0, -14.0, 6.0, 16.0, 27.0, 10.0),
            box(15.0, -16.0, 5.0, 16.0, -14.0, 11.0),
            box(0.0, -14.0, 6.0, 1.0, 27.0, 10.0),
            box(0.0, -16.0, 5.0, 1.0, -14.0, 11.0),
            box(14.8, 0.0, -4.8, 15.8, 4.0, -1.8)
         );
         case EAST -> Shapes.or(
            box(6.0, 27.0, 0.0, 10.0, 30.0, 16.0),
            box(5.0, 30.0, 0.0, 11.0, 32.0, 16.0),
            box(20.8, -16.0, 14.2, 21.8, 27.0, 15.2),
            box(16.8, -16.0, 14.2, 17.8, 27.0, 15.2),
            box(12.8, -16.0, 14.2, 13.8, 27.0, 15.2),
            box(8.8, -16.0, 14.2, 9.8, 27.0, 15.2),
            box(6.0, -14.0, 15.0, 10.0, 27.0, 16.0),
            box(5.0, -16.0, 15.0, 11.0, -14.0, 16.0),
            box(6.0, -14.0, 0.0, 10.0, 27.0, 1.0),
            box(5.0, -16.0, 0.0, 11.0, -14.0, 1.0),
            box(17.8, 0.0, 14.8, 20.8, 4.0, 15.8)
         );
         case WEST -> Shapes.or(
            box(6.0, 27.0, 0.0, 10.0, 30.0, 16.0),
            box(5.0, 30.0, 0.0, 11.0, 32.0, 16.0),
            box(-5.8, -16.0, 0.8, -4.8, 27.0, 1.8),
            box(-1.8, -16.0, 0.8, -0.8, 27.0, 1.8),
            box(2.2, -16.0, 0.8, 3.2, 27.0, 1.8),
            box(6.2, -16.0, 0.8, 7.2, 27.0, 1.8),
            box(6.0, -14.0, 0.0, 10.0, 27.0, 1.0),
            box(5.0, -16.0, 0.0, 11.0, -14.0, 1.0),
            box(6.0, -14.0, 15.0, 10.0, 27.0, 16.0),
            box(5.0, -16.0, 15.0, 11.0, -14.0, 16.0),
            box(-4.8, 0.0, 0.2, -1.8, 4.0, 1.2)
         );
         default -> Shapes.or(
            box(0.0, 27.0, 6.0, 16.0, 30.0, 10.0),
            box(0.0, 30.0, 5.0, 16.0, 32.0, 11.0),
            box(0.8, -16.0, 20.8, 1.8, 27.0, 21.8),
            box(0.8, -16.0, 16.8, 1.8, 27.0, 17.8),
            box(0.8, -16.0, 12.8, 1.8, 27.0, 13.8),
            box(0.8, -16.0, 8.8, 1.8, 27.0, 9.8),
            box(0.0, -14.0, 6.0, 1.0, 27.0, 10.0),
            box(0.0, -16.0, 5.0, 1.0, -14.0, 11.0),
            box(15.0, -14.0, 6.0, 16.0, 27.0, 10.0),
            box(15.0, -16.0, 5.0, 16.0, -14.0, 11.0),
            box(0.2, 0.0, 17.8, 1.2, 4.0, 20.8)
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

   @Override
   public InteractionResult use(BlockState blockstate, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hit) {
      super.use(blockstate, world, pos, entity, hand, hit);
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      double hitX = hit.getLocation().x;
      double hitY = hit.getLocation().y;
      double hitZ = hit.getLocation().z;
      Direction direction = hit.getDirection();
      CellDoorOpenOnBlockRightClickedProcedure.execute(world, x, y, z);
      return InteractionResult.SUCCESS;
   }
}
