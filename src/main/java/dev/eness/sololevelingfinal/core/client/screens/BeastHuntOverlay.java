package dev.eness.sololevelingfinal.core.client.screens;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.util.BeastHuntClientState;

@EventBusSubscriber(Dist.CLIENT)
public final class BeastHuntOverlay {
   private BeastHuntOverlay() {
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      BeastHuntClientState.clear();
   }
}
