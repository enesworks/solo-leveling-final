package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.CentipedeEntity;

public class CentipedeOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (world.getLevelData().getGameTime() % 20L == 0L) {
            if (entity instanceof CentipedeEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(
                     CentipedeEntity.DATA_life, (entity instanceof CentipedeEntity _datEntI ? _datEntI.getEntityData().get(CentipedeEntity.DATA_life) : 0) + 1
                  );
            }

            if ((entity instanceof CentipedeEntity _datEntI ? _datEntI.getEntityData().get(CentipedeEntity.DATA_life) : 0) >= 100
               && !entity.level().isClientSide()) {
               entity.discard();
            }
         }
      }
   }
}
