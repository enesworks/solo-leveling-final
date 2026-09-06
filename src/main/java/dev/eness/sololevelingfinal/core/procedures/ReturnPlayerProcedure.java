package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnPlayerProcedure {
   public static Entity execute(Entity entity) {
      return entity == null ? null : entity;
   }
}
