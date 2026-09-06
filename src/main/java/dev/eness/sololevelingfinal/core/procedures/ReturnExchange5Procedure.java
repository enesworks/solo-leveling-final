package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnExchange5Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return ExchangeCon5Procedure.execute(entity) ? ExchangeCordReturn5Procedure.execute(entity) : "";
      }
   }
}
