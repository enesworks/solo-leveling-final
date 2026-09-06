package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class JoiningPartySecondProcedure {
   @SubscribeEvent
   public static void onChat(ServerChatEvent event) {
      execute(event);
   }

   public static void execute() {
      execute(null);
   }

   private static void execute(@Nullable Event event) {
   }
}
