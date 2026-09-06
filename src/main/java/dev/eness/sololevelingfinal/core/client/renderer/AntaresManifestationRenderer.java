package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent.Post;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.client.aura.ClientPlayerAuraManager;
import dev.eness.sololevelingfinal.core.client.aura.PlayerAuraRegistry;
import dev.eness.sololevelingfinal.core.client.renderer.shader.AntaresVfxRenderTypes;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DeferredWorldShaderRenderer;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class AntaresManifestationRenderer {
   private AntaresManifestationRenderer() {
   }

   @SubscribeEvent
   public static void renderManifestation(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      Player player = event.getEntity();
      if (minecraft.level != null && !player.isSpectator() && (player != minecraft.player || !minecraft.options.getCameraType().isFirstPerson())) {
         float envelope = 0.0F;
         long now = minecraft.level.getGameTime();

         for (ClientPlayerAuraManager.AuraInstance aura : ClientPlayerAuraManager.activeFor(player.getId())) {
            if (PlayerAuraRegistry.ANTARES_MANIFESTATION.id().equals(aura.auraId())) {
               envelope = Math.max(envelope, aura.envelope(event.getPartialTick(), now) * aura.intensity());
            }
         }

         if (!(envelope <= 0.02F)) {
            float age = player.tickCount + event.getPartialTick();
            float spread = Mth.clamp(envelope, 0.12F, 1.0F) * (0.97F + Mth.sin(age * 0.12F) * 0.035F);
            int light = LevelRenderer.getLightColor(minecraft.level, player.blockPosition());
            VertexConsumer surface = DeferredWorldShaderRenderer.buffer(event.getMultiBufferSource(), AntaresVfxRenderTypes.surface());
            AntaresVfxRenderer.drawWingPair(
               surface,
               event.getPoseStack().last(),
               new AntaresVfxRenderer.FrameBudget(160),
               player.getYRot(),
               spread,
               Mth.clamp(envelope, 0.0F, 1.0F),
               AntaresVfxRenderer.Pass.SURFACE,
               light
            );
            if (minecraft.options.particles().get() != ParticleStatus.MINIMAL) {
               VertexConsumer emissive = DeferredWorldShaderRenderer.buffer(event.getMultiBufferSource(), AntaresVfxRenderTypes.emissive());
               AntaresVfxRenderer.drawWingPair(
                  emissive,
                  event.getPoseStack().last(),
                  new AntaresVfxRenderer.FrameBudget(160),
                  player.getYRot(),
                  spread,
                  Mth.clamp(envelope, 0.0F, 1.0F),
                  AntaresVfxRenderer.Pass.EMISSIVE,
                  light
               );
            }
         }
      }
   }
}
