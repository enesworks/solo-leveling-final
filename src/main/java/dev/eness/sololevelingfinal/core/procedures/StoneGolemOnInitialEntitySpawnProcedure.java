package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.StoneGolemEntity;

public class StoneGolemOnInitialEntitySpawnProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof StoneGolemEntity) {
            ((StoneGolemEntity)entity).setAnimation("spawn");
         }
      }
   }
}
