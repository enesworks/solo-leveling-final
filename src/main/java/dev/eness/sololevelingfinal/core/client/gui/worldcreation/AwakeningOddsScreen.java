package dev.eness.sololevelingfinal.core.client.gui.worldcreation;

import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import dev.eness.sololevelingfinal.core.util.HunterEvaluationRules;

public final class AwakeningOddsScreen extends Screen {
   private static final String[] RANK_LABELS = new String[]{"E", "D", "C", "B", "A", "S"};
   private static final int[] RANK_COLORS = new int[]{-7695200, -11025301, -11886849, -5216257, -24003, -8090};
   private static final int CONTROL_WIDTH = 300;
   private static final int CONTROL_HEIGHT = 20;
   private static final int ROW_SPACING = 3;
   private final Screen parent;
   private final Consumer<int[]> onApply;
   private final int[] weights;
   private final AwakeningOddsScreen.WeightSlider[] sliders = new AwakeningOddsScreen.WeightSlider[6];
   private int listTop;

   public AwakeningOddsScreen(Screen parent, int[] initial, Consumer<int[]> onApply) {
      super(Component.literal("Awakening Odds"));
      this.parent = parent;
      this.onApply = onApply;
      this.weights = new int[6];

      for (int index = 0; index < this.weights.length; index++) {
         this.weights[index] = initial != null && index < initial.length ? Mth.clamp(initial[index], 0, 100) : HunterEvaluationRules.DEFAULT_RANK_ODDS[index];
      }
   }

   @Override
   protected void init() {
      int left = (this.width - 300) / 2;
      int rows = 6;
      int blockHeight = rows * 23;
      this.listTop = Math.max(56, (this.height - blockHeight) / 2 - 6);

      for (int index = 0; index < rows; index++) {
         int rank = index;
         AwakeningOddsScreen.WeightSlider slider = new AwakeningOddsScreen.WeightSlider(left, this.listTop + index * 23, rank);
         slider.setTooltip(Tooltip.create(Component.literal("Relative weight for " + RANK_LABELS[rank] + "-Rank. Values are rescaled so all six total 100%.")));
         this.sliders[rank] = this.addRenderableWidget(slider);
      }

      int buttonY = this.listTop + blockHeight + 30;
      int half = 148;
      this.addRenderableWidget(Button.builder(Component.literal("Reset to Default"), button -> this.reset()).bounds(left, buttonY, half, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Even Split"), button -> this.evenSplit()).bounds(left + half + 4, buttonY, half, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.applyAndClose()).bounds(left, buttonY + 20 + 4, half, 20).build());
      this.addRenderableWidget(
         Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(left + half + 4, buttonY + 20 + 4, half, 20).build()
      );
      this.refreshLabels();
   }

   private void reset() {
      System.arraycopy(HunterEvaluationRules.DEFAULT_RANK_ODDS, 0, this.weights, 0, this.weights.length);
      this.syncSliders();
   }

   private void evenSplit() {
      for (int index = 0; index < this.weights.length; index++) {
         this.weights[index] = 100 / this.weights.length;
      }

      this.syncSliders();
   }

   private void syncSliders() {
      for (AwakeningOddsScreen.WeightSlider slider : this.sliders) {
         if (slider != null) {
            slider.pullFromWeights();
         }
      }

      this.refreshLabels();
   }

   private void refreshLabels() {
      int[] normalized = HunterEvaluationRules.normalizedRankOdds(this.weights);

      for (int index = 0; index < this.sliders.length; index++) {
         if (this.sliders[index] != null) {
            this.sliders[index].showEffective(normalized[index]);
         }
      }
   }

   private void applyAndClose() {
      if (this.onApply != null) {
         this.onApply.accept((int[])this.weights.clone());
      }

      this.onClose();
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(graphics);
      graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
      graphics.drawCenteredString(
         this.font, Component.literal("Chance of awakening at each rank when first evaluated").withStyle(ChatFormatting.GRAY), this.width / 2, 28, 10526880
      );
      int[] normalized = HunterEvaluationRules.normalizedRankOdds(this.weights);
      int total = 0;

      for (int weight : this.weights) {
         total += weight;
      }

      int left = (this.width - 300) / 2;
      this.renderDistributionBar(graphics, left, this.listTop - 16, normalized);
      String summary = total == 100 ? "Total 100% - used as entered" : String.format(Locale.ROOT, "Total %d%% - rescaled to 100%%", total);
      graphics.drawCenteredString(
         this.font,
         Component.literal(summary).withStyle(total == 100 ? ChatFormatting.GREEN : ChatFormatting.YELLOW),
         this.width / 2,
         this.listTop + this.sliders.length * 23 + 8,
         16777215
      );
      if (total <= 0) {
         graphics.drawCenteredString(
            this.font,
            Component.literal("All zero - defaults will be used").withStyle(ChatFormatting.RED),
            this.width / 2,
            this.listTop + this.sliders.length * 23 + 19,
            16777215
         );
      }

      super.render(graphics, mouseX, mouseY, partialTicks);
   }

   private void renderDistributionBar(GuiGraphics graphics, int x, int y, int[] normalized) {
      graphics.fill(x - 1, y - 1, x + 300 + 1, y + 7, -15723496);
      int cursor = x;

      for (int index = 0; index < normalized.length; index++) {
         int segment = normalized[index] * 300 / 100;
         if (index == normalized.length - 1) {
            segment = x + 300 - cursor;
         }

         if (segment > 0) {
            graphics.fill(cursor, y, cursor + segment, y + 6, RANK_COLORS[index]);
            cursor += segment;
         }
      }
   }

   private final class WeightSlider extends AbstractSliderButton {
      private final int rank;
      private int effective;

      private WeightSlider(int x, int y, int rank) {
         super(x, y, 300, 20, Component.empty(), AwakeningOddsScreen.this.weights[rank] / 100.0);
         this.rank = rank;
         this.effective = AwakeningOddsScreen.this.weights[rank];
         this.updateMessage();
      }

      private void pullFromWeights() {
         this.value = AwakeningOddsScreen.this.weights[this.rank] / 100.0;
         this.updateMessage();
      }

      private void showEffective(int percent) {
         this.effective = percent;
         this.updateMessage();
      }

      @Override
      protected void updateMessage() {
         int weight = AwakeningOddsScreen.this.weights[this.rank];
         this.setMessage(Component.literal(AwakeningOddsScreen.RANK_LABELS[this.rank] + "-Rank:  " + weight + "  ->  " + this.effective + "%"));
      }

      @Override
      protected void applyValue() {
         AwakeningOddsScreen.this.weights[this.rank] = Mth.clamp((int)Math.round(this.value * 100.0), 0, 100);
         AwakeningOddsScreen.this.refreshLabels();
      }
   }
}
