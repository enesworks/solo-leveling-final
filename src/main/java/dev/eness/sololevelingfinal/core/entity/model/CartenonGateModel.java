package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.CartenonGateEntity;
import software.bernie.geckolib.model.GeoModel;

public class CartenonGateModel extends GeoModel<CartenonGateEntity> {
   private static final ResourceLocation MODEL = new ResourceLocation("sololeveling", "geo/portalgate.geo.json");
   private static final ResourceLocation ANIMATION = new ResourceLocation("sololeveling", "animations/portalgate.animation.json");
   private static final ResourceLocation TEXTURE = new ResourceLocation("sololeveling", "textures/entities/portalgate2.png");

   public ResourceLocation getModelResource(CartenonGateEntity entity) {
      return MODEL;
   }

   public ResourceLocation getTextureResource(CartenonGateEntity entity) {
      return TEXTURE;
   }

   public ResourceLocation getAnimationResource(CartenonGateEntity entity) {
      return ANIMATION;
   }
}
