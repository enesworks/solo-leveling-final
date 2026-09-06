package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public final class ShadowStorageTiersProcedure {
   private ShadowStorageTiersProcedure() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.level().isClientSide() && event.player.tickCount % 40 == Math.floorMod(event.player.getId(), 40)) {
         execute(event.player);
      }
   }

   public static void execute(Entity entity) {
      if (entity != null) {
         SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if ((int)vars.JOB == 1) {
            double target = vars.Level >= 120.0 ? 200.0 : (vars.Level >= 100.0 ? 150.0 : (vars.Level >= 90.0 ? 100.0 : (vars.Level >= 70.0 ? 40.0 : 20.0)));
            if (Double.compare(vars.shadowstorage, target) != 0) {
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.shadowstorage = target;
                  capability.syncPlayerVariables(entity);
               });
            }
         }
      }
   }
}
