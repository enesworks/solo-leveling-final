package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class SystemVoidDimensionPlayerEntersDimensionProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.setNoGravity(true);
      }
   }
}
