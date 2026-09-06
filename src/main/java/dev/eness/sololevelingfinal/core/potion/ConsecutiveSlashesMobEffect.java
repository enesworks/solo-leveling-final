package dev.eness.sololevelingfinal.core.potion;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import dev.eness.sololevelingfinal.core.procedures.ConsecutiveSlashesOnEffectActiveTickProcedure;

public class ConsecutiveSlashesMobEffect extends MobEffect {
   public ConsecutiveSlashesMobEffect() {
      super(MobEffectCategory.BENEFICIAL, -1);
   }

   @Override
   public String getDescriptionId() {
      return "effect.sololeveling.consecutive_slashes";
   }

   @Override
   public boolean isInstantenous() {
      return true;
   }

   @Override
   public void applyEffectTick(LivingEntity entity, int amplifier) {
      ConsecutiveSlashesOnEffectActiveTickProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
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
