package dev.eness.sololevelingfinal.core.client.screens;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Post;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.client.gui.DungeonBuilderClientState;
import dev.eness.sololevelingfinal.core.item.DungeonBuilderWandItem;
import dev.eness.sololevelingfinal.core.network.DungeonBuilderStatusMessage;
import dev.eness.sololevelingfinal.core.util.DungeonBuilderMode;

@EventBusSubscriber(Dist.CLIENT)
public final class DungeonBuilderHudOverlay {
   private static final int CYAN = -11346443;
   private static final int GREEN = -10164598;
   private static final int YELLOW = -14249;
   private static final int RED = -41113;
   private static final int WHITE = -853249;
   private static final int MUTED = -5653562;
   private static final int DIM = -9337971;

   private DungeonBuilderHudOverlay() {
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void render(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null
         && minecraft.level != null
         && !minecraft.options.hideGui
         && minecraft.screen == null
         && DungeonBuilderMode.isActive(minecraft.level)) {
         DungeonBuilderStatusMessage.View view = DungeonBuilderClientState.view();
         if (view.active()) {
            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();
            int width = Math.max(190, Math.min(254, screenWidth - 12));
            int x = Math.max(6, screenWidth - width - 6);
            int y = 6;
            int fixedHeight = 112;
            int maxLines = Math.max(3, Math.min(view.lines().size(), (screenHeight - fixedHeight - 18) / 11));
            int height = fixedHeight + maxLines * 11;
            if (height > screenHeight - 12) {
               maxLines = Math.max(1, (screenHeight - fixedHeight - 12) / 11);
               height = fixedHeight + maxLines * 11;
            }

            renderPanel(event.getGuiGraphics(), minecraft.font, view, x, y, width, height, maxLines);
         }
      }
   }

   private static void renderPanel(GuiGraphics graphics, Font font, DungeonBuilderStatusMessage.View view, int x, int y, int width, int height, int maxLines) {
      int accent = view.errors() == 0 ? -10164598 : -41113;
      drawPanel(graphics, x, y, width, height, accent);
      graphics.drawString(font, "DUNGEON BUILDER", x + 8, y + 6, -11346443, false);
      String result = view.errors() == 0 ? "READY" : view.errors() + " ERROR" + (view.errors() == 1 ? "" : "S");
      graphics.drawString(font, result, x + width - 8 - font.width(result), y + 6, accent, false);
      graphics.drawString(font, fit(font, view.projectId(), width - 16), x + 8, y + 18, -853249, false);
      String type = view.type().toUpperCase(Locale.ROOT) + "  |  RANK " + view.ranks();
      graphics.drawString(font, fit(font, type, width - 16), x + 8, y + 29, -5653562, false);
      graphics.drawString(font, fit(font, "ONE MODULE PROJECT = ONE ROOM", width - 16), x + 8, y + 40, -11346443, false);
      String tool = heldTool();
      graphics.drawString(font, fit(font, tool, width - 16), x + 8, y + 52, tool.startsWith("Wand:") ? -14249 : -9337971, false);
      graphics.drawString(font, fit(font, "Group: " + view.group() + "  |  Pool: " + view.pool(), width - 16), x + 8, y + 63, -5653562, false);
      graphics.drawString(font, fit(font, "Bounds: " + view.bounds(), width - 16), x + 8, y + 74, -5653562, false);
      String counts = view.regionCount() + " regions  |  " + view.socketCount() + " sockets  |  " + view.markerCount() + " markers";
      graphics.drawString(font, fit(font, counts, width - 16), x + 8, y + 85, -9337971, false);
      int lineY = y + 98;
      List<DungeonBuilderStatusMessage.StatusLine> lines = view.lines();

      for (int index = 0; index < Math.min(maxLines, lines.size()); index++) {
         DungeonBuilderStatusMessage.StatusLine line = lines.get(index);

         String prefix = switch (line.status()) {
            case 0 -> "[OK] ";
            default -> "[ ] ";
            case 2 -> "[!] ";
            case 3 -> "[i] ";
            case 4 -> "[~] ";
         };
         int color = statusColor(line.status());
         String text = prefix + line.label() + ": " + line.detail();
         graphics.drawString(font, fit(font, text, width - 16), x + 8, lineY, color, false);
         lineY += 11;
      }

      if (!view.pending().isBlank()) {
         graphics.drawString(font, fit(font, view.pending(), width - 16), x + 8, y + height - 12, -14249, false);
      } else {
         graphics.drawString(font, fit(font, "N: Builder Studio  |  Sneak+RMB: wand mode", width - 16), x + 8, y + height - 12, -9337971, false);
      }
   }

   private static String heldTool() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null) {
         return "Hold a builder wand to see its mode";
      }

      ItemStack stack = minecraft.player.getMainHandItem();
      if (!(stack.getItem() instanceof DungeonBuilderWandItem)) {
         stack = minecraft.player.getOffhandItem();
      }

      return stack.getItem() instanceof DungeonBuilderWandItem wand
         ? "Wand: " + human(wand.tool().name()) + "  |  Mode: " + human(wand.currentMode(stack))
         : "Hold a builder wand to see its mode";
   }

   private static String human(String value) {
      String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT).replace('_', ' ');
      StringBuilder result = new StringBuilder();

      for (String word : normalized.split(" ")) {
         if (!word.isBlank()) {
            if (!result.isEmpty()) {
               result.append(' ');
            }

            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
         }
      }

      return result.toString();
   }

   private static int statusColor(int status) {
      return switch (status) {
         case 0 -> -10164598;
         case 1, 4 -> -14249;
         case 2 -> -41113;
         case 3 -> -11346443;
         default -> -5653562;
      };
   }

   private static String fit(Font font, String text, int width) {
      if (font.width(text) <= width) {
         return text;
      }

      String ellipsis = "...";
      return font.plainSubstrByWidth(text, Math.max(1, width - font.width(ellipsis))) + ellipsis;
   }

   private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, int accent) {
      graphics.fill(x, y, x + width, y + height, -519433960);
      graphics.fill(x, y, x + width, y + 2, accent);
      graphics.fill(x, y + height - 1, x + width, y + height, -869710766);
      graphics.fill(x, y, x + 1, y + height, -869710766);
      graphics.fill(x + width - 1, y, x + width, y + height, accent);
      graphics.fill(x + 4, y + 4, x + 16, y + 5, -1437409803);
      graphics.fill(x + 4, y + 4, x + 5, y + 16, -1437409803);
   }
}
