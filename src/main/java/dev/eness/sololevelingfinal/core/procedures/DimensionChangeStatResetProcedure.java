package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.util.PlayerVitalSync;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber
public class DimensionChangeStatResetProcedure {
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ShadowMonarchManager.dismissLoadedOwnedShadows(player, event.getFrom());
      }

      execute(event, event.getEntity());
   }

   public static void execute(Entity entity) {
      execute(null, entity);
   }

   private static void execute(@Nullable Event event, Entity entity) {
      if (entity instanceof ServerPlayer player) {
         PlayerVitalSync.refreshClientState(player);
         SololevelingMod.queueServerWork(1, () -> {
            if (!player.isRemoved()) {
               PlayerVitalSync.refreshClientState(player);
            }
         });
      }
   }
}
