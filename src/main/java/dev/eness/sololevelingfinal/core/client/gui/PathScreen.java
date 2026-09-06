package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemContainerScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemQuestsScreen;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DkcTowerBackgroundRenderTypes;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.network.PathButtonMessage;
import dev.eness.sololevelingfinal.core.world.inventory.PathMenu;

public class PathScreen extends SystemContainerScreen<PathMenu> {
   private static final int PANEL_W = 360;
   private static final int PANEL_H = 294;
   private static final int HEADER_H = 26;
   private static final int VIEW_X = 9;
   private static final int VIEW_Y = 31;
   private static final int VIEW_W = 212;
   private static final int VIEW_H = 228;
   private static final int VIEW_HEADER_H = 16;
   private static final int TOWER_TOP = 47;
   private static final int TOWER_H = 211;
   private static final int SCROLLBAR_RESERVE = 12;
   private static final int DETAIL_X = 229;
   private static final int DETAIL_Y = 31;
   private static final int DETAIL_W = 122;
   private static final int DETAIL_H = 228;
   private static final int FOOTER_Y = 265;
   private static final int FOOTER_H = 20;
   private static final int FLOOR_STEP = 54;
   private static final int CONTENT_PAD = 30;
   private static final int FOUNDATION_H = 52;
   private static final int CONTENT_HEIGHT = 1138;
   private static final int PLAQUE_W = 106;
   private static final int PLAQUE_H = 34;
   private static final int LANDMARK_W = 120;
   private static final int LANDMARK_H = 40;
   private static final int NUMERAL_W = 27;
   private static final int CRIMSON = -50608;
   private static final int CRIMSON_HOT = -34219;
   private static final int CRIMSON_DIM = -7463121;
   private static final int CRIMSON_SOFT = 1728001357;
   private static final int EMBER = -26040;
   private static final int GOLD = -15261;
   private static final int GOLD_DIM = -6657499;
   private static final int VIOLET = -2847489;
   private static final int CLEARED = -3249032;
   private static final int LOCKED_BORDER = -10860720;
   private static final int TEXT_MAIN = -4624;
   private static final int TEXT_SUB = -2774865;
   private static final int TEXT_MUTED = -7441542;
   private static final int VOID_INK = -16383482;
   private PathScreen.DkcButton backButton;
   private PathScreen.DkcButton enterButton;
   private PathScreen.DkcButton focusButton;
   private PathScreen.DkcButton exitButton;
   private int selectedFloor;
   private int hoveredFloor;
   private float scroll;
   private float targetScroll;
   private boolean draggingTower;
   private boolean draggingScrollbar;
   private boolean returningToQuests;
   private boolean travelRequested;
   private int pendingFloor;
   private boolean pendingExit;
   private final float[] floorGlow = new float[21];
   private long selectionChangedAt;
   private float lastMouseX = 0.5F;
   private float lastMouseY = 0.5F;
   private float mouseVelocityX;
   private float mouseVelocityY;
   private boolean mouseSampled;
   private float focusRatio = -1.0F;

   public PathScreen(PathMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.pRelX = -180;
      this.pRelY = -147;
      this.pW = 360;
      this.pH = 294;
   }

   @Override
   protected void init() {
      this.returningToQuests = false;
      this.travelRequested = false;
      this.pendingFloor = 0;
      this.pendingExit = false;
      this.draggingTower = false;
      this.draggingScrollbar = false;
      this.mouseSampled = false;
      this.mouseVelocityX = 0.0F;
      this.mouseVelocityY = 0.0F;
      this.focusRatio = -1.0F;
      Arrays.fill(this.floorGlow, 0.0F);
      super.init();
      int left = this.panelLeft();
      int top = this.panelTop();
      int initialFloor = this.menu.insideDkc() && this.menu.currentFloor() > 0
         ? this.menu.currentFloor()
         : (this.menu.highestUnlockedFloor() > 0 ? this.menu.highestUnlockedFloor() : this.menu.currentFloor());
      this.selectedFloor = clampFloor(initialFloor <= 0 ? 1 : initialFloor);
      this.selectionChangedAt = Util.getMillis();
      this.targetScroll = this.focusOffset(this.selectedFloor);
      this.scroll = this.targetScroll;
      this.backButton = this.addRenderableWidget(
         new PathScreen.DkcButton(left + 6, top + 5, 46, 15, () -> Component.literal("< BACK"), () -> true, button -> this.returnToQuestHub())
      );
      boolean inside = this.menu.insideDkc();
      this.focusButton = this.addRenderableWidget(
         new PathScreen.DkcButton(
            left + 9,
            top + 265,
            inside ? 104 : 212,
            20,
            () -> Component.literal(this.menu.insideDkc() ? "FIND CURRENT" : "FIND HIGHEST"),
            () -> true,
            button -> this.focusProgressFloor()
         )
      );
      if (inside) {
         this.exitButton = this.addRenderableWidget(
            new PathScreen.DkcButton(
               left + 9 + 108, top + 265, 104, 20, () -> Component.literal("EXIT CASTLE"), () -> !this.travelRequested, button -> this.exitCastle()
            )
         );
      }

      this.enterButton = this.addRenderableWidget(
         new PathScreen.DkcButton(left + 229 + 4, top + 265, 114, 20, this::enterButtonText, this::canEnterSelected, button -> this.enterSelectedFloor())
      );
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      this.advanceAnimation();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      graphics.drawManaged(() -> this.renderPanel(graphics, mouseX, mouseY));
      RenderSystem.disableBlend();
   }

   private void renderPanel(GuiGraphics graphics, int mouseX, int mouseY) {
      int left = this.panelLeft();
      int top = this.panelTop();
      graphics.fillGradient(left, top, left + 360, top + 294, 1494352649, 1845690372);
      drawPanelFrame(graphics, left, top, 360, 294);
      this.renderHeader(graphics, left, top);
      int viewLeft = left + 9;
      int viewTop = top + 31;
      this.renderViewportChrome(graphics, viewLeft, viewTop);
      int towerTop = top + 47;
      ResponsiveGuiScale.enableScissor(graphics, this.responsiveTransform(), viewLeft + 1, towerTop, viewLeft + 212 - 1, towerTop + 211);
      this.renderTower(graphics, mouseX, mouseY, viewLeft, towerTop);
      graphics.disableScissor();
      this.renderScrollbar(graphics, viewLeft, towerTop);
      int detailLeft = left + 229;
      int detailTop = top + 31;
      this.renderSelectionLink(graphics, viewLeft, towerTop, detailLeft);
      this.renderDetailPanel(graphics, detailLeft, detailTop);
      this.renderFooterRail(graphics, left, top);
   }

