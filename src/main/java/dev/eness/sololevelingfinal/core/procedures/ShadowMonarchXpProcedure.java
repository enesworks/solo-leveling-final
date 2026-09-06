package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.util.ShadowExperienceManager;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber
public class ShadowMonarchXpProcedure {
   @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
   public static void onEntityDamaged(LivingDamageEvent event) {
      ShadowExperienceManager.recordDamage(event);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && !event.getEntity().level().isClientSide()) {
         ShadowExperienceManager.awardContributions(event.getEntity());
         Entity finishingShadow = ShadowExperienceManager.resolveShadow(event.getSource().getEntity());
         if (finishingShadow == null) {
            finishingShadow = ShadowExperienceManager.resolveShadow(event.getSource().getDirectEntity());
         }

         if (finishingShadow != null) {
            ShadowMonarchManager.collectManaStoneDropsFromKill(finishingShadow, event.getEntity());
         }
      }
   }
}
