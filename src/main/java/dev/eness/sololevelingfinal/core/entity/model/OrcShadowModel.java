package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.OrcShadowEntity;
import software.bernie.geckolib.model.GeoModel;

public class OrcShadowModel extends GeoModel<OrcShadowEntity> {
   public ResourceLocation getAnimationResource(OrcShadowEntity entity) {
      return new ResourceLocation("sololeveling", "animations/greenorc.animation.json");
   }

   public ResourceLocation getModelResource(OrcShadowEntity entity) {
      return new ResourceLocation("sololeveling", "geo/greenorc.geo.json");
   }

   public ResourceLocation getTextureResource(OrcShadowEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
