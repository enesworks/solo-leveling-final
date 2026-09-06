package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DRPB1Procedure {
   @SubscribeEvent
   public static void onEntityTick(LivingTickEvent event) {
      execute(event);
   }

   public static void execute() {
      execute(null);
   }

   private static void execute(@Nullable Event event) {
   }
}
