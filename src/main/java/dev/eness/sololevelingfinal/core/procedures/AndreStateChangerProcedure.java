package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class AndreStateChangerProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double StateSelector = 0.0;
         entity.getPersistentData().putString("PreviousPreviousState", entity.getPersistentData().getString("PreviousState"));
         entity.getPersistentData().putString("PreviousState", entity.getPersistentData().getString("State"));
         entity.getPersistentData().putDouble("IA", 0.0);
         StateSelector = Mth.nextInt(RandomSource.create(), 0, 0);
         if (StateSelector == 0.0) {
            entity.getPersistentData().putString("State", "Punch");
         }

         if (StateSelector == 1.0) {
            entity.getPersistentData().putString("State", "Dash");
         }

         if (StateSelector == 2.0) {
            entity.getPersistentData().putString("State", "Slam");
         }

         if (StateSelector == 3.0) {
            entity.getPersistentData().putString("State", "Combo");
         }

         if (StateSelector == 4.0) {
            entity.getPersistentData().putString("State", "pull");
         }
      }
   }
}
