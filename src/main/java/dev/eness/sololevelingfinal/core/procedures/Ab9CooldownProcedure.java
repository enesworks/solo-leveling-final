package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class Ab9CooldownProcedure {
   public static boolean execute(Entity entity) {
      return entity == null ? false : CooldownManager.isOnCooldown(entity, "aura");
   }
}
