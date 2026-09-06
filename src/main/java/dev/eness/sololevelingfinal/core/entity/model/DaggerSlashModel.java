package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.DaggerSlashEntity;
import software.bernie.geckolib.model.GeoModel;

public class DaggerSlashModel extends GeoModel<DaggerSlashEntity> {
   public ResourceLocation getAnimationResource(DaggerSlashEntity entity) {
      return new ResourceLocation("sololeveling", "animations/daggerslash.animation.json");
   }

   public ResourceLocation getModelResource(DaggerSlashEntity entity) {
      return new ResourceLocation("sololeveling", "geo/daggerslash.geo.json");
   }

   public ResourceLocation getTextureResource(DaggerSlashEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
