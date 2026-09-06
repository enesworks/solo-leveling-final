package dev.eness.sololevelingfinal.core.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import dev.eness.sololevelingfinal.core.procedures.SwordEnhanceOnEffectActiveTickProcedure;

public class SwordEnhanceMobEffect extends MobEffect {
   public SwordEnhanceMobEffect() {
      super(MobEffectCategory.BENEFICIAL, -13261);
   }

   @Override
   public String getDescriptionId() {
      return "effect.sololeveling.sword_enhance";
   }

   @Override
   public boolean isInstantenous() {
      return true;
   }

   @Override
   public void applyEffectTick(LivingEntity entity, int amplifier) {
      SwordEnhanceOnEffectActiveTickProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
   }
}
