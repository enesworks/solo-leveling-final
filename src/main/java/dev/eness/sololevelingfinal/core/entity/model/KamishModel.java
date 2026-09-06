package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.KamishEntity;
import software.bernie.geckolib.model.GeoModel;

public class KamishModel extends GeoModel<KamishEntity> {
   public ResourceLocation getAnimationResource(KamishEntity entity) {
      return new ResourceLocation("sololeveling", "animations/dragon.animation.json");
   }

   public ResourceLocation getModelResource(KamishEntity entity) {
      return new ResourceLocation("sololeveling", "geo/dragon.geo.json");
   }

   public ResourceLocation getTextureResource(KamishEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
