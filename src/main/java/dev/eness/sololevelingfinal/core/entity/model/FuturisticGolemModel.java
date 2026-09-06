package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.FuturisticGolemEntity;
import software.bernie.geckolib.model.GeoModel;

public class FuturisticGolemModel extends GeoModel<FuturisticGolemEntity> {
   public ResourceLocation getAnimationResource(FuturisticGolemEntity entity) {
      return new ResourceLocation("sololeveling", "animations/futuristicgolem.animation.json");
   }

   public ResourceLocation getModelResource(FuturisticGolemEntity entity) {
      return new ResourceLocation("sololeveling", "geo/futuristicgolem.geo.json");
   }

   public ResourceLocation getTextureResource(FuturisticGolemEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
