package dev.eness.sololevelingfinal.core.client.gui.system;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import dev.eness.sololevelingfinal.core.client.gui.ResponsiveGuiScale;
import dev.eness.sololevelingfinal.core.client.renderer.shader.SystemBackgroundRenderTypes;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;
import org.joml.Matrix4f;

public abstract class SystemContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
   protected static final int ACCENT = -12597505;
   protected static final int ACCENT_SOFT = 1430243071;
   protected int pRelX = -80;
   protected int pRelY = -100;
   protected int pW = 160;
   protected int pH = 200;
   private static final long ANIM_MS = 170L;
   private SystemContainerScreen.State state = SystemContainerScreen.State.OPENING;
   private long animStart;
   private boolean closed;
   private boolean accessDenied;
   private float reveal;

   protected SystemContainerScreen(T menu, Inventory inv, Component title) {
      super(menu, inv, title);
   }

   @Override
   protected void init() {
      super.init();
      this.accessDenied = !SystemPlayerAccess.hasSystem(this.minecraft == null ? null : this.minecraft.player) && !this.allowsNonSystemAccess();
      if (this.accessDenied) {
         if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
         }
      } else {
         if (this.shouldPlaySystemSounds()) {
            SystemGuiSounds.enter();
         }

         this.leftPos = (this.width - this.pW) / 2 - this.pRelX;
         this.topPos = (this.height - this.pH) / 2 - this.pRelY;
         this.state = SystemContainerScreen.State.OPENING;
         this.animStart = Util.getMillis();
         this.closed = false;
      }
   }

   protected boolean isOpen() {
      return this.state == SystemContainerScreen.State.OPEN;
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
      if (key != 256 && (this.minecraft == null || !this.minecraft.options.keyInventory.matches(key, scan))) {
         return super.keyPressed(key, scan, mods);
      }

      this.beginClose();
      return true;
   }

   protected void beginClose() {
      if (this.state != SystemContainerScreen.State.CLOSING) {
         if (this.shouldPlaySystemSounds()) {
            SystemGuiSounds.exit();
         }

         this.state = SystemContainerScreen.State.CLOSING;
         this.animStart = Util.getMillis();
      }
   }

   protected void openSystemScreen(Screen screen) {
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

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return !this.isOpen() ? true : super.mouseClicked(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY), button);
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

   private void updateAnimation() {
      float raw = Math.min(1.0F, (float)(Util.getMillis() - this.animStart) / 170.0F);
      float eased = raw * raw * (3.0F - 2.0F * raw);
      switch (this.state) {
         case OPENING:
            this.reveal = eased;
            if (raw >= 1.0F) {
               this.state = SystemContainerScreen.State.OPEN;
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
               this.onBeforeCloseAnimationFinished();
               if (this.minecraft != null && this.minecraft.player != null) {
                  this.minecraft.player.closeContainer();
               }

               this.onCloseAnimationFinished();
            }
      }
   }

   protected void onCloseAnimationFinished() {
   }

   protected void onBeforeCloseAnimationFinished() {
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
   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      if (!this.accessDenied) {
         this.updateAnimation();
         if (!this.closed) {
            this.setWidgetsVisible(this.state == SystemContainerScreen.State.OPEN);
            ResponsiveGuiScale.Transform transform = this.responsiveTransform();
            int logicalMouseX = transform.logicalMouseX(mouseX);
            int logicalMouseY = transform.logicalMouseY(mouseY);
            this.renderBackground(g);
            g.flush();
            ResponsiveGuiScale.push(g, transform);
            int ax = this.leftPos + this.pRelX;
            int ay = this.topPos + this.pRelY;
            int centerY = ay + this.pH / 2;
            int halfH = Math.round((this.pH / 2.0F + 4.0F) * this.reveal);
            int top = centerY - halfH;
            int bottom = centerY + halfH;
            int sx0 = ax - 3;
            int sx1 = ax + this.pW + 3;
            ResponsiveGuiScale.enableScissor(g, transform, sx0, top, sx1, bottom);
            this.drawShaderBackground(g, ax, ay, logicalMouseX, logicalMouseY);
            super.render(g, logicalMouseX, logicalMouseY, partialTicks);
            g.disableScissor();
            if (this.reveal < 1.0F) {
               int accent = this.revealAccent();
               int accentSoft = this.revealAccentSoft();
               g.fill(sx0, top, sx1, top + 1, accent);
               g.fill(sx0, bottom - 1, sx1, bottom, accent);
               g.fill(sx0, top + 1, sx1, top + 2, accentSoft);
               g.fill(sx0, bottom - 2, sx1, bottom - 1, accentSoft);
            }

            if (this.state == SystemContainerScreen.State.OPEN) {
               this.renderExtras(g, logicalMouseX, logicalMouseY);
            }

            ResponsiveGuiScale.pop(g);
            if (this.state == SystemContainerScreen.State.OPEN) {
               this.renderTooltip(g, mouseX, mouseY);
            }
         }
      }
   }

   protected ResponsiveGuiScale.Transform responsiveTransform() {
      return ResponsiveGuiScale.fit(this.width, this.height, this.pW + 8, this.pH + 8);
   }

   protected double logicalMouseX(double mouseX) {
      return this.responsiveTransform().logicalX(mouseX);
   }

   protected double logicalMouseY(double mouseY) {
      return this.responsiveTransform().logicalY(mouseY);
   }

   protected void renderExtras(GuiGraphics g, int mouseX, int mouseY) {
   }

   protected void drawShaderBackground(GuiGraphics g, int ax, int ay, int mouseX, int mouseY) {
      float localX = clamp01((float)(mouseX - ax) / this.pW);
      float localY = clamp01((float)(mouseY - ay) / this.pH);
      ShaderInstance shader = this.backgroundShader();
      if (shader == null) {
         this.renderBackgroundFallback(g, ax, ay, localX, localY);
      } else {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShader(this::backgroundShader);
         this.configureBackgroundShader(shader, localX, localY);
         Matrix4f matrix = g.pose().last().pose();
         BufferBuilder buffer = Tesselator.getInstance().getBuilder();
         buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         buffer.vertex(matrix, ax, ay + this.pH, 0.0F).uv(0.0F, 1.0F).endVertex();
         buffer.vertex(matrix, ax + this.pW, ay + this.pH, 0.0F).uv(1.0F, 1.0F).endVertex();
         buffer.vertex(matrix, ax + this.pW, ay, 0.0F).uv(1.0F, 0.0F).endVertex();
         buffer.vertex(matrix, ax, ay, 0.0F).uv(0.0F, 0.0F).endVertex();
         Tesselator.getInstance().end();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   protected ShaderInstance backgroundShader() {
      return SystemBackgroundRenderTypes.get();
   }

   protected void configureBackgroundShader(ShaderInstance shader, float localX, float localY) {
      AbstractUniform mouse = shader.safeGetUniform("MousePos");
      mouse.set(localX, localY);
      shader.safeGetUniform("MouseGlitch").set(1.0F);
   }

   protected void renderBackgroundFallback(GuiGraphics g, int ax, int ay, float localX, float localY) {
      g.fillGradient(ax, ay, ax + this.pW, ay + this.pH, -268038881, -268369399);
   }

   protected int revealAccent() {
      return -12597505;
   }

   protected int revealAccentSoft() {
      return 1430243071;
   }

   protected static float clamp01(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }

   private enum State {
      OPENING,
      OPEN,
      CLOSING;
   }
}
