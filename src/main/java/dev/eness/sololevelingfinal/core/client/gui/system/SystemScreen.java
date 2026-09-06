package dev.eness.sololevelingfinal.core.client.gui.system;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import dev.eness.sololevelingfinal.core.client.gui.ResponsiveGuiScale;
import dev.eness.sololevelingfinal.core.client.renderer.shader.SystemBackgroundRenderTypes;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;
import org.joml.Matrix4f;

public abstract class SystemScreen extends Screen {
   protected static final int ACCENT = -12597505;
   protected static final int ACCENT_DIM = -14519384;
   protected static final int ACCENT_SOFT = 1430243071;
   protected static final int TEXT_MAIN = -1509633;
   protected static final int TEXT_SUB = -7358248;
   protected static final int PANEL_FILL = -1039791594;
   protected int panelW = 196;
   protected int panelH = 300;
   protected int panelX;
   protected int panelY;
   private static final long ANIM_MS = 180L;
   private SystemScreen.State state = SystemScreen.State.OPENING;
   private long animStart;
   private boolean closed;
   private boolean accessDenied;
   private float reveal;

   protected SystemScreen(Component title) {
      super(title);
   }

   @Override
   protected void init() {
      super.init();
      this.accessDenied = !SystemPlayerAccess.hasSystem(this.minecraft == null ? null : this.minecraft.player) && !this.allowsNonSystemAccess();
      if (this.accessDenied) {
         if (this.minecraft != null) {
            this.minecraft.setScreen(null);
         }
      } else {
         if (this.shouldPlaySystemSounds()) {
            SystemGuiSounds.enter();
         }

         this.panelX = (this.width - this.panelW) / 2;
         this.panelY = (this.height - this.panelH) / 2;
         this.state = SystemScreen.State.OPENING;
         this.animStart = Util.getMillis();
         this.closed = false;
      }
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
   public boolean keyPressed(int key, int scan, int mods) {
      if (key == 256) {
         this.beginClose();
         return true;
      } else {
         return super.keyPressed(key, scan, mods);
      }
   }

   protected void beginClose() {
      if (this.state != SystemScreen.State.CLOSING) {
         if (this.shouldPlaySystemSounds()) {
            SystemGuiSounds.exit();
         }

         this.state = SystemScreen.State.CLOSING;
         this.animStart = Util.getMillis();
      }
   }

   protected boolean isFullyOpen() {
      return this.state == SystemScreen.State.OPEN;
   }

   private void updateAnimation() {
      float raw = Math.min(1.0F, (float)(Util.getMillis() - this.animStart) / 180.0F);
      float eased = raw * raw * (3.0F - 2.0F * raw);
      switch (this.state) {
         case OPENING:
            this.reveal = eased;
            if (raw >= 1.0F) {
               this.state = SystemScreen.State.OPEN;
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
               this.minecraft.setScreen(null);
            }
      }
   }

   private void setWidgetsVisible(boolean visible) {
      for (GuiEventListener child : this.children()) {
         if (child instanceof AbstractWidget widget) {
            widget.visible = visible;
            widget.active = visible;
         }
      }
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (!this.accessDenied) {
         this.updateAnimation();
         if (!this.closed) {
            ResponsiveGuiScale.Transform transform = this.responsiveTransform();
            int logicalMouseX = transform.logicalMouseX(mouseX);
            int logicalMouseY = transform.logicalMouseY(mouseY);
            this.setWidgetsVisible(this.state == SystemScreen.State.OPEN);
            this.renderBackground(guiGraphics);
            guiGraphics.flush();
            ResponsiveGuiScale.push(guiGraphics, transform);
            int centerY = this.panelY + this.panelH / 2;
            int halfH = Math.round((this.panelH / 2.0F + 4.0F) * this.reveal);
            int top = centerY - halfH;
            int bottom = centerY + halfH;
            int sx0 = this.panelX - 2;
            int sx1 = this.panelX + this.panelW + 2;
            ResponsiveGuiScale.enableScissor(guiGraphics, transform, sx0, top, sx1, bottom);
            this.renderAnimatedBackground(guiGraphics, logicalMouseX, logicalMouseY);
            this.renderFrame(guiGraphics);
            this.renderContent(guiGraphics, logicalMouseX, logicalMouseY, partialTicks);
            super.render(guiGraphics, logicalMouseX, logicalMouseY, partialTicks);
            guiGraphics.disableScissor();
            if (this.reveal < 1.0F) {
               guiGraphics.fill(sx0, top, sx1, top + 1, -12597505);
               guiGraphics.fill(sx0, bottom - 1, sx1, bottom, -12597505);
               guiGraphics.fill(sx0, top + 1, sx1, top + 2, 1430243071);
               guiGraphics.fill(sx0, bottom - 2, sx1, bottom - 1, 1430243071);
            }

            ResponsiveGuiScale.pop(guiGraphics);
            if (this.state == SystemScreen.State.OPEN) {
               List<Component> tip = this.getHoverTooltip(logicalMouseX, logicalMouseY);
               if (tip != null && !tip.isEmpty()) {
                  SystemTooltip.render(guiGraphics, this.font, tip, mouseX, mouseY, this.width, this.height);
               }
            }
         }
      }
   }

   protected ResponsiveGuiScale.Transform responsiveTransform() {
      return ResponsiveGuiScale.fit(this.width, this.height, this.panelW + 6, this.panelH + 6);
   }

   protected double logicalMouseX(double mouseX) {
      return this.responsiveTransform().logicalX(mouseX);
   }

   protected double logicalMouseY(double mouseY) {
      return this.responsiveTransform().logicalY(mouseY);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return super.mouseClicked(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY), button);
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

   protected abstract void renderContent(GuiGraphics var1, int var2, int var3, float var4);

   protected List<Component> getHoverTooltip(int mouseX, int mouseY) {
      return null;
   }

   protected void openChild(Screen screen) {
      if (this.minecraft != null) {
         if (this.shouldPlaySystemSounds()) {
            SystemGuiSounds.switchInsideSystem();
         }

         this.minecraft.setScreen(screen);
      }
   }

   protected boolean allowsNonSystemAccess() {
      return false;
   }

   protected boolean shouldPlaySystemSounds() {
      return SystemPlayerAccess.hasSystem(this.minecraft == null ? null : this.minecraft.player);
   }

   protected static boolean isOver(int mouseX, int mouseY, int x, int y, int w, int h) {
      return mouseX >= x && mouseX < x + w && mouseY >= y - 1 && mouseY < y + h;
   }

   protected void renderAnimatedBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      float localX = clamp01((float)(mouseX - this.panelX) / this.panelW);
      float localY = clamp01((float)(mouseY - this.panelY) / this.panelH);
      ShaderInstance shader = SystemBackgroundRenderTypes.get();
      if (shader == null) {
         this.renderJavaFallbackBackground(guiGraphics, localX, localY);
      } else {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShader(SystemBackgroundRenderTypes::get);
         AbstractUniform mouse = shader.safeGetUniform("MousePos");
         mouse.set(localX, localY);
         shader.safeGetUniform("MouseGlitch").set(1.0F);
         int x0 = this.panelX;
         int y0 = this.panelY;
         int x1 = this.panelX + this.panelW;
         int y1 = this.panelY + this.panelH;
         Matrix4f matrix = guiGraphics.pose().last().pose();
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

   private void renderJavaFallbackBackground(GuiGraphics g, float localX, float localY) {
      int x0 = this.panelX;
      int y0 = this.panelY;
      int w = this.panelW;
      int h = this.panelH;
      g.fillGradient(x0, y0, x0 + w, y0 + h, -268039400, -268369400);
      float t = (float)(Util.getMillis() % 100000L) / 1000.0F;

      for (int i = 0; i < 40; i++) {
         float seed = i * 12.9898F;
         float fx = frac((float)Math.sin(seed) * 43758.547F);
         float fy = frac((float)Math.sin(seed * 1.7F) * 43758.547F);
         float drift = (fy * h + t * (8.0F + fx * 24.0F)) % h;
         int px = x0 + (int)(fx * w);
         int py = y0 + (int)drift;
         int alpha = (int)(60.0 + 60.0 * Math.sin(t * 1.6F + i));
         alpha = Math.max(20, Math.min(160, alpha));
         g.fill(px, py, px + 1, py + 1, alpha << 24 | 4179711);
      }

      int bandY = y0 + (int)(t * 26.0F % h);

      for (int sx = 0; sx < w; sx += 2) {
         float rnd = frac((float)Math.sin(sx * 3.1F + Math.floor(t * 3.0F)) * 4517.3F);
         if (rnd > 0.5F) {
            int a = (int)(40.0F + 80.0F * rnd);
            g.fill(x0 + sx, bandY, x0 + sx + 1, bandY + 2, a << 24 | 3054550);
         }
      }

      int mx = x0 + (int)(localX * w);
      int my = y0 + (int)(localY * h);
      long tick = (long)Math.floor(t * 12.0F);
      g.fill(mx - 3, my, mx + 4, my + 1, -2009086209);
      g.fill(mx, my - 3, mx + 1, my + 4, 1715455743);
      g.fill(mx - 6, my - 5, mx + 6, my + 5, 306169599);

      for (int i = 0; i < 12; i++) {
         float seed = i * 31.37F + (float)tick * 7.13F;
         float rx = frac((float)Math.sin(seed) * 43758.547F);
         float ry = frac((float)Math.sin(seed * 1.83F) * 24634.635F);
         float rw = frac((float)Math.sin(seed * 2.41F) * 18331.473F);
         int ox = (int)((rx - 0.5F) * 48.0F);
         int oy = (int)((ry - 0.5F) * 34.0F);
         int len = 4 + (int)(rw * 18.0F);
         int alpha = 36 + (int)(90.0F * rw);
         int y = my + oy;
         int x = mx + ox;
         g.fill(x, y, x + len, y + 1, alpha << 24 | 4179711);
         if (rw > 0.64F) {
            g.fill(x - 1, y, x + 1, y + 1, 1157585806);
            g.fill(x + len, y, x + len + 2, y + 1, 1429630207);
         }
      }

      g.fillGradient(x0, y0, x0 + w, y0 + h, 570425344, 1996488704);
   }

   private static float frac(float v) {
      return v - (float)Math.floor(v);
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }

   protected void renderFrame(GuiGraphics g) {
      int x = this.panelX;
      int y = this.panelY;
      int w = this.panelW;
      int h = this.panelH;
      g.fill(x - 1, y - 1, x + w + 1, y, 1430243071);
      g.fill(x - 1, y + h, x + w + 1, y + h + 1, 1430243071);
      g.fill(x - 1, y, x, y + h, 1430243071);
      g.fill(x + w, y, x + w + 1, y + h, 1430243071);
      this.drawRectOutline(g, x, y, w, h, -14519384);
      g.fill(x, y, x + w, y + 18, 1712335422);
      g.fill(x, y + 18, x + w, y + 19, -12597505);
      this.drawCornerBrackets(g, x, y, w, h);
      Font font = Minecraft.getInstance().font;
      String title = "[ " + this.title.getString() + " ]";
      g.drawString(font, title, x + (w - font.width(title)) / 2, y + 5, -12597505, false);
   }

   private void drawRectOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
      g.fill(x, y, x + w, y + 1, color);
      g.fill(x, y + h - 1, x + w, y + h, color);
      g.fill(x, y, x + 1, y + h, color);
      g.fill(x + w - 1, y, x + w, y + h, color);
   }

   private void drawCornerBrackets(GuiGraphics g, int x, int y, int w, int h) {
      int len = 12;
      g.fill(x - 1, y - 1, x + len, y + 1, -12597505);
      g.fill(x - 1, y - 1, x + 1, y + len, -12597505);
      g.fill(x + w - len, y - 1, x + w + 1, y + 1, -12597505);
      g.fill(x + w - 1, y - 1, x + w + 1, y + len, -12597505);
      g.fill(x - 1, y + h - 1, x + len, y + h + 1, -12597505);
      g.fill(x - 1, y + h - len, x + 1, y + h + 1, -12597505);
      g.fill(x + w - len, y + h - 1, x + w + 1, y + h + 1, -12597505);
      g.fill(x + w - 1, y + h - len, x + w + 1, y + h + 1, -12597505);
   }

   private enum State {
      OPENING,
      OPEN,
      CLOSING;
   }

   public static class SystemButton extends Button {
      public SystemButton(int x, int y, int w, int h, Component label, OnPress onPress) {
         super(x, y, w, h, label, onPress, DEFAULT_NARRATION);
      }

      @Override
      protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            boolean hovered = this.isHoveredOrFocused();
            int fill = hovered ? -2142258968 : 1427120952;
            int border = hovered ? -8395521 : -14519384;
            int text = hovered ? -1 : -1509633;
            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, fill);
            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, border);
            g.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, border);
            g.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, border);
            g.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, border);
            Font font = Minecraft.getInstance().font;
            g.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, text);
         }
      }
   }

   public static class SystemSlider extends AbstractSliderButton {
      private final DoubleFunction<Component> labelFn;
      private final DoubleConsumer onApply;
      private final Runnable onRelease;

      public SystemSlider(int x, int y, int w, int h, double value, DoubleFunction<Component> labelFn, DoubleConsumer onApply, Runnable onRelease) {
         super(x, y, w, h, Component.empty(), value);
         this.labelFn = labelFn;
         this.onApply = onApply;
         this.onRelease = onRelease;
         this.updateMessage();
      }

      @Override
      protected void updateMessage() {
         this.setMessage(this.labelFn.apply(this.value));
      }

      @Override
      protected void applyValue() {
         this.onApply.accept(this.value);
      }

      @Override
      public void onRelease(double mouseX, double mouseY) {
         if (this.onRelease != null) {
            this.onRelease.run();
         }
      }

      @Override
      public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            boolean hovered = this.isHoveredOrFocused();
            int border = !this.active ? 1429230180 : (hovered ? -8395521 : -14519384);
            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.active ? 1427120952 : 856692772);
            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, border);
            g.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, border);
            g.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, border);
            g.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, border);
            int fillW = (int)(this.value * (this.width - 2));
            g.fill(this.getX() + 1, this.getY() + 1, this.getX() + 1 + fillW, this.getY() + this.height - 1, 1430243071);
            int knobX = this.getX() + (int)(this.value * (this.width - 4));
            g.fill(knobX, this.getY(), knobX + 4, this.getY() + this.height, !this.active ? 1716150384 : (hovered ? -8395521 : -12597505));
            Font font = Minecraft.getInstance().font;
            g.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, this.active ? -1509633 : -10454902);
         }
      }
   }
}
