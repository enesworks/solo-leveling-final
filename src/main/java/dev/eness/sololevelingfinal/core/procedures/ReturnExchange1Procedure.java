package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnExchange1Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return ExchangeCon1Procedure.execute(entity) ? ExchangeCordReturn1Procedure.execute(entity) : "";
      }
   }
}
