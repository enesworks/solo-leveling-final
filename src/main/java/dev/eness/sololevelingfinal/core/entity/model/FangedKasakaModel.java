package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import software.bernie.geckolib.model.GeoModel;

public class FangedKasakaModel extends GeoModel<FangedKasakaEntity> {
   public ResourceLocation getAnimationResource(FangedKasakaEntity entity) {
      return new ResourceLocation("sololeveling", "animations/fanged_kasaka.animation.json");
   }

   public ResourceLocation getModelResource(FangedKasakaEntity entity) {
      return new ResourceLocation("sololeveling", "geo/fanged_kasaka.geo.json");
   }

   public ResourceLocation getTextureResource(FangedKasakaEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
