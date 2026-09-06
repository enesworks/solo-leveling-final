package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ThomasAndreEntity;
import software.bernie.geckolib.model.GeoModel;

public class ThomasAndreModel extends GeoModel<ThomasAndreEntity> {
   public ResourceLocation getAnimationResource(ThomasAndreEntity entity) {
      return new ResourceLocation("sololeveling", "animations/thomass.animation.json");
   }

   public ResourceLocation getModelResource(ThomasAndreEntity entity) {
      return new ResourceLocation("sololeveling", "geo/thomass.geo.json");
   }

   public ResourceLocation getTextureResource(ThomasAndreEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
