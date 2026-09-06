package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.ChoijongEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;
import dev.eness.sololevelingfinal.core.util.FireMageSpellManager;

public class ChoijongOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         double dmg_modifier = 0.0;
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            Entity target = entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null;
            entity.lookAt(
               Anchor.EYES,
               new Vec3((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ())
            );
            CombatRangeHelper.maintainRangedBand(entity, target, 9.0, 22.0, 1.15);
            if (entity instanceof ChoijongEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(ChoijongEntity.DATA_IA, (entity instanceof ChoijongEntity _datEntI ? _datEntI.getEntityData().get(ChoijongEntity.DATA_IA) : 0) + 1);
            }

            int attackTimer = entity instanceof ChoijongEntity choi ? choi.getEntityData().get(ChoijongEntity.DATA_IA) : 0;
            if (attackTimer % 24 == 0
               && entity instanceof ChoijongEntity choi
               && target instanceof LivingEntity livingTarget
               && CombatRangeHelper.withinSurfaceRange(entity, target, 24.0)
               && choi.getSensing().hasLineOfSight(livingTarget)) {
               choi.performRangedAttack(livingTarget, 1.0F);
            }

            if ((entity instanceof ChoijongEntity _datEntI ? _datEntI.getEntityData().get(ChoijongEntity.DATA_IA) : 0) == 60) {
               rand = Mth.nextInt(RandomSource.create(), 1, 100);
               String spell = rand <= 30.0
                  ? "Inferno Lance"
                  : (
                     rand <= 52.0
                        ? "Ignition Orb"
                        : (rand <= 70.0 ? "Flashfire" : (rand <= 86.0 ? "Cremation" : (rand <= 97.0 ? "Furnace Dominion" : "Heavenfall")))
                  );
               if (!FireMageSpellManager.castNpc(entity, spell) && "Cremation".equals(spell)) {
                  FireMageSpellManager.castNpc(entity, "Inferno Lance");
               }
            } else if ((entity instanceof ChoijongEntity _datEntI ? _datEntI.getEntityData().get(ChoijongEntity.DATA_IA) : 0) > 80
               && entity instanceof ChoijongEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(ChoijongEntity.DATA_IA, 0);
            }
         } else if (entity instanceof ChoijongEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(ChoijongEntity.DATA_IA, 0);
         }
      }
   }
}
