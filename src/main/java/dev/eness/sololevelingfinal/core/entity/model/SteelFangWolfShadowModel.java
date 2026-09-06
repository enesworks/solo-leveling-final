package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfShadowEntity;
import software.bernie.geckolib.model.GeoModel;

public class SteelFangWolfShadowModel extends GeoModel<SteelFangWolfShadowEntity> {
   public ResourceLocation getAnimationResource(SteelFangWolfShadowEntity entity) {
      return new ResourceLocation("sololeveling", "animations/lycan.animation.json");
   }

   public ResourceLocation getModelResource(SteelFangWolfShadowEntity entity) {
      return new ResourceLocation("sololeveling", "geo/lycan.geo.json");
   }

   public ResourceLocation getTextureResource(SteelFangWolfShadowEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
