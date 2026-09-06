package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.procedures.IsInPunishmentZoneProcedure;
import dev.eness.sololevelingfinal.core.procedures.SurviveTextProcedure;

@EventBusSubscriber(Dist.CLIENT)
public class PunishmentRemainingTimeOverlay {
   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void eventHandler(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc.player;
      if (player != null && !mc.options.hideGui && mc.screen == null) {
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         if (IsInPunishmentZoneProcedure.execute(player)) {
            renderTimer(event.getGuiGraphics(), mc.font, event.getWindow().getGuiScaledWidth(), player);
         }

         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static void renderTimer(GuiGraphics graphics, Font font, int screenW, Player player) {
      String text = SurviveTextProcedure.execute(player);
      int width = Math.max(150, font.width(text) + 28);
      int x = (screenW - width) / 2;
      int y = 16;
      drawPanel(graphics, x, y, width, 32);
      graphics.drawString(font, Component.literal("PENALTY ZONE"), x + 8, y + 6, -44719, false);
      graphics.drawString(font, Component.literal(text), x + 8, y + 18, -10281, false);
   }

   private static void drawPanel(GuiGraphics graphics, int x, int y, int w, int h) {
      graphics.fill(x, y, x + w, y + h, -771094768);
      graphics.fill(x, y, x + w, y + 1, -49859);
      graphics.fill(x, y + h - 1, x + w, y + h, -8773846);
      graphics.fill(x, y, x + 1, y + h, -8773846);
      graphics.fill(x + w - 1, y, x + w, y + h, -49859);
      graphics.fill(x + 3, y + 3, x + 14, y + 4, -1426113219);
      graphics.fill(x + 3, y + 3, x + 4, y + 14, -1426113219);
   }
}
