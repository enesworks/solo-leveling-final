package dev.eness.sololevelingfinal.core.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
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

public class DungeonGrave1Block extends Block {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public DungeonGrave1Block() {
      super(
         Properties.of()
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.STONE)
            .strength(1.0F, 10.0F)
            .requiresCorrectToolForDrops()
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
            box(1.0, 0.0, 5.0, 15.0, 1.0, 12.0),
            box(2.0, 1.0, 6.0, 14.0, 4.0, 11.0),
            box(2.5, 4.0, 7.0, 13.5, 14.0, 10.0),
            box(4.5, 14.0, 7.0, 11.5, 19.0, 10.0),
            box(3.26777, 12.85355, 7.0, 4.66777, 15.70355, 10.0),
            box(3.52735, 19.0124, 7.0, 12.37735, 20.4124, 10.0),
            box(5.52735, 20.3624, 7.0, 10.37735, 22.4124, 10.0),
            box(11.33223, 12.85355, 7.0, 12.73223, 15.70355, 10.0)
         );
         case EAST -> Shapes.or(
            box(4.0, 0.0, 1.0, 11.0, 1.0, 15.0),
            box(5.0, 1.0, 2.0, 10.0, 4.0, 14.0),
            box(6.0, 4.0, 2.5, 9.0, 14.0, 13.5),
            box(6.0, 14.0, 4.5, 9.0, 19.0, 11.5),
            box(6.0, 12.85355, 3.26777, 9.0, 15.70355, 4.66777),
            box(6.0, 19.0124, 3.52735, 9.0, 20.4124, 12.37735),
            box(6.0, 20.3624, 5.52735, 9.0, 22.4124, 10.37735),
            box(6.0, 12.85355, 11.33223, 9.0, 15.70355, 12.73223)
         );
         case WEST -> Shapes.or(
            box(5.0, 0.0, 1.0, 12.0, 1.0, 15.0),
            box(6.0, 1.0, 2.0, 11.0, 4.0, 14.0),
            box(7.0, 4.0, 2.5, 10.0, 14.0, 13.5),
            box(7.0, 14.0, 4.5, 10.0, 19.0, 11.5),
            box(7.0, 12.85355, 11.33223, 10.0, 15.70355, 12.73223),
            box(7.0, 19.0124, 3.62265, 10.0, 20.4124, 12.47265),
            box(7.0, 20.3624, 5.62265, 10.0, 22.4124, 10.47265),
            box(7.0, 12.85355, 3.26777, 10.0, 15.70355, 4.66777)
         );
         default -> Shapes.or(
            box(1.0, 0.0, 4.0, 15.0, 1.0, 11.0),
            box(2.0, 1.0, 5.0, 14.0, 4.0, 10.0),
            box(2.5, 4.0, 6.0, 13.5, 14.0, 9.0),
            box(4.5, 14.0, 6.0, 11.5, 19.0, 9.0),
            box(11.33223, 12.85355, 6.0, 12.73223, 15.70355, 9.0),
            box(3.62265, 19.0124, 6.0, 12.47265, 20.4124, 9.0),
            box(5.62265, 20.3624, 6.0, 10.47265, 22.4124, 9.0),
            box(3.26777, 12.85355, 6.0, 4.66777, 15.70355, 9.0)
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
   public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
      return player.getInventory().getSelected().getItem() instanceof PickaxeItem tieredItem ? tieredItem.getTier().getLevel() >= 1 : false;
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      List<ItemStack> dropsOriginal = super.getDrops(state, builder);
      return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
   }
}
