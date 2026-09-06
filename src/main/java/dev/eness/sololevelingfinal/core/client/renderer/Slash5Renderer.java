package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import dev.eness.sololevelingfinal.core.client.model.ModelSlash5;
import dev.eness.sololevelingfinal.core.entity.Slash5Entity;

public class Slash5Renderer extends EntityRenderer<Slash5Entity> {
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/entities/slash.png");
   private final ModelSlash5 model;

   public Slash5Renderer(Context context) {
      super(context);
      this.model = new ModelSlash5(context.bakeLayer(ModelSlash5.LAYER_LOCATION));
   }

   public void render(Slash5Entity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
      VertexConsumer vb = bufferIn.getBuffer(RenderType.entityCutout(this.getTextureLocation(entityIn)));
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot()) - 90.0F));
      poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F + Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
      this.model.renderToBuffer(poseStack, vb, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.0625F);
      poseStack.popPose();
      super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
   }

   public ResourceLocation getTextureLocation(Slash5Entity entity) {
      return texture;
   }
}
