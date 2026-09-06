package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class Ab5CooldownProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : CooldownManager.isOnCooldown(entity, "Cross Strike")
            || CooldownManager.isOnCooldown(entity, "Critical Strike")
            || CooldownManager.isOnCooldown(entity, "Protection Mark")
            || CooldownManager.isOnCooldown(entity, "Protection Mark")
            || CooldownManager.isOnCooldown(entity, "Blessing Mark");
   }
}
