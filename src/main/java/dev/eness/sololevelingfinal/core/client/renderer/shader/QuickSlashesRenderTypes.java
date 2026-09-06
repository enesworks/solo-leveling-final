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
public class QuickSlashesRenderTypes extends RenderStateShard {
   private static ShaderInstance quickSlashesShader;

   private QuickSlashesRenderTypes(String name, Runnable setupState, Runnable clearState) {
      super(name, setupState, clearState);
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) throws IOException {
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_quick_slashes"), WorldShaderVertexFormat.NEW_ENTITY),
         shader -> quickSlashesShader = shader
      );
   }

   public static RenderType slashes(ResourceLocation texture) {
      if (quickSlashesShader == null) {
         return RenderType.entityTranslucentEmissive(texture);
      }

      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(() -> quickSlashesShader))
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setDepthTestState(NO_DEPTH_TEST)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create("quick_slashes", WorldShaderVertexFormat.NEW_ENTITY, Mode.QUADS, 512, false, true, state);
   }
}