   @Override
   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
   }

   private void renderHeader(GuiGraphics graphics, int left, int top) {
      graphics.fillGradient(left + 1, top + 1, left + 360 - 1, top + 26, -433453551, -871431672);
      graphics.fill(left + 1, top + 8, left + 360 - 1, top + 9, 419416000);
      graphics.fill(left + 1, top + 13, left + 360 - 1, top + 14, 268435456);
      graphics.fill(left + 1, top + 26 - 1, left + 360 - 1, top + 26, -7463121);
      graphics.fill(left + 120, top + 26 - 1, left + 240, top + 26, -50608);
      String title = "DEMON KING'S CASTLE";
      int titleWidth = this.trackedWidth(title, 1);
      int titleX = left + (360 - titleWidth) / 2;
      this.drawTracked(graphics, title, titleX + 1, top + 8, -870710776, 1);
      this.drawTracked(graphics, title, titleX, top + 7, -34219, 1);
      drawOrnament(graphics, titleX - 9, top + 10, -7463121);
      drawOrnament(graphics, titleX + titleWidth + 7, top + 10, -7463121);
      String tally = String.format("%02d", this.menu.clearedFloors()) + "/20";
      int tallyWidth = this.font.width(tally);
      int tallyX = left + 360 - 7 - tallyWidth;
      graphics.drawString(this.font, tally, tallyX, top + 7, this.menu.conquered() ? -15261 : -3249032, false);
      String tallyLabel = "CLEARED";
      int labelWidth = this.trackedWidth(tallyLabel, 1);
      this.drawTracked(graphics, tallyLabel, tallyX - 5 - labelWidth, top + 7, -7441542, 1);
      this.renderConquestBar(graphics, left + 8, top + 19, 344);
   }

   private void renderConquestBar(GuiGraphics graphics, int x, int y, int width) {
      int floors = 20;
      graphics.fill(x - 1, y - 1, x + width + 1, y + 5, -1727725309);

      for (int floor = 1; floor <= floors; floor++) {
         int index = floor - 1;
         int x0 = x + width * index / floors;
         int x1 = x + width * (index + 1) / floors - 1;
         boolean cleared = this.menu.isFloorCleared(floor);
         boolean unlocked = this.menu.isFloorUnlocked(floor);
         int fill = cleared ? -4968378 : (unlocked ? -10611422 : -15003112);
         graphics.fill(x0, y, x1, y + 4, fill);
         if (cleared) {
            graphics.fill(x0, y, x1, y + 1, -50608);
         }

         if (isLandmark(floor)) {
            graphics.fill(x0, y + 3, x1, y + 4, !cleared && !unlocked ? -6657499 : -15261);
         }

         if (this.menu.insideDkc() && this.menu.currentFloor() == floor) {
            int alpha = 150 + (int)(105.0F * pulse01());
            graphics.fill(x0 - 1, y - 1, x1 + 1, y + 5, alpha << 24 | 16761955);
         }
      }
   }

   private void renderViewportChrome(GuiGraphics graphics, int viewLeft, int viewTop) {
      graphics.fillGradient(viewLeft, viewTop, viewLeft + 212, viewTop + 228, 1711472900, -1946025981);
      drawOutline(graphics, viewLeft, viewTop, 212, 228, -7463121);
      graphics.fill(viewLeft + 1, viewTop + 1, viewLeft + 212 - 1, viewTop + 16 - 1, -1306524144);
      graphics.fill(viewLeft + 1, viewTop + 16 - 1, viewLeft + 212 - 1, viewTop + 16, -7463121);
      this.drawTracked(graphics, "ASCENT", viewLeft + 6, viewTop + 4, -2774865, 1);
      String marker = this.menu.insideDkc() && this.menu.currentFloor() > 0
         ? "F" + String.format("%02d", this.menu.currentFloor())
         : "F" + String.format("%02d", clampFloor(this.selectedFloor));
      String suffix = " / 20";
      int suffixWidth = this.font.width(suffix);
      int markerWidth = this.font.width(marker);
      int markerX = viewLeft + 212 - 8 - suffixWidth - markerWidth;
      graphics.drawString(this.font, marker, markerX, viewTop + 4, this.menu.insideDkc() ? -15261 : -34219, false);
      graphics.drawString(this.font, suffix, markerX + markerWidth, viewTop + 4, -7441542, false);
   }

   private void renderSelectionLink(GuiGraphics graphics, int viewLeft, int towerTop, int detailLeft) {
      PathScreen.NodeBounds node = this.nodeBounds(clampFloor(this.selectedFloor), viewLeft, towerTop, Math.round(this.scroll));
      int linkY = node.y() + node.height() / 2;
      if (linkY >= towerTop + 4 && linkY <= towerTop + 211 - 4) {
         int x0 = viewLeft + 212;
         int x1 = detailLeft;
         float flash = Math.max(0.0F, 1.0F - (float)(Util.getMillis() - this.selectionChangedAt) / 320.0F);
         graphics.fill(x0, linkY, x1, linkY + 1, flash > 0.0F ? -34219 : -7463121);
         graphics.fill(x1 - 3, linkY - 2, x1 - 2, linkY + 3, -34219);
         graphics.fill(x1 - 2, linkY - 1, x1 - 1, linkY + 2, -34219);
         int pulseX = x0 + Math.round((x1 - x0 - 2) * (flash > 0.0F ? 1.0F - flash : pulse01()));
         graphics.fill(pulseX, linkY - 1, pulseX + 2, linkY + 2, -15261);
      }
   }

   private void renderFooterRail(GuiGraphics graphics, int left, int top) {
      int y = top + 265 - 4;
      graphics.fill(left + 9, y, left + 9 + 212, y + 1, 1716983331);
      graphics.fill(left + 229, y, left + 229 + 122, y + 1, 1716983331);
   }

   private void renderTower(GuiGraphics graphics, int mouseX, int mouseY, int viewLeft, int towerTop) {
      int centerX = viewLeft + 100;
      int towerBottom = towerTop + 211;
      int roundedScroll = Math.round(this.scroll);
      this.hoveredFloor = this.floorAt(mouseX, mouseY, viewLeft, towerTop);
      float time = (float)(Util.getMillis() % 600000L) / 1000.0F;
      this.renderHaze(graphics, viewLeft, towerTop, roundedScroll);
      int summitTop = towerTop + floorContentY(20) - roundedScroll;
      if (summitTop > towerTop - 40) {
         this.renderSummit(graphics, centerX, summitTop);
      }

      for (int floor = 20; floor >= 1; floor--) {
         int y = towerTop + floorContentY(floor) - roundedScroll;
         if (y <= towerBottom + 54 && y + 54 >= towerTop - 54) {
            this.renderStorey(graphics, floor, centerX, y);
         }
      }

      int baseBottom = towerTop + floorContentY(1) + 54 - roundedScroll;
      if (baseBottom < towerBottom + 40) {
         this.renderFoundation(graphics, centerX, baseBottom);
      }

      for (int floor = 20; floor >= 1; floor--) {
         PathScreen.NodeBounds node = this.nodeBounds(floor, viewLeft, towerTop, roundedScroll);
         if (node.y() <= towerBottom + 8 && node.y() + node.height() >= towerTop - 8) {
            this.renderFloorPlaque(graphics, floor, node);
         }
      }

      this.renderEmbers(graphics, viewLeft + 1, towerTop, 210, 211, time);
      graphics.fillGradient(viewLeft + 1, towerTop, viewLeft + 212 - 1, towerTop + 20, -435813882, 393734);
      graphics.fillGradient(viewLeft + 1, towerBottom - 24, viewLeft + 212 - 1, towerBottom, 393734, -435813882);
   }

   private void renderHaze(GuiGraphics graphics, int viewLeft, int towerTop, int roundedScroll) {
      int parallax = Math.round(roundedScroll * 0.34F);

      for (int band = 0; band < 9; band++) {
         int bandY = towerTop + Math.floorMod(band * 74 - parallax, 361) - 60;
         if (bandY + 26 >= towerTop && bandY <= towerTop + 211) {
            int alpha = 22 + band % 3 * 9;
            graphics.fillGradient(viewLeft + 1, bandY, viewLeft + 212 - 1, bandY + 26, alpha << 24 | 5378082, 3279381);
         }
      }
   }

   private void renderStorey(GuiGraphics graphics, int floor, int centerX, int y) {
      int halfWidth = shaftHalfWidth(floor);
      int top = y;
      int bottom = y + 54;
      boolean lit = this.menu.isFloorUnlocked(floor);
      boolean cleared = this.menu.isFloorCleared(floor);
      graphics.fill(centerX - halfWidth, top, centerX + halfWidth, bottom, lit ? -233173998 : -233961458);
      this.renderMasonry(graphics, floor, centerX, halfWidth, top + 5, bottom);
      renderShaftShading(graphics, centerX, halfWidth, top, bottom);
      int overhang = 5;
      graphics.fill(centerX - halfWidth - overhang, top, centerX + halfWidth + overhang, top + 5, lit ? -14675686 : -15397355);
      graphics.fill(centerX - halfWidth - overhang, top, centerX + halfWidth + overhang, top + 1, lit ? -1717554104 : 1715877952);
      graphics.fill(centerX - halfWidth - overhang, top + 5, centerX + halfWidth + overhang, top + 6, -1946157056);
      graphics.fill(centerX - halfWidth, top, centerX - halfWidth + 2, bottom, cleared ? -862047162 : (lit ? -1149361100 : -2008664000));
      graphics.fill(centerX + halfWidth - 2, top, centerX + halfWidth, bottom, cleared ? -1720835792 : (lit ? -2008016860 : 1714431016));
      graphics.fill(centerX - halfWidth - 1, top, centerX - halfWidth, bottom, -872415232);
      graphics.fill(centerX + halfWidth, top, centerX + halfWidth + 1, bottom, -872415232);
      int selfHalf = (isLandmark(floor) ? 40 : 34) / 2;
      int nextHalf = floor > 1 ? (isLandmark(floor - 1) ? 40 : 34) / 2 : 8;
      this.renderStairFlight(graphics, floor, centerX, top + selfHalf + 1, top + 54 - nextHalf - 1, lit);
      this.renderTorch(graphics, centerX - halfWidth + 7, top + 24, floor, lit);
      this.renderTorch(graphics, centerX + halfWidth - 9, top + 24, floor * 7 + 3, lit);
   }

   private void renderMasonry(GuiGraphics graphics, int floor, int centerX, int halfWidth, int top, int bottom) {
      int left = centerX - halfWidth + 2;
      int right = centerX + halfWidth - 2;
      int courseHeight = 7;
      int course = 0;

      for (int cy = top; cy < bottom; course++) {
         int courseBottom = Math.min(bottom, cy + courseHeight);
         graphics.fill(left, cy, right, cy + 1, 1291845632);
         graphics.fill(left, cy + 1, right, cy + 2, 352309956);
         int offset = (course & 1) == 0 ? 0 : 10;

         for (int bx = left + offset; bx < right; bx += 20) {
            float seed = hash01(floor * 37 + course, bx);
            int blockRight = Math.min(right, bx + 19);
            if (seed > 0.86F) {
               graphics.fill(bx + 1, cy + 2, blockRight, courseBottom, 1073741824);
            } else if (seed < 0.14F) {
               graphics.fill(bx + 1, cy + 2, blockRight, courseBottom, 536859332);
            }

            if (bx > left) {
               graphics.fill(bx, cy + 1, bx + 1, courseBottom, 1073741824);
            }
         }

         cy += courseHeight;
      }
   }

   private static void renderShaftShading(GuiGraphics graphics, int centerX, int halfWidth, int top, int bottom) {
      int strips = 8;

      for (int index = 0; index < strips; index++) {
         int x0 = centerX - halfWidth + 2 * halfWidth * index / strips;
         int x1 = centerX - halfWidth + 2 * halfWidth * (index + 1) / strips;
         float t = (index + 0.5F) / strips;
         float curvature = 1.0F - Math.abs(t * 2.0F - 1.0F);
         int shadow = (int)(112.0F * (1.0F - curvature) * (t > 0.5F ? 1.0F : 0.45F));
         if (shadow > 3) {
            graphics.fill(x0, top, x1, bottom, shadow << 24);
         }

         float light = Math.max(0.0F, 1.0F - Math.abs(t - 0.34F) * 3.1F);
         int warm = (int)(52.0F * light);
         if (warm > 3) {
            graphics.fill(x0, top, x1, bottom, warm << 24 | 16751206);
         }
      }
   }

   private void renderStairFlight(GuiGraphics graphics, int floor, int centerX, int top, int bottom, boolean lit) {
      int height = bottom - top;
      if (height >= 8) {
         int steps = 4;
         int run = 56;
         int stepWidth = run / steps;
         int stepHeight = Math.max(2, height / steps);
         boolean leftToRight = (floor & 1) == 0;
         int tread = lit ? -11654604 : -14016729;
         int nosing = lit ? -289256868 : -1722267568;

         for (int index = 0; index < steps; index++) {
            int sx = leftToRight ? centerX - run / 2 + index * stepWidth : centerX + run / 2 - (index + 1) * stepWidth;
            int sy = bottom - (index + 1) * stepHeight;
            graphics.fill(sx, sy, sx + stepWidth, sy + stepHeight, tread);
            graphics.fill(sx, sy, sx + stepWidth, sy + 1, nosing);
            graphics.fill(sx, sy, sx + 1, sy + stepHeight, 1711276032);
         }

         int doorX = leftToRight ? centerX + run / 2 : centerX - run / 2 - 11;
         int doorTop = bottom - steps * stepHeight;
         graphics.fill(doorX, doorTop, doorX + 11, bottom, -234552829);
         graphics.fill(doorX, doorTop, doorX + 11, doorTop + 1, lit ? -862901446 : 1715482936);
         if (lit) {
            int alpha = 80 + (int)(70.0F * pulse01());
            graphics.fill(doorX + 2, doorTop + 2, doorX + 9, bottom, alpha << 24 | 16734762);
         }
      }
   }

   private void renderTorch(GuiGraphics graphics, int x, int y, int seed, boolean lit) {
      graphics.fill(x, y + 3, x + 2, y + 8, -14018018);
      graphics.fill(x, y + 7, x + 2, y + 8, -15594996);
      if (lit) {
         float flicker = 0.55F + 0.45F * (float)Math.sin(Util.getMillis() * 0.011 + seed * 1.7);
         int height = 3 + Math.round(flicker * 2.0F);
         graphics.fill(x, y + 3 - height, x + 2, y + 3, -26040);
         graphics.fill(x, y + 2 - height, x + 2, y + 3 - height, -15261);
         int glow = 34 + (int)(34.0F * flicker);
         graphics.fill(x - 3, y - 3, x + 5, y + 6, glow << 24 | 16742960);
         graphics.fill(x - 5, y - 1, x + 7, y + 4, glow / 2 << 24 | 16734752);
      }
   }

   private void renderSummit(GuiGraphics graphics, int centerX, int summitTop) {
      int halfWidth = shaftHalfWidth(20) + 5;
      boolean conquered = this.menu.conquered();
      int merlon = conquered ? -13494754 : -14807016;

      for (int x = centerX - halfWidth; x < centerX + halfWidth; x += 13) {
         int x1 = Math.min(centerX + halfWidth, x + 8);
         graphics.fill(x, summitTop - 9, x1, summitTop, merlon);
         graphics.fill(x, summitTop - 9, x1, summitTop - 8, conquered ? -6657499 : -11916236);
      }

      graphics.fill(centerX - halfWidth, summitTop - 12, centerX + halfWidth, summitTop - 9, -14413287);

      for (int step = 0; step < 11; step++) {
         int spireHalf = 11 - step;
         int y = summitTop - 13 - step * 3;
         graphics.fill(centerX - spireHalf, y - 3, centerX + spireHalf, y, -15004139);
         graphics.fill(centerX - spireHalf, y - 3, centerX - spireHalf + 1, y, -12377556);
      }

      boolean summitOpen = this.menu.isFloorUnlocked(20);
      int beaconY = summitTop - 50;
      int alpha = summitOpen ? 150 + (int)(105.0F * pulse01()) : 60;
      graphics.fill(centerX - 2, beaconY, centerX + 2, beaconY + 5, alpha << 24 | (summitOpen ? 16761955 : 7031381));
      if (summitOpen) {
         graphics.fill(centerX - 5, beaconY + 1, centerX + 5, beaconY + 3, 1442825315);
         graphics.fill(centerX - 1, beaconY - 6, centerX + 1, beaconY, 1157612643);
      }
   }

   private void renderFoundation(GuiGraphics graphics, int centerX, int baseBottom) {
      int halfWidth = shaftHalfWidth(1);

      for (int step = 0; step < 5; step++) {
         int stepHalf = halfWidth + 3 + step * 5;
         int y = baseBottom + step * 6;
         graphics.fill(centerX - stepHalf, y, centerX + stepHalf, y + 6, -15397869);
         graphics.fill(centerX - stepHalf, y, centerX + stepHalf, y + 1, -13426646);
      }

      int gateHalf = 15;
      int gateTop = baseBottom + 4;
      int gateBottom = baseBottom + 30;
      graphics.fill(centerX - gateHalf, gateTop, centerX + gateHalf, gateBottom, -16317691);

      for (int step = 0; step < 4; step++) {
         graphics.fill(centerX - gateHalf + step, gateTop - 4 + step, centerX + gateHalf - step, gateTop - 3 + step, -14019040);
      }

      int alpha = 90 + (int)(60.0F * pulse01());
      graphics.fill(centerX - gateHalf + 3, gateTop + 3, centerX + gateHalf - 3, gateBottom, alpha << 24 | 12855838);
      graphics.fill(centerX - 1, gateTop + 3, centerX + 1, gateBottom, 2013227580);
   }

   private void renderEmbers(GuiGraphics graphics, int left, int top, int width, int height, float time) {
      for (int mote = 0; mote < 34; mote++) {
         float speed = 8.0F + mote % 11 * 3.4F;
         float sway = (float)Math.sin(time * 0.9 + mote * 1.37) * 5.0F;
         int x = left + (int)(hash01(mote, 3) * width + sway);
         int cycle = height + 60;
         int y = top + height - (int)((time * speed + hash01(mote, 7) * cycle) % cycle) + 30;
         if (x >= left && x < left + width && y >= top && y < top + height) {
            boolean bright = mote % 6 == 0;
            int size = bright ? 2 : 1;
            graphics.fill(x, y, x + size, y + size + 1, bright ? -855658411 : -1711324106);
            if (bright) {
               graphics.fill(x, y + 2, x + 1, y + 5, 1157589564);
            }
         }
      }
   }

   private void renderFloorPlaque(GuiGraphics graphics, int floor, PathScreen.NodeBounds node) {
      boolean unlocked = this.menu.isFloorUnlocked(floor);
      boolean cleared = this.menu.isFloorCleared(floor);
      boolean here = this.menu.insideDkc() && this.menu.currentFloor() == floor;
      boolean selected = this.selectedFloor == floor;
      boolean landmark = isLandmark(floor);
      float glow = this.floorGlow[floor];
      int x = node.x();
      int y = node.y();
      int width = node.width();
      int height = node.height();
      int cut = 4;
      fillChamfer(graphics, x + 2, y + 3, width, height, cut, -2113929216);
      if (landmark) {
         String badge = floor == 15 ? this.radiruBadge() : "BOSS";
         int badgeWidth = this.font.width(badge);
         int tabWidth = badgeWidth + 12;
         int tabX = x + width - 8 - tabWidth;
         int accent = floor == 15 ? -2847489 : -15261;
         fillChamfer(graphics, tabX, y - 10, tabWidth, 13, 3, floor == 15 ? -433450944 : -431805940);
         outlineChamfer(graphics, tabX, y - 10, tabWidth, 13, 3, unlocked ? accent : -10860720);
         graphics.drawString(this.font, badge, tabX + 6, y - 7, unlocked ? accent : -7441542, false);
      }

      int topFill = here ? -229303277 : (cleared ? -231269855 : (unlocked ? -230943456 : -232974046));
      int bottomFill = here ? -232847098 : (cleared ? -233436657 : (unlocked ? -233371632 : -234026479));
      fillChamfer(graphics, x, y, width, height, cut, topFill);
      graphics.fillGradient(x + 1, y + cut, x + width - 1, y + height - cut, 0, bottomFill & -1291845633);

      for (int fleck = 0; fleck < 9; fleck++) {
         int fx = x + 4 + (int)(hash01(floor * 13 + fleck, 91) * (width - 9));
         int fy = y + 3 + (int)(hash01(fleck, floor * 5 + 41) * (height - 7));
         graphics.fill(fx, fy, fx + 1, fy + 1, fleck % 3 == 0 ? 587192524 : 855638016);
      }

      int border = here ? -15261 : (cleared ? -3249032 : (unlocked ? -50608 : -10860720));
      if (selected) {
         border = -7210;
      }

      outlineChamfer(graphics, x, y, width, height, cut, border);
      graphics.fill(x + cut, y + 1, x + width - cut, y + 2, 872415231);
      graphics.fill(x + cut, y + height - 2, x + width - cut, y + height - 1, 1140850688);
      int numeralRight = x + 27;
      graphics.fill(x + 1, y + cut, numeralRight, y + height - cut, unlocked ? 1712456713 : 1711933456);
      graphics.fill(numeralRight, y + 3, numeralRight + 1, y + height - 3, unlocked ? -1719262159 : 1715746880);
      String numeral = String.valueOf(floor);
      int numeralColor = here ? -15261 : (cleared ? -3249032 : (unlocked ? -9774 : -9742754));
      int numeralX = x + 1 + (26 - this.font.width(numeral) * 2) / 2;
      int numeralY = y + (height - 16) / 2;
      this.drawScaled(graphics, numeral, numeralX + 1, numeralY + 1, 2.0F, -872415232);
      this.drawScaled(graphics, numeral, numeralX, numeralY, 2.0F, numeralColor);
      int textLeft = numeralRight + 5;
      int textWidth = x + width - 5 - textLeft;
      List<FormattedCharSequence> nameLines = this.font.split(Component.literal(DkcFloorRegistry.name(floor)), textWidth);
      int nameCount = Math.max(1, Math.min(2, nameLines.size()));
      int nameTop = y + 3 + Math.max(0, (height - 15 - nameCount * 9) / 2);

      for (int line = 0; line < nameCount; line++) {
         graphics.drawString(this.font, nameLines.get(line), textLeft, nameTop + line * 9, unlocked ? -4624 : -7441542, false);
      }

      int stateY = y + height - 12;
      int stateColor = this.floorStateColor(floor);
      graphics.fill(textLeft, stateY + 2, textLeft + 3, stateY + 5, stateColor);
      String state = this.shortFloorState(floor);
      graphics.drawString(this.font, state, textLeft + 6, stateY, stateColor, false);
      if (!DkcFloorRegistry.isBossFloor(floor)) {
         String count = "x" + DkcFloorRegistry.requiredKills(floor);
         int countX = x + width - 6 - this.font.width(count);
         if (countX > textLeft + 10 + this.font.width(state)) {
            graphics.drawString(this.font, count, countX, stateY, unlocked ? -7441542 : -10728878, false);
         }
      }

      drawRivet(graphics, x + 4, y + 4, border);
      drawRivet(graphics, x + width - 6, y + 4, border);
      drawRivet(graphics, x + 4, y + height - 6, border);
      drawRivet(graphics, x + width - 6, y + height - 6, border);
      if (glow > 0.01F) {
         int alpha = (int)(58.0F * glow);
         fillChamfer(graphics, x, y, width, height, cut, alpha << 24 | 16742997);
         int sweep = x + 2 + Math.round((width - 5) * pulse01());
         graphics.fill(sweep, y + 2, sweep + 1, y + height - 2, (int)(70.0F * glow) << 24 | 16777215);
      }

      if (selected) {
         outlineChamfer(graphics, x - 2, y - 2, width + 4, height + 4, cut + 1, 1728001357);
         int tick = 6;
         graphics.fill(x - 3, y - 3, x - 3 + tick, y - 2, -34219);
         graphics.fill(x - 3, y - 3, x - 2, y - 3 + tick, -34219);
         graphics.fill(x + width + 3 - tick, y - 3, x + width + 3, y - 2, -34219);
         graphics.fill(x + width + 2, y - 3, x + width + 3, y - 3 + tick, -34219);
         graphics.fill(x - 3, y + height + 2, x - 3 + tick, y + height + 3, -34219);
         graphics.fill(x - 3, y + height + 3 - tick, x - 2, y + height + 3, -34219);
         graphics.fill(x + width + 3 - tick, y + height + 2, x + width + 3, y + height + 3, -34219);
         graphics.fill(x + width + 2, y + height + 3 - tick, x + width + 3, y + height + 3, -34219);
      }

      if (here) {
         int alpha = 110 + (int)(110.0F * pulse01());
         outlineChamfer(graphics, x - 1, y - 1, width + 2, height + 2, cut, alpha << 24 | 16761955);
         graphics.fill(x + 9, y - 7, x + 15, y + 3, -4971462);
         graphics.fill(x + 9, y - 7, x + 15, y - 6, -15261);
         graphics.fill(x + 9, y + 3, x + 11, y + 5, -4971462);
         graphics.fill(x + 13, y + 3, x + 15, y + 5, -4971462);
      }
   }

   private void renderScrollbar(GuiGraphics graphics, int viewLeft, int towerTop) {
      int trackX = viewLeft + 212 - 8;
      int trackWidth = 5;
      int trackTop = towerTop + 3;
      int trackBottom = towerTop + 211 - 3;
      graphics.fillGradient(trackX, trackTop, trackX + trackWidth, trackBottom, -1072167927, -1073085946);
      drawOutline(graphics, trackX, trackTop, trackWidth, trackBottom - trackTop, -2007559134);
      int span = trackBottom - trackTop - 5;

      for (int floor = 1; floor <= 20; floor++) {
         float t = (20 - floor) / 19.0F;
         int notchY = trackTop + 2 + Math.round(t * span);
         boolean landmark = isLandmark(floor);
         int color = this.menu.isFloorCleared(floor) ? -3249032 : (this.menu.isFloorUnlocked(floor) ? -7463121 : -13753815);
         int inset = landmark ? 0 : 1;
         graphics.fill(
            trackX + inset,
            notchY,
            trackX + trackWidth - inset,
            notchY + 1,
            !landmark || !this.menu.isFloorUnlocked(floor) && !this.menu.isFloorCleared(floor) ? color : -15261
         );
         if (floor == clampFloor(this.selectedFloor)) {
            graphics.fill(trackX - 4, notchY - 1, trackX - 1, notchY + 2, -34219);
            graphics.fill(trackX - 2, notchY, trackX, notchY + 1, -4624);
         }

         if (this.menu.insideDkc() && this.menu.currentFloor() == floor) {
            int alpha = 150 + (int)(105.0F * pulse01());
            graphics.fill(trackX + trackWidth + 1, notchY - 1, trackX + trackWidth + 4, notchY + 2, alpha << 24 | 16761955);
         }
      }

      float maximum = maxScroll();
      int usable = 203;
      int thumbHeight = Math.max(24, Math.round(usable * 0.185413F));
      int travel = Math.max(1, usable - thumbHeight);
      int thumbY = towerTop + 4 + (maximum <= 0.0F ? 0 : Math.round(this.scroll / maximum * travel));
      graphics.fill(trackX - 1, thumbY, trackX + trackWidth + 1, thumbY + thumbHeight, 1157577296);
      drawOutline(graphics, trackX - 1, thumbY, trackWidth + 2, thumbHeight, -34219);
      graphics.fill(trackX - 1, thumbY, trackX + trackWidth + 1, thumbY + 2, -50608);
      graphics.fill(trackX - 1, thumbY + thumbHeight - 2, trackX + trackWidth + 1, thumbY + thumbHeight, -50608);
      int gripY = thumbY + thumbHeight / 2 - 3;

      for (int grip = 0; grip < 3; grip++) {
         graphics.fill(trackX + 1, gripY + grip * 3, trackX + trackWidth - 1, gripY + grip * 3 + 1, -1711290432);
      }
   }

   private void renderDetailPanel(GuiGraphics graphics, int x, int y) {
      int floor = clampFloor(this.selectedFloor);
      boolean landmark = isLandmark(floor);
      int inner = 110;
      graphics.fillGradient(x, y, x + 122, y + 228, -652212463, -1006305021);
      drawOutline(graphics, x, y, 122, 228, -7463121);
      drawCornerBrackets(graphics, x, y, 122, 228, -50608);
      graphics.fill(x + 1, y + 1, x + 122 - 1, y + 2, 587188160);
      graphics.fillGradient(x + 1, y + 1, x + 122 - 1, y + 42, -1942352869, 2229777);
      this.drawTracked(graphics, "FLOOR", x + 8, y + 7, -7441542, 1);
      String numeral = String.format("%02d", floor);
      this.drawScaled(graphics, numeral, x + 8, y + 18, 2.0F, -872415232);
      this.drawScaled(graphics, numeral, x + 7, y + 17, 2.0F, landmark ? -15261 : -34219);
      this.renderFloorSigil(graphics, x + 122 - 25, y + 21, floor);
      int cursor = y + 44;
      graphics.fill(x + 6, cursor, x + 122 - 6, cursor + 1, 1999374868);
      drawOrnament(graphics, x + 61, cursor, -7463121);
      cursor += 6;
      List<FormattedCharSequence> nameLines = this.font.split(Component.literal(DkcFloorRegistry.name(floor)), inner);

      for (int line = 0; line < Math.min(2, nameLines.size()); line++) {
         graphics.drawString(this.font, nameLines.get(line), x + 6, cursor, -4624, false);
         cursor += 10;
      }

      cursor += 2;
      int stateColor = this.floorStateColor(floor);
      String state = this.longFloorState(floor);
      int chipWidth = Math.min(inner, this.font.width(state) + 12);
      fillChamfer(graphics, x + 6, cursor, chipWidth, 12, 3, 1711276032);
      outlineChamfer(graphics, x + 6, cursor, chipWidth, 12, 3, stateColor);
      graphics.fill(x + 10, cursor + 4, x + 13, cursor + 7, stateColor);
      graphics.drawString(this.font, state, x + 16, cursor + 2, stateColor, false);
      if (floor > 1 && this.menu.isTransitionArmed(floor - 1)) {
         int permitX = x + 8 + chipWidth;
         if (permitX + this.font.width("PERMIT") + 6 <= x + 122 - 6) {
            graphics.fill(permitX, cursor + 1, permitX + 2, cursor + 11, -3249032);
            graphics.drawString(this.font, "PERMIT", permitX + 4, cursor + 2, -3249032, false);
         }
      }

      cursor += 16;
      cursor = this.drawSection(graphics, "OBJECTIVE", x + 6, cursor, inner);
      cursor = this.drawWrapped(graphics, this.floorObjective(floor), x + 6, cursor, inner, -2774865, 4);
      cursor += 4;
      cursor = this.drawSection(graphics, "GARRISON", x + 6, cursor, inner);
      cursor = this.renderGarrison(graphics, floor, x + 6, cursor, inner);
      cursor += 4;
      cursor = this.drawSection(graphics, "SPOILS", x + 6, cursor, inner);
      cursor = this.drawWrapped(graphics, this.floorReward(floor), x + 6, cursor, inner, -2774865, 4);
      int footY = y + 228 - 13;
      graphics.fill(x + 6, footY - 4, x + 122 - 6, footY - 3, 1428949524);
      boolean formed = this.menu.isFloorGenerated(floor);
      String generation = formed ? "FLOOR FORMED" : (this.menu.isFloorUnlocked(floor) ? "FORMS ON ENTRY" : "UNFORMED");
      graphics.fill(x + 6, footY + 2, x + 9, footY + 5, formed ? -3249032 : -7441542);
      graphics.drawString(this.font, generation, x + 12, footY, formed ? -3249032 : -7441542, false);
   }

   private int renderGarrison(GuiGraphics graphics, int floor, int x, int y, int width) {
      int cursor = y;
      switch (floor) {
         case 1:
            cursor = this.drawStat(graphics, "CHAMPION", "CERBERUS", x, cursor, width, -15261);
            cursor = this.drawStat(graphics, "CLASS", "BEAST", x, cursor, width, -2774865);
            break;
         case 20:
            cursor = this.drawStat(graphics, "CHAMPION", "BARAN", x, cursor, width, -15261);
            cursor = this.drawStat(graphics, "CONSORT", "KAISELIN", x, cursor, width, -15261);
            break;
         default:
            cursor = this.drawStat(graphics, "DEFENDERS", String.valueOf(DkcFloorRegistry.requiredKills(floor)), x, cursor, width, -4624);
            cursor = this.drawStat(graphics, "ENGAGED", String.valueOf(DkcFloorRegistry.activeEnemyCap(floor)), x, cursor, width, -2774865);
            int knights = Math.round(DkcFloorRegistry.knightShare(floor) * 100.0F);
            cursor = this.drawStat(graphics, "KNIGHTS", knights + "%", x, cursor, width, knights > 0 ? -26040 : -7441542);
      }

      if (DkcFloorRegistry.isBossFloor(floor) && floor != 1 && floor != 20) {
         cursor = this.drawStat(graphics, "GUARDIAN", "VULCAN", x, cursor, width, -15261);
      }

      return cursor;
   }

   private void renderFloorSigil(GuiGraphics graphics, int centerX, int centerY, int floor) {
      boolean unlocked = this.menu.isFloorUnlocked(floor);
      int ring = floor == 15 ? -2847489 : (isLandmark(floor) ? -15261 : (unlocked ? -50608 : -10860720));
      int glyph = unlocked ? (floor == 15 ? -2847489 : -34219) : -9742754;
      drawDiamond(graphics, centerX, centerY, 15, 1426063360 | ring & 16777215);
      drawDiamond(graphics, centerX, centerY, 13, ring);
      double spin = Util.getMillis() * 6.0E-4;

      for (int tick = 0; tick < 8; tick++) {
         double angle = spin + tick * Math.PI / 4.0;
         int tx = centerX + (int)Math.round(Math.cos(angle) * 9.0);
         int ty = centerY + (int)Math.round(Math.sin(angle) * 9.0);
         graphics.fill(tx, ty, tx + 1, ty + 1, unlocked ? ring : -12240832);
      }

      if (floor == 20) {
         graphics.fill(centerX - 5, centerY + 2, centerX + 6, centerY + 4, glyph);
         graphics.fill(centerX - 5, centerY - 3, centerX - 4, centerY + 2, glyph);
         graphics.fill(centerX - 1, centerY - 5, centerX, centerY + 2, glyph);
         graphics.fill(centerX + 4, centerY - 3, centerX + 5, centerY + 2, glyph);
      } else {
         drawRune(graphics, centerX, centerY, 4, floor, glyph);
      }
   }

   private int drawSection(GuiGraphics graphics, String label, int x, int y, int width) {
      graphics.fill(x, y + 1, x + 2, y + 7, -50608);
      int end = this.drawTracked(graphics, label, x + 5, y, -26040, 1);

      for (int dot = end + 4; dot < x + width; dot += 3) {
         graphics.fill(dot, y + 3, dot + 1, y + 4, 1428949524);
      }

      return y + 11;
   }

   private int drawStat(GuiGraphics graphics, String label, String value, int x, int y, int width, int valueColor) {
      graphics.drawString(this.font, label, x, y, -7441542, false);
      int valueWidth = this.font.width(value);
      int leaderStart = x + this.font.width(label) + 3;
      int leaderEnd = x + width - valueWidth - 3;

      for (int dot = leaderStart; dot < leaderEnd; dot += 3) {
         graphics.fill(dot, y + 6, dot + 1, y + 7, 1151689839);
      }

      graphics.drawString(this.font, value, x + width - valueWidth, y, valueColor, false);
      return y + 10;
   }

   private int drawWrapped(GuiGraphics graphics, String text, int x, int y, int width, int color, int maxLines) {
      List<FormattedCharSequence> lines = this.font.split(Component.literal(text), width);
      int count = Math.min(maxLines, lines.size());

      for (int index = 0; index < count; index++) {
         graphics.drawString(this.font, lines.get(index), x, y, color, false);
         y += 10;
      }

      if (lines.size() > maxLines) {
         graphics.drawString(this.font, "...", x, y - 10, color, false);
      }

      return y;
   }

   private static boolean prefersCheapBackground() {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft != null && minecraft.options != null && minecraft.options.graphicsMode().get() == GraphicsStatus.FAST;
   }

   @Override
   protected ShaderInstance backgroundShader() {
      return prefersCheapBackground() ? null : DkcTowerBackgroundRenderTypes.get();
   }

   @Override
   protected void configureBackgroundShader(ShaderInstance shader, float localX, float localY) {
      if (!this.mouseSampled) {
         this.lastMouseX = localX;
         this.lastMouseY = localY;
         this.mouseSampled = true;
      }

      float rawVelocityX = (localX - this.lastMouseX) * 11.0F;
      float rawVelocityY = (localY - this.lastMouseY) * 11.0F;
      this.mouseVelocityX = this.mouseVelocityX * 0.72F + rawVelocityX * 0.28F;
      this.mouseVelocityY = this.mouseVelocityY * 0.72F + rawVelocityY * 0.28F;
      this.lastMouseX = localX;
      this.lastMouseY = localY;
      float target = (clampFloor(this.selectedFloor) - 1) / 19.0F;
      this.focusRatio = this.focusRatio < 0.0F ? target : this.focusRatio + (target - this.focusRatio) * 0.11F;
      shader.safeGetUniform("MousePos").set(localX, localY);
      shader.safeGetUniform("MouseVelocity").set(this.mouseVelocityX, this.mouseVelocityY);
      shader.safeGetUniform("ScrollOffset").set(this.scroll);
      shader.safeGetUniform("UnlockedRatio").set(this.menu.highestUnlockedFloor() / 20.0F);
      shader.safeGetUniform("FocusRatio").set(this.focusRatio);
   }

   @Override
   protected void renderBackgroundFallback(GuiGraphics graphics, int x, int y, float localX, float localY) {
      graphics.fillGradient(x, y, x + this.pW, y + this.pH, -265616887, -268369918);
      float time = (float)(Util.getMillis() % 600000L) / 1000.0F;

      for (int band = 0; band < 7; band++) {
         int bandY = y + Math.floorMod((int)(time * (5 + band) + band * 47), this.pH);
         int alpha = 16 + band * 4;
         graphics.fill(x, bandY, x + this.pW, Math.min(y + this.pH, bandY + 6), alpha << 24 | 7013908);
      }

      for (int streak = 0; streak < 40; streak++) {
         int sx = x + Math.floorMod(streak * 53 + streak * streak, this.pW);
         int sy = y + Math.floorMod((int)(time * (150 + streak % 9 * 22) + streak * 71), this.pH);
         graphics.fill(sx, sy, sx + 1, Math.min(y + this.pH, sy + 7), 868255892);
      }

      for (int mote = 0; mote < 46; mote++) {
         int px = x + Math.floorMod(mote * 67 + mote * mote * 3, this.pW);
         int speed = 8 + mote % 17;
         int py = y + this.pH - Math.floorMod((int)(time * speed + mote * 41), this.pH);
         int color = mote % 7 == 0 ? -855664064 : -1711331010;
         graphics.fill(px, py, px + (mote % 9 == 0 ? 2 : 1), py + 1, color);
      }

      int mouseX = x + Math.round(localX * this.pW);
      int mouseY = y + Math.round(localY * this.pH);
      int radius = 22;
      int[] px = new int[5];
      int[] py = new int[5];

      for (int point = 0; point < 5; point++) {
         double angle = (-Math.PI / 2) + point * Math.PI * 2.0 / 5.0 + time * 0.25;
         px[point] = mouseX + (int)Math.round(Math.cos(angle) * radius);
         py[point] = mouseY + (int)Math.round(Math.sin(angle) * radius);
      }

      for (int point = 0; point < 5; point++) {
         drawLine(graphics, px[point], py[point], px[(point + 2) % 5], py[(point + 2) % 5], -1996540595);
      }

      drawDiamond(graphics, mouseX, mouseY, 29, 1728014925);
   }

   @Override
   protected int revealAccent() {
      return -34219;
   }

   @Override
   protected int revealAccentSoft() {
      return 1728001357;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isOpen() && button == 0) {
         int logicalX = (int)Math.round(this.logicalMouseX(mouseX));
         int logicalY = (int)Math.round(this.logicalMouseY(mouseY));
         int viewLeft = this.panelLeft() + 9;
         int towerTop = this.panelTop() + 47;
         if (!inside(logicalX, logicalY, viewLeft, towerTop, 212, 211)) {
            return super.mouseClicked(mouseX, mouseY, button);
         } else if (logicalX >= viewLeft + 212 - 11) {
            this.draggingScrollbar = true;
            this.setScrollFromScrollbar(logicalY, towerTop);
            return true;
         } else {
            int floor = this.floorAt(logicalX, logicalY, viewLeft, towerTop);
            if (floor > 0) {
               this.select(floor);
               return true;
            } else {
               this.draggingTower = true;
               return true;
            }
         }
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (button == 0 && (this.draggingTower || this.draggingScrollbar)) {
         if (this.draggingScrollbar) {
            int logicalY = (int)Math.round(this.logicalMouseY(mouseY));
            this.setScrollFromScrollbar(logicalY, this.panelTop() + 47);
         } else {
            float scale = this.responsiveTransform().scale();
            this.targetScroll = clampScroll(this.targetScroll - (float)(dragY / scale));
         }

         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      boolean handled = this.draggingTower || this.draggingScrollbar;
      this.draggingTower = false;
      this.draggingScrollbar = false;
      return handled || super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      int logicalX = (int)Math.round(this.logicalMouseX(mouseX));
      int logicalY = (int)Math.round(this.logicalMouseY(mouseY));
      int viewLeft = this.panelLeft() + 9;
      int viewTop = this.panelTop() + 31;
      if (inside(logicalX, logicalY, viewLeft, viewTop, 212, 228)) {
         this.targetScroll = clampScroll(this.targetScroll - (float)delta * 54.0F * 1.25F);
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, delta);
      }
   }

   @Override
   public boolean keyPressed(int key, int scanCode, int modifiers) {
      if (this.isOpen()) {
         if (key == 265 || key == 87) {
            this.selectAndFocus(this.selectedFloor + 1);
            return true;
         }

         if (key == 264 || key == 83) {
            this.selectAndFocus(this.selectedFloor - 1);
            return true;
         }

         if (key == 266) {
            this.selectAndFocus(this.selectedFloor + 5);
            return true;
         }

         if (key == 267) {
            this.selectAndFocus(this.selectedFloor - 5);
            return true;
         }

         if (key == 268) {
            this.selectAndFocus(20);
            return true;
         }

         if (key == 269) {
            this.selectAndFocus(1);
            return true;
         }

         if (key == 257 || key == 335) {
            this.enterSelectedFloor();
            return true;
         }
      }

      return super.keyPressed(key, scanCode, modifiers);
   }

   @Override
   protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
      if (this.hoveredFloor > 0) {
         List<Component> lines = new ArrayList<>();
         lines.add(Component.literal("FLOOR " + this.hoveredFloor + " - " + DkcFloorRegistry.name(this.hoveredFloor)));
         lines.add(Component.literal(this.longFloorState(this.hoveredFloor)));
         if (!DkcFloorRegistry.isBossFloor(this.hoveredFloor)) {
            lines.add(Component.literal(DkcFloorRegistry.requiredKills(this.hoveredFloor) + " defenders garrisoned"));
         }

         lines.add(Component.literal("Click to inspect"));
         this.drawDkcTooltip(graphics, lines, mouseX, mouseY);
      }
   }

   @Override
   protected void onBeforeCloseAnimationFinished() {
      if (this.pendingExit) {
         this.pendingExit = false;
         SololevelingMod.PACKET_HANDLER.sendToServer(PathButtonMessage.exitCastle(this.menu.x, this.menu.y, this.menu.z));
      } else if (this.pendingFloor > 0) {
         int floor = this.pendingFloor;
         this.pendingFloor = 0;
         SololevelingMod.PACKET_HANDLER.sendToServer(PathButtonMessage.enterFloor(floor, this.menu.x, this.menu.y, this.menu.z));
      }
   }

   @Override
   protected void onCloseAnimationFinished() {
      if (this.returningToQuests) {
         this.returningToQuests = false;
         if (this.minecraft != null && this.minecraft.player != null && this.minecraft.getConnection() != null) {
            this.minecraft.setScreen(new SystemQuestsScreen());
         }
      }
   }

   private void returnToQuestHub() {
      this.returningToQuests = true;
      this.beginClose();
   }

   private void focusProgressFloor() {
      int floor = this.menu.insideDkc() && this.menu.currentFloor() > 0 ? this.menu.currentFloor() : this.menu.highestUnlockedFloor();
      this.selectAndFocus(floor <= 0 ? 1 : floor);
   }

   private void select(int floor) {
      int clamped = clampFloor(floor);
      if (clamped != this.selectedFloor) {
         this.selectionChangedAt = Util.getMillis();
      }

      this.selectedFloor = clamped;
   }

   private void selectAndFocus(int floor) {
      this.select(floor);
      this.targetScroll = this.focusOffset(this.selectedFloor);
   }

   private void enterSelectedFloor() {
      if (this.canEnterSelected()) {
         this.travelRequested = true;
         this.pendingFloor = this.selectedFloor;
         this.beginClose();
      }
   }

   private void exitCastle() {
      if (this.menu.insideDkc() && !this.travelRequested) {
         this.travelRequested = true;
         this.pendingExit = true;
         this.beginClose();
      }
   }

   private boolean canEnterSelected() {
      if (this.travelRequested || this.menu.conquered() || !this.menu.isFloorUnlocked(this.selectedFloor)) {
         return false;
      } else {
         return this.menu.insideDkc()
            ? this.menu.currentFloor() != this.selectedFloor
            : this.menu.entity != null && this.menu.entity.level().dimension().equals(Level.OVERWORLD);
      }
   }

   private Component enterButtonText() {
      if (this.travelRequested) {
         return Component.literal("OPENING PATH...");
      } else if (this.menu.conquered()) {
         return Component.literal("CONQUERED");
      } else if (!this.menu.isFloorUnlocked(this.selectedFloor)) {
         return Component.literal("FLOOR SEALED");
      } else if (this.menu.insideDkc() && this.menu.currentFloor() == this.selectedFloor) {
         return Component.literal("YOU ARE HERE");
      } else {
         return this.menu.insideDkc() || this.menu.entity != null && this.menu.entity.level().dimension().equals(Level.OVERWORLD)
            ? Component.literal("ENTER FLOOR")
            : Component.literal("OVERWORLD ONLY");
      }
   }

   private String shortFloorState(int floor) {
      if (this.menu.insideDkc() && this.menu.currentFloor() == floor) {
         return "HERE";
      } else if (this.menu.isFloorCleared(floor)) {
         return "CLEARED";
      } else if (this.menu.isFloorUnlocked(floor)) {
         return floor == this.menu.highestUnlockedFloor() ? "CURRENT" : "OPEN";
      } else {
         return this.needsEntryPermit(floor) ? "PERMIT" : "SEALED";
      }
   }

   private String longFloorState(int floor) {
      if (this.menu.insideDkc() && this.menu.currentFloor() == floor) {
         return "YOU ARE HERE";
      } else if (this.menu.isFloorCleared(floor)) {
         return "CLEARED";
      } else if (this.menu.isFloorUnlocked(floor)) {
         return "AVAILABLE";
      } else if (this.needsEntryPermit(floor)) {
         return "ENTRY PERMIT REQUIRED";
      } else {
         return floor <= 1 ? "FIRST SEAL REQUIRED" : "CLEAR FLOOR " + (floor - 1);
      }
   }

   private int floorStateColor(int floor) {
      if (this.menu.insideDkc() && this.menu.currentFloor() == floor) {
         return -15261;
      } else if (this.menu.isFloorCleared(floor)) {
         return -3249032;
      } else if (this.menu.isFloorUnlocked(floor)) {
         return -34219;
      } else {
         return this.needsEntryPermit(floor) ? -15261 : -7441542;
      }
   }

   private boolean needsEntryPermit(int floor) {
      return floor > 1 && !this.menu.isFloorUnlocked(floor) && this.menu.clearedFloors() >= floor - 1 && !this.menu.isTransitionArmed(floor - 1);
   }

   private String floorObjective(int floor) {
      return switch (floor) {
         case 1 -> "Defeat Cerberus at the Ashen Threshold.";
         case 10 -> "Purge 32 defenders, then defeat Vulcan.";
         case 15 -> this.menu.radiruPact()
            ? "House Radiru stands as your sanctuary."
            : (this.menu.radiruSlaughtered() ? "House Radiru has fallen by your hand." : "Defeat 30 defenders and decide House Radiru's fate.");
         case 20 -> "Defeat Baran and Kaiselin at the Tempest Throne.";
         default -> "Defeat " + DkcFloorRegistry.requiredKills(floor) + " castle defenders.";
      };
   }

   private String floorReward(int floor) {
      return switch (floor) {
         case 1 -> "Entry Permit / World Tree Fragment / Full Recovery";
         case 10 -> "Entry Permit / Spring Water / Orb of Avarice";
         case 15 -> this.menu.radiruPact()
            ? "Radiru sanctuary / 1,500 XP"
            : (this.menu.radiruSlaughtered() ? "Cold Blood / 4,000 XP" : "Entry Permit / outcome reward");
         case 20 -> "Demon King weapons / Purified Blood / Kaisel";
         default -> "Entry Permit / " + floor * 100 + " XP";
      };
   }

   private String radiruBadge() {
      if (this.menu.radiruPact()) {
         return "PACT";
      } else {
         return this.menu.radiruSlaughtered() ? "FALLEN" : "RADIRU";
      }
   }

   private int floorAt(int mouseX, int mouseY, int viewLeft, int towerTop) {
      if (!inside(mouseX, mouseY, viewLeft, towerTop, 200, 211)) {
         return 0;
      }

      int roundedScroll = Math.round(this.scroll);

      for (int floor = 1; floor <= 20; floor++) {
         PathScreen.NodeBounds node = this.nodeBounds(floor, viewLeft, towerTop, roundedScroll);
         if (inside(mouseX, mouseY, node.x(), node.y(), node.width(), node.height())) {
            return floor;
         }
      }

      return 0;
   }

   private PathScreen.NodeBounds nodeBounds(int floor, int viewLeft, int towerTop, int roundedScroll) {
      boolean landmark = isLandmark(floor);
      int width = landmark ? 120 : 106;
      int height = landmark ? 40 : 34;
      int centerX = viewLeft + 100;
      int y = towerTop + floorContentY(floor) - roundedScroll - height / 2;
      return new PathScreen.NodeBounds(centerX - width / 2, y, width, height);
   }

   private static int shaftHalfWidth(int floor) {
      int descent = 20 - floor;
      return 64 + descent * 14 / 19;
   }

   private static boolean isLandmark(int floor) {
      return floor == 1 || floor == 10 || floor == 15 || floor == 20;
   }

   private static int floorContentY(int floor) {
      return 30 + (20 - floor) * 54;
   }

   private float focusOffset(int floor) {
      float nodeY = floorContentY(clampFloor(floor));
      return clampScroll(nodeY - 122.38F);
   }

   private void advanceAnimation() {
      this.targetScroll = clampScroll(this.targetScroll);
      this.scroll = this.scroll + (this.targetScroll - this.scroll) * 0.24F;
      if (Math.abs(this.targetScroll - this.scroll) < 0.08F) {
         this.scroll = this.targetScroll;
      }

      this.mouseVelocityX *= 0.94F;
      this.mouseVelocityY *= 0.94F;

      for (int floor = 1; floor <= 20; floor++) {
         float target = floor == this.hoveredFloor ? 1.0F : 0.0F;
         this.floorGlow[floor] = this.floorGlow[floor] + (target - this.floorGlow[floor]) * 0.22F;
      }
   }

   private void setScrollFromScrollbar(int mouseY, int towerTop) {
      int usable = 203;
      int thumbHeight = Math.max(24, Math.round(usable * 0.185413F));
      int travel = Math.max(1, usable - thumbHeight);
      float fraction = (mouseY - towerTop - 4 - thumbHeight / 2.0F) / travel;
      this.targetScroll = clampScroll(fraction * maxScroll());
      this.scroll = this.targetScroll;
   }

   private static float maxScroll() {
      return Math.max(0, 927);
   }

   private static float clampScroll(float value) {
      return Math.max(0.0F, Math.min(maxScroll(), value));
   }

   private static int clampFloor(int floor) {
      return Math.max(1, Math.min(20, floor));
   }

   private int panelLeft() {
      return this.leftPos + this.pRelX;
   }

   private int panelTop() {
      return this.topPos + this.pRelY;
   }

   private static float pulse01() {
      return 0.5F + 0.5F * (float)Math.sin(Util.getMillis() * 0.007);
   }

   private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
      return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }

   private static float hash01(int x, int y) {
      int hash = x * 374761393 + y * 668265263;
      hash = (hash ^ hash >>> 13) * 1274126177;
      return ((hash ^ hash >>> 16) & 65535) / 65535.0F;
   }

   private int drawTracked(GuiGraphics graphics, String text, int x, int y, int color, int tracking) {
      int cursor = x;

      for (int index = 0; index < text.length(); index++) {
         String glyph = String.valueOf(text.charAt(index));
         graphics.drawString(this.font, glyph, cursor, y, color, false);
         cursor += this.font.width(glyph) + tracking;
      }

      return cursor - tracking;
   }

   private int trackedWidth(String text, int tracking) {
      if (text.isEmpty()) {
         return 0;
      }

      int width = -tracking;

      for (int index = 0; index < text.length(); index++) {
         width += this.font.width(String.valueOf(text.charAt(index))) + tracking;
      }

      return width;
   }

   private void drawScaled(GuiGraphics graphics, String text, int x, int y, float scale, int color) {
      graphics.pose().pushPose();
      graphics.pose().translate(x, y, 0.0F);
      graphics.pose().scale(scale, scale, 1.0F);
      graphics.drawString(this.font, text, 0, 0, color, false);
      graphics.pose().popPose();
   }

   private static void fillChamfer(GuiGraphics graphics, int x, int y, int width, int height, int cut, int color) {
      graphics.fill(x, y + cut, x + width, y + height - cut, color);

      for (int step = 0; step < cut; step++) {
         graphics.fill(x + cut - step, y + step, x + width - cut + step, y + step + 1, color);
         graphics.fill(x + cut - step, y + height - step - 1, x + width - cut + step, y + height - step, color);
      }
   }

   private static void outlineChamfer(GuiGraphics graphics, int x, int y, int width, int height, int cut, int color) {
      graphics.fill(x + cut, y, x + width - cut, y + 1, color);
      graphics.fill(x + cut, y + height - 1, x + width - cut, y + height, color);
      graphics.fill(x, y + cut, x + 1, y + height - cut, color);
      graphics.fill(x + width - 1, y + cut, x + width, y + height - cut, color);

      for (int step = 0; step < cut; step++) {
         graphics.fill(x + cut - 1 - step, y + step, x + cut - step, y + step + 1, color);
         graphics.fill(x + width - cut + step, y + step, x + width - cut + step + 1, y + step + 1, color);
         graphics.fill(x + cut - 1 - step, y + height - step - 1, x + cut - step, y + height - step, color);
         graphics.fill(x + width - cut + step, y + height - step - 1, x + width - cut + step + 1, y + height - step, color);
      }
   }

   private static void drawRivet(GuiGraphics graphics, int x, int y, int tint) {
      graphics.fill(x, y, x + 2, y + 2, -871758584);
      graphics.fill(x, y, x + 1, y + 1, -2013265920 | tint & 16777215);
   }

   private static void drawOrnament(GuiGraphics graphics, int centerX, int centerY, int color) {
      graphics.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, color);
      graphics.fill(centerX - 2, centerY, centerX + 3, centerY + 1, color);
      graphics.fill(centerX, centerY - 2, centerX + 1, centerY + 3, color);
   }

   private static void drawRune(GuiGraphics graphics, int centerX, int centerY, int size, int seed, int color) {
      int[] pointX = new int[9];
      int[] pointY = new int[9];

      for (int index = 0; index < 9; index++) {
         pointX[index] = centerX + (index % 3 - 1) * size;
         pointY[index] = centerY + (index / 3 - 1) * size;
      }

      for (int chord = 0; chord < 4; chord++) {
         int from = (int)(hash01(seed, chord * 2) * 8.999F);
         int to = (int)(hash01(seed, chord * 2 + 1) * 8.999F);
         if (from == to) {
            to = (to + 3) % 9;
         }

         drawLine(graphics, pointX[from], pointY[from], pointX[to], pointY[to], color);
      }
   }

   private static void drawPanelFrame(GuiGraphics graphics, int x, int y, int width, int height) {
      graphics.fill(x - 2, y - 2, x + width + 2, y - 1, 872363341);
      graphics.fill(x - 2, y + height + 1, x + width + 2, y + height + 2, 872363341);
      graphics.fill(x - 2, y - 1, x - 1, y + height + 1, 872363341);
      graphics.fill(x + width + 1, y - 1, x + width + 2, y + height + 1, 872363341);
      graphics.fill(x - 1, y - 1, x + width + 1, y, 1728001357);
      graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, 1728001357);
      graphics.fill(x - 1, y, x, y + height, 1728001357);
      graphics.fill(x + width, y, x + width + 1, y + height, 1728001357);
      drawOutline(graphics, x, y, width, height, -7463121);
      drawCornerBrackets(graphics, x, y, width, height, -50608);
   }

   private static void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      graphics.fill(x, y, x + width, y + 1, color);
      graphics.fill(x, y + height - 1, x + width, y + height, color);
      graphics.fill(x, y, x + 1, y + height, color);
      graphics.fill(x + width - 1, y, x + width, y + height, color);
   }

   private static void drawCornerBrackets(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      int length = 12;
      int stub = 5;
      graphics.fill(x - 1, y - 1, x + length, y + 1, color);
      graphics.fill(x - 1, y - 1, x + 1, y + length, color);
      graphics.fill(x + width - length, y - 1, x + width + 1, y + 1, color);
      graphics.fill(x + width - 1, y - 1, x + width + 1, y + length, color);
      graphics.fill(x - 1, y + height - 1, x + length, y + height + 1, color);
      graphics.fill(x - 1, y + height - length, x + 1, y + height + 1, color);
      graphics.fill(x + width - length, y + height - 1, x + width + 1, y + height + 1, color);
      graphics.fill(x + width - 1, y + height - length, x + width + 1, y + height + 1, color);
      int inner = 1711276032 | color & 16777215;
      graphics.fill(x + 2, y + 2, x + 2 + stub, y + 3, inner);
      graphics.fill(x + 2, y + 2, x + 3, y + 2 + stub, inner);
      graphics.fill(x + width - 2 - stub, y + 2, x + width - 2, y + 3, inner);
      graphics.fill(x + width - 3, y + 2, x + width - 2, y + 2 + stub, inner);
      graphics.fill(x + 2, y + height - 3, x + 2 + stub, y + height - 2, inner);
      graphics.fill(x + 2, y + height - 2 - stub, x + 3, y + height - 2, inner);
      graphics.fill(x + width - 2 - stub, y + height - 3, x + width - 2, y + height - 2, inner);
      graphics.fill(x + width - 3, y + height - 2 - stub, x + width - 2, y + height - 2, inner);
   }

   private static void drawLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
      int dx = x1 - x0;
      int dy = y1 - y0;
      int steps = Math.max(Math.abs(dx), Math.abs(dy));
      if (steps <= 0) {
         graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
      } else {
         for (int step = 0; step <= steps; step++) {
            int x = x0 + Math.round(dx * ((float)step / steps));
            int y = y0 + Math.round(dy * ((float)step / steps));
            graphics.fill(x, y, x + 1, y + 1, color);
         }
      }
   }

   private static void drawDiamond(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
      drawLine(graphics, centerX, centerY - radius, centerX + radius, centerY, color);
      drawLine(graphics, centerX + radius, centerY, centerX, centerY + radius, color);
      drawLine(graphics, centerX, centerY + radius, centerX - radius, centerY, color);
      drawLine(graphics, centerX - radius, centerY, centerX, centerY - radius, color);
   }

   private void drawDkcTooltip(GuiGraphics graphics, List<Component> lines, int mouseX, int mouseY) {
      int padding = 6;
      int textWidth = 0;

      for (Component line : lines) {
         textWidth = Math.max(textWidth, this.font.width(line));
      }

      int width = textWidth + padding * 2 + 3;
      int height = lines.size() * 10 + padding * 2 - 1;
      int x = mouseX + 12;
      int y = mouseY - 12;
      if (x + width > this.width - 2) {
         x = mouseX - width - 12;
      }

      if (y + height > this.height - 2) {
         y = this.height - height - 2;
      }

      x = Math.max(2, x);
      y = Math.max(2, y);
      graphics.fill(x + 2, y + 3, x + width + 2, y + height + 3, 1996488704);
      graphics.fillGradient(x, y, x + width, y + height, -231078122, -16383482);
      drawOutline(graphics, x, y, width, height, -7463121);
      drawCornerBrackets(graphics, x, y, width, height, -50608);
      graphics.fill(x + 1, y + 1, x + 4, y + height - 1, -7463121);
      int textY = y + padding;

      for (int index = 0; index < lines.size(); index++) {
         graphics.drawString(this.font, lines.get(index), x + padding + 3, textY, index == 0 ? -34219 : (index == lines.size() - 1 ? -7441542 : -4624), false);
         if (index == 0) {
            graphics.fill(x + padding + 3, textY + 10, x + width - padding, textY + 11, 1428949524);
            textY += 2;
         }

         textY += 10;
      }
   }

   private static final class DkcButton extends Button {
      private final Supplier<Component> label;
      private final BooleanSupplier enabled;

      private DkcButton(int x, int y, int width, int height, Supplier<Component> label, BooleanSupplier enabled, OnPress onPress) {
         super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
         this.label = label;
         this.enabled = enabled;
      }

      @Override
      public void onPress() {
         if (this.enabled.getAsBoolean()) {
            super.onPress();
         }
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            graphics.drawManaged(() -> this.renderButton(graphics));
         }
      }

      private void renderButton(GuiGraphics graphics) {
         Component currentLabel = this.label.get();
         this.setMessage(currentLabel);
         boolean usable = this.enabled.getAsBoolean();
         boolean hovered = usable && this.isHoveredOrFocused();
         int x = this.getX();
         int y = this.getY();
         int cut = 3;
         int top = !usable ? -870903526 : (hovered ? -428730330 : -868610792);
         int bottom = !usable ? -871692275 : (hovered ? -433715440 : -870972407);
         int border = !usable ? -10860720 : (hovered ? -34219 : -7463121);
         int text = !usable ? -7441542 : (hovered ? -1 : -4624);
         PathScreen.fillChamfer(graphics, x + 1, y + 2, this.width, this.height, cut, 1711276032);
         PathScreen.fillChamfer(graphics, x, y, this.width, this.height, cut, top);
         graphics.fillGradient(x + 1, y + cut, x + this.width - 1, y + this.height - cut, 0, bottom);
         PathScreen.outlineChamfer(graphics, x, y, this.width, this.height, cut, border);
         graphics.fill(x + cut, y + 1, x + this.width - cut, y + 2, usable ? 1157627903 : 587202559);
         if (usable && hovered) {
            graphics.fill(x + 2, y + this.height / 2 - 2, x + 4, y + this.height / 2 + 2, -34219);
            graphics.fill(x + this.width - 4, y + this.height / 2 - 2, x + this.width - 2, y + this.height / 2 + 2, -34219);
         } else if (!usable) {
            for (int fleck = 0; fleck < 10; fleck++) {
               int fx = x + 4 + (int)(PathScreen.hash01(fleck, y) * (this.width - 8));
               int fy = y + 3 + (int)(PathScreen.hash01(x, fleck) * (this.height - 6));
               graphics.fill(fx, fy, fx + 1, fy + 1, 855638016);
            }
         }

         Font font = Minecraft.getInstance().font;
         graphics.drawCenteredString(font, currentLabel, x + this.width / 2, y + (this.height - 8) / 2, text);
      }
   }

   private record NodeBounds(int x, int y, int width, int height) {
   }
}
