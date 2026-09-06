package dev.eness.sololevelingfinal.core.block.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.entity.DungeonWallTileEntity;
import dev.eness.sololevelingfinal.core.block.model.DungeonWallBlockModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DungeonWallTileRenderer extends GeoBlockRenderer<DungeonWallTileEntity> {
   public DungeonWallTileRenderer() {
      super(new DungeonWallBlockModel());
   }

   public RenderType getRenderType(DungeonWallTileEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }
}
