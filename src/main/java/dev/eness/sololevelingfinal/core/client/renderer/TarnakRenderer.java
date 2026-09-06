package dev.eness.sololevelingfinal.core.client.renderer;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.client.model.TarnakModel;
import dev.eness.sololevelingfinal.core.entity.TarnakEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TarnakRenderer extends HumanoidMobRenderer<TarnakEntity, TarnakModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "textures/entity/tarnak.png");
    public TarnakRenderer(EntityRendererProvider.Context context) {
        super(context, new TarnakModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TarnakEntity entity) {
        return TEXTURE;
    }
}
