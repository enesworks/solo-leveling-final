package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.BearTrapEntity;

public class BearTrapOnInitialEntitySpawnProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof BearTrapEntity) {
            ((BearTrapEntity)entity).setAnimation("spawn");
         }
      }
   }
}
