package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.StatueaxeEntity;
import software.bernie.geckolib.model.GeoModel;

public class StatueaxeModel extends GeoModel<StatueaxeEntity> {
   public ResourceLocation getAnimationResource(StatueaxeEntity entity) {
      return new ResourceLocation("sololeveling", "animations/statue_axe.animation.json");
   }

   public ResourceLocation getModelResource(StatueaxeEntity entity) {
      return new ResourceLocation("sololeveling", "geo/statue_axe.geo.json");
   }

   public ResourceLocation getTextureResource(StatueaxeEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }
}
