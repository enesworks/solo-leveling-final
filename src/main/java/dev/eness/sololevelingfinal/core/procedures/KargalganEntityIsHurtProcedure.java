package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;

public class KargalganEntityIsHurtProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof KargalganEntity _datEntSetI) {
            _datEntSetI.getEntityData()
               .set(KargalganEntity.DATA_Push, (entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_Push) : 0) + 1);
         }
      }
   }
}
