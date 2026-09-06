package dev.eness.sololevelingfinal.core.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;

public class DKnight2Renderer extends HumanoidMobRenderer<DKnight2Entity, HumanoidModel<DKnight2Entity>> {
   public DKnight2Renderer(Context context) {
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

   public ResourceLocation getTextureLocation(DKnight2Entity entity) {
      return new ResourceLocation("sololeveling:textures/entities/knight_skin.png");
   }
}
