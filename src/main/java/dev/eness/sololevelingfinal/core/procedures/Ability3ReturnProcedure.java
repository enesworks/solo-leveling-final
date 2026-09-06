package dev.eness.sololevelingfinal.core.procedures;

import dev.eness.sololevelingfinal.core.init.SololevelingModKeyMappings;

public class Ability3ReturnProcedure {
   public static String execute() {
      return SololevelingModKeyMappings.ABILITY_3.saveString().replace("key.keyboard.", "");
   }
}
