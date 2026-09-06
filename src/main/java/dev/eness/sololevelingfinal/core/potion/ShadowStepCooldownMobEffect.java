package dev.eness.sololevelingfinal.core.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ShadowStepCooldownMobEffect extends MobEffect {
   public ShadowStepCooldownMobEffect() {
      super(MobEffectCategory.NEUTRAL, -1);
   }

   @Override
   public String getDescriptionId() {
      return "effect.sololeveling.shadow_step_cooldown";
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
   }
}
