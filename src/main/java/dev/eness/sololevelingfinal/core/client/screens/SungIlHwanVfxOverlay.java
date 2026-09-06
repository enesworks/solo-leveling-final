package dev.eness.sololevelingfinal.core.client.screens;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Post;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.client.renderer.SungIlHwanVfxClientState;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class SungIlHwanVfxOverlay {
   private static final int VOID = -687404787;
   private static final int VOID_SOFT = 1913327123;
   private static final int SILVER = -386675098;
   private static final int SILVER_DIM = -1598522858;
   private static final int GOLD = -251661908;

   private SungIlHwanVfxOverlay() {
   }

   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void render(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.level != null && !minecraft.options.hideGui) {
         SungIlHwanVfxClientState.OverlayState state = SungIlHwanVfxClientState.overlay(event.getPartialTick());
         if (state.active()) {
            GuiGraphics graphics = event.getGuiGraphics();
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            if (state.targeting().active()) {
               renderTargeting(graphics, minecraft, state.targeting(), width, height);
            }

            if (state.exhaustion().active()) {
               renderExhaustion(graphics, minecraft, state.exhaustion(), width, height);
            }

            if (state.risk().active()) {
               renderRisk(graphics, minecraft, state.risk(), width, height);
            }
         }
      }
   }

   private static void renderTargeting(GuiGraphics graphics, Minecraft minecraft, SungIlHwanVfxClientState.OverlayFrame frame, int width, int height) {
      int centerX = width / 2;
      int centerY = height / 2;
      float pulse = 1.0F + 0.035F * Mth.sin(((float)minecraft.level.getGameTime() + frame.progress() * 4.0F) * 0.54F);
      int radius = Math.max(28, Math.min(62, Math.round((34.0F + frame.radius() * 2.4F) * pulse)));
      drawCorner(graphics, centerX - radius, centerY - radius, 1, 1, 13, -687404787, -386675098);
      drawCorner(graphics, centerX + radius, centerY - radius, -1, 1, 13, -687404787, -386675098);
      drawCorner(graphics, centerX - radius, centerY + radius, 1, -1, 13, -687404787, -386675098);
      drawCorner(graphics, centerX + radius, centerY + radius, -1, -1, 13, -687404787, -386675098);
      int segments = 40;
      float charge = Math.max(frame.progress(), frame.intensity());
      int charged = Mth.clamp(Math.round(charge * segments), 0, segments);

      for (int index = 0; index < segments; index++) {
         double angle = index * Math.PI * 2.0 / segments - (Math.PI / 2);
         int x = centerX + Mth.floor(Math.cos(angle) * radius);
         int y = centerY + Mth.floor(Math.sin(angle) * radius);
         int color = index <= charged ? -251661908 : -1598522858;
         graphics.fill(x - 1, y - 1, x + 1, y + 1, color);
      }

      drawDashedHorizontal(graphics, centerX, centerY, Math.round(radius * 0.82F), -1598522858);
      drawDashedVertical(graphics, centerX, centerY, Math.round(radius * 0.82F), -1598522858);
      graphics.fill(centerX - 12, centerY - 1, centerX - 3, centerY + 1, -251661908);
      graphics.fill(centerX + 3, centerY - 1, centerX + 12, centerY + 1, -251661908);
      graphics.fill(centerX - 1, centerY - 12, centerX + 1, centerY - 3, -251661908);
      graphics.fill(centerX - 1, centerY + 3, centerX + 1, centerY + 12, -251661908);
      graphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, -184553284);
      String distanceText = String.format(Locale.ROOT, "AREA %.1fm", Math.min(99.9, frame.radius()));
      int textWidth = minecraft.font.width(distanceText);
      int labelY = centerY + radius + 8;
      graphics.fill(centerX - textWidth / 2 - 4, labelY - 2, centerX + textWidth / 2 + 4, labelY + 10, -687404787);
      graphics.drawString(minecraft.font, Component.literal(distanceText), centerX - textWidth / 2, labelY, 15194021, false);
   }

   private static void renderExhaustion(GuiGraphics graphics, Minecraft minecraft, SungIlHwanVfxClientState.OverlayFrame frame, int width, int height) {
      float remaining = 1.0F - frame.progress();
      float strength = frame.intensity() * remaining;
      int edgeAlpha = Mth.clamp(Math.round(72.0F * strength), 0, 72);
      if (edgeAlpha > 0) {
         int edge = edgeAlpha << 24 | 461069;
         int thickness = 7 + Math.round(strength * 11.0F);
         graphics.fill(0, 0, width, thickness, edge);
         graphics.fill(0, height - thickness, width, height, edge);
         graphics.fill(0, thickness, thickness, height - thickness, edge);
         graphics.fill(width - thickness, thickness, width, height - thickness, edge);
      }

      int barWidth = 74;
      int barX = width / 2 - barWidth / 2;
      int barY = height - 29;
      int fill = Math.round((barWidth - 4) * strength);
      graphics.fill(barX - 2, barY - 2, barX + barWidth + 2, barY + 7, 1913327123);
      graphics.fill(barX, barY, barX + barWidth, barY + 5, -653652718);
      if (fill > 0) {
         graphics.fill(barX + 2, barY + 1, barX + 2 + fill, barY + 4, strength > 0.72F ? -251661908 : -1598522858);
      }
   }

   private static void renderRisk(GuiGraphics graphics, Minecraft minecraft, SungIlHwanVfxClientState.OverlayFrame frame, int width, int height) {
      float envelope = Mth.sin(Mth.clamp(frame.progress(), 0.0F, 1.0F) * (float) Math.PI);
      float intensity = frame.intensity() * envelope;
      int centerX = width / 2;
      int centerY = height / 2;
      int spread = 72 + Math.round((1.0F - envelope) * 20.0F);
      int alpha = Mth.clamp(Math.round(210.0F * intensity), 0, 210);
      int color = alpha << 24 | 15194021;

      for (int side = -1; side <= 1; side += 2) {
         int x = centerX + side * spread;
         graphics.fill(x - 1, centerY - 18, x + 1, centerY + 18, color);
         graphics.fill(x - side * 8, centerY - 18, x + side, centerY - 16, color);
         graphics.fill(x - side * 8, centerY + 16, x + side, centerY + 18, color);
      }

      if (frame.intensity() > 0.74F) {
         int flash = Mth.clamp(Math.round(34.0F * intensity), 0, 34);
         graphics.fill(0, 0, width, height, flash << 24 | 14207387);
      }
   }

   private static void drawCorner(GuiGraphics graphics, int x, int y, int horizontalDirection, int verticalDirection, int length, int backing, int accent) {
      int endX = x + horizontalDirection * length;
      int endY = y + verticalDirection * length;
      graphics.fill(Math.min(x, endX) - 2, y - 2, Math.max(x, endX) + 2, y + 2, backing);
      graphics.fill(x - 2, Math.min(y, endY) - 2, x + 2, Math.max(y, endY) + 2, backing);
      graphics.fill(Math.min(x, endX), y - 1, Math.max(x, endX) + 1, y + 1, accent);
      graphics.fill(x - 1, Math.min(y, endY), x + 1, Math.max(y, endY) + 1, accent);
   }

   private static void drawDashedHorizontal(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
      for (int x = -radius; x <= radius; x += 7) {
         graphics.fill(centerX + x, centerY, centerX + Math.min(radius + 1, x + 3), centerY + 1, color);
      }
   }

   private static void drawDashedVertical(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
      for (int y = -radius; y <= radius; y += 7) {
         graphics.fill(centerX, centerY + y, centerX + 1, centerY + Math.min(radius + 1, y + 3), color);
      }
   }
}
