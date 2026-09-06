package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;

public class KamishWrathHasItemGlowingEffectProcedure {
   public static boolean execute(Entity entity) {
      return entity == null ? false : entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get());
   }
}
