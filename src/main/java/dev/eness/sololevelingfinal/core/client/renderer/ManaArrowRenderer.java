package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.ManaArrowEntity;

public class ManaArrowRenderer extends EntityRenderer<ManaArrowEntity> {
   public ManaArrowRenderer(Context context) {
      super(context);
   }

   public void render(ManaArrowEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
      poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
      int stage = Math.max(1, entity.getRangerStage());
      float pulse = 0.92F + Mth.sin((entity.tickCount + partialTicks) * 0.26F) * 0.08F;
      ManaArrowVisual.render(poseStack, buffers, new Vec3(-0.48, 0.0, 0.0), new Vec3(0.52, 0.0, 0.0), stage, stage >= 3, pulse, 0.018, 0.18, 0.065, 0.2, 0.06);
      poseStack.popPose();
      super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
   }

   public ResourceLocation getTextureLocation(ManaArrowEntity entity) {
      return ManaArrowVisual.WHITE_TEXTURE;
   }
}
