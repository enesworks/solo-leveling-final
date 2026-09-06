package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.ThomasAndreEntity;

@EventBusSubscriber
public class AndreTargetSetProcedure {
   @SubscribeEvent
   public static void onEntitySetsAttackTarget(LivingChangeTargetEvent event) {
      execute(event, event.getOriginalTarget());
   }

   public static void execute(Entity entity) {
      execute(null, entity);
   }

   private static void execute(@Nullable Event event, Entity entity) {
      if (entity != null) {
         if (entity instanceof ThomasAndreEntity) {
            entity.setSprinting(true);
         }
      }
   }
}
