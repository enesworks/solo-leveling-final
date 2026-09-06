package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import dev.eness.sololevelingfinal.core.client.renderer.shader.ShadowSummonBackgroundRenderTypes;
import dev.eness.sololevelingfinal.core.init.SololevelingModSounds;
import org.joml.Matrix4f;

public abstract class ShadowStyledScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
   protected static final int ACCENT = -4760321;
   protected static final int ACCENT_BLUE = -12334849;
   protected static final int ACCENT_DIM = -9818728;
   protected static final int TEXT_MAIN = -1250305;
   protected static final int TEXT_SUB = -6510635;
   private static final long ANIM_MS = 190L;
   private static final float OPEN_SOUND_PITCH = 0.76F;
   private static final float CLOSE_SOUND_PITCH = 0.7F;
   private static final float SOUND_VOLUME = 0.46F;
   private ShadowStyledScreen.State state = ShadowStyledScreen.State.OPENING;
   private long animStart;
   private boolean closed;
   private float reveal;

   protected ShadowStyledScreen(T container, Inventory inventory, Component title, int imageWidth, int imageHeight) {
      super(container, inventory, title);
      this.imageWidth = imageWidth;
      this.imageHeight = imageHeight;
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
      this.state = ShadowStyledScreen.State.OPENING;
      this.animStart = Util.getMillis();
      this.closed = false;
      this.reveal = 0.0F;
      this.playPanelSound(SololevelingModSounds.PANELOPEN.get(), 0.76F);
      this.initShadowWidgets();
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.updateAnimation();
      if (!this.closed) {
         ResponsiveGuiScale.Transform transform = this.responsiveTransform();
         int logicalMouseX = transform.logicalMouseX(mouseX);
         int logicalMouseY = transform.logicalMouseY(mouseY);
         this.renderBackground(guiGraphics);
         guiGraphics.flush();
         ResponsiveGuiScale.push(guiGraphics, transform);
         int centerY = this.topPos + this.imageHeight / 2;
         int halfH = Math.round((this.imageHeight / 2.0F + 4.0F) * this.reveal);
         int top = centerY - halfH;
         int bottom = centerY + halfH;
         int sx0 = this.leftPos - 3;
         int sx1 = this.leftPos + this.imageWidth + 3;
         ResponsiveGuiScale.enableScissor(guiGraphics, transform, sx0, top, sx1, bottom);
         super.render(guiGraphics, logicalMouseX, logicalMouseY, partialTicks);
         guiGraphics.disableScissor();
         if (this.reveal < 1.0F) {
            guiGraphics.fill(sx0, top, sx1, top + 1, -4760321);
            guiGraphics.fill(sx0, bottom - 1, sx1, bottom, -4760321);
            guiGraphics.fill(sx0, top + 1, sx1, top + 2, 2008505599);
            guiGraphics.fill(sx0, bottom - 2, sx1, bottom - 1, 2000931071);
         }

         if (this.state == ShadowStyledScreen.State.OPEN) {
            this.renderShadowTooltips(guiGraphics, logicalMouseX, logicalMouseY);
         }

         ResponsiveGuiScale.pop(guiGraphics);
         if (this.state == ShadowStyledScreen.State.OPEN) {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
         }
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      this.renderAnimatedBackground(guiGraphics, mouseX, mouseY);
      this.renderFrame(guiGraphics);
      this.renderShadowSections(guiGraphics);
      RenderSystem.disableBlend();
   }

   @Override
   public boolean keyPressed(int key, int scanCode, int modifiers) {
      if (key != 256 && (this.minecraft == null || !this.minecraft.options.keyInventory.matches(key, scanCode))) {
         return super.keyPressed(key, scanCode, modifiers);
      }

      this.beginClose();
      return true;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.state != ShadowStyledScreen.State.OPEN ? true : super.mouseClicked(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY), button);
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
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      return super.mouseScrolled(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY), delta);
   }

   @Override
   public void mouseMoved(double mouseX, double mouseY) {
      super.mouseMoved(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY));
   }

   protected ResponsiveGuiScale.Transform responsiveTransform() {
      return ResponsiveGuiScale.fit(this.width, this.height, this.imageWidth + 8, this.imageHeight + 8);
   }

   protected double logicalMouseX(double mouseX) {
      return this.responsiveTransform().logicalX(mouseX);
   }

   protected double logicalMouseY(double mouseY) {
      return this.responsiveTransform().logicalY(mouseY);
   }

   @Override
   public void onClose() {
      this.beginClose();
   }

   protected abstract String shadowTitle();

   protected abstract void initShadowWidgets();

   protected abstract void renderShadowSections(GuiGraphics var1);

   protected void renderShadowTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
   }

   protected boolean isOpeningOrOpen() {
      return this.state == ShadowStyledScreen.State.OPENING || this.state == ShadowStyledScreen.State.OPEN;
   }

   private void beginClose() {
      if (this.state != ShadowStyledScreen.State.CLOSING && !this.closed) {
         this.state = ShadowStyledScreen.State.CLOSING;
         this.animStart = Util.getMillis();
         this.playPanelSound(SololevelingModSounds.PANELCLOSE.get(), 0.7F);
      }
   }

   private void updateAnimation() {
      float raw = Math.min(1.0F, (float)(Util.getMillis() - this.animStart) / 190.0F);
      float eased = raw * raw * (3.0F - 2.0F * raw);
      switch (this.state) {
         case OPENING:
            this.reveal = eased;
            if (raw >= 1.0F) {
               this.state = ShadowStyledScreen.State.OPEN;
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
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getSoundManager() != null) {
         mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, 0.46F));
      }
   }

   private void renderFrame(GuiGraphics g) {
      int x = this.leftPos;
      int y = this.topPos;
      int w = this.imageWidth;
      int h = this.imageHeight;
      g.fill(x - 1, y - 1, x + w + 1, y, -2008823553);
      g.fill(x - 1, y + h, x + w + 1, y + h + 1, -2008823553);
      g.fill(x - 1, y, x, y + h, -2008823553);
      g.fill(x + w, y, x + w + 1, y + h, -2008823553);
      outline(g, x, y, w, h, -9818728);
      g.fill(x, y, x + w, y + 22, 2048133409);
      g.fill(x, y + 22, x + w, y + 23, -4760321);
      this.drawCornerBrackets(g, x, y, w, h);
      String title = "[ " + this.shadowTitle() + " ]";
      g.drawString(this.font, title, x + (w - this.font.width(title)) / 2, y + 7, -4760321, false);
   }

   private void renderAnimatedBackground(GuiGraphics g, int mouseX, int mouseY) {
      float localX = clamp01((float)(mouseX - this.leftPos) / this.imageWidth);
      float localY = clamp01((float)(mouseY - this.topPos) / this.imageHeight);
      ShaderInstance shader = ShadowSummonBackgroundRenderTypes.get();
      if (shader == null) {
         this.renderFallbackBackground(g, localX, localY);
      } else {
         RenderSystem.setShader(ShadowSummonBackgroundRenderTypes::get);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableCull();
         AbstractUniform mouse = shader.safeGetUniform("MousePos");
         mouse.set(localX, localY);
         Matrix4f matrix = g.pose().last().pose();
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

   private void renderFallbackBackground(GuiGraphics g, float localX, float localY) {
      int x = this.leftPos;
      int y = this.topPos;
      g.fillGradient(x, y, x + this.imageWidth, y + this.imageHeight, -268040430, -267122915);
      float t = (float)(Util.getMillis() % 100000L) / 1000.0F;
      int mx = x + (int)(localX * this.imageWidth);
      int my = y + (int)(localY * this.imageHeight);

      for (int i = 0; i < 14; i++) {
         int lineY = my - 38 + i * 6;
         int shift = (int)(Math.sin(t * 12.0F + i * 1.7F) * 10.0);
         int alpha = 35 + (int)(25.0 * (0.5 + 0.5 * Math.sin(t * 8.0F + i)));
         g.fill(mx - 54 + shift, lineY, mx + 54 + shift, lineY + 1, alpha << 24 | 12016895);
         if (i % 3 == 0) {
            g.fill(mx - 44 - shift, lineY + 2, mx + 42 - shift, lineY + 3, alpha << 24 | 4442367);
         }
      }

      g.fillGradient(x, y, x + this.imageWidth, y + this.imageHeight, 0, 1711276032);
   }

   protected static void outline(GuiGraphics g, int x, int y, int w, int h, int color) {
      g.fill(x, y, x + w, y + 1, color);
      g.fill(x, y + h - 1, x + w, y + h, color);
      g.fill(x, y, x + 1, y + h, color);
      g.fill(x + w - 1, y, x + w, y + h, color);
   }

   protected static boolean isOver(double mouseX, double mouseY, Button button) {
      return button != null
         && mouseX >= button.getX()
         && mouseX < button.getX() + button.getWidth()
         && mouseY >= button.getY()
         && mouseY < button.getY() + button.getHeight();
   }

   private void drawCornerBrackets(GuiGraphics g, int x, int y, int w, int h) {
      int len = 15;
      g.fill(x - 1, y - 1, x + len, y + 1, -12334849);
      g.fill(x - 1, y - 1, x + 1, y + len, -12334849);
      g.fill(x + w - len, y - 1, x + w + 1, y + 1, -4760321);
      g.fill(x + w - 1, y - 1, x + w + 1, y + len, -4760321);
      g.fill(x - 1, y + h - 1, x + len, y + h + 1, -12334849);
      g.fill(x - 1, y + h - len, x + 1, y + h + 1, -12334849);
      g.fill(x + w - len, y + h - 1, x + w + 1, y + h + 1, -4760321);
      g.fill(x + w - 1, y + h - len, x + w + 1, y + h + 1, -4760321);
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : Math.min(v, 1.0F);
   }

   protected static class ShadowButton extends Button {
      private final boolean purpleAccent;
      private final boolean compact;

      ShadowButton(int x, int y, int w, int h, Component label, boolean purpleAccent, OnPress onPress) {
         this(x, y, w, h, label, purpleAccent, false, onPress);
      }

      ShadowButton(int x, int y, int w, int h, Component label, boolean purpleAccent, boolean compact, OnPress onPress) {
         super(x, y, w, h, label, onPress, DEFAULT_NARRATION);
         this.purpleAccent = purpleAccent;
         this.compact = compact;
      }

      @Override
      protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
         boolean hovered = this.isHoveredOrFocused();
         int accent = this.purpleAccent ? -4760321 : -12334849;
         int border = !this.active ? 1716804468 : (hovered ? -1 : accent);
         int fill = !this.active ? 856691487 : (hovered ? (this.purpleAccent ? -2008408728 : -2008823553) : 1427120952);
         g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, fill);
         ShadowStyledScreen.outline(g, this.getX(), this.getY(), this.width, this.height, border);
         Font font = Minecraft.getInstance().font;
         int color = !this.active ? -9604468 : (hovered ? -1 : -1250305);
         if (this.compact) {
            g.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
         } else {
            String text = font.plainSubstrByWidth(this.getMessage().getString(), this.width - 14);
            g.drawString(font, text, this.getX() + 7, this.getY() + (this.height - 8) / 2, color, false);
         }
      }
   }

   private enum State {
      OPENING,
      OPEN,
      CLOSING;
   }
}
