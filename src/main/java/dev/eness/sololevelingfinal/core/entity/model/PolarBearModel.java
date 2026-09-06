package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.PolarBearEntity;
import software.bernie.geckolib.model.GeoModel;

public class PolarBearModel extends GeoModel<PolarBearEntity> {
   public ResourceLocation getAnimationResource(PolarBearEntity entity) {
      return new ResourceLocation("sololeveling", "animations/polarbear.animation.json");
   }

   public ResourceLocation getModelResource(PolarBearEntity entity) {
      return new ResourceLocation("sololeveling", "geo/polarbear.geo.json");
   }

   public ResourceLocation getTextureResource(PolarBearEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
