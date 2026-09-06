package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class TimerTextProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         double timer = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).dailytimer;
         if (timer > 6000.0) {
            return "§l§fTime: §f" + Math.round(timer / 20.0);
         } else if (timer > 1200.0) {
            return "§l§fTime: §6" + Math.round(timer / 20.0);
         } else {
            return timer > 0.0 ? "§l§fTime: §4" + Math.round(timer / 20.0) : "§fNo Active Mission";
         }
      }
   }
}
