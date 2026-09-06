package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.DummyPortalPurpleEntity;
import software.bernie.geckolib.model.GeoModel;

public class DummyPortalPurpleModel extends GeoModel<DummyPortalPurpleEntity> {
   public ResourceLocation getAnimationResource(DummyPortalPurpleEntity entity) {
      return new ResourceLocation("sololeveling", "animations/portalgate.animation.json");
   }

   public ResourceLocation getModelResource(DummyPortalPurpleEntity entity) {
      return new ResourceLocation("sololeveling", "geo/portalgate.geo.json");
   }

   public ResourceLocation getTextureResource(DummyPortalPurpleEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
