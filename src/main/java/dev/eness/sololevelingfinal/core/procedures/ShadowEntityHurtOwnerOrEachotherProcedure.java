package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber
public class ShadowEntityHurtOwnerOrEachotherProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, entity, event.getSource().getEntity());
      }
   }

   public static void execute(Entity entity, Entity sourceentity) {
      execute(null, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (ShadowMonarchManager.haveSameShadowOwner(sourceentity, entity) && event != null && event.isCancelable()) {
            event.setCanceled(true);
         }

         if (entity instanceof LivingEntity owner && ShadowMonarchManager.isOwnedShadow(sourceentity, owner) && event != null && event.isCancelable()) {
            event.setCanceled(true);
         }
      }
   }
}
