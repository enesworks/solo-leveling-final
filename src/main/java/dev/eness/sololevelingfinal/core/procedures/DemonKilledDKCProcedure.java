package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.CerberusEntity;
import dev.eness.sololevelingfinal.core.entity.DemonEntity;
import dev.eness.sololevelingfinal.core.entity.DemonKnightEntity;
import dev.eness.sololevelingfinal.core.entity.KaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.VulcanEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

@EventBusSubscriber
public class DemonKilledDKCProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         execute(
            event,
            event.getEntity().level(),
            event.getEntity().getX(),
            event.getEntity().getY(),
            event.getEntity().getZ(),
            event.getEntity(),
            event.getSource().getEntity()
         );
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceEntity) {
      execute(null, world, x, y, z, entity, sourceEntity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceEntity) {
      if (entity != null) {
         if (!world.isClientSide()) {
            if (entity instanceof DemonEntity || entity instanceof DemonKnightEntity) {
               DKCKillCounterProcedure.execute(world, entity, sourceEntity);
            }

            if (entity instanceof CerberusEntity) {
               DKCBossKillRewardProcedure.execute(world, x, y, z, entity, sourceEntity);
            }

            if (entity instanceof VulcanEntity) {
               DKCBossKillRewardProcedure.execute(world, x, y, z, entity, sourceEntity);
            }

            if (entity instanceof BaranEntity) {
               DKCBossKillRewardProcedure.execute(world, x, y, z, entity, sourceEntity);
            }

            if (entity instanceof KaiselinEntity && entity.getType() == SololevelingModEntities.KAISELIN.get()) {
               DKCBossKillRewardProcedure.execute(world, x, y, z, entity, sourceEntity);
            }
         }
      }
   }
}
