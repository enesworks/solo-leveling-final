package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TitleManager;

public class ReturnTitleProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      }

      int title = (int)entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).title;
      return "§f§lTitle: " + TitleManager.displayName(title);
   }
}
