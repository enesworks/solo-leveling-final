package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.ArrowSplashEntity;

public class ArrowSplashOnEntityTickUpdateProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof ArrowSplashEntity _datEntSetI) {
            _datEntSetI.getEntityData()
               .set(
                  ArrowSplashEntity.DATA_life,
                  (entity instanceof ArrowSplashEntity _datEntI ? _datEntI.getEntityData().get(ArrowSplashEntity.DATA_life) : 0) + 1
               );
         }

         if ((entity instanceof ArrowSplashEntity _datEntI ? _datEntI.getEntityData().get(ArrowSplashEntity.DATA_life) : 0) >= 20
            && !entity.level().isClientSide()) {
            entity.discard();
         }
      }
   }
}
