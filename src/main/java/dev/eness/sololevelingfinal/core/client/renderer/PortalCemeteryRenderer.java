package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.renderer.shader.GateRenderTypes;
import dev.eness.sololevelingfinal.core.entity.PortalCemeteryEntity;
import dev.eness.sololevelingfinal.core.entity.model.PortalCemeteryModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PortalCemeteryRenderer extends GeoEntityRenderer<PortalCemeteryEntity> {
   public PortalCemeteryRenderer(Context renderManager) {
      super(renderManager, new PortalCemeteryModel());
      this.shadowRadius = 0.0F;
   }

   public RenderType getRenderType(PortalCemeteryEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return GateRenderTypes.emissive(this.getTextureLocation(animatable));
   }

   public void preRender(
      PoseStack poseStack,
      PortalCemeteryEntity entity,
      BakedGeoModel model,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha
   ) {
      float scale = 1.5F;
      this.scaleHeight = scale;
      this.scaleWidth = scale;
      super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
   }

   protected float getDeathMaxRotation(PortalCemeteryEntity entityLivingBaseIn) {
      return 0.0F;
   }
}
