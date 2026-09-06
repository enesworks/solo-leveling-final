package dev.eness.sololevelingfinal.core.block;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public final class ManaCrystalDepositBlock extends Block {
   private static final VoxelShape SHAPE = Shapes.or(
      Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0),
      Block.box(5.0, 8.0, 5.0, 11.0, 16.0, 11.0),
      Block.box(2.0, 8.0, 3.0, 6.0, 14.0, 8.0),
      Block.box(10.0, 8.0, 8.0, 14.0, 14.0, 13.0)
   );

   public ManaCrystalDepositBlock() {
      super(
         Properties.of()
            .strength(4.2F, 9.0F)
            .sound(SoundType.AMETHYST_CLUSTER)
            .lightLevel(state -> 5)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false)
      );
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Override
   public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
      return Shapes.empty();
   }

   @Override
   public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (random.nextInt(18) == 0) {
         double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
         double y = pos.getY() + 0.62 + random.nextDouble() * 0.42;
         double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
         level.addParticle(SololevelingModParticleTypes.MANA_BLUE.get(), x, y, z, 0.0, 0.012, 0.0);
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("block.sololeveling.mana_crystal_deposit.tooltip").withStyle(ChatFormatting.AQUA));
      tooltip.add(Component.translatable("block.sololeveling.mana_crystal_deposit.hint").withStyle(ChatFormatting.DARK_GRAY));
   }
}
