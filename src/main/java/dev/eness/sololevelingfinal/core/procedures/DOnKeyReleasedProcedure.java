package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class DOnKeyReleasedProcedure {
   public static void execute(LevelAccessor world, double x, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get())) {
            if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.KAMISH_WRATH.get()) {
               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.KAMISHCOOL.get(), 40, 1));
               }

               double _setval = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.kamishcharge = _setval;
                  capability.syncPlayerVariables(entity);
               });
            }

            if (entity instanceof LivingEntity _entity) {
               _entity.removeEffect(SololevelingModMobEffects.SWORD_ENHANCE.get());
            }
         }
      }
   }
}
