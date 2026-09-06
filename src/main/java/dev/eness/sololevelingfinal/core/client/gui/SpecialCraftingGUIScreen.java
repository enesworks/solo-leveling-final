package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemContainerScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemPanelScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemTooltip;
import dev.eness.sololevelingfinal.core.network.SpecialCraftingGUIButtonMessage;
import dev.eness.sololevelingfinal.core.world.inventory.SpecialCraftingGUIMenu;

public class SpecialCraftingGUIScreen extends SystemContainerScreen<SpecialCraftingGUIMenu> {
   private static final int SLOT_0_X = -8;
   private static final int SLOT_0_Y = -120;
   private static final int SLOT_1_X = 42;
   private static final int SLOT_1_Y = -48;
   private static final int SLOT_2_X = -58;
   private static final int SLOT_2_Y = -48;
   private static final int OUTPUT_X = -8;
   private static final int OUTPUT_Y = -74;
   private static final int INV_X = -81;
   private static final int INV_Y = 28;
   private static final int HOTBAR_Y = 86;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;

   public SpecialCraftingGUIScreen(SpecialCraftingGUIMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.pRelX = -113;
      this.pRelY = -140;
      this.pW = 226;
      this.pH = 286;
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int gx, int gy) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      int ax = this.leftPos + this.pRelX;
      int ay = this.topPos + this.pRelY;
      ShopStyle.panel(g, ax, ay, this.pW, this.pH);
      ShopStyle.titleBar(g, this.font, ax, ay, this.pW, "SYSTEM CRAFTING");
      this.renderCraftingSigil(g);
      ShopStyle.slot(g, this.leftPos + -8, this.topPos + -120);
      ShopStyle.slot(g, this.leftPos + 42, this.topPos + -48);
      ShopStyle.slot(g, this.leftPos + -58, this.topPos + -48);
      ShopStyle.slot(g, this.leftPos + -8, this.topPos + -74);
      this.outlineSlot(g, this.leftPos + -8, this.topPos + -74, -1438660865);

      for (int si = 0; si < 3; si++) {
         for (int sj = 0; sj < 9; sj++) {
            ShopStyle.slot(g, this.leftPos + -81 + sj * 18, this.topPos + 28 + si * 18);
         }
      }

      for (int si = 0; si < 9; si++) {
         ShopStyle.slot(g, this.leftPos + -81 + si * 18, this.topPos + 86);
      }

