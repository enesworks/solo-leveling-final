package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnExchange6Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return ExchangeCon6Procedure.execute(entity) ? ExchangeCordReturn6Procedure.execute(entity) : "";
      }
   }
}
