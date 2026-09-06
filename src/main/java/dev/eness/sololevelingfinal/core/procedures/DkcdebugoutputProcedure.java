package dev.eness.sololevelingfinal.core.procedures;

import java.text.DecimalFormat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class DkcdebugoutputProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         Player player = (Player)entity;
         CompoundTag data = player.getPersistentData();
         if (player != null && !player.level().isClientSide()) {
            player.displayClientMessage(Component.literal("§6======================"), false);
            player.displayClientMessage(
               Component.literal(
                  "§eFloor Cleared: §6"
                     + new DecimalFormat("##")
                        .format(
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .dkc_cleared
                        )
               ),
               false
            );
            player.displayClientMessage(
               Component.literal(
                  "§eFloor Unlocked: §6"
                     + (
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).dkc_started
                           ? new DecimalFormat("##")
                              .format(
                                 Math.min(
                                    20.0,
                                    Math.max(
                                       entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                                .orElse(new SololevelingModVariables.PlayerVariables())
                                             .dkc_cleared
                                          + 1.0,
                                       0.0
                                    )
                                 )
                              )
                           : 0
                     )
               ),
               false
            );
            int currentFloor = 0;
            if (DkcFloorRegistry.isDkc(entity.level())) {
               currentFloor = DKCFloorDetectorProcedure.getCurrentFloor(entity);
            }

            player.displayClientMessage(Component.literal("§eCurrent Floor: §6" + (currentFloor > 0 ? currentFloor : "None")), false);
            if (currentFloor >= 2 && currentFloor <= 19 && currentFloor != 1 && currentFloor != 20) {
               double killed = data.getDouble("dkc_floor_" + currentFloor + "_killed");
               double required = data.getDouble("dkc_floor_" + currentFloor + "_required");
               double remaining = Math.max(0.0, required - killed);
               player.displayClientMessage(Component.literal("§7---"), false);
               player.displayClientMessage(Component.literal("§eKills: §a" + (int)killed + " §7/ §c" + (int)required), false);
               player.displayClientMessage(Component.literal("§eRemaining: §c" + (int)remaining), false);
               double percentage = required > 0.0 ? killed / required * 100.0 : 0.0;
               player.displayClientMessage(Component.literal("§eProgress: §6" + new DecimalFormat("##.#").format(percentage) + "%"), false);
            } else if (currentFloor == 1 || currentFloor == 10 || currentFloor == 20) {
               player.displayClientMessage(Component.literal("§7---"), false);
               player.displayClientMessage(Component.literal("§4§lBoss Floor"), false);
            }

            player.displayClientMessage(Component.literal("§6======================"), false);
         }
      }
   }
}
