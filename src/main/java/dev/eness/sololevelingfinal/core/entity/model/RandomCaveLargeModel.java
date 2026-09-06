package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.RandomCaveLargeEntity;
import software.bernie.geckolib.model.GeoModel;

public class RandomCaveLargeModel extends GeoModel<RandomCaveLargeEntity> {
   public ResourceLocation getAnimationResource(RandomCaveLargeEntity entity) {
      return new ResourceLocation("sololeveling", "animations/portalgate.animation.json");
   }

   public ResourceLocation getModelResource(RandomCaveLargeEntity entity) {
      return new ResourceLocation("sololeveling", "geo/portalgate.geo.json");
   }

   public ResourceLocation getTextureResource(RandomCaveLargeEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
