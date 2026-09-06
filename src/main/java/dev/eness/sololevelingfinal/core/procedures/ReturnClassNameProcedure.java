package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ReturnClassNameProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
         == 1.0) {
         return "§bAssassin";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
         == 2.0) {
         String specialization = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .map(data -> data.mageSpecialization)
            .orElse("");
         return "barrier".equals(specialization)
            ? "§bBarrier Mage"
            : ("arcane".equals(specialization) ? "§dArcane Mage" : ("storm".equals(specialization) ? "§eStorm Mage" : "§cFire Mage"));
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
         == 3.0) {
         return "§cFighter";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
         == 4.0) {
         return "§fTanker";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
         == 5.0) {
         return "§aSupport Mage";
      } else {
         return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
               == 6.0
            ? "§2Ranger"
            : " none";
      }
   }
}
