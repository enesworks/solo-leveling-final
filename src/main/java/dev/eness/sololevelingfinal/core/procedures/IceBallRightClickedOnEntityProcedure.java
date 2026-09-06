package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class IceBallRightClickedOnEntityProcedure {
   public static void execute(Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         double motionZ = 0.0;
         double deltaZ = 0.0;
         double deltaX = 0.0;
         double motionY = 0.0;
         double deltaY = 0.0;
         double motionX = 0.0;
         double speed = 0.0;
         if (entity.getPersistentData().getString("caster").equals(sourceentity.getDisplayName().getString())) {
            entity.getPersistentData().putString("state", "move");
            CooldownManager.set(sourceentity, "job_1", 100);
            deltaX = -Math.sin(sourceentity.getYRot() / 180.0F * (float) Math.PI);
            deltaY = -Math.sin(sourceentity.getXRot() / 180.0F * (float) Math.PI);
            deltaZ = Math.cos(sourceentity.getYRot() / 180.0F * (float) Math.PI);
            speed = 2.0;
            motionX = deltaX * speed;
            motionY = deltaY * speed;
            motionZ = deltaZ * speed;
            entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
            entity.getPersistentData().putDouble("IceX", -Math.sin(sourceentity.getYRot() / 180.0F * (float) Math.PI));
            entity.getPersistentData().putDouble("IceY", -Math.sin(sourceentity.getXRot() / 180.0F * (float) Math.PI));
            entity.getPersistentData().putDouble("IceZ", Math.cos(sourceentity.getYRot() / 180.0F * (float) Math.PI));
            sourceentity.getPersistentData().putBoolean("UsingIceBall", false);
         }
      }
   }
}
