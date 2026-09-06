package dev.eness.sololevelingfinal.core.procedures;

import dev.eness.sololevelingfinal.core.init.SololevelingModKeyMappings;

public class Ability2ReturnProcedure {
   public static String execute() {
      return SololevelingModKeyMappings.ABILITY_2.saveString().replace("key.keyboard.", "");
   }
}
