package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class ShadowIgrisStateChangerProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         rand = Mth.nextInt(RandomSource.create(), 1, 6);
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            if (CombatRangeHelper.withinSurfaceRange(entity, entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null, 5.0)) {
               if (rand != 1.0 && rand != 2.0) {
                  if (rand != 3.0 && rand != 4.0) {
                     if (rand != 5.0 && rand != 6.0) {
                        if (rand == 7.0) {
                           if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                              _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 10));
                           }

                           entity.getPersistentData().putString("state", "scream");
                           entity.getPersistentData().putDouble("MF", 0.0);
                        }
                     } else {
                        if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                           _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10));
                        }

                        entity.getPersistentData().putString("state", "slam");
                        entity.getPersistentData().putDouble("MF", 0.0);
                     }
                  } else {
                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10));
                     }

                     entity.getPersistentData().putString("state", "stab");
                     entity.getPersistentData().putDouble("MF", 0.0);
                  }
               } else {
                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10));
                  }

                  entity.getPersistentData().putString("state", "spin");
                  entity.getPersistentData().putDouble("MF", 0.0);
               }
            } else {
               entity.getPersistentData().putString("state", "idle");
               entity.getPersistentData().putDouble("MF", 0.0);
            }
         }
      }
   }
}
