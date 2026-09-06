package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.GemGolemEntity;
import software.bernie.geckolib.model.GeoModel;

public class GemGolemModel extends GeoModel<GemGolemEntity> {
   public ResourceLocation getAnimationResource(GemGolemEntity entity) {
      return new ResourceLocation("sololeveling", "animations/gemgolem.animation.json");
   }

   public ResourceLocation getModelResource(GemGolemEntity entity) {
      return new ResourceLocation("sololeveling", "geo/gemgolem.geo.json");
   }

   public ResourceLocation getTextureResource(GemGolemEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
