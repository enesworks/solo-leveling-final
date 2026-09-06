package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.model.Modelinv;
import dev.eness.sololevelingfinal.core.entity.DetectEyeInvEntity;

public class DetectEyeInvRenderer extends MobRenderer<DetectEyeInvEntity, Modelinv<DetectEyeInvEntity>> {
   public DetectEyeInvRenderer(Context context) {
      super(context, new Modelinv<>(context.bakeLayer(Modelinv.LAYER_LOCATION)), 0.1F);
   }

   protected void scale(DetectEyeInvEntity entity, PoseStack poseStack, float f) {
      poseStack.scale(0.1F, 0.1F, 0.1F);
   }

   public ResourceLocation getTextureLocation(DetectEyeInvEntity entity) {
      return new ResourceLocation("sololeveling:textures/entities/invistext.png");
   }
}
