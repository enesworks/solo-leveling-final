package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.PortalSewersEntity;
import software.bernie.geckolib.model.GeoModel;

public class PortalSewersModel extends GeoModel<PortalSewersEntity> {
   public ResourceLocation getAnimationResource(PortalSewersEntity entity) {
      return new ResourceLocation("sololeveling", "animations/portalgate.animation.json");
   }

   public ResourceLocation getModelResource(PortalSewersEntity entity) {
      return new ResourceLocation("sololeveling", "geo/portalgate.geo.json");
   }

   public ResourceLocation getTextureResource(PortalSewersEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
