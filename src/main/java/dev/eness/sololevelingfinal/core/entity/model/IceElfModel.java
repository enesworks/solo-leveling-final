package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.IceElfEntity;
import software.bernie.geckolib.model.GeoModel;

public class IceElfModel extends GeoModel<IceElfEntity> {
   public ResourceLocation getAnimationResource(IceElfEntity entity) {
      return new ResourceLocation("sololeveling", "animations/iceelf.animation.json");
   }

   public ResourceLocation getModelResource(IceElfEntity entity) {
      return new ResourceLocation("sololeveling", "geo/iceelf.geo.json");
   }

   public ResourceLocation getTextureResource(IceElfEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
