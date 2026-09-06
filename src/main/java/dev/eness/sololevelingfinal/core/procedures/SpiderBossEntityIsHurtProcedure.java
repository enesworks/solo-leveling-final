package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class SpiderBossEntityIsHurtProcedure {
   public static void execute(Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (entity instanceof Mob _entity && sourceentity instanceof LivingEntity _ent) {
            _entity.setTarget(_ent);
         }
      }
   }
}
