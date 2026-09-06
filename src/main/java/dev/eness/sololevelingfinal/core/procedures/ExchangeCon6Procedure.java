package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ExchangeCon6Procedure {
   public static boolean execute(Entity entity) {
      return entity == null ? false : !ExchangeCordReturn6Procedure.execute(entity).equals("");
   }
}
