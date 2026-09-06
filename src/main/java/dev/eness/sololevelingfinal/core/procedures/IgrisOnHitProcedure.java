package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.util.IgrisCombatTeleportHelper;

@EventBusSubscriber
public class IgrisOnHitProcedure {
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
         if (sourceentity instanceof BloodRedComIgrisEntity || sourceentity instanceof IgrisShadowEntity) {
            rand = Mth.nextInt(RandomSource.create(), 1, 2);
            if (Math.random() < 0.25) {
               if (rand == 1.0) {
                  IgrisCombatTeleportHelper.tryMoveBehindTarget(sourceentity, entity);
               } else if (rand == 2.0 && IgrisCombatTeleportHelper.tryElevatedReposition(sourceentity, entity)) {
                  if (sourceentity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.NO_FALL_DAMAGE.get(), 30, 1, false, false));
                  }

                  if (sourceentity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 1, false, false));
                  }
               }
            }
         }
      }
   }
}
