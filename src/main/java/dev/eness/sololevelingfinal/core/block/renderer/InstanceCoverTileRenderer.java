package dev.eness.sololevelingfinal.core.block.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.entity.InstanceCoverTileEntity;
import dev.eness.sololevelingfinal.core.block.model.InstanceCoverBlockModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class InstanceCoverTileRenderer extends GeoBlockRenderer<InstanceCoverTileEntity> {
   public InstanceCoverTileRenderer() {
      super(new InstanceCoverBlockModel());
   }

   public RenderType getRenderType(InstanceCoverTileEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }
}
