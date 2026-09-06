package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.StoneGolemEntity;
import software.bernie.geckolib.model.GeoModel;

public class StoneGolemModel extends GeoModel<StoneGolemEntity> {
   public ResourceLocation getAnimationResource(StoneGolemEntity entity) {
      return new ResourceLocation("sololeveling", "animations/bossgolem.animation.json");
   }

   public ResourceLocation getModelResource(StoneGolemEntity entity) {
      return new ResourceLocation("sololeveling", "geo/bossgolem.geo.json");
   }

   public ResourceLocation getTextureResource(StoneGolemEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
