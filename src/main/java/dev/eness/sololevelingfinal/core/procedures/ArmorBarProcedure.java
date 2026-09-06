package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ArmorBarProcedure {
   public static String execute(Entity entity) {
      return entity == null ? "" : Math.round(entity instanceof LivingEntity _livEnt ? _livEnt.getArmorValue() : 0.0F) + "";
   }
}
