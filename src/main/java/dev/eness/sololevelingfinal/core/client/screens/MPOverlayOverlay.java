package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.ArmorBarProcedure;
import dev.eness.sololevelingfinal.core.procedures.FatigueTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.Health0Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health100Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health10Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health20Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health30Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health40Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health50Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health60Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health70Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health80Procedure;
import dev.eness.sololevelingfinal.core.procedures.Health90Procedure;
import dev.eness.sololevelingfinal.core.procedures.HealthTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.HungerBarProcedure;
import dev.eness.sololevelingfinal.core.procedures.IfInSurvivalProcedure;
import dev.eness.sololevelingfinal.core.procedures.LevelBarProcedure;
import dev.eness.sololevelingfinal.core.procedures.Mana0Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana100Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana10Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana20Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana30Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana40Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana50Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana60Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana70Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana80Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana90Procedure;
import dev.eness.sololevelingfinal.core.procedures.ManaTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.TitleTextProcedure;
import dev.eness.sololevelingfinal.core.util.SystemClientConfig;
import dev.eness.sololevelingfinal.core.util.TitleManager;

@EventBusSubscriber(Dist.CLIENT)
public class MPOverlayOverlay {
   private static final int PANEL_X = 8;
   private static final int PANEL_Y = 8;
   private static final int PANEL_W = 166;
   private static final int PANEL_H = 64;

   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void eventHandler(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc.player;
      if (player != null && !mc.options.hideGui && !mc.options.renderDebug && mc.screen == null) {
         if (IfInSurvivalProcedure.execute(player)) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(SystemClientConfig.isLegacyOverlayEnabled() ? GameRenderer::getPositionTexShader : GameRenderer::getPositionColorShader);
            RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (SystemClientConfig.isLegacyOverlayEnabled()) {
               renderLegacyHud(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), player);
            } else {
               renderSystemHud(event.getGuiGraphics(), mc, player);
            }

            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }

   private static void renderSystemHud(GuiGraphics graphics, Minecraft mc, Player player) {
      SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      Font font = mc.font;
      drawPanel(graphics, 8, 8, 166, 64, -721024234, -585910017);
      drawScanLines(graphics, 11, 11, 160, 58, 370391295);
      drawEnergyNoise(graphics, 13, 13, 156, 54, 840153343);
      String job = clean(TitleTextProcedure.execute(player));
      String clazz = className((int)Math.round(vars.Classes));
      String title = TitleManager.displayName((int)Math.round(vars.title));
      if ("None".equals(title)) {
         title = "No Title";
      }

      int metaY = 15;
      String identity = "No Job".equals(job) ? clazz : job;
      if (font.width(identity) > 150) {
         identity = clazz;
      }

      graphics.drawString(font, Component.literal(identity), 16, metaY, -1378049, false);
      graphics.drawString(font, Component.literal(title), 16, metaY + 10, -4616961, false);
      float hp = player.getHealth();
      float maxHp = Math.max(1.0F, player.getMaxHealth());
      float mp = (float)Math.max(0.0, vars.MP);
      float maxMp = (float)Math.max(1.0, vars.Mana);
      int barX = 16;
      int barY = 39;
      int barW = 150;
      drawLabeledBar(graphics, font, "HP", hp, maxHp, barX, barY, barW, 11, -13103080, -52903, -30051);
      drawLabeledBar(graphics, font, "MP", mp, maxMp, barX, barY + 15, barW, 11, -16311508, -14889985, -6427905);
      renderSurvivalChips(graphics, font, player, vars, 179, 8);
   }

   private static void renderSurvivalChips(GuiGraphics graphics, Font font, Player player, SololevelingModVariables.PlayerVariables vars, int x, int y) {
      int armor = player.getArmorValue();
      int food = player.getFoodData().getFoodLevel();
      int fatigue = (int)Math.round(vars.Fatigue / 10.0);
      drawChip(graphics, font, "DEF", String.valueOf(armor), x, y, 46, 20, -737734632, -8738561);
      drawChip(graphics, font, "FOOD", food + "/20", x, y + 22, 46, 20, -737734632, -13973);
      drawChip(graphics, font, "FTG", String.valueOf(fatigue), x, y + 44, 46, 20, -737734632, -33589);
   }

