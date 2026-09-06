package dev.eness.sololevelingfinal.core.init;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.GameRules.Key;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD)
public class SololevelingModGameRules {
   public static final Key<BooleanValue> SOLO_LEVELING_STORY_MODE = GameRules.register("soloLevelingStoryMode", Category.PLAYER, BooleanValue.create(false));
   public static final Key<IntegerValue> SOLO_LEVELING_PROGRESSION_PRESET = GameRules.register(
      "soloLevelingProgressionPreset", Category.PLAYER, IntegerValue.create(0)
   );
   public static final Key<IntegerValue> SOLO_LEVELING_DIFFICULTY_PRESET = GameRules.register(
      "soloLevelingDifficultyPreset", Category.MOBS, IntegerValue.create(0)
   );
   public static final Key<IntegerValue> SOLO_LEVELING_XP_MULTIPLIER = GameRules.register("soloLevelingXPMultiplier", Category.PLAYER, IntegerValue.create(10));
   public static final Key<IntegerValue> SOLO_LEVELING_JOB_CHANGE_LEVEL = GameRules.register(
      "soloLevelingJobChangeLevel", Category.PLAYER, IntegerValue.create(40)
   );
   public static final Key<IntegerValue> SOLO_LEVELING_ENEMY_SCALE = GameRules.register("soloLevelingEnemyScale", Category.MOBS, IntegerValue.create(100));
   public static final Key<IntegerValue> SOLO_LEVELING_BOSS_POWER = GameRules.register("soloLevelingBossPower", Category.MOBS, IntegerValue.create(100));
   public static final Key<BooleanValue> SOLO_LEVELING_RANKED_GATES = GameRules.register(
      "soloLevelingRankedGates", Category.SPAWNING, BooleanValue.create(true)
   );
   public static final Key<IntegerValue> SOLO_LEVELING_DEATH_RULES = GameRules.register("soloLevelingDeathRules", Category.PLAYER, IntegerValue.create(0));
   public static final Key<IntegerValue> SOLO_LEVELING_MONARCH_LIMIT = GameRules.register("soloLevelingMonarchLimit", Category.PLAYER, IntegerValue.create(1));
   public static final Key<IntegerValue> SOLO_LEVELING_JOB_CHANGE_POINTS = GameRules.register(
      "soloLevelingJobChangePoints", Category.PLAYER, IntegerValue.create(50)
   );
   public static final Key<BooleanValue> SOLO_LEVELING_LORE_ACCURATE_RANKS = GameRules.register(
      "soloLevelingLoreAccurateRanks", Category.PLAYER, BooleanValue.create(true)
   );
   public static final Key<IntegerValue> SOLO_LEVELING_RANK_ODDS_E = GameRules.register("soloLevelingRankOddsE", Category.PLAYER, IntegerValue.create(25));
   public static final Key<IntegerValue> SOLO_LEVELING_RANK_ODDS_D = GameRules.register("soloLevelingRankOddsD", Category.PLAYER, IntegerValue.create(25));
   public static final Key<IntegerValue> SOLO_LEVELING_RANK_ODDS_C = GameRules.register("soloLevelingRankOddsC", Category.PLAYER, IntegerValue.create(25));
   public static final Key<IntegerValue> SOLO_LEVELING_RANK_ODDS_B = GameRules.register("soloLevelingRankOddsB", Category.PLAYER, IntegerValue.create(12));
   public static final Key<IntegerValue> SOLO_LEVELING_RANK_ODDS_A = GameRules.register("soloLevelingRankOddsA", Category.PLAYER, IntegerValue.create(10));
   public static final Key<IntegerValue> SOLO_LEVELING_RANK_ODDS_S = GameRules.register("soloLevelingRankOddsS", Category.PLAYER, IntegerValue.create(3));
   public static final Key<BooleanValue> DISABLE_BLOCK_BREAKING = GameRules.register("disableBlockBreaking", Category.PLAYER, BooleanValue.create(false));
   public static final Key<BooleanValue> SOLO_DAILY_QUEST = GameRules.register("soloDailyQuest", Category.PLAYER, BooleanValue.create(true));
   public static final Key<BooleanValue> SOLO_DUNGEON_PROGRESSION_ONLY = GameRules.register(
      "soloDungeonProgressionOnly", Category.PLAYER, BooleanValue.create(true)
   );
   public static final Key<BooleanValue> SOLO_FATIGUE = GameRules.register("soloFatigue", Category.PLAYER, BooleanValue.create(true));
   public static final Key<BooleanValue> SOLO_ABILITY_DESTRUCTION = GameRules.register("soloAbilityDestruction", Category.PLAYER, BooleanValue.create(false));
   @Deprecated
   public static final Key<BooleanValue> SOLO_WORLD_GRIEFING = SOLO_ABILITY_DESTRUCTION;
   public static final Key<BooleanValue> SOLO_GATE_NOTIFICATION = GameRules.register("soloGateNotification", Category.MISC, BooleanValue.create(true));
   public static final Key<IntegerValue> SOLO_GATE_DELAY = GameRules.register("soloGateDelay", Category.SPAWNING, IntegerValue.create(400));
   public static final Key<BooleanValue> SOLO_GATE_SPAWNING = GameRules.register("soloGateSpawning", Category.SPAWNING, BooleanValue.create(true));
   public static final Key<BooleanValue> SOLO_MISC_ITEMS = GameRules.register("soloMiscItems", Category.MISC, BooleanValue.create(true));
   public static final Key<BooleanValue> SOLO_DUNGEON_BREAK = GameRules.register("soloDungeonBreak", Category.PLAYER, BooleanValue.create(true));
   public static final Key<BooleanValue> SOLO_BLOOD_EFFECTS = GameRules.register("soloBloodEffects", Category.PLAYER, BooleanValue.create(true));
   public static final Key<BooleanValue> SOLO_PUNISHMENT = GameRules.register("soloPunishment", Category.PLAYER, BooleanValue.create(true));
}
