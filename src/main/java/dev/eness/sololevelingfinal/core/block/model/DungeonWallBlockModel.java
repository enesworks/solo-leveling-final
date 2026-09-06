package dev.eness.sololevelingfinal.core.block.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.entity.DungeonWallTileEntity;
import software.bernie.geckolib.model.GeoModel;

public class DungeonWallBlockModel extends GeoModel<DungeonWallTileEntity> {
   public ResourceLocation getAnimationResource(DungeonWallTileEntity animatable) {
      return new ResourceLocation("sololeveling", "animations/dungeon_wall.animation.json");
   }

   public ResourceLocation getModelResource(DungeonWallTileEntity animatable) {
      return new ResourceLocation("sololeveling", "geo/dungeon_wall.geo.json");
   }

   public ResourceLocation getTextureResource(DungeonWallTileEntity entity) {
      return new ResourceLocation("sololeveling", "textures/block/dungeon_wall.png");
   }
}
