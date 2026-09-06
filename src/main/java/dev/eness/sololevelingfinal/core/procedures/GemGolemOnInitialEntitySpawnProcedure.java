package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class GemGolemOnInitialEntitySpawnProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.setDeltaMovement(new Vec3(0.0, 0.5, 0.0));
      }
   }
}
