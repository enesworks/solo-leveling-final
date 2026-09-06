package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;

public class MagicalSkullOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            entity.getPersistentData().putDouble("life", entity.getPersistentData().getDouble("life") + 1.0);
            entity.lookAt(
               Anchor.EYES,
               new Vec3(
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
               )
            );
            if (entity.getPersistentData().getDouble("life") >= 20.0) {
               entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * 0.65, entity.getLookAngle().y * 0.65, entity.getLookAngle().z * 0.65));
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, x, y, z, 5, 0.1, 0.1, 0.1, 0.0);
               }
            }

            if (entity.getPersistentData().getDouble("life") >= 40.0) {
               if (world instanceof Level _level && !_level.isClientSide()) {
                  _level.explode(entity, x, y, z, 2.0F, false, ExplosionInteraction.NONE);
               }

               if (!entity.level().isClientSide()) {
                  entity.discard();
               }
            }
         }
      }
   }
}
