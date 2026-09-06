package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.SkeletonBruteEntity;
import software.bernie.geckolib.model.GeoModel;

public class SkeletonBruteModel extends GeoModel<SkeletonBruteEntity> {
   public ResourceLocation getAnimationResource(SkeletonBruteEntity entity) {
      return new ResourceLocation("sololeveling", "animations/nocsy_skeleton_brute1.animation.json");
   }

   public ResourceLocation getModelResource(SkeletonBruteEntity entity) {
      return new ResourceLocation("sololeveling", "geo/nocsy_skeleton_brute1.geo.json");
   }

   public ResourceLocation getTextureResource(SkeletonBruteEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
