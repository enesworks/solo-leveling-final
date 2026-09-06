package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.AncientGolemEntity;
import software.bernie.geckolib.model.GeoModel;

public class AncientGolemModel extends GeoModel<AncientGolemEntity> {
   public ResourceLocation getAnimationResource(AncientGolemEntity entity) {
      return new ResourceLocation("sololeveling", "animations/ancientgolem.animation.json");
   }

   public ResourceLocation getModelResource(AncientGolemEntity entity) {
      return new ResourceLocation("sololeveling", "geo/ancientgolem.geo.json");
   }

   public ResourceLocation getTextureResource(AncientGolemEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
