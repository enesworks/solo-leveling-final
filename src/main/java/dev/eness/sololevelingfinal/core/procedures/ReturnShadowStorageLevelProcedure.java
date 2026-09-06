package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ReturnShadowStorageLevelProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      }

      String string = "";
      if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level < 70.0) {
         string = "Lvl 1";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
         < 90.0) {
         string = "Lvl 2";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
         < 100.0) {
         string = "Lvl 3";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
         < 120.0) {
         string = "Lvl 4";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
         >= 120.0) {
         string = "Lvl 5";
      }

      return Math.round(
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).shadowstorageusage
         )
         + " / "
         + Math.round(
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).shadowstorage
         )
         + " ["
         + string
         + "]";
   }
}
