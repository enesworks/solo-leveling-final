package dev.eness.sololevelingfinal.core.client.gui.system;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.RewardPanelButtonMessage;
import dev.eness.sololevelingfinal.core.util.RewardManager;

public class SystemRewardsScreen extends SystemScreen {
   private final List<SystemScreen.SystemButton> claimButtons = new ArrayList<>();

   public SystemRewardsScreen() {
      super(Component.literal("REWARDS"));
      this.panelW = 230;
      this.panelH = 160;
   }

   @Override
   protected void init() {
      Player player = Minecraft.getInstance().player;
      int rows = Math.max(3, player == null ? 0 : RewardManager.allRewards(player).size());
      this.panelH = Math.max(160, 72 + rows * 34);
      super.init();
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.panelX + 3, this.panelY + 3, 40, 12, Component.literal("< Back"), b -> this.openChild(new SystemPanelScreen()))
      );
      this.claimButtons.clear();

      for (int slot = 1; slot <= rows; slot++) {
         int rewardSlot = slot;
         SystemScreen.SystemButton button = new SystemScreen.SystemButton(
            this.panelX + this.panelW - 62, this.rowY(slot - 1), 50, 20, Component.literal("Claim"), b -> this.claim(rewardSlot)
         );
         this.claimButtons.add(button);
         this.addRenderableWidget(button);
      }
   }

   private int rowY(int index) {
      return this.panelY + 40 + index * 34;
   }

   private void claim(int slot) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         BlockPos bp = player.blockPosition();
         SololevelingMod.PACKET_HANDLER.sendToServer(new RewardPanelButtonMessage(99 + slot, bp.getX(), bp.getY(), bp.getZ()));
      }
   }

   @Override
   protected void renderContent(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      Player entity = Minecraft.getInstance().player;
      if (entity != null) {
         List<String> rewards = RewardManager.allRewards(entity);
         boolean any = false;

         for (int index = 0; index < this.claimButtons.size(); index++) {
            int y = this.rowY(index);
            boolean empty = index >= rewards.size();
            String shown = empty ? "§8- empty -" : RewardManager.displayName(rewards.get(index));
            SystemScreen.SystemButton button = this.claimButtons.get(index);
            button.active = !empty;
            button.visible = this.isFullyOpen() && !empty;
            if (!empty) {
               any = true;
            }

            g.drawString(this.font, "§7Slot " + (index + 1), this.panelX + 12, y - 8, -7358248, false);
            g.drawString(this.font, shown, this.panelX + 12, y + 4, -1509633, false);
            g.fill(this.panelX + 10, y + 24, this.panelX + this.panelW - 10, y + 25, 1430243071);
         }

         if (!any) {
            String none = "No rewards to collect";
            g.drawString(this.font, none, this.panelX + (this.panelW - this.font.width(none)) / 2, this.panelY + 24, -7358248, false);
         }
      }
   }
}
