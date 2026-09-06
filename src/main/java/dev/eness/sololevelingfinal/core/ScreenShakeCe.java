package dev.eness.sololevelingfinal.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;

@EventBusSubscriber(bus = Bus.MOD)
public class ScreenShakeCe {
   @SubscribeEvent
   public static void init(FMLCommonSetupEvent event) {
      new ScreenShakeCe();
   }

   @EventBusSubscriber
   private static class ForgeBusEvents {
      @SubscribeEvent
      public static void serverLoad(ServerStartingEvent event) {
      }

      @OnlyIn(Dist.CLIENT)
      @SubscribeEvent
      public static void clientLoad(FMLClientSetupEvent event) {
      }

      @OnlyIn(Dist.CLIENT)
      @SubscribeEvent
      public static void CameraShake(ComputeCameraAngles event) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null && player.hasEffect(SololevelingModMobEffects.SCREEN_SHAKE.get())) {
            if (Math.random() < 0.5) {
               event.setPitch((float)(event.getPitch() + Math.random() * 2.0));
               event.setRoll((float)(event.getRoll() + Math.random() * 2.0));
               event.setYaw((float)(event.getYaw() + Math.random() * 2.0));
            } else {
               event.setPitch((float)(event.getPitch() - Math.random() * 2.0));
               event.setRoll((float)(event.getRoll() - Math.random() * 2.0));
               event.setYaw((float)(event.getYaw() - Math.random() * 2.0));
            }
         }
      }
   }
}
