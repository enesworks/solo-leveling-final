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
public final class WeaponTooltipRenderTypes {
   private static ShaderInstance tooltipShader;

   private WeaponTooltipRenderTypes() {
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_weapon_tooltip"), DefaultVertexFormat.POSITION_TEX),
            shader -> tooltipShader = shader
         );
      } catch (Exception exception) {
         tooltipShader = null;
         SololevelingMod.LOGGER.warn("Failed to load the weapon tooltip shader; using static backgrounds.", exception);
      }
   }

   public static ShaderInstance get() {
      return tooltipShader;
   }
}
