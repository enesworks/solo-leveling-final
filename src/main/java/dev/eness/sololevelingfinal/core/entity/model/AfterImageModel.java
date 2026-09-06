package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.AfterImageEntity;
import software.bernie.geckolib.model.GeoModel;

public class AfterImageModel extends GeoModel<AfterImageEntity> {
   public ResourceLocation getAnimationResource(AfterImageEntity entity) {
      return new ResourceLocation("sololeveling", "animations/afterimage.animation.json");
   }

   public ResourceLocation getModelResource(AfterImageEntity entity) {
      return new ResourceLocation("sololeveling", "geo/afterimage.geo.json");
   }

   public ResourceLocation getTextureResource(AfterImageEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
