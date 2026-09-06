package dev.eness.sololevelingfinal.core.client.renderer.shader;

import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.client.renderer.AntaresVfxClientState;
import org.slf4j.Logger;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
public final class AntaresVfxRenderTypes extends RenderStateShard {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final ResourceLocation MATERIAL_TEXTURE = new ResourceLocation("minecraft", "textures/misc/white.png");
   private static ShaderInstance surfaceShader;
   private static ShaderInstance emissiveShader;
   private static final RenderType SURFACE = create("antares_vfx_surface", () -> surfaceShader, TRANSLUCENT_TRANSPARENCY);
   private static final RenderType EMISSIVE = create("antares_vfx_emissive", () -> emissiveShader, ADDITIVE_TRANSPARENCY);

   private AntaresVfxRenderTypes(String name, Runnable setup, Runnable clear) {
      super(name, setup, clear);
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) {
      surfaceShader = null;
      emissiveShader = null;
      AntaresVfxClientState.onResourceReload();

      try {
         event.registerShader(
            new ShaderInstance(
               event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_antares_vfx_surface"), WorldShaderVertexFormat.NEW_ENTITY
            ),
            loaded -> surfaceShader = loaded
         );
      } catch (IOException exception) {
         LOGGER.warn("[SoloLeveling] Antares surface shader failed to load; using the vanilla fallback.", exception);
      }

      try {
         event.registerShader(
            new ShaderInstance(
               event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_antares_vfx_emissive"), WorldShaderVertexFormat.NEW_ENTITY
            ),
            loaded -> emissiveShader = loaded
         );
      } catch (IOException exception) {
         LOGGER.warn("[SoloLeveling] Antares emissive shader failed to load; using the vanilla fallback.", exception);
      }
   }

   public static RenderType surface() {
      return surfaceShader == null ? RenderType.entityTranslucent(MATERIAL_TEXTURE) : SURFACE;
   }

   public static RenderType emissive() {
      return emissiveShader == null ? RenderType.entityTranslucentEmissive(MATERIAL_TEXTURE) : EMISSIVE;
   }

   private static RenderType create(String name, Supplier<ShaderInstance> shader, TransparencyStateShard transparency) {
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(shader))
         .setTextureState(new TextureStateShard(MATERIAL_TEXTURE, false, false))
         .setTransparencyState(transparency)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create(name, WorldShaderVertexFormat.NEW_ENTITY, Mode.QUADS, 24576, false, true, state);
   }
}
