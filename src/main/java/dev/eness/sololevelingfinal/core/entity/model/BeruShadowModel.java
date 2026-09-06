package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import software.bernie.geckolib.model.GeoModel;

public class BeruShadowModel extends GeoModel<BeruShadowEntity> {
   public ResourceLocation getAnimationResource(BeruShadowEntity entity) {
      return new ResourceLocation("sololeveling", "animations/beru_lucid.animation.json");
   }

   public ResourceLocation getModelResource(BeruShadowEntity entity) {
      return new ResourceLocation("sololeveling", "geo/beru_lucid.geo.json");
   }

   public ResourceLocation getTextureResource(BeruShadowEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
