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
public final class WhiteFlameVfxRenderTypes extends RenderStateShard {
   private static ShaderInstance shader;

   private WhiteFlameVfxRenderTypes(String name, Runnable setup, Runnable clear) {
      super(name, setup, clear);
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) throws IOException {
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_white_flame_vfx"), WorldShaderVertexFormat.NEW_ENTITY),
         loaded -> shader = loaded
      );
   }

   public static RenderType effect(ResourceLocation fallback) {
      if (shader == null) {
         return RenderType.entityTranslucentEmissive(fallback);
      }

      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(() -> shader))
         .setTextureState(new TextureStateShard(fallback, false, false))
         .setTransparencyState(ADDITIVE_TRANSPARENCY)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create("white_flame_vfx", WorldShaderVertexFormat.NEW_ENTITY, Mode.QUADS, 512, false, true, state);
   }
}
