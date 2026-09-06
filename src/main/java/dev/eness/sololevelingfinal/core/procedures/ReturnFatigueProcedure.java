package dev.eness.sololevelingfinal.core.procedures;

import java.text.DecimalFormat;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ReturnFatigueProcedure {
   public static String execute(Entity entity) {
      return entity == null
         ? ""
         : "§f§lFTG: "
            + new DecimalFormat("##")
               .format(
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Fatigue
                     / 10.0
               );
   }
}
