package dev.eness.sololevelingfinal.core.block.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.display.HunterRankEvaluatorDisplayItem;
import dev.eness.sololevelingfinal.core.block.model.HunterRankEvaluatorDisplayModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HunterRankEvaluatorDisplayItemRenderer extends GeoItemRenderer<HunterRankEvaluatorDisplayItem> {
   public HunterRankEvaluatorDisplayItemRenderer() {
      super(new HunterRankEvaluatorDisplayModel());
   }

   public RenderType getRenderType(HunterRankEvaluatorDisplayItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }
}
