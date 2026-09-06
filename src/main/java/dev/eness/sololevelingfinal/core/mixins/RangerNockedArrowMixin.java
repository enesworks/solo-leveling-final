package dev.eness.sololevelingfinal.core.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.client.renderer.RangerManaQuiverHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class RangerNockedArrowMixin {
   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1, shift = Shift.BEFORE))
   private void sololeveling$renderNockedManaArrow(
      ItemStack stack,
      ItemDisplayContext context,
      boolean leftHand,
      PoseStack poseStack,
      MultiBufferSource buffers,
      int packedLight,
      int packedOverlay,
      BakedModel model,
      CallbackInfo callback
   ) {
      RangerManaQuiverHandRenderer.renderNockedArrow(stack, context, leftHand, poseStack, buffers);
   }
}
