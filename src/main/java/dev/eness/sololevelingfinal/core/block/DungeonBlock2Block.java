package dev.eness.sololevelingfinal.core.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootParams.Builder;

public class DungeonBlock2Block extends Block {
   public DungeonBlock2Block() {
      super(
         Properties.of()
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.EMPTY)
            .strength(-1.0F, 3600000.0F)
            .lightLevel(s -> 1)
            .noCollission()
            .hasPostProcess((bs, br, bp) -> true)
            .emissiveRendering((bs, br, bp) -> true)
      );
   }

   @Override
   public void appendHoverText(ItemStack itemstack, BlockGetter world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
   }

   @Override
   public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
      return adjacentBlockState.getBlock() == this ? true : super.skipRendering(state, adjacentBlockState, side);
   }

   @Override
   public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
      return 15;
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, Builder builder) {
      List<ItemStack> dropsOriginal = super.getDrops(state, builder);
      return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
   }
}
