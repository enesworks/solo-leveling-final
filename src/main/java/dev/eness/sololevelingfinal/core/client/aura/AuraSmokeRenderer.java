package dev.eness.sololevelingfinal.core.client.aura;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.client.renderer.shader.AuraSmokeRenderTypes;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DeferredWorldShaderRenderer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class AuraSmokeRenderer {
   private AuraSmokeRenderer() {
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_PARTICLES || event.getStage() == Stage.AFTER_LEVEL) {
         boolean renderStage = DeferredWorldShaderRenderer.isRenderStage(event, Stage.AFTER_PARTICLES);
         if (renderStage || event.getStage() == Stage.AFTER_PARTICLES) {
            Map<Integer, List<AuraSmokeField.Puff>> all = AuraSmokeField.puffs();
            if (!all.isEmpty()) {
               Minecraft minecraft = Minecraft.getInstance();
               if (minecraft.level != null && minecraft.player != null) {
                  int selfId = minecraft.player.getId();
                  boolean firstPerson = minecraft.options.getCameraType().isFirstPerson();
                  if (hasRenderablePuffs(all, selfId, firstPerson)) {
                     DeferredWorldShaderRenderer.requestDepthAtStage(event, Stage.AFTER_PARTICLES);
                     if (renderStage) {
                        if (DeferredWorldShaderRenderer.beginWorldPass(event)) {
                           try {
                              Camera camera = event.getCamera();
                              Vec3 cameraPosition = camera.getPosition();
                              Quaternionf billboard = minecraft.getEntityRenderDispatcher().cameraOrientation();
                              float partialTick = event.getPartialTick();
                              boolean custom = AuraSmokeRenderTypes.usesCustomShader();
                              PoseStack poseStack = DeferredWorldShaderRenderer.worldPoseStack(event);
                              BufferSource buffers = minecraft.renderBuffers().bufferSource();

                              for (int pass = 0; pass < 2; pass++) {
                                 boolean brightPass = pass == 1;

                                 for (Entry<Integer, List<AuraSmokeField.Puff>> entry : all.entrySet()) {
                                    if (!firstPerson || entry.getKey() != selfId) {
                                       for (AuraSmokeField.Puff puff : entry.getValue()) {
                                          if (puff.bright == brightPass) {
                                             renderPuff(poseStack, buffers, puff, cameraPosition, billboard, partialTick, custom);
                                          }
                                       }
                                    }
                                 }
                              }

                              buffers.endBatch();
                           } finally {
                              DeferredWorldShaderRenderer.endWorldPass();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean hasRenderablePuffs(Map<Integer, List<AuraSmokeField.Puff>> all, int selfId, boolean firstPerson) {
      for (Entry<Integer, List<AuraSmokeField.Puff>> entry : all.entrySet()) {
         if (!firstPerson || entry.getKey() != selfId) {
            for (AuraSmokeField.Puff puff : entry.getValue()) {
               if (puff.maxAge > 0.0 && puff.age < puff.maxAge && puff.baseAlpha >= 0.01F) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static void renderPuff(
      PoseStack poseStack, MultiBufferSource buffers, AuraSmokeField.Puff puff, Vec3 cameraPosition, Quaternionf billboard, float partialTick, boolean custom
   ) {
      double life = puff.age / puff.maxAge;
      if (!(life >= 1.0)) {
         float lifeFraction = (float)life;
         float fadeIn = Mth.clamp(lifeFraction / 0.14F, 0.0F, 1.0F);
         float fadeOut = puff.bright
            ? 1.0F - Mth.clamp((lifeFraction - 0.35F) / 0.65F, 0.0F, 1.0F)
            : 1.0F - Mth.clamp((lifeFraction - 0.5F) / 0.5F, 0.0F, 1.0F);
         float alpha = puff.baseAlpha * fadeIn * fadeOut;
         if (!(alpha < 0.01F)) {
            double x = Mth.lerp(partialTick, puff.ppx, puff.px) - cameraPosition.x;
            double y = Mth.lerp(partialTick, puff.ppy, puff.py) - cameraPosition.y;
            double z = Mth.lerp(partialTick, puff.ppz, puff.pz) - cameraPosition.z;
            float size = puff.size * (0.55F + 0.95F * lifeFraction);
            RenderType type = puff.bright ? AuraSmokeRenderTypes.ember(puff.texture) : AuraSmokeRenderTypes.smoke(puff.texture);
            VertexConsumer vertices = buffers.getBuffer(type);
            int red = custom ? puff.color >> 16 & 0xFF : 255;
            int green = custom ? puff.color >> 8 & 0xFF : 255;
            int blue = custom ? puff.color & 0xFF : 255;
            int packedAlpha = (int)(alpha * 255.0F);
            float uBase = custom ? puff.seed : 0.0F;
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(billboard);
            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            vertex(vertices, pose, normal, -size, -size, uBase, 1.0F, red, green, blue, packedAlpha);
            vertex(vertices, pose, normal, size, -size, uBase + 1.0F, 1.0F, red, green, blue, packedAlpha);
            vertex(vertices, pose, normal, size, size, uBase + 1.0F, 0.0F, red, green, blue, packedAlpha);
            vertex(vertices, pose, normal, -size, size, uBase, 0.0F, red, green, blue, packedAlpha);
            poseStack.popPose();
         }
      }
   }

   private static void vertex(
      VertexConsumer vertices, Matrix4f pose, Matrix3f normal, float x, float y, float u, float v, int red, int green, int blue, int alpha
   ) {
      vertices.vertex(pose, x, y, 0.0F)
         .color(red, green, blue, alpha)
         .uv(u, v)
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(240)
         .normal(normal, 0.0F, 0.0F, 1.0F)
         .endVertex();
   }
}
