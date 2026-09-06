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
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

public class HunterRenderer extends HumanoidMobRenderer<HunterEntity, HumanoidModel<HunterEntity>> {
   private static boolean variantVisible(HunterEntity entity, EntityDataAccessor<Integer> accessor, int value) {
      return entity.getEntityData().get(accessor) == value && !entity.hasEffect(MobEffects.INVISIBILITY);
   }

   private static boolean variantVisibleNoInvis(HunterEntity entity, EntityDataAccessor<Integer> accessor, int value) {
      return entity.getEntityData().get(accessor) == value;
   }

   private void addVariantLayer(Context context, String texturePath, final EntityDataAccessor<Integer> accessor, final int value) {
      final ResourceLocation tex = new ResourceLocation("sololeveling:" + texturePath);
      this.addLayer(
         new RenderLayer<HunterEntity, HumanoidModel<HunterEntity>>(this) {
            public void render(
               PoseStack poseStack,
               MultiBufferSource bufferSource,
               int light,
               HunterEntity entity,
               float limbSwing,
               float limbSwingAmount,
               float partialTicks,
               float ageInTicks,
               float netHeadYaw,
               float headPitch
            ) {
               if (HunterRenderer.variantVisible(entity, accessor, value)) {
                  VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
                  this.getParentModel().renderToBuffer(poseStack, vc, 15728640, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
               }
            }
         }
      );
   }

   private void addVariantLayerNoInvis(Context context, String texturePath, final EntityDataAccessor<Integer> accessor, final int value) {
      final ResourceLocation tex = new ResourceLocation("sololeveling:" + texturePath);
      this.addLayer(
         new RenderLayer<HunterEntity, HumanoidModel<HunterEntity>>(this) {
            public void render(
               PoseStack poseStack,
               MultiBufferSource bufferSource,
               int light,
               HunterEntity entity,
               float limbSwing,
               float limbSwingAmount,
               float partialTicks,
               float ageInTicks,
               float netHeadYaw,
               float headPitch
            ) {
               if (HunterRenderer.variantVisibleNoInvis(entity, accessor, value)) {
                  VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
                  this.getParentModel().renderToBuffer(poseStack, vc, 15728640, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
               }
            }
         }
      );
   }

   public HunterRenderer(Context context) {
      super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
      this.addLayer(
         new HumanoidArmorLayer<>(
            this,
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
            context.getModelManager()
         )
      );

      for (int i = 1; i <= 8; i++) {
         this.addVariantLayerNoInvis(context, "textures/entities/eyes_var" + i + ".png", HunterEntity.DATA_Eyes, i);
      }

      for (int i = 1; i <= 8; i++) {
         this.addVariantLayer(context, "textures/entities/mhair_var" + i + ".png", HunterEntity.DATA_Hair, i);
      }

      for (int i = 1; i <= 4; i++) {
         this.addVariantLayer(context, "textures/entities/top_in_var" + i + ".png", HunterEntity.DATA_TopIn, i);
      }

      for (int i = 1; i <= 15; i++) {
         this.addVariantLayer(context, "textures/entities/top_out_var" + i + ".png", HunterEntity.DATA_TopOut, i);
      }

      for (int i = 1; i <= 5; i++) {
         this.addVariantLayer(context, "textures/entities/bottom_var" + i + ".png", HunterEntity.DATA_Bottom, i);
      }

      for (int i = 1; i <= 4; i++) {
         this.addVariantLayer(context, "textures/entities/foot_var" + i + ".png", HunterEntity.DATA_Foot, i);
      }

      this.addVariantLayer(context, "textures/entities/mouth_var1.png", HunterEntity.DATA_Mouth, 1);
      this.addVariantLayer(context, "textures/entities/mouth_var2.png", HunterEntity.DATA_EyeBs, 2);
      this.addVariantLayer(context, "textures/entities/eyeb_var1.png", HunterEntity.DATA_EyeBs, 1);
      this.addVariantLayer(context, "textures/entities/eyeb_var2.png", HunterEntity.DATA_EyeBs, 2);
   }

   public ResourceLocation getTextureLocation(HunterEntity entity) {
      return new ResourceLocation("sololeveling:textures/entities/human_base.png");
   }
}
