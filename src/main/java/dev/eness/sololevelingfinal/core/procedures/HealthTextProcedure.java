package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class HealthTextProcedure {
   public static String execute(Entity entity) {
      return entity == null
         ? ""
         : Math.round(entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)
            + "/"
            + Math.round(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
   }
}
