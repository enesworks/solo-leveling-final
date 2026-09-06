package dev.eness.sololevelingfinal.core.client.renderer.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.SololevelingMod;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
public class ShadowSummonBackgroundRenderTypes {
   private static ShaderInstance shadowSummonBackgroundShader;

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(
               event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_shadow_summon_background"), DefaultVertexFormat.POSITION_TEX
            ),
            shader -> shadowSummonBackgroundShader = shader
         );
      } catch (Exception e) {
         shadowSummonBackgroundShader = null;
         SololevelingMod.LOGGER.warn("Failed to load Shadow Summon GUI background shader; using Java fallback.", e);
      }
   }

   public static ShaderInstance get() {
      return shadowSummonBackgroundShader;
   }
}
