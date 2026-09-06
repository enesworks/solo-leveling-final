package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;

@EventBusSubscriber
public class BloodRedComIgrisOnInitialEntitySpawnProcedure {
   @SubscribeEvent
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      execute(event, event.getEntity());
   }

   public static void execute(Entity entity) {
      execute(null, entity);
   }

   private static void execute(@Nullable Event event, Entity entity) {
      if (entity != null) {
         if (entity instanceof BloodRedComIgrisEntity && entity instanceof BloodRedComIgrisEntity animatable) {
            animatable.setTexture("igris_marcus");
         }
      }
   }
}
