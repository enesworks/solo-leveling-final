package dev.eness.sololevelingfinal.core.world.features.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator.Context;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;

@EventBusSubscriber(bus = Bus.MOD)
public class DungeonBiomeSnowFruitDecorator extends CocoaDecorator {
   public static Codec<DungeonBiomeSnowFruitDecorator> CODEC = Codec.unit(DungeonBiomeSnowFruitDecorator::new);
   public static TreeDecoratorType<?> DECORATOR_TYPE = new TreeDecoratorType(CODEC);

   @SubscribeEvent
   public static void registerPointOfInterest(RegisterEvent event) {
      event.register(Keys.TREE_DECORATOR_TYPES, registerHelper -> registerHelper.register("dungeon_biome_snow_tree_fruit_decorator", DECORATOR_TYPE));
   }

   public DungeonBiomeSnowFruitDecorator() {
      super(0.2F);
   }

   @Override
   protected TreeDecoratorType<?> type() {
      return DECORATOR_TYPE;
   }

   @Override
   public void place(Context context) {
      RandomSource randomsource = context.random();
      if (!(randomsource.nextFloat() >= 0.2F)) {
         List<BlockPos> list = context.logs();
         int i = list.get(0).getY();
         list.stream().filter(p_69980_ -> p_69980_.getY() - i <= 2).forEach(p_226026_ -> {
            for (Direction direction : Plane.HORIZONTAL) {
               if (randomsource.nextFloat() <= 0.25F) {
                  Direction direction1 = direction.getOpposite();
                  BlockPos blockpos = p_226026_.offset(direction1.getStepX(), 0, direction1.getStepZ());
                  if (context.isAir(blockpos)) {
                     context.setBlock(blockpos, Blocks.SNOW_BLOCK.defaultBlockState());
                  }
               }
            }
         });
      }
   }
}
