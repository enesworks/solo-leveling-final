package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ShadowIronEntity;
import dev.eness.sololevelingfinal.core.entity.layer.ShadowIronEmissiveLayer;
import dev.eness.sololevelingfinal.core.entity.model.ShadowIronModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ShadowIronRenderer extends GeoEntityRenderer<ShadowIronEntity> {
   public ShadowIronRenderer(Context context) {
      super(context, new ShadowIronModel());
      this.shadowRadius = 0.62F;
      this.addRenderLayer(new ShadowIronEmissiveLayer(this));
   }

   public RenderType getRenderType(ShadowIronEntity entity, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(entity));
   }

   public void preRender(
      PoseStack poseStack,
      ShadowIronEntity entity,
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
      this.scaleWidth = 1.0F;
      this.scaleHeight = 1.0F;
      super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
