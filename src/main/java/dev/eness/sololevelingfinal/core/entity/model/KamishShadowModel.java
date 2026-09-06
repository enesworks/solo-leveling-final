package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.KamishShadowEntity;
import software.bernie.geckolib.model.GeoModel;

public class KamishShadowModel extends GeoModel<KamishShadowEntity> {
   public ResourceLocation getAnimationResource(KamishShadowEntity entity) {
      return new ResourceLocation("sololeveling", "animations/dragon.animation.json");
   }

   public ResourceLocation getModelResource(KamishShadowEntity entity) {
      return new ResourceLocation("sololeveling", "geo/dragon.geo.json");
   }

   public ResourceLocation getTextureResource(KamishShadowEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
