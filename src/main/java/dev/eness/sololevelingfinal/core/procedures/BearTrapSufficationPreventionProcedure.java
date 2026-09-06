package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BearTrapEntity;

@EventBusSubscriber
public class BearTrapSufficationPreventionProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingHurtEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, event.getSource(), entity);
      }
   }

   public static void execute(DamageSource damagesource, Entity entity) {
      execute(null, damagesource, entity);
   }

   private static void execute(@Nullable Event event, DamageSource damagesource, Entity entity) {
      if (damagesource != null && entity != null) {
         if (entity instanceof BearTrapEntity && damagesource.is(DamageTypes.IN_WALL) && event != null && event.isCancelable()) {
            event.setCanceled(true);
         }
      }
   }
}
