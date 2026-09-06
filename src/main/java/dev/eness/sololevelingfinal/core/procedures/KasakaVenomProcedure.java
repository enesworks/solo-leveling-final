package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.KasakaEntity;

@EventBusSubscriber
public class KasakaVenomProcedure {
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
         double rand = 0.0;
         rand = Math.random();
         if (sourceentity instanceof KasakaEntity) {
            if (rand < 0.2) {
               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.POISON, 70, 1, false, false));
               }
            } else if (rand < 0.7 && entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.POISON, 70, 0, false, false));
            }
         }
      }
   }
}
