package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.KasakaEntity;
import software.bernie.geckolib.model.GeoModel;

public class KasakaModel extends GeoModel<KasakaEntity> {
   public ResourceLocation getAnimationResource(KasakaEntity entity) {
      return new ResourceLocation("sololeveling", "animations/serpent.animation.json");
   }

   public ResourceLocation getModelResource(KasakaEntity entity) {
      return new ResourceLocation("sololeveling", "geo/serpent.geo.json");
   }

   public ResourceLocation getTextureResource(KasakaEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
