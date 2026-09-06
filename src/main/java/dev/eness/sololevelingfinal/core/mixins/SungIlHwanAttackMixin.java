package dev.eness.sololevelingfinal.core.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SungIlHwanAttackMessage;
import dev.eness.sololevelingfinal.core.util.SungIlHwanCombatManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 900)
public abstract class SungIlHwanAttackMixin {
   @Inject(method = "startAttack", at = @At("HEAD"))
   private void sololeveling$requestAssassinLineCut(CallbackInfoReturnable<Boolean> callback) {
      Minecraft minecraft = (Minecraft)this;
      LocalPlayer player = minecraft.player;
      if (player != null && minecraft.screen == null && SungIlHwanCombatManager.shouldReplaceBasicAttack(player)) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new SungIlHwanAttackMessage());
      }
   }
}
