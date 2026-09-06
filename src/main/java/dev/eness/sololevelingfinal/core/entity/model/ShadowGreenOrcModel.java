package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ShadowGreenOrcEntity;
import software.bernie.geckolib.model.GeoModel;

public class ShadowGreenOrcModel extends GeoModel<ShadowGreenOrcEntity> {
   public ResourceLocation getAnimationResource(ShadowGreenOrcEntity entity) {
      return new ResourceLocation("sololeveling", "animations/greenorc.animation.json");
   }

   public ResourceLocation getModelResource(ShadowGreenOrcEntity entity) {
      return new ResourceLocation("sololeveling", "geo/greenorc.geo.json");
   }

   public ResourceLocation getTextureResource(ShadowGreenOrcEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
