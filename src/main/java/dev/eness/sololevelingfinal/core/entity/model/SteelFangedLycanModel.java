package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import software.bernie.geckolib.model.GeoModel;

public class SteelFangedLycanModel extends GeoModel<SteelFangedLycanEntity> {
   public ResourceLocation getAnimationResource(SteelFangedLycanEntity entity) {
      return new ResourceLocation("sololeveling", "animations/lycan_normal.animation.json");
   }

   public ResourceLocation getModelResource(SteelFangedLycanEntity entity) {
      return new ResourceLocation("sololeveling", "geo/lycan_normal.geo.json");
   }

   public ResourceLocation getTextureResource(SteelFangedLycanEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
