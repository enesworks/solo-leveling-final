package dev.eness.sololevelingfinal.core.client.renderer.layer;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
public final class GoliathArmorGlowLayerRegistration {
   private GoliathArmorGlowLayerRegistration() {
   }

   @SubscribeEvent
   public static void addPlayerLayers(AddLayers event) {
      for (String skin : event.getSkins()) {
         PlayerRenderer renderer = event.getSkin(skin);
         if (renderer != null) {
            renderer.addLayer(new GoliathArmorGlowLayer(renderer, event.getEntityModels()));
         }
      }
   }
}
