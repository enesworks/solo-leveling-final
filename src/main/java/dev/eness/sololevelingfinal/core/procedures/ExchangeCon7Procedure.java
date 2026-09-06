package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ExchangeCon7Procedure {
   public static boolean execute(Entity entity) {
      return entity == null ? false : !ExchangeCordReturn7Procedure.execute(entity).equals("");
   }
}
