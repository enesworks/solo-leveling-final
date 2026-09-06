package dev.eness.sololevelingfinal.core.block.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.display.InstanceCoverDisplayItem;
import dev.eness.sololevelingfinal.core.block.model.InstanceCoverDisplayModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class InstanceCoverDisplayItemRenderer extends GeoItemRenderer<InstanceCoverDisplayItem> {
   public InstanceCoverDisplayItemRenderer() {
      super(new InstanceCoverDisplayModel());
   }

   public RenderType getRenderType(InstanceCoverDisplayItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }
}
