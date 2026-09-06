package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class ReturnAbilitySlotProcedure {
   public static String execute(Entity entity, int slot) {
      if (entity == null) {
         return "";
      }

      SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      String value = SkillSlotHelper.getSlot(vars, slot);
      return value.isEmpty() ? "Not set!" : ShadowMonarchManager.displaySkillName(entity, value);
   }
}
