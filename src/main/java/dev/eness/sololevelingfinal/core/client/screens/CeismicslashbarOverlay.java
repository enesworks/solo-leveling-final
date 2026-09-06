package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.procedures.CeismicslashbarDisplayOverlayIngameProcedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge0Procedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge1Procedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge2Procedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge3Procedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge4Procedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge5Procedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge6Procedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge7Procedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge8Procedure;
import dev.eness.sololevelingfinal.core.procedures.Kamishcharge9Procedure;

@EventBusSubscriber(Dist.CLIENT)
public class CeismicslashbarOverlay {
   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void eventHandler(Pre event) {
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

      boolean visible = CeismicslashbarDisplayOverlayIngameProcedure.execute(entity);
      if (visible) {
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         if (visible) {
            if (Kamishcharge0Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_1.png"), w / 2 + -31, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }

            if (Kamishcharge1Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_2.png"), w / 2 + -31, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }

            if (Kamishcharge2Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_3.png"), w / 2 + -31, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }

            if (Kamishcharge3Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_4.png"), w / 2 + -32, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }

            if (Kamishcharge4Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_5.png"), w / 2 + -32, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }

            if (Kamishcharge5Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_6.png"), w / 2 + -33, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }

            if (Kamishcharge6Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_7.png"), w / 2 + -33, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }

            if (Kamishcharge7Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_8.png"), w / 2 + -33, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }

            if (Kamishcharge8Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_9.png"), w / 2 + -33, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }

            if (Kamishcharge9Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/progress_10.png"), w / 2 + -33, h / 2 + -115, 0.0F, 0.0F, 63, 8, 63, 8);
            }
         }

         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
