package dev.eness.sololevelingfinal.core.world.features.treedecorators;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator.Context;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;

@EventBusSubscriber(bus = Bus.MOD)
public class DungeonBiomeSnowLeaveDecorator extends LeaveVineDecorator {
   public static Codec<LeaveVineDecorator> CODEC = Codec.unit(DungeonBiomeSnowLeaveDecorator::new);
   public static TreeDecoratorType<?> DECORATOR_TYPE = new TreeDecoratorType(CODEC);

   @SubscribeEvent
   public static void registerPointOfInterest(RegisterEvent event) {
      event.register(Keys.TREE_DECORATOR_TYPES, registerHelper -> registerHelper.register("dungeon_biome_snow_tree_leave_decorator", DECORATOR_TYPE));
   }

   public DungeonBiomeSnowLeaveDecorator() {
      super(0.25F);
   }

   @Override
   protected TreeDecoratorType<?> type() {
      return DECORATOR_TYPE;
   }

   @Override
   public void place(Context context) {
      context.leaves().forEach(blockpos -> {
         if (context.random().nextFloat() < 0.25F) {
            BlockPos pos = blockpos.west();
            if (context.isAir(pos)) {
               addVine(pos, context);
            }
         }

         if (context.random().nextFloat() < 0.25F) {
            BlockPos pos = blockpos.east();
            if (context.isAir(pos)) {
               addVine(pos, context);
            }
         }

         if (context.random().nextFloat() < 0.25F) {
            BlockPos pos = blockpos.north();
            if (context.isAir(pos)) {
               addVine(pos, context);
            }
         }

         if (context.random().nextFloat() < 0.25F) {
            BlockPos pos = blockpos.south();
            if (context.isAir(pos)) {
               addVine(pos, context);
            }
         }
      });
   }

   private static void addVine(BlockPos pos, Context context) {
      context.setBlock(pos, Blocks.SNOW_BLOCK.defaultBlockState());
      int i = 4;

      for (BlockPos blockpos = pos.below(); context.isAir(blockpos) && i > 0; i--) {
         context.setBlock(blockpos, Blocks.SNOW_BLOCK.defaultBlockState());
         blockpos = blockpos.below();
      }
   }
}
