package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.AfterImage2Entity;
import software.bernie.geckolib.model.GeoModel;

public class AfterImage2Model extends GeoModel<AfterImage2Entity> {
   public ResourceLocation getAnimationResource(AfterImage2Entity entity) {
      return new ResourceLocation("sololeveling", "animations/afterimage2.animation.json");
   }

   public ResourceLocation getModelResource(AfterImage2Entity entity) {
      return new ResourceLocation("sololeveling", "geo/afterimage2.geo.json");
   }

   public ResourceLocation getTextureResource(AfterImage2Entity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
