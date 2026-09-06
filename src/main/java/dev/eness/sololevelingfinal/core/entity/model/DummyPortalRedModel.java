package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.DummyPortalRedEntity;
import software.bernie.geckolib.model.GeoModel;

public class DummyPortalRedModel extends GeoModel<DummyPortalRedEntity> {
   public ResourceLocation getAnimationResource(DummyPortalRedEntity entity) {
      return new ResourceLocation("sololeveling", "animations/portalgate.animation.json");
   }

   public ResourceLocation getModelResource(DummyPortalRedEntity entity) {
      return new ResourceLocation("sololeveling", "geo/portalgate.geo.json");
   }

   public ResourceLocation getTextureResource(DummyPortalRedEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
