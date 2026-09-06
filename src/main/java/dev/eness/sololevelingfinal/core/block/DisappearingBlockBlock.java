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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.BlockHitResult;
import dev.eness.sololevelingfinal.core.procedures.DisappearingBlockOnBlockRightClickedProcedure;

public class DisappearingBlockBlock extends Block {
   public DisappearingBlockBlock() {
      super(
         Properties.of()
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.METAL)
            .strength(10.0F, 0.0F)
            .hasPostProcess((bs, br, bp) -> true)
            .emissiveRendering((bs, br, bp) -> true)
      );
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
   public List<ItemStack> getDrops(BlockState state, Builder builder) {
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
      DisappearingBlockOnBlockRightClickedProcedure.execute(world, x, y, z);
      return InteractionResult.SUCCESS;
   }
}
