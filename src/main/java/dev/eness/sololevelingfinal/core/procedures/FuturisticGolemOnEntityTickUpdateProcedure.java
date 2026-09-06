package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class FuturisticGolemOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double RandomSpecial = 0.0;
         if (!world.isClientSide()) {
            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
               Entity target = entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null;
               double distance = CombatRangeHelper.surfaceDistance(entity, target);
               entity.lookAt(
                  Anchor.EYES,
                  new Vec3(
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY(),
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

               if (distance <= 3.0) {
                  if (entity.getPersistentData().getString("state").equals("idle")) {
                     entity.getPersistentData().putString("state", "melee");
                  }
               } else if (distance <= 32.0) {
                  if (entity.getPersistentData().getString("state").equals("idle")) {
                     entity.getPersistentData().putString("state", "ranged");
                  }
               } else {
                  entity.getPersistentData().putString("state", "idle");
               }
            } else {
               entity.getPersistentData().putString("state", "idle");
            }

            if (entity.getPersistentData().getString("state").equals("idle")) {
               entity.setNoGravity(false);
            }

            if (entity.getPersistentData().getString("state").equals("melee")) {
               FuturisticGolemMeleeProcedure.execute(world, x, y, z, entity);
               entity.getPersistentData().putDouble("AI", entity.getPersistentData().getDouble("AI") + 1.0);
            }

            if (entity.getPersistentData().getString("state").equals("ranged")) {
               FuturisticGolemTeleportProcedure.execute(world, x, y, z, entity);
               entity.getPersistentData().putDouble("MF", entity.getPersistentData().getDouble("MF") + 1.0);
            }

            if (entity.getPersistentData().getDouble("MF") >= 101.0) {
               entity.getPersistentData().putString("state", "idle");
               entity.getPersistentData().putDouble("MF", 0.0);
            }

            if (entity.getPersistentData().getDouble("AI") >= 12.0) {
               entity.getPersistentData().putString("state", "idle");
               entity.getPersistentData().putDouble("AI", 0.0);
            }
         }
      }
   }
}
