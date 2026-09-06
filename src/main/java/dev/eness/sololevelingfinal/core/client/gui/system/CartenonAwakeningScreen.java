package dev.eness.sololevelingfinal.core.client.gui.system;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.CartenonAwakeningChoiceMessage;

public final class CartenonAwakeningScreen extends SystemScreen {
   private boolean decisionSent;

   public CartenonAwakeningScreen() {
      super(Component.literal("SYSTEM"));
      this.panelW = 286;
      this.panelH = 188;
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
      this.addRenderableWidget(new SystemScreen.SystemButton(this.panelX + 31, buttonY, 100, 22, Component.literal("YES"), button -> this.choose(true)));
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.panelX + this.panelW - 131, buttonY, 100, 22, Component.literal("NO"), button -> this.choose(false))
      );
   }

   @Override
   protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      graphics.drawCenteredString(this.font, Component.literal("WARNING"), this.panelX + this.panelW / 2, this.panelY + 39, -44182);
      graphics.drawCenteredString(this.font, Component.literal("Death is imminent."), this.panelX + this.panelW / 2, this.panelY + 60, -23376);
      graphics.drawCenteredString(
         this.font, Component.literal("The System has detected a compatible host."), this.panelX + this.panelW / 2, this.panelY + 82, -7358248
      );
      graphics.drawCenteredString(
         this.font, Component.literal("Would you like to become a Player?"), this.panelX + this.panelW / 2, this.panelY + 104, -1509633
      );
      graphics.fill(this.panelX + 34, this.panelY + 122, this.panelX + this.panelW - 34, this.panelY + 123, -14519384);
   }

   @Override
   protected void beginClose() {
   }

   @Override
   public void onClose() {
   }

   private void choose(boolean accept) {
      if (!this.decisionSent) {
         this.decisionSent = true;
         if (!accept) {
            SystemGuiSounds.negativeNotification();
         }

         SololevelingMod.PACKET_HANDLER.sendToServer(new CartenonAwakeningChoiceMessage(accept));
      }
   }

   public static void handleServerState(boolean open) {
      Minecraft minecraft = Minecraft.getInstance();
      if (open) {
         if (!(minecraft.screen instanceof CartenonAwakeningScreen)) {
            minecraft.setScreen(new CartenonAwakeningScreen());
         }
      } else {
         if (minecraft.screen instanceof CartenonAwakeningScreen) {
            SystemGuiSounds.exit();
            minecraft.setScreen(null);
         }
      }
   }
}
