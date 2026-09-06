package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.StatueswordEntity;
import software.bernie.geckolib.model.GeoModel;

public class StatueswordModel extends GeoModel<StatueswordEntity> {
   public ResourceLocation getAnimationResource(StatueswordEntity entity) {
      return new ResourceLocation("sololeveling", "animations/statue_sword.animation.json");
   }

   public ResourceLocation getModelResource(StatueswordEntity entity) {
      return new ResourceLocation("sololeveling", "geo/statue_sword.geo.json");
   }

   public ResourceLocation getTextureResource(StatueswordEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
