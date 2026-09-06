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
import net.minecraftforge.client.event.RenderGuiEvent.Post;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.client.gui.FrostArchitectureClientState;
import dev.eness.sololevelingfinal.core.util.FrostArchitectureBlueprint;
import org.joml.Matrix4f;

@EventBusSubscriber(Dist.CLIENT)
public final class FrostArchitectureRadialOverlay {
   private static final float INNER_RADIUS = 31.0F;
   private static final float OUTER_RADIUS = 94.0F;

   private FrostArchitectureRadialOverlay() {
   }

   @SubscribeEvent
   public static void onRenderGui(Post event) {
      if (FrostArchitectureClientState.isActive()) {
         FrostArchitectureClientState.updateMouseFromFrame();
         if (FrostArchitectureClientState.isActive()) {
            Minecraft minecraft = Minecraft.getInstance();
            GuiGraphics graphics = event.getGuiGraphics();
            int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
            int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;
            FrostArchitectureBlueprint selected = FrostArchitectureClientState.selectedBlueprint();
            FrostArchitectureBlueprint[] options = FrostArchitectureBlueprint.values();
            double rotation = FrostArchitectureClientState.rotation();
            double step = (Math.PI * 2) / options.length;
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(0.0, 0.0, 400.0);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            for (int i = 0; i < options.length; i++) {
               double middle = (-Math.PI / 2) + i * step + rotation;
               boolean top = options[i] == selected;
               float start = (float)(middle - step * 0.475);
               float end = (float)(middle + step * 0.475);
               if (top) {
                  renderSector(pose, centerX, centerY, start, end, 0.24F, 0.88F, 1.0F, 0.9F);
               } else {
                  renderSector(pose, centerX, centerY, start, end, 0.035F, 0.11F, 0.17F, 0.86F);
               }

               renderArc(pose, centerX, centerY, 92.5F, 94.0F, start, end, top ? 0.75F : 0.2F, top ? 0.98F : 0.55F, 1.0F, top ? 0.95F : 0.65F);
               renderArc(pose, centerX, centerY, 31.0F, 32.5F, start, end, top ? 0.75F : 0.2F, top ? 0.98F : 0.55F, 1.0F, top ? 0.95F : 0.65F);
               float labelRadius = 62.5F;
               int labelX = Math.round(centerX + (float)Math.cos(middle) * labelRadius);
               int labelY = Math.round(centerY + (float)Math.sin(middle) * labelRadius);
               Component label = Component.translatable(options[i].translationKey());
               int color = top ? -1 : -6370858;
               graphics.drawCenteredString(minecraft.font, label, labelX, labelY - 4, color);
            }

            renderCenter(pose, centerX, centerY);
            drawTopPointer(pose, centerX, centerY);
            Component selectedName = Component.translatable(selected.translationKey());
            graphics.drawCenteredString(minecraft.font, selectedName, centerX, centerY - 5, -1);
            graphics.drawCenteredString(
               minecraft.font, Component.translatable("gui.sololeveling.frost_architecture.rotate"), centerX, centerY + Math.round(94.0F) + 16, -4591617
            );
            graphics.drawCenteredString(
               minecraft.font, Component.translatable("gui.sololeveling.frost_architecture.release"), centerX, centerY + Math.round(94.0F) + 29, -1
            );
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            pose.popPose();
         }
      }
   }

   private static void renderSector(PoseStack pose, float centerX, float centerY, float start, float end, float red, float green, float blue, float alpha) {
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f matrix = pose.last().pose();
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
      int segments = 18;

      for (int i = 0; i <= segments; i++) {
         float angle = start + (end - start) * i / segments;
         float cosine = (float)Math.cos(angle);
         float sine = (float)Math.sin(angle);
         buffer.vertex(matrix, centerX + cosine * 94.0F, centerY + sine * 94.0F, 0.0F).color(red, green, blue, alpha).endVertex();
         buffer.vertex(matrix, centerX + cosine * 31.0F, centerY + sine * 31.0F, 0.0F).color(red * 0.42F, green * 0.52F, blue * 0.6F, alpha).endVertex();
      }

      BufferUploader.drawWithShader(buffer.end());
   }

   private static void renderArc(
      PoseStack pose, float centerX, float centerY, float inner, float outer, float start, float end, float red, float green, float blue, float alpha
   ) {
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f matrix = pose.last().pose();
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
      int segments = 18;

      for (int i = 0; i <= segments; i++) {
         float angle = start + (end - start) * i / segments;
         float cosine = (float)Math.cos(angle);
         float sine = (float)Math.sin(angle);
         buffer.vertex(matrix, centerX + cosine * outer, centerY + sine * outer, 0.3F).color(red, green, blue, alpha).endVertex();
         buffer.vertex(matrix, centerX + cosine * inner, centerY + sine * inner, 0.3F).color(red, green, blue, alpha).endVertex();
      }

      BufferUploader.drawWithShader(buffer.end());
   }

   private static void renderCenter(PoseStack pose, float centerX, float centerY) {
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f matrix = pose.last().pose();
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
      buffer.vertex(matrix, centerX, centerY, 0.6F).color(0.03F, 0.12F, 0.18F, 0.98F).endVertex();

      for (int i = 0; i <= 48; i++) {
         float angle = (float)((Math.PI * 2) * i / 48.0);
         buffer.vertex(matrix, centerX + (float)Math.cos(angle) * 29.0F, centerY + (float)Math.sin(angle) * 29.0F, 0.6F)
            .color(0.12F, 0.42F, 0.55F, 0.98F)
            .endVertex();
      }

      BufferUploader.drawWithShader(buffer.end());
   }

   private static void drawTopPointer(PoseStack pose, float centerX, float centerY) {
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f matrix = pose.last().pose();
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
      float y = centerY - 94.0F - 5.0F;
      buffer.vertex(matrix, centerX, y + 10.0F, 1.0F).color(0.85F, 1.0F, 1.0F, 1.0F).endVertex();
      buffer.vertex(matrix, centerX - 7.0F, y, 1.0F).color(0.2F, 0.85F, 1.0F, 1.0F).endVertex();
      buffer.vertex(matrix, centerX + 7.0F, y, 1.0F).color(0.2F, 0.85F, 1.0F, 1.0F).endVertex();
      BufferUploader.drawWithShader(buffer.end());
   }
}
