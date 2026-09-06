package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

@EventBusSubscriber
public class SwordDanceOnKillProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         execute(event, event.getEntity(), event.getSource().getEntity());
      }
   }

   public static void execute(Entity entity, Entity sourceentity) {
      execute(null, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         double rand = 0.0;
         if (sourceentity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(SololevelingModMobEffects.SWORD_DANCE.get())) {
            CooldownManager.set(
               sourceentity,
               "dance",
               (
                     entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(SololevelingModMobEffects.SWORD_DANCE.get())
                        ? _livEnt.getEffect(SololevelingModMobEffects.SWORD_DANCE.get()).getDuration()
                        : 0
                  )
                  + 20
            );
         }
      }
   }
}
