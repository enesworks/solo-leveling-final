package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.BellOfHealingEntity;
import software.bernie.geckolib.model.GeoModel;

public class BellOfHealingModel extends GeoModel<BellOfHealingEntity> {
   public ResourceLocation getAnimationResource(BellOfHealingEntity entity) {
      return new ResourceLocation("sololeveling", "animations/bellofhealing.animation.json");
   }

   public ResourceLocation getModelResource(BellOfHealingEntity entity) {
      return new ResourceLocation("sololeveling", "geo/bellofhealing.geo.json");
   }

   public ResourceLocation getTextureResource(BellOfHealingEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
