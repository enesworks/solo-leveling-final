package dev.eness.sololevelingfinal.core.block.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.display.InstanceDungeonKeyLoggerDisplayItem;
import software.bernie.geckolib.model.GeoModel;

public class InstanceDungeonKeyLoggerDisplayModel extends GeoModel<InstanceDungeonKeyLoggerDisplayItem> {
   public ResourceLocation getAnimationResource(InstanceDungeonKeyLoggerDisplayItem animatable) {
      return new ResourceLocation("sololeveling", "animations/keyhole.animation.json");
   }

   public ResourceLocation getModelResource(InstanceDungeonKeyLoggerDisplayItem animatable) {
      return new ResourceLocation("sololeveling", "geo/keyhole.geo.json");
   }

   public ResourceLocation getTextureResource(InstanceDungeonKeyLoggerDisplayItem entity) {
      return new ResourceLocation("sololeveling", "textures/block/instancecover1.png");
   }
}
