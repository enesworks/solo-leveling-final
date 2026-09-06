package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.IceChunkEntity;

public class IceChunkOnEntityTickUpdateProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof IceChunkEntity _datEntSetI) {
            _datEntSetI.getEntityData()
               .set(IceChunkEntity.DATA_life, (entity instanceof IceChunkEntity _datEntI ? _datEntI.getEntityData().get(IceChunkEntity.DATA_life) : 0) + 1);
         }

         if ((entity instanceof IceChunkEntity _datEntI ? _datEntI.getEntityData().get(IceChunkEntity.DATA_life) : 0) == 70 && !entity.level().isClientSide()) {
            entity.discard();
         }
      }
   }
}
