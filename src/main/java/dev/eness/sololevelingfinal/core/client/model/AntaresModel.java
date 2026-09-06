package dev.eness.sololevelingfinal.core.client.model;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.entity.AntaresEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AntaresModel extends GeoModel<AntaresEntity> {
    @Override public ResourceLocation getModelResource(AntaresEntity entity){return ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID,"geo/antares.geo.json");}
    @Override public ResourceLocation getTextureResource(AntaresEntity entity){return ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID,"textures/entity/antares.png");}
    @Override public ResourceLocation getAnimationResource(AntaresEntity entity){return ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID,"animations/antares.animation.json");}
}
