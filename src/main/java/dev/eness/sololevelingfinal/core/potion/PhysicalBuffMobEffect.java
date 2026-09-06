package dev.eness.sololevelingfinal.core.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import dev.eness.sololevelingfinal.core.procedures.PhysicalBuffEffectExpiresProcedure;
import dev.eness.sololevelingfinal.core.procedures.PhysicalBuffEffectStartedappliedProcedure;
import dev.eness.sololevelingfinal.core.procedures.PhysicalBuffOnEffectActiveTickProcedure;

public class PhysicalBuffMobEffect extends MobEffect {
   public PhysicalBuffMobEffect() {
      super(MobEffectCategory.BENEFICIAL, -37888);
   }

   @Override
   public String getDescriptionId() {
      return "effect.sololeveling.physical_buff";
   }

   @Override
   public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
      PhysicalBuffEffectStartedappliedProcedure.execute(entity);
   }

   @Override
   public void applyEffectTick(LivingEntity entity, int amplifier) {
      PhysicalBuffOnEffectActiveTickProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
   }

   @Override
   public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
      super.removeAttributeModifiers(entity, attributeMap, amplifier);
      PhysicalBuffEffectExpiresProcedure.execute(entity);
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
   }
}
