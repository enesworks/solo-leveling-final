package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.OrcEntity;
import software.bernie.geckolib.model.GeoModel;

public class OrcModel extends GeoModel<OrcEntity> {
   public ResourceLocation getAnimationResource(OrcEntity entity) {
      return new ResourceLocation("sololeveling", "animations/greenorc.animation.json");
   }

   public ResourceLocation getModelResource(OrcEntity entity) {
      return new ResourceLocation("sololeveling", "geo/greenorc.geo.json");
   }

   public ResourceLocation getTextureResource(OrcEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
