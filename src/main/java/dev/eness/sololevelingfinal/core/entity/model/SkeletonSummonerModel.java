package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.SkeletonSummonerEntity;
import software.bernie.geckolib.model.GeoModel;

public class SkeletonSummonerModel extends GeoModel<SkeletonSummonerEntity> {
   public ResourceLocation getAnimationResource(SkeletonSummonerEntity entity) {
      return new ResourceLocation("sololeveling", "animations/nocsy_skeleton_necromancer.animation.json");
   }

   public ResourceLocation getModelResource(SkeletonSummonerEntity entity) {
      return new ResourceLocation("sololeveling", "geo/nocsy_skeleton_necromancer.geo.json");
   }

   public ResourceLocation getTextureResource(SkeletonSummonerEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
