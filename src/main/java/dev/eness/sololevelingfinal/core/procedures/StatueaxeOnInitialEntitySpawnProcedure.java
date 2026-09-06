package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class StatueaxeOnInitialEntitySpawnProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.getPersistentData().putString("state", "idle");
         ((Mob)entity).setNoAi(true);
      }
   }
}
