package dev.eness.sololevelingfinal.core.procedures;

import dev.eness.sololevelingfinal.core.init.SololevelingModKeyMappings;

public class Ability4ReturnProcedure {
   public static String execute() {
      return SololevelingModKeyMappings.ABILITY_4.saveString().replace("key.keyboard.", "");
   }
}
