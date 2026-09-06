package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.renderer.shader.ShadowSummonBackgroundRenderTypes;
import dev.eness.sololevelingfinal.core.init.SololevelingModSounds;
import dev.eness.sololevelingfinal.core.network.ShadowGlowColorMessage;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowCustomizationMenu;
import org.joml.Matrix4f;

public class ShadowCustomizationScreen extends AbstractContainerScreen<ShadowCustomizationMenu> {
   private static final int PANEL_W = 336;
   private static final int PANEL_H = 300;
   private static final int HEADER_H = 20;
   private static final int LIST_X = 10;
   private static final int LIST_Y = 28;
   private static final int LIST_W = 116;
   private static final int ROW_H = 12;
   private static final int EDITOR_X = 134;
   private static final int EDITOR_Y = 28;
   private static final int EDITOR_W = 192;
   private static final int ACCENT = -4760321;
   private static final int ACCENT_BLUE = -12334849;
   private static final int ACCENT_DIM = -9818728;
   private static final int TEXT_MAIN = -1250305;
   private static final int TEXT_SUB = -6510635;
   private static final int INK = -871889648;
   private static final int GOLD = -14262;
   private static final long ANIM_MS = 190L;
   private static final int[] PRESETS = new int[]{4179711, 12016895, 16767334, 16735118, 6029214, 16777215};
   private final List<String> types = ShadowMonarchManager.customizableTypes();
   private ShadowCustomizationScreen.State state = ShadowCustomizationScreen.State.OPENING;
   private long animStart;
   private boolean closed;
   private float reveal;
   private int selected;
   private int draftColor = -1;
   private boolean draftDirty;
   private int draftIdleTicks;
   private ShadowCustomizationScreen.ColorSlider redSlider;
   private ShadowCustomizationScreen.ColorSlider greenSlider;
   private ShadowCustomizationScreen.ColorSlider blueSlider;
   private Button toggleButton;

