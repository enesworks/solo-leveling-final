package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class GoblinClubOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) > 0.0F) {
            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
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

               if ((entity instanceof GoblinClubEntity _datEntS ? _datEntS.getEntityData().get(GoblinClubEntity.DATA_state) : "").equals("idle")) {
                  if (CombatRangeHelper.withinSurfaceRange(entity, entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null, 2.5)) {
                     if (entity instanceof GoblinClubEntity _datEntSetS) {
                        _datEntSetS.getEntityData().set(GoblinClubEntity.DATA_state, "attack");
                     }
                  } else if (entity instanceof GoblinClubEntity _datEntSetS) {
                     _datEntSetS.getEntityData().set(GoblinClubEntity.DATA_state, "idle");
                  }
               }
            }

            if ((entity instanceof GoblinClubEntity _datEntS ? _datEntS.getEntityData().get(GoblinClubEntity.DATA_state) : "").equals("attack")) {
               GoblinClubattackProcedure.execute(world, entity);
               if (entity instanceof GoblinClubEntity _datEntSetI) {
                  _datEntSetI.getEntityData()
                     .set(
                        GoblinClubEntity.DATA_MF,
                        (entity instanceof GoblinClubEntity _datEntI ? _datEntI.getEntityData().get(GoblinClubEntity.DATA_MF) : 0) + 1
                     );
               }

               if ((entity instanceof GoblinClubEntity _datEntI ? _datEntI.getEntityData().get(GoblinClubEntity.DATA_MF) : 0) >= 11) {
                  if (entity instanceof GoblinClubEntity _datEntSetS) {
                     _datEntSetS.getEntityData().set(GoblinClubEntity.DATA_state, "idle");
                  }

                  if (entity instanceof GoblinClubEntity _datEntSetI) {
                     _datEntSetI.getEntityData().set(GoblinClubEntity.DATA_MF, 0);
                  }
               }
            }

            if ((entity instanceof GoblinClubEntity _datEntS ? _datEntS.getEntityData().get(GoblinClubEntity.DATA_state) : "").equals("attack2")) {
               if (entity instanceof GoblinClubEntity _datEntSetI) {
                  _datEntSetI.getEntityData()
                     .set(
                        GoblinClubEntity.DATA_MF,
                        (entity instanceof GoblinClubEntity _datEntI ? _datEntI.getEntityData().get(GoblinClubEntity.DATA_MF) : 0) + 1
                     );
               }

               if ((entity instanceof GoblinClubEntity _datEntI ? _datEntI.getEntityData().get(GoblinClubEntity.DATA_MF) : 0) >= 21) {
                  if (entity instanceof GoblinClubEntity _datEntSetS) {
                     _datEntSetS.getEntityData().set(GoblinClubEntity.DATA_state, "idle");
                  }

                  if (entity instanceof GoblinClubEntity _datEntSetI) {
                     _datEntSetI.getEntityData().set(GoblinClubEntity.DATA_MF, 0);
                  }
               }
            }
         }
      }
   }
}
