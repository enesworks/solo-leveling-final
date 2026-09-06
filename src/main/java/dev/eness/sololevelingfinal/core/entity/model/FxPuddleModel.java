package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.FxPuddleEntity;
import software.bernie.geckolib.model.GeoModel;

public class FxPuddleModel extends GeoModel<FxPuddleEntity> {
   public ResourceLocation getAnimationResource(FxPuddleEntity entity) {
      return new ResourceLocation("sololeveling", "animations/fx_puddle.animation.json");
   }

   public ResourceLocation getModelResource(FxPuddleEntity entity) {
      return new ResourceLocation("sololeveling", "geo/fx_puddle.geo.json");
   }

   public ResourceLocation getTextureResource(FxPuddleEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
