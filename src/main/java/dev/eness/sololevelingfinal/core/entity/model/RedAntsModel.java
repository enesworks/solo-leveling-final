package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.RedAntsEntity;
import software.bernie.geckolib.model.GeoModel;

public class RedAntsModel extends GeoModel<RedAntsEntity> {
   public ResourceLocation getAnimationResource(RedAntsEntity entity) {
      return new ResourceLocation("sololeveling", "animations/jejuantnormal.animation.json");
   }

   public ResourceLocation getModelResource(RedAntsEntity entity) {
      return new ResourceLocation("sololeveling", "geo/jejuantnormal.geo.json");
   }

   public ResourceLocation getTextureResource(RedAntsEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
