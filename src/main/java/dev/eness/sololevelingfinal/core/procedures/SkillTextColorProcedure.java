package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class SkillTextColorProcedure {
   public static int execute(Entity entity) {
      if (entity == null) {
         return -26266;
      }

      String selectedPower = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables())
         .PselectedPower;
      int skillColor = ShadowMonarchManager.skillColor(entity, selectedPower);
      return skillColor == 16777215 ? -26266 : skillColor;
   }
}
