package dev.eness.sololevelingfinal.core.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FrostArchitecturePauseScreen extends Screen {
   public FrostArchitecturePauseScreen() {
      super(Component.translatable("gui.sololeveling.frost_architecture.title"));
   }

   @Override
   public boolean isPauseScreen() {
      return true;
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
   }

   @Override
   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      if (FrostArchitectureClientState.isActivationKey(keyCode, scanCode)) {
         FrostArchitectureClientState.releaseAndSend();
         return true;
      } else {
         return super.keyReleased(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public void removed() {
      FrostArchitectureClientState.onPauseScreenRemoved(this);
      super.removed();
   }
}