   private static void drawLabeledBar(
      GuiGraphics graphics, Font font, String label, float value, float max, int x, int y, int width, int height, int trackColor, int fillColor, int hotColor
   ) {
      float ratio = clamp(value / max, 0.0F, 1.0F);
      int fillW = Math.round(width * ratio);
      int color = ratio <= 0.22F && "HP".equals(label) ? pulseColor(fillColor, hotColor) : fillColor;
      graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, -1442374614);
      graphics.fill(x, y, x + width, y + height, trackColor);
      if (fillW > 0) {
         graphics.fill(x, y, x + fillW, y + height, color);
         graphics.fill(x, y, x + fillW, y + 1, hotColor);

         for (int sx = x + 9; sx < x + fillW; sx += 12) {
            graphics.fill(sx, y + 1, sx + 1, y + height - 1, 1157627903);
         }
      }

      String text = label + " " + Math.round(value) + "/" + Math.round(max);
      int textY = y + Math.max(0, (height - 9) / 2);
      int textW = font.width(text);
      graphics.fill(x + 2, textY, x + textW + 7, textY + 9, 1711276032);
      if ("MP".equals(label)) {
         drawManaFlow(graphics, x, y, fillW, height);
      } else if ("HP".equals(label)) {
         drawHealthFluctuation(graphics, x, y, fillW, height);
      }

