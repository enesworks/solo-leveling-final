package dev.eness.sololevelingfinal.core.block.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.display.InstanceCoverDisplayItem;
import software.bernie.geckolib.model.GeoModel;

public class InstanceCoverDisplayModel extends GeoModel<InstanceCoverDisplayItem> {
   public ResourceLocation getAnimationResource(InstanceCoverDisplayItem animatable) {
      return new ResourceLocation("sololeveling", "animations/instancecover.animation.json");
   }

   public ResourceLocation getModelResource(InstanceCoverDisplayItem animatable) {
      return new ResourceLocation("sololeveling", "geo/instancecover.geo.json");
   }

   public ResourceLocation getTextureResource(InstanceCoverDisplayItem entity) {
      return new ResourceLocation("sololeveling", "textures/block/instancecover1.png");
   }
}
