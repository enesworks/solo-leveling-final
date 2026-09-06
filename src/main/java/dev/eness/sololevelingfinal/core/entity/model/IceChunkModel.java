package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.IceChunkEntity;
import software.bernie.geckolib.model.GeoModel;

public class IceChunkModel extends GeoModel<IceChunkEntity> {
   public ResourceLocation getAnimationResource(IceChunkEntity entity) {
      return new ResourceLocation("sololeveling", "animations/icechunks.animation.json");
   }

   public ResourceLocation getModelResource(IceChunkEntity entity) {
      return new ResourceLocation("sololeveling", "geo/icechunks.geo.json");
   }

   public ResourceLocation getTextureResource(IceChunkEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
