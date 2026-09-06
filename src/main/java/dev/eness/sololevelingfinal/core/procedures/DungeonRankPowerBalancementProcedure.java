package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DungeonRankPowerBalancementProcedure {
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
      double ratio = 0.0;
      double dmg = 0.0;
      DamageSource src = null;
      Entity evnt = null;
      Entity srcent = null;
   }
}
