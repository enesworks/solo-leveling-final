package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.DummyPortalNormalEntity;
import software.bernie.geckolib.model.GeoModel;

public class DummyPortalNormalModel extends GeoModel<DummyPortalNormalEntity> {
   public ResourceLocation getAnimationResource(DummyPortalNormalEntity entity) {
      return new ResourceLocation("sololeveling", "animations/portalgate.animation.json");
   }

   public ResourceLocation getModelResource(DummyPortalNormalEntity entity) {
      return new ResourceLocation("sololeveling", "geo/portalgate.geo.json");
   }

   public ResourceLocation getTextureResource(DummyPortalNormalEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
