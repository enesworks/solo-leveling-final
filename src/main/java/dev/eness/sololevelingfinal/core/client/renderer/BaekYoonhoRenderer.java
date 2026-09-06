package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.BaekYoonhoEntity;

public class BaekYoonhoRenderer extends HumanoidMobRenderer<BaekYoonhoEntity, HumanoidModel<BaekYoonhoEntity>> {
   public BaekYoonhoRenderer(Context context) {
      super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
      this.addLayer(
         new HumanoidArmorLayer<>(
            this,
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
            context.getModelManager()
         )
      );
   }

   protected void scale(BaekYoonhoEntity entity, PoseStack poseStack, float f) {
      poseStack.scale(1.2F, 1.2F, 1.2F);
   }

   public ResourceLocation getTextureLocation(BaekYoonhoEntity entity) {
      return new ResourceLocation("sololeveling:textures/entities/baekyoonho.png");
   }
}
