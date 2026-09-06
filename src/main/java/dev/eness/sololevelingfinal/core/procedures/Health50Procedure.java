package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class Health50Procedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) / (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F)
               >= 0.45
            && (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)
                  / (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F)
               < 0.55;
   }
}
