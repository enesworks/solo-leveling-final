package dev.eness.sololevelingfinal.core;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlockEntities;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class BlockEntityRendererRegistry {
   @SubscribeEvent
   public static void registerEntityRenderers(RegisterRenderers event) {
      event.registerBlockEntityRenderer(SololevelingModBlockEntities.CUSTOM_PORTAL.get(), CustomPortalBlockEntityRenderer::new);
   }
}
