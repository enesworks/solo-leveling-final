package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.FxspikEntity;
import software.bernie.geckolib.model.GeoModel;

public class FxspikModel extends GeoModel<FxspikEntity> {
   public ResourceLocation getAnimationResource(FxspikEntity entity) {
      return new ResourceLocation("sololeveling", "animations/fx_spike.animation.json");
   }

   public ResourceLocation getModelResource(FxspikEntity entity) {
      return new ResourceLocation("sololeveling", "geo/fx_spike.geo.json");
   }

   public ResourceLocation getTextureResource(FxspikEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
