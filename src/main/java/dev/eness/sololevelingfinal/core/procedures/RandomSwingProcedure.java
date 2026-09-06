package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class RandomSwingProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         rand = Mth.nextInt(RandomSource.create(), 1, 3);
         if (rand == 1.0) {
            Swing1Procedure.execute(world, x, y, z, entity);
         }

         if (rand == 2.0) {
            Swing2Procedure.execute(world, x, y, z, entity);
         }

         if (rand == 3.0) {
            Swing3Procedure.execute(world, x, y, z, entity);
         }
      }
   }
}
