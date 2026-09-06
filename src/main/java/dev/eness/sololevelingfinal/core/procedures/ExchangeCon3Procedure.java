package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ExchangeCon3Procedure {
   public static boolean execute(Entity entity) {
      return entity == null ? false : !ExchangeCordReturn3Procedure.execute(entity).equals("");
   }
}
