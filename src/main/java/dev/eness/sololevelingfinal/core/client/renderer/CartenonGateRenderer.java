package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.renderer.shader.GateRenderTypes;
import dev.eness.sololevelingfinal.core.entity.CartenonGateEntity;
import dev.eness.sololevelingfinal.core.entity.model.CartenonGateModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CartenonGateRenderer extends GeoEntityRenderer<CartenonGateEntity> {
   public CartenonGateRenderer(Context context) {
      super(context, new CartenonGateModel());
      this.shadowRadius = 0.0F;
   }

   public RenderType getRenderType(CartenonGateEntity entity, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return GateRenderTypes.emissive(texture);
   }

   public void preRender(
      PoseStack poseStack,
      CartenonGateEntity entity,
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
      this.scaleWidth = 1.2F;
      this.scaleHeight = 1.2F;
      super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
