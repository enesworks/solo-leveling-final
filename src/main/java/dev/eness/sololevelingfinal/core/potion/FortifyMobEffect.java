package dev.eness.sololevelingfinal.core.potion;

import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

public class FortifyMobEffect extends MobEffect {
   public FortifyMobEffect() {
      super(MobEffectCategory.BENEFICIAL, -10092544);
   }

   @Override
   public String getDescriptionId() {
      return "effect.sololeveling.fortify";
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
   }

   @Override
   public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
      consumer.accept(new IClientMobEffectExtensions() {
         @Override
         public boolean isVisibleInGui(MobEffectInstance effect) {
            return false;
         }
      });
   }
}
