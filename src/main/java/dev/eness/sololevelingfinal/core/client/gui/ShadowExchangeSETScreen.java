package dev.eness.sololevelingfinal.core.client.gui;

import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.ShadowExchangeSETButtonMessage;
import dev.eness.sololevelingfinal.core.util.ShadowExchangeManager;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeSETMenu;

public class ShadowExchangeSETScreen extends ShadowStyledScreen<ShadowExchangeSETMenu> {
   private static final HashMap<String, Object> guistate = ShadowExchangeSETMenu.guistate;
   private static final int SLOT_COUNT = 7;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private final ShadowStyledScreen.ShadowButton[] teleportButtons = new ShadowStyledScreen.ShadowButton[7];
   private final ShadowStyledScreen.ShadowButton[] clearButtons = new ShadowStyledScreen.ShadowButton[7];

   public ShadowExchangeSETScreen(ShadowExchangeSETMenu container, Inventory inventory, Component text) {
      super(container, inventory, text, 420, 252);
      this.world = container.world;
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
   }

   @Override
   protected String shadowTitle() {
      return "SHADOW EXCHANGE";
   }

   @Override
   protected void initShadowWidgets() {
      for (int i = 0; i < 7; i++) {
         int slot = i + 1;
         ShadowStyledScreen.ShadowButton teleport = new ShadowStyledScreen.ShadowButton(
            0, 0, 298, 22, Component.literal(""), i % 2 == 1, b -> this.sendTeleport(slot)
         );
         ShadowStyledScreen.ShadowButton clear = new ShadowStyledScreen.ShadowButton(
            0, 0, 64, 22, Component.literal("Clear"), true, true, b -> this.sendClear(slot)
         );
         this.teleportButtons[i] = teleport;
         this.clearButtons[i] = clear;
         guistate.put("button:shadow_exchange_tp_" + slot, teleport);
         guistate.put("button:shadow_exchange_clear_" + slot, clear);
         this.addRenderableWidget(teleport);
         this.addRenderableWidget(clear);
      }

      this.layoutButtons();
   }

   @Override
   public void containerTick() {
      super.containerTick();
      this.layoutButtons();
   }

   @Override
   protected void renderShadowSections(GuiGraphics guiGraphics) {
      int x = this.leftPos;
      int y = this.topPos;
      outline(guiGraphics, x + 12, y + 32, 396, 202, 2000931071);
      guiGraphics.fill(x + 13, y + 33, x + 407, y + 47, 856836982);
   }

   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, "SAVED POSITIONS", 18, 37, -12334849, false);
      if (this.visibleCount() == 0) {
         guiGraphics.drawCenteredString(this.font, Component.literal("No exchange positions saved."), this.imageWidth / 2, 126, -6510635);
      }
   }

   @Override
   protected void renderShadowTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      for (int i = 0; i < 7; i++) {
         if (isOver(mouseX, mouseY, this.teleportButtons[i])) {
            guiGraphics.renderTooltip(this.font, Component.literal("Exchange to saved position " + (i + 1)), mouseX, mouseY);
            return;
         }

         if (isOver(mouseX, mouseY, this.clearButtons[i])) {
            guiGraphics.renderTooltip(this.font, Component.literal("Remove saved position " + (i + 1)), mouseX, mouseY);
            return;
         }
      }
   }

   private void layoutButtons() {
      int row = 0;

      for (int i = 0; i < 7; i++) {
         boolean show = this.hasPosition(i + 1);
         ShadowStyledScreen.ShadowButton teleport = this.teleportButtons[i];
         ShadowStyledScreen.ShadowButton clear = this.clearButtons[i];
         if (teleport != null && clear != null) {
            teleport.visible = show;
            teleport.active = show;
            clear.visible = show;
            clear.active = show;
            if (show) {
               int by = this.topPos + 56 + row * 25;
               teleport.setPosition(this.leftPos + 24, by);
               teleport.setMessage(Component.literal(this.positionText(i + 1)));
               clear.setPosition(this.leftPos + 330, by);
               row++;
            }
         }
      }
   }

   private int visibleCount() {
      int count = 0;

      for (int slot = 1; slot <= 7; slot++) {
         if (this.hasPosition(slot)) {
            count++;
         }
      }

      return count;
   }

   private boolean hasPosition(int slot) {
      return ShadowExchangeManager.hasAnchor(this.entity, slot);
   }

   private String positionText(int slot) {
      String value = ShadowExchangeManager.anchorDisplay(this.entity, slot);
      return value != null && !value.isBlank() ? value : "Saved Position " + slot;
   }

   private void sendTeleport(int slot) {
      if (this.hasPosition(slot)) {
         this.sendButton(slot - 1);
      }
   }

   private void sendClear(int slot) {
      if (this.hasPosition(slot)) {
         this.sendButton(6 + slot);
         this.layoutButtons();
      }
   }

   private void sendButton(int buttonId) {
      SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowExchangeSETButtonMessage(buttonId, this.x, this.y, this.z));
      ShadowExchangeSETButtonMessage.handleButtonAction(this.entity, buttonId, this.x, this.y, this.z);
   }
}
