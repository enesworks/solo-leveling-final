package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import software.bernie.geckolib.model.GeoModel;

public class SteelFangWolfModel extends GeoModel<SteelFangWolfEntity> {
   public ResourceLocation getAnimationResource(SteelFangWolfEntity entity) {
      return new ResourceLocation("sololeveling", "animations/lycan.animation.json");
   }

   public ResourceLocation getModelResource(SteelFangWolfEntity entity) {
      return new ResourceLocation("sololeveling", "geo/lycan.geo.json");
   }

   public ResourceLocation getTextureResource(SteelFangWolfEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
