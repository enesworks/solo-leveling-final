package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.GreenOrcEntity;
import software.bernie.geckolib.model.GeoModel;

public class GreenOrcModel extends GeoModel<GreenOrcEntity> {
   public ResourceLocation getAnimationResource(GreenOrcEntity entity) {
      return new ResourceLocation("sololeveling", "animations/greenorc.animation.json");
   }

   public ResourceLocation getModelResource(GreenOrcEntity entity) {
      return new ResourceLocation("sololeveling", "geo/greenorc.geo.json");
   }

   public ResourceLocation getTextureResource(GreenOrcEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
