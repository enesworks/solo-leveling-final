package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.model.Modelshadowsoul;
import dev.eness.sololevelingfinal.core.entity.ShadowSoulEntity;

public class ShadowSoulRenderer extends MobRenderer<ShadowSoulEntity, Modelshadowsoul<ShadowSoulEntity>> {
   public ShadowSoulRenderer(Context context) {
      super(context, new Modelshadowsoul<>(context.bakeLayer(Modelshadowsoul.LAYER_LOCATION)), 0.5F);
   }

   protected void scale(ShadowSoulEntity entity, PoseStack poseStack, float f) {
      poseStack.scale(0.5F, 0.5F, 0.5F);
   }

   public ResourceLocation getTextureLocation(ShadowSoulEntity entity) {
      return new ResourceLocation("sololeveling:textures/entities/soultext.png");
   }

   protected boolean isBodyVisible(ShadowSoulEntity entity) {
      return false;
   }

   protected boolean isShaking(ShadowSoulEntity entity) {
      return true;
   }
}
