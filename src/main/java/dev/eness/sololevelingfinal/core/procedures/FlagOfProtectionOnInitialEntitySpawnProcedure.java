package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.FlagOfProtectionEntity;

public class FlagOfProtectionOnInitialEntitySpawnProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity instanceof FlagOfProtectionEntity) {
            ((FlagOfProtectionEntity)entity).setAnimation("spawnin");
         }

         SololevelingMod.queueServerWork(180, () -> {
            if (entity instanceof FlagOfProtectionEntity) {
               ((FlagOfProtectionEntity)entity).setAnimation("despawn");
            }

            SololevelingMod.queueServerWork(20, () -> {
               if (!entity.level().isClientSide()) {
                  entity.discard();
               }
            });
         });
      }
   }
}
