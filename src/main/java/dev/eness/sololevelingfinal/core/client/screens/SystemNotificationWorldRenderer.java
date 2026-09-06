package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemNotificationManager;
import dev.eness.sololevelingfinal.core.client.renderer.shader.IrisCompat;
import dev.eness.sololevelingfinal.core.client.renderer.shader.SystemBackgroundRenderTypes;
import dev.eness.sololevelingfinal.core.util.SystemClientConfig;
import org.joml.Matrix4f;

@EventBusSubscriber(Dist.CLIENT)
public class SystemNotificationWorldRenderer {
   private static final int ACCENT = -12597505;
   private static final int LIGHT = 15728880;
   private static final float DIST = 1.4F;
   private static final float LEFT_AMT = 0.95F;
   private static final float UP_AMT = 0.02F;
   private static final float STACK_GAP_WORLD = 0.08F;
   private static final float PANEL_TILT = 16.0F;
   private static final float WORLD_SCALE = 0.008F;
   private static final float TITLE_SCALE = 1.7F;
   private static final int PAD_X = 9;
   private static final int PAD_Y = 6;
   private static final int LINE_GAP = 3;
   private static final int FH = 9;
   private static final int UNDER_MAX_WIDTH = 150;

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_ENTITIES) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && !mc.options.hideGui) {
            List<SystemNotificationManager.Notification> list = SystemNotificationManager.INSTANCE.active();
            if (!list.isEmpty()) {
               Font font = mc.font;
               Camera cam = event.getCamera();
               long now = System.currentTimeMillis();
               PoseStack ps = event.getPoseStack();
               BufferSource buffer = mc.renderBuffers().bufferSource();
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableCull();
               RenderSystem.disableDepthTest();
               float notificationScale = SystemClientConfig.getNotificationScale();
               float s = 0.008F * notificationScale;
               float stackCenter = 0.0F;
               float previousHalfHeight = 0.0F;
               float stackGap = 0.08F * notificationScale;

               for (int idx = 0; idx < list.size(); idx++) {
                  SystemNotificationManager.Notification n = list.get(list.size() - 1 - idx);
                  int[] size = measure(font, n);
                  float currentHalfHeight = size[1] * s * 0.5F;
                  if (idx > 0) {
                     stackCenter += previousHalfHeight + stackGap + currentHalfHeight;
                  }

                  float horizontal;
                  if (SystemClientConfig.isDynamicNotificationsEnabled()) {
                     float panelHalfWidth = size[0] * s * 0.5F;
                     horizontal = Math.max(0.48F, panelHalfWidth + 0.2F);
                  } else {
                     horizontal = 0.95F + SystemClientConfig.getNotificationHorizontalOffset();
                  }

                  Vec3 offset = cameraScreenOffset(cam, stackCenter, horizontal);
                  ps.pushPose();
                  ps.translate(offset.x, offset.y, offset.z);
                  ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
                  ps.mulPose(Axis.YP.rotationDegrees(16.0F));
                  ps.scale(-s, -s, s);
                  renderPanel(ps, font, buffer, n, now, size);
                  ps.popPose();
                  previousHalfHeight = currentHalfHeight;
               }

               buffer.endBatch();
               RenderSystem.enableDepthTest();
               RenderSystem.enableCull();
               RenderSystem.disableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }
   }

   private static Vec3 cameraScreenOffset(Camera cam, float stackOffset, float horizontal) {
      Vec3 forward = Vec3.directionFromRotation(cam.getXRot(), cam.getYRot()).normalize();
      Vec3 left = new Vec3(cam.getLeftVector()).normalize();
      Vec3 up = new Vec3(cam.getUpVector()).normalize();
      return forward.scale(1.4F).add(left.scale(horizontal)).add(up.scale(0.02F + stackOffset));
   }

   private static void renderPanel(PoseStack ps, Font font, MultiBufferSource buffer, SystemNotificationManager.Notification n, long now, int[] size) {
      float reveal = n.reveal(now);
      if (!(reveal <= 0.02F)) {
         boolean trans = n.transitioning(now);
         float jitter = trans ? (float)((Math.random() - 0.5) * 6.0 * (1.0F - reveal)) : 0.0F;
         ps.pushPose();
         ps.translate(jitter, 0.0F, 0.0F);
         ps.scale(reveal, 1.0F, 1.0F);
         int tabW = size[0];
         int tabH = size[1];
         int hw = tabW / 2;
         int hh = tabH / 2;
         Matrix4f m = ps.last().pose();
         drawRect(m, -hw - 2, -hh - 2, hw + 2, hh + 2, -12597505);
         drawShaderRect(m, hw, hh);
         if (trans && reveal > 0.06F) {
            for (int s = 0; s < 2; s++) {
               float sy = (float)((Math.random() - 0.5) * tabH);
               drawRect(m, -hw, sy, hw, sy + 1.0F, -1715475969);
            }
         }

         boolean hasT = n.title != null;
         boolean hasU = n.under != null;
         if (hasT && hasU) {
            int titleH = Math.round(15.3F);
            int underH = size[2];
            float titleCY = -hh + 6 + titleH / 2.0F;
            float underCY = hh - 6 - underH / 2.0F;
            drawTitle(ps, font, buffer, n.title, titleCY);
            drawUnder(ps, font, buffer, n.under, underCY);
         } else if (hasT) {
            drawTitle(ps, font, buffer, n.title, 0.0F);
         } else {
            drawUnder(ps, font, buffer, n.under, 0.0F);
         }

         ps.popPose();
      }
   }

   private static void drawTitle(PoseStack ps, Font font, MultiBufferSource buffer, Component title, float cy) {
      ps.pushPose();
      ps.translate(0.0F, cy, 0.1F);
      ps.scale(1.7F, 1.7F, 1.0F);
      int tw = font.width(title);
      font.drawInBatch(title, -tw / 2.0F, -4.0F, -1, false, ps.last().pose(), buffer, DisplayMode.SEE_THROUGH, 0, 15728880);
      ps.popPose();
   }

   private static void drawUnder(PoseStack ps, Font font, MultiBufferSource buffer, Component under, float cy) {
      List<FormattedCharSequence> lines = underLines(font, under);
      if (!lines.isEmpty()) {
         ps.pushPose();
         ps.translate(0.0F, cy, 0.1F);
         float totalH = underHeight(lines);
         float y = -totalH / 2.0F;

         for (FormattedCharSequence line : lines) {
            int uw = font.width(line);
            font.drawInBatch(line, -uw / 2.0F, y, -1509633, false, ps.last().pose(), buffer, DisplayMode.SEE_THROUGH, 0, 15728880);
            y += 12.0F;
         }

         ps.popPose();
      }
   }

   private static int[] measure(Font font, SystemNotificationManager.Notification n) {
      boolean hasT = n.title != null;
      boolean hasU = n.under != null;
      int titleW = hasT ? Math.round(font.width(n.title) * 1.7F) : 0;
      int titleH = hasT ? Math.round(15.3F) : 0;
      List<FormattedCharSequence> underLines = hasU ? underLines(font, n.under) : List.of();
      int underW = 0;

      for (FormattedCharSequence line : underLines) {
         underW = Math.max(underW, font.width(line));
      }

      int underH = hasU ? underHeight(underLines) : 0;
      int innerW = Math.max(titleW, underW);
      int innerH = titleH + underH + (hasT && hasU ? 3 : 0);
      int tabW = Math.max(52, innerW + 18);
      int tabH = innerH + 12;
      return new int[]{tabW, tabH, underH};
   }

   private static List<FormattedCharSequence> underLines(Font font, Component under) {
      List<FormattedCharSequence> lines = new ArrayList<>();
      if (under == null) {
         return lines;
      }

      List<MutableComponent> paragraphs = new ArrayList<>();
      paragraphs.add(Component.empty());
      under.visit((style, text) -> {
         String[] parts = text.replace("\\n", "\n").split("\n", -1);

         for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
               paragraphs.get(paragraphs.size() - 1).append(Component.literal(parts[i]).withStyle(style));
            }

            if (i < parts.length - 1) {
               paragraphs.add(Component.empty());
            }
         }

         return Optional.empty();
      }, Style.EMPTY);

      for (MutableComponent paragraph : paragraphs) {
         Component line = paragraph.getString().isEmpty() ? Component.literal(" ") : paragraph;
         List<FormattedCharSequence> split = font.split(line, 150);
         lines.addAll(split);
      }

      return lines;
   }

   private static int underHeight(List<FormattedCharSequence> lines) {
      return lines.isEmpty() ? 0 : lines.size() * 9 + (lines.size() - 1) * 3;
   }

   private static void drawShaderRect(Matrix4f m, float hw, float hh) {
      ShaderInstance shader = IrisCompat.isShaderPackInUse() ? null : SystemBackgroundRenderTypes.get();
      if (shader == null) {
         drawRect(m, -hw, -hh, hw, hh, -267773904);
      } else {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShader(SystemBackgroundRenderTypes::get);
         shader.safeGetUniform("MousePos").set(0.5F, 0.35F);
         shader.safeGetUniform("MouseGlitch").set(0.0F);
         BufferBuilder buf = Tesselator.getInstance().getBuilder();
         buf.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         buf.vertex(m, -hw, hh, 0.0F).uv(0.0F, 1.0F).endVertex();
         buf.vertex(m, hw, hh, 0.0F).uv(1.0F, 1.0F).endVertex();
         buf.vertex(m, hw, -hh, 0.0F).uv(1.0F, 0.0F).endVertex();
         buf.vertex(m, -hw, -hh, 0.0F).uv(0.0F, 0.0F).endVertex();
         Tesselator.getInstance().end();
      }
   }

   private static void drawRect(Matrix4f m, float x0, float y0, float x1, float y1, int argb) {
      float a = (argb >>> 24 & 0xFF) / 255.0F;
      float r = (argb >> 16 & 0xFF) / 255.0F;
      float g = (argb >> 8 & 0xFF) / 255.0F;
      float b = (argb & 0xFF) / 255.0F;
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      BufferBuilder buf = Tesselator.getInstance().getBuilder();
      buf.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      buf.vertex(m, x0, y1, 0.0F).color(r, g, b, a).endVertex();
      buf.vertex(m, x1, y1, 0.0F).color(r, g, b, a).endVertex();
      buf.vertex(m, x1, y0, 0.0F).color(r, g, b, a).endVertex();
      buf.vertex(m, x0, y0, 0.0F).color(r, g, b, a).endVertex();
      Tesselator.getInstance().end();
   }
}
