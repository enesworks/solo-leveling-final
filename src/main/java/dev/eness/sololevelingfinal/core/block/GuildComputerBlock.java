package dev.eness.sololevelingfinal.core.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import dev.eness.sololevelingfinal.core.block.entity.GuildComputerBlockEntity;

public class GuildComputerBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public GuildComputerBlock() {
      super(Properties.of().sound(SoundType.METAL).strength(3.5F, 12.0F));
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new GuildComputerBlockEntity(pos, state);
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   @Override
   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (world.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else if (world.getBlockEntity(pos) instanceof GuildComputerBlockEntity computer) {
         NetworkHooks.openScreen((ServerPlayer)player, computer, buf -> computer.writeScreenOpeningData(player, buf));
         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      return List.of(this.buildDrop(be));
   }

   @Override
   public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
      return this.buildDrop(level.getBlockEntity(pos));
   }

   @Override
   public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
      if (!world.isClientSide() && player.isCreative()) {
         BlockEntity be = world.getBlockEntity(pos);
         ItemStack drop = this.buildDrop(be);
         ItemEntity ie = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
         ie.setDefaultPickUpDelay();
         world.addFreshEntity(ie);
      }

      super.playerWillDestroy(world, pos, state, player);
   }

   @Override
   public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         super.onRemove(state, world, pos, newState, isMoving);
      }
   }

   private ItemStack buildDrop(@Nullable BlockEntity be) {
      ItemStack stack = new ItemStack(this);
      if (be instanceof GuildComputerBlockEntity computer) {
         CompoundTag beTag = computer.saveWithoutMetadata();
         if (!beTag.isEmpty()) {
            stack.addTagElement("BlockEntityTag", beTag);
         }
      }

      return stack;
   }
}
