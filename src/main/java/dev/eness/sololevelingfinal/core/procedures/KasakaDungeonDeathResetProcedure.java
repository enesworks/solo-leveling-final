package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class KasakaDungeonDeathResetProcedure {
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
         if ((entity instanceof BloodRedComIgrisEntity || entity instanceof FangedKasakaEntity)
            && (
               entity.level().dimension() == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_kasaka"))
                  || entity.level().dimension()
                     == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_igris"))
            )) {
            boolean _setval = true;
            sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.instancecomplete = _setval;
               capability.syncPlayerVariables(sourceentity);
            });
         }
      }
   }
}
