package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.eness.sololevelingfinal.core.client.model.MonarchOfGiantsModel;
import dev.eness.sololevelingfinal.core.entity.MonarchOfGiantsEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class MonarchOfGiantsRenderer extends GeoEntityRenderer<MonarchOfGiantsEntity> {
    public MonarchOfGiantsRenderer(EntityRendererProvider.Context context) {
        super(context, new MonarchOfGiantsModel());
        this.shadowRadius = 1.35F;
        addRenderLayer(new GlowLayer(this));
    }

    @Override
    public RenderType getRenderType(
            MonarchOfGiantsEntity entity,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }

    @Override
    public void preRender(
            PoseStack poseStack,
            MonarchOfGiantsEntity entity,
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
        this.scaleHeight = 2.3F;
        this.scaleWidth = 2.3F;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(MonarchOfGiantsEntity entity) {
        return 0.0F;
    }

    private static final class GlowLayer extends GeoRenderLayer<MonarchOfGiantsEntity> {
        private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath("solo_leveling", "textures/entities/mg_glow.png");

        private GlowLayer(GeoRenderer<MonarchOfGiantsEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(
                PoseStack poseStack,
                MonarchOfGiantsEntity entity,
                BakedGeoModel bakedModel,
                RenderType renderType,
                MultiBufferSource bufferSource,
                VertexConsumer buffer,
                float partialTick,
                int packedLight,
                int packedOverlay
        ) {
            RenderType glowType = RenderType.eyes(GLOW);
            getRenderer().reRender(
                    getDefaultBakedModel(entity),
                    poseStack,
                    bufferSource,
                    entity,
                    glowType,
                    bufferSource.getBuffer(glowType),
                    partialTick,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );
        }
    }
}
