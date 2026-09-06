package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnNameAndGuildProcedure {
   public static String execute(Entity entity) {
      return entity == null ? "" : entity.getDisplayName().getString();
   }
}
