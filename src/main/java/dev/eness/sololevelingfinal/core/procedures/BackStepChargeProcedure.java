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
public class BackStepChargeProcedure {
   private static final int MAX_CHARGES = 3;
   private static final int RECHARGE_TICKS = 180;
   private static final String INITIALIZED = "slr_back_step_charges_initialized";

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
         if (!entity.level().isClientSide()) {
            SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables());
            if (vars.Classes == 6.0 || vars.Plist.contains("Back Step")) {
               if (!entity.getPersistentData().getBoolean("slr_back_step_charges_initialized")) {
                  entity.getPersistentData().putBoolean("slr_back_step_charges_initialized", true);
                  setCharges(entity, 3.0, 0.0, true);
               } else {
                  double charges = Math.max(0.0, Math.min(3.0, vars.rangerleapnum));
                  double timer = Math.max(0.0, vars.rangerleaptimer);
                  if (charges >= 3.0) {
                     if (vars.rangerleapnum != 3.0 || vars.rangerleaptimer != 0.0) {
                        setCharges(entity, 3.0, 0.0, true);
                     }
                  } else {
                     double previousCharges = charges;
                     if (timer <= 0.0) {
                        timer = 180.0;
                     } else {
                        timer--;
                     }

                     if (timer <= 0.0) {
                        charges = Math.min(3.0, charges + 1.0);
                        timer = charges >= 3.0 ? 0.0 : 180.0;
                     }

                     setCharges(entity, charges, timer, charges != previousCharges || entity.tickCount % 10 == 0);
                  }
               }
            }
         }
      }
   }

   private static void setCharges(Entity entity, double charges, double timer, boolean sync) {
      entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.rangerleapnum = charges;
         capability.rangerleaptimer = timer;
         if (sync) {
            capability.syncPlayerVariables(entity);
         }
      });
   }
}
