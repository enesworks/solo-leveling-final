package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class RedAntsOnEntityTickUpdateProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            entity.lookAt(
               Anchor.EYES,
               new Vec3(
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + entity.getBbHeight(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
               )
            );
            if (entity instanceof Mob _entity) {
               _entity.getNavigation()
                  .moveTo(
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY(),
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ(),
                     1.0
                  );
            }
         }
      }
   }
}
