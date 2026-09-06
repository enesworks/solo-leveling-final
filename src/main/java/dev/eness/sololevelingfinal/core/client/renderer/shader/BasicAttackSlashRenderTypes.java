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
public class BasicAttackSlashRenderTypes extends RenderStateShard {
   private static ShaderInstance fistShader;
   private static ShaderInstance swordShader;
   private static ShaderInstance daggerShader;
   private static ShaderInstance dualDaggerShader;

   private BasicAttackSlashRenderTypes(String name, Runnable setupState, Runnable clearState) {
      super(name, setupState, clearState);
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) throws IOException {
      event.registerShader(
         new ShaderInstance(
            event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_basic_slash_fist"), WorldShaderVertexFormat.NEW_ENTITY
         ),
         shader -> fistShader = shader
      );
      event.registerShader(
         new ShaderInstance(
            event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_basic_slash_sword"), WorldShaderVertexFormat.NEW_ENTITY
         ),
         shader -> swordShader = shader
      );
      event.registerShader(
         new ShaderInstance(
            event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_basic_slash_dagger"), WorldShaderVertexFormat.NEW_ENTITY
         ),
         shader -> daggerShader = shader
      );
      event.registerShader(
         new ShaderInstance(
            event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_basic_slash_dual_dagger"), WorldShaderVertexFormat.NEW_ENTITY
         ),
         shader -> dualDaggerShader = shader
      );
   }

   public static RenderType slash(int style, ResourceLocation texture) {
      ShaderInstance shader = switch (style) {
         case 0 -> fistShader;
         default -> swordShader;
         case 2 -> daggerShader;
         case 3 -> dualDaggerShader;
      };
      if (shader == null) {
         return RenderType.entityTranslucentEmissive(texture);
      }

      String name = switch (style) {
         case 0 -> "basic_slash_fist";
         default -> "basic_slash_sword";
         case 2 -> "basic_slash_dagger";
         case 3 -> "basic_slash_dual_dagger";
      };
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(() -> shader))
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setDepthTestState(NO_DEPTH_TEST)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create(name, WorldShaderVertexFormat.NEW_ENTITY, Mode.QUADS, 256, false, true, state);
   }
}
