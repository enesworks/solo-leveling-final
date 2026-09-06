package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.SkeletonWarriorEntity;
import software.bernie.geckolib.model.GeoModel;

public class SkeletonWarriorModel extends GeoModel<SkeletonWarriorEntity> {
   public ResourceLocation getAnimationResource(SkeletonWarriorEntity entity) {
      return new ResourceLocation("sololeveling", "animations/nocsy_skeleton_warrior.animation.json");
   }

   public ResourceLocation getModelResource(SkeletonWarriorEntity entity) {
      return new ResourceLocation("sololeveling", "geo/nocsy_skeleton_warrior.geo.json");
   }

   public ResourceLocation getTextureResource(SkeletonWarriorEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
