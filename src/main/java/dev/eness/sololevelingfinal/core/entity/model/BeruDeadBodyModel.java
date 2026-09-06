package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.BeruDeadBodyEntity;
import software.bernie.geckolib.model.GeoModel;

public class BeruDeadBodyModel extends GeoModel<BeruDeadBodyEntity> {
   public ResourceLocation getAnimationResource(BeruDeadBodyEntity entity) {
      return new ResourceLocation("sololeveling", "animations/beru_final.animation.json");
   }

   public ResourceLocation getModelResource(BeruDeadBodyEntity entity) {
      return new ResourceLocation("sololeveling", "geo/beru_final.geo.json");
   }

   public ResourceLocation getTextureResource(BeruDeadBodyEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
