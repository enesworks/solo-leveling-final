package dev.eness.sololevelingfinal.core.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import dev.eness.sololevelingfinal.core.procedures.HasteBuffEffectExpiresProcedure;
import dev.eness.sololevelingfinal.core.procedures.HasteBuffEffectStartedappliedProcedure;
import dev.eness.sololevelingfinal.core.procedures.HasteBuffOnEffectActiveTickProcedure;

public class HasteBuffMobEffect extends MobEffect {
   public HasteBuffMobEffect() {
      super(MobEffectCategory.BENEFICIAL, -16726529);
   }

   @Override
   public String getDescriptionId() {
      return "effect.sololeveling.haste_buff";
   }

   @Override
   public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
      HasteBuffEffectStartedappliedProcedure.execute(entity);
   }

   @Override
   public void applyEffectTick(LivingEntity entity, int amplifier) {
      HasteBuffOnEffectActiveTickProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
   }

   @Override
   public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
      super.removeAttributeModifiers(entity, attributeMap, amplifier);
      HasteBuffEffectExpiresProcedure.execute(entity);
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
   }
}
