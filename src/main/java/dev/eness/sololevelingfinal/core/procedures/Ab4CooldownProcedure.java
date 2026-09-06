package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class Ab4CooldownProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : CooldownManager.isOnCooldown(entity, "Slash Fury")
            || CooldownManager.isOnCooldown(entity, "Murderious Intent")
            || CooldownManager.isOnCooldown(entity, "Overheal")
            || CooldownManager.isOnCooldown(entity, "Proximity Trap")
            || CooldownManager.isOnCooldown(entity, "Reinforcement")
            || CooldownManager.isOnCooldown(entity, "Cold Blood");
   }
}
