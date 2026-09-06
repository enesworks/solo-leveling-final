package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;

public class BeruShadowOnInitialEntitySpawnProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof TamableAnimal _tamEnt && _tamEnt.isTame()) {
            entity.lookAt(
               Anchor.EYES,
               new Vec3(
                  (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getX(),
                  (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getY(),
                  (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getZ()
               )
            );
         }
      }
   }
}
