package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ShadowIgrisEntity;
import software.bernie.geckolib.model.GeoModel;

public class ShadowIgrisModel extends GeoModel<ShadowIgrisEntity> {
   public ResourceLocation getAnimationResource(ShadowIgrisEntity entity) {
      return new ResourceLocation("sololeveling", "animations/igris_prev.animation.json");
   }

   public ResourceLocation getModelResource(ShadowIgrisEntity entity) {
      return new ResourceLocation("sololeveling", "geo/igris_prev.geo.json");
   }

   public ResourceLocation getTextureResource(ShadowIgrisEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
