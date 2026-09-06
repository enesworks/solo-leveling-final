package dev.eness.sololevelingfinal.core.item.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.item.ManaGunItem;
import software.bernie.geckolib.model.GeoModel;

public class ManaGunItemModel extends GeoModel<ManaGunItem> {
   public ResourceLocation getAnimationResource(ManaGunItem animatable) {
      return new ResourceLocation("sololeveling", "animations/magicgun.animation.json");
   }

   public ResourceLocation getModelResource(ManaGunItem animatable) {
      return new ResourceLocation("sololeveling", "geo/magicgun.geo.json");
   }

   public ResourceLocation getTextureResource(ManaGunItem animatable) {
      return new ResourceLocation("sololeveling", "textures/item/managuntex.png");
   }
}
