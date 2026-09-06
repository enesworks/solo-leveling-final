package dev.eness.sololevelingfinal.core.client.gui.system;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.renderer.shader.VesselSelectionBackgroundRenderTypes;
import dev.eness.sololevelingfinal.core.network.VesselSelectionMessage;
import dev.eness.sololevelingfinal.core.util.VesselManager;
import org.joml.Matrix4f;

public final class VesselSelectionScreen extends SystemScreen {
   private static final int ROW_Y = 70;
   private static final int ROW_STEP = 34;
   private static final int ROW_W = 141;
   private static final int ROW_H = 29;
   private static final int THEME_SYSTEM = 0;
   private static final int THEME_RULER = 1;
   private static final int THEME_SHADOW = 2;
   private static final int THEME_FROST = 3;
   private static final int THEME_WHITE_FLAME = 4;
   private static final int THEME_BEAST = 5;
   private static final int THEME_MONARCH = 6;
   private static final int THEME_DESTRUCTION = 7;
   private static final int THEME_COUNT = 8;
   private static final float THEME_RESPONSE_SECONDS = 0.32F;
   private static final int[] THEME_ACCENTS = new int[]{-12597505, -14262, -4823809, -2689025, -4592897, -46792, -3767041, -55745};
   private static final int[] THEME_TEXT = new int[]{-1509633, -2865, -858881, -655873, -984065, -5658, -858369, -6179};
   private static final int[] THEME_SUBTEXT = new int[]{-7358248, -1983126, -3627289, -5319713, -6437410, -2647406, -4284968, -1269611};
   private static final int[] FALLBACK_TOP = new int[]{-16313557, -13821436, -14613201, -16106917, -16243636, -13367803, -14285262, -13631480};
   private static final int[] FALLBACK_BOTTOM = new int[]{-16710902, -16252160, -16580602, -16708840, -16710127, -16252671, -16515065, -16449534};
   private final List<VesselSelectionScreen.VesselButton> vesselButtons = new ArrayList<>();
   private final float[] themeWeights = new float[8];
   private long lastThemeUpdate;
   private int advancementPoints;
   private int requiredPoints;
   private int vesselLimit;
   private int[] claimCounts;
   private boolean developerMode;
   private int selectingIndex = -1;

   public VesselSelectionScreen(int advancementPoints, int requiredPoints, int vesselLimit, int[] claimCounts, boolean developerMode) {
      super(Component.literal("SELECT YOUR VESSEL"));
      this.panelW = 312;
      this.panelH = 292;
      this.themeWeights[0] = 1.0F;
      this.updateState(advancementPoints, requiredPoints, vesselLimit, claimCounts, developerMode);
   }

