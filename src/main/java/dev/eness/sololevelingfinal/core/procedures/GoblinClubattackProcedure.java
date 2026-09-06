package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class GoblinClubattackProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) > 0.0F) {
            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
               if ((entity instanceof GoblinClubEntity _datEntS ? _datEntS.getEntityData().get(GoblinClubEntity.DATA_state) : "").equals("attack")) {
                  if ((entity instanceof GoblinClubEntity _datEntI ? _datEntI.getEntityData().get(GoblinClubEntity.DATA_MF) : 0) == 1) {
                     if (entity instanceof GoblinClubEntity) {
                        ((GoblinClubEntity)entity).setAnimation("attack_1");
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 12, 2, false, false));
                     }
                  }

                  if ((entity instanceof GoblinClubEntity _datEntI ? _datEntI.getEntityData().get(GoblinClubEntity.DATA_MF) : 0) == 4
                     && CombatRangeHelper.withinSurfaceRange(entity, entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null, 2.0)) {
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)
                        .hurt(
                           new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity),
                           3.0F
                        );
                  }

                  if ((entity instanceof GoblinClubEntity _datEntI ? _datEntI.getEntityData().get(GoblinClubEntity.DATA_MF) : 0) == 8
                     && CombatRangeHelper.withinSurfaceRange(entity, entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null, 2.0)) {
                     if (entity instanceof GoblinClubEntity _datEntSetS) {
                        _datEntSetS.getEntityData().set(GoblinClubEntity.DATA_state, "attack2");
                     }

                     if (entity instanceof GoblinClubEntity _datEntSetI) {
                        _datEntSetI.getEntityData().set(GoblinClubEntity.DATA_MF, 0);
                     }
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
                  if ((entity instanceof GoblinClubEntity _datEntI ? _datEntI.getEntityData().get(GoblinClubEntity.DATA_MF) : 0) == 1) {
                     if (entity instanceof GoblinClubEntity) {
                        ((GoblinClubEntity)entity).setAnimation("attack_2");
                     }

                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 22, 2, false, false));
                     }
                  }

                  if ((entity instanceof GoblinClubEntity _datEntI ? _datEntI.getEntityData().get(GoblinClubEntity.DATA_MF) : 0) == 2
                     && CombatRangeHelper.withinSurfaceRange(entity, entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null, 2.0)) {
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)
                        .hurt(
                           new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity),
                           3.0F
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
            } else if (entity instanceof GoblinClubEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(GoblinClubEntity.DATA_state, "idle");
            }
         }
      }
   }
}
