package dev.eness.sololevelingfinal.core.block.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.entity.HunterRankEvaluatorTileEntity;
import dev.eness.sololevelingfinal.core.block.model.HunterRankEvaluatorBlockModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class HunterRankEvaluatorTileRenderer extends GeoBlockRenderer<HunterRankEvaluatorTileEntity> {
   public HunterRankEvaluatorTileRenderer() {
      super(new HunterRankEvaluatorBlockModel());
   }

   public RenderType getRenderType(HunterRankEvaluatorTileEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }
}
