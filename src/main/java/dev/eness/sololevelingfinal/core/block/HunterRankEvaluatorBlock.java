package dev.eness.sololevelingfinal.core.block;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlockEntities;
import dev.eness.sololevelingfinal.core.procedures.RankEvaluatorOnBlockRightClickedProcedure;

public class HunterRankEvaluatorBlock extends BaseEntityBlock implements EntityBlock {
   public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 1);
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public HunterRankEvaluatorBlock() {
      super(Properties.of().sound(SoundType.METAL).strength(-1.0F, 3600000.0F).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
      return SololevelingModBlockEntities.HUNTER_RANK_EVALUATOR.get().create(blockPos, blockState);
   }

   @Override
   public void appendHoverText(ItemStack itemstack, BlockGetter world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("Right-click the crystal to begin Hunter Evaluation."));
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
   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return switch ((Direction)state.getValue(FACING)) {
         case NORTH -> Shapes.or(
            box(-14.5, 0.0, -14.5, 30.5, 2.0, 30.5),
            box(30.5, 0.0, -16.0, 32.0, 4.75, 32.0),
            box(-16.0, 0.0, -16.0, -14.5, 4.75, 32.0),
            box(-14.5, 2.0, 30.5, 30.5, 4.75, 32.0),
            box(-14.5, 4.75, -16.0, 30.5, 7.0, -14.5),
            box(0.5, 2.0, 1.0, 15.5, 4.0, 15.0),
            box(-13.0, 2.0, -13.0, 29.0, 2.75, 29.0),
            box(-8.0, 2.5, -7.25, 23.25, 3.75, 23.75),
            box(7.204, 0.0, 3.25, 8.796, 16.75, 13.0),
            box(7.204, 0.0, 4.0, 8.796, 16.75, 12.0),
            box(7.204, 0.0, 1.5, 8.796, 17.5, 14.5),
            box(7.204, 0.0, 4.0, 8.796, 16.75, 12.0),
            box(7.204, 0.0, 3.25, 8.796, 16.75, 13.0),
            box(4.0, 0.0, 7.204, 12.0, 16.75, 8.796),
            box(1.5, 0.0, 7.204, 15.0, 17.5, 8.796),
            box(4.0, 0.0, 7.204, 12.0, 16.75, 8.796),
            box(-6.5, 17.75, -6.75, 22.75, 45.75, 22.25)
         );
         case EAST -> Shapes.or(
            box(-14.5, 0.0, -14.5, 30.5, 2.0, 30.5),
            box(-16.0, 0.0, 30.5, 32.0, 4.75, 32.0),
            box(-16.0, 0.0, -16.0, 32.0, 4.75, -14.5),
            box(-16.0, 2.0, -14.5, -14.5, 4.75, 30.5),
            box(30.5, 4.75, -14.5, 32.0, 7.0, 30.5),
            box(1.0, 2.0, 0.5, 15.0, 4.0, 15.5),
            box(-13.0, 2.0, -13.0, 29.0, 2.75, 29.0),
            box(-7.75, 2.5, -8.0, 23.25, 3.75, 23.25),
            box(3.0, 0.0, 7.204, 12.75, 16.75, 8.796),
            box(4.0, 0.0, 7.204, 12.0, 16.75, 8.796),
            box(1.5, 0.0, 7.204, 14.5, 17.5, 8.796),
            box(4.0, 0.0, 7.204, 12.0, 16.75, 8.796),
            box(3.0, 0.0, 7.204, 12.75, 16.75, 8.796),
            box(7.204, 0.0, 4.0, 8.796, 16.75, 12.0),
            box(7.204, 0.0, 1.5, 8.796, 17.5, 15.0),
            box(7.204, 0.0, 4.0, 8.796, 16.75, 12.0),
            box(-6.25, 17.75, -6.5, 22.75, 45.75, 22.75)
         );
         case WEST -> Shapes.or(
            box(-14.5, 0.0, -14.5, 30.5, 2.0, 30.5),
            box(-16.0, 0.0, -16.0, 32.0, 4.75, -14.5),
            box(-16.0, 0.0, 30.5, 32.0, 4.75, 32.0),
            box(30.5, 2.0, -14.5, 32.0, 4.75, 30.5),
            box(-16.0, 4.75, -14.5, -14.5, 7.0, 30.5),
            box(1.0, 2.0, 0.5, 15.0, 4.0, 15.5),
            box(-13.0, 2.0, -13.0, 29.0, 2.75, 29.0),
            box(-7.25, 2.5, -7.25, 23.75, 3.75, 24.0),
            box(3.25, 0.0, 7.204, 13.0, 16.75, 8.796),
            box(4.0, 0.0, 7.204, 12.0, 16.75, 8.796),
            box(1.5, 0.0, 7.204, 14.5, 17.5, 8.796),
            box(4.0, 0.0, 7.204, 12.0, 16.75, 8.796),
            box(3.25, 0.0, 7.204, 13.0, 16.75, 8.796),
            box(7.204, 0.0, 4.0, 8.796, 16.75, 12.0),
            box(7.204, 0.0, 1.0, 8.796, 17.5, 14.5),
            box(7.204, 0.0, 4.0, 8.796, 16.75, 12.0),
            box(-6.75, 17.75, -6.75, 22.25, 45.75, 22.5)
         );
         default -> Shapes.or(
            box(-14.5, 0.0, -14.5, 30.5, 2.0, 30.5),
            box(-16.0, 0.0, -16.0, -14.5, 4.75, 32.0),
            box(30.5, 0.0, -16.0, 32.0, 4.75, 32.0),
            box(-14.5, 2.0, -16.0, 30.5, 4.75, -14.5),
            box(-14.5, 4.75, 30.5, 30.5, 7.0, 32.0),
            box(0.5, 2.0, 1.0, 15.5, 4.0, 15.0),
            box(-13.0, 2.0, -13.0, 29.0, 2.75, 29.0),
            box(-7.25, 2.5, -7.75, 24.0, 3.75, 23.25),
            box(7.204, 0.0, 3.0, 8.796, 16.75, 12.75),
            box(7.204, 0.0, 4.0, 8.796, 16.75, 12.0),
            box(7.204, 0.0, 1.5, 8.796, 17.5, 14.5),
            box(7.204, 0.0, 4.0, 8.796, 16.75, 12.0),
            box(7.204, 0.0, 3.0, 8.796, 16.75, 12.75),
            box(4.0, 0.0, 7.204, 12.0, 16.75, 8.796),
            box(1.0, 0.0, 7.204, 14.5, 17.5, 8.796),
            box(4.0, 0.0, 7.204, 12.0, 16.75, 8.796),
            box(-6.75, 17.75, -6.25, 22.5, 45.75, 22.75)
         );
      };
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(ANIMATION, FACING);
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
      RankEvaluatorOnBlockRightClickedProcedure.execute(world, x, y, z, entity);
      return InteractionResult.sidedSuccess(world.isClientSide());
   }
}
