package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;
import software.bernie.geckolib.model.GeoModel;

public class KargalganModel extends GeoModel<KargalganEntity> {
   public ResourceLocation getAnimationResource(KargalganEntity entity) {
      return new ResourceLocation("sololeveling", "animations/kardalgan_boss.animation.json");
   }

   public ResourceLocation getModelResource(KargalganEntity entity) {
      return new ResourceLocation("sololeveling", "geo/kardalgan_boss.geo.json");
   }

   public ResourceLocation getTextureResource(KargalganEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
