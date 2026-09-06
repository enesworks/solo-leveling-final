package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.BeruBossEntity;
import software.bernie.geckolib.model.GeoModel;

public class BeruBossModel extends GeoModel<BeruBossEntity> {
   public ResourceLocation getAnimationResource(BeruBossEntity entity) {
      return new ResourceLocation("sololeveling", "animations/beru_lucid.animation.json");
   }

   public ResourceLocation getModelResource(BeruBossEntity entity) {
      return new ResourceLocation("sololeveling", "geo/beru_lucid.geo.json");
   }

   public ResourceLocation getTextureResource(BeruBossEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
