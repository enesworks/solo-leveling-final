package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ExchangeCon1Procedure {
   public static boolean execute(Entity entity) {
      return entity == null ? false : !ExchangeCordReturn1Procedure.execute(entity).equals("");
   }
}
