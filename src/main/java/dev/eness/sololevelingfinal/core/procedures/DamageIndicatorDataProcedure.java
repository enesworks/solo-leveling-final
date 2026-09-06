package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DamageIndicatorDataProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingHurtEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event);
      }
   }

   public static void execute() {
      execute(null);
   }

   private static void execute(@Nullable Event event) {
      double damageValue = 0.0;
      double dirX = 0.0;
      double dirY = 0.0;
      double dirZ = 0.0;
      double offsetX = 0.0;
      double textY = 0.0;
      double offsetZ = 0.0;
   }
}
