package dev.eness.sololevelingfinal.core.client.gui.worldcreation;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.IntConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.GameRules.Key;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.util.HunterRankOdds;

public final class SoloLevelingWorldCreationTab extends GridLayoutTab {
   private static final int COLUMN_WIDTH = 152;
   private static final int COLUMN_GAP = 8;
   private static final int FULL_WIDTH = 312;
   private static final int CONTROL_HEIGHT = 20;
   private final WorldCreationUiState uiState;
   private final CycleButton<Boolean> storyMode;
   private final CycleButton<Boolean> abilityDestruction;
   private final CycleButton<SoloLevelingWorldCreationTab.ProgressionPreset> progression;
   private final CycleButton<SoloLevelingWorldCreationTab.DifficultyPreset> difficulty;
   private final CycleButton<Integer> xpRate;
   private final SoloLevelingWorldCreationTab.JobChangeLevelSlider jobChange;
   private final CycleButton<Integer> enemyScale;
   private final CycleButton<Integer> bossPower;
   private final CycleButton<Boolean> gateProgress;
   private final CycleButton<SoloLevelingWorldCreationTab.DeathRule> deathRules;
   private final Button awakeningOdds;
   private boolean synchronizing;

   public SoloLevelingWorldCreationTab(Font font, WorldCreationUiState uiState) {
      super(Component.literal("Solo Leveling"));
      this.uiState = uiState;
      GameRules rules = uiState.getGameRules();
      normalizeOfferedValues(rules);
      SoloLevelingWorldCreationTab.ProgressionPreset storedProgression = SoloLevelingWorldCreationTab.ProgressionPreset.fromId(
         rules.getInt(SololevelingModGameRules.SOLO_LEVELING_PROGRESSION_PRESET)
      );
      SoloLevelingWorldCreationTab.ProgressionPreset initialProgression = storedProgression == SoloLevelingWorldCreationTab.ProgressionPreset.CUSTOM
         ? SoloLevelingWorldCreationTab.ProgressionPreset.CUSTOM
         : progressionFor(rules);
      SoloLevelingWorldCreationTab.DifficultyPreset storedDifficulty = SoloLevelingWorldCreationTab.DifficultyPreset.fromId(
         rules.getInt(SololevelingModGameRules.SOLO_LEVELING_DIFFICULTY_PRESET)
      );
      SoloLevelingWorldCreationTab.DifficultyPreset initialDifficulty = storedDifficulty == SoloLevelingWorldCreationTab.DifficultyPreset.CUSTOM
         ? SoloLevelingWorldCreationTab.DifficultyPreset.CUSTOM
         : difficultyFor(rules);
      writeInteger(rules, SololevelingModGameRules.SOLO_LEVELING_PROGRESSION_PRESET, initialProgression.id);
      writeInteger(rules, SololevelingModGameRules.SOLO_LEVELING_DIFFICULTY_PRESET, initialDifficulty.id);
      this.storyMode = CycleButton.onOffBuilder(rules.getBoolean(SololevelingModGameRules.SOLO_LEVELING_STORY_MODE))
         .withTooltip(value -> Tooltip.create(Component.literal("Start with the Solo Leveling story introduction. Designed for a single-player experience.")))
         .create(0, 0, 312, 20, Component.literal("Story Mode"), (button, value) -> {
            this.writeBoolean(SololevelingModGameRules.SOLO_LEVELING_STORY_MODE, value);
            this.notifyChanged();
         });
      this.abilityDestruction = CycleButton.onOffBuilder(rules.getBoolean(SololevelingModGameRules.SOLO_ABILITY_DESTRUCTION))
         .withTooltip(
            value -> Tooltip.create(
               Component.literal(
                  value
                     ? "High-impact abilities can destroy eligible terrain. Block drops are suppressed and protected blocks remain safe."
                     : "Abilities cannot damage terrain. This is the safe default and can be changed later with /gamerule soloAbilityDestruction."
               )
            )
         )
         .create(0, 0, 312, 20, Component.literal("Ability Destruction"), (button, value) -> {
            this.writeBoolean(SololevelingModGameRules.SOLO_ABILITY_DESTRUCTION, value);
            this.notifyChanged();
         });
      this.xpRate = this.integerOption(
         "XP Rate",
         "Adjusts experience gained from Solo Leveling content.",
         List.of(10, 15, 20),
         value -> Component.literal(String.format(Locale.ROOT, "%.1fx", value.intValue() / 10.0)),
         closest(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER), List.of(10, 15, 20)),
         value -> {
            this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER, value);
            this.syncProgressionPreset();
         }
      );
      this.jobChange = new SoloLevelingWorldCreationTab.JobChangeLevelSlider(
         Mth.clamp(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_JOB_CHANGE_LEVEL), 20, 100), value -> {
            this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_JOB_CHANGE_LEVEL, value);
            this.syncProgressionPreset();
         }
      );
      this.jobChange.setTooltip(Tooltip.create(Component.literal("Sets the level at which the Job Change quest unlocks (20-100).")));
      this.enemyScale = this.integerOption(
         "Enemy Scale",
         "Controls the health and damage of Solo Leveling enemies.",
         List.of(100, 125, 150),
         SoloLevelingWorldCreationTab::enemyScaleLabel,
         closest(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_ENEMY_SCALE), List.of(100, 125, 150)),
         value -> {
            this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_ENEMY_SCALE, value);
            this.syncDifficultyPreset();
         }
      );
      this.bossPower = this.integerOption(
         "Boss Power",
         "Adjusts boss health, damage, and resistance.",
         List.of(100, 125, 150),
         SoloLevelingWorldCreationTab::bossPowerLabel,
         closest(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_BOSS_POWER), List.of(100, 125, 150)),
         value -> {
            this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_BOSS_POWER, value);
            this.syncDifficultyPreset();
         }
      );
      this.gateProgress = CycleButton.<Boolean>builder(value -> Component.literal(value ? "Ranked" : "Open"))
         .withValues(true, false)
         .withInitialValue(rules.getBoolean(SololevelingModGameRules.SOLO_LEVELING_RANKED_GATES))
         .withTooltip(value -> Tooltip.create(Component.literal("Ranked limits natural gates to your current progression; Open allows every gate rank.")))
         .create(0, 0, 152, 20, Component.literal("Gate Progress"), (button, value) -> {
            this.writeBoolean(SololevelingModGameRules.SOLO_LEVELING_RANKED_GATES, value);
            this.syncProgressionPreset();
         });
      this.deathRules = CycleButton.<SoloLevelingWorldCreationTab.DeathRule>builder(value -> Component.literal(value.label))
         .withValues(SoloLevelingWorldCreationTab.DeathRule.values())
         .withInitialValue(SoloLevelingWorldCreationTab.DeathRule.fromId(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_DEATH_RULES)))
         .withTooltip(value -> Tooltip.create(Component.literal(value.tooltip)))
         .create(0, 0, 152, 20, Component.literal("Death Rules"), (button, value) -> {
            this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_DEATH_RULES, value.id);
            this.syncDifficultyPreset();
         });
      this.progression = CycleButton.<SoloLevelingWorldCreationTab.ProgressionPreset>builder(value -> Component.literal(value.label))
         .withValues(SoloLevelingWorldCreationTab.ProgressionPreset.values())
         .withInitialValue(initialProgression)
         .withTooltip(value -> Tooltip.create(Component.literal(value.tooltip)))
         .create(0, 0, 152, 20, Component.literal("Progression"), (button, value) -> this.applyProgressionPreset(value));
      this.difficulty = CycleButton.<SoloLevelingWorldCreationTab.DifficultyPreset>builder(value -> Component.literal(value.label))
         .withValues(SoloLevelingWorldCreationTab.DifficultyPreset.values())
         .withInitialValue(initialDifficulty)
         .withTooltip(value -> Tooltip.create(Component.literal(value.tooltip)))
         .create(0, 0, 152, 20, Component.literal("Difficulty"), (button, value) -> this.applyDifficultyPreset(value));
      this.awakeningOdds = Button.builder(Component.empty(), button -> this.openAwakeningOdds())
         .bounds(0, 0, 312, 20)
         .tooltip(Tooltip.create(Component.literal("Set the chance of awakening at each Hunter rank. Values are rescaled so all six total 100%.")))
         .build();
      this.updateAwakeningOddsLabel(rules);
      this.layout.columnSpacing(8).rowSpacing(3);
      RowHelper rows = this.layout.createRowHelper(2);
      rows.addChild(this.storyMode, 2, rows.newCellSettings().alignHorizontallyCenter());
      rows.addChild(this.abilityDestruction, 2, rows.newCellSettings().alignHorizontallyCenter());
      rows.addChild(text(font, 312, "Settings are stored with this world.", 10526880, true), 2);
      rows.addChild(heading(font, "PROGRESSION"));
      rows.addChild(heading(font, "DIFFICULTY"));
      rows.addChild(this.progression);
      rows.addChild(this.difficulty);
      rows.addChild(this.xpRate);
      rows.addChild(this.enemyScale);
      rows.addChild(this.jobChange);
      rows.addChild(this.bossPower);
      rows.addChild(this.gateProgress);
      rows.addChild(this.deathRules);
      rows.addChild(this.awakeningOdds, 2, rows.newCellSettings().alignHorizontallyCenter());
      rows.addChild(text(font, 312, "Presets update the settings below; individual changes use Custom.", 8421504, true), 2);
      this.uiState.addListener(ignored -> this.resyncWidgets());
   }

   private void openAwakeningOdds() {
      Minecraft minecraft = Minecraft.getInstance();
      Screen parent = minecraft.screen;
      GameRules rules = this.uiState.getGameRules();
      minecraft.setScreen(new AwakeningOddsScreen(parent, HunterRankOdds.readWeights(rules), weights -> {
         HunterRankOdds.writeWeights(rules, weights);
         this.updateAwakeningOddsLabel(rules);
         this.notifyChanged();
      }));
   }

   private void updateAwakeningOddsLabel(GameRules rules) {
      int[] normalized = HunterRankOdds.normalized(rules);
      this.awakeningOdds.setMessage(Component.literal("Awakening Odds...  (S " + normalized[normalized.length - 1] + "%)"));
   }

   private void applyProgressionPreset(SoloLevelingWorldCreationTab.ProgressionPreset preset) {
      this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_PROGRESSION_PRESET, preset.id);
      if (preset == SoloLevelingWorldCreationTab.ProgressionPreset.STANDARD) {
         this.setProgressionChildren(10, 40, true);
      } else if (preset == SoloLevelingWorldCreationTab.ProgressionPreset.FAST) {
         this.setProgressionChildren(15, 40, false);
      }

      this.notifyChanged();
   }

   private void setProgressionChildren(int xp, int jobLevel, boolean rankedGates) {
      this.xpRate.setValue(xp);
      this.jobChange.setLevel(jobLevel);
      this.gateProgress.setValue(rankedGates);
      this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER, xp);
      this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_JOB_CHANGE_LEVEL, jobLevel);
      this.writeBoolean(SololevelingModGameRules.SOLO_LEVELING_RANKED_GATES, rankedGates);
   }

   private void applyDifficultyPreset(SoloLevelingWorldCreationTab.DifficultyPreset preset) {
      this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_DIFFICULTY_PRESET, preset.id);
      switch (preset) {
         case NORMAL:
            this.setDifficultyChildren(100, 100, SoloLevelingWorldCreationTab.DeathRule.STANDARD);
            break;
         case HARD:
            this.setDifficultyChildren(125, 125, SoloLevelingWorldCreationTab.DeathRule.STANDARD);
            break;
         case BRUTAL:
            this.setDifficultyChildren(150, 150, SoloLevelingWorldCreationTab.DeathRule.HARSH);
         case CUSTOM:
      }

      this.notifyChanged();
   }

   private void setDifficultyChildren(int enemyPercent, int bossPercent, SoloLevelingWorldCreationTab.DeathRule deathRule) {
      this.enemyScale.setValue(enemyPercent);
      this.bossPower.setValue(bossPercent);
      this.deathRules.setValue(deathRule);
      this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_ENEMY_SCALE, enemyPercent);
      this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_BOSS_POWER, bossPercent);
      this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_DEATH_RULES, deathRule.id);
   }

   private void syncProgressionPreset() {
      SoloLevelingWorldCreationTab.ProgressionPreset preset = this.progression.getValue() == SoloLevelingWorldCreationTab.ProgressionPreset.CUSTOM
         ? SoloLevelingWorldCreationTab.ProgressionPreset.CUSTOM
         : progressionFor(this.uiState.getGameRules());
      this.progression.setValue(preset);
      this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_PROGRESSION_PRESET, preset.id);
      this.notifyChanged();
   }

   private void syncDifficultyPreset() {
      SoloLevelingWorldCreationTab.DifficultyPreset preset = this.difficulty.getValue() == SoloLevelingWorldCreationTab.DifficultyPreset.CUSTOM
         ? SoloLevelingWorldCreationTab.DifficultyPreset.CUSTOM
         : difficultyFor(this.uiState.getGameRules());
      this.difficulty.setValue(preset);
      this.writeInteger(SololevelingModGameRules.SOLO_LEVELING_DIFFICULTY_PRESET, preset.id);
      this.notifyChanged();
   }

   private static SoloLevelingWorldCreationTab.ProgressionPreset progressionFor(GameRules rules) {
      int xp = rules.getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER);
      int jobLevel = rules.getInt(SololevelingModGameRules.SOLO_LEVELING_JOB_CHANGE_LEVEL);
      boolean rankedGates = rules.getBoolean(SololevelingModGameRules.SOLO_LEVELING_RANKED_GATES);
      if (xp == 10 && jobLevel == 40 && rankedGates) {
         return SoloLevelingWorldCreationTab.ProgressionPreset.STANDARD;
      } else {
         return xp == 15 && jobLevel == 40 && !rankedGates
            ? SoloLevelingWorldCreationTab.ProgressionPreset.FAST
            : SoloLevelingWorldCreationTab.ProgressionPreset.CUSTOM;
      }
   }

   private static SoloLevelingWorldCreationTab.DifficultyPreset difficultyFor(GameRules rules) {
      int enemy = rules.getInt(SololevelingModGameRules.SOLO_LEVELING_ENEMY_SCALE);
      int boss = rules.getInt(SololevelingModGameRules.SOLO_LEVELING_BOSS_POWER);
      SoloLevelingWorldCreationTab.DeathRule deathRule = SoloLevelingWorldCreationTab.DeathRule.fromId(
         rules.getInt(SololevelingModGameRules.SOLO_LEVELING_DEATH_RULES)
      );
      if (enemy == 100 && boss == 100 && deathRule == SoloLevelingWorldCreationTab.DeathRule.STANDARD) {
         return SoloLevelingWorldCreationTab.DifficultyPreset.NORMAL;
      } else if (enemy == 125 && boss == 125 && deathRule == SoloLevelingWorldCreationTab.DeathRule.STANDARD) {
         return SoloLevelingWorldCreationTab.DifficultyPreset.HARD;
      } else {
         return enemy == 150 && boss == 150 && deathRule == SoloLevelingWorldCreationTab.DeathRule.HARSH
            ? SoloLevelingWorldCreationTab.DifficultyPreset.BRUTAL
            : SoloLevelingWorldCreationTab.DifficultyPreset.CUSTOM;
      }
   }

   private CycleButton<Integer> integerOption(
      String label, String tooltip, List<Integer> values, Function<Integer, Component> display, int initial, IntConsumer onChanged
   ) {
      return CycleButton.builder(display)
         .withValues(values)
         .withInitialValue(initial)
         .withTooltip(value -> Tooltip.create(Component.literal(tooltip)))
         .create(0, 0, 152, 20, Component.literal(label), (button, value) -> onChanged.accept(value));
   }

   private void writeInteger(Key<IntegerValue> key, int value) {
      writeInteger(this.uiState.getGameRules(), key, value);
   }

   private static void writeInteger(GameRules rules, Key<IntegerValue> key, int value) {
      rules.getRule(key).set(value, null);
   }

   private void writeBoolean(Key<BooleanValue> key, boolean value) {
      this.uiState.getGameRules().getRule(key).set(value, null);
   }

   private void notifyChanged() {
      this.uiState.setGameRules(this.uiState.getGameRules());
   }

   private void resyncWidgets() {
      if (!this.synchronizing) {
         this.synchronizing = true;

         try {
            GameRules rules = this.uiState.getGameRules();
            normalizeOfferedValues(rules);
            this.storyMode.setValue(rules.getBoolean(SololevelingModGameRules.SOLO_LEVELING_STORY_MODE));
            this.abilityDestruction.setValue(rules.getBoolean(SololevelingModGameRules.SOLO_ABILITY_DESTRUCTION));
            this.xpRate.setValue(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER));
            this.jobChange.setLevel(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_JOB_CHANGE_LEVEL));
            this.enemyScale.setValue(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_ENEMY_SCALE));
            this.bossPower.setValue(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_BOSS_POWER));
            this.gateProgress.setValue(rules.getBoolean(SololevelingModGameRules.SOLO_LEVELING_RANKED_GATES));
            this.deathRules.setValue(SoloLevelingWorldCreationTab.DeathRule.fromId(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_DEATH_RULES)));
            this.updateAwakeningOddsLabel(rules);
            SoloLevelingWorldCreationTab.ProgressionPreset storedProgression = SoloLevelingWorldCreationTab.ProgressionPreset.fromId(
               rules.getInt(SololevelingModGameRules.SOLO_LEVELING_PROGRESSION_PRESET)
            );
            SoloLevelingWorldCreationTab.ProgressionPreset displayedProgression = storedProgression == SoloLevelingWorldCreationTab.ProgressionPreset.CUSTOM
               ? SoloLevelingWorldCreationTab.ProgressionPreset.CUSTOM
               : progressionFor(rules);
            this.progression.setValue(displayedProgression);
            writeInteger(rules, SololevelingModGameRules.SOLO_LEVELING_PROGRESSION_PRESET, displayedProgression.id);
            SoloLevelingWorldCreationTab.DifficultyPreset storedDifficulty = SoloLevelingWorldCreationTab.DifficultyPreset.fromId(
               rules.getInt(SololevelingModGameRules.SOLO_LEVELING_DIFFICULTY_PRESET)
            );
            SoloLevelingWorldCreationTab.DifficultyPreset displayedDifficulty = storedDifficulty == SoloLevelingWorldCreationTab.DifficultyPreset.CUSTOM
               ? SoloLevelingWorldCreationTab.DifficultyPreset.CUSTOM
               : difficultyFor(rules);
            this.difficulty.setValue(displayedDifficulty);
            writeInteger(rules, SololevelingModGameRules.SOLO_LEVELING_DIFFICULTY_PRESET, displayedDifficulty.id);
         } finally {
            this.synchronizing = false;
         }
      }
   }

   private static void normalizeOfferedValues(GameRules rules) {
      normalize(rules, SololevelingModGameRules.SOLO_LEVELING_PROGRESSION_PRESET, List.of(0, 1, 2));
      normalize(rules, SololevelingModGameRules.SOLO_LEVELING_DIFFICULTY_PRESET, List.of(0, 1, 2, 3));
      normalize(rules, SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER, List.of(10, 15, 20));
      clamp(rules, SololevelingModGameRules.SOLO_LEVELING_JOB_CHANGE_LEVEL, 20, 100);
      normalize(rules, SololevelingModGameRules.SOLO_LEVELING_ENEMY_SCALE, List.of(100, 125, 150));
      normalize(rules, SololevelingModGameRules.SOLO_LEVELING_BOSS_POWER, List.of(100, 125, 150));
      normalize(rules, SololevelingModGameRules.SOLO_LEVELING_DEATH_RULES, List.of(0, 1, 2));
   }

   private static void normalize(GameRules rules, Key<IntegerValue> key, List<Integer> allowed) {
      writeInteger(rules, key, closest(rules.getInt(key), allowed));
   }

   private static void clamp(GameRules rules, Key<IntegerValue> key, int minimum, int maximum) {
      writeInteger(rules, key, Mth.clamp(rules.getInt(key), minimum, maximum));
   }

   private static int closest(int value, List<Integer> allowed) {
      int closest = allowed.get(0);
      int distance = Math.abs(value - closest);

      for (int candidate : allowed) {
         int candidateDistance = Math.abs(value - candidate);
         if (candidateDistance < distance) {
            closest = candidate;
            distance = candidateDistance;
         }
      }

      return closest;
   }

   private static Component enemyScaleLabel(int value) {
      return Component.literal(switch (value) {
         case 125 -> "High";
         case 150 -> "Extreme";
         default -> "Standard";
      });
   }

   private static Component bossPowerLabel(int value) {
      return Component.literal(value == 100 ? "Standard" : "+" + (value - 100) + "%");
   }

   private static StringWidget heading(Font font, String label) {
      Component text = Component.literal(label).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
      return new StringWidget(152, 9, text, font).alignLeft();
   }

   private static StringWidget text(Font font, int width, String label, int color, boolean centered) {
      StringWidget widget = new StringWidget(width, 9, Component.literal(label), font).setColor(color);
      return centered ? widget.alignCenter() : widget.alignLeft();
   }

   private enum DeathRule {
      STANDARD(0, "Standard", "Vanilla inventory and experience loss."),
      FORGIVING(1, "Forgiving", "Keep inventory and vanilla experience after a Solo Leveling dungeon death."),
      HARSH(2, "Harsh", "Vanilla death rules, plus lose 25% Solo XP and 10% gold in Solo Leveling dungeons.");

      private final int id;
      private final String label;
      private final String tooltip;

      DeathRule(int id, String label, String tooltip) {
         this.id = id;
         this.label = label;
         this.tooltip = tooltip;
      }

      private static SoloLevelingWorldCreationTab.DeathRule fromId(int id) {
         for (SoloLevelingWorldCreationTab.DeathRule value : values()) {
            if (value.id == id) {
               return value;
            }
         }

         return STANDARD;
      }
   }

   private enum DifficultyPreset {
      NORMAL(0, "Normal", "Standard enemy power, boss power, and death rules."),
      HARD(1, "Hard", "Enemies and bosses gain 25% health and damage."),
      BRUTAL(2, "Brutal", "Enemies and bosses gain 50% power and Harsh dungeon deaths apply."),
      CUSTOM(3, "Custom", "Keeps the individual difficulty settings selected below.");

      private final int id;
      private final String label;
      private final String tooltip;

      DifficultyPreset(int id, String label, String tooltip) {
         this.id = id;
         this.label = label;
         this.tooltip = tooltip;
      }

      private static SoloLevelingWorldCreationTab.DifficultyPreset fromId(int id) {
         for (SoloLevelingWorldCreationTab.DifficultyPreset value : values()) {
            if (value.id == id) {
               return value;
            }
         }

         return NORMAL;
      }
   }

   private static final class JobChangeLevelSlider extends AbstractSliderButton {
      private static final int MINIMUM = 20;
      private static final int MAXIMUM = 100;
      private final IntConsumer changed;
      private int level;

      private JobChangeLevelSlider(int initial, IntConsumer changed) {
         super(0, 0, 152, 20, Component.empty(), normalized(initial));
         this.changed = changed;
         this.level = Mth.clamp(initial, 20, 100);
         this.value = normalized(this.level);
         this.updateMessage();
      }

      private void setLevel(int level) {
         this.level = Mth.clamp(level, 20, 100);
         this.value = normalized(this.level);
         this.updateMessage();
      }

      @Override
      protected void updateMessage() {
         this.setMessage(Component.literal("Job Change: Lv. " + this.level));
      }

      @Override
      protected void applyValue() {
         int selected = Mth.clamp((int)Math.round(20.0 + this.value * 80.0), 20, 100);
         this.level = selected;
         this.value = normalized(selected);
         this.updateMessage();
         this.changed.accept(selected);
      }

      private static double normalized(int level) {
         return (Mth.clamp(level, 20, 100) - 20) / 80.0;
      }
   }

   private enum ProgressionPreset {
      STANDARD(0, "Standard", "1.0x XP, Job Change at level 40, and ranked natural gates."),
      FAST(1, "Fast", "1.5x XP, Job Change at level 40, and all natural gate ranks open."),
      CUSTOM(2, "Custom", "Keeps the individual progression settings selected below.");

      private final int id;
      private final String label;
      private final String tooltip;

      ProgressionPreset(int id, String label, String tooltip) {
         this.id = id;
         this.label = label;
         this.tooltip = tooltip;
      }

      private static SoloLevelingWorldCreationTab.ProgressionPreset fromId(int id) {
         for (SoloLevelingWorldCreationTab.ProgressionPreset value : values()) {
            if (value.id == id) {
               return value;
            }
         }

         return STANDARD;
      }
   }
}
