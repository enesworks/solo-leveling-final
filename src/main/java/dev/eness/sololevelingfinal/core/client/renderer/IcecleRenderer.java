package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.model.Modelicecle;
import dev.eness.sololevelingfinal.core.entity.IcecleEntity;

public class IcecleRenderer extends MobRenderer<IcecleEntity, Modelicecle<IcecleEntity>> {
   public IcecleRenderer(Context context) {
      super(context, new Modelicecle<>(context.bakeLayer(Modelicecle.LAYER_LOCATION)), 0.5F);
   }

   protected void scale(IcecleEntity entity, PoseStack poseStack, float f) {
      poseStack.scale(2.0F, 2.0F, 2.0F);
   }

   public ResourceLocation getTextureLocation(IcecleEntity entity) {
      return new ResourceLocation("sololeveling:textures/entities/icecle.png");
   }
}
