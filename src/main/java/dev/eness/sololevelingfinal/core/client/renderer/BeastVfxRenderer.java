package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.renderer.shader.BeastVfxRenderTypes;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DeferredWorldShaderRenderer;
import dev.eness.sololevelingfinal.core.entity.BeastVfxEntity;

public final class BeastVfxRenderer extends EntityRenderer<BeastVfxEntity> {
   private static final ResourceLocation FALLBACK = new ResourceLocation("sololeveling", "textures/particle/slashgood1.png");

   public BeastVfxRenderer(Context context) {
      super(context);
   }

   public void render(BeastVfxEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
      float fade = entity.getFade(partialTick);
      if (!(fade <= 0.0F)) {
         VertexConsumer surface = DeferredWorldShaderRenderer.buffer(buffers, BeastVfxRenderTypes.surface());
         VertexConsumer glow = DeferredWorldShaderRenderer.buffer(buffers, BeastVfxRenderTypes.additive());
         poseStack.pushPose();
         switch (entity.getStyle()) {
            case 0:
               this.renderClaw(entity, poseStack, surface, glow, fade);
               break;
            case 1:
               this.renderScar(entity, poseStack, surface, glow, fade);
               break;
            case 2:
               this.renderIntercept(entity, poseStack, surface, glow, fade);
               break;
            case 3:
               this.renderQuarry(entity, poseStack, surface, glow, fade);
               break;
            case 4:
               this.renderOpening(entity, poseStack, surface, glow, fade);
               break;
            case 5:
               this.renderRift(entity, poseStack, surface, glow, fade);
               break;
            case 6:
               this.renderRubbleJaw(entity, poseStack, surface, glow, fade);
               break;
            case 7:
               this.renderKingsMaul(entity, poseStack, surface, glow, fade);
               break;
            case 8:
               this.renderReconstitution(entity, poseStack, surface, glow, fade);
               break;
            case 9:
               this.renderWhiteFang(entity, poseStack, surface, glow, fade);
               break;
            default:
               this.renderClaw(entity, poseStack, surface, glow, fade);
         }

         poseStack.popPose();
         super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
      }
   }

   private void renderClaw(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      orientForward(entity, stack);
      int fans = entity.getVariant() >= 3 ? 2 : 1;

      for (int fan = 0; fan < fans; fan++) {
         float baseRoll = switch (entity.getVariant()) {
            case 1 -> -27.0F;
            case 2 -> 27.0F;
            default -> fan == 0 ? -31.0F : 31.0F;
         };

         for (int claw = -1; claw <= 1; claw++) {
            stack.pushPose();
            stack.translate(claw * entity.getScale() * 0.1, claw * entity.getScale() * 0.13, -fan * 0.014 - Math.abs(claw) * 0.004);
            stack.mulPose(Axis.ZP.rotationDegrees(entity.getRoll() + baseRoll + claw * 2.2F));
            float outerScale = claw == 0 ? 1.0F : 0.9F;
            drawLayered(
               stack,
               surface,
               glow,
               entity.getScale() * outerScale,
               entity.getLength() * 0.105F,
               0.0F,
               entity.getPrimaryColor(),
               entity.getSecondaryColor(),
               fade * (claw == 0 ? 1.0F : 0.88F),
               224,
               78
            );
            stack.popPose();
         }
      }
   }

   private void renderScar(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      float expansion = 0.86F + entity.getProgress(0.0F) * 0.2F;

      for (int i = -1; i <= 1; i++) {
         stack.pushPose();
         stack.translate(i * entity.getScale() * 0.18, i * -0.035, i * 0.004);
         stack.mulPose(Axis.ZP.rotationDegrees(entity.getRoll() + i * 4.5F));
         drawLayered(
            stack,
            surface,
            glow,
            entity.getScale() * 0.72F * expansion,
            entity.getLength() * 0.16F,
            1.0F,
            entity.getPrimaryColor(),
            entity.getSecondaryColor(),
            fade,
            205,
            82
         );
         stack.popPose();
      }
   }

   private void renderIntercept(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      orientForward(entity, stack);
      float pulse = 0.94F + 0.06F * (float)Math.sin(entity.tickCount * 1.25F);
      float width = entity.getScale() * pulse;
      float trail = entity.getLength() * 0.55F;

      for (int i = 0; i < 3; i++) {
         stack.pushPose();
         stack.translate(0.0, (i - 1) * 0.22, -trail * 0.82 - i * 0.18);
         stack.mulPose(Axis.XP.rotationDegrees(90.0F));
         stack.mulPose(Axis.ZP.rotationDegrees((i - 1) * 13.0F));
         drawLayered(
            stack,
            surface,
            glow,
            width * (1.0F - i * 0.16F),
            trail,
            2.0F,
            entity.getPrimaryColor(),
            entity.getSecondaryColor(),
            fade * (1.0F - i * 0.18F),
            174,
            70
         );
         stack.popPose();
      }
   }

   private void renderQuarry(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      stack.translate(0.0, entity.getLength() * 0.58, 0.0);
      float pulse = 0.96F + 0.08F * (float)Math.sin(entity.tickCount * 0.72F);
      drawLayered(
         stack,
         surface,
         glow,
         entity.getScale() * pulse,
         entity.getLength() * 0.55F * pulse,
         3.0F,
         entity.getPrimaryColor(),
         entity.getSecondaryColor(),
         fade,
         222,
         98
      );
   }