      graphics.drawString(font, Component.literal(text), x + 4, textY, -1, true);
   }

   private static void drawChip(GuiGraphics graphics, Font font, String label, String value, int x, int y, int w, int h, int bg, int accent) {
      drawPanel(graphics, x, y, w, h, bg, accent);
      graphics.drawString(font, Component.literal(label), x + 4, y + 3, -8922625, false);
      graphics.drawString(font, Component.literal(value), x + 4, y + 11, -1378049, false);
   }

   private static void drawPanel(GuiGraphics graphics, int x, int y, int w, int h, int bg, int accent) {
      graphics.fill(x, y, x + w, y + h, bg);
      graphics.fill(x, y, x + w, y + 1, accent);
      graphics.fill(x, y + h - 1, x + w, y + h, -1724551279);
      graphics.fill(x, y, x + 1, y + h, -1724551279);
      graphics.fill(x + w - 1, y, x + w, y + h, accent);
      graphics.fill(x + 3, y + 3, x + 13, y + 4, -1726760705);
      graphics.fill(x + 3, y + 3, x + 4, y + 13, -1726760705);
      graphics.fill(x + w - 13, y + h - 4, x + w - 3, y + h - 3, -1726760705);
      graphics.fill(x + w - 4, y + h - 13, x + w - 3, y + h - 3, -1726760705);
   }

   private static void renderLegacyHud(GuiGraphics graphics, int w, int h, Player entity) {
      ResourceLocation empty = new ResourceLocation("sololeveling:textures/screens/bar1.png");
      int barX = w / 2 - 85;
      int manaY = h - 48;
      int healthY = h - 35;
      graphics.blit(manaTexture(entity, empty), barX, manaY, 0.0F, 0.0F, 90, 10, 90, 10);
      graphics.blit(healthTexture(entity, empty), barX, healthY, 0.0F, 0.0F, 90, 10, 90, 10);
      graphics.blit(new ResourceLocation("sololeveling:textures/screens/armorbar.png"), w / 2 + 6, h - 36, 0.0F, 0.0F, 12, 12, 12, 12);
      graphics.blit(new ResourceLocation("sololeveling:textures/screens/hungerbar.png"), w / 2 + 6, h - 49, 0.0F, 0.0F, 12, 12, 12, 12);
      graphics.blit(new ResourceLocation("sololeveling:textures/screens/levelbar.png"), w / 2 + 44, h - 49, 0.0F, 0.0F, 12, 12, 12, 12);
      graphics.blit(new ResourceLocation("sololeveling:textures/screens/fatiguebar.png"), w / 2 + 42, h - 38, 0.0F, 0.0F, 16, 16, 16, 16);
      Font font = Minecraft.getInstance().font;
      graphics.drawString(font, ManaTextProcedure.execute(entity), w / 2 - 76, h - 47, -1, false);
      graphics.drawString(font, HealthTextProcedure.execute(entity), w / 2 - 76, h - 34, -1, false);
      graphics.drawString(font, ArmorBarProcedure.execute(entity), w / 2 + 19, h - 35, -1, false);
      graphics.drawString(font, HungerBarProcedure.execute(entity), w / 2 + 19, h - 47, -1, false);
      graphics.drawString(font, LevelBarProcedure.execute(entity), w / 2 + 55, h - 47, -1, false);
      graphics.drawString(font, FatigueTextProcedure.execute(entity), w / 2 + 55, h - 34, -1, false);
   }

   private static ResourceLocation manaTexture(Player entity, ResourceLocation empty) {
      if (Mana100Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana100.png");
      } else if (Mana90Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana90.png");
      } else if (Mana80Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana80.png");
      } else if (Mana70Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana70.png");
      } else if (Mana60Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana60.png");
      } else if (Mana50Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana50.png");
      } else if (Mana40Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana40.png");
      } else if (Mana30Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana30.png");
      } else if (Mana20Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana20.png");
      } else if (Mana10Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barmana10.png");
      } else {
         return Mana0Procedure.execute(entity) ? empty : empty;
      }
   }

   private static ResourceLocation healthTexture(Player entity, ResourceLocation empty) {
      if (Health100Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth100.png");
      } else if (Health90Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth90.png");
      } else if (Health80Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth80.png");
      } else if (Health70Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth70.png");
      } else if (Health60Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth60.png");
      } else if (Health50Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth50.png");
      } else if (Health40Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth40.png");
      } else if (Health30Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth30.png");
      } else if (Health20Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth20.png");
      } else if (Health10Procedure.execute(entity)) {
         return new ResourceLocation("sololeveling:textures/screens/barhealth10.png");
      } else {
         return Health0Procedure.execute(entity) ? empty : empty;
      }
   }

   private static void drawScanLines(GuiGraphics graphics, int x, int y, int w, int h, int color) {
      for (int line = y + 3; line < y + h; line += 5) {
         graphics.fill(x, line, x + w, line + 1, color);
      }
   }

   private static void drawEnergyNoise(GuiGraphics graphics, int x, int y, int w, int h, int color) {
      long tick = gameTick();

      for (int i = 0; i < 6; i++) {
         int yy = y + (int)((tick * (i + 2) + i * 11) % Math.max(1, h));
         int xx = x + 6 + i * 23;
         graphics.fill(xx, yy, Math.min(x + w, xx + 16), yy + 1, color);
      }
   }

   private static void drawManaFlow(GuiGraphics graphics, int x, int y, int fillW, int height) {
      if (fillW > 3) {
         int offset = (int)(gameTick() % 16L);

         for (int sx = x - 16 + offset; sx < x + fillW; sx += 16) {
            int start = Math.max(x, sx);
            int end = Math.min(x + fillW, sx + 6);
            if (end > start) {
               graphics.fill(start, y + 2, end, y + height - 2, 1388180991);
            }
         }
      }
   }

   private static void drawHealthFluctuation(GuiGraphics graphics, int x, int y, int fillW, int height) {
      if (fillW > 4) {
         long tick = gameTick();

         for (int i = 0; i < 5; i++) {
            int sx = x + (int)((tick * 3L + i * 27) % Math.max(1, fillW));
            int pulseH = 2 + (int)((tick + i) % Math.max(1, height - 1));
            graphics.fill(sx, y + Math.max(1, height - pulseH), Math.min(x + fillW, sx + 1), y + height - 1, 1728053247);
         }
      }
   }

   private static long gameTick() {
      return Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
   }

   private static int pulseColor(int base, int hot) {
      long tick = gameTick();
      return tick / 8L % 2L == 0L ? hot : base;
   }

   private static String clean(String text) {
      return text != null && !text.isBlank() && !"none".equalsIgnoreCase(text) ? text.replaceAll("\\u00A7.", "") : "No Job";
   }

   private static String className(int cls) {
      return switch (cls) {
         case 1 -> "Assassin";
         case 2 -> "Combat Mage";
         case 3 -> "Fighter";
         case 4 -> "Tanker";
         case 5 -> "Support Mage";
         case 6 -> "Ranger";
         default -> "No Class";
      };
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }
}
