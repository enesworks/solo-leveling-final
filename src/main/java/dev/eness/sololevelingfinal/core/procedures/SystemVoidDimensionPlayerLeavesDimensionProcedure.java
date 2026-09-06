package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class SystemVoidDimensionPlayerLeavesDimensionProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.setNoGravity(false);
      }
   }
}
