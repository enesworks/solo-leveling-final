package dev.eness.sololevelingfinal.core.client.gui;

import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.ShadowExchangeMainGUIButtonMessage;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeMainGUIMenu;

public class ShadowExchangeMainGUIScreen extends ShadowStyledScreen<ShadowExchangeMainGUIMenu> {
   private static final HashMap<String, Object> guistate = ShadowExchangeMainGUIMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private ShadowStyledScreen.ShadowButton saveButton;
   private ShadowStyledScreen.ShadowButton teleportButton;

   public ShadowExchangeMainGUIScreen(ShadowExchangeMainGUIMenu container, Inventory inventory, Component text) {
      super(container, inventory, text, 360, 154);
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
      int bx = this.leftPos + 24;
      int by = this.topPos + 62;
      this.saveButton = new ShadowStyledScreen.ShadowButton(bx, by, 146, 34, Component.literal("Set Shadow"), false, button -> this.sendButton(0));
      this.teleportButton = new ShadowStyledScreen.ShadowButton(bx + 166, by, 146, 34, Component.literal("Exchange"), true, button -> this.sendButton(1));
      guistate.put("button:shadow_exchange_save_mode", this.saveButton);
      guistate.put("button:shadow_exchange_teleport_mode", this.teleportButton);
      this.addRenderableWidget(this.saveButton);
      this.addRenderableWidget(this.teleportButton);
   }

   @Override
   protected void renderShadowSections(GuiGraphics guiGraphics) {
      int x = this.leftPos;
      int y = this.topPos;
      outline(guiGraphics, x + 12, y + 32, 336, 94, 2000931071);
      guiGraphics.fill(x + 13, y + 33, x + 347, y + 47, 856836982);
   }

   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, "MODE", 18, 37, -12334849, false);
      guiGraphics.drawString(this.font, "SAVE", 31, 52, -6510635, false);
      guiGraphics.drawString(this.font, "MOVE", 197, 52, -6510635, false);
   }

   @Override
   protected void renderShadowTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (isOver(mouseX, mouseY, this.saveButton)) {
         guiGraphics.renderTooltip(this.font, Component.literal("Place a shadow on the ground to save this location."), mouseX, mouseY);
      }

      if (isOver(mouseX, mouseY, this.teleportButton)) {
         guiGraphics.renderTooltip(this.font, Component.literal("Teleport to one of your saved exchange positions."), mouseX, mouseY);
      }
   }

   private void sendButton(int buttonId) {
      SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowExchangeMainGUIButtonMessage(buttonId, this.x, this.y, this.z));
      ShadowExchangeMainGUIButtonMessage.handleButtonAction(this.entity, buttonId, this.x, this.y, this.z);
   }
}
