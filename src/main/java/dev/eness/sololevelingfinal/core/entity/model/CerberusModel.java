package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.CerberusEntity;
import software.bernie.geckolib.model.GeoModel;

public class CerberusModel extends GeoModel<CerberusEntity> {
   public ResourceLocation getAnimationResource(CerberusEntity entity) {
      return new ResourceLocation("sololeveling", "animations/cerberus.animation.json");
   }

   public ResourceLocation getModelResource(CerberusEntity entity) {
      return new ResourceLocation("sololeveling", "geo/cerberus.geo.json");
   }

   public ResourceLocation getTextureResource(CerberusEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
