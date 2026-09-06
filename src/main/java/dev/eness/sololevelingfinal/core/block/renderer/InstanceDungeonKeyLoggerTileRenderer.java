package dev.eness.sololevelingfinal.core.block.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.entity.InstanceDungeonKeyLoggerTileEntity;
import dev.eness.sololevelingfinal.core.block.model.InstanceDungeonKeyLoggerBlockModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class InstanceDungeonKeyLoggerTileRenderer extends GeoBlockRenderer<InstanceDungeonKeyLoggerTileEntity> {
   public InstanceDungeonKeyLoggerTileRenderer() {
      super(new InstanceDungeonKeyLoggerBlockModel());
   }

   public RenderType getRenderType(InstanceDungeonKeyLoggerTileEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }
}
