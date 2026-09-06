package dev.eness.sololevelingfinal.core.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.eness.sololevelingfinal.core.procedures.GoblinSpawnerUpdateTickProcedure;

public class GoblinSpawnerBlock extends Block {
   public GoblinSpawnerBlock() {
      super(
         Properties.of()
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.STONE)
            .strength(-1.0F, 3600000.0F)
            .noOcclusion()
            .isRedstoneConductor((bs, br, bp) -> false)
      );
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
   public List<ItemStack> getDrops(BlockState state, Builder builder) {
      List<ItemStack> dropsOriginal = super.getDrops(state, builder);
      return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
   }

   @Override
   public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
      super.onPlace(blockstate, world, pos, oldState, moving);
      world.scheduleTick(pos, this, 1);
   }

   @Override
   public void tick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
      super.tick(blockstate, world, pos, random);
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      GoblinSpawnerUpdateTickProcedure.execute(world, x, y, z);
      world.scheduleTick(pos, this, 1);
   }
}
