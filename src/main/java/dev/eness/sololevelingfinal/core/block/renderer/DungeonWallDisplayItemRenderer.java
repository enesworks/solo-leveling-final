package dev.eness.sololevelingfinal.core.block.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.display.DungeonWallDisplayItem;
import dev.eness.sololevelingfinal.core.block.model.DungeonWallDisplayModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DungeonWallDisplayItemRenderer extends GeoItemRenderer<DungeonWallDisplayItem> {
   public DungeonWallDisplayItemRenderer() {
      super(new DungeonWallDisplayModel());
   }

   public RenderType getRenderType(DungeonWallDisplayItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }
}
