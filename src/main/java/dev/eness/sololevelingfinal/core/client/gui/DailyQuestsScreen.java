package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemContainerScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemQuestsScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemTooltip;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.DailyQuestHelper;
import dev.eness.sololevelingfinal.core.world.inventory.DailyQuestsMenu;

public class DailyQuestsScreen extends SystemContainerScreen<DailyQuestsMenu> {
   private static final int PANEL_W = 250;
   private static final int PANEL_H = 254;
   private static final int ROW_START = 51;
   private static final int ROW_HEIGHT = 50;
   private static final int ROW_STEP = 54;
   private static final int FOOTER_Y = 219;
   private static final int ROW_FILL = 1427120952;
   private static final int ROW_COMPLETE = 1345497208;
   private static final int COMPLETE = -10230107;
   private static final int WARNING = -14756;
   private static final int DANGER = -38019;
   private static final int SECRET_ACCENT = -51891;
   private static final int SECRET_ACCENT_DIM = -6412747;
   private static final int SECRET_ACCENT_SOFT = 1728001357;
   private static final int SECRET_PANEL_FILL = -1726674670;
   private static final int SECRET_ROW_FILL = 1713637907;
   private final Player entity;
   private boolean returnToQuests;

   public DailyQuestsScreen(DailyQuestsMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.entity = container.entity;
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.pRelX = -125;
      this.pRelY = -127;
      this.pW = 250;
      this.pH = 254;
   }

   @Override
   protected void init() {
      this.returnToQuests = false;
      super.init();
      int panelLeft = this.leftPos + this.pRelX;
      int panelTop = this.topPos + this.pRelY;
      this.addRenderableWidget(
         new SystemScreen.SystemButton(panelLeft + 3, panelTop + 2, 42, 12, Component.literal("< Back"), button -> this.returnToQuestHub())
      );
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      int panelLeft = this.leftPos + this.pRelX;
      int panelTop = this.topPos + this.pRelY;
      SololevelingModVariables.PlayerVariables vars = this.variables();
      boolean secretRevealed = this.isActiveSecretRevealed(vars);
      int accent = secretRevealed ? -51891 : -12597505;
      int accentDim = secretRevealed ? -6412747 : -14519384;
      int accentSoft = secretRevealed ? 1728001357 : 1430243071;
      if (secretRevealed) {
         drawSecretPanel(g, panelLeft, panelTop, this.pW, this.pH);
         drawSecretTitleBar(g, this.font, panelLeft, panelTop, this.pW);
      } else {
         ShopStyle.panel(g, panelLeft, panelTop, this.pW, this.pH);
         ShopStyle.titleBar(g, this.font, panelLeft, panelTop, this.pW, "DAILY QUEST");
      }

      for (int objective = 0; objective < 3; objective++) {
         int rowY = panelTop + 51 + objective * 54;
         double progress = objectiveProgress(vars, objective);
         double target = this.objectiveTarget(objective);
         boolean complete = objectiveComplete(vars, objective, progress, target);
         drawObjectiveRow(g, panelLeft + 9, rowY, this.pW - 18, 50, complete ? 1345497208 : (secretRevealed ? 1713637907 : 1427120952), accentDim, accentSoft);
         drawProgressBar(
            g,
            panelLeft + 14,
            rowY + 39,
            this.pW - 28,
            6,
            objective == 1 && vars.dailyCombatWaived ? visibleTarget(objective) : progress,
            visibleTarget(objective),
            complete ? -10230107 : accent,
            accentDim
         );
      }

      g.fill(panelLeft + 9, panelTop + 219, panelLeft + this.pW - 9, panelTop + 219 + 1, accentSoft);
      RenderSystem.disableBlend();
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      SololevelingModVariables.PlayerVariables vars = this.variables();
      boolean complete = this.allObjectivesComplete(vars);
      boolean secretRevealed = this.isActiveSecretRevealed(vars);
      int accent = secretRevealed ? -51891 : -12597505;
      String status = this.questStatus(vars, complete);
      g.drawString(this.font, status, this.pRelX + 12, this.pRelY + 24, this.statusColor(vars, complete), false);
      String timer = this.timerText(vars);
      g.drawString(this.font, timer, this.pRelX + this.pW - 12 - this.font.width(timer), this.pRelY + 24, this.timerColor(vars), false);
      g.drawString(
         this.font, secretRevealed ? "SECRET OBJECTIVES" : "AUTOMATIC OBJECTIVES", this.pRelX + 12, this.pRelY + 38, secretRevealed ? accent : -7358248, false
      );

      for (int objective = 0; objective < 3; objective++) {
         int rowY = this.pRelY + 51 + objective * 54;
         double progress = objectiveProgress(vars, objective);
         boolean objectiveComplete = objectiveComplete(vars, objective, progress, this.objectiveTarget(objective));
         g.drawString(this.font, objectiveName(objective), this.pRelX + 14, rowY + 7, objectiveComplete ? -10230107 : -1509633, false);
         String progressText = this.objectiveProgressText(vars, objective, progress, objectiveComplete);
         g.drawString(
            this.font, progressText, this.pRelX + this.pW - 14 - this.font.width(progressText), rowY + 7, objectiveComplete ? -10230107 : accent, false
         );
         this.drawLeftFitted(g, objectiveDescription(vars, objective), this.pRelX + 14, rowY + 22, this.pW - 28, secretRevealed ? -22350 : -7358248);
      }

      String footer = !vars.ActiveDaily && complete
         ? "Today's Daily Quest is complete."
         : (!vars.ActiveDaily ? "No active Daily Quest." : (complete ? "All objectives complete." : "Progress is tracked automatically."));
      this.drawCenteredFitted(g, footer, this.pRelY + 228, complete ? -10230107 : (!vars.ActiveDaily ? -38019 : -1509633));
      this.drawCenteredFitted(g, "Open this panel anytime to check your progress.", this.pRelY + 241, -7358248);
   }

