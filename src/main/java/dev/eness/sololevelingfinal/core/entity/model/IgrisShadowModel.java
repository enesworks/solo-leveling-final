package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import software.bernie.geckolib.model.GeoModel;

public class IgrisShadowModel extends GeoModel<IgrisShadowEntity> {
   public ResourceLocation getAnimationResource(IgrisShadowEntity entity) {
      return new ResourceLocation("sololeveling", "animations/igrismarcus.animation.json");
   }

   public ResourceLocation getModelResource(IgrisShadowEntity entity) {
      return new ResourceLocation("sololeveling", "geo/igrismarcus.geo.json");
   }

   public ResourceLocation getTextureResource(IgrisShadowEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
