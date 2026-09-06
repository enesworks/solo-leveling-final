package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.FxspikEntity;

public class FxspikEntityIsHurtProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof FxspikEntity) {
            ((FxspikEntity)entity).setAnimation("hit");
         }
      }
   }
}
