package dev.eness.sololevelingfinal.core.block.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.entity.InstanceDungeonKeyLoggerTileEntity;
import software.bernie.geckolib.model.GeoModel;

public class InstanceDungeonKeyLoggerBlockModel extends GeoModel<InstanceDungeonKeyLoggerTileEntity> {
   public ResourceLocation getAnimationResource(InstanceDungeonKeyLoggerTileEntity animatable) {
      return new ResourceLocation("sololeveling", "animations/keyhole.animation.json");
   }

   public ResourceLocation getModelResource(InstanceDungeonKeyLoggerTileEntity animatable) {
      return new ResourceLocation("sololeveling", "geo/keyhole.geo.json");
   }

   public ResourceLocation getTextureResource(InstanceDungeonKeyLoggerTileEntity entity) {
      return new ResourceLocation("sololeveling", "textures/block/instancecover1.png");
   }
}