   private void renderOpening(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      float expansion = 0.82F + entity.getProgress(0.0F) * 0.42F;

      for (int i = 0; i < 2; i++) {
         stack.pushPose();
         stack.mulPose(Axis.ZP.rotationDegrees((i == 0 ? -34.0F : 34.0F) + entity.getRoll()));
         drawLayered(
            stack,
            surface,
            glow,
            entity.getScale() * expansion,
            entity.getLength() * 0.16F,
            4.0F,
            entity.getPrimaryColor(),
            entity.getSecondaryColor(),
            fade,
            228,
            86
         );
         stack.popPose();
      }
   }

   private void renderRift(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      orientForward(entity, stack);
      float pulse = 0.94F + 0.06F * (float)Math.sin((entity.tickCount + entity.getVariant() * 3) * 0.9F);

      for (int claw = -1; claw <= 1; claw++) {
         stack.pushPose();
         stack.translate(claw * entity.getScale() * 0.24, claw * 0.045, claw * -0.006);
         stack.mulPose(Axis.ZP.rotationDegrees(claw * 3.5F + entity.getRoll()));
         drawLayered(
            stack,
            surface,
            glow,
            entity.getScale() * 0.56F * pulse,
            entity.getLength(),
            0.0F,
            entity.getPrimaryColor(),
            entity.getSecondaryColor(),
            fade * (claw == 0 ? 1.0F : 0.82F),
            218,
            122
         );
         stack.popPose();
      }
   }

   private void renderRubbleJaw(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      stack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
      stack.mulPose(Axis.XP.rotationDegrees(90.0F));
      float opening = 42.0F - entity.getProgress(0.0F) * 13.0F;

      for (int side = -1; side <= 1; side += 2) {
         stack.pushPose();
         stack.translate(side * entity.getScale() * 0.33, 0.0, 0.008);
         stack.mulPose(Axis.ZP.rotationDegrees(side * opening));
         drawLayered(
            stack, surface, glow, entity.getLength(), entity.getScale() * 0.16F, 4.0F, entity.getPrimaryColor(), entity.getSecondaryColor(), fade, 196, 92
         );
         stack.popPose();
      }
   }

   private void renderKingsMaul(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      float snap = 0.72F + entity.getProgress(0.0F) * 0.34F;

      for (int side = -1; side <= 1; side += 2) {
         for (int claw = -1; claw <= 1; claw++) {
            stack.pushPose();
            stack.translate(claw * entity.getScale() * 0.1, side * entity.getScale() * 0.08, claw * 0.004);
            stack.mulPose(Axis.ZP.rotationDegrees(side * 38.0F + claw * 2.0F));
            drawLayered(
               stack,
               surface,
               glow,
               entity.getScale() * snap,
               entity.getLength() * 0.09F,
               0.0F,
               entity.getPrimaryColor(),
               entity.getSecondaryColor(),
               fade * (claw == 0 ? 1.0F : 0.84F),
               238,
               94
            );
            stack.popPose();
         }
      }
   }

   private void renderReconstitution(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      float close = 1.25F - entity.getProgress(0.0F) * 0.48F;

      for (int i = 0; i < 5; i++) {
         stack.pushPose();
         stack.mulPose(Axis.ZP.rotationDegrees(i * 72.0F + entity.tickCount * 2.8F));
         stack.translate(0.0, entity.getScale() * close, i * -0.004);
         stack.mulPose(Axis.ZP.rotationDegrees(90.0F));
         drawLayered(
            stack,
            surface,
            glow,
            entity.getScale() * 0.58F,
            entity.getLength() * 0.11F,
            4.0F,
            entity.getPrimaryColor(),
            entity.getSecondaryColor(),
            fade * 0.78F,
            186,
            74
         );
         stack.popPose();
      }
   }

   private void renderWhiteFang(BeastVfxEntity entity, PoseStack stack, VertexConsumer surface, VertexConsumer glow, float fade) {
      stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      float burst = 0.62F + entity.getProgress(0.0F) * 1.2F;

      for (int i = 0; i < 6; i++) {
         stack.pushPose();
         stack.mulPose(Axis.ZP.rotationDegrees(i * 60.0F + entity.getRoll()));
         stack.translate(0.0, entity.getScale() * burst, i * -0.004);
         drawLayered(
            stack,
            surface,
            glow,
            entity.getScale() * 0.9F,
            entity.getLength() * 0.09F,
            4.0F,
            entity.getPrimaryColor(),
            entity.getSecondaryColor(),
            fade,
            236,
            82
         );
         stack.popPose();
      }
   }

   private static void drawLayered(
      PoseStack stack,
      VertexConsumer surface,
      VertexConsumer glow,
      float width,
      float height,
      float kind,
      int primary,
      int secondary,
      float fade,
      int glowAlpha,
      int surfaceAlpha
   ) {
      LiuSwordBeamRenderer.drawPlane(surface, stack.last(), width * 1.05F, height * 1.18F, kind, secondary, alpha(surfaceAlpha * fade));
      stack.pushPose();
      stack.translate(0.0, 0.0, 0.006);
      LiuSwordBeamRenderer.drawPlane(glow, stack.last(), width, height, kind, primary, alpha(glowAlpha * fade));
      stack.popPose();
   }

   private static void orientForward(BeastVfxEntity entity, PoseStack stack) {
      stack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
      stack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
   }

   private static int alpha(float value) {
      return Math.max(0, Math.min(255, Math.round(value)));
   }

   public ResourceLocation getTextureLocation(BeastVfxEntity entity) {
      return FALLBACK;
   }
}
