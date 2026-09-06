package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.AncientSamuraiEntity;
import software.bernie.geckolib.model.GeoModel;

public class AncientSamuraiModel extends GeoModel<AncientSamuraiEntity> {
   public ResourceLocation getAnimationResource(AncientSamuraiEntity entity) {
      return new ResourceLocation("sololeveling", "animations/ancientsamurai.animation.json");
   }

   public ResourceLocation getModelResource(AncientSamuraiEntity entity) {
      return new ResourceLocation("sololeveling", "geo/ancientsamurai.geo.json");
   }

   public ResourceLocation getTextureResource(AncientSamuraiEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
