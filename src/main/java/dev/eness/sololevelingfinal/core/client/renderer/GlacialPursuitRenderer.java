package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DeferredWorldShaderRenderer;
import dev.eness.sololevelingfinal.core.client.renderer.shader.GlacialPursuitRenderTypes;
import dev.eness.sololevelingfinal.core.entity.GlacialPursuitEntity;

public class GlacialPursuitRenderer extends EntityRenderer<GlacialPursuitEntity> {
   private static final ResourceLocation FALLBACK = new ResourceLocation("sololeveling", "textures/particle/mana_blue.png");

   public GlacialPursuitRenderer(Context context) {
      super(context);
   }

   public void render(GlacialPursuitEntity entity, float entityYaw, float partialTick, PoseStack stack, MultiBufferSource buffers, int packedLight) {
      boolean firstPersonRider = entity.hasPassenger(Minecraft.getInstance().player) && Minecraft.getInstance().options.getCameraType().isFirstPerson();
      float pulse = 0.94F + 0.06F * (float)Math.sin((entity.tickCount + partialTick) * 0.62F);
      float manifested = entity.isManifested() ? 1.22F : 1.0F;
      VertexConsumer out = DeferredWorldShaderRenderer.buffer(buffers, GlacialPursuitRenderTypes.effect(FALLBACK));
      stack.pushPose();
      stack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
      stack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
      float wakeStart = firstPersonRider ? -1.35F : -0.12F;
      drawWake(out, stack.last(), 0.62F * manifested, 5.4F * manifested, wakeStart, entity.isRiderMode() ? 128 : 158);
      if (!firstPersonRider) {
         for (int i = 0; i < 3; i++) {
            stack.pushPose();
            stack.mulPose(Axis.ZP.rotationDegrees(i * 60.0F + entity.tickCount * 4.2F));
            drawDiamond(out, stack.last(), 0.72F * pulse * manifested, 0.94F * pulse * manifested, 0.0F, 232 - i * 35);
            stack.popPose();
         }
      }

      if (entity.isRiderMode()) {
         stack.pushPose();
         stack.translate(0.0, -0.2, -0.28);
         drawCrest(out, stack.last(), 1.18F * manifested, 2.8F * manifested, firstPersonRider ? 74 : 142);
         stack.popPose();
      }

      stack.popPose();
      super.render(entity, entityYaw, partialTick, stack, buffers, packedLight);
   }

   private static void drawWake(VertexConsumer out, Pose pose, float halfWidth, float length, float front, int alpha) {
      float back = front - length;
      vertex(out, pose, -halfWidth, 0.0F, front, 1.04F, 0.03F, alpha);
      vertex(out, pose, halfWidth, 0.0F, front, 1.96F, 0.03F, alpha);
      vertex(out, pose, halfWidth * 0.05F, 0.0F, back, 1.96F, 0.97F, 0);
      vertex(out, pose, -halfWidth * 0.05F, 0.0F, back, 1.04F, 0.97F, 0);
      vertex(out, pose, 0.0F, -halfWidth, front, 1.04F, 0.03F, alpha);
      vertex(out, pose, 0.0F, halfWidth, front, 1.96F, 0.03F, alpha);
      vertex(out, pose, 0.0F, halfWidth * 0.05F, back, 1.96F, 0.97F, 0);
      vertex(out, pose, 0.0F, -halfWidth * 0.05F, back, 1.04F, 0.97F, 0);
   }

   private static void drawDiamond(VertexConsumer out, Pose pose, float width, float height, float z, int alpha) {
      vertex(out, pose, 0.0F, -height, z, 0.5F, 0.98F, 0);
      vertex(out, pose, width, 0.0F, z, 0.98F, 0.5F, alpha);
      vertex(out, pose, 0.0F, height, z, 0.5F, 0.02F, alpha);
      vertex(out, pose, -width, 0.0F, z, 0.02F, 0.5F, alpha);
   }

   private static void drawCrest(VertexConsumer out, Pose pose, float width, float length, int alpha) {
      float front = length * 0.36F;
      float back = -length * 0.64F;
      vertex(out, pose, -width * 0.18F, 0.0F, front, 2.4F, 0.02F, alpha);
      vertex(out, pose, width * 0.18F, 0.0F, front, 2.6F, 0.02F, alpha);
      vertex(out, pose, width * 0.5F, 0.0F, back, 2.86F, 0.98F, 0);
      vertex(out, pose, -width * 0.5F, 0.0F, back, 2.14F, 0.98F, 0);
   }

   private static void vertex(VertexConsumer out, Pose pose, float x, float y, float z, float u, float v, int alpha) {
      out.vertex(pose.pose(), x, y, z)
         .color(185, 238, 255, alpha)
         .uv(u, v)
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(240)
         .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
         .endVertex();
   }

   public ResourceLocation getTextureLocation(GlacialPursuitEntity entity) {
      return FALLBACK;
   }
}
