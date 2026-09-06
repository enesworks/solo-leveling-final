package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.SlasheffectswordEntity;
import software.bernie.geckolib.model.GeoModel;

public class SlasheffectswordModel extends GeoModel<SlasheffectswordEntity> {
   public ResourceLocation getAnimationResource(SlasheffectswordEntity entity) {
      return new ResourceLocation("sololeveling", "animations/slasheffectlatest.animation.json");
   }

   public ResourceLocation getModelResource(SlasheffectswordEntity entity) {
      return new ResourceLocation("sololeveling", "geo/slasheffectlatest.geo.json");
   }

   public ResourceLocation getTextureResource(SlasheffectswordEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
