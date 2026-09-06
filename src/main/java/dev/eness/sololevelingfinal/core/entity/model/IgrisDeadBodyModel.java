package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.IgrisDeadBodyEntity;
import software.bernie.geckolib.model.GeoModel;

public class IgrisDeadBodyModel extends GeoModel<IgrisDeadBodyEntity> {
   public ResourceLocation getAnimationResource(IgrisDeadBodyEntity entity) {
      return new ResourceLocation("sololeveling", "animations/igrismarcus.animation.json");
   }

   public ResourceLocation getModelResource(IgrisDeadBodyEntity entity) {
      return new ResourceLocation("sololeveling", "geo/igrismarcus.geo.json");
   }

   public ResourceLocation getTextureResource(IgrisDeadBodyEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
