package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ShadowPolarBearEntity;
import software.bernie.geckolib.model.GeoModel;

public class ShadowPolarBearModel extends GeoModel<ShadowPolarBearEntity> {
   public ResourceLocation getAnimationResource(ShadowPolarBearEntity entity) {
      return new ResourceLocation("sololeveling", "animations/polarbear.animation.json");
   }

   public ResourceLocation getModelResource(ShadowPolarBearEntity entity) {
      return new ResourceLocation("sololeveling", "geo/polarbear.geo.json");
   }

   public ResourceLocation getTextureResource(ShadowPolarBearEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
