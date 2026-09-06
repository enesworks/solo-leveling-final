package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.client.gui.DkcQuestProgressClientState;
import dev.eness.sololevelingfinal.core.client.gui.UrgentQuestClientState;
import dev.eness.sololevelingfinal.core.procedures.DungeoningProcedure;
import dev.eness.sololevelingfinal.core.procedures.QuestInfoGetProcedure;
import dev.eness.sololevelingfinal.core.procedures.QuestLinesProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnQuestNameProcedure;

@EventBusSubscriber(Dist.CLIENT)
public class QuestsOverlayOverlay {
   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void eventHandler(Pre event) {
      int w = event.getWindow().getGuiScaledWidth();
      int h = event.getWindow().getGuiScaledHeight();
      Player entity = Minecraft.getInstance().player;
      boolean visible = QuestInfoGetProcedure.execute(entity);
      if (visible) {
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         if (visible) {
            if (DkcQuestProgressClientState.isActive(entity)) {
               renderDkcQuest(event.getGuiGraphics(), w, h);
            } else if (UrgentQuestClientState.isActive()) {
               renderUrgentQuest(event.getGuiGraphics(), w);
            } else {
               renderStoryQuest(event.getGuiGraphics(), entity, w);
            }
         }

         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static void renderDkcQuest(GuiGraphics graphics, int screenWidth, int screenHeight) {
      Font font = Minecraft.getInstance().font;
      int x = 8;
      int width = Math.max(120, Math.min(230, screenWidth - 16));
      int accent = dkcAccent(DkcQuestProgressClientState.phase());
      List<FormattedCharSequence> objectiveLines = font.split(Component.literal(DkcQuestProgressClientState.objective()), width - 12);
      List<FormattedCharSequence> detailLines = DkcQuestProgressClientState.detail().isBlank()
         ? List.of()
         : font.split(Component.literal(DkcQuestProgressClientState.detail()), width - 12);
      boolean objectiveProgress = DkcQuestProgressClientState.target() > 0;
      boolean urgent = UrgentQuestClientState.isActive();
      List<FormattedCharSequence> urgentLines = urgent ? font.split(Component.literal(UrgentQuestClientState.objective()), width - 12) : List.of();
      int urgentLineCount = Math.min(2, urgentLines.size());
      int height = 34 + objectiveLines.size() * 10 + detailLines.size() * 10 + (objectiveProgress ? 17 : 0);
      if (urgent) {
         height += 25 + urgentLineCount * 10;
      }

      int y = Math.min(134, Math.max(8, screenHeight - height - 8));
      drawSystemPanel(graphics, x, y, width, height, -771225069, accent);
      Component heading = Component.literal("DEMON KING'S CASTLE").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
      graphics.drawString(font, heading, x + 6, y + 5, -1, false);
      String overall = DkcQuestProgressClientState.cleared() + "/20";
      graphics.drawString(font, overall, x + width - 6 - font.width(overall), y + 5, -14490, false);
      String floorLine = "FLOOR " + DkcQuestProgressClientState.floor() + " - " + DkcQuestProgressClientState.floorName().toUpperCase(Locale.ROOT);
      if (font.width(floorLine) > width - 12) {
         floorLine = font.plainSubstrByWidth(floorLine, width - 24) + "...";
      }

      graphics.drawString(font, floorLine, x + 6, y + 17, accent, false);
      int cursor = y + 29;

      for (FormattedCharSequence line : objectiveLines) {
         graphics.drawString(font, line, x + 6, cursor, -722945, false);
         cursor += 10;
      }

      for (FormattedCharSequence line : detailLines) {
         graphics.drawString(font, line, x + 6, cursor, -5392185, false);
         cursor += 10;
      }

      if (objectiveProgress) {
         int progress = Math.min(DkcQuestProgressClientState.progress(), DkcQuestProgressClientState.target());
         String progressText = "Objective: " + progress + "/" + DkcQuestProgressClientState.target();
         graphics.drawString(font, progressText, x + 6, cursor, -1378049, false);
         cursor += 10;
         int barWidth = width - 12;
         int filled = (int)Math.round(barWidth * ((double)progress / DkcQuestProgressClientState.target()));
         graphics.fill(x + 6, cursor + 1, x + 6 + barWidth, cursor + 5, -1441127888);
         if (filled > 0) {
            graphics.fill(x + 6, cursor + 1, x + 6 + filled, cursor + 5, accent);
         }

         cursor += 7;
      }

      if (urgent) {
         graphics.fill(x + 6, cursor + 1, x + width - 6, cursor + 2, -1434837206);
         cursor += 6;
         Component urgentHeading = Component.literal("URGENT: ")
            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
            .append(Component.literal(UrgentQuestClientState.title()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
         List<FormattedCharSequence> headingLines = font.split(urgentHeading, width - 12);
         if (!headingLines.isEmpty()) {
            graphics.drawString(font, headingLines.get(0), x + 6, cursor, -38294, false);
         }

         cursor += 10;

         for (int index = 0; index < urgentLineCount; index++) {
            graphics.drawString(font, urgentLines.get(index), x + 6, cursor, -2570032, false);
            cursor += 10;
         }

         String urgentStatus = urgentStatus();
         graphics.drawString(font, urgentStatus, x + 6, cursor, -14490, false);
      }
   }

   private static String urgentStatus() {
      String status = switch (UrgentQuestClientState.kind()) {
         case "kill", "pvp", "kang" -> "Progress " + UrgentQuestClientState.progress() + "/" + UrgentQuestClientState.target();
         case "no_skills" -> "No skills used";
         default -> "Active";
      };
      int remaining = UrgentQuestClientState.remainingSeconds();
      return remaining < 0 ? status : status + String.format("  %02d:%02d", remaining / 60, remaining % 60);
   }

   private static int dkcAccent(String phase) {
      return switch (phase) {
         case "boss" -> -45747;
         case "radiru", "sanctuary" -> -2528001;
         case "ascent" -> -11544577;
         case "permit" -> -14490;
         case "conquered" -> -18371;
         default -> -34235;
      };
   }

   private static void renderUrgentQuest(GuiGraphics graphics, int screenWidth) {
      Font font = Minecraft.getInstance().font;
      int x = 8;
      int y = 134;
      int width = Math.min(220, Math.max(128, screenWidth - 12));
      List<FormattedCharSequence> objectiveLines = font.split(Component.literal(UrgentQuestClientState.objective()), width - 12);
      int height = 46 + objectiveLines.size() * 10;
      graphics.fill(x, y, x + width, y + height, -771094768);
      graphics.fill(x, y, x + width, y + 1, -49859);
      graphics.fill(x, y + height - 1, x + width, y + height, -8773846);
      graphics.fill(x, y, x + 1, y + height, -8773846);
      graphics.fill(x + width - 1, y, x + width, y + height, -8773846);
      Component heading = Component.literal("URGENT QUEST: ")
         .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
         .append(Component.literal(UrgentQuestClientState.title()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
      graphics.drawString(font, heading, x + 6, y + 5, -1, false);
      int lineY = y + 17;

      for (FormattedCharSequence line : objectiveLines) {
         graphics.drawString(font, line, x + 6, lineY, -2570032, false);
         lineY += 10;
      }
      String status = switch (UrgentQuestClientState.kind()) {
         case "kill", "pvp", "kang" -> "Progress: " + UrgentQuestClientState.progress() + "/" + UrgentQuestClientState.target();
         case "no_skills" -> "Status: Active - No skills used";
         default -> "Status: Active";
      };
      int remaining = UrgentQuestClientState.remainingSeconds();
      String timer = remaining < 0 ? "Time: No limit" : String.format("Time: %02d:%02d", remaining / 60, remaining % 60);
      graphics.drawString(font, status, x + 6, y + height - 25, -30070, false);
      graphics.drawString(font, timer, x + 6, y + height - 15, -14490, false);
   }

   private static void renderStoryQuest(GuiGraphics graphics, Player entity, int screenWidth) {
      Font font = Minecraft.getInstance().font;
      int x = 8;
      int y = 134;
      int width = Math.min(220, Math.max(150, screenWidth - 16));
      boolean dungeon = DungeoningProcedure.execute(entity);
      Component title = Component.literal(ReturnQuestNameProcedure.execute(entity));
      Component body = Component.literal(QuestLinesProcedure.execute(entity));
      List<FormattedCharSequence> bodyLines = font.split(body, width - 12);
      int height = 28 + bodyLines.size() * 10 + (dungeon ? 12 : 0);
      drawSystemPanel(graphics, x, y, width, height, dungeon ? -771223528 : -872019178, dungeon ? -14490 : -585910017);
      graphics.drawString(font, title, x + 6, y + 5, dungeon ? -14490 : -6427905, false);
      int lineY = y + 17;

      for (FormattedCharSequence line : bodyLines) {
         graphics.drawString(font, line, x + 6, lineY, -1378049, false);
         lineY += 10;
      }

      if (dungeon) {
         graphics.drawString(font, Component.translatable("gui.sololeveling.quests_overlay.label_clear_the_dungeon"), x + 6, y + height - 11, -30070, false);
      }
   }

   private static void drawSystemPanel(GuiGraphics graphics, int x, int y, int width, int height, int bg, int accent) {
      graphics.fill(x, y, x + width, y + height, bg);
      graphics.fill(x, y, x + width, y + 1, accent);
      graphics.fill(x, y + height - 1, x + width, y + height, -1724551279);
      graphics.fill(x, y, x + 1, y + height, -1724551279);
      graphics.fill(x + width - 1, y, x + width, y + height, accent);
      graphics.fill(x + 3, y + 3, x + 14, y + 4, -1726760705);
      graphics.fill(x + 3, y + 3, x + 4, y + 14, -1726760705);
   }
}
