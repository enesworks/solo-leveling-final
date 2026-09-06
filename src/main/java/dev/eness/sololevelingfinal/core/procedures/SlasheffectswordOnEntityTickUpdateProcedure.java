package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class SlasheffectswordOnEntityTickUpdateProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.getPersistentData().putDouble("life", entity.getPersistentData().getDouble("life") + 1.0);
         if (entity.getPersistentData().getDouble("life") >= 8.0 && !entity.level().isClientSide()) {
            entity.discard();
         }
      }
   }
}
