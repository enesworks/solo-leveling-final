package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class AttributeBugFixBelow0Procedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player);
      }
   }

   public static void execute(Entity entity) {
      execute(null, entity);
   }

   private static void execute(@Nullable Event event, Entity entity) {
      if (entity != null) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            boolean changed = false;
            if (capability.Vitality < 0.0) {
               capability.Vitality = 0.0;
               changed = true;
            }

            if (capability.Strength < 0.0) {
               capability.Strength = 0.0;
               changed = true;
            }

            if (capability.Intelligence < 0.0) {
               capability.Intelligence = 0.0;
               changed = true;
            }

            if (capability.Speed < 0.0) {
               capability.Speed = 0.0;
               changed = true;
            }

            if (capability.Durability < 0.0) {
               capability.Durability = 0.0;
               changed = true;
            }

            if (capability.perception < 0.0) {
               capability.perception = 0.0;
               changed = true;
            }

            if (changed) {
               capability.syncPlayerVariables(entity);
            }
         });
      }
   }
}
