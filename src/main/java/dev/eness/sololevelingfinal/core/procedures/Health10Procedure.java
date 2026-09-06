package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class Health10Procedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) / (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F)
               >= 0.05
            && (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)
                  / (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F)
               < 0.15;
   }
}
