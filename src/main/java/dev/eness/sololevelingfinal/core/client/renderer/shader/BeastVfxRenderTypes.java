package dev.eness.sololevelingfinal.core.client.renderer.shader;

import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.io.IOException;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
public final class BeastVfxRenderTypes extends RenderStateShard {
   private static final ResourceLocation FALLBACK = new ResourceLocation("sololeveling", "textures/particle/slashgood1.png");
   private static ShaderInstance additiveShader;
   private static ShaderInstance surfaceShader;
   private static final RenderType ADDITIVE = create("beast_vfx_additive", () -> additiveShader, ADDITIVE_TRANSPARENCY, COLOR_WRITE);
   private static final RenderType SURFACE = create("beast_vfx_surface", () -> surfaceShader, TRANSLUCENT_TRANSPARENCY, COLOR_WRITE);

   private BeastVfxRenderTypes(String name, Runnable setup, Runnable clear) {
      super(name, setup, clear);
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) throws IOException {
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_beast_vfx"), WorldShaderVertexFormat.NEW_ENTITY),
         loaded -> additiveShader = loaded
      );
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_beast_surface"), WorldShaderVertexFormat.NEW_ENTITY),
         loaded -> surfaceShader = loaded
      );
   }

   public static RenderType additive() {
      return additiveShader == null ? RenderType.entityTranslucentEmissive(FALLBACK) : ADDITIVE;
   }

   public static RenderType surface() {
      return surfaceShader == null ? RenderType.entityTranslucent(FALLBACK) : SURFACE;
   }

   private static RenderType create(String name, Supplier<ShaderInstance> shaderSupplier, TransparencyStateShard transparency, WriteMaskStateShard writeMask) {
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(shaderSupplier))
         .setTextureState(new TextureStateShard(FALLBACK, false, false))
         .setTransparencyState(transparency)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setWriteMaskState(writeMask)
         .createCompositeState(false);
      return RenderType.create(name, WorldShaderVertexFormat.NEW_ENTITY, Mode.QUADS, 1024, false, true, state);
   }
}
