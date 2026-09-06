package dev.eness.sololevelingfinal.core.mixins;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public abstract class DisableHotbarKeymappingMixin {
   @Shadow
   private int clickCount;

   @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
   public void inject1(CallbackInfoReturnable<Boolean> cir) {
      String keyName = ((KeyMapping)this).getName();
      if (keyName.startsWith("key.hotbar.")) {
         Entity entity = Minecraft.getInstance().player;
         if (entity != null
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).combatmode
            )
          {
            this.clickCount = 0;
            cir.setReturnValue(false);
         }
      }
   }
}
