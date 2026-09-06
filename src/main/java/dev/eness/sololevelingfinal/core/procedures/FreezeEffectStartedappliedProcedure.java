package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;

public class FreezeEffectStartedappliedProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         entity.getPersistentData().putDouble("FreezeX", entity.getX());
         entity.getPersistentData().putDouble("FreezeY", entity.getY());
         entity.getPersistentData().putDouble("FreezeZ", entity.getZ());
      }
   }
}
