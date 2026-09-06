package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ShadowHighOrcEntity;
import software.bernie.geckolib.model.GeoModel;

public class ShadowHighOrcModel extends GeoModel<ShadowHighOrcEntity> {
   public ResourceLocation getAnimationResource(ShadowHighOrcEntity entity) {
      return new ResourceLocation("sololeveling", "animations/greenorc.animation.json");
   }

   public ResourceLocation getModelResource(ShadowHighOrcEntity entity) {
      return new ResourceLocation("sololeveling", "geo/greenorc.geo.json");
   }

   public ResourceLocation getTextureResource(ShadowHighOrcEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
