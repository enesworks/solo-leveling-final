package dev.eness.sololevelingfinal.core.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public final class LiuBetterCombatBootstrap {
   private LiuBetterCombatBootstrap() {
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      if (ModList.get().isLoaded("bettercombat")) {
         event.enqueueWork(() -> {
            try {
               Class.forName("dev.eness.sololevelingfinal.core.client.compat.bettercombat.LiuBetterCombatCompat").getMethod("register").invoke(null);
               Class.forName("dev.eness.sololevelingfinal.core.client.compat.bettercombat.SungIlHwanBetterCombatCompat").getMethod("register").invoke(null);
            } catch (ReflectiveOperationException exception) {
               throw new IllegalStateException("Unable to register vessel Better Combat hooks", exception);
            }
         });
      }
   }
}
