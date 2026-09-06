package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.OverlayPanelWelcomeConditionProcedure;

@EventBusSubscriber(Dist.CLIENT)
public class OverlayPanelWelcomeOverlay {
   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void eventHandler(Pre event) {
      if (!legacyWelcomeOverlayDisabled()) {
         int w = event.getWindow().getGuiScaledWidth();
         int h = event.getWindow().getGuiScaledHeight();
         Level world = null;
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         Player entity = Minecraft.getInstance().player;
         if (entity != null) {
            world = entity.level();
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
         }

         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderSystem.setShaderColor(
            1.0F,
            1.0F,
            1.0F,
            (float)entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overlay_alpha_welcome
         );
         if (OverlayPanelWelcomeConditionProcedure.execute(entity)) {
            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/notice.png"), w / 2 + -8, h / 2 + -68, 0.0F, 0.0F, 16, 16, 16, 16);
            event.getGuiGraphics()
               .blit(
                  new ResourceLocation("sololeveling:textures/screens/panel_rework_empty_small.png"), w / 2 + -96, h / 2 + -103, 0.0F, 0.0F, 200, 160, 200, 160
               );
            event.getGuiGraphics()
               .drawString(
                  Minecraft.getInstance().font,
                  Component.translatable("gui.sololeveling.overlay_panel_welcome.label_sslssfwelcome_ssaplayer"),
                  w / 2 + -36,
                  h / 2 + -33,
                  -1,
                  false
               );
            event.getGuiGraphics()
               .drawString(
                  Minecraft.getInstance().font,
                  Component.translatable("gui.sololeveling.overlay_panel_welcome.label_sslnotice"),
                  w / 2 + -16,
                  h / 2 + -49,
                  -13210,
                  false
               );
         }

         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(
            1.0F,
            1.0F,
            1.0F,
            (float)entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overlay_alpha_welcome
         );
      }
   }

   private static boolean legacyWelcomeOverlayDisabled() {
      return true;
   }
}
