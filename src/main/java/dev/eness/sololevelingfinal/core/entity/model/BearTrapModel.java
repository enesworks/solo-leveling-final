package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.BearTrapEntity;
import software.bernie.geckolib.model.GeoModel;

public class BearTrapModel extends GeoModel<BearTrapEntity> {
   public ResourceLocation getAnimationResource(BearTrapEntity entity) {
      return new ResourceLocation("sololeveling", "animations/beartrap.animation.json");
   }

   public ResourceLocation getModelResource(BearTrapEntity entity) {
      return new ResourceLocation("sololeveling", "geo/beartrap.geo.json");
   }

   public ResourceLocation getTextureResource(BearTrapEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
