package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.MiniGemGolemEntity;
import software.bernie.geckolib.model.GeoModel;

public class MiniGemGolemModel extends GeoModel<MiniGemGolemEntity> {
   public ResourceLocation getAnimationResource(MiniGemGolemEntity entity) {
      return new ResourceLocation("sololeveling", "animations/dungeonmons1.animation.json");
   }

   public ResourceLocation getModelResource(MiniGemGolemEntity entity) {
      return new ResourceLocation("sololeveling", "geo/dungeonmons1.geo.json");
   }

   public ResourceLocation getTextureResource(MiniGemGolemEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
