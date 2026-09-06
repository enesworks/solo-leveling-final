package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class Ab3CooldownSymbolProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : CooldownManager.isOnCooldown(entity, "Slash Dash")
            || CooldownManager.isOnCooldown(entity, "Stealth")
            || CooldownManager.isOnCooldown(entity, "Heal Beam")
            || CooldownManager.isOnCooldown(entity, "Tank Leap");
   }
}