   @Override
   protected void renderExtras(GuiGraphics g, int mouseX, int mouseY) {
      int panelLeft = this.leftPos + this.pRelX;
      int panelTop = this.topPos + this.pRelY;

      for (int objective = 0; objective < 3; objective++) {
         int rowY = panelTop + 51 + objective * 54;
         if (isOver(mouseX, mouseY, panelLeft + 9, rowY, this.pW - 18, 50)) {
            SystemTooltip.render(g, this.font, objectiveTooltip(objective), mouseX, mouseY, this.width, this.height);
            return;
         }
      }
   }

   @Override
   protected void onCloseAnimationFinished() {
      if (this.returnToQuests) {
         this.returnToQuests = false;
         if (this.minecraft != null && this.minecraft.player != null && this.minecraft.getConnection() != null) {
            this.minecraft.setScreen(new SystemQuestsScreen());
         }
      }
   }

   private void returnToQuestHub() {
      this.returnToQuests = true;
      this.beginClose();
   }

   private String questStatus(SololevelingModVariables.PlayerVariables vars, boolean complete) {
      if (!vars.ActiveDaily && complete) {
         return "COMPLETED";
      } else if (!vars.ActiveDaily) {
         return "INACTIVE";
      } else if (complete) {
         return "ALL OBJECTIVES COMPLETE";
      } else {
         return DailyQuestHelper.isSecretQuestRevealed(this.entity) ? "SECRET OBJECTIVES ACTIVE" : "QUEST IN PROGRESS";
      }
   }

   private int statusColor(SololevelingModVariables.PlayerVariables vars, boolean complete) {
      if (complete) {
         return -10230107;
      } else if (!vars.ActiveDaily) {
         return -38019;
      } else {
         return DailyQuestHelper.isSecretQuestRevealed(this.entity) ? -51891 : -12597505;
      }
   }

   private boolean allObjectivesComplete(SololevelingModVariables.PlayerVariables vars) {
      return vars.dailyMinedBlocks >= this.objectiveTarget(0)
         && (vars.dailyCombatWaived || vars.dailyThreatPoints >= this.objectiveTarget(1))
         && vars.RUN >= this.objectiveTarget(2);
   }

   private boolean isActiveSecretRevealed(SololevelingModVariables.PlayerVariables vars) {
      return vars.ActiveDaily && DailyQuestHelper.isSecretQuestRevealed(this.entity);
   }

