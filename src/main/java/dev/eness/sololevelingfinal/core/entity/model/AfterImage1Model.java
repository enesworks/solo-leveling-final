package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.AfterImage1Entity;
import software.bernie.geckolib.model.GeoModel;

public class AfterImage1Model extends GeoModel<AfterImage1Entity> {
   public ResourceLocation getAnimationResource(AfterImage1Entity entity) {
      return new ResourceLocation("sololeveling", "animations/afterimage1.animation.json");
   }

   public ResourceLocation getModelResource(AfterImage1Entity entity) {
      return new ResourceLocation("sololeveling", "geo/afterimage1.geo.json");
   }

   public ResourceLocation getTextureResource(AfterImage1Entity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
