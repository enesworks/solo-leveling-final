package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ExchangeCon2Procedure {
   public static boolean execute(Entity entity) {
      return entity == null ? false : !ExchangeCordReturn2Procedure.execute(entity).equals("");
   }
}
