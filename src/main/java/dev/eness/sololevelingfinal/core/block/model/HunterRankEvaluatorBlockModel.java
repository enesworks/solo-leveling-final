package dev.eness.sololevelingfinal.core.block.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.block.entity.HunterRankEvaluatorTileEntity;
import software.bernie.geckolib.model.GeoModel;

public class HunterRankEvaluatorBlockModel extends GeoModel<HunterRankEvaluatorTileEntity> {
   public ResourceLocation getAnimationResource(HunterRankEvaluatorTileEntity animatable) {
      return new ResourceLocation("sololeveling", "animations/evaluator.animation.json");
   }

   public ResourceLocation getModelResource(HunterRankEvaluatorTileEntity animatable) {
      return new ResourceLocation("sololeveling", "geo/evaluator.geo.json");
   }

   public ResourceLocation getTextureResource(HunterRankEvaluatorTileEntity entity) {
      return new ResourceLocation("sololeveling", "textures/block/altartexture1.png");
   }
}
