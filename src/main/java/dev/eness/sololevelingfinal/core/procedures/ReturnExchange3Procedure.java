package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnExchange3Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return ExchangeCon3Procedure.execute(entity) ? ExchangeCordReturn3Procedure.execute(entity) : "";
      }
   }
}
