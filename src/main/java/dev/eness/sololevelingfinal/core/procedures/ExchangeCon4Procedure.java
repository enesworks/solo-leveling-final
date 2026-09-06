package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ExchangeCon4Procedure {
   public static boolean execute(Entity entity) {
      return entity == null ? false : !ExchangeCordReturn4Procedure.execute(entity).equals("");
   }
}
