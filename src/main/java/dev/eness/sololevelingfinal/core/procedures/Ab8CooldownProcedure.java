package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class Ab8CooldownProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : CooldownManager.isOnCooldown(entity, "Dualwield")
            || CooldownManager.isOnCooldown(entity, "Sword Dance")
            || CooldownManager.isOnCooldown(entity, "Willpower")
            || CooldownManager.isOnCooldown(entity, "Haste Buff");
   }
}
