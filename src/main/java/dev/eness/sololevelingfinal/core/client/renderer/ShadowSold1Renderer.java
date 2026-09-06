package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ShadowSold1Entity;

public class ShadowSold1Renderer extends HumanoidMobRenderer<ShadowSold1Entity, HumanoidModel<ShadowSold1Entity>> {
   public ShadowSold1Renderer(Context context) {
      super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
      this.addLayer(
         new HumanoidArmorLayer<>(
            this,
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
            context.getModelManager()
         )
      );
      this.addLayer(
         new RenderLayer<ShadowSold1Entity, HumanoidModel<ShadowSold1Entity>>(this) {
            final ResourceLocation LAYER_TEXTURE = new ResourceLocation("sololeveling:textures/entities/shadowsoldalt.png");

            public void render(
               PoseStack poseStack,
               MultiBufferSource bufferSource,
               int light,
               ShadowSold1Entity entity,
               float limbSwing,
               float limbSwingAmount,
               float partialTicks,
               float ageInTicks,
               float netHeadYaw,
               float headPitch
            ) {
               VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.eyes(this.LAYER_TEXTURE));
               this.getParentModel()
                  .renderToBuffer(poseStack, vertexConsumer, 15728640, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      );
   }

   public ResourceLocation getTextureLocation(ShadowSold1Entity entity) {
      return new ResourceLocation("sololeveling:textures/entities/shadowsoldalt.png");
   }
}
