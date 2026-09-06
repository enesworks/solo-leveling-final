package dev.eness.sololevelingfinal.core.client.gui.system;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.network.TitleSelectionMessage;
import dev.eness.sololevelingfinal.core.util.TitleManager;

public class SystemTitlesScreen extends SystemScreen {
   private static final int NONE_ROW_Y = 56;
   private static final int WOLF_ROW_Y = 94;
   private static final int ROW_X = 18;
   private static final int ROW_W = 184;
   private static final int ROW_H = 28;

   public SystemTitlesScreen() {
      super(Component.literal("TITLES"));
      this.panelW = 220;
      this.panelH = 180;
   }

   @Override
   protected void init() {
      super.init();
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.panelX + 3, this.panelY + 3, 40, 12, Component.literal("< Back"), b -> this.openChild(new SystemPanelScreen()))
      );
      this.addTitleButton(0, 56);
      this.addTitleButton(1, 94);
   }

   private void addTitleButton(int titleId, int rowY) {
      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.panelX + 18 + 184 - 54, this.panelY + rowY + 15, 46, 10, Component.literal("Select"), b -> this.selectTitle(titleId)
         )
      );
   }

   private void selectTitle(int titleId) {
      SololevelingMod.PACKET_HANDLER.sendToServer(new TitleSelectionMessage(titleId));
   }

   @Override
   protected void renderContent(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         SololevelingModVariables.PlayerVariables vars = vars(player);
         g.drawString(this.font, "AVAILABLE TITLES", this.panelX + 18, this.panelY + 30, -12597505, false);
         this.drawTitleRow(g, vars, 0, 56);
         this.drawTitleRow(g, vars, 1, 94);
      }
   }

   private void drawTitleRow(GuiGraphics g, SololevelingModVariables.PlayerVariables vars, int titleId, int rowY) {
      int x = this.panelX + 18;
      int y = this.panelY + rowY;
      boolean unlocked = TitleManager.isUnlocked(vars, titleId);
      boolean equipped = (int)vars.title == titleId;
      int border = equipped ? -9882 : (unlocked ? -14519384 : -11841184);
      int fill = equipped ? 1145030399 : 856695608;
      g.fill(x, y, x + 184, y + 28, fill);
      g.fill(x, y, x + 184, y + 1, border);
      g.fill(x, y + 28 - 1, x + 184, y + 28, border);
      g.fill(x, y, x + 1, y + 28, border);
      g.fill(x + 184 - 1, y, x + 184, y + 28, border);
      String name = TitleManager.displayName(titleId);
      String state = equipped ? "EQUIPPED" : (unlocked ? "UNLOCKED" : "LOCKED");
      int stateColor = equipped ? -9882 : (unlocked ? -8395521 : -38037);
      g.drawString(this.font, name, x + 8, y + 6, unlocked ? -1509633 : -7358248, false);
      g.drawString(this.font, state, x + 184 - this.font.width(state) - 8, y + 4, stateColor, false);
      if (titleId == 1 && !unlocked) {
         String progress = (int)Math.min(vars.wolfAssassinKills, 20.0) + "/20";
         g.drawString(this.font, progress, x + 8, y + 17, -7358248, false);
      }
   }

   @Override
   protected List<Component> getHoverTooltip(int mouseX, int mouseY) {
      Player player = Minecraft.getInstance().player;
      if (player == null) {
         return null;
      } else {
         SololevelingModVariables.PlayerVariables vars = vars(player);
         if (isOver(mouseX, mouseY, this.panelX + 18, this.panelY + 56, 184, 28)) {
            return TitleManager.tooltip(vars, 0);
         } else {
            return isOver(mouseX, mouseY, this.panelX + 18, this.panelY + 94, 184, 28) ? TitleManager.tooltip(vars, 1) : null;
         }
      }
   }

   private static SololevelingModVariables.PlayerVariables vars(Player player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }
}
