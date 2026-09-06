package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class AuraAbilityCooldownProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return CooldownManager.getRemainingSeconds(entity, "aura") > 0 ? Math.round(CooldownManager.getRemainingSeconds(entity, "aura")) + "" : "";
      }
   }
}
