package dev.eness.sololevelingfinal.core.world.dimension;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.client.dimension.DkcDimensionSpecialEffects;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;

@EventBusSubscriber(modid = "sololeveling")
public class DungeonDimensionDKCDimension {
   private DungeonDimensionDKCDimension() {
   }

   @EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
   public static class DimensionSpecialEffectsHandler {
      private DimensionSpecialEffectsHandler() {
      }

      @SubscribeEvent
      @OnlyIn(Dist.CLIENT)
      public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
         event.register(DkcFloorRegistry.SHARED_DIMENSION.location(), new DkcDimensionSpecialEffects());
      }
   }
}
