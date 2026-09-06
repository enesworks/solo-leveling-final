package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.VesselManager;

public class ReturnJobProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         VesselManager.VesselDefinition vessel = VesselManager.currentDefinition(entity);
         if (vessel != null) {
            return "§f§lJob: " + vessel.powerName();
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
            == 1.0) {
            return "§f§lJob: Shadow Monarch";
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
            == 2.0) {
            return "§f§lJob: Grand Mage";
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
            == 3.0) {
            return "§f§lJob: Frost Monarch";
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
            == 4.0) {
            return "§f§lJob: Monarch Of White Flames";
         } else {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
                  == 9.0
               ? "§f§lJob: Monarch Of Fangs"
               : "§f§lJob: None";
         }
      }
   }
}
