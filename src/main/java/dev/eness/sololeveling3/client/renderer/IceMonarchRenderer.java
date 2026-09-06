package dev.eness.sololeveling3.client.renderer;

import dev.eness.sololeveling3.client.model.IceMonarchModel;
import dev.eness.sololeveling3.entity.IceMonarchEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class IceMonarchRenderer extends GeoEntityRenderer<IceMonarchEntity> {
    public IceMonarchRenderer(EntityRendererProvider.Context context) {
        super(context, new IceMonarchModel());
        this.shadowRadius = 0.55F;
    }

    @Override
    public RenderType getRenderType(
            IceMonarchEntity entity,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
}
