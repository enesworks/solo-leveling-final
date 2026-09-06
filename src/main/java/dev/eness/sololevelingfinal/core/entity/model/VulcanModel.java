package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.VulcanEntity;
import software.bernie.geckolib.model.GeoModel;

public class VulcanModel extends GeoModel<VulcanEntity> {
   public ResourceLocation getAnimationResource(VulcanEntity entity) {
      return new ResourceLocation("sololeveling", "animations/vulcan.animation.json");
   }

   public ResourceLocation getModelResource(VulcanEntity entity) {
      return new ResourceLocation("sololeveling", "geo/vulcan.geo.json");
   }

   public ResourceLocation getTextureResource(VulcanEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
