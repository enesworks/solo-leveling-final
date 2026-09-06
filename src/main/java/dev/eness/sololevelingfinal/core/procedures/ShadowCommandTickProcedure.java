package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber
public class ShadowCommandTickProcedure {
   @SubscribeEvent
   public static void onLevelTick(LevelTickEvent event) {
      if (event.phase == Phase.END
         && !event.level.isClientSide()
         && event.level.getGameTime() % 10L == 0L
         && event.level instanceof ServerLevel level
         && !level.players().isEmpty()) {
         for (ServerPlayer player : level.players()) {
            ShadowMonarchManager.tickCommandedShadows(player);
         }
      }
   }
}
