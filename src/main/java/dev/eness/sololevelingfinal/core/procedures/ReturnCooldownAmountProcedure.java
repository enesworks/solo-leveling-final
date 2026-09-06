package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnCooldownAmountProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return !CooldownRemainingOnTickProcedure.execute(entity).equals("") ? "CD: " + CooldownRemainingOnTickProcedure.execute(entity) : "Ready!";
      }
   }
}
