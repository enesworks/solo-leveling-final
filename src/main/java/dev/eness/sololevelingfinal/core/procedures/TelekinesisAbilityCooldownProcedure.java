package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class TelekinesisAbilityCooldownProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return CooldownManager.getRemainingSeconds(entity, "telekinesis") > 0
            ? Math.round(CooldownManager.getRemainingSeconds(entity, "telekinesis")) + ""
            : "";
      }
   }
}
