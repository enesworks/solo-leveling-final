package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class GoblinBossstatechangerProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         rand = Mth.nextInt(RandomSource.create(), 1, 2);
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            double distance = CombatRangeHelper.surfaceDistance(entity, entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null);
            if (distance <= 3.0) {
               if (rand == 1.0) {
                  if (entity instanceof GoblinKingEntity _datEntSetI) {
                     _datEntSetI.getEntityData().set(GoblinKingEntity.DATA_MF, 0);
                  }

                  if (entity instanceof GoblinKingEntity _datEntSetS) {
                     _datEntSetS.getEntityData().set(GoblinKingEntity.DATA_state, "attack1");
                  }

                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10));
                  }
               } else if (rand == 2.0) {
                  if (entity instanceof GoblinKingEntity _datEntSetI) {
                     _datEntSetI.getEntityData().set(GoblinKingEntity.DATA_MF, 0);
                  }

                  if (entity instanceof GoblinKingEntity _datEntSetS) {
                     _datEntSetS.getEntityData().set(GoblinKingEntity.DATA_state, "attack2");
                  }

                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10));
                  }
               }
            } else if (distance <= 8.0) {
               if (entity instanceof GoblinKingEntity _datEntSetS) {
                  _datEntSetS.getEntityData().set(GoblinKingEntity.DATA_state, "idle");
               }

               if (entity instanceof GoblinKingEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(GoblinKingEntity.DATA_MF, 0);
               }
            } else if (rand == 1.0) {
               if (entity instanceof GoblinKingEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(GoblinKingEntity.DATA_MF, 0);
               }

               if (entity instanceof GoblinKingEntity _datEntSetS) {
                  _datEntSetS.getEntityData().set(GoblinKingEntity.DATA_state, "Dash");
               }
            } else if (rand == 2.0) {
               if (entity instanceof GoblinKingEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(GoblinKingEntity.DATA_MF, 0);
               }

               if (entity instanceof GoblinKingEntity _datEntSetS) {
                  _datEntSetS.getEntityData().set(GoblinKingEntity.DATA_state, "Slam");
               }

               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10));
               }
            }
         }
      }
   }
}
