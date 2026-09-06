package dev.eness.sololevelingfinal.core.world.dimension.rift;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "sololeveling")
public final class DimensionalRiftDimension {
   public static final ResourceLocation ID = new ResourceLocation("sololeveling", "dimensional_rift");
   public static final ResourceKey<Level> LEVEL_KEY = ResourceKey.create(Registries.DIMENSION, ID);

   private DimensionalRiftDimension() {
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      if (LEVEL_KEY.equals(event.getTo()) && event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
         DimensionalRiftEntry.ensurePlatform(level);
         if (RiftGeometry.distance(player.getX(), player.getZ()) > 3500.0 || player.getY() < level.getMinBuildHeight()) {
            DimensionalRiftEntry.teleportToCenter(player, level);
         }
      }
   }

   @EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
   public static final class Effects {
      private Effects() {
      }

      @SubscribeEvent
      @OnlyIn(Dist.CLIENT)
      public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
         DimensionSpecialEffects effects = new DimensionSpecialEffects(Float.NaN, true, SkyType.END, false, false) {
            @Override
            public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
               return color.multiply(0.48, 0.38, 0.58);
            }

            @Override
            public boolean isFoggyAt(int x, int y) {
               return false;
            }
         };
         event.register(DimensionalRiftDimension.ID, effects);
      }
   }
}
