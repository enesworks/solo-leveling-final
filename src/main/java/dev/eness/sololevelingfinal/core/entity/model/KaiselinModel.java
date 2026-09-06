package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.KaiselinEntity;
import software.bernie.geckolib.model.GeoModel;

public class KaiselinModel<T extends KaiselinEntity> extends GeoModel<T> {
   public ResourceLocation getAnimationResource(T entity) {
      return new ResourceLocation("sololeveling", "animations/kaiselin.animation.json");
   }

   public ResourceLocation getModelResource(T entity) {
      return new ResourceLocation("sololeveling", "geo/kaiselin.geo.json");
   }

   public ResourceLocation getTextureResource(T entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
