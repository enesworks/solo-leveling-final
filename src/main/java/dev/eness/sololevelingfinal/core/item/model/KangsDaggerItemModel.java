package dev.eness.sololevelingfinal.core.item.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.item.KangsDaggerItem;
import software.bernie.geckolib.model.GeoModel;

public class KangsDaggerItemModel extends GeoModel<KangsDaggerItem> {
   public ResourceLocation getAnimationResource(KangsDaggerItem animatable) {
      return new ResourceLocation("sololeveling", "animations/taeshikdagger_-_converted.animation.json");
   }

   public ResourceLocation getModelResource(KangsDaggerItem animatable) {
      return new ResourceLocation("sololeveling", "geo/taeshikdagger_-_converted.geo.json");
   }

   public ResourceLocation getTextureResource(KangsDaggerItem animatable) {
      return new ResourceLocation("sololeveling", "textures/item/taeshikdagger.png");
   }
}
