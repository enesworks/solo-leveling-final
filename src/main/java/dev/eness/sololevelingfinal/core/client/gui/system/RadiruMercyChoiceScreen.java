package dev.eness.sololevelingfinal.core.client.gui.system;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.RadiruMercyChoiceMessage;

public final class RadiruMercyChoiceScreen extends SystemScreen {
   private boolean decisionSent;

   public RadiruMercyChoiceScreen() {
      super(Component.translatable("gui.sololeveling.radiru_mercy.title"));
      this.panelW = 310;
      this.panelH = 210;
   }

   @Override
   protected boolean allowsNonSystemAccess() {
      return true;
   }

   @Override
   protected boolean shouldPlaySystemSounds() {
      return true;
   }

   @Override
   protected void init() {
      super.init();
      int buttonY = this.panelY + this.panelH - 43;
      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.panelX + 27, buttonY, 122, 22, Component.translatable("gui.sololeveling.radiru_mercy.spare"), button -> this.choose(true)
         )
      );
      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.panelX + this.panelW - 149, buttonY, 122, 22, Component.translatable("gui.sololeveling.radiru_mercy.later"), button -> this.choose(false)
         )
      );
   }

   @Override
   protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      int centerX = this.panelX + this.panelW / 2;
      graphics.drawCenteredString(this.font, Component.translatable("entity.sololeveling.esil_radiru"), centerX, this.panelY + 38, -2585345);
      graphics.drawCenteredString(this.font, Component.translatable("dialogue.sololeveling.esil.mercy.plea"), centerX, this.panelY + 59, -1509633);
      graphics.drawCenteredString(this.font, Component.translatable("dialogue.sololeveling.esil.mercy.offer"), centerX, this.panelY + 76, -7358248);
      graphics.fill(this.panelX + 37, this.panelY + 98, this.panelX + this.panelW - 37, this.panelY + 99, -14519384);
      graphics.drawCenteredString(this.font, Component.translatable("gui.sololeveling.radiru_mercy.instruction"), centerX, this.panelY + 112, -1509633);
      graphics.drawCenteredString(this.font, Component.translatable("gui.sololeveling.radiru_mercy.warning"), centerX, this.panelY + 132, -23376);
   }

   @Override
   protected void beginClose() {
      this.choose(false);
   }

   @Override
   public void onClose() {
      if (!this.decisionSent) {
         this.choose(false);
      }

      super.onClose();
   }

   private void choose(boolean spare) {
      if (!this.decisionSent) {
         this.decisionSent = true;
         if (!spare) {
            SystemGuiSounds.negativeNotification();
         }

         SololevelingMod.PACKET_HANDLER.sendToServer(new RadiruMercyChoiceMessage(spare));
      }
   }

   public static void handleServerState(boolean open) {
      Minecraft minecraft = Minecraft.getInstance();
      if (open) {
         if (!(minecraft.screen instanceof RadiruMercyChoiceScreen)) {
            minecraft.setScreen(new RadiruMercyChoiceScreen());
         }
      } else {
         if (minecraft.screen instanceof RadiruMercyChoiceScreen screen) {
            screen.decisionSent = true;
            SystemGuiSounds.exit();
            minecraft.setScreen(null);
         }
      }
   }
}
