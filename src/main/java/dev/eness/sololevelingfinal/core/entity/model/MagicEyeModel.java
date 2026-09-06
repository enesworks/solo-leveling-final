package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.MagicEyeEntity;
import software.bernie.geckolib.model.GeoModel;

public class MagicEyeModel extends GeoModel<MagicEyeEntity> {
   public ResourceLocation getAnimationResource(MagicEyeEntity entity) {
      return new ResourceLocation("sololeveling", "animations/detecteye.animation.json");
   }

   public ResourceLocation getModelResource(MagicEyeEntity entity) {
      return new ResourceLocation("sololeveling", "geo/detecteye.geo.json");
   }

   public ResourceLocation getTextureResource(MagicEyeEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
