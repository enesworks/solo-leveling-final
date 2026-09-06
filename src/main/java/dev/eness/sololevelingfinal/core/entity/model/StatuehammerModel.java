package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.StatuehammerEntity;
import software.bernie.geckolib.model.GeoModel;

public class StatuehammerModel extends GeoModel<StatuehammerEntity> {
   public ResourceLocation getAnimationResource(StatuehammerEntity entity) {
      return new ResourceLocation("sololeveling", "animations/statue_hammer.animation.json");
   }

   public ResourceLocation getModelResource(StatuehammerEntity entity) {
      return new ResourceLocation("sololeveling", "geo/statue_hammer.geo.json");
   }

   public ResourceLocation getTextureResource(StatuehammerEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
