package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class Dun1RightclickedProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         RandomCorridor1Procedure.execute(world, entity);
      }
   }
}
