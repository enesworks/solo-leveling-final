package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.BeruDeadBodyEntity;

public class BeruDeadBodyOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity instanceof BeruDeadBodyEntity beru && beru.getEntityData().get(BeruDeadBodyEntity.DATA_tries) <= 0 && !entity.level().isClientSide()) {
            entity.discard();
         }
      }
   }
}
