package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ExchangeCon5Procedure {
   public static boolean execute(Entity entity) {
      return entity == null ? false : !ExchangeCordReturn5Procedure.execute(entity).equals("");
   }
}
