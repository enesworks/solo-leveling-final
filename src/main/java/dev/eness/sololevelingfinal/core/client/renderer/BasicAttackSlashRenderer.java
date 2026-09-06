package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.renderer.shader.BasicAttackSlashRenderTypes;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DeferredWorldShaderRenderer;
import dev.eness.sololevelingfinal.core.entity.BasicAttackSlashEntity;

public class BasicAttackSlashRenderer extends EntityRenderer<BasicAttackSlashEntity> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("sololeveling:textures/particle/slashgood1.png");

   public BasicAttackSlashRenderer(Context context) {
      super(context);
   }

   public void render(BasicAttackSlashEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      float fade = entity.getFade(partialTick);
      if (!(fade <= 0.0F)) {
         int style = entity.getStyle();
         float agePush = 1.0F + (1.0F - fade) * 0.18F;
         float scale = entity.getScale() * agePush;
         float halfWidth = widthFor(style) * scale;
         float halfHeight = heightFor(style) * scale;
         int alpha = Math.round(alphaFor(style) * fade);
         poseStack.pushPose();
         poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
         poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getRoll()));
         RenderType renderType = BasicAttackSlashRenderTypes.slash(style, TEXTURE);
         VertexConsumer vertexConsumer = DeferredWorldShaderRenderer.buffer(bufferSource, renderType, false);
         Pose pose = poseStack.last();
         if (style == 3) {
            drawQuad(vertexConsumer, pose, halfWidth, halfHeight, -26.0F, alpha);
            drawQuad(vertexConsumer, pose, halfWidth * 0.96F, halfHeight, 26.0F, alpha);
         } else {
            drawQuad(vertexConsumer, pose, halfWidth, halfHeight, 0.0F, alpha);
         }

         poseStack.popPose();
         super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      }
   }

   private static float widthFor(int style) {
      return switch (style) {
         case 0 -> 0.82F;
         default -> 2.65F;
         case 2 -> 1.75F;
         case 3 -> 1.95F;
      };
   }

   private static float heightFor(int style) {
      return switch (style) {
         case 0 -> 0.82F;
         default -> 0.42F;
         case 2 -> 0.28F;
         case 3 -> 0.26F;
      };
   }

   private static int alphaFor(int style) {
      return style == 0 ? 210 : 235;
   }

   private static void drawQuad(VertexConsumer vertexConsumer, Pose pose, float halfWidth, float halfHeight, float roll, int alpha) {
      float sin = (float)Math.sin(Math.toRadians(roll));
      float cos = (float)Math.cos(Math.toRadians(roll));
      vertex(vertexConsumer, pose, -halfWidth, -halfHeight, 0.0F, 0.0F, 1.0F, alpha, sin, cos);
      vertex(vertexConsumer, pose, halfWidth, -halfHeight, 0.0F, 1.0F, 1.0F, alpha, sin, cos);
      vertex(vertexConsumer, pose, halfWidth, halfHeight, 0.0F, 1.0F, 0.0F, alpha, sin, cos);
      vertex(vertexConsumer, pose, -halfWidth, halfHeight, 0.0F, 0.0F, 0.0F, alpha, sin, cos);
   }

   private static void vertex(VertexConsumer vertexConsumer, Pose pose, float x, float y, float z, float u, float v, int alpha, float sin, float cos) {
      float rx = x * cos - y * sin;
      float ry = x * sin + y * cos;
      vertexConsumer.vertex(pose.pose(), rx, ry, z)
         .color(255, 255, 255, alpha)
         .uv(u, v)
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(240)
         .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
         .endVertex();
   }

   public ResourceLocation getTextureLocation(BasicAttackSlashEntity entity) {
      return TEXTURE;
   }
}
