package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DeferredWorldShaderRenderer;
import dev.eness.sololevelingfinal.core.client.renderer.shader.WhiteFlameVfxRenderTypes;
import dev.eness.sololevelingfinal.core.entity.WhiteFlameVfxEntity;

public class WhiteFlameVfxRenderer extends EntityRenderer<WhiteFlameVfxEntity> {
   private static final ResourceLocation FALLBACK = new ResourceLocation("sololeveling", "textures/particle/glow_yellow.png");

   public WhiteFlameVfxRenderer(Context context) {
      super(context);
   }

   public void render(WhiteFlameVfxEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
      float fade = entity.getFade(partialTick);
      if (!(fade <= 0.0F)) {
         VertexConsumer vertices = DeferredWorldShaderRenderer.buffer(buffers, WhiteFlameVfxRenderTypes.effect(FALLBACK));
         poseStack.pushPose();
         switch (entity.getStyle()) {
            case 0:
               this.renderBreath(entity, poseStack, vertices, fade);
               break;
            case 1:
               this.renderStrike(entity, poseStack, vertices, fade);
               break;
            case 2:
               this.renderVerdict(entity, poseStack, vertices, fade);
               break;
            case 3:
               this.renderDoppelgangers(entity, poseStack, vertices, fade);
               break;
            case 4:
               this.renderGate(entity, poseStack, vertices, fade);
               break;
            case 5:
               this.renderDodge(entity, poseStack, vertices, fade);
               break;
            default:
               this.renderImpact(entity, poseStack, vertices, fade);
         }

         poseStack.popPose();
         super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
      }
   }

   private void renderBreath(WhiteFlameVfxEntity entity, PoseStack stack, VertexConsumer vertices, float fade) {
      stack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
      stack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
      float width = entity.getScale();
      float length = entity.getLength();
      drawBeam(vertices, stack.last(), width, length, 0.0F, alpha(210.0F * fade), 0.0F);
      drawBeam(vertices, stack.last(), width * 0.72F, length, 90.0F, alpha(170.0F * fade), 0.0F);
   }

