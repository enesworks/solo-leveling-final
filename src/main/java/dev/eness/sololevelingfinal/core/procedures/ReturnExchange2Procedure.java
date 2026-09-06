package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnExchange2Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return ExchangeCon2Procedure.execute(entity) ? ExchangeCordReturn2Procedure.execute(entity) : "";
      }
   }
}
