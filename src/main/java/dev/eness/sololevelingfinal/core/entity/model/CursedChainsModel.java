package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.CursedChainsEntity;
import software.bernie.geckolib.model.GeoModel;

public class CursedChainsModel extends GeoModel<CursedChainsEntity> {
   public ResourceLocation getAnimationResource(CursedChainsEntity entity) {
      return new ResourceLocation("sololeveling", "animations/curse_chains.animation.json");
   }

   public ResourceLocation getModelResource(CursedChainsEntity entity) {
      return new ResourceLocation("sololeveling", "geo/curse_chains.geo.json");
   }

   public ResourceLocation getTextureResource(CursedChainsEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
