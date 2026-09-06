package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.IceBallEntity;
import software.bernie.geckolib.model.GeoModel;

public class IceBallModel extends GeoModel<IceBallEntity> {
   public ResourceLocation getAnimationResource(IceBallEntity entity) {
      return new ResourceLocation("sololeveling", "animations/iceball.animation.json");
   }

   public ResourceLocation getModelResource(IceBallEntity entity) {
      return new ResourceLocation("sololeveling", "geo/iceball.geo.json");
   }

   public ResourceLocation getTextureResource(IceBallEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
