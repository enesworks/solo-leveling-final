package dev.eness.sololevelingfinal.core.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;

public class DKnight1Renderer extends HumanoidMobRenderer<DKnight1Entity, HumanoidModel<DKnight1Entity>> {
   public DKnight1Renderer(Context context) {
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

   public ResourceLocation getTextureLocation(DKnight1Entity entity) {
      return new ResourceLocation("sololeveling:textures/entities/skin_dark_knight.png");
   }
}
