package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ChaHaeInEntity;
import software.bernie.geckolib.model.GeoModel;

public class ChaHaeInModel extends GeoModel<ChaHaeInEntity> {
   public ResourceLocation getAnimationResource(ChaHaeInEntity entity) {
      return new ResourceLocation("sololeveling", "animations/chahaeinmodel1.animation.json");
   }

   public ResourceLocation getModelResource(ChaHaeInEntity entity) {
      return new ResourceLocation("sololeveling", "geo/chahaeinmodel1.geo.json");
   }

   public ResourceLocation getTextureResource(ChaHaeInEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
