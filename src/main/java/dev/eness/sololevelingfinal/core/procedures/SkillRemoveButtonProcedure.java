package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SkillRemoveButtonProcedure {
   public static void execute(Entity entity, int slot) {
      if (entity != null) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            SkillSlotHelper.setSlot(capability, slot, "");
            capability.syncPlayerVariables(entity);
         });
      }
   }
}
