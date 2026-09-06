package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ClassPassiveClientState;
import dev.eness.sololevelingfinal.core.util.RangerClientState;
import dev.eness.sololevelingfinal.core.util.StormClientState;
import org.joml.Matrix4f;

@EventBusSubscriber(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClassPassivesOverlay {
   private static final int PANEL_X = 8;
   private static final int NORMAL_PANEL_Y = 76;
   private static final int CREATIVE_PANEL_Y = 8;
   private static final int PANEL_W = 154;
   private static final int PANEL_H = 27;
   private static final int PANEL_GAP = 4;
   private static final String[] RANGER_STAGE_LABEL_KEYS = new String[]{
      "gui.sololeveling.ranger.quiver.stage.manifested", "gui.sololeveling.ranger.quiver.stage.linear", "gui.sololeveling.ranger.quiver.stage.seeking"
   };
   private static final int[] RANGER_STAGE_ACTIVE_COLORS = new int[]{-9836398, -10297345, -11415};

   @SubscribeEvent
   public static void onRenderGui(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && !mc.options.hideGui && !mc.options.renderDebug && mc.screen == null) {
         SololevelingModVariables.PlayerVariables vars = mc.player
            .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         int cls = (int)Math.round(vars.Classes);
         boolean rangerCombatOnly = cls == 6 && shouldRenderRangerCombatHud(mc);
         if (vars.CustomHUD || rangerCombatOnly || StormClientState.hasAccess) {
            GameType gameType = gameType(mc);
            if (gameType != GameType.SPECTATOR) {
               if (cls != 0 || StormClientState.hasAccess) {
                  int panelY = gameType == GameType.CREATIVE ? 8 : 76;
                  GuiGraphics graphics = event.getGuiGraphics();
                  PoseStack pose = graphics.pose();
                  pose.pushPose();
                  pose.translate(0.0F, 0.0F, 210.0F);
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableDepthTest();
                  if (vars.CustomHUD) {
                     switch (cls) {
                        case 1:
                           renderSegmentPassive(graphics, mc.font, panelY, "Tempo", ClassPassiveClientState.assassinTempo, 5, -15265251, -6400513, -2309377);
                        case 2:
                        default:
                           break;
                        case 3:
                           renderBarPassive(graphics, mc.font, panelY, "Battle", ClassPassiveClientState.fighterPower, 100.0, -14412019, -32466, -11958);
                           break;
                        case 4:
                           renderSegmentPassive(graphics, mc.font, panelY, "Guard", ClassPassiveClientState.tankWallStacks, 10, -15722204, -13593857, -5908225);
                           break;
                        case 5:
                           renderSegmentPassive(
                              graphics, mc.font, panelY, "Resonance", ClassPassiveClientState.healerResonance, 5, -15917804, -13114492, -6094896
                           );
                           break;
                        case 6:
                           renderRangerPassive(graphics, mc, panelY, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
                     }

                     if (StormClientState.hasAccess) {
                        boolean nativeStormMage = cls == 2 && "storm".equalsIgnoreCase(vars.mageSpecialization);
                        int stormY = !nativeStormMage && hasBaseClassPanel(cls) ? panelY + 27 + 4 : panelY;
                        renderStormPassive(graphics, mc.font, stormY);
                     }
                  } else {
                     if (rangerCombatOnly) {
                        renderRangerCombatHud(graphics, mc.font, event.getWindow().getGuiScaledWidth() * 0.5F, event.getWindow().getGuiScaledHeight() * 0.5F);
                     }

                     if (StormClientState.hasAccess) {
                        renderStormPassive(graphics, mc.font, panelY);
                     }
                  }

                  RenderSystem.enableDepthTest();
                  RenderSystem.disableBlend();
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  pose.popPose();
               }
            }
         }
      }
   }

   private static void renderBarPassive(GuiGraphics graphics, Font font, int panelY, String title, double value, double max, int track, int fill, int hot) {
      double ratio = clamp(value / Math.max(1.0, max), 0.0, 1.0);
      drawPanel(graphics, 8, panelY, 154, 27, -922219244, fill);
      graphics.drawString(font, Component.literal(title), 15, panelY + 4, hot, false);
      String valueText = value >= max ? "Ready" : Math.round(value) + "%";
      graphics.drawString(font, Component.literal(valueText), 155 - font.width(valueText), panelY + 4, -2497816, false);
      int barX = 15;
      int barY = panelY + 18;
      int barW = 140;
      int filled = (int)Math.round(barW * ratio);
      int barH = 4;
      graphics.fill(barX, barY, barX + barW, barY + barH, track);
      if (filled > 0) {
         graphics.fill(barX, barY, barX + filled, barY + barH, value >= max ? hot : fill);
         graphics.fill(barX, barY, barX + filled, barY + 1, -1426063361);
      }
   }

   private static void renderSegmentPassive(GuiGraphics graphics, Font font, int panelY, String title, int filled, int max, int track, int fill, int hot) {
      drawPanel(graphics, 8, panelY, 154, 27, -922219244, fill);
      graphics.drawString(font, Component.literal(title), 15, panelY + 4, hot, false);
      String valueText = Math.max(0, Math.min(max, filled)) + "/" + max;
      graphics.drawString(font, Component.literal(valueText), 155 - font.width(valueText), panelY + 4, -2497816, false);
      int sx = 15;
      int sy = panelY + 18;
      int gap = 2;
      int segW = (140 - gap * (max - 1)) / max;

      for (int i = 0; i < max; i++) {
         int x = sx + i * (segW + gap);
         boolean active = i < filled;
         graphics.fill(x, sy, x + segW, sy + 4, active ? fill : track);
         if (active) {
            graphics.fill(x, sy, x + segW, sy + 1, hot);
         }
      }
   }

   private static void renderRangerPassive(GuiGraphics graphics, Minecraft mc, int panelY, int screenW, int screenH) {
      double focus = ClassPassiveClientState.rangerFocus;
      String focusTitle = Component.translatable("gui.sololeveling.ranger.focus.title").getString();
      renderBarPassive(graphics, mc.font, panelY, focusTitle, focus, 100.0, -14673402, -20448, -7030);
      float cx = screenW * 0.5F;
      float cy = screenH * 0.5F;
      renderRangerCombatHud(graphics, mc.font, cx, cy);
   }

   private static boolean hasBaseClassPanel(int cls) {
      return cls == 1 || cls == 3 || cls == 4 || cls == 5 || cls == 6;
   }

   private static void renderStormPassive(GuiGraphics graphics, Font font, int panelY) {
      int stage = Math.max(1, Math.min(6, StormClientState.effectiveStage));
      int voltage = Math.max(0, Math.min(100, StormClientState.voltage));
      int accent = StormClientState.spiritualizationBonus ? -5981 : (StormClientState.overcharged ? -1 : -11214849);
      int fill = StormClientState.overcharged ? -2873 : -12861462;
      int track = -16311509;
      drawPanel(graphics, 8, panelY, 154, 27, -871952360, accent);
      String title = "Storm";
      String valueText = "Stage " + stage + "  " + voltage + "%";
      graphics.drawString(font, Component.literal(title), 15, panelY + 4, -8000257, false);
      graphics.drawString(font, Component.literal(valueText), 155 - font.width(valueText), panelY + 4, accent, false);
      int barX = 15;
      int barY = panelY + 18;
      int barW = 140;
      int barH = 4;
      int filled = Math.round(barW * voltage / 100.0F);
      graphics.fill(barX, barY, barX + barW, barY + barH, track);
      if (filled > 0) {
         graphics.fill(barX, barY, barX + filled, barY + barH, fill);
         graphics.fill(barX, barY, barX + filled, barY + 1, StormClientState.spiritualizationBonus ? -1 : -1427637505);
      }
   }

   private static void renderRangerCombatHud(GuiGraphics graphics, Font font, float cx, float cy) {
      boolean quiverActive = RangerClientState.quiverActive
         && Minecraft.getInstance().player != null
         && Minecraft.getInstance().player.isUsingItem()
         && Minecraft.getInstance().player.getUseItem().getItem() instanceof BowItem;
      int fivefoldCharges = Math.max(0, Math.min(5, RangerClientState.fivefoldCharges));
      if (quiverActive || fivefoldCharges > 0) {
         if (quiverActive) {
            renderManaQuiverStages(graphics, font, Math.round(cx), Math.round(cy) + 27);
            renderRangerLock(graphics, font, cx, cy);
         }

         if (fivefoldCharges > 0) {
            renderFivefoldCharges(graphics, font, Math.round(cx), Math.round(cy) + (quiverActive ? 46 : 29), fivefoldCharges);
         }
      }
   }

   private static boolean shouldRenderRangerCombatHud(Minecraft minecraft) {
      boolean drawingManaBow = RangerClientState.quiverActive
         && minecraft.player != null
         && minecraft.player.isUsingItem()
         && minecraft.player.getUseItem().getItem() instanceof BowItem;
      return drawingManaBow || RangerClientState.fivefoldCharges > 0;
   }

   private static void renderManaQuiverStages(GuiGraphics graphics, Font font, int centerX, int y) {
      int stageWidth = 48;
      int gap = 3;
      int totalWidth = stageWidth * 3 + gap * 2;
      int startX = centerX - totalWidth / 2;
      int chargedStage = Math.max(0, Math.min(3, RangerClientState.chargeStage));
      int maximumStage = Math.max(1, Math.min(3, RangerClientState.maximumStage));

      for (int index = 0; index < 3; index++) {
         int stage = index + 1;
         int x = startX + index * (stageWidth + gap);
         boolean available = stage <= maximumStage;
         boolean charged = available && stage <= chargedStage;
         int background = available ? -1256577744 : -1710617326;
         int accent = charged ? RANGER_STAGE_ACTIVE_COLORS[index] : (available ? -10849920 : -14275533);
         int textColor = charged ? RANGER_STAGE_ACTIVE_COLORS[index] : (available ? -6376514 : -11906728);
         graphics.fill(x, y, x + stageWidth, y + 13, background);
         graphics.fill(x, y, x + stageWidth, y + 2, accent);
         graphics.fill(x, y + 12, x + stageWidth, y + 13, -1442840576);
         graphics.fill(x, y, x + 1, y + 13, accent);
         graphics.fill(x + stageWidth - 1, y, x + stageWidth, y + 13, accent);
         Component label = Component.translatable(RANGER_STAGE_LABEL_KEYS[index]);
         graphics.drawString(font, label, x + (stageWidth - font.width(label)) / 2, y + 4, textColor, false);
      }
   }

   private static void renderRangerLock(GuiGraphics graphics, Font font, float cx, float cy) {
      float progress = Math.max(0.0F, Math.min(1.0F, RangerClientState.lockProgress));
      boolean locked = RangerClientState.locked;
      if (locked || !(progress <= 0.001F)) {
         PoseStack pose = graphics.pose();
         float radius = 13.0F;
         renderArcSegment(pose, cx, cy, radius, 0.0F, 360.0F, 2.8F, 0.04F, 0.12F, 0.17F, 0.72F);
         renderArcSegment(
            pose, cx, cy, radius, 0.0F, 360.0F * (locked ? 1.0F : progress), 2.0F, locked ? 0.36F : 0.25F, locked ? 1.0F : 0.82F, locked ? 0.48F : 1.0F, 0.96F
         );
         Component lockText = locked
            ? Component.translatable("gui.sololeveling.ranger.lock.locked")
            : Component.translatable("gui.sololeveling.ranger.lock.progress", Math.round(progress * 100.0F));
         int x = Math.round(cx) - font.width(lockText) / 2;
         graphics.drawString(font, lockText, x, Math.round(cy) - 34, locked ? -9044064 : -8918785, false);
      }
   }

   private static void renderFivefoldCharges(GuiGraphics graphics, Font font, int centerX, int y, int charges) {
      Component label = Component.translatable("gui.sololeveling.ranger.fivefold", charges);
      graphics.drawString(font, label, centerX - font.width(label) / 2, y, -10374, false);
      int pipWidth = 8;
      int gap = 2;
      int totalWidth = pipWidth * 5 + gap * 4;
      int startX = centerX - totalWidth / 2;
      int pipY = y + 9 + 1;

      for (int index = 0; index < 5; index++) {
         int x = startX + index * (pipWidth + gap);
         boolean active = index < charges;
         graphics.fill(x, pipY, x + pipWidth, pipY + 4, active ? -18371 : -1707924208);
         if (active) {
            graphics.fill(x, pipY, x + pipWidth, pipY + 1, -3918);
         }
      }
   }

   private static void drawPanel(GuiGraphics graphics, int x, int y, int w, int h, int bg, int accent) {
      graphics.fill(x, y, x + w, y + h, bg);
      graphics.fill(x, y, x + 2, y + h, accent);
      graphics.fill(x + 2, y + h - 1, x + w, y + h, 1429093704);
   }

   private static void renderArcSegment(
      PoseStack pose, float cx, float cy, float radius, float startDeg, float endDeg, float thickness, float red, float green, float blue, float alpha
   ) {
      float outerR = radius + thickness * 0.5F;
      float innerR = radius - thickness * 0.5F;
      float startRad = (float)Math.toRadians(startDeg - 90.0F);
      float endRad = (float)Math.toRadians(endDeg - 90.0F);
      if (endRad < startRad) {
         endRad += (float) (Math.PI * 2);
      }

      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f matrix = pose.last().pose();
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
      int segments = 72;

      for (int i = 0; i <= segments; i++) {
         float angle = startRad + (endRad - startRad) * ((float)i / segments);
         float cos = (float)Math.cos(angle);
         float sin = (float)Math.sin(angle);
         buffer.vertex(matrix, cx + cos * outerR, cy + sin * outerR, 0.0F).color(red, green, blue, alpha).endVertex();
         buffer.vertex(matrix, cx + cos * innerR, cy + sin * innerR, 0.0F).color(red, green, blue, alpha).endVertex();
      }

      BufferUploader.drawWithShader(buffer.end());
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private static GameType gameType(Minecraft mc) {
      if (mc.player != null && mc.getConnection() != null) {
         PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getGameProfile().getId());
         return info == null ? GameType.SURVIVAL : info.getGameMode();
      } else {
         return GameType.SURVIVAL;
      }
   }
}
