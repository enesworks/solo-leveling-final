package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class FuturisticGolemOnInitialEntitySpawnProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.getPersistentData().putString("state", "idle");
      }
   }
}
