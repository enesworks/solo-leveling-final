package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.IceElfEntity;

public class IceElfEntityIsHurtProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof IceElfEntity) {
            ((IceElfEntity)entity).setAnimation("misc.hurt");
         }
      }
   }
}
