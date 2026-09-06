package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ReturnPotionf2Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 400.0
            ? "§lRestores 50 FTG §b400 Gold"
            : "§lRestores 50 FTG §c400 Gold";
      }
   }
}
