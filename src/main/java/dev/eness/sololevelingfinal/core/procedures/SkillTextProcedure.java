package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class SkillTextProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      }

      String selectedPower = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables())
         .PselectedPower;
      return ShadowMonarchManager.displaySkillName(entity, selectedPower);
   }
}
