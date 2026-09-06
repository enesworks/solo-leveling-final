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
public final class DkcTowerBackgroundRenderTypes {
   private static ShaderInstance shader;

   private DkcTowerBackgroundRenderTypes() {
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) {
      shader = null;

      try {
         event.registerShader(
            new ShaderInstance(
               event.getResourceProvider(), new ResourceLocation("sololeveling", "rendertype_dkc_tower_background"), DefaultVertexFormat.POSITION_TEX
            ),
            loaded -> shader = loaded
         );
      } catch (Exception exception) {
         SololevelingMod.LOGGER.warn("Failed to load the DKC tower background shader; using the screen fallback.", exception);
      }
   }

   public static ShaderInstance get() {
      return shader;
   }
}
