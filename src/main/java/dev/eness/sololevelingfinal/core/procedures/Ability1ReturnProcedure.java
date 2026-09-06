package dev.eness.sololevelingfinal.core.procedures;

import dev.eness.sololevelingfinal.core.init.SololevelingModKeyMappings;

public class Ability1ReturnProcedure {
   public static String execute() {
      return SololevelingModKeyMappings.ABILITY_1.saveString().replace("key.keyboard.", "");
   }
}
