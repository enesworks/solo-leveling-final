package dev.eness.sololevelingfinal.core.block.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.display.InstanceDungeonKeyLoggerDisplayItem;
import dev.eness.sololevelingfinal.core.block.model.InstanceDungeonKeyLoggerDisplayModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class InstanceDungeonKeyLoggerDisplayItemRenderer extends GeoItemRenderer<InstanceDungeonKeyLoggerDisplayItem> {
   public InstanceDungeonKeyLoggerDisplayItemRenderer() {
      super(new InstanceDungeonKeyLoggerDisplayModel());
   }

   public RenderType getRenderType(InstanceDungeonKeyLoggerDisplayItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }
}
