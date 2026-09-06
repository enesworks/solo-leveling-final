package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnNameProcedure {
   public static String execute(Entity entity) {
      return entity == null ? "" : "§f§lName: " + entity.getDisplayName().getString();
   }
}
