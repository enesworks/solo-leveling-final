package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ReturnPotionh1Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 100.0
            ? "§lRestores 5 HP §b100 Gold"
            : "§lRestores 5 HP §c100 Gold";
      }
   }
}
