package dev.eness.sololevelingfinal.core.world.dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber
public class DungeonDimensionKasakaDimension {
   @EventBusSubscriber(bus = Bus.MOD)
   public static class DimensionSpecialEffectsHandler {
      @SubscribeEvent
      @OnlyIn(Dist.CLIENT)
      public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
         DimensionSpecialEffects customEffect = new DimensionSpecialEffects(Float.NaN, true, SkyType.NONE, false, false) {
            @Override
            public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
               return new Vec3(0.0, 0.0, 0.0);
            }

            @Override
            public boolean isFoggyAt(int x, int y) {
               return false;
            }
         };
         event.register(new ResourceLocation("sololeveling:dungeon_dimension_kasaka"), customEffect);
      }
   }
}
