package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.MutatedEntity;
import software.bernie.geckolib.model.GeoModel;

public class MutatedModel extends GeoModel<MutatedEntity> {
   public ResourceLocation getAnimationResource(MutatedEntity entity) {
      return new ResourceLocation("sololeveling", "animations/mutated.animation.json");
   }

   public ResourceLocation getModelResource(MutatedEntity entity) {
      return new ResourceLocation("sololeveling", "geo/mutated.geo.json");
   }

   public ResourceLocation getTextureResource(MutatedEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
