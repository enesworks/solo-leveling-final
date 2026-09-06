package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class DaggerSlashOnEntityTickUpdateProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.getPersistentData().putDouble("Life", entity.getPersistentData().getDouble("Life") + 1.0);
         if (entity.getPersistentData().getDouble("Life") >= 15.0 && !entity.level().isClientSide()) {
            entity.discard();
         }
      }
   }
}
