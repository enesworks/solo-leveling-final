package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.BearTrapEntity;

public class BearTrapPlayerCollidesWithThisEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (entity instanceof TamableAnimal _tamEnt
            && _tamEnt.isTame()
            && (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) != sourceentity) {
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 4, false, false));
            }

            if (sourceentity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 7, 0, false, false));
            }

            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false));
            }

            if (entity instanceof BearTrapEntity) {
               ((BearTrapEntity)entity).setAnimation("close");
            }

            SololevelingMod.queueServerWork(
               7,
               () -> {
                  if (world instanceof Level _level && !_level.isClientSide()) {
                     _level.explode(
                        entity instanceof TamableAnimal _tamEntxxx ? _tamEntxxx.getOwner() : null,
                        new DamageSource(
                           world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_EXPLOSION),
                           entity,
                           entity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null
                        ),
                        null,
                        x,
                        y,
                        z,
                        4.0F,
                        false,
                        ExplosionInteraction.NONE
                     );
                  }

                  if (!entity.level().isClientSide()) {
                     entity.discard();
                  }
               }
            );
         }
      }
   }
}
