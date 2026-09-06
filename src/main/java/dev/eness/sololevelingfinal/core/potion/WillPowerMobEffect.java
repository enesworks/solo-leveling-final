package dev.eness.sololevelingfinal.core.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import dev.eness.sololevelingfinal.core.procedures.WillPowerEffectExpiresProcedure;
import dev.eness.sololevelingfinal.core.procedures.WillPowerOnEffectActiveTickProcedure;

public class WillPowerMobEffect extends MobEffect {
   public WillPowerMobEffect() {
      super(MobEffectCategory.BENEFICIAL, -2720512);
   }

   @Override
   public String getDescriptionId() {
      return "effect.sololeveling.will_power";
   }

   @Override
   public void applyEffectTick(LivingEntity entity, int amplifier) {
      WillPowerOnEffectActiveTickProcedure.execute(entity.level(), entity.getX(), entity.getZ(), entity);
   }

   @Override
   public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
      super.removeAttributeModifiers(entity, attributeMap, amplifier);
      WillPowerEffectExpiresProcedure.execute(entity.level(), entity);
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
   }
}
