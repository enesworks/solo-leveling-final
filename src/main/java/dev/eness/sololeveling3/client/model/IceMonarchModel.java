package dev.eness.sololeveling3.client.model;

import dev.eness.sololeveling3.entity.IceMonarchEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class IceMonarchModel extends GeoModel<IceMonarchEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("solo_leveling", "geo/icemonarch.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("solo_leveling", "textures/entities/icemonarch.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath("solo_leveling", "animations/icemonarch.animation.json");

    @Override
    public ResourceLocation getModelResource(IceMonarchEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(IceMonarchEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(IceMonarchEntity entity) {
        return ANIMATION;
    }
}
