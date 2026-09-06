package dev.eness.sololevelingfinal.core.client.gui.system;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.TrainingGUIButtonMessage;

public class SystemTrainScreen extends SystemScreen {
   private static final String[] INFO = new String[]{
      "Spawn a training bot to test", "your skills on and level up.", "", "The bot's strength scales with", "your current level."
   };

   public SystemTrainScreen() {
      super(Component.literal("TRAINING"));
      this.panelW = 200;
      this.panelH = 150;
   }

   @Override
   protected void init() {
      super.init();
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.panelX + 3, this.panelY + 3, 40, 12, Component.literal("< Back"), b -> this.openChild(new SystemPanelScreen()))
      );
      int bw = 130;
      int bh = 22;
      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.panelX + (this.panelW - bw) / 2, this.panelY + this.panelH - 32, bw, bh, Component.literal("Spawn Training Bot"), b -> this.spawn()
         )
      );
   }

   private void spawn() {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         BlockPos bp = player.blockPosition();
         SololevelingMod.PACKET_HANDLER.sendToServer(new TrainingGUIButtonMessage(1, bp.getX(), bp.getY(), bp.getZ()));
      }
   }

   @Override
   protected void renderContent(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      int y = this.panelY + 30;

      for (String line : INFO) {
         if (!line.isEmpty()) {
            g.drawString(this.font, line, this.panelX + (this.panelW - this.font.width(line)) / 2, y, -7358248, false);
         }

         y += 12;
      }
   }
}
