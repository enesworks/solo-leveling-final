package dev.eness.sololevelingfinal.core.client.renderer.shader;

import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
public final class AuraSmokeRenderTypes extends RenderStateShard {
   private static final Map<ResourceLocation, RenderType> SMOKE_TYPES = new HashMap<>();
   private static final Map<ResourceLocation, RenderType> EMBER_TYPES = new HashMap<>();
   private static final Map<ResourceLocation, RenderType> FALLBACK_TYPES = new HashMap<>();
   private static ShaderInstance smokeShader;
   private static ShaderInstance emberShader;

   private AuraSmokeRenderTypes(String name, Runnable setupState, Runnable clearState) {
      super(name, setupState, clearState);
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) throws IOException {
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "aura_smoke"), WorldShaderVertexFormat.NEW_ENTITY), shader -> {
            smokeShader = shader;
            SMOKE_TYPES.clear();
         }
      );
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "aura_smoke_add"), WorldShaderVertexFormat.NEW_ENTITY),
         shader -> {
            emberShader = shader;
            EMBER_TYPES.clear();
         }
      );
   }

   public static boolean usesCustomShader() {
      return smokeShader != null && emberShader != null;
   }

   public static RenderType smoke(ResourceLocation texture) {
      return !usesCustomShader()
         ? fallback(texture)
         : SMOKE_TYPES.computeIfAbsent(texture, resourceLocation -> build("aura_smoke_", resourceLocation, smokeShader, false));
   }

   public static RenderType ember(ResourceLocation texture) {
      return !usesCustomShader()
         ? fallback(texture)
         : EMBER_TYPES.computeIfAbsent(texture, resourceLocation -> build("aura_ember_", resourceLocation, emberShader, true));
   }

   private static RenderType fallback(ResourceLocation texture) {
      return FALLBACK_TYPES.computeIfAbsent(texture, RenderType::entityTranslucentEmissive);
   }

   private static RenderType build(String namePrefix, ResourceLocation texture, ShaderInstance shader, boolean additive) {
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(() -> shader))
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(additive ? ADDITIVE_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create(namePrefix + texture.getPath().replace('/', '_'), WorldShaderVertexFormat.NEW_ENTITY, Mode.QUADS, 2048, false, true, state);
   }
}
