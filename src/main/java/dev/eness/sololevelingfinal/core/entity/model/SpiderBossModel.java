package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.SpiderBossEntity;
import software.bernie.geckolib.model.GeoModel;

public class SpiderBossModel extends GeoModel<SpiderBossEntity> {
   public ResourceLocation getAnimationResource(SpiderBossEntity entity) {
      return new ResourceLocation("sololeveling", "animations/spiderboss.animation.json");
   }

   public ResourceLocation getModelResource(SpiderBossEntity entity) {
      return new ResourceLocation("sololeveling", "geo/spiderboss.geo.json");
   }

   public ResourceLocation getTextureResource(SpiderBossEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
