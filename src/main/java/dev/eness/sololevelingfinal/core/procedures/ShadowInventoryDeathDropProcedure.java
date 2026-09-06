package dev.eness.sololevelingfinal.core.procedures;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber
public class ShadowInventoryDeathDropProcedure {
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onPlayerClone(Clone event) {
      if (event != null) {
         ShadowMonarchManager.preserveProgressAfterPlayerClone(event.getOriginal(), event.getEntity());
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null && !event.getEntity().level().isClientSide()) {
         if (ShadowMonarchManager.isTrackedShadowEntity(event.getEntity())) {
            ShadowMonarchManager.dropStoredShadowInventory(event.getEntity());
            ShadowMonarchManager.handleTrackedShadowDeath(event.getEntity());
         }
      }
   }
}
