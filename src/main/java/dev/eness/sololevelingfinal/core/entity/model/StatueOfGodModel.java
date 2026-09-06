package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.StatueOfGodEntity;
import software.bernie.geckolib.model.GeoModel;

public class StatueOfGodModel extends GeoModel<StatueOfGodEntity> {
   public ResourceLocation getAnimationResource(StatueOfGodEntity entity) {
      return new ResourceLocation("sololeveling", "animations/statueofgod.animation.json");
   }

   public ResourceLocation getModelResource(StatueOfGodEntity entity) {
      return new ResourceLocation("sololeveling", "geo/statueofgod.geo.json");
   }

   public ResourceLocation getTextureResource(StatueOfGodEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
