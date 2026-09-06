package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.HighOrcEntity;
import software.bernie.geckolib.model.GeoModel;

public class HighOrcModel extends GeoModel<HighOrcEntity> {
   public ResourceLocation getAnimationResource(HighOrcEntity entity) {
      return new ResourceLocation("sololeveling", "animations/greenorc.animation.json");
   }

   public ResourceLocation getModelResource(HighOrcEntity entity) {
      return new ResourceLocation("sololeveling", "geo/greenorc.geo.json");
   }

   public ResourceLocation getTextureResource(HighOrcEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