   public static void handleServerState(boolean open, int advancementPoints, int requiredPoints, int vesselLimit, int[] claimCounts, boolean developerMode) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!open) {
         if (minecraft.screen instanceof VesselSelectionScreen) {
            SystemGuiSounds.exit();
            minecraft.setScreen(null);
         }
      } else {
         if (minecraft.screen instanceof VesselSelectionScreen screen) {
            screen.updateState(advancementPoints, requiredPoints, vesselLimit, claimCounts, developerMode);
         } else {
            minecraft.setScreen(new VesselSelectionScreen(advancementPoints, requiredPoints, vesselLimit, claimCounts, developerMode));
         }
      }
   }

   @Override
   protected void init() {
      super.init();
      this.vesselButtons.clear();
      int rulerRow = 0;
      int monarchRow = 0;
      List<VesselManager.VesselDefinition> definitions = VesselManager.definitions();

      for (int i = 0; i < definitions.size(); i++) {
         VesselManager.VesselDefinition definition = definitions.get(i);
         boolean rulerColumn = isRulerColumn(definition);
         int row = rulerColumn ? rulerRow++ : monarchRow++;
         int x = this.panelX + (rulerColumn ? 10 : 161);
         int y = this.panelY + 70 + row * 34;
         VesselSelectionScreen.VesselButton button = new VesselSelectionScreen.VesselButton(this, i, definition, x, y, 141, 29);
         this.vesselButtons.add(button);
         this.addRenderableWidget(button);
      }

      this.lastThemeUpdate = Util.getMillis();
   }

   @Override
   public boolean keyPressed(int key, int scanCode, int modifiers) {
      return key == 256 ? true : super.keyPressed(key, scanCode, modifiers);
   }

   @Override
   protected void beginClose() {
   }

   @Override
   protected boolean allowsNonSystemAccess() {
      return true;
   }

   @Override
   public void onClose() {
   }

   @Override
   protected void renderAnimatedBackground(GuiGraphics graphics, int mouseX, int mouseY) {
      this.updateTheme(this.findHoveredTheme(mouseX, mouseY));
      float localX = clamp01((float)(mouseX - this.panelX) / this.panelW);
      float localY = clamp01((float)(mouseY - this.panelY) / this.panelH);
      ShaderInstance shader = VesselSelectionBackgroundRenderTypes.get();
      if (shader == null) {
         this.renderFallbackBackground(graphics);
      } else {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShader(VesselSelectionBackgroundRenderTypes::get);
         AbstractUniform mouse = shader.safeGetUniform("MousePos");
         mouse.set(localX, localY);
         shader.safeGetUniform("ThemeWeights0").set(this.themeWeights[0], this.themeWeights[1], this.themeWeights[2], this.themeWeights[3]);
         shader.safeGetUniform("ThemeWeights1").set(this.themeWeights[4], this.themeWeights[5], this.themeWeights[6], this.themeWeights[7]);
         int x0 = this.panelX;
         int y0 = this.panelY;
         int x1 = this.panelX + this.panelW;
         int y1 = this.panelY + this.panelH;
         Matrix4f matrix = graphics.pose().last().pose();
         BufferBuilder buffer = Tesselator.getInstance().getBuilder();
         buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         buffer.vertex(matrix, x0, y1, 0.0F).uv(0.0F, 1.0F).endVertex();
         buffer.vertex(matrix, x1, y1, 0.0F).uv(1.0F, 1.0F).endVertex();
         buffer.vertex(matrix, x1, y0, 0.0F).uv(1.0F, 0.0F).endVertex();
         buffer.vertex(matrix, x0, y0, 0.0F).uv(0.0F, 0.0F).endVertex();
         Tesselator.getInstance().end();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   @Override
   protected void renderFrame(GuiGraphics graphics) {
      int accent = this.weightedColor(THEME_ACCENTS);
      int x = this.panelX;
      int y = this.panelY;
      int w = this.panelW;
      int h = this.panelH;
      int soft = withAlpha(accent, 86);
      int dim = mixRgb(-16314342, accent, 0.62F);
      graphics.fill(x - 1, y - 1, x + w + 1, y, soft);
      graphics.fill(x - 1, y + h, x + w + 1, y + h + 1, soft);
      graphics.fill(x - 1, y, x, y + h, soft);
      graphics.fill(x + w, y, x + w + 1, y + h, soft);
      drawOutline(graphics, x, y, w, h, dim);
      graphics.fill(x, y, x + w, y + 18, withAlpha(mixRgb(-16645368, accent, 0.13F), 221));
      graphics.fill(x, y + 18, x + w, y + 19, accent);
      drawCorners(graphics, x, y, w, h, accent);
      Font font = Minecraft.getInstance().font;
      String titleText = "[ " + this.title.getString() + " ]";
      graphics.drawString(font, titleText, x + (w - font.width(titleText)) / 2, y + 5, this.weightedColor(THEME_TEXT), false);
   }

   @Override
   protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      int accent = this.weightedColor(THEME_ACCENTS);
      int mainText = this.weightedColor(THEME_TEXT);
      int subText = this.weightedColor(THEME_SUBTEXT);
      graphics.fill(this.panelX + 5, this.panelY + 22, this.panelX + this.panelW - 5, this.panelY + 68, 1912603144);
      String instruction = "The choice is permanent. Select one vessel.";
      graphics.drawCenteredString(this.font, instruction, this.panelX + this.panelW / 2, this.panelY + 27, mainText);
      String progress = "ADVANCEMENT POINTS  " + this.advancementPoints + "/" + this.requiredPoints;
      graphics.drawCenteredString(
         this.font, Component.literal(progress).withStyle(ChatFormatting.BOLD), this.panelX + this.panelW / 2, this.panelY + 40, mixRgb(accent, -1, 0.32F)
      );
      graphics.fill(this.panelX + 10, this.panelY + 53, this.panelX + this.panelW - 10, this.panelY + 54, withAlpha(accent, 138));
      graphics.fill(this.panelX + 155, this.panelY + 55, this.panelX + 156, this.panelY + 241, withAlpha(accent, 54));
      graphics.drawString(
         this.font,
         Component.literal("RULER VESSELS").withStyle(ChatFormatting.BOLD),
         this.panelX + 10,
         this.panelY + 58,
         mixRgb(-14262, mainText, this.themeWeights[0] * 0.34F),
         false
      );
      graphics.drawString(
         this.font,
         Component.literal("MONARCH VESSELS").withStyle(ChatFormatting.BOLD),
         this.panelX + 161,
         this.panelY + 58,
         mixRgb(-3767041, mainText, this.themeWeights[0] * 0.25F),
         false
      );
      boolean anyAvailable = false;

      for (int i = 0; i < VesselManager.definitions().size(); i++) {
         anyAvailable |= this.isAvailable(i);
      }

      graphics.fill(this.panelX + 5, this.panelY + this.panelH - 28, this.panelX + this.panelW - 5, this.panelY + this.panelH - 4, -2113928955);
      if (anyAvailable) {
         graphics.drawCenteredString(
            this.font, "Locked or claimed vessels cannot be selected.", this.panelX + this.panelW / 2, this.panelY + this.panelH - 18, subText
         );
      } else {
         graphics.drawCenteredString(this.font, "All vessels are claimed.", this.panelX + this.panelW / 2, this.panelY + this.panelH - 24, -38808);
         graphics.drawCenteredString(
            this.font, "Increase /gamerule soloLevelingMonarchLimit.", this.panelX + this.panelW / 2, this.panelY + this.panelH - 13, -24416
         );
      }
   }

   @Override
   protected List<Component> getHoverTooltip(int mouseX, int mouseY) {
      for (VesselSelectionScreen.VesselButton button : this.vesselButtons) {
         if (button.contains(mouseX, mouseY)) {
            VesselManager.VesselDefinition definition = button.definition;
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(displayName(definition)).withStyle(themeFormatting(definition), ChatFormatting.BOLD));
            if (this.isWorkInProgress(definition)) {
               tooltip.add(Component.literal("WIP (Work in progress)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            } else {
               tooltip.add(Component.literal(displayPower(definition)).withStyle(ChatFormatting.WHITE));
               tooltip.add(Component.literal(definition.description()).withStyle(ChatFormatting.GRAY));
            }

            int count = this.claimCount(button.index);
            String capacity = this.vesselLimit <= 0 ? "Unlimited vessels" : count + "/" + this.vesselLimit + " claimed";
            if (!this.isWorkInProgress(definition)) {
               tooltip.add(Component.literal(capacity).withStyle(this.isAvailable(button.index) ? ChatFormatting.GREEN : ChatFormatting.RED));
            }

            return tooltip;
         }
      }

      return null;
   }

   private void choose(int index, VesselManager.VesselDefinition definition) {
      if (VesselManager.isAntares(definition)) {
         this.chooseAntares(index);
      } else {
         this.submitChoice(index, definition);
      }
   }

   private void chooseAntares(int index) {
      VesselManager.VesselDefinition antares = VesselManager.antaresDefinition();
      if (index == VesselManager.definitions().indexOf(antares)) {
         this.submitChoice(index, antares);
      }
   }

   private void submitChoice(int index, VesselManager.VesselDefinition definition) {
      if (this.isAvailable(index) && this.selectingIndex < 0) {
         this.selectingIndex = index;
         SololevelingMod.PACKET_HANDLER.sendToServer(new VesselSelectionMessage(definition.type(), definition.identity()));
      }
   }

   private void updateState(int advancementPoints, int requiredPoints, int vesselLimit, int[] claimCounts, boolean developerMode) {
      this.advancementPoints = Math.max(0, advancementPoints);
      this.requiredPoints = Math.max(1, requiredPoints);
      this.vesselLimit = vesselLimit;
      this.claimCounts = claimCounts == null ? new int[0] : (int[])claimCounts.clone();
      this.developerMode = developerMode;
      this.selectingIndex = -1;
   }

   private int claimCount(int index) {
      return index >= 0 && index < this.claimCounts.length ? this.claimCounts[index] : 0;
   }

   private boolean isAvailable(int index) {
      return index >= 0 && index < VesselManager.definitions().size() && !this.isWorkInProgress(VesselManager.definitions().get(index))
         ? this.vesselLimit <= 0 || this.claimCount(index) < this.vesselLimit
         : false;
   }

   private boolean isWorkInProgress(VesselManager.VesselDefinition definition) {
      return VesselManager.isWorkInProgress(definition) && (!this.developerMode || !VesselManager.isDeveloperPreview(definition));
   }

   private int findHoveredTheme(int mouseX, int mouseY) {
      for (VesselSelectionScreen.VesselButton button : this.vesselButtons) {
         if (button.visible && button.contains(mouseX, mouseY)) {
            return themeFor(button.definition);
         }
      }

      return 0;
   }

   private void updateTheme(int targetTheme) {
      long now = Util.getMillis();
      float elapsed = Math.min(0.1F, Math.max(0.0F, (float)(now - this.lastThemeUpdate) / 1000.0F));
      this.lastThemeUpdate = now;
      float follow = 1.0F - (float)Math.exp(-elapsed / 0.32F);

      for (int i = 0; i < 8; i++) {
         float target = i == targetTheme ? 1.0F : 0.0F;
         this.themeWeights[i] = this.themeWeights[i] + (target - this.themeWeights[i]) * follow;
      }

      this.normalizeThemeWeights();
   }

   private void normalizeThemeWeights() {
      float total = 0.0F;

      for (float weight : this.themeWeights) {
         total += weight;
      }

      if (total <= 1.0E-4F) {
         this.themeWeights[0] = 1.0F;
      } else {
         for (int i = 0; i < this.themeWeights.length; i++) {
            this.themeWeights[i] = this.themeWeights[i] / total;
         }
      }
   }

   private void renderFallbackBackground(GuiGraphics graphics) {
      int top = this.weightedColor(FALLBACK_TOP);
      int bottom = this.weightedColor(FALLBACK_BOTTOM);
      int accent = this.weightedColor(THEME_ACCENTS);
      graphics.fillGradient(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + this.panelH, top, bottom);
      float time = (float)(Util.getMillis() % 100000L) / 1000.0F;

      for (int i = 0; i < 36; i++) {
         float seed = i * 19.731F;
         float fx = frac((float)Math.sin(seed) * 43758.547F);
         float fy = frac((float)Math.sin(seed * 1.79F) * 24634.635F);
         int x = this.panelX + (int)(fx * this.panelW);
         int y = this.panelY + (int)((fy * this.panelH + time * (5.0F + fx * 13.0F)) % this.panelH);
         graphics.fill(x, y, x + 1, y + 1, withAlpha(accent, 116));
      }

      this.renderDestructionFallback(graphics, time, this.themeWeights[7]);
      graphics.fillGradient(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + this.panelH, 134217728, 1979711488);
   }

   private void renderDestructionFallback(GuiGraphics graphics, float time, float rawWeight) {
      float weight = clamp01(rawWeight);
      if (!(weight <= 0.015F)) {
         int centerX = this.panelX + this.panelW / 2;
         int centerY = this.panelY + (int)(this.panelH * 0.43F);
         float pulse = 0.78F + 0.22F * (float)Math.sin(time * 1.7F);

         for (int ring = 0; ring < 3; ring++) {
            int radius = 28 + ring * 14 + (int)(4.0 * Math.sin(time * 0.8F + ring));
            int alpha = Math.round(weight * pulse * (72 - ring * 15));
            drawOutline(graphics, centerX - radius, centerY - radius, radius * 2, radius * 2, withAlpha(-55745, alpha));
         }

         for (int row = -7; row <= 7; row++) {
            float normalized = row / 7.0F;
            int halfWidth = Math.round(50.0F * (float)Math.sqrt(Math.max(0.0F, 1.0F - normalized * normalized)));
            int alpha = Math.round(weight * (1.0F - Math.abs(normalized)) * 90.0F);
            graphics.fill(centerX - halfWidth, centerY + row, centerX + halfWidth + 1, centerY + row + 1, withAlpha(-50396, alpha));
         }

         graphics.fill(centerX - 2, centerY - 12, centerX + 3, centerY + 13, withAlpha(-16187391, Math.round(weight * 220.0F)));

         for (int ray = 0; ray < 11; ray++) {
            float angle = (float)(ray * Math.PI * 2.0 / 11.0 + Math.sin(time * 0.28F) * 0.08F);
            int length = 58 + ray % 3 * 13;
            int endX = centerX + Math.round((float)Math.cos(angle) * length);
            int endY = centerY + Math.round((float)Math.sin(angle) * length);
            drawPixelLine(graphics, centerX, centerY, endX, endY, withAlpha(-58088, Math.round(weight * pulse * 76.0F)));
         }
      }
   }

   private static void drawPixelLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
      int dx = Math.abs(x1 - x0);
      int stepX = x0 < x1 ? 1 : -1;
      int dy = -Math.abs(y1 - y0);
      int stepY = y0 < y1 ? 1 : -1;
      int error = dx + dy;

      while (true) {
         graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
         if (x0 == x1 && y0 == y1) {
            return;
         }

         int doubled = error * 2;
         if (doubled >= dy) {
            error += dy;
            x0 += stepX;
         }

         if (doubled <= dx) {
            error += dx;
            y0 += stepY;
         }
      }
   }

   private int weightedColor(int[] palette) {
      float red = 0.0F;
      float green = 0.0F;
      float blue = 0.0F;

      for (int i = 0; i < 8; i++) {
         red += (palette[i] >> 16 & 0xFF) * this.themeWeights[i];
         green += (palette[i] >> 8 & 0xFF) * this.themeWeights[i];
         blue += (palette[i] & 0xFF) * this.themeWeights[i];
      }

      return 0xFF000000 | Math.round(red) << 16 | Math.round(green) << 8 | Math.round(blue);
   }

   private static int themeFor(VesselManager.VesselDefinition definition) {
      if (isRulerColumn(definition)) {
         return 1;
      }

      return switch (definition.identity()) {
         case "ashborn" -> 2;
         case "sillad" -> 3;
         case "baran" -> 4;
         case "rakan" -> 5;
         case "antares" -> 7;
         default -> 6;
      };
   }

   private static boolean isRulerColumn(VesselManager.VesselDefinition definition) {
      return "ruler".equals(definition.type()) && !"ashborn".equals(definition.identity());
   }

   private static String displayName(VesselManager.VesselDefinition definition) {
      return switch (definition.identity()) {
         case "ashborn" -> "Ashborn";
         case "go_gunhee" -> "Brightest Fragment";
         case "liu_zhigang" -> "Sharpest Fragment";
         case "thomas_andre" -> "Adamant Fragment";
         case "christopher_reed" -> "Blazing Fragment";
         case "sung_il_hwan" -> "Silent Fragment";
         default -> definition.name();
      };
   }

   private static String displayPower(VesselManager.VesselDefinition definition) {
      if ("ashborn".equals(definition.identity())) {
         return "Monarch of Shadows";
      } else {
         return isRulerColumn(definition) ? definition.name() : definition.powerName();
      }
   }

   private static ChatFormatting themeFormatting(VesselManager.VesselDefinition definition) {
      return switch (themeFor(definition)) {
         case 1 -> ChatFormatting.GOLD;
         default -> ChatFormatting.LIGHT_PURPLE;
         case 3 -> ChatFormatting.AQUA;
         case 4 -> ChatFormatting.BLUE;
         case 5 -> ChatFormatting.RED;
         case 7 -> ChatFormatting.DARK_RED;
      };
   }

   private static int withAlpha(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   private static int mixRgb(int from, int to, float amount) {
      float value = clamp01(amount);
      int red = Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * value);
      int green = Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * value);
      int blue = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * value);
      return 0xFF000000 | red << 16 | green << 8 | blue;
   }

   private static float frac(float value) {
      return value - (float)Math.floor(value);
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      graphics.fill(x, y, x + width, y + 1, color);
      graphics.fill(x, y + height - 1, x + width, y + height, color);
      graphics.fill(x, y, x + 1, y + height, color);
      graphics.fill(x + width - 1, y, x + width, y + height, color);
   }

   private static void drawCorners(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      int length = 12;
      graphics.fill(x - 1, y - 1, x + length, y + 1, color);
      graphics.fill(x - 1, y - 1, x + 1, y + length, color);
      graphics.fill(x + width - length, y - 1, x + width + 1, y + 1, color);
      graphics.fill(x + width - 1, y - 1, x + width + 1, y + length, color);
      graphics.fill(x - 1, y + height - 1, x + length, y + height + 1, color);
      graphics.fill(x - 1, y + height - length, x + 1, y + height + 1, color);
      graphics.fill(x + width - length, y + height - 1, x + width + 1, y + height + 1, color);
      graphics.fill(x + width - 1, y + height - length, x + width + 1, y + height + 1, color);
   }

   private static final class VesselButton extends Button {
      private final VesselSelectionScreen screen;
      private final int index;
      private final VesselManager.VesselDefinition definition;

      private VesselButton(VesselSelectionScreen screen, int index, VesselManager.VesselDefinition definition, int x, int y, int width, int height) {
         super(x, y, width, height, Component.empty(), button -> screen.choose(index, definition), DEFAULT_NARRATION);
         this.screen = screen;
         this.index = index;
         this.definition = definition;
      }

      private boolean contains(int mouseX, int mouseY) {
         return mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= this.getY() && mouseY < this.getY() + this.height;
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            boolean available = this.screen.isAvailable(this.index);
            boolean waiting = this.screen.selectingIndex == this.index;
            boolean hovered = this.contains(mouseX, mouseY) && available;
            int identityAccent = VesselSelectionScreen.THEME_ACCENTS[VesselSelectionScreen.themeFor(this.definition)];
            int activeAccent = hovered ? this.screen.weightedColor(VesselSelectionScreen.THEME_ACCENTS) : identityAccent;
            int border = available ? (hovered ? activeAccent : VesselSelectionScreen.mixRgb(-16314342, identityAccent, 0.58F)) : -10274224;
            int fill = available
               ? VesselSelectionScreen.withAlpha(VesselSelectionScreen.mixRgb(-16644854, activeAccent, hovered ? 0.24F : 0.1F), hovered ? 206 : 168)
               : -1609234152;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, fill);
            VesselSelectionScreen.drawOutline(graphics, this.getX(), this.getY(), this.width, this.height, border);
            if (hovered) {
               graphics.fill(
                  this.getX() + 1,
                  this.getY() + this.height - 3,
                  this.getX() + this.width - 1,
                  this.getY() + this.height - 1,
                  VesselSelectionScreen.withAlpha(activeAccent, 114)
               );
            }

            Font font = Minecraft.getInstance().font;
            boolean workInProgress = this.screen.isWorkInProgress(this.definition);
            String state = waiting ? "WAIT" : (workInProgress ? "WIP" : (available ? "OPEN" : "LOCKED"));
            int stateColor = waiting ? -10645 : (available ? activeAccent : -38808);
            int nameColor = available
               ? (
                  hovered
                     ? this.screen.weightedColor(VesselSelectionScreen.THEME_TEXT)
                     : VesselSelectionScreen.THEME_TEXT[VesselSelectionScreen.themeFor(this.definition)]
               )
               : -6914163;
            int subColor = available
               ? (
                  hovered
                     ? this.screen.weightedColor(VesselSelectionScreen.THEME_SUBTEXT)
                     : VesselSelectionScreen.THEME_SUBTEXT[VesselSelectionScreen.themeFor(this.definition)]
               )
               : -9085333;
            graphics.drawString(
               font, fit(font, VesselSelectionScreen.displayName(this.definition), this.width - 10), this.getX() + 5, this.getY() + 5, nameColor, false
            );
            int powerWidth = this.width - font.width(state) - 15;
            String power = workInProgress ? "Work in progress" : VesselSelectionScreen.displayPower(this.definition);
            graphics.drawString(font, fit(font, power, powerWidth), this.getX() + 5, this.getY() + 17, subColor, false);
            graphics.drawString(font, state, this.getX() + this.width - font.width(state) - 5, this.getY() + 17, stateColor, false);
         }
      }

      private static String fit(Font font, String text, int maxWidth) {
         if (font.width(text) <= maxWidth) {
            return text;
         }

         String value = text;

         while (!value.isEmpty() && font.width(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
         }

         return value + "...";
      }
   }
}
