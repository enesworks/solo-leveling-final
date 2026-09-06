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
import dev.eness.sololevelingfinal.core.procedures.ParalyzeViewConditionProcedure;

@EventBusSubscriber(Dist.CLIENT)
public class UsedParalyzeOverlay {
   @SubscribeEvent(priority = EventPriority.HIGH)
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

      boolean visible = ParalyzeViewConditionProcedure.execute(entity);
      if (visible) {
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         if (visible) {
            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/notice.png"), w / 2 + -8, h / 2 + -117, 0.0F, 0.0F, 16, 16, 16, 16);
            event.getGuiGraphics()
               .drawString(
                  Minecraft.getInstance().font,
                  Component.translatable("gui.sololeveling.used_paralyze.label_used_paralyze"),
                  w / 2 + -37,
                  h / 2 + -100,
                  -13312,
                  false
               );
         }

         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
