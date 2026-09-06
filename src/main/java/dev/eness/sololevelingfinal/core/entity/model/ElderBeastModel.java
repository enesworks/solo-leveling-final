package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ElderBeastEntity;
import software.bernie.geckolib.model.GeoModel;

public class ElderBeastModel extends GeoModel<ElderBeastEntity> {
   public ResourceLocation getAnimationResource(ElderBeastEntity entity) {
      return new ResourceLocation("sololeveling", "animations/elder_beast.animation.json");
   }

   public ResourceLocation getModelResource(ElderBeastEntity entity) {
      return new ResourceLocation("sololeveling", "geo/elder_beast.geo.json");
   }

   public ResourceLocation getTextureResource(ElderBeastEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
