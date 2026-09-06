package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class PlistButtonConProcedure {
   public static boolean execute(Entity entity, int index) {
      return entity == null ? false : !PlistReturnProcedure.execute(entity, index).equals("empty");
   }
}
