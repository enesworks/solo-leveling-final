package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.MiniGemGolemEntity;

public class MiniGemGolemEntityIsHurtProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof MiniGemGolemEntity) {
            ((MiniGemGolemEntity)entity).setAnimation("hurt");
         }
      }
   }
}
