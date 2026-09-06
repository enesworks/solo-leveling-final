package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class IceBallOnInitialEntitySpawnProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.getPersistentData().putString("state", "");
         entity.getPersistentData().putDouble("IceLife", 0.0);
      }
   }
}
