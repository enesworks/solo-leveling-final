package dev.eness.sololevelingfinal.core.client.renderer.shader;

import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.io.IOException;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
public class CrossStrikeRenderTypes extends RenderStateShard {
   private static ShaderInstance crossShader;

   private CrossStrikeRenderTypes(String name, Runnable setupState, Runnable clearState) {
      super(name, setupState, clearState);
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) throws IOException {
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_cross_strike"), WorldShaderVertexFormat.NEW_ENTITY),
         shader -> crossShader = shader
      );
   }

   public static RenderType cross(ResourceLocation texture) {
      if (crossShader == null) {
         return RenderType.entityTranslucentEmissive(texture);
      }

      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(() -> crossShader))
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setDepthTestState(NO_DEPTH_TEST)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create("cross_strike", WorldShaderVertexFormat.NEW_ENTITY, Mode.QUADS, 512, false, true, state);
   }
}
