package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.BarukaEntity;
import software.bernie.geckolib.model.GeoModel;

public class BarukaModel extends GeoModel<BarukaEntity> {
   public ResourceLocation getAnimationResource(BarukaEntity entity) {
      return new ResourceLocation("sololeveling", "animations/baruka.animation.json");
   }

   public ResourceLocation getModelResource(BarukaEntity entity) {
      return new ResourceLocation("sololeveling", "geo/baruka.geo.json");
   }

   public ResourceLocation getTextureResource(BarukaEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
