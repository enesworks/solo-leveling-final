package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.RedGateEntity;
import software.bernie.geckolib.model.GeoModel;

public class RedGateModel extends GeoModel<RedGateEntity> {
   public ResourceLocation getAnimationResource(RedGateEntity entity) {
      return new ResourceLocation("sololeveling", "animations/portalgate.animation.json");
   }

   public ResourceLocation getModelResource(RedGateEntity entity) {
      return new ResourceLocation("sololeveling", "geo/portalgate.geo.json");
   }

   public ResourceLocation getTextureResource(RedGateEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
