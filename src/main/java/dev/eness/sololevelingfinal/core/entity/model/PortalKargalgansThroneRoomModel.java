package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.PortalKargalgansThroneRoomEntity;
import software.bernie.geckolib.model.GeoModel;

public class PortalKargalgansThroneRoomModel extends GeoModel<PortalKargalgansThroneRoomEntity> {
   public ResourceLocation getAnimationResource(PortalKargalgansThroneRoomEntity entity) {
      return new ResourceLocation("sololeveling", "animations/portalgate.animation.json");
   }

   public ResourceLocation getModelResource(PortalKargalgansThroneRoomEntity entity) {
      return new ResourceLocation("sololeveling", "geo/portalgate.geo.json");
   }

   public ResourceLocation getTextureResource(PortalKargalgansThroneRoomEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
