package dev.eness.sololevelingfinal.core.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ShadowIronEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class ShadowIronEmissiveLayer extends GeoRenderLayer<ShadowIronEntity> {
   private static final ResourceLocation NORMAL = new ResourceLocation("sololeveling", "textures/entities/iron_shadow_em.png");
   private static final ResourceLocation DOMAIN = new ResourceLocation("sololeveling", "textures/entities/iron_shadow_em_domain.png");

   public ShadowIronEmissiveLayer(GeoRenderer<ShadowIronEntity> renderer) {
      super(renderer);
   }

   public void render(
      PoseStack poseStack,
      ShadowIronEntity entity,
      BakedGeoModel bakedModel,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      ResourceLocation texture = entity.isDomainBoosted() ? DOMAIN : NORMAL;
      RenderType emissive = RenderType.eyes(texture);
      this.getRenderer()
         .reRender(
            this.getDefaultBakedModel(entity),
            poseStack,
            bufferSource,
            entity,
            emissive,
            bufferSource.getBuffer(emissive),
            partialTick,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            1.0F,
            1.0F,
            1.0F,
            0.92F
         );
   }
}
