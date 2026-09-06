package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.DragonheadEntity;
import software.bernie.geckolib.model.GeoModel;

public class DragonheadModel extends GeoModel<DragonheadEntity> {
   public ResourceLocation getAnimationResource(DragonheadEntity entity) {
      return new ResourceLocation("sololeveling", "animations/dragonhead.animation.json");
   }

   public ResourceLocation getModelResource(DragonheadEntity entity) {
      return new ResourceLocation("sololeveling", "geo/dragonhead.geo.json");
   }

   public ResourceLocation getTextureResource(DragonheadEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
