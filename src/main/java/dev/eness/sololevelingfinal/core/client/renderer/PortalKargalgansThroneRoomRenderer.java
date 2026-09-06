package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.renderer.shader.GateRenderTypes;
import dev.eness.sololevelingfinal.core.entity.PortalKargalgansThroneRoomEntity;
import dev.eness.sololevelingfinal.core.entity.model.PortalKargalgansThroneRoomModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PortalKargalgansThroneRoomRenderer extends GeoEntityRenderer<PortalKargalgansThroneRoomEntity> {
   public PortalKargalgansThroneRoomRenderer(Context renderManager) {
      super(renderManager, new PortalKargalgansThroneRoomModel());
      this.shadowRadius = 0.5F;
   }

   public RenderType getRenderType(PortalKargalgansThroneRoomEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return GateRenderTypes.emissive(this.getTextureLocation(animatable));
   }

   public void preRender(
      PoseStack poseStack,
      PortalKargalgansThroneRoomEntity entity,
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

   protected float getDeathMaxRotation(PortalKargalgansThroneRoomEntity entityLivingBaseIn) {
      return 0.0F;
   }
}
