package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnExchange7Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return ExchangeCon7Procedure.execute(entity) ? ExchangeCordReturn7Procedure.execute(entity) : "";
      }
   }
}
