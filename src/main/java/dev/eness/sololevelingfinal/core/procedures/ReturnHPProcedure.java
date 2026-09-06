package dev.eness.sololevelingfinal.core.procedures;

import java.text.DecimalFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ReturnHPProcedure {
   public static String execute(Entity entity) {
      return entity == null
         ? ""
         : "§f§lHP:§c["
            + new DecimalFormat("##").format(entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0)
            + "/"
            + new DecimalFormat("##").format(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0)
            + "]";
   }
}
