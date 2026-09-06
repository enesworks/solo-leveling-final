package dev.eness.sololevelingfinal.core.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.procedures.GoldTextProcedure;

public final class ShopStyle {
   public static final int ACCENT = -12597505;
   public static final int ACCENT_DIM = -14519384;
   public static final int ACCENT_SOFT = 1430243071;
   public static final int PANEL_FILL = -1727591398;
   public static final int SLOT_FILL = -1341450972;
   public static final int TEXT_MAIN = -1509633;
   public static final int TEXT_SUB = -7358248;
   public static final int GOLD = -9882;

   private ShopStyle() {
   }

   public static void panel(GuiGraphics g, int x, int y, int w, int h) {
      g.fill(x, y, x + w, y + h, -1727591398);
      g.fill(x - 1, y - 1, x + w + 1, y, 1430243071);
      g.fill(x - 1, y + h, x + w + 1, y + h + 1, 1430243071);
      g.fill(x - 1, y, x, y + h, 1430243071);
      g.fill(x + w, y, x + w + 1, y + h, 1430243071);
      g.fill(x, y, x + w, y + 1, -14519384);
      g.fill(x, y + h - 1, x + w, y + h, -14519384);
      g.fill(x, y, x + 1, y + h, -14519384);
      g.fill(x + w - 1, y, x + w, y + h, -14519384);
      corner(g, x, y, w, h);
   }

   private static void corner(GuiGraphics g, int x, int y, int w, int h) {
      int l = 10;
      g.fill(x - 1, y - 1, x + l, y + 1, -12597505);
      g.fill(x - 1, y - 1, x + 1, y + l, -12597505);
      g.fill(x + w - l, y - 1, x + w + 1, y + 1, -12597505);
      g.fill(x + w - 1, y - 1, x + w + 1, y + l, -12597505);
      g.fill(x - 1, y + h - 1, x + l, y + h + 1, -12597505);
      g.fill(x - 1, y + h - l, x + 1, y + h + 1, -12597505);
      g.fill(x + w - l, y + h - 1, x + w + 1, y + h + 1, -12597505);
      g.fill(x + w - 1, y + h - l, x + w + 1, y + h + 1, -12597505);
   }

   public static void titleBar(GuiGraphics g, Font font, int x, int y, int w, String title) {
      g.fill(x, y, x + w, y + 16, 1712335422);
      g.fill(x, y + 16, x + w, y + 17, -12597505);
      g.drawString(font, title, x + (w - font.width(title)) / 2, y + 4, -12597505, false);
   }

   public static void slot(GuiGraphics g, int slotX, int slotY) {
      int x = slotX - 1;
      int y = slotY - 1;
      g.fill(x, y, x + 18, y + 18, -1341450972);
      g.fill(x, y, x + 18, y + 1, -14519384);
      g.fill(x, y + 17, x + 18, y + 18, -14519384);
      g.fill(x, y, x + 1, y + 18, -14519384);
      g.fill(x + 17, y, x + 18, y + 18, -14519384);
   }

   public static void slotHover(GuiGraphics g, int slotX, int slotY) {
      int x = slotX - 1;
      int y = slotY - 1;
      g.fill(x, y, x + 18, y + 18, 1346356991);
   }

   public static void gold(GuiGraphics g, Font font, Entity entity, int x, int y) {
      g.drawString(font, GoldTextProcedure.execute(entity), x, y, -9882, false);
   }
}
