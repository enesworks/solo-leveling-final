package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnExchange4Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return ExchangeCon4Procedure.execute(entity) ? ExchangeCordReturn4Procedure.execute(entity) : "";
      }
   }
}
