package dev.eness.sololevelingfinal.core.util;

import java.util.List;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.GameRules.Key;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;

public final class HunterRankOdds {
   private static final List<Key<IntegerValue>> KEYS = List.of(
      SololevelingModGameRules.SOLO_LEVELING_RANK_ODDS_E,
      SololevelingModGameRules.SOLO_LEVELING_RANK_ODDS_D,
      SololevelingModGameRules.SOLO_LEVELING_RANK_ODDS_C,
      SololevelingModGameRules.SOLO_LEVELING_RANK_ODDS_B,
      SololevelingModGameRules.SOLO_LEVELING_RANK_ODDS_A,
      SololevelingModGameRules.SOLO_LEVELING_RANK_ODDS_S
   );

   private HunterRankOdds() {
   }

   public static int[] readWeights(GameRules rules) {
      int[] weights = new int[6];
      if (rules == null) {
         return (int[])HunterEvaluationRules.DEFAULT_RANK_ODDS.clone();
      }

      for (int index = 0; index < weights.length; index++) {
         weights[index] = Math.max(0, Math.min(100, rules.getInt(KEYS.get(index))));
      }

      return weights;
   }

   public static void writeWeights(GameRules rules, int[] weights) {
      if (rules != null && weights != null) {
         for (int index = 0; index < KEYS.size() && index < weights.length; index++) {
            int weight = Math.max(0, Math.min(100, weights[index]));
            rules.getRule(KEYS.get(index)).set(weight, null);
         }
      }
   }

   public static int[] normalized(GameRules rules) {
      return HunterEvaluationRules.normalizedRankOdds(readWeights(rules));
   }

   public static int[] normalized(Level level) {
      return normalized(level == null ? null : level.getGameRules());
   }

   public static int roll(Level level, int inclusiveRoll) {
      return HunterEvaluationRules.weightedRank(normalized(level), inclusiveRoll);
   }
}
