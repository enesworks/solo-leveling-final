package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;

public class ThomasAndreOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            entity.getPersistentData().putDouble("IA", entity.getPersistentData().getDouble("IA") + 1.0);
            entity.setSprinting(true);
         } else {
            entity.getPersistentData().putString("State", "Idle");
            entity.getPersistentData().putDouble("IA", 0.0);
            entity.setSprinting(false);
         }

         if (entity.getPersistentData().getString("State").equals("Idle") && entity.getPersistentData().getDouble("IA") == 20.0) {
            AndreStateChangerProcedure.execute(entity);
         }

         if (entity.getPersistentData().getString("State").equals("Punch")) {
            ThomasPunchProcedure.execute(world, entity);
         }
      }
   }
}