      RenderSystem.disableBlend();
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      g.drawString(this.font, "INVENTORY", this.pRelX + 16, 14, -7358248, false);
   }

   @Override
   protected void renderExtras(GuiGraphics g, int mouseX, int mouseY) {
      this.highlightHoveredSlot(g, mouseX, mouseY);
      List<Component> tip = this.slotTip(mouseX, mouseY);
      if (tip != null) {
         SystemTooltip.render(g, this.font, tip, mouseX, mouseY, this.width, this.height);
      }
   }

   @Override
   public void init() {
      super.init();
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.leftPos + this.pRelX + 4, this.topPos + this.pRelY + 3, 46, 13, Component.literal("< Back"), b -> {
            if (this.minecraft != null && this.minecraft.player != null) {
               this.minecraft.player.closeContainer();
               this.openSystemScreen(new SystemPanelScreen());
            }
         })
      );
      this.addRenderableWidget(new SystemScreen.SystemButton(this.leftPos - 34, this.topPos - 18, 68, 20, Component.literal("Craft"), b -> this.craft()));
   }

   private void craft() {
      SololevelingMod.PACKET_HANDLER.sendToServer(new SpecialCraftingGUIButtonMessage(0, this.x, this.y, this.z));
   }

   private void renderCraftingSigil(GuiGraphics g) {
      int cx = this.leftPos;
      int cy = this.topPos - 72;
      int glow = 1430243071;
      int topX = this.slotCenterX(-8);
      int topY = this.slotCenterY(-120);
      int rightX = this.slotCenterX(42);
      int rightY = this.slotCenterY(-48);
      int leftX = this.slotCenterX(-58);
      int leftY = this.slotCenterY(-48);
      this.drawLine(g, topX, topY, rightX, rightY, glow);
      this.drawLine(g, rightX, rightY, leftX, leftY, glow);
      this.drawLine(g, leftX, leftY, topX, topY, glow);
      this.drawRing(g, cx, cy, 54, glow);
      this.drawRing(g, cx, cy, 35, 860862696);
   }

   private int slotCenterX(int slotX) {
      return this.leftPos + slotX + 8;
   }

   private int slotCenterY(int slotY) {
      return this.topPos + slotY + 8;
   }

   private void drawRing(GuiGraphics g, int cx, int cy, int r, int color) {
      for (int i = -r; i <= r; i += 3) {
         int h = (int)Math.sqrt(Math.max(0, r * r - i * i));
         if (Math.abs(i) % 9 == 0) {
            g.fill(cx + i, cy - h, cx + i + 1, cy - h + 2, color);
            g.fill(cx + i, cy + h - 1, cx + i + 1, cy + h + 1, color);
         }

         if (Math.abs(h) % 9 == 0) {
            g.fill(cx - h, cy + i, cx - h + 2, cy + i + 1, color);
            g.fill(cx + h - 1, cy + i, cx + h + 1, cy + i + 1, color);
         }
      }
   }

   private void drawLine(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
      int dx = Math.abs(x1 - x0);
      int dy = Math.abs(y1 - y0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int err = dx - dy;

      while (true) {
         g.fill(x0, y0, x0 + 1, y0 + 1, color);
         if (x0 == x1 && y0 == y1) {
            return;
         }

         int e2 = err * 2;
         if (e2 > -dy) {
            err -= dy;
            x0 += sx;
         }

         if (e2 < dx) {
            err += dx;
            y0 += sy;
         }
      }
   }

   private List<Component> slotTip(int mouseX, int mouseY) {
      if (this.overSlot(mouseX, mouseY, -8, -120)) {
         return List.of(tipTitle("Input I"), tipText("Blood, stick, or blaze rod."));
      } else if (this.overSlot(mouseX, mouseY, 42, -48)) {
         return List.of(tipTitle("Input II"), tipText("World Tree fragment or Kamish tooth."));
      } else if (this.overSlot(mouseX, mouseY, -58, -48)) {
         return List.of(tipTitle("Input III"), tipText("Spring water or S-rank mana crystal."));
      } else {
         return this.overSlot(mouseX, mouseY, -8, -74) ? List.of(tipTitle("Output"), tipText("Crafted result appears here.")) : null;
      }
   }

   private static Component tipTitle(String text) {
      return Component.literal(text).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
   }

   private static Component tipText(String text) {
      return Component.literal(text).withStyle(ChatFormatting.GRAY);
   }

   private void highlightHoveredSlot(GuiGraphics g, int mouseX, int mouseY) {
      if (this.overSlot(mouseX, mouseY, -8, -120)) {
         ShopStyle.slotHover(g, this.leftPos + -8, this.topPos + -120);
      } else if (this.overSlot(mouseX, mouseY, 42, -48)) {
         ShopStyle.slotHover(g, this.leftPos + 42, this.topPos + -48);
      } else if (this.overSlot(mouseX, mouseY, -58, -48)) {
         ShopStyle.slotHover(g, this.leftPos + -58, this.topPos + -48);
      } else if (this.overSlot(mouseX, mouseY, -8, -74)) {
         ShopStyle.slotHover(g, this.leftPos + -8, this.topPos + -74);
      }
   }

   private void outlineSlot(GuiGraphics g, int slotX, int slotY, int color) {
      int x = slotX - 2;
      int y = slotY - 2;
      g.fill(x, y, x + 20, y + 1, color);
      g.fill(x, y + 19, x + 20, y + 20, color);
      g.fill(x, y, x + 1, y + 20, color);
      g.fill(x + 19, y, x + 20, y + 20, color);
   }

   private boolean overSlot(int mouseX, int mouseY, int slotX, int slotY) {
      int sx = this.leftPos + slotX - 1;
      int sy = this.topPos + slotY - 1;
      return mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18;
   }
}
