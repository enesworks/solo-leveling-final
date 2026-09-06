package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.CentipedeEntity;
import software.bernie.geckolib.model.GeoModel;

public class CentipedeModel extends GeoModel<CentipedeEntity> {
   public ResourceLocation getAnimationResource(CentipedeEntity entity) {
      return new ResourceLocation("sololeveling", "animations/centipede.animation.json");
   }

   public ResourceLocation getModelResource(CentipedeEntity entity) {
      return new ResourceLocation("sololeveling", "geo/centipede.geo.json");
   }

   public ResourceLocation getTextureResource(CentipedeEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
