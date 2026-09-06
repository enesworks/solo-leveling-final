package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class Ab7CooldownProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : CooldownManager.isOnCooldown(entity, "Ghost Step")
            || CooldownManager.isOnCooldown(entity, "Detection")
            || CooldownManager.isOnCooldown(entity, "Ground Slam")
            || CooldownManager.isOnCooldown(entity, "Shield Bash")
            || CooldownManager.isOnCooldown(entity, "Physical Buff");
   }
}
