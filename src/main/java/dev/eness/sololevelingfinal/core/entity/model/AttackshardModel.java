package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.AttackshardEntity;
import software.bernie.geckolib.model.GeoModel;

public class AttackshardModel extends GeoModel<AttackshardEntity> {
   public ResourceLocation getAnimationResource(AttackshardEntity entity) {
      return new ResourceLocation("sololeveling", "animations/attackcrystal.animation.json");
   }

   public ResourceLocation getModelResource(AttackshardEntity entity) {
      return new ResourceLocation("sololeveling", "geo/attackcrystal.geo.json");
   }

   public ResourceLocation getTextureResource(AttackshardEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
