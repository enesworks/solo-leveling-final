package dev.eness.sololevelingfinal.core;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public class DisableHealthbar {
   @SubscribeEvent
   public static void RenderHealthBar(Pre event) {
      Minecraft minecraft = Minecraft.getInstance();
      Entity entity = minecraft.player;
      if (entity != null) {
         if (!minecraft.options.renderDebug
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).CustomHUD
            && VanillaGuiOverlay.PLAYER_HEALTH.type() == event.getOverlay()) {
            event.setCanceled(true);
         }
      }
   }
}
