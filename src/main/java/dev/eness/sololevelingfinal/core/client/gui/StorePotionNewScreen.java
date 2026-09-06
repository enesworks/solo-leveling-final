package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemContainerScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemScreen;
import dev.eness.sololevelingfinal.core.network.StorePotionNewButtonMessage;
import dev.eness.sololevelingfinal.core.world.inventory.StorePotionNewMenu;

public class StorePotionNewScreen extends SystemContainerScreen<StorePotionNewMenu> {
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private static final int[] COLS = StorePotionNewMenu.COLS;
   private static final int[] ROWS = StorePotionNewMenu.ROWS;
   private static final String[] COL_PRICE = new String[]{"§b100g", "§b400g", "§b800g"};
   private static final String[] ROW_LABEL = new String[]{"§cHP", "§9MP", "§eFTG"};

   public StorePotionNewScreen(StorePotionNewMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.pRelX = -74;
      this.pRelY = -102;
      this.pW = 180;
      this.pH = 236;
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int gx, int gy) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShopStyle.panel(g, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, this.pH);
      ShopStyle.titleBar(g, this.font, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, "POTION SHOP");

      for (int r = 0; r < 3; r++) {
         for (int c = 0; c < 3; c++) {
            ShopStyle.slot(g, this.leftPos + COLS[c], this.topPos + ROWS[r]);
         }
      }

      for (int si = 0; si < 3; si++) {
         for (int sj = 0; sj < 9; sj++) {
            ShopStyle.slot(g, this.leftPos + -68 + sj * 18, this.topPos + 52 + si * 18);
         }
      }

      for (int si = 0; si < 9; si++) {
         ShopStyle.slot(g, this.leftPos + -68 + si * 18, this.topPos + 112);
      }

      RenderSystem.disableBlend();
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      ShopStyle.gold(g, this.font, this.entity, this.pRelX + 6, this.pRelY + 20);

      for (int c = 0; c < 3; c++) {
         int cx = COLS[c] + 8 - this.font.width(COL_PRICE[c]) / 2;
         g.drawString(this.font, COL_PRICE[c], cx, ROWS[0] - 13, -9882, false);
      }

      for (int r = 0; r < 3; r++) {
         g.drawString(this.font, ROW_LABEL[r], COLS[0] - 30, ROWS[r] + 4, -1509633, false);
      }
   }

   private int shopSlotAt(double mouseX, double mouseY) {
      for (int r = 0; r < 3; r++) {
         for (int c = 0; c < 3; c++) {
            int sx = this.leftPos + COLS[c] - 1;
            int sy = this.topPos + ROWS[r] - 1;
            if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18) {
               return r * 3 + c;
            }
         }
      }

      return -1;
   }

   @Override
   protected void renderExtras(GuiGraphics g, int mouseX, int mouseY) {
      int hovered = this.shopSlotAt(mouseX, mouseY);
      if (hovered >= 0) {
         ShopStyle.slotHover(g, this.leftPos + COLS[hovered % 3], this.topPos + ROWS[hovered / 3]);
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isOpen() && button == 0) {
         int slot = this.shopSlotAt(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY));
         if (slot >= 0) {
            int id = slot + 1;
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorePotionNewButtonMessage(id, this.x, this.y, this.z));
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public void init() {
      super.init();
      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.leftPos + this.pRelX + 3,
            this.topPos + this.pRelY + 2,
            40,
            12,
            Component.literal("< Back"),
            b -> SololevelingMod.PACKET_HANDLER.sendToServer(new StorePotionNewButtonMessage(0, this.x, this.y, this.z))
         )
      );
   }
}
