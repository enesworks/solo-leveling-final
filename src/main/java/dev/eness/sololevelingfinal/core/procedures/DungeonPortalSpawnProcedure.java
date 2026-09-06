package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DungeonPortalSpawnProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event);
      }
   }

   public static void execute() {
      execute(null);
   }

   private static void execute(@Nullable Event event) {
      double RANDtime = 0.0;
      double RANDx = 0.0;
      double RANDz = 0.0;
      double RANDdungeon = 0.0;
   }
}
