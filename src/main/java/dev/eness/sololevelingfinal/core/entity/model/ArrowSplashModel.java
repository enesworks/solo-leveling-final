package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ArrowSplashEntity;
import software.bernie.geckolib.model.GeoModel;

public class ArrowSplashModel extends GeoModel<ArrowSplashEntity> {
   public ResourceLocation getAnimationResource(ArrowSplashEntity entity) {
      return new ResourceLocation("sololeveling", "animations/arrowsplash.animation.json");
   }

   public ResourceLocation getModelResource(ArrowSplashEntity entity) {
      return new ResourceLocation("sololeveling", "geo/arrowsplash.geo.json");
   }

   public ResourceLocation getTextureResource(ArrowSplashEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
