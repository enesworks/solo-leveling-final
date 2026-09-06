package dev.eness.sololevelingfinal.core.block.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.display.HunterRankEvaluatorDisplayItem;
import software.bernie.geckolib.model.GeoModel;

public class HunterRankEvaluatorDisplayModel extends GeoModel<HunterRankEvaluatorDisplayItem> {
   public ResourceLocation getAnimationResource(HunterRankEvaluatorDisplayItem animatable) {
      return new ResourceLocation("sololeveling", "animations/evaluator.animation.json");
   }

   public ResourceLocation getModelResource(HunterRankEvaluatorDisplayItem animatable) {
      return new ResourceLocation("sololeveling", "geo/evaluator.geo.json");
   }

   public ResourceLocation getTextureResource(HunterRankEvaluatorDisplayItem entity) {
      return new ResourceLocation("sololeveling", "textures/block/altartexture1.png");
   }
}
