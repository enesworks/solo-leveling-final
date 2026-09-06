package dev.eness.sololevelingfinal.core.block.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.entity.InstanceCoverTileEntity;
import software.bernie.geckolib.model.GeoModel;

public class InstanceCoverBlockModel extends GeoModel<InstanceCoverTileEntity> {
   public ResourceLocation getAnimationResource(InstanceCoverTileEntity animatable) {
      return new ResourceLocation("sololeveling", "animations/instancecover.animation.json");
   }

   public ResourceLocation getModelResource(InstanceCoverTileEntity animatable) {
      return new ResourceLocation("sololeveling", "geo/instancecover.geo.json");
   }

   public ResourceLocation getTextureResource(InstanceCoverTileEntity entity) {
      return new ResourceLocation("sololeveling", "textures/block/instancecover1.png");
   }
}
