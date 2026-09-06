package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class CoinTierPot3Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 800.0
            ? "§6800G"
            : "§c800G";
      }
   }
}
