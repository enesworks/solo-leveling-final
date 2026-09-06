package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

@EventBusSubscriber
public class HunterHurtProcedure {
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
         if (!(entity instanceof HunterEntity hunter && hunter.isStoryTempleFollower())) {
            if (entity instanceof HunterEntity) {
               if (sourceentity instanceof HunterEntity
                  && (sourceentity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null
                  && (sourceentity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != entity
                  && !(entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Enemies) : "")
                     .contains(sourceentity.getStringUUID())) {
                  if (event != null && event.isCancelable()) {
                     event.setCanceled(true);
                  }

                  return;
               }

               if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Allies) : "")
                     .contains(sourceentity.getStringUUID())
                  && entity instanceof HunterEntity _datEntSetS) {
                  _datEntSetS.getEntityData()
                     .set(
                        HunterEntity.DATA_Allies,
                        (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Allies) : "")
                           .replace(sourceentity.getStringUUID(), "")
                     );
               }

               if (!(entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Enemies) : "")
                     .contains(sourceentity.getStringUUID())
                  && sourceentity != entity
                  && entity instanceof HunterEntity _datEntSetS) {
                  _datEntSetS.getEntityData()
                     .set(
                        HunterEntity.DATA_Enemies,
                        (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Enemies) : "")
                           + ","
                           + sourceentity.getStringUUID()
                     );
               }

               HunterAIHelper.tryDefensiveReaction(event, entity, sourceentity);
            }
         }
      }
   }
}
