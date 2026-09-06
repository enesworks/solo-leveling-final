package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class QuestInfoOnKeyReleasedProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.questinfo) {
               capability.questinfo = false;
            }
         });
      }
   }
}
