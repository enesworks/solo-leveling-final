package dev.eness.sololevelingfinal.core.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import dev.eness.sololevelingfinal.core.procedures.BleedOnEffectActiveTickProcedure;

public class BleedMobEffect extends MobEffect {
   public BleedMobEffect() {
      super(MobEffectCategory.HARMFUL, -4784128);
   }

   @Override
   public String getDescriptionId() {
      return "effect.sololeveling.bleed";
   }

   @Override
   public boolean isInstantenous() {
      return true;
   }

   @Override
   public void applyEffectTick(LivingEntity entity, int amplifier) {
      BleedOnEffectActiveTickProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
   }
}