   private static boolean objectiveComplete(SololevelingModVariables.PlayerVariables vars, int objective, double progress, double target) {
      return objective == 1 && vars.dailyCombatWaived || progress >= target;
   }

   private String timerText(SololevelingModVariables.PlayerVariables vars) {
      if (vars.ActiveDaily && !(vars.dailytimer <= 0.0)) {
         long seconds = Math.max(0L, (long)Math.ceil(vars.dailytimer / 20.0));
         return String.format(Locale.ROOT, "%02d:%02d", seconds / 60L, seconds % 60L);
      } else {
         return "--:--";
      }
   }

   private int timerColor(SololevelingModVariables.PlayerVariables vars) {
      if (!vars.ActiveDaily || vars.dailytimer <= 0.0) {
         return -38019;
      } else if (vars.dailytimer <= 1200.0) {
         return -38019;
      } else {
         return vars.dailytimer <= 6000.0 ? -14756 : -1509633;
      }
   }

   private SololevelingModVariables.PlayerVariables variables() {
      return this.entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private double objectiveTarget(int objective) {
      boolean secret = DailyQuestHelper.isSecretQuest(this.entity);

      return switch (objective) {
         case 0 -> secret ? 64.0 : 32.0;
         case 1 -> secret ? 16.0 : 8.0;
         case 2 -> secret ? 1000.0 : 500.0;
         default -> 1.0;
      };
   }

   private static double visibleTarget(int objective) {
      return switch (objective) {
         case 0 -> 32.0;
         case 1 -> 8.0;
         case 2 -> 500.0;
         default -> 1.0;
      };
   }

   private static double objectiveProgress(SololevelingModVariables.PlayerVariables vars, int objective) {
      return switch (objective) {
         case 0 -> vars.dailyMinedBlocks;
         case 1 -> vars.dailyThreatPoints;
         case 2 -> vars.RUN;
         default -> 0.0;
      };
   }

   private static String objectiveName(int objective) {
      return switch (objective) {
         case 0 -> "MINING DRILL";
         case 1 -> "COMBAT READINESS";
         case 2 -> "ENDURANCE";
         default -> "";
      };
   }

   private static String objectiveDescription(SololevelingModVariables.PlayerVariables vars, int objective) {
      return switch (objective) {
         case 0 -> "Stone/ores, Nether stone, End Stone, mana deposits";
         case 1 -> vars.dailyCombatWaived ? "Waived for this quest (Peaceful difficulty)" : "Defeat hostiles or your System Training Bot";
         case 2 -> "Travel on foot";
         default -> "";
      };
   }

   private static List<Component> objectiveTooltip(int objective) {
      return switch (objective) {
         case 0 -> List.of(
            Component.literal("Counts: natural stone, deepslate and ores."),
            Component.literal("Also: netherrack, blackstone, basalt and end stone."),
            Component.literal("Mana Crystal Deposits count. Use the correct tool.")
         );
         case 1 -> List.of(
            Component.literal("Defeat hostile creatures to earn combat points."),
            Component.literal("Stronger enemies can be worth more points."),
            Component.literal("Your System Training Bot is worth 8 points.")
         );
         case 2 -> List.of(
            Component.literal("Walking and sprinting are tracked automatically."), Component.literal("Vehicles, flight and teleportation do not count.")
         );
         default -> List.of();
      };
   }

   private String objectiveProgressText(SololevelingModVariables.PlayerVariables vars, int objective, double progress, boolean complete) {
      if (objective == 1 && vars.dailyCombatWaived) {
         return "WAIVED";
      } else if (complete) {
         return "COMPLETE";
      } else {
         double visibleTarget = visibleTarget(objective);
         boolean hiddenRemainder = this.isActiveSecretRevealed(vars) && progress >= visibleTarget;
         if (objective == 2) {
            return hiddenRemainder ? formatDistance(visibleTarget) + "+ / ??? KM" : formatDistance(progress) + " / " + formatDistance(visibleTarget) + " KM";
         } else {
            return hiddenRemainder ? Math.round(visibleTarget) + "+ / ???" : Math.round(progress) + " / " + Math.round(visibleTarget);
         }
      }
   }

   private void drawCenteredFitted(GuiGraphics g, String text, int y, int color) {
      int maxWidth = this.pW - 24;
      String fitted = text;
      if (this.font.width(fitted) > maxWidth) {
         String suffix = "...";
         fitted = this.font.plainSubstrByWidth(fitted, maxWidth - this.font.width(suffix)) + suffix;
      }

      g.drawString(this.font, fitted, this.pRelX + (this.pW - this.font.width(fitted)) / 2, y, color, false);
   }

   private void drawLeftFitted(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
      String fitted = this.font.width(text) <= maxWidth ? text : this.font.plainSubstrByWidth(text, maxWidth - this.font.width("...")) + "...";
      g.drawString(this.font, fitted, x, y, color, false);
   }

   private static String formatDistance(double blocks) {
      double distance = blocks / 50.0;
      return Math.abs(distance - Math.rint(distance)) < 0.001 ? Long.toString(Math.round(distance)) : String.format(Locale.ROOT, "%.1f", distance);
   }

   private static void drawObjectiveRow(GuiGraphics g, int x, int y, int width, int height, int fill, int accentDim, int accentSoft) {
      g.fill(x, y, x + width, y + height, fill);
      g.fill(x, y, x + width, y + 1, accentDim);
      g.fill(x, y + height - 1, x + width, y + height, accentSoft);
      g.fill(x, y, x + 1, y + height, accentDim);
      g.fill(x + width - 1, y, x + width, y + height, accentDim);
   }

   private static void drawProgressBar(GuiGraphics g, int x, int y, int width, int height, double progress, double target, int color, int accentDim) {
      g.fill(x, y, x + width, y + height, -1442510318);
      g.fill(x, y, x + width, y + 1, accentDim);
      if (!(target <= 0.0)) {
         int filled = Math.max(0, Math.min(width, (int)Math.round(width * progress / target)));
         if (filled > 0) {
            g.fill(x, y + 1, x + filled, y + height - 1, color);
         }
      }
   }

   private static void drawSecretPanel(GuiGraphics g, int x, int y, int width, int height) {
      g.fill(x, y, x + width, y + height, -1726674670);
      g.fill(x - 1, y - 1, x + width + 1, y, 1728001357);
      g.fill(x - 1, y + height, x + width + 1, y + height + 1, 1728001357);
      g.fill(x - 1, y, x, y + height, 1728001357);
      g.fill(x + width, y, x + width + 1, y + height, 1728001357);
      g.fill(x, y, x + width, y + 1, -6412747);
      g.fill(x, y + height - 1, x + width, y + height, -6412747);
      g.fill(x, y, x + 1, y + height, -6412747);
      g.fill(x + width - 1, y, x + width, y + height, -6412747);
      drawSecretCorners(g, x, y, width, height);
   }

   private static void drawSecretCorners(GuiGraphics g, int x, int y, int width, int height) {
      int length = 10;
      g.fill(x - 1, y - 1, x + length, y + 1, -51891);
      g.fill(x - 1, y - 1, x + 1, y + length, -51891);
      g.fill(x + width - length, y - 1, x + width + 1, y + 1, -51891);
      g.fill(x + width - 1, y - 1, x + width + 1, y + length, -51891);
      g.fill(x - 1, y + height - 1, x + length, y + height + 1, -51891);
      g.fill(x - 1, y + height - length, x + 1, y + height + 1, -51891);
      g.fill(x + width - length, y + height - 1, x + width + 1, y + height + 1, -51891);
      g.fill(x + width - 1, y + height - length, x + width + 1, y + height + 1, -51891);
   }

   private static void drawSecretTitleBar(GuiGraphics g, Font font, int x, int y, int width) {
      String title = "SECRET QUEST";
      g.fill(x, y, x + width, y + 16, 1999309072);
      g.fill(x, y + 16, x + width, y + 17, -51891);
      g.drawString(font, title, x + (width - font.width(title)) / 2, y + 4, -51891, false);
   }

   private static boolean isOver(int mouseX, int mouseY, int x, int y, int width, int height) {
      return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }
}
