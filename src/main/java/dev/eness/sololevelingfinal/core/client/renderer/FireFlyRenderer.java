package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.FireFlyEntity;
import dev.eness.sololevelingfinal.core.entity.layer.FireFlyLayer;
import dev.eness.sololevelingfinal.core.entity.model.FireFlyModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FireFlyRenderer extends GeoEntityRenderer<FireFlyEntity> {
   public FireFlyRenderer(Context renderManager) {
      super(renderManager, new FireFlyModel());
      this.shadowRadius = 0.3F;
      this.addRenderLayer(new FireFlyLayer(this));
   }

   public RenderType getRenderType(FireFlyEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }

   public void preRender(
      PoseStack poseStack,
      FireFlyEntity entity,
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
      float scale = 3.0F;
      this.scaleHeight = scale;
      this.scaleWidth = scale;
      super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
