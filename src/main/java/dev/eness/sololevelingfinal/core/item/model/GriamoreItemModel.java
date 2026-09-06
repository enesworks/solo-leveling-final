package dev.eness.sololevelingfinal.core.item.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.item.GriamoreItem;
import software.bernie.geckolib.model.GeoModel;

public class GriamoreItemModel extends GeoModel<GriamoreItem> {
   public ResourceLocation getAnimationResource(GriamoreItem animatable) {
      return new ResourceLocation("sololeveling", "animations/griamore.animation.json");
   }

   public ResourceLocation getModelResource(GriamoreItem animatable) {
      return new ResourceLocation("sololeveling", "geo/griamore.geo.json");
   }

   public ResourceLocation getTextureResource(GriamoreItem animatable) {
      return new ResourceLocation("sololeveling", "textures/item/griamore.png");
   }
}
