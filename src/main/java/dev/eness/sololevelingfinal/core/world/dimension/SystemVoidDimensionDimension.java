package dev.eness.sololevelingfinal.core.world.dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.procedures.SystemVoidDimensionPlayerEntersDimensionProcedure;
import dev.eness.sololevelingfinal.core.procedures.SystemVoidDimensionPlayerLeavesDimensionProcedure;

@EventBusSubscriber
public class SystemVoidDimensionDimension {
   @SubscribeEvent
   public static void onPlayerChangedDimensionEvent(PlayerChangedDimensionEvent event) {
      Entity entity = event.getEntity();
      Level world = entity.level();
      double x = entity.getX();
      double y = entity.getY();
      double z = entity.getZ();
      if (event.getFrom() == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:system_void_dimension"))) {
         SystemVoidDimensionPlayerLeavesDimensionProcedure.execute(entity);
      }

      if (event.getTo() == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:system_void_dimension"))) {
         SystemVoidDimensionPlayerEntersDimensionProcedure.execute(entity);
      }
   }

   @EventBusSubscriber(bus = Bus.MOD)
   public static class DimensionSpecialEffectsHandler {
      @SubscribeEvent
      @OnlyIn(Dist.CLIENT)
      public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
         DimensionSpecialEffects customEffect = new DimensionSpecialEffects(Float.NaN, true, SkyType.NONE, false, false) {
            @Override
            public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
               return color;
            }

            @Override
            public boolean isFoggyAt(int x, int y) {
               return false;
            }
         };
         event.register(new ResourceLocation("sololeveling:system_void_dimension"), customEffect);
      }
   }
}
