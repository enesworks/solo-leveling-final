package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.FlagOfProtectionEntity;
import software.bernie.geckolib.model.GeoModel;

public class FlagOfProtectionModel extends GeoModel<FlagOfProtectionEntity> {
   public ResourceLocation getAnimationResource(FlagOfProtectionEntity entity) {
      return new ResourceLocation("sololeveling", "animations/flagofprotection.animation.json");
   }

   public ResourceLocation getModelResource(FlagOfProtectionEntity entity) {
      return new ResourceLocation("sololeveling", "geo/flagofprotection.geo.json");
   }

   public ResourceLocation getTextureResource(FlagOfProtectionEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
