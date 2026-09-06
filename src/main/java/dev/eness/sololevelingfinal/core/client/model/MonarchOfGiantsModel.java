package dev.eness.sololevelingfinal.core.client.model;

import dev.eness.sololevelingfinal.core.entity.MonarchOfGiantsEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MonarchOfGiantsModel extends GeoModel<MonarchOfGiantsEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("solo_leveling", "geo/monarch_of_giants.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("solo_leveling", "textures/entities/monarch_of_giants.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath("solo_leveling", "animations/monarch_of_giants.animation.json");

    @Override
    public ResourceLocation getModelResource(MonarchOfGiantsEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MonarchOfGiantsEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MonarchOfGiantsEntity entity) {
        return ANIMATION;
    }
}
