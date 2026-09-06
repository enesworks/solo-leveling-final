package dev.eness.sololevelingfinal.core.world.dimension;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.client.dimension.SnowDungeonSpecialEffects;

@EventBusSubscriber(modid = "sololeveling")
public class DungeonDimensionSnowDimension {
   private static final ResourceLocation EFFECTS_ID = new ResourceLocation("sololeveling", "dungeon_dimension_snow");

   private DungeonDimensionSnowDimension() {
   }

   @EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
   public static class DimensionSpecialEffectsHandler {
      private DimensionSpecialEffectsHandler() {
      }

      @SubscribeEvent
      @OnlyIn(Dist.CLIENT)
      public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
         event.register(DungeonDimensionSnowDimension.EFFECTS_ID, new SnowDungeonSpecialEffects());
      }
   }
}