   private void renderStrike(WhiteFlameVfxEntity entity, PoseStack stack, VertexConsumer vertices, float fade) {
      stack.mulPose(Axis.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));
      float width = entity.getScale();
      drawVertical(vertices, stack.last(), width, entity.getLength(), alpha(220.0F * fade), 1.0F);
      stack.mulPose(Axis.YP.rotationDegrees(90.0F));
      drawVertical(vertices, stack.last(), width * 0.72F, entity.getLength(), alpha(150.0F * fade), 1.0F);
   }

   private void renderVerdict(WhiteFlameVfxEntity entity, PoseStack stack, VertexConsumer vertices, float fade) {
      float radius = entity.getScale() * (0.85F + entity.tickCount * 0.035F);
      drawHorizontal(vertices, stack.last(), radius, alpha(205.0F * fade), 2.0F);
      stack.translate(0.0, entity.getLength() * 0.45, 0.0);
      stack.mulPose(Axis.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));
      drawVertical(vertices, stack.last(), radius * 0.65F, entity.getLength(), alpha(150.0F * fade), 2.0F);
   }

   private void renderDoppelgangers(WhiteFlameVfxEntity entity, PoseStack stack, VertexConsumer vertices, float fade) {
      stack.mulPose(Axis.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));

      for (int i = -1; i <= 1; i++) {
         stack.pushPose();
         stack.translate(i * entity.getScale() * 1.35F, 0.0, Math.abs(i) * -0.2);
         drawVertical(vertices, stack.last(), entity.getScale() * 0.42F, entity.getLength(), alpha((i == 0 ? 95 : 175) * fade), 3.0F);
         stack.popPose();
      }
   }

   private void renderGate(WhiteFlameVfxEntity entity, PoseStack stack, VertexConsumer vertices, float fade) {
      stack.mulPose(Axis.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));
      drawVertical(vertices, stack.last(), entity.getScale(), entity.getLength(), alpha(200.0F * fade), 4.0F);
   }

   private void renderDodge(WhiteFlameVfxEntity entity, PoseStack stack, VertexConsumer vertices, float fade) {
      stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      float size = entity.getScale() * (1.0F + entity.tickCount * 0.12F);
      drawBillboard(vertices, stack.last(), size, size, alpha(190.0F * fade), 5.0F);
   }

   private void renderImpact(WhiteFlameVfxEntity entity, PoseStack stack, VertexConsumer vertices, float fade) {
      float radius = entity.getScale() * (1.0F + entity.tickCount * 0.09F);
      drawHorizontal(vertices, stack.last(), radius, alpha(220.0F * fade), 6.0F);
   }

   private static void drawBeam(VertexConsumer out, Pose pose, float width, float length, float roll, int alpha, float style) {
      float sin = (float)Math.sin(Math.toRadians(roll));
      float cos = (float)Math.cos(Math.toRadians(roll));
      beamVertex(out, pose, -width, 0.0F, 0.0F, style, 1.0F, alpha, sin, cos);
      beamVertex(out, pose, width, 0.0F, 0.0F, style + 1.0F, 1.0F, alpha, sin, cos);
      beamVertex(out, pose, width * 0.2F, 0.0F, length, style + 1.0F, 0.0F, alpha, sin, cos);
      beamVertex(out, pose, -width * 0.2F, 0.0F, length, style, 0.0F, alpha, sin, cos);
   }

   private static void beamVertex(VertexConsumer out, Pose pose, float x, float y, float z, float u, float v, int alpha, float sin, float cos) {
      vertex(out, pose, x * cos - y * sin, x * sin + y * cos, z, u, v, alpha);
   }

   private static void drawVertical(VertexConsumer out, Pose pose, float halfWidth, float height, int alpha, float style) {
      vertex(out, pose, -halfWidth, 0.0F, 0.0F, style, 1.0F, alpha);
      vertex(out, pose, halfWidth, 0.0F, 0.0F, style + 1.0F, 1.0F, alpha);
      vertex(out, pose, halfWidth * 0.38F, height, 0.0F, style + 1.0F, 0.0F, alpha);
      vertex(out, pose, -halfWidth * 0.38F, height, 0.0F, style, 0.0F, alpha);
   }

   private static void drawHorizontal(VertexConsumer out, Pose pose, float radius, int alpha, float style) {
      vertex(out, pose, -radius, 0.03F, -radius, style, 1.0F, alpha);
      vertex(out, pose, -radius, 0.03F, radius, style + 1.0F, 1.0F, alpha);
      vertex(out, pose, radius, 0.03F, radius, style + 1.0F, 0.0F, alpha);
      vertex(out, pose, radius, 0.03F, -radius, style, 0.0F, alpha);
   }

   private static void drawBillboard(VertexConsumer out, Pose pose, float width, float height, int alpha, float style) {
      vertex(out, pose, -width, -height, 0.0F, style, 1.0F, alpha);
      vertex(out, pose, width, -height, 0.0F, style + 1.0F, 1.0F, alpha);
      vertex(out, pose, width, height, 0.0F, style + 1.0F, 0.0F, alpha);
      vertex(out, pose, -width, height, 0.0F, style, 0.0F, alpha);
   }

   private static void vertex(VertexConsumer out, Pose pose, float x, float y, float z, float u, float v, int alpha) {
      out.vertex(pose.pose(), x, y, z)
         .color(255, 255, 255, alpha)
         .uv(u, v)
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(240)
         .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
         .endVertex();
   }

   private static int alpha(float value) {
      return Math.max(0, Math.min(255, Math.round(value)));
   }

   public ResourceLocation getTextureLocation(WhiteFlameVfxEntity entity) {
      return FALLBACK;
   }
}
