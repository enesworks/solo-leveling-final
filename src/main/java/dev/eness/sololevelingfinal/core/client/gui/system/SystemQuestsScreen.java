package dev.eness.sololevelingfinal.core.client.gui.system;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.QuestsButtonMessage;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

public class SystemQuestsScreen extends SystemScreen {
   public SystemQuestsScreen() {
      super(Component.literal("QUESTS"));
      this.panelW = 190;
      this.panelH = 154;
   }

   @Override
   protected void init() {
      super.init();
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.panelX + 3, this.panelY + 3, 40, 12, Component.literal("< Back"), b -> this.openChild(new SystemPanelScreen()))
      );
      int bw = 150;
      int bh = 24;
      int x = this.panelX + (this.panelW - bw) / 2;
      int y = this.panelY + 40;
      this.addRenderableWidget(new SystemScreen.SystemButton(x, y, bw, bh, Component.literal("Daily Quests"), b -> this.sendQuest(0)));
      y += 32;
      Player player = Minecraft.getInstance().player;
      if (JobChangeQuestManager.isVisible(player)) {
         this.addRenderableWidget(new SystemScreen.SystemButton(x, y, bw, bh, Component.literal("Job Change Quest"), b -> this.sendQuest(2)));
         y += 32;
      }

      if (DkcQuestManager.isVisible(player)) {
         this.addRenderableWidget(new SystemScreen.SystemButton(x, y, bw, bh, DkcQuestManager.buttonLabel(player), b -> this.sendQuest(1)));
      }
   }

   private void sendQuest(int id) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         SystemGuiSounds.exit();
         BlockPos bp = player.blockPosition();
         SololevelingMod.PACKET_HANDLER.sendToServer(new QuestsButtonMessage(id, bp.getX(), bp.getY(), bp.getZ()));
      }
   }

   @Override
   protected void renderContent(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      String sub = "Review System objectives";
      g.drawString(this.font, sub, this.panelX + (this.panelW - this.font.width(sub)) / 2, this.panelY + 26, -7358248, false);
   }
}
