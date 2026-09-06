package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.DaggerSlashEntity;
import dev.eness.sololevelingfinal.core.entity.layer.DaggerSlashLayer;
import dev.eness.sololevelingfinal.core.entity.model.DaggerSlashModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DaggerSlashRenderer extends GeoEntityRenderer<DaggerSlashEntity> {
   public DaggerSlashRenderer(Context renderManager) {
      super(renderManager, new DaggerSlashModel());
      this.shadowRadius = 1.0F;
      this.addRenderLayer(new DaggerSlashLayer(this));
   }

   public RenderType getRenderType(DaggerSlashEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }

   public void preRender(
      PoseStack poseStack,
      DaggerSlashEntity entity,
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

   protected float getDeathMaxRotation(DaggerSlashEntity entityLivingBaseIn) {
      return 0.0F;
   }
}
