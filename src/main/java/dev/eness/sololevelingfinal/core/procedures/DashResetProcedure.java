package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@EventBusSubscriber
public class DashResetProcedure {
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
         SololevelingModVariables.PlayerVariables capability = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
         if (capability != null) {
            if (capability.MP < 100.0 && capability.dash != 1.0) {
               capability.dash = 1.0;
               capability.syncPlayerVariables(entity);
            }

            if (capability.dash == 1.3) {
               capability.MP = capability.MP - Math.round(2.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 30.0);
               capability.syncPlayerVariables(entity);
               CooldownManager.set(entity, "mana_refresh", 20);
            } else if (capability.dash == 1.5) {
               capability.MP = capability.MP - Math.round(4.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 30.0);
               capability.syncPlayerVariables(entity);
               CooldownManager.set(entity, "mana_refresh", 20);
            }
         }
      }
   }
}
