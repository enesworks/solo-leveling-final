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
import dev.eness.sololevelingfinal.core.client.aura.PlayerAuraDefinition;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
public final class PlayerAuraRenderTypes extends RenderStateShard {
   private static final Map<ResourceLocation, RenderType> NORMAL_AURA_TYPES = new HashMap<>();
   private static final Map<ResourceLocation, RenderType> FALLBACK_TYPES = new HashMap<>();
   private static ShaderInstance auraShader;

   private PlayerAuraRenderTypes(String name, Runnable setupState, Runnable clearState) {
      super(name, setupState, clearState);
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) throws IOException {
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_player_aura"), WorldShaderVertexFormat.NEW_ENTITY),
         shader -> {
            auraShader = shader;
            NORMAL_AURA_TYPES.clear();
         }
      );
   }

   public static RenderType aura(PlayerAuraDefinition definition) {
      return normalAura(definition.fallbackTexture());
   }

   public static RenderType aura(ResourceLocation fallbackTexture) {
      return normalAura(fallbackTexture);
   }

   private static RenderType normalAura(ResourceLocation texture) {
      return shaderType(texture, auraShader, NORMAL_AURA_TYPES, "player_aura_", false);
   }

   private static RenderType shaderType(
      ResourceLocation texture, ShaderInstance shader, Map<ResourceLocation, RenderType> cache, String namePrefix, boolean additive
   ) {
      return shader == null
         ? FALLBACK_TYPES.computeIfAbsent(texture, RenderType::entityTranslucentEmissive)
         : cache.computeIfAbsent(
            texture,
            resourceLocation -> {
               CompositeState state = CompositeState.builder()
                  .setShaderState(new ShaderStateShard(() -> shader))
                  .setTextureState(new TextureStateShard(resourceLocation, false, false))
                  .setTransparencyState(additive ? ADDITIVE_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
                  .setDepthTestState(LEQUAL_DEPTH_TEST)
                  .setCullState(NO_CULL)
                  .setLightmapState(LIGHTMAP)
                  .setOverlayState(OVERLAY)
                  .setWriteMaskState(COLOR_WRITE)
                  .createCompositeState(false);
               return RenderType.create(
                  namePrefix + resourceLocation.getPath().replace('/', '_'), WorldShaderVertexFormat.NEW_ENTITY, Mode.QUADS, 2048, false, true, state
               );
            }
         );
   }
}
