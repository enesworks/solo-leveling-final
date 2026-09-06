package dev.eness.sololevelingfinal.core.client.renderer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.EsilRadiruEntity;

public final class EsilRadiruRenderer extends HumanoidMobRenderer<EsilRadiruEntity, PlayerModel<EsilRadiruEntity>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("sololeveling", "textures/entities/esil_radiru.png");

   public EsilRadiruRenderer(Context context) {
      super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
   }

   public ResourceLocation getTextureLocation(EsilRadiruEntity entity) {
      return TEXTURE;
   }
}
