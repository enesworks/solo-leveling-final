package dev.eness.sololevelingfinal.core.client.gui.system;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import dev.eness.sololevelingfinal.core.client.renderer.shader.SystemBackgroundRenderTypes;
import org.joml.Matrix4f;

public final class SystemTooltip {
   private static final int ACCENT = -12597505;
   private static final int ACCENT_DIM = -14519384;
   private static final int TEXT_MAIN = -1509633;

   private SystemTooltip() {
   }

   public static void render(GuiGraphics g, Font font, List<Component> lines, int mouseX, int mouseY, int screenW, int screenH) {
      if (lines != null && !lines.isEmpty()) {
         int pad = 5;
         int textW = 0;

         for (Component c : lines) {
            textW = Math.max(textW, font.width(c));
         }

         int boxW = textW + pad * 2;
         int boxH = lines.size() * 10 + pad * 2 - 2;
         int bx = mouseX + 12;
         int by = mouseY - 12;
         if (bx + boxW > screenW - 2) {
            bx = mouseX - boxW - 12;
         }

         if (bx < 2) {
            bx = 2;
         }

         if (by + boxH > screenH - 2) {
            by = screenH - boxH - 2;
         }

         if (by < 2) {
            by = 2;
         }

         g.flush();
         drawBackground(g, bx, by, boxW, boxH, mouseX, mouseY);
         g.fill(bx, by, bx + boxW, by + boxH, 1711276048);
         g.fill(bx, by, bx + boxW, by + 1, -14519384);
         g.fill(bx, by + boxH - 1, bx + boxW, by + boxH, -14519384);
         g.fill(bx, by, bx + 1, by + boxH, -14519384);
         g.fill(bx + boxW - 1, by, bx + boxW, by + boxH, -14519384);
         int l = 5;
         g.fill(bx - 1, by - 1, bx + l, by, -12597505);
         g.fill(bx - 1, by - 1, bx, by + l, -12597505);
         g.fill(bx + boxW - l, by - 1, bx + boxW + 1, by, -12597505);
         g.fill(bx + boxW - 1, by - 1, bx + boxW + 1, by + l, -12597505);
         g.fill(bx - 1, by + boxH, bx + l, by + boxH + 1, -12597505);
         g.fill(bx - 1, by + boxH - l, bx, by + boxH + 1, -12597505);
         g.fill(bx + boxW - l, by + boxH, bx + boxW + 1, by + boxH + 1, -12597505);
         g.fill(bx + boxW - 1, by + boxH - l, bx + boxW + 1, by + boxH + 1, -12597505);
         int ty = by + pad;

         for (Component c : lines) {
            g.drawString(font, c, bx + pad, ty, -1509633, false);
            ty += 10;
         }
      }
   }

   private static void drawBackground(GuiGraphics g, int bx, int by, int boxW, int boxH, int mouseX, int mouseY) {
      ShaderInstance shader = SystemBackgroundRenderTypes.get();
      if (shader == null) {
         g.fillGradient(bx, by, bx + boxW, by + boxH, -268038881, -268369399);
      } else {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShader(SystemBackgroundRenderTypes::get);
         AbstractUniform mouse = shader.safeGetUniform("MousePos");
         mouse.set(clamp01((float)(mouseX - bx) / boxW), clamp01((float)(mouseY - by) / boxH));
         shader.safeGetUniform("MouseGlitch").set(0.0F);
         Matrix4f matrix = g.pose().last().pose();
         BufferBuilder buffer = Tesselator.getInstance().getBuilder();
         buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         buffer.vertex(matrix, bx, by + boxH, 0.0F).uv(0.0F, 1.0F).endVertex();
         buffer.vertex(matrix, bx + boxW, by + boxH, 0.0F).uv(1.0F, 1.0F).endVertex();
         buffer.vertex(matrix, bx + boxW, by, 0.0F).uv(1.0F, 0.0F).endVertex();
         buffer.vertex(matrix, bx, by, 0.0F).uv(0.0F, 0.0F).endVertex();
         Tesselator.getInstance().end();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }
}
