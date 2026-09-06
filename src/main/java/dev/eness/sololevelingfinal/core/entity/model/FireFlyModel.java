package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.FireFlyEntity;
import software.bernie.geckolib.model.GeoModel;

public class FireFlyModel extends GeoModel<FireFlyEntity> {
   public ResourceLocation getAnimationResource(FireFlyEntity entity) {
      return new ResourceLocation("sololeveling", "animations/fireflies.animation.json");
   }

   public ResourceLocation getModelResource(FireFlyEntity entity) {
      return new ResourceLocation("sololeveling", "geo/fireflies.geo.json");
   }

   public ResourceLocation getTextureResource(FireFlyEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
