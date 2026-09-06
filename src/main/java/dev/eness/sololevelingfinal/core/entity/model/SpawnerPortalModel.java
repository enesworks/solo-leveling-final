package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.SpawnerPortalEntity;
import software.bernie.geckolib.model.GeoModel;

public class SpawnerPortalModel extends GeoModel<SpawnerPortalEntity> {
   public ResourceLocation getAnimationResource(SpawnerPortalEntity entity) {
      return new ResourceLocation("sololeveling", "animations/spawnerportal.animation.json");
   }

   public ResourceLocation getModelResource(SpawnerPortalEntity entity) {
      return new ResourceLocation("sololeveling", "geo/spawnerportal.geo.json");
   }

   public ResourceLocation getTextureResource(SpawnerPortalEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
