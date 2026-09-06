package dev.eness.sololevelingfinal.core.block.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.display.DungeonWallDisplayItem;
import software.bernie.geckolib.model.GeoModel;

public class DungeonWallDisplayModel extends GeoModel<DungeonWallDisplayItem> {
   public ResourceLocation getAnimationResource(DungeonWallDisplayItem animatable) {
      return new ResourceLocation("sololeveling", "animations/dungeon_wall.animation.json");
   }

   public ResourceLocation getModelResource(DungeonWallDisplayItem animatable) {
      return new ResourceLocation("sololeveling", "geo/dungeon_wall.geo.json");
   }

   public ResourceLocation getTextureResource(DungeonWallDisplayItem entity) {
      return new ResourceLocation("sololeveling", "textures/block/dungeon_wall.png");
   }
}