   public ShadowCustomizationScreen(ShadowCustomizationMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 336;
      this.imageHeight = 300;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public void init() {
      super.init();
      this.state = ShadowCustomizationScreen.State.OPENING;
      this.animStart = Util.getMillis();
      this.closed = false;
      this.reveal = 0.0F;
      this.selected = Math.max(0, this.types.indexOf(this.menu.shadowType()));
      this.draftColor = this.menu.glowColor(this.selected);
      this.playPanelSound(SololevelingModSounds.PANELOPEN.get(), 0.78F);
      this.buildControls();
   }

   private void buildControls() {
      this.clearWidgets();
      int x = this.leftPos + 134 + 8;
      int y = this.topPos + 28 + 74;
      this.redSlider = this.addRenderableWidget(new ShadowCustomizationScreen.ColorSlider(x, y, "R", 16));
      this.greenSlider = this.addRenderableWidget(new ShadowCustomizationScreen.ColorSlider(x, y + 18, "G", 8));
      this.blueSlider = this.addRenderableWidget(new ShadowCustomizationScreen.ColorSlider(x, y + 36, "B", 0));
      this.toggleButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.toggleOutline()).bounds(x, y + 58, 176, 18).build());
      this.refreshToggleLabel();
   }

   private void refreshToggleLabel() {
      if (this.toggleButton != null) {
         this.toggleButton.setMessage(Component.literal(this.hasOutline() ? "Remove Outline" : "Apply Outline"));
      }
   }

   private boolean hasOutline() {
      return this.draftColor != -1;
   }

   private int editableColor() {
      return this.hasOutline() ? this.draftColor : PRESETS[0];
   }

   private void select(int index) {
      if (index != this.selected && index >= 0 && index < this.types.size()) {
         this.commitDraft();
         this.selected = index;
         this.draftColor = this.menu.glowColor(index);
         this.syncSliders();
         this.refreshToggleLabel();
      }
   }

   private void syncSliders() {
      if (this.redSlider != null) {
         this.redSlider.pull();
      }

      if (this.greenSlider != null) {
         this.greenSlider.pull();
      }

      if (this.blueSlider != null) {
         this.blueSlider.pull();
      }
   }

   private void toggleOutline() {
      this.draftColor = this.hasOutline() ? -1 : PRESETS[0];
      this.syncSliders();
      this.refreshToggleLabel();
      this.sendColor();
   }

   private void applyPreset(int rgb) {
      this.draftColor = rgb;
      this.syncSliders();
      this.refreshToggleLabel();
      this.sendColor();
   }

   private void commitDraft() {
      if (this.draftDirty) {
         this.sendColor();
      }
   }

   private void sendColor() {
      this.draftDirty = false;
      this.draftIdleTicks = 0;
      this.menu.setGlowColorLocal(this.selected, this.draftColor);
      SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowGlowColorMessage(this.types.get(this.selected), this.draftColor));
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      if (this.draftDirty && ++this.draftIdleTicks >= 5) {
         this.commitDraft();
      }
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.updateAnimation();
      if (!this.closed) {
         ResponsiveGuiScale.Transform transform = this.responsiveTransform();
         int logicalMouseX = transform.logicalMouseX(mouseX);
         int logicalMouseY = transform.logicalMouseY(mouseY);
         this.renderBackground(graphics);
         graphics.flush();
         ResponsiveGuiScale.push(graphics, transform);
         int centerY = this.topPos + this.imageHeight / 2;
         int halfH = Math.round((this.imageHeight / 2.0F + 4.0F) * this.reveal);
         int top = centerY - halfH;
         int bottom = centerY + halfH;
         int sx0 = this.leftPos - 3;
         int sx1 = this.leftPos + this.imageWidth + 3;
         ResponsiveGuiScale.enableScissor(graphics, transform, sx0, top, sx1, bottom);
         super.render(graphics, logicalMouseX, logicalMouseY, partialTicks);
         graphics.disableScissor();
         if (this.reveal < 1.0F) {
            graphics.fill(sx0, top, sx1, top + 1, -4760321);
            graphics.fill(sx0, bottom - 1, sx1, bottom, -12334849);
         }

         ResponsiveGuiScale.pop(graphics);
         if (this.state == ShadowCustomizationScreen.State.OPEN) {
            this.renderTooltip(graphics, mouseX, mouseY);
         }
      }
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      this.renderAnimatedBackground(graphics, mouseX, mouseY);
      graphics.drawManaged(() -> {
         this.renderFrame(graphics);
         this.renderRoster(graphics, mouseX, mouseY);
         this.renderEditor(graphics);
      });
      RenderSystem.disableBlend();
   }

   @Override
   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
   }

   private void renderFrame(GuiGraphics graphics) {
      int x = this.leftPos;
      int y = this.topPos;
      graphics.fill(x - 1, y - 1, x + this.imageWidth + 1, y, -2008823553);
      graphics.fill(x - 1, y + this.imageHeight, x + this.imageWidth + 1, y + this.imageHeight + 1, -2008823553);
      graphics.fill(x - 1, y, x, y + this.imageHeight, -2008823553);
      graphics.fill(x + this.imageWidth, y, x + this.imageWidth + 1, y + this.imageHeight, -2008823553);
      outline(graphics, x, y, this.imageWidth, this.imageHeight, -9818728);
      graphics.fillGradient(x, y, x + this.imageWidth, y + 20, -1306523085, -1944843999);
      graphics.fill(x, y + 20 - 1, x + this.imageWidth, y + 20, -4760321);
      drawCornerBrackets(graphics, x, y, this.imageWidth, this.imageHeight);
      String title = "SHADOW OUTLINES";
      graphics.drawString(this.font, title, x + (this.imageWidth - this.font.width(title)) / 2, y + 6, -4760321, false);
   }

   private void renderRoster(GuiGraphics graphics, int mouseX, int mouseY) {
      int x = this.leftPos + 10;
      int y = this.topPos + 28;
      int height = this.types.size() * 12 + 4;
      graphics.fill(x, y, x + 116, y + height, 1711670032);
      outline(graphics, x, y, 116, height, 1430505727);

      for (int index = 0; index < this.types.size(); index++) {
         int rowY = y + 2 + index * 12;
         boolean active = index == this.selected;
         boolean hovered = mouseX >= x && mouseX < x + 116 && mouseY >= rowY && mouseY < rowY + 12;
         if (active) {
            graphics.fill(x + 1, rowY, x + 116 - 1, rowY + 12, 1723292927);
         } else if (hovered) {
            graphics.fill(x + 1, rowY, x + 116 - 1, rowY + 12, 872415231);
         }

         if (active) {
            graphics.fill(x + 1, rowY, x + 3, rowY + 12, -4760321);
         }

         int color = this.menu.glowColor(index);
         int dotX = x + 7;
         int dotY = rowY + 4;
         if (color != -1) {
            graphics.fill(dotX, dotY, dotX + 6, dotY + 6, 0xFF000000 | color);
         } else {
            outline(graphics, dotX, dotY, 6, 6, -11909792);
         }

         boolean owned = this.menu.ownsShadow(index);
         graphics.drawString(this.font, displayName(this.types.get(index)), x + 18, rowY + 3, active ? -1250305 : (owned ? -6510635 : -9804672), false);
      }
   }

   private void renderEditor(GuiGraphics graphics) {
      int x = this.leftPos + 134;
      int y = this.topPos + 28;
      int height = this.types.size() * 12 + 4;
      graphics.fill(x, y, x + 192, y + height, 1711670032);
      outline(graphics, x, y, 192, height, 1438080255);
      graphics.drawString(this.font, displayName(this.types.get(this.selected)), x + 8, y + 7, -1250305, false);
      int swatchX = x + 8;
      int swatchY = y + 21;
      int swatchW = 176;
      graphics.fill(swatchX, swatchY, swatchX + swatchW, swatchY + 22, -871889648);
      if (this.hasOutline()) {
         int color = 0xFF000000 | this.draftColor;
         float pulse = 0.55F + 0.45F * (float)Math.sin(Util.getMillis() / 320.0);
         graphics.fill(swatchX + 1, swatchY + 1, swatchX + swatchW - 1, swatchY + 21, withAlpha(this.draftColor, (int)(51.0F + 60.0F * pulse)));
         outline(graphics, swatchX, swatchY, swatchW, 22, color);
         String hex = String.format("#%06X", this.draftColor);
         graphics.drawCenteredString(this.font, hex, swatchX + swatchW / 2, swatchY + 8, color);
      } else {
         outline(graphics, swatchX, swatchY, swatchW, 22, -12962480);
         graphics.drawCenteredString(this.font, "NO OUTLINE", swatchX + swatchW / 2, swatchY + 8, -9804672);
      }

      int presetY = y + 49;
      int presetW = (swatchW - (PRESETS.length - 1) * 3) / PRESETS.length;

      for (int index = 0; index < PRESETS.length; index++) {
         int px = swatchX + index * (presetW + 3);
         graphics.fill(px, presetY, px + presetW, presetY + 14, 0xFF000000 | PRESETS[index]);
         if (this.hasOutline() && this.draftColor == PRESETS[index]) {
            outline(graphics, px - 1, presetY - 1, presetW + 2, 16, -1250305);
         }
      }

      if (this.menu.supportsArtifact()) {
         this.renderArtifact(graphics, x, y + height + 4);
      }

      this.renderSlotBackgrounds(graphics);
   }

   private void renderArtifact(GuiGraphics graphics, int x, int y) {
      boolean equipped = this.hasEquippedArtifact();
      graphics.drawString(this.font, "ARTIFACT", x + 8, y + 4, -12334849, false);
      graphics.drawString(
         this.font, equipped ? "EQUIPPED" : "EMPTY", x + 192 - 8 - this.font.width(equipped ? "EQUIPPED" : "EMPTY"), y + 4, equipped ? -14262 : -6510635, false
      );
   }

   private void renderSlotBackgrounds(GuiGraphics graphics) {
      if (this.menu.supportsArtifact()) {
         int equipmentX = this.leftPos + 300 - 1;
         int equipmentY = this.topPos + 194 - 1;
         boolean equipped = this.hasEquippedArtifact();
         graphics.fill(equipmentX, equipmentY, equipmentX + 18, equipmentY + 18, equipped ? -2008731890 : -871889648);
         outline(graphics, equipmentX, equipmentY, 18, 18, equipped ? -14262 : -4760321);
      }

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            drawSlot(graphics, this.leftPos + 87 + column * 18 - 1, this.topPos + 216 + row * 18 - 1);
         }
      }

      for (int column = 0; column < 9; column++) {
         drawSlot(graphics, this.leftPos + 87 + column * 18 - 1, this.topPos + 274 - 1);
      }
   }

   private static void drawSlot(GuiGraphics graphics, int x, int y) {
      graphics.fill(x, y, x + 18, y + 18, -871889648);
      outline(graphics, x, y, 18, 18, -2007729240);
   }

   private boolean hasEquippedArtifact() {
      Slot slot = this.menu.get().get(0);
      return slot != null && slot.hasItem();
   }

   private static String displayName(String type) {
      return switch (type) {
         case "goblin_club" -> "Goblin Fighter";
         case "goblin_archer" -> "Goblin Archer";
         case "goblin_mage" -> "Goblin Mage";
         case "wolf" -> "Lycan";
         case "knight" -> "Knight";
         case "polar_bear" -> "Polar Bear";
         case "orc" -> "Orc";
         case "high_orc" -> "High Orc";
         case "igris" -> "Igris";
         case "beru" -> "Beru";
         case "kamish" -> "Kamish";
         case "tusk" -> "Tusk";
         case "kaisel" -> "Kaisel";
         case "iron" -> "Iron";
         default -> type;
      };
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.state != ShadowCustomizationScreen.State.OPEN) {
         return true;
      }

      double logicalX = this.logicalMouseX(mouseX);
      double logicalY = this.logicalMouseY(mouseY);
      int listX = this.leftPos + 10;
      int listY = this.topPos + 28 + 2;
      if (button == 0 && logicalX >= listX && logicalX < listX + 116) {
         int row = (int)((logicalY - listY) / 12.0);
         if (row >= 0 && row < this.types.size() && logicalY >= listY) {
            this.select(row);
            return true;
         }
      }

      int swatchX = this.leftPos + 134 + 8;
      int presetY = this.topPos + 28 + 49;
      int swatchW = 176;
      int presetW = (swatchW - (PRESETS.length - 1) * 3) / PRESETS.length;
      if (button == 0 && logicalY >= presetY && logicalY < presetY + 14) {
         for (int index = 0; index < PRESETS.length; index++) {
            int px = swatchX + index * (presetW + 3);
            if (logicalX >= px && logicalX < px + presetW) {
               this.applyPreset(PRESETS[index]);
               return true;
            }
         }
      }

      return super.mouseClicked(logicalX, logicalY, button);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY), button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      float scale = this.responsiveTransform().scale();
      return super.mouseDragged(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY), button, dragX / scale, dragY / scale);
   }

   @Override
   public void mouseMoved(double mouseX, double mouseY) {
      super.mouseMoved(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY));
   }

   @Override
   public boolean keyPressed(int key, int scanCode, int modifiers) {
      if (key != 256 && (this.minecraft == null || !this.minecraft.options.keyInventory.matches(key, scanCode))) {
         return this.state != ShadowCustomizationScreen.State.OPEN || super.keyPressed(key, scanCode, modifiers);
      }

      this.beginClose();
      return true;
   }

   @Override
   public void onClose() {
      this.beginClose();
   }

   private void beginClose() {
      if (this.state != ShadowCustomizationScreen.State.CLOSING && !this.closed) {
         this.commitDraft();
         this.state = ShadowCustomizationScreen.State.CLOSING;
         this.animStart = Util.getMillis();
         this.playPanelSound(SololevelingModSounds.PANELCLOSE.get(), 0.72F);
      }
   }

   private void updateAnimation() {
      float raw = Math.min(1.0F, (float)(Util.getMillis() - this.animStart) / 190.0F);
      float eased = raw * raw * (3.0F - 2.0F * raw);
      switch (this.state) {
         case OPENING:
            this.reveal = eased;
            if (raw >= 1.0F) {
               this.state = ShadowCustomizationScreen.State.OPEN;
               this.reveal = 1.0F;
            }
            break;
         case OPEN:
            this.reveal = 1.0F;
            break;
         case CLOSING:
            this.reveal = 1.0F - eased;
            if (raw >= 1.0F && !this.closed) {
               this.closed = true;
               if (this.minecraft != null && this.minecraft.player != null) {
                  this.minecraft.player.closeContainer();
               }
            }
      }
   }

   private void playPanelSound(SoundEvent sound, float pitch) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null && minecraft.getSoundManager() != null) {
         minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, 0.46F));
      }
   }

   private ResponsiveGuiScale.Transform responsiveTransform() {
      return ResponsiveGuiScale.fit(this.width, this.height, 344, 308);
   }

   private double logicalMouseX(double mouseX) {
      return this.responsiveTransform().logicalX(mouseX);
   }

   private double logicalMouseY(double mouseY) {
      return this.responsiveTransform().logicalY(mouseY);
   }

   private void renderAnimatedBackground(GuiGraphics graphics, int mouseX, int mouseY) {
      float localX = clamp01((float)(mouseX - this.leftPos) / this.imageWidth);
      float localY = clamp01((float)(mouseY - this.topPos) / this.imageHeight);
      ShaderInstance shader = ShadowSummonBackgroundRenderTypes.get();
      if (shader == null) {
         graphics.fillGradient(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, -268040430, -267122915);
      } else {
         RenderSystem.setShader(ShadowSummonBackgroundRenderTypes::get);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableCull();
         AbstractUniform mouse = shader.safeGetUniform("MousePos");
         mouse.set(localX, localY);
         Matrix4f matrix = graphics.pose().last().pose();
         BufferBuilder buffer = Tesselator.getInstance().getBuilder();
         buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         buffer.vertex(matrix, this.leftPos, this.topPos + this.imageHeight, 0.0F).uv(0.0F, 1.0F).endVertex();
         buffer.vertex(matrix, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0.0F).uv(1.0F, 1.0F).endVertex();
         buffer.vertex(matrix, this.leftPos + this.imageWidth, this.topPos, 0.0F).uv(1.0F, 0.0F).endVertex();
         buffer.vertex(matrix, this.leftPos, this.topPos, 0.0F).uv(0.0F, 0.0F).endVertex();
         Tesselator.getInstance().end();
         RenderSystem.enableCull();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static float clamp01(float value) {
      return value < 0.0F ? 0.0F : Math.min(value, 1.0F);
   }

   private static int withAlpha(int rgb, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 16777215;
   }

   private static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      graphics.fill(x, y, x + width, y + 1, color);
      graphics.fill(x, y + height - 1, x + width, y + height, color);
      graphics.fill(x, y, x + 1, y + height, color);
      graphics.fill(x + width - 1, y, x + width, y + height, color);
   }

   private static void drawCornerBrackets(GuiGraphics graphics, int x, int y, int width, int height) {
      int length = 13;
      graphics.fill(x - 1, y - 1, x + length, y + 1, -12334849);
      graphics.fill(x - 1, y - 1, x + 1, y + length, -12334849);
      graphics.fill(x + width - length, y - 1, x + width + 1, y + 1, -4760321);
      graphics.fill(x + width - 1, y - 1, x + width + 1, y + length, -4760321);
      graphics.fill(x - 1, y + height - 1, x + length, y + height + 1, -12334849);
      graphics.fill(x - 1, y + height - length, x + 1, y + height + 1, -12334849);
      graphics.fill(x + width - length, y + height - 1, x + width + 1, y + height + 1, -4760321);
      graphics.fill(x + width - 1, y + height - length, x + width + 1, y + height + 1, -4760321);
   }

   private final class ColorSlider extends AbstractSliderButton {
      private final String channel;
      private final int shift;

      private ColorSlider(int x, int y, String channel, int shift) {
         super(x, y, 176, 16, Component.empty(), (ShadowCustomizationScreen.this.editableColor() >> shift & 0xFF) / 255.0);
         this.channel = channel;
         this.shift = shift;
         this.updateMessage();
      }

      private void pull() {
         this.value = (ShadowCustomizationScreen.this.editableColor() >> this.shift & 0xFF) / 255.0;
         this.updateMessage();
      }

      @Override
      protected void updateMessage() {
         this.setMessage(Component.literal(this.channel + "  " + (int)Math.round(this.value * 255.0)));
      }

      @Override
      protected void applyValue() {
         int channelValue = (int)Math.round(this.value * 255.0);
         int base = ShadowCustomizationScreen.this.editableColor();
         ShadowCustomizationScreen.this.draftColor = base & ~(255 << this.shift) | channelValue << this.shift;
         ShadowCustomizationScreen.this.draftDirty = true;
         ShadowCustomizationScreen.this.draftIdleTicks = 0;
         ShadowCustomizationScreen.this.refreshToggleLabel();
         this.updateMessage();
      }
   }

   private enum State {
      OPENING,
      OPEN,
      CLOSING;
   }
}
