package dev.eness.sololevelingfinal.core.client.gui.system;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.AbilitiesGUIButtonMessage;
import dev.eness.sololevelingfinal.core.network.PanelRework2ButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.ReturnAgilityProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnFatigueProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnHPProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnIntelligenceProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnJobProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnLevelProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnMPProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnNameProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnPerceptionProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnRemainingXPProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnSPProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnStrengthProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnTitleProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnVitalityProcedure;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class SystemPanelScreen extends SystemScreen {
   private static final int STAT_ROW0 = 136;
   private static final int STAT_STEP = 16;
   private static final List<List<Component>> STAT_TIPS = List.of(
      List.of(tipTitle("Strength"), tipText("Increases physical attack damage.")),
      List.of(tipTitle("Agility"), tipText("Increases movement speed and mobility.")),
      List.of(tipTitle("Perception"), tipText("Raises your chance to auto-dodge attacks.")),
      List.of(tipTitle("Vitality"), tipText("Increases maximum health (HP) and armor.")),
      List.of(tipTitle("Intelligence"), tipText("Increases your maximum mana (MP)."))
   );

   public SystemPanelScreen() {
      super(Component.literal("SYSTEM"));
      this.panelW = 196;
      this.panelH = 282;
   }

   @Override
   protected void init() {
      super.init();
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.panelX + this.panelW - 15, this.panelY + 3, 12, 12, Component.literal("X"), b -> this.beginClose())
      );
      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.panelX + this.panelW - 29, this.panelY + 3, 12, 12, Component.literal("S"), b -> this.openChild(new SystemSettingsScreen())
         )
      );
      this.addRenderableWidget(
         new SystemPanelScreen.InvisibleButton(this.panelX + 12, this.panelY + 46, 135, 14, b -> this.openChild(new SystemTitlesScreen()))
      );

      for (int i = 0; i < 5; i++) {
         int id = i;
         int y = this.panelY + 136 + i * 16;
         this.addRenderableWidget(
            new SystemScreen.SystemButton(this.panelX + this.panelW - 24, y - 2, 14, 12, Component.literal("+"), b -> this.sendPanelAction(id))
         );
      }

      Runnable[] navActions = new Runnable[]{
         () -> this.sendPanelAction(5),
         () -> this.openChild(new SystemQuestsScreen()),
         () -> this.openChild(new SystemRewardsScreen()),
         () -> this.openChild(new PartyScreen(false)),
         () -> this.sendPanelAction(8),
         this::openSkills
      };
      String[] navLabels = new String[]{"Shop", "Quests", "Rewards", "Party", "Craft", "Skills"};
      int navX0 = this.panelX + 12;
      int navY0 = this.panelY + 236;
      int navW = 53;
      int navH = 18;
      int gap = 6;

      for (int i = 0; i < navLabels.length; i++) {
         Runnable action = navActions[i];
         int col = i % 3;
         int row = i / 3;
         int x = navX0 + col * (navW + gap);
         int y = navY0 + row * (navH + gap);
         this.addRenderableWidget(new SystemScreen.SystemButton(x, y, navW, navH, Component.literal(navLabels[i]), b -> action.run()));
      }
   }

   private void openSkills() {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         BlockPos bp = player.blockPosition();
         SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(5, bp.getX(), bp.getY(), bp.getZ()));
      }
   }

   private void sendPanelAction(int id) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         BlockPos bp = player.blockPosition();
         SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(id, bp.getX(), bp.getY(), bp.getZ()));
      }
   }

   @Override
   protected void renderContent(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      Player entity = Minecraft.getInstance().player;
      if (entity != null) {
         Font font = this.font;
         int lx = this.panelX + 12;
         g.drawString(font, ReturnLevelProcedure.execute(entity), lx, this.panelY + 24, -1509633, false);
         g.drawString(font, ReturnNameProcedure.execute(entity), lx, this.panelY + 36, -1509633, false);
         g.drawString(font, ReturnTitleProcedure.execute(entity), lx, this.panelY + 48, -7358248, false);
         g.drawString(font, ReturnJobProcedure.execute(entity), lx, this.panelY + 60, -7358248, false);
         g.drawString(font, ReturnFatigueProcedure.execute(entity), lx, this.panelY + 72, -7358248, false);
         this.divider(g, this.panelY + 86);
         g.drawString(font, ReturnHPProcedure.execute(entity), lx, this.panelY + 92, -38037, false);
         g.drawString(font, ReturnMPProcedure.execute(entity), lx, this.panelY + 104, -9717505, false);
         this.divider(g, this.panelY + 118);
         g.drawString(font, "ATTRIBUTES", lx, this.panelY + 122, -12597505, false);

         for (int i = 0; i < 5; i++) {
            String baseText = statString(entity, i);
            int y = this.panelY + 136 + i * 16;
            g.drawString(font, baseText, lx, y, -1509633, false);
            Component bonus = bonusComponent(entity, i);
            if (!bonus.getString().isEmpty()) {
               g.drawString(font, bonus, lx + font.width(baseText), y, -1, false);
            }
         }

         g.drawString(font, ReturnSPProcedure.execute(entity), lx, this.panelY + 218, -9882, false);
         this.divider(g, this.panelY + 230);
      }
   }

   @Override
   protected List<Component> getHoverTooltip(int mouseX, int mouseY) {
      Player entity = Minecraft.getInstance().player;
      if (entity == null) {
         return null;
      }

      Font font = this.font;
      int lx = this.panelX + 12;
      String levelStr = ReturnLevelProcedure.execute(entity);
      if (isOver(mouseX, mouseY, lx, this.panelY + 24, font.width(levelStr), 10)) {
         return List.of(tipTitle("Level Progress"), tipText(ReturnRemainingXPProcedure.execute(entity)));
      }

      for (int i = 0; i < 5; i++) {
         int y = this.panelY + 136 + i * 16;
         String baseText = statString(entity, i);
         Component bonus = bonusComponent(entity, i);
         int bonusX = lx + font.width(baseText);
         if (!bonus.getString().isEmpty() && isOver(mouseX, mouseY, bonusX, y, font.width(bonus), 10)) {
            return bonusTooltip(entity, i);
         }

         if (isOver(mouseX, mouseY, lx, y, font.width(baseText), 10)) {
            return STAT_TIPS.get(i);
         }
      }

      return isOver(mouseX, mouseY, this.panelX + this.panelW - 29, this.panelY + 3, 12, 12)
         ? List.of(tipTitle("Settings"), tipText("Open System settings."))
         : null;
   }

   private static Component tipTitle(String text) {
      return Component.literal(text).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
   }

   private static Component tipText(String text) {
      return Component.literal(text).withStyle(ChatFormatting.GRAY);
   }

   private static String statString(Player entity, int i) {
      return switch (i) {
         case 0 -> ReturnStrengthProcedure.execute(entity);
         case 1 -> ReturnAgilityProcedure.execute(entity);
         case 2 -> ReturnPerceptionProcedure.execute(entity);
         case 3 -> ReturnVitalityProcedure.execute(entity);
         default -> ReturnIntelligenceProcedure.execute(entity);
      };
   }

   private static Component bonusComponent(Player entity, int statIndex) {
      double bonus = TemporaryStatBonusManager.bonusValue(entity, stat(statIndex));
      return bonus <= 0.0
         ? Component.empty()
         : Component.literal(" (+" + TemporaryStatBonusManager.format(bonus) + ")").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
   }

   private static List<Component> bonusTooltip(Player entity, int statIndex) {
      TemporaryStatBonusManager.Stat stat = stat(statIndex);
      List<TemporaryStatBonusManager.BonusSource> sources = TemporaryStatBonusManager.sources(entity, stat);
      double total = sources.stream().mapToDouble(TemporaryStatBonusManager.BonusSource::amount).sum();
      List<Component> tooltip = new ArrayList<>();
      tooltip.add(
         Component.literal("Temporary " + stat.displayName() + " (+" + TemporaryStatBonusManager.format(total) + ")")
            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
      );

      for (TemporaryStatBonusManager.BonusSource source : sources) {
         tooltip.add(
            Component.literal("+" + TemporaryStatBonusManager.format(source.amount()) + " from ")
               .withStyle(ChatFormatting.GREEN)
               .append(source.displayName().copy().withStyle(ChatFormatting.GRAY))
         );
      }

      return tooltip;
   }

   private static TemporaryStatBonusManager.Stat stat(int statIndex) {
      return switch (statIndex) {
         case 0 -> TemporaryStatBonusManager.Stat.STRENGTH;
         case 1 -> TemporaryStatBonusManager.Stat.AGILITY;
         case 2 -> TemporaryStatBonusManager.Stat.PERCEPTION;
         case 3 -> TemporaryStatBonusManager.Stat.VITALITY;
         default -> TemporaryStatBonusManager.Stat.INTELLIGENCE;
      };
   }

   private void divider(GuiGraphics g, int y) {
      g.fill(this.panelX + 10, y, this.panelX + this.panelW - 10, y + 1, 1430243071);
   }

   private static class InvisibleButton extends Button {
      InvisibleButton(int x, int y, int w, int h, OnPress onPress) {
         super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
      }

      @Override
      protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      }
   }
}
