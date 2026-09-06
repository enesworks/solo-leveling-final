package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.PortalAncientGolemEntity;
import software.bernie.geckolib.model.GeoModel;

public class PortalAncientGolemModel extends GeoModel<PortalAncientGolemEntity> {
   public ResourceLocation getAnimationResource(PortalAncientGolemEntity entity) {
      return new ResourceLocation("sololeveling", "animations/portalgate.animation.json");
   }

   public ResourceLocation getModelResource(PortalAncientGolemEntity entity) {
      return new ResourceLocation("sololeveling", "geo/portalgate.geo.json");
   }

   public ResourceLocation getTextureResource(PortalAncientGolemEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
