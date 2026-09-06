package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent.Post;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.MageQTEState;
import dev.eness.sololevelingfinal.core.util.QTEResult;
import org.joml.Matrix4f;

@EventBusSubscriber(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class MageQTEOverlay {
   private static final float RADIUS = 60.0F;
   private static final float RING_THICKNESS = 5.0F;

   @SubscribeEvent
   public static void onRenderGui(Post event) {
      MageQTEState state = MageQTEState.INSTANCE;
      if (state.isActive() && state.hasTimedOut()) {
         state.cancelQTE();
         state.showResult(QTEResult.MISS);
      }

      boolean showRing = state.isActive();
      boolean showResult = state.isShowingResult();
      if (showRing || showResult) {
         Minecraft mc = Minecraft.getInstance();
         GuiGraphics gg = event.getGuiGraphics();
         int w = mc.getWindow().getGuiScaledWidth();
         int h = mc.getWindow().getGuiScaledHeight();
         float cx = w / 2.0F;
         float cy = h / 2.0F;
         PoseStack ps = gg.pose();
         ps.pushPose();
         ps.translate(0.0F, 0.0F, 300.0F);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         if (showRing) {
            renderFilledCircleGradient(ps, cx, cy, 55.0F, 0.18F, 0.18F, 0.22F, 0.72F, 0.1F, 0.1F, 0.12F, 0.5F);
            renderRing(ps, cx, cy, 60.0F, 11.0F, 0.08F, 0.08F, 0.08F, 1.0F);
            renderRing(ps, cx, cy, 60.0F, 5.0F, 0.58F, 0.58F, 0.63F, 0.95F);
            float goodStart = state.getGoodZoneStart();
            float goodEnd = state.getGoodZoneEnd();
            renderArc(ps, cx, cy, 60.0F, goodStart, goodEnd, 13.0F, 0.0F, 0.0F, 0.0F, 0.95F);
            renderArc(ps, cx, cy, 60.0F, goodStart, goodEnd, 11.0F, 1.0F, 0.75F, 0.08F, 0.97F);
            float perfectStart = state.getPerfectZoneStart();
            float perfectEnd = state.getPerfectZoneEnd();
            renderArc(ps, cx, cy, 60.0F, perfectStart, perfectEnd, 15.0F, 0.0F, 0.0F, 0.0F, 0.95F);
            renderArc(ps, cx, cy, 60.0F, perfectStart, perfectEnd, 13.0F, 0.2F, 0.85F, 1.0F, 0.97F);
            float rot = state.getCurrentRotation();
            renderPerimeterTick(ps, cx, cy, 60.0F, rot, 18.0F, 3.8F, 7.0F, 0.0F, 0.0F, 0.0F, 1.0F);
            renderPerimeterTick(ps, cx, cy, 60.0F, rot, 18.0F, 2.4F, 7.0F, 0.1F, 0.4F, 1.0F, 1.0F);
            String skillName = "";
            if (mc.player != null) {
               SololevelingModVariables.PlayerVariables cap = mc.player
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables());
               skillName = cap.PselectedPower;
            }

            if (!skillName.isEmpty()) {
               int nameW = mc.font.width(skillName);
               gg.drawString(mc.font, Component.literal("§f" + skillName), (int)(cx - nameW / 2.0F), (int)(cy - 5.0F), -1, true);
            }

            String hint = "Release in the §egold§r zone!";
            int hintW = mc.font.width(hint);
            gg.drawString(mc.font, Component.literal(hint), (int)(cx - hintW / 2.0F), (int)(cy + 60.0F + 10.0F), -5592406, false);
            renderTimingBar(gg, mc, state, cx, cy + 60.0F + 28.0F);
         }

         if (showResult) {
            QTEResult result = state.getLastResult();
            float alpha = state.getResultAlpha();
            int alphaInt = (int)(alpha * 255.0F) << 24;

            String label = switch (result) {
               case PERFECT -> "§bPERFECT!";
               case GOOD -> "§eGOOD!";
               case MISS -> "§cMISS!";
            };
            int labelW = mc.font.width(label);
            gg.drawString(mc.font, Component.literal(label), (int)(cx - labelW / 2.0F), (int)(cy - 60.0F - 24.0F), 16777215 | alphaInt, true);
         }

         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         ps.popPose();
      }
   }

   private static void renderTimingBar(GuiGraphics gg, Minecraft mc, MageQTEState state, float cx, float y) {
      int width = 164;
      int height = 9;
      int x = (int)(cx - width / 2.0F);
      int top = (int)y;
      gg.fill(x - 2, top - 2, x + width + 2, top + height + 2, -805306368);
      gg.fill(x, top, x + width, top + height, -586148574);
      drawZoneOnBar(gg, x, top, width, height, state.getGoodZoneStart(), state.getGoodZoneEnd(), -14006);
      drawZoneOnBar(gg, x, top - 1, width, height + 2, state.getPerfectZoneStart(), state.getPerfectZoneEnd(), -12130305);
      int marker = x + Math.round(state.getCurrentRotation() / 360.0F * width);
      gg.fill(marker - 1, top - 5, marker + 2, top + height + 5, -1);
      gg.fill(marker, top - 4, marker + 1, top + height + 4, -9131777);
      gg.drawString(mc.font, Component.literal("RELEASE"), x + width + 8, top, -1509377, true);
   }

   private static void drawZoneOnBar(GuiGraphics gg, int x, int y, int width, int height, float startDeg, float endDeg, int color) {
      if (endDeg < startDeg) {
         drawZoneOnBar(gg, x, y, width, height, startDeg, 360.0F, color);
         drawZoneOnBar(gg, x, y, width, height, 0.0F, endDeg, color);
      } else {
         int start = x + Math.round(startDeg / 360.0F * width);
         int end = x + Math.round(endDeg / 360.0F * width);
         gg.fill(start, y, Math.max(start + 1, end), y + height, color);
      }
   }

   private static void renderFilledCircleGradient(
      PoseStack ps, float cx, float cy, float radius, float cr, float cg, float cb, float ca, float er, float eg, float eb, float ea
   ) {
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f m = ps.last().pose();
      BufferBuilder buf = Tesselator.getInstance().getBuilder();
      buf.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
      buf.vertex(m, cx, cy, 0.0F).color(cr, cg, cb, ca).endVertex();
      int seg = 72;

      for (int i = 0; i <= seg; i++) {
         float ang = (float)((Math.PI * 2) * i / seg);
         buf.vertex(m, cx + (float)Math.cos(ang) * radius, cy + (float)Math.sin(ang) * radius, 0.0F).color(er, eg, eb, ea).endVertex();
      }

      BufferUploader.drawWithShader(buf.end());
   }

   private static void renderRing(PoseStack ps, float cx, float cy, float radius, float thickness, float r, float g, float b, float a) {
      float outerR = radius + thickness * 0.5F;
      float innerR = radius - thickness * 0.5F;
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f m = ps.last().pose();
      BufferBuilder buf = Tesselator.getInstance().getBuilder();
      buf.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
      int seg = 96;

      for (int i = 0; i <= seg; i++) {
         float ang = (float)((Math.PI * 2) * i / seg);
         float cos = (float)Math.cos(ang);
         float sin = (float)Math.sin(ang);
         buf.vertex(m, cx + cos * outerR, cy + sin * outerR, 0.0F).color(r, g, b, a).endVertex();
         buf.vertex(m, cx + cos * innerR, cy + sin * innerR, 0.0F).color(r, g, b, a).endVertex();
      }

      BufferUploader.drawWithShader(buf.end());
   }

   private static void renderArc(
      PoseStack ps, float cx, float cy, float radius, float startDeg, float endDeg, float thickness, float r, float g, float b, float a
   ) {
      float outerR = radius + thickness * 0.5F;
      float innerR = radius - thickness * 0.5F;
      float startRad = (float)Math.toRadians(startDeg - 90.0F);
      float endRad = (float)Math.toRadians(endDeg - 90.0F);
      if (endRad < startRad) {
         endRad = (float)(endRad + (Math.PI * 2));
      }

      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f m = ps.last().pose();
      BufferBuilder buf = Tesselator.getInstance().getBuilder();
      buf.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
      int seg = 72;

      for (int i = 0; i <= seg; i++) {
         float ang = startRad + (endRad - startRad) * ((float)i / seg);
         float cos = (float)Math.cos(ang);
         float sin = (float)Math.sin(ang);
         buf.vertex(m, cx + cos * outerR, cy + sin * outerR, 0.0F).color(r, g, b, a).endVertex();
         buf.vertex(m, cx + cos * innerR, cy + sin * innerR, 0.0F).color(r, g, b, a).endVertex();
      }

      BufferUploader.drawWithShader(buf.end());
   }

   private static void renderPerimeterTick(
      PoseStack ps, float cx, float cy, float radius, float angleDeg, float tickLen, float halfWidth, float outwardOffset, float r, float g, float b, float a
   ) {
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      float ang = (float)Math.toRadians(angleDeg - 90.0F);
      float cos = (float)Math.cos(ang);
      float sin = (float)Math.sin(ang);
      float ox = cx + cos * (radius + outwardOffset);
      float oy = cy + sin * (radius + outwardOffset);
      float ix = cx + cos * (radius + outwardOffset - tickLen);
      float iy = cy + sin * (radius + outwardOffset - tickLen);
      float px = -sin * halfWidth;
      float py = cos * halfWidth;
      Matrix4f m = ps.last().pose();
      BufferBuilder buf = Tesselator.getInstance().getBuilder();
      buf.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      buf.vertex(m, ix - px, iy - py, 0.0F).color(r, g, b, a).endVertex();
      buf.vertex(m, ix + px, iy + py, 0.0F).color(r, g, b, a).endVertex();
      buf.vertex(m, ox + px, oy + py, 0.0F).color(r, g, b, a).endVertex();
      buf.vertex(m, ox - px, oy - py, 0.0F).color(r, g, b, a).endVertex();
      BufferUploader.drawWithShader(buf.end());
   }
}
