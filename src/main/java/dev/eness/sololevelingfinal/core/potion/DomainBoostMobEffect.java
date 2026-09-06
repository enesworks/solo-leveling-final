package dev.eness.sololevelingfinal.core.potion;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import dev.eness.sololevelingfinal.core.procedures.DomainBoostEffectExpiresProcedure;
import dev.eness.sololevelingfinal.core.procedures.DomainBoostEffectStartedappliedProcedure;

public class DomainBoostMobEffect extends MobEffect {
   public DomainBoostMobEffect() {
      super(MobEffectCategory.NEUTRAL, -1);
   }

   @Override
   public String getDescriptionId() {
      return "effect.sololeveling.domain_boost";
   }

   @Override
   public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
      DomainBoostEffectStartedappliedProcedure.execute(entity);
   }

   @Override
   public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
      super.removeAttributeModifiers(entity, attributeMap, amplifier);
      DomainBoostEffectExpiresProcedure.execute(entity);
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
   }

   @Override
   public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
      consumer.accept(
         new IClientMobEffectExtensions() {
            @Override
            public boolean isVisibleInInventory(MobEffectInstance effect) {
               return false;
            }

            @Override
            public boolean renderInventoryText(
               MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset
            ) {
               return false;
            }

            @Override
            public boolean isVisibleInGui(MobEffectInstance effect) {
               return false;
            }
         }
      );
   }
}
