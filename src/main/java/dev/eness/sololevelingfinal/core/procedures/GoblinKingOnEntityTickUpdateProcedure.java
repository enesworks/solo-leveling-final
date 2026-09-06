package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;

public class GoblinKingOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         double Chain = 0.0;
         double ChainWait = 0.0;
         if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) > 0.0F) {
            if (entity instanceof GoblinKingEntity _datEntL1 && _datEntL1.getEntityData().get(GoblinKingEntity.DATA_sprint)) {
               entity.setSprinting(true);
            } else {
               entity.setSprinting(false);
            }

            if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)
                  <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F) / 3.0F
               && entity instanceof GoblinKingEntity _datEntSetL) {
               _datEntSetL.getEntityData().set(GoblinKingEntity.DATA_sprint, true);
            }

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

               if (entity instanceof GoblinKingEntity _datEntSetI) {
                  _datEntSetI.getEntityData()
                     .set(
                        GoblinKingEntity.DATA_MF,
                        (entity instanceof GoblinKingEntity _datEntI ? _datEntI.getEntityData().get(GoblinKingEntity.DATA_MF) : 0) + 1
                     );
               }
            } else {
               if (entity instanceof GoblinKingEntity _datEntSetL) {
                  _datEntSetL.getEntityData().set(GoblinKingEntity.DATA_sprint, false);
               }

               if (entity instanceof GoblinKingEntity _datEntSetS) {
                  _datEntSetS.getEntityData().set(GoblinKingEntity.DATA_state, "idle");
               }

               if (entity instanceof GoblinKingEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(GoblinKingEntity.DATA_MF, 0);
               }
            }

            if ((entity instanceof GoblinKingEntity _datEntS ? _datEntS.getEntityData().get(GoblinKingEntity.DATA_state) : "").equals("idle")
               && (entity instanceof GoblinKingEntity _datEntI ? _datEntI.getEntityData().get(GoblinKingEntity.DATA_MF) : 0) == 20) {
               GoblinBossstatechangerProcedure.execute(entity);
            }

            if ((entity instanceof GoblinKingEntity _datEntS ? _datEntS.getEntityData().get(GoblinKingEntity.DATA_state) : "").equals("attack1")) {
               GoblinBossattack1Procedure.execute(world, entity);
            }

            if ((entity instanceof GoblinKingEntity _datEntS ? _datEntS.getEntityData().get(GoblinKingEntity.DATA_state) : "").equals("attack2")) {
               GoblinBossattack2Procedure.execute(world, entity);
            }

            if ((entity instanceof GoblinKingEntity _datEntS ? _datEntS.getEntityData().get(GoblinKingEntity.DATA_state) : "").equals("Dash")) {
               GoblinBossDashProcedure.execute(world, entity);
            }

            if ((entity instanceof GoblinKingEntity _datEntS ? _datEntS.getEntityData().get(GoblinKingEntity.DATA_state) : "").equals("Slam")) {
               GoblinBossSmashProcedure.execute(world, entity);
            }
         } else if (entity instanceof GoblinKingEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(GoblinKingEntity.DATA_MF, 0);
         }
      }
   }
}
