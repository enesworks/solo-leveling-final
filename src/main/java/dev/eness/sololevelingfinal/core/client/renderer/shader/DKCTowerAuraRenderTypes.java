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
public class DKCTowerAuraRenderTypes extends RenderStateShard {
   private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("sololeveling:textures/particle/mana_red.png");
   private static ShaderInstance towerAuraShader;
   private static RenderType towerAura;
   private static RenderType fallbackTowerAura;

   private DKCTowerAuraRenderTypes(String name, Runnable setupState, Runnable clearState) {
      super(name, setupState, clearState);
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) throws IOException {
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_dkc_tower_aura"), WorldShaderVertexFormat.NEW_ENTITY),
         shader -> towerAuraShader = shader
      );
   }

   public static RenderType towerAura() {
      if (towerAuraShader == null) {
         if (fallbackTowerAura == null) {
            fallbackTowerAura = create("dkc_tower_aura_fallback", RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER);
         }

         return fallbackTowerAura;
      } else {
         if (towerAura == null) {
            towerAura = create("dkc_tower_aura", new ShaderStateShard(() -> towerAuraShader));
         }

         return towerAura;
      }
   }

   private static RenderType create(String name, ShaderStateShard shaderState) {
      CompositeState state = CompositeState.builder()
         .setShaderState(shaderState)
         .setTextureState(new TextureStateShard(FALLBACK_TEXTURE, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create(name, WorldShaderVertexFormat.NEW_ENTITY, Mode.QUADS, 16384, false, true, state);
   }
}
