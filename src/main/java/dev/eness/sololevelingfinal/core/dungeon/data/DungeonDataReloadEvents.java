package dev.eness.sololevelingfinal.core.dungeon.data;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE)
public final class DungeonDataReloadEvents {
   private DungeonDataReloadEvents() {
   }

   @SubscribeEvent
   public static void addReloadListener(AddReloadListenerEvent event) {
      event.addListener(DungeonDataManager.reloadListener(event.getConditionContext()));
   }

   @SubscribeEvent
   public static void serverStopped(ServerStoppedEvent event) {
      DungeonDataManager.clear();
   }
}
