package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.SpawnerPortalEntity;

public class SpawnerPortalOnInitialEntitySpawnProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof SpawnerPortalEntity) {
            ((SpawnerPortalEntity)entity).setAnimation("spawn");
         }

         entity.getPersistentData().putDouble("portalspan", 0.0);
      }
   }
}
